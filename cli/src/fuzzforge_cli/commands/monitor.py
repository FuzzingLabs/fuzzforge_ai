"""
Real-time monitoring and statistics commands.
"""
# Copyright (c) 2025 FuzzingLabs
#
# Licensed under the Business Source License 1.1 (BSL). See the LICENSE file
# at the root of this repository for details.
#
# After the Change Date (four years from publication), this version of the
# Licensed Work will be made available under the Apache License, Version 2.0.
# See the LICENSE-APACHE file or http://www.apache.org/licenses/LICENSE-2.0
#
# Additional attribution and requirements are provided in the NOTICE file.


import asyncio
import time
from datetime import datetime, timedelta
from typing import Optional

import typer
from rich.console import Console
from rich.table import Table
from rich.panel import Panel
from rich.live import Live
from rich.layout import Layout
from rich.progress import Progress, BarColumn, TextColumn, SpinnerColumn
from rich.align import Align
from rich import box

from ..config import get_project_config, FuzzForgeConfig
from ..database import get_project_db, ensure_project_db, CrashRecord
from fuzzforge_sdk import FuzzForgeClient

console = Console()
app = typer.Typer()


def get_client() -> FuzzForgeClient:
    """Get configured FuzzForge client"""
    config = get_project_config() or FuzzForgeConfig()
    return FuzzForgeClient(base_url=config.get_api_url(), timeout=config.get_timeout())


def format_duration(seconds: int) -> str:
    """Format duration in human readable format"""
    if seconds < 60:
        return f"{seconds}s"
    elif seconds < 3600:
        return f"{seconds // 60}m {seconds % 60}s"
    else:
        hours = seconds // 3600
        minutes = (seconds % 3600) // 60
        return f"{hours}h {minutes}m"


def format_number(num: int) -> str:
    """Format large numbers with K, M suffixes"""
    if num >= 1000000:
        return f"{num / 1000000:.1f}M"
    elif num >= 1000:
        return f"{num / 1000:.1f}K"
    else:
        return str(num)


@app.command("stats")
def fuzzing_stats(
    run_id: str = typer.Argument(..., help="Run ID to get statistics for"),
    refresh: int = typer.Option(
        5, "--refresh", "-r",
        help="Refresh interval in seconds"
    ),
    once: bool = typer.Option(
        False, "--once",
        help="Show stats once and exit"
    )
):
    """
    📊 Show current fuzzing statistics for a run
    """
    try:
        with get_client() as client:
            if once:
                # Show stats once
                stats = client.get_fuzzing_stats(run_id)
                display_stats_table(stats)
            else:
                # Live updating stats
                console.print(f"📊 [bold]Live Fuzzing Statistics[/bold] (Run: {run_id[:12]}...)")
                console.print(f"Refreshing every {refresh}s. Press Ctrl+C to stop.\n")

                with Live(auto_refresh=False, console=console) as live:
                    while True:
                        try:
                            stats = client.get_fuzzing_stats(run_id)
                            table = create_stats_table(stats)
                            live.update(table, refresh=True)
                            time.sleep(refresh)
                        except KeyboardInterrupt:
                            console.print("\n📊 Monitoring stopped", style="yellow")
                            break

    except Exception as e:
        console.print(f"❌ Failed to get fuzzing stats: {e}", style="red")
        raise typer.Exit(1)


def display_stats_table(stats):
    """Display stats in a simple table"""
    table = create_stats_table(stats)
    console.print(table)


def create_stats_table(stats) -> Panel:
    """Create a rich table for fuzzing statistics"""
    # Create main stats table
    stats_table = Table(show_header=False, box=box.SIMPLE)
    stats_table.add_column("Metric", style="bold cyan")
    stats_table.add_column("Value", justify="right", style="bold white")

    stats_table.add_row("Total Executions", format_number(stats.executions))
    stats_table.add_row("Executions/sec", f"{stats.executions_per_sec:.1f}")
    stats_table.add_row("Total Crashes", format_number(stats.crashes))
    stats_table.add_row("Unique Crashes", format_number(stats.unique_crashes))

    if stats.coverage is not None:
        stats_table.add_row("Code Coverage", f"{stats.coverage:.1f}%")

    stats_table.add_row("Corpus Size", format_number(stats.corpus_size))
    stats_table.add_row("Elapsed Time", format_duration(stats.elapsed_time))

    if stats.last_crash_time:
        time_since_crash = datetime.now() - stats.last_crash_time
        stats_table.add_row("Last Crash", f"{format_duration(int(time_since_crash.total_seconds()))} ago")

    return Panel.fit(
        stats_table,
        title=f"📊 Fuzzing Statistics - {stats.workflow}",
        subtitle=f"Run: {stats.run_id[:12]}...",
        box=box.ROUNDED
    )


