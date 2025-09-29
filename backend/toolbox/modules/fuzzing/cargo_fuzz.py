"""
Cargo Fuzz Module

This module uses cargo-fuzz for fuzzing Rust code with libFuzzer integration.
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
import json
import os
from pathlib import Path
from typing import Dict, Any, List, Tuple
import subprocess
import logging
import httpx
import re
from datetime import datetime, timedelta

try:
    from prefect import get_run_context
except ImportError:
    # Fallback for when not running in Prefect context
    get_run_context = None

from ..base import BaseModule, ModuleMetadata, ModuleFinding, ModuleResult
from . import register_module

logger = logging.getLogger(__name__)


@register_module
class CargoFuzzModule(BaseModule):
    """Cargo Fuzz Rust fuzzing module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="cargo_fuzz",
            version="0.11.2",
            description="Rust fuzzing integration with libFuzzer using cargo-fuzz",
            author="FuzzForge Team",
            category="fuzzing",
            tags=["rust", "libfuzzer", "cargo", "coverage-guided", "sanitizers"],
            input_schema={
                "type": "object",
                "properties": {
                    "project_dir": {
                        "type": "string",
                        "description": "Path to Rust project directory (with Cargo.toml)"
                    },
                    "fuzz_target": {
                        "type": "string",
                        "description": "Name of the fuzz target to run"
                    },
                    "max_total_time": {
                        "type": "integer",
                        "default": 600,
                        "description": "Maximum total time to run fuzzing (seconds)"
                    },
                    "jobs": {
                        "type": "integer",
                        "default": 1,
                        "description": "Number of worker processes"
                    },
                    "corpus_dir": {
                        "type": "string",
                        "description": "Custom corpus directory"
                    },
                    "artifacts_dir": {
                        "type": "string",
                        "description": "Custom artifacts directory"
                    },
                    "sanitizer": {
                        "type": "string",
                        "enum": ["address", "memory", "thread", "leak", "none"],
                        "default": "address",
                        "description": "Sanitizer to use"
                    },
                    "release": {
                        "type": "boolean",
                        "default": False,
                        "description": "Use release mode"
                    },
                    "debug_assertions": {
                        "type": "boolean",
                        "default": True,
                        "description": "Enable debug assertions"
                    }
                }
            },
            output_schema={
                "type": "object",
                "properties": {
                    "findings": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "crash_type": {"type": "string"},
                                "artifact_path": {"type": "string"},
                                "stack_trace": {"type": "string"}
                            }
                        }
                    }
                }
            }
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate configuration"""
        project_dir = config.get("project_dir")
        if not project_dir:
            raise ValueError("project_dir is required")

        fuzz_target = config.get("fuzz_target")
        if not fuzz_target:
            raise ValueError("fuzz_target is required")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path, stats_callback=None) -> ModuleResult:
        """Execute cargo-fuzz fuzzing"""
        self.start_timer()

        try:
            # Initialize last observed stats for summary propagation
            self._last_stats = {
                'executions': 0,
                'executions_per_sec': 0.0,
                'crashes': 0,
                'corpus_size': 0,
                'elapsed_time': 0,
            }
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info("Running cargo-fuzz Rust fuzzing")

            # Check installation
            await self._check_cargo_fuzz_installation()

            # Setup project
            project_dir = workspace / config["project_dir"]
            await self._setup_cargo_fuzz_project(project_dir, config)

            # Run fuzzing
            findings = await self._run_cargo_fuzz(project_dir, config, workspace, stats_callback)

            # Create summary and enrich with last observed runtime stats
            summary = self._create_summary(findings)
            try:
                summary.update({
                    'executions': self._last_stats.get('executions', 0),
                    'executions_per_sec': self._last_stats.get('executions_per_sec', 0.0),
                    'corpus_size': self._last_stats.get('corpus_size', 0),
                    'crashes': self._last_stats.get('crashes', 0),
                    'elapsed_time': self._last_stats.get('elapsed_time', 0),
                })
            except Exception:
                pass

            logger.info(f"cargo-fuzz found {len(findings)} issues")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"cargo-fuzz module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    async def _check_cargo_fuzz_installation(self):
        """Check if cargo-fuzz is installed"""
        try:
            process = await asyncio.create_subprocess_exec(
                "cargo", "fuzz", "--version",
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            stdout, stderr = await process.communicate()

            if process.returncode != 0:
                raise RuntimeError("cargo-fuzz not installed. Install with: cargo install cargo-fuzz")

        except Exception as e:
            raise RuntimeError(f"cargo-fuzz installation check failed: {e}")

    async def _setup_cargo_fuzz_project(self, project_dir: Path, config: Dict[str, Any]):
        """Setup cargo-fuzz project"""
        if not project_dir.exists():
            raise FileNotFoundError(f"Project directory not found: {project_dir}")

        cargo_toml = project_dir / "Cargo.toml"
        if not cargo_toml.exists():
            raise FileNotFoundError(f"Cargo.toml not found in {project_dir}")

        # Check if fuzz directory exists, if not initialize
        fuzz_dir = project_dir / "fuzz"
        if not fuzz_dir.exists():
            logger.info("Initializing cargo-fuzz project")
            process = await asyncio.create_subprocess_exec(
                "cargo", "fuzz", "init",
                cwd=project_dir,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            await process.communicate()

    async def _run_cargo_fuzz(self, project_dir: Path, config: Dict[str, Any], workspace: Path, stats_callback=None) -> List[ModuleFinding]:
        """Run cargo-fuzz with real-time statistics reporting"""
        findings = []

        # Get run_id from Prefect context for statistics reporting
        run_id = None
        if get_run_context:
            try:
                context = get_run_context()
                run_id = str(context.flow_run.id)
            except Exception:
                logger.warning("Could not get run_id from Prefect context")

        try:
            # Build command
            cmd = ["cargo", "fuzz", "run", config["fuzz_target"]]

            # Add options
            if config.get("jobs", 1) > 1:
                cmd.extend(["--", f"-jobs={config['jobs']}"])

            max_time = config.get("max_total_time", 600)
            cmd.extend(["--", f"-max_total_time={max_time}"])

            # Set sanitizer
            sanitizer = config.get("sanitizer", "address")
            if sanitizer != "none":
                cmd.append(f"--sanitizer={sanitizer}")

            if config.get("release", False):
                cmd.append("--release")

            # Set environment
            env = os.environ.copy()
            if config.get("debug_assertions", True):
                env["RUSTFLAGS"] = env.get("RUSTFLAGS", "") + " -C debug-assertions=on"

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run with streaming output processing for real-time stats
            try:
                process = await asyncio.create_subprocess_exec(
                    *cmd,
                    stdout=asyncio.subprocess.PIPE,
                    stderr=asyncio.subprocess.STDOUT,  # Merge stderr into stdout
                    cwd=project_dir,
                    env=env
                )

                # Process output in real-time
                stdout_data, stderr_data = await self._process_streaming_output(
                    process, max_time, config, stats_callback
                )

                # Parse final results
                findings = self._parse_cargo_fuzz_output(
                    stdout_data, stderr_data, project_dir, workspace, config
                )

            except Exception as e:
                logger.warning(f"Error running cargo-fuzz: {e}")

        except Exception as e:
            logger.warning(f"Error in cargo-fuzz execution: {e}")

        return findings

    def _parse_cargo_fuzz_output(self, stdout: str, stderr: str, project_dir: Path, workspace: Path, config: Dict[str, Any]) -> List[ModuleFinding]:
        """Parse cargo-fuzz output"""
        findings = []

        try:
            full_output = stdout + "\n" + stderr

            # Look for crash artifacts
            artifacts_dir = project_dir / "fuzz" / "artifacts" / config["fuzz_target"]
            if artifacts_dir.exists():
                for artifact in artifacts_dir.iterdir():
                    if artifact.is_file():
                        finding = self._create_artifact_finding(artifact, workspace, full_output)
                        if finding:
                            findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing cargo-fuzz output: {e}")

        return findings

    def _create_artifact_finding(self, artifact_path: Path, workspace: Path, output: str) -> ModuleFinding:
        """Create finding from artifact file"""
        try:
            # Try to determine crash type from filename or content
            crash_type = "crash"
            if "leak" in artifact_path.name.lower():
                crash_type = "memory_leak"
            elif "timeout" in artifact_path.name.lower():
                crash_type = "timeout"

            # Extract stack trace from output
            stack_trace = self._extract_stack_trace_from_output(output, artifact_path.name)

            try:
                rel_path = artifact_path.relative_to(workspace)
                file_path = str(rel_path)
            except ValueError:
                file_path = str(artifact_path)

            severity = "high" if "crash" in crash_type else "medium"

            finding = self.create_finding(
                title=f"cargo-fuzz {crash_type.title()}",
                description=f"cargo-fuzz discovered a {crash_type} in the Rust code",
                severity=severity,
                category=self._get_crash_category(crash_type),
                file_path=file_path,
                recommendation=self._get_crash_recommendation(crash_type),
                metadata={
                    "crash_type": crash_type,
                    "artifact_path": str(artifact_path),
                    "stack_trace": stack_trace,
                    "fuzzer": "cargo_fuzz"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating artifact finding: {e}")
            return None

    def _extract_stack_trace_from_output(self, output: str, artifact_name: str) -> str:
        """Extract stack trace from output"""
        try:
            lines = output.split('\n')
            stack_lines = []
            in_stack = False

            for line in lines:
                if artifact_name in line or "stack backtrace:" in line.lower():
                    in_stack = True
                    continue

                if in_stack:
                    if line.strip() and ("at " in line or "::" in line or line.strip().startswith("0:")):
                        stack_lines.append(line.strip())
                    elif not line.strip() and stack_lines:
                        break

            return '\n'.join(stack_lines[:20])  # Limit stack trace size

        except Exception:
            return ""

    def _get_crash_category(self, crash_type: str) -> str:
        """Get category for crash type"""
        if "leak" in crash_type:
            return "memory_leak"
        elif "timeout" in crash_type:
            return "performance_issues"
        else:
            return "memory_safety"

    def _get_crash_recommendation(self, crash_type: str) -> str:
        """Get recommendation for crash type"""
        if "leak" in crash_type:
            return "Fix memory leak by ensuring proper cleanup of allocated resources. Review memory management patterns."
        elif "timeout" in crash_type:
            return "Fix timeout by optimizing performance, avoiding infinite loops, and implementing reasonable bounds."
        else:
            return "Fix the crash by analyzing the stack trace and addressing memory safety issues."

    async def _process_streaming_output(self, process, max_time: int, config: Dict[str, Any], stats_callback=None) -> Tuple[str, str]:
        """Process cargo-fuzz output in real-time and report statistics"""
        stdout_lines = []
        start_time = datetime.utcnow()
        last_update = start_time
        stats_data = {
            'executions': 0,
            'executions_per_sec': 0.0,
            'crashes': 0,
            'corpus_size': 0,
            'elapsed_time': 0
        }

        # Get run_id from Prefect context for statistics reporting
        run_id = None
        if get_run_context:
            try:
                context = get_run_context()
                run_id = str(context.flow_run.id)
            except Exception:
                logger.debug("Could not get run_id from Prefect context")

        try:
            # Emit an initial baseline update so dashboards show activity immediately
            try:
                await self._send_stats_via_callback(stats_callback, run_id, stats_data)
            except Exception:
                pass
            # Monitor process output in chunks to capture libFuzzer carriage-return updates
            buffer = ""
            while True:
                try:
                    chunk = await asyncio.wait_for(process.stdout.read(4096), timeout=1.0)
                    if not chunk:
                        # Process finished
                        break

                    buffer += chunk.decode('utf-8', errors='ignore')

                    # Split on both newline and carriage return
                    if "\n" in buffer or "\r" in buffer:
                        parts = re.split(r"[\r\n]", buffer)
                        buffer = parts[-1]
                        for part in parts[:-1]:
                            line = part.strip()
                            if not line:
                                continue
                            stdout_lines.append(line)
                            self._parse_stats_from_line(line, stats_data)

                except asyncio.TimeoutError:
                    # No output this second; continue to periodic update check
                    pass

                # Periodic update (even if there was no output)
                current_time = datetime.utcnow()
                stats_data['elapsed_time'] = int((current_time - start_time).total_seconds())
                if current_time - last_update >= timedelta(seconds=3):
                    try:
                        self._last_stats = dict(stats_data)
                    except Exception:
                        pass
                    await self._send_stats_via_callback(stats_callback, run_id, stats_data)
                    last_update = current_time

                # Check if max time exceeded
                if stats_data['elapsed_time'] >= max_time:
                    logger.info("Max time reached, terminating cargo-fuzz")
                    process.terminate()
                    break

            # Wait for process to complete
            await process.wait()

            # Send final stats update
            try:
                self._last_stats = dict(stats_data)
            except Exception:
                pass
            await self._send_stats_via_callback(stats_callback, run_id, stats_data)

        except Exception as e:
            logger.warning(f"Error processing streaming output: {e}")

        stdout_data = '\n'.join(stdout_lines)
        return stdout_data, ""

    def _parse_stats_from_line(self, line: str, stats_data: Dict[str, Any]):
        """Parse statistics from a cargo-fuzz output line"""
        try:
            # cargo-fuzz typically shows stats like:
            # "#12345: DONE    cov: 1234 ft: 5678 corp: 9/10Mb exec/s: 1500 rss: 234Mb"
            # "#12345: NEW     cov: 1234 ft: 5678 corp: 9/10Mb exec/s: 1500 rss: 234Mb L: 45/67 MS: 3 ..."

            # Extract execution count (the #number)
            exec_match = re.search(r'#(\d+)(?::)?', line)
            if exec_match:
                stats_data['executions'] = int(exec_match.group(1))
            else:
                # libFuzzer stats format alternative
                exec_alt = re.search(r'stat::number_of_executed_units:\s*(\d+)', line)
                if exec_alt:
                    stats_data['executions'] = int(exec_alt.group(1))
                else:
                    exec_alt2 = re.search(r'executed units:?\s*(\d+)', line, re.IGNORECASE)
                    if exec_alt2:
                        stats_data['executions'] = int(exec_alt2.group(1))

            # Extract executions per second
            exec_per_sec_match = re.search(r'exec/s:\s*([0-9\.]+)', line)
            if exec_per_sec_match:
                stats_data['executions_per_sec'] = float(exec_per_sec_match.group(1))
            else:
                eps_alt = re.search(r'stat::execs_per_sec:\s*([0-9\.]+)', line)
                if eps_alt:
                    stats_data['executions_per_sec'] = float(eps_alt.group(1))

            # Extract corpus size (corp: X/YMb)
            corp_match = re.search(r'corp(?:us)?:\s*(\d+)', line)
            if corp_match:
                stats_data['corpus_size'] = int(corp_match.group(1))

            # Look for crash indicators
            if any(keyword in line.lower() for keyword in ['crash', 'assert', 'panic', 'abort']):
                stats_data['crashes'] += 1

        except Exception as e:
            logger.debug(f"Error parsing stats from line '{line}': {e}")

    async def _send_stats_via_callback(self, stats_callback, run_id: str, stats_data: Dict[str, Any]):
        """Send statistics update via callback function"""
        if not stats_callback or not run_id:
            return

        try:
            # Prepare statistics payload
            stats_payload = {
                "run_id": run_id,
                "workflow": "language_fuzzing",
                "executions": stats_data['executions'],
                "executions_per_sec": stats_data['executions_per_sec'],
                "crashes": stats_data['crashes'],
                "unique_crashes": stats_data['crashes'],  # Assume all crashes are unique for now
                "corpus_size": stats_data['corpus_size'],
                "elapsed_time": stats_data['elapsed_time'],
                "timestamp": datetime.utcnow().isoformat()
            }

            # Call the callback function provided by the Prefect task
            await stats_callback(stats_payload)
            logger.info(
                "LIVE STATS SENT: exec=%s eps=%.2f crashes=%s corpus=%s elapsed=%s",
                stats_data['executions'],
                stats_data['executions_per_sec'],
                stats_data['crashes'],
                stats_data['corpus_size'],
                stats_data['elapsed_time'],
            )

        except Exception as e:
            logger.debug(f"Error sending stats via callback: {e}")

    def _create_summary(self, findings: List[ModuleFinding]) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}

        for finding in findings:
            severity_counts[finding.severity] += 1
            category_counts[finding.category] = category_counts.get(finding.category, 0) + 1

        return {
            "total_findings": len(findings),
            "severity_counts": severity_counts,
            "category_counts": category_counts
        }
