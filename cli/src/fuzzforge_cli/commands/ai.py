"""AI integration commands for the FuzzForge CLI."""
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


from __future__ import annotations

import asyncio
import os
from datetime import datetime
from typing import Optional

import typer
from rich.console import Console
from rich.panel import Panel
from rich.table import Table

from ..config import ProjectConfigManager

console = Console()
app = typer.Typer(name="ai", help="Interact with the FuzzForge AI system")


@app.command("agent")
def ai_agent() -> None:
    """Launch the full AI agent CLI with A2A orchestration."""
    console.print("[cyan]🤖 Opening Project FuzzForge AI Agent session[/cyan]\n")

    try:
        from fuzzforge_ai.cli import FuzzForgeCLI

        cli = FuzzForgeCLI()
        asyncio.run(cli.run())
    except ImportError as exc:
        console.print(f"[red]Failed to import AI CLI:[/red] {exc}")
        console.print("[dim]Ensure AI dependencies are installed (pip install -e .)[/dim]")
        raise typer.Exit(1) from exc
    except Exception as exc:  # pragma: no cover - runtime safety
        console.print(f"[red]Failed to launch AI agent:[/red] {exc}")
        console.print("[dim]Check that .env contains LITELLM_MODEL and API keys[/dim]")
        raise typer.Exit(1) from exc


# Memory + health commands
@app.command("status")
def ai_status() -> None:
    """Show AI system health and configuration."""
    try:
        status = asyncio.run(get_ai_status_async())
    except Exception as exc:  # pragma: no cover
        console.print(f"[red]Failed to get AI status:[/red] {exc}")
        raise typer.Exit(1) from exc

    console.print("[bold cyan]🤖 FuzzForge AI System Status[/bold cyan]\n")

    config_table = Table(title="Configuration", show_header=True, header_style="bold magenta")
    config_table.add_column("Setting", style="bold")
    config_table.add_column("Value", style="cyan")
    config_table.add_column("Status", style="green")

    for key, info in status["config"].items():
        status_icon = "✅" if info["configured"] else "❌"
        display_value = info["value"] if info["value"] else "-"
        config_table.add_row(key, display_value, f"{status_icon}")

    console.print(config_table)
    console.print()

    components_table = Table(title="AI Components", show_header=True, header_style="bold magenta")
    components_table.add_column("Component", style="bold")
    components_table.add_column("Status", style="green")
    components_table.add_column("Details", style="dim")

    for component, info in status["components"].items():
        status_icon = "🟢" if info["available"] else "🔴"
        components_table.add_row(component, status_icon, info["details"])

    console.print(components_table)

    if status["agents"]:
        console.print()
        console.print(f"[bold green]✓[/bold green] {len(status['agents'])} agents registered")


@app.command("server")
def ai_server(
    port: int = typer.Option(10100, "--port", "-p", help="Server port (default: 10100)"),
) -> None:
    """Start AI system as an A2A server."""
    console.print(f"[cyan]🚀 Starting FuzzForge AI Server on port {port}[/cyan]")
    console.print("[dim]Other agents can register this instance at the A2A endpoint[/dim]\n")

    try:
        os.environ["FUZZFORGE_PORT"] = str(port)
        from fuzzforge_ai.__main__ import main as start_server

        start_server()
    except Exception as exc:  # pragma: no cover
        console.print(f"[red]Failed to start AI server:[/red] {exc}")
        raise typer.Exit(1) from exc


# ---------------------------------------------------------------------------
# Helper functions (largely adapted from the OSS implementation)
# ---------------------------------------------------------------------------


@app.callback(invoke_without_command=True)
def ai_callback(ctx: typer.Context):
    """
    🤖 AI integration features
    """
    # Check if a subcommand is being invoked
    if ctx.invoked_subcommand is not None:
        # Let the subcommand handle it
        return

    # Show not implemented message for default command
    console.print("🚧 [yellow]AI command is not fully implemented yet.[/yellow]")
    console.print("Please use specific subcommands:")
    console.print("  • [cyan]ff ai agent[/cyan] - Launch the full AI agent CLI")
    console.print("  • [cyan]ff ai status[/cyan] - Show AI system health and configuration")
    console.print("  • [cyan]ff ai server[/cyan] - Start AI system as an A2A server")