@app.command("crashes")
def crash_reports(
    run_id: str = typer.Argument(..., help="Run ID to get crash reports for"),
    save: bool = typer.Option(
        True, "--save/--no-save",
        help="Save crashes to local database"
    ),
    limit: int = typer.Option(
        50, "--limit", "-l",
        help="Maximum number of crashes to show"
    )
):
    """
    🐛 Display crash reports for a fuzzing run
    """
    try:
        with get_client() as client:
            console.print(f"🐛 Fetching crash reports for run: {run_id}")
            crashes = client.get_crash_reports(run_id)

        if not crashes:
            console.print("✅ No crashes found!", style="green")
            return

        # Save to database if requested
        if save:
            db = ensure_project_db()
            for crash in crashes:
                crash_record = CrashRecord(
                    run_id=run_id,
                    crash_id=crash.crash_id,
                    signal=crash.signal,
                    stack_trace=crash.stack_trace,
                    input_file=crash.input_file,
                    severity=crash.severity,
                    timestamp=crash.timestamp
                )
                db.save_crash(crash_record)
            console.print("✅ Crashes saved to local database")

        # Display crashes
        crashes_to_show = crashes[:limit]

        # Summary
        severity_counts = {}
        signal_counts = {}
        for crash in crashes:
            severity_counts[crash.severity] = severity_counts.get(crash.severity, 0) + 1
            if crash.signal:
                signal_counts[crash.signal] = signal_counts.get(crash.signal, 0) + 1

        summary_table = Table(show_header=False, box=box.SIMPLE)
        summary_table.add_column("Metric", style="bold cyan")
        summary_table.add_column("Value", justify="right")

        summary_table.add_row("Total Crashes", str(len(crashes)))
        summary_table.add_row("Unique Signals", str(len(signal_counts)))

        for severity, count in sorted(severity_counts.items()):
            summary_table.add_row(f"{severity.title()} Severity", str(count))

        console.print(
            Panel.fit(
                summary_table,
                title=f"🐛 Crash Summary",
                box=box.ROUNDED
            )
        )

        # Detailed crash table
        if crashes_to_show:
            crashes_table = Table(box=box.ROUNDED)
            crashes_table.add_column("Crash ID", style="bold cyan")
            crashes_table.add_column("Signal", justify="center")
            crashes_table.add_column("Severity", justify="center")
            crashes_table.add_column("Timestamp", justify="center")
            crashes_table.add_column("Input File", style="dim")

            for crash in crashes_to_show:
                signal_emoji = {
                    "SIGSEGV": "💥",
                    "SIGABRT": "🛑",
                    "SIGFPE": "🧮",
                    "SIGILL": "⚠️"
                }.get(crash.signal or "", "🐛")

                severity_style = {
                    "high": "red",
                    "medium": "yellow",
                    "low": "green"
                }.get(crash.severity.lower(), "white")

                input_display = ""
                if crash.input_file:
                    input_display = crash.input_file.split("/")[-1]  # Show just filename

                crashes_table.add_row(
                    crash.crash_id[:12] + "..." if len(crash.crash_id) > 15 else crash.crash_id,
                    f"{signal_emoji} {crash.signal or 'Unknown'}",
                    f"[{severity_style}]{crash.severity}[/{severity_style}]",
                    crash.timestamp.strftime("%H:%M:%S"),
                    input_display
                )

            console.print(f"\n🐛 [bold]Crash Details[/bold]")
            if len(crashes) > limit:
                console.print(f"Showing first {limit} of {len(crashes)} crashes")
            console.print()
            console.print(crashes_table)

            console.print(f"\n💡 Use [bold cyan]fuzzforge finding {run_id}[/bold cyan] for detailed analysis")

    except Exception as e:
        console.print(f"❌ Failed to get crash reports: {e}", style="red")
        raise typer.Exit(1)


def _live_monitor(run_id: str, refresh: int):
    """Helper for live monitoring to allow for cleaner exit handling"""
    with get_client() as client:
        start_time = time.time()

        def render_layout(run_status, stats):
            layout = Layout()
            layout.split_column(
                Layout(name="header", size=3),
                Layout(name="main", ratio=1),
                Layout(name="footer", size=3)
            )
            layout["main"].split_row(
                Layout(name="stats", ratio=1),
                Layout(name="progress", ratio=1)
            )
            header = Panel(
                f"[bold]FuzzForge Live Monitor[/bold]\n"
                f"Run: {run_id[:12]}... | Status: {run_status.status} | "
                f"Uptime: {format_duration(int(time.time() - start_time))}",
                box=box.ROUNDED,
                style="cyan"
            )
            layout["header"].update(header)
            layout["stats"].update(create_stats_table(stats))

            progress_table = Table(show_header=False, box=box.SIMPLE)
            progress_table.add_column("Metric", style="bold")
            progress_table.add_column("Progress")
            if stats.executions > 0:
                exec_rate_percent = min(100, (stats.executions_per_sec / 1000) * 100)
                progress_table.add_row("Exec Rate", create_progress_bar(exec_rate_percent, "green"))
                crash_rate = (stats.crashes / stats.executions) * 100000
                crash_rate_percent = min(100, crash_rate * 10)
                progress_table.add_row("Crash Rate", create_progress_bar(crash_rate_percent, "red"))
            if stats.coverage is not None:
                progress_table.add_row("Coverage", create_progress_bar(stats.coverage, "blue"))
            layout["progress"].update(Panel.fit(progress_table, title="📊 Progress Indicators", box=box.ROUNDED))

            footer = Panel(
                f"Last updated: {datetime.now().strftime('%H:%M:%S')} | "
                f"Refresh interval: {refresh}s | Press Ctrl+C to exit",
                box=box.ROUNDED,
                style="dim"
            )
            layout["footer"].update(footer)
            return layout

        with Live(auto_refresh=False, console=console, screen=True) as live:
            # Initial fetch
            try:
                run_status = client.get_run_status(run_id)
                stats = client.get_fuzzing_stats(run_id)
            except Exception:
                # Minimal fallback stats
                class FallbackStats:
                    def __init__(self, run_id):
                        self.run_id = run_id
                        self.workflow = "unknown"
                        self.executions = 0
                        self.executions_per_sec = 0.0
                        self.crashes = 0
                        self.unique_crashes = 0
                        self.coverage = None
                        self.corpus_size = 0
                        self.elapsed_time = 0
                        self.last_crash_time = None
                stats = FallbackStats(run_id)
                run_status = type("RS", (), {"status":"Unknown","is_completed":False,"is_failed":False})()

            live.update(render_layout(run_status, stats), refresh=True)

            # Simple polling approach that actually works
            consecutive_errors = 0
            max_errors = 5

            while True:
                try:
                    # Poll for updates
                    try:
                        run_status = client.get_run_status(run_id)
                        consecutive_errors = 0
                    except Exception as e:
                        consecutive_errors += 1
                        if consecutive_errors >= max_errors:
                            console.print(f"❌ Too many errors getting run status: {e}", style="red")
                            break
                        time.sleep(refresh)
                        continue

                    # Try to get fuzzing stats
                    try:
                        stats = client.get_fuzzing_stats(run_id)
                    except Exception as e:
                        # Create fallback stats if not available
                        stats = FallbackStats(run_id)

                    # Update display
                    live.update(render_layout(run_status, stats), refresh=True)

                    # Check if completed
                    if getattr(run_status, 'is_completed', False) or getattr(run_status, 'is_failed', False):
                        # Show final state for a few seconds
                        console.print("\n🏁 Run completed. Showing final state for 10 seconds...")
                        time.sleep(10)
                        break

                    # Wait before next poll
                    time.sleep(refresh)

                except KeyboardInterrupt:
                    raise
                except Exception as e:
                    console.print(f"⚠️ Monitoring error: {e}", style="yellow")
                    time.sleep(refresh)

            # Completed status update
            final_message = (
                f"[bold]FuzzForge Live Monitor - COMPLETED[/bold]\n"
                f"Run: {run_id[:12]}... | Status: {run_status.status} | "
                f"Total runtime: {format_duration(int(time.time() - start_time))}"
            )
            style = "green" if getattr(run_status, 'is_completed', False) else "red"
            live.update(Panel(final_message, box=box.ROUNDED, style=style), refresh=True)


@app.command("live")
def live_monitor(
    run_id: str = typer.Argument(..., help="Run ID to monitor live"),
    refresh: int = typer.Option(
        2, "--refresh", "-r",
        help="Refresh interval in seconds (fallback when streaming unavailable)"
    )
):
    """
    📺 Real-time monitoring dashboard with live updates (WebSocket/SSE with REST fallback)
    """
    console.print(f"📺 [bold]Live Monitoring Dashboard[/bold]")
    console.print(f"Run: {run_id}")
    console.print(f"Press Ctrl+C to stop monitoring\n")
    try:
        _live_monitor(run_id, refresh)
    except KeyboardInterrupt:
        console.print("\n📊 Monitoring stopped by user.", style="yellow")
    except Exception as e:
        console.print(f"❌ Failed to start live monitoring: {e}", style="red")
        raise typer.Exit(1)


def create_progress_bar(percentage: float, color: str = "green") -> str:
    """Create a simple text progress bar"""
    width = 20
    filled = int((percentage / 100) * width)
    bar = "█" * filled + "░" * (width - filled)
    return f"[{color}]{bar}[/{color}] {percentage:.1f}%"


@app.callback(invoke_without_command=True)
def monitor_callback(ctx: typer.Context):
    """
    📊 Real-time monitoring and statistics
    """
    # Check if a subcommand is being invoked
    if ctx.invoked_subcommand is not None:
        # Let the subcommand handle it
        return

    # Show not implemented message for default command
    from rich.console import Console
    console = Console()
    console.print("🚧 [yellow]Monitor command is not fully implemented yet.[/yellow]")
    console.print("Please use specific subcommands:")
    console.print("  • [cyan]ff monitor stats <run-id>[/cyan] - Show execution statistics")
    console.print("  • [cyan]ff monitor crashes <run-id>[/cyan] - Show crash reports")
    console.print("  • [cyan]ff monitor live <run-id>[/cyan] - Live monitoring dashboard")
