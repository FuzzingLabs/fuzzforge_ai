"""
AFL-RS Fuzzing Module

This module uses AFL-RS (AFL in Rust) for high-performance coverage-guided fuzzing
with modern Rust implementations and optimizations.
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
from typing import Dict, Any, List
import subprocess
import logging
import re

from ..base import BaseModule, ModuleMetadata, ModuleFinding, ModuleResult
from . import register_module

logger = logging.getLogger(__name__)


@register_module
class AFLRSModule(BaseModule):
    """AFL-RS Rust-based fuzzing module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="aflrs",
            version="0.2.0",
            description="High-performance AFL implementation in Rust with modern fuzzing features",
            author="FuzzForge Team",
            category="fuzzing",
            tags=["coverage-guided", "rust", "afl", "high-performance", "modern"],
            input_schema={
                "type": "object",
                "properties": {
                    "target_binary": {
                        "type": "string",
                        "description": "Path to the target binary (compiled with AFL-RS instrumentation)"
                    },
                    "input_dir": {
                        "type": "string",
                        "description": "Directory containing seed input files"
                    },
                    "output_dir": {
                        "type": "string",
                        "default": "aflrs_output",
                        "description": "Output directory for AFL-RS results"
                    },
                    "dictionary": {
                        "type": "string",
                        "description": "Dictionary file for token-based mutations"
                    },
                    "timeout": {
                        "type": "integer",
                        "default": 1000,
                        "description": "Timeout for each execution (ms)"
                    },
                    "memory_limit": {
                        "type": "integer",
                        "default": 50,
                        "description": "Memory limit for target process (MB)"
                    },
                    "max_total_time": {
                        "type": "integer",
                        "default": 3600,
                        "description": "Maximum total fuzzing time (seconds)"
                    },
                    "cpu_cores": {
                        "type": "integer",
                        "default": 1,
                        "description": "Number of CPU cores to use"
                    },
                    "mutation_depth": {
                        "type": "integer",
                        "default": 4,
                        "description": "Maximum depth for cascaded mutations"
                    },
                    "skip_deterministic": {
                        "type": "boolean",
                        "default": false,
                        "description": "Skip deterministic mutations"
                    },
                    "power_schedule": {
                        "type": "string",
                        "enum": ["explore", "fast", "coe", "lin", "quad", "exploit", "rare", "mmopt", "seek"],
                        "default": "fast",
                        "description": "Power scheduling algorithm"
                    },
                    "custom_mutators": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Custom mutator libraries to load"
                    },
                    "cmplog": {
                        "type": "boolean",
                        "default": true,
                        "description": "Enable CmpLog for comparison logging"
                    },
                    "redqueen": {
                        "type": "boolean",
                        "default": true,
                        "description": "Enable RedQueen input-to-state correspondence"
                    },
                    "unicorn_mode": {
                        "type": "boolean",
                        "default": false,
                        "description": "Enable Unicorn mode for emulation"
                    },
                    "persistent_mode": {
                        "type": "boolean",
                        "default": false,
                        "description": "Enable persistent mode for faster execution"
                    },
                    "target_args": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Arguments to pass to target binary"
                    },
                    "env_vars": {
                        "type": "object",
                        "description": "Environment variables to set"
                    },
                    "ignore_timeouts": {
                        "type": "boolean",
                        "default": false,
                        "description": "Ignore timeout signals and continue fuzzing"
                    },
                    "ignore_crashes": {
                        "type": "boolean",
                        "default": false,
                        "description": "Ignore crashes and continue fuzzing"
                    },
                    "sync_dir": {
                        "type": "string",
                        "description": "Directory for syncing with other AFL instances"
                    },
                    "sync_id": {
                        "type": "string",
                        "description": "Fuzzer ID for syncing"
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
                                "crash_id": {"type": "string"},
                                "crash_file": {"type": "string"},
                                "signal": {"type": "string"},
                                "execution_time": {"type": "integer"}
                            }
                        }
                    }
                }
            }
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate configuration"""
        target_binary = config.get("target_binary")
        if not target_binary:
            raise ValueError("target_binary is required for AFL-RS")

        input_dir = config.get("input_dir")
        if not input_dir:
            raise ValueError("input_dir is required for AFL-RS")

        cpu_cores = config.get("cpu_cores", 1)
        if cpu_cores < 1:
            raise ValueError("cpu_cores must be at least 1")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute AFL-RS fuzzing"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info("Running AFL-RS fuzzing campaign")

            # Check AFL-RS installation
            await self._check_aflrs_installation()

            # Setup directories and files
            target_binary, input_dir, output_dir = self._setup_aflrs_directories(config, workspace)

            # Run AFL-RS fuzzing
            findings = await self._run_aflrs_fuzzing(target_binary, input_dir, output_dir, config, workspace)

            # Create summary
            summary = self._create_summary(findings, output_dir)

            logger.info(f"AFL-RS found {len(findings)} crashes")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"AFL-RS module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    async def _check_aflrs_installation(self):
        """Check if AFL-RS is installed and available"""
        try:
            # Check if aflrs is available (assuming aflrs binary)
            process = await asyncio.create_subprocess_exec(
                "which", "aflrs",
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            stdout, stderr = await process.communicate()

            if process.returncode != 0:
                # Try alternative AFL-RS command names
                alt_commands = ["afl-fuzz-rs", "afl-rs", "cargo-afl"]
                found = False

                for cmd in alt_commands:
                    process = await asyncio.create_subprocess_exec(
                        "which", cmd,
                        stdout=asyncio.subprocess.PIPE,
                        stderr=asyncio.subprocess.PIPE
                    )
                    stdout, stderr = await process.communicate()

                    if process.returncode == 0:
                        found = True
                        break

                if not found:
                    raise RuntimeError("AFL-RS not found. Please install AFL-RS or ensure it's in PATH")

        except Exception as e:
            logger.warning(f"AFL-RS installation check failed: {e}")

    def _setup_aflrs_directories(self, config: Dict[str, Any], workspace: Path):
        """Setup AFL-RS directories and validate files"""
        # Check target binary
        target_binary = workspace / config["target_binary"]
        if not target_binary.exists():
            raise FileNotFoundError(f"Target binary not found: {target_binary}")

        # Check input directory
        input_dir = workspace / config["input_dir"]
        if not input_dir.exists():
            raise FileNotFoundError(f"Input directory not found: {input_dir}")

        # Validate input files exist
        input_files = list(input_dir.glob("*"))
        if not input_files:
            raise ValueError(f"Input directory is empty: {input_dir}")

        # Create output directory
        output_dir = workspace / config.get("output_dir", "aflrs_output")
        output_dir.mkdir(exist_ok=True)

        return target_binary, input_dir, output_dir

    async def _run_aflrs_fuzzing(self, target_binary: Path, input_dir: Path, output_dir: Path, config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Run AFL-RS fuzzing"""
        findings = []

        try:
            # Build AFL-RS command
            cmd = await self._build_aflrs_command(target_binary, input_dir, output_dir, config, workspace)

            # Set up environment
            env = self._setup_aflrs_environment(config)

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run AFL-RS with timeout
            max_total_time = config.get("max_total_time", 3600)

            try:
                process = await asyncio.create_subprocess_exec(
                    *cmd,
                    stdout=asyncio.subprocess.PIPE,
                    stderr=asyncio.subprocess.PIPE,
                    cwd=workspace,
                    env=env
                )

                # Wait for specified time then terminate
                try:
                    stdout, stderr = await asyncio.wait_for(
                        process.communicate(), timeout=max_total_time
                    )
                    logger.info(f"AFL-RS completed after {max_total_time} seconds")
                except asyncio.TimeoutError:
                    logger.info(f"AFL-RS fuzzing timed out after {max_total_time} seconds, terminating")
                    process.terminate()
                    try:
                        await asyncio.wait_for(process.wait(), timeout=10)
                    except asyncio.TimeoutError:
                        process.kill()
                        await process.wait()

                # Parse results
                findings = self._parse_aflrs_results(output_dir, workspace)

            except Exception as e:
                logger.warning(f"Error running AFL-RS process: {e}")

        except Exception as e:
            logger.warning(f"Error in AFL-RS fuzzing: {e}")

        return findings

    async def _build_aflrs_command(self, target_binary: Path, input_dir: Path, output_dir: Path, config: Dict[str, Any], workspace: Path) -> List[str]:
        """Build AFL-RS command"""
        # Try to determine the correct AFL-RS command
        aflrs_cmd = "aflrs"  # Default

        # Try alternative command names
        alt_commands = ["aflrs", "afl-fuzz-rs", "afl-rs"]
        for cmd in alt_commands:
            try:
                process = await asyncio.create_subprocess_exec(
                    "which", cmd,
                    stdout=asyncio.subprocess.PIPE,
                    stderr=asyncio.subprocess.PIPE
                )
                stdout, stderr = await process.communicate()
                if process.returncode == 0:
                    aflrs_cmd = cmd
                    break
            except Exception:
                continue

        cmd = [aflrs_cmd]

        # Add input and output directories
        cmd.extend(["-i", str(input_dir)])
        cmd.extend(["-o", str(output_dir)])

        # Add dictionary if specified
        dictionary = config.get("dictionary")
        if dictionary:
            dict_path = workspace / dictionary
            if dict_path.exists():
                cmd.extend(["-x", str(dict_path)])

        # Add timeout and memory limit
        cmd.extend(["-t", str(config.get("timeout", 1000))])
        cmd.extend(["-m", str(config.get("memory_limit", 50))])

        # Add CPU cores
        cpu_cores = config.get("cpu_cores", 1)
        if cpu_cores > 1:
            cmd.extend(["-j", str(cpu_cores)])

        # Add mutation depth
        mutation_depth = config.get("mutation_depth", 4)
        cmd.extend(["-d", str(mutation_depth)])

        # Add power schedule
        power_schedule = config.get("power_schedule", "fast")
        cmd.extend(["-p", power_schedule])

        # Add skip deterministic
        if config.get("skip_deterministic", False):
            cmd.append("-D")

        # Add custom mutators
        custom_mutators = config.get("custom_mutators", [])
        for mutator in custom_mutators:
            cmd.extend(["-c", mutator])

        # Add advanced features
        if config.get("cmplog", True):
            cmd.append("-l")

        if config.get("redqueen", True):
            cmd.append("-I")

        if config.get("unicorn_mode", False):
            cmd.append("-U")

        if config.get("persistent_mode", False):
            cmd.append("-P")

        # Add ignore options
        if config.get("ignore_timeouts", False):
            cmd.append("-T")

        if config.get("ignore_crashes", False):
            cmd.append("-C")

        # Add sync options
        sync_dir = config.get("sync_dir")
        if sync_dir:
            cmd.extend(["-F", sync_dir])

        sync_id = config.get("sync_id")
        if sync_id:
            cmd.extend(["-S", sync_id])

        # Add target binary and arguments
        cmd.append("--")
        cmd.append(str(target_binary))

        target_args = config.get("target_args", [])
        cmd.extend(target_args)

        return cmd

    def _setup_aflrs_environment(self, config: Dict[str, Any]) -> Dict[str, str]:
        """Setup environment variables for AFL-RS"""
        env = os.environ.copy()

        # Add user-specified environment variables
        env_vars = config.get("env_vars", {})
        env.update(env_vars)

        # Set AFL-RS specific environment variables
        env["AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES"] = "1"
        env["AFL_SKIP_CPUFREQ"] = "1"

        # Enable advanced features if requested
        if config.get("cmplog", True):
            env["AFL_USE_CMPLOG"] = "1"

        if config.get("redqueen", True):
            env["AFL_USE_REDQUEEN"] = "1"

        return env

    def _parse_aflrs_results(self, output_dir: Path, workspace: Path) -> List[ModuleFinding]:
        """Parse AFL-RS results from output directory"""
        findings = []

        try:
            # Look for crashes directory
            crashes_dir = output_dir / "crashes"
            if not crashes_dir.exists():
                logger.info("No crashes directory found in AFL-RS output")
                return findings

            # Process crash files
            crash_files = [f for f in crashes_dir.iterdir() if f.is_file() and not f.name.startswith(".")]

            for crash_file in crash_files:
                finding = self._create_aflrs_crash_finding(crash_file, workspace)
                if finding:
                    findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing AFL-RS results: {e}")

        return findings

    def _create_aflrs_crash_finding(self, crash_file: Path, workspace: Path) -> ModuleFinding:
        """Create finding from AFL-RS crash file"""
        try:
            # Parse crash filename
            filename = crash_file.name
            crash_info = self._parse_aflrs_filename(filename)

            # Try to read crash file (limited size)
            crash_content = ""
            crash_size = 0
            try:
                crash_data = crash_file.read_bytes()
                crash_size = len(crash_data)
                # Store first 500 bytes as hex
                crash_content = crash_data[:500].hex()
            except Exception:
                pass

            # Determine severity based on signal or crash type
            signal = crash_info.get("signal", "")
            severity = self._get_crash_severity(signal)

            # Create relative path
            try:
                rel_path = crash_file.relative_to(workspace)
                file_path = str(rel_path)
            except ValueError:
                file_path = str(crash_file)

            finding = self.create_finding(
                title=f"AFL-RS Crash: {signal or 'Unknown Signal'}",
                description=f"AFL-RS discovered a crash in the target program{' with signal ' + signal if signal else ''}",
                severity=severity,
                category=self._get_crash_category(signal),
                file_path=file_path,
                recommendation=self._get_crash_recommendation(signal),
                metadata={
                    "crash_id": crash_info.get("id", ""),
                    "signal": signal,
                    "execution_time": crash_info.get("time", ""),
                    "crash_file": crash_file.name,
                    "crash_size": crash_size,
                    "crash_content_hex": crash_content,
                    "fuzzer": "aflrs"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating AFL-RS crash finding: {e}")
            return None

    def _parse_aflrs_filename(self, filename: str) -> Dict[str, str]:
        """Parse AFL-RS crash filename for information"""
        info = {}

        try:
            # AFL-RS may use similar format to AFL++
            # Example: id_000000_sig_11_src_000000_time_12345_op_havoc_rep_128
            parts = filename.replace("id:", "id_").replace("sig:", "sig_").replace("src:", "src_").replace("time:", "time_").replace("op:", "op_").replace("rep:", "rep_").split("_")

            i = 0
            while i < len(parts) - 1:
                if parts[i] in ["id", "sig", "src", "time", "op", "rep"]:
                    info[parts[i]] = parts[i + 1]
                    i += 2
                else:
                    i += 1

        except Exception:
            # Fallback: try to extract signal from filename
            signal_match = re.search(r'sig[_:]?(\d+)', filename)
            if signal_match:
                info["signal"] = signal_match.group(1)

        return info

    def _get_crash_severity(self, signal: str) -> str:
        """Determine crash severity based on signal"""
        if not signal:
            return "medium"

        try:
            sig_num = int(signal)
        except ValueError:
            return "medium"

        # Map common signals to severity
        if sig_num == 11:  # SIGSEGV
            return "critical"
        elif sig_num == 6:  # SIGABRT
            return "high"
        elif sig_num == 4:  # SIGILL
            return "high"
        elif sig_num == 8:  # SIGFPE
            return "medium"
        elif sig_num == 9:  # SIGKILL
            return "medium"
        else:
            return "medium"

    def _get_crash_category(self, signal: str) -> str:
        """Determine crash category based on signal"""
        if not signal:
            return "program_crash"

        try:
            sig_num = int(signal)
        except ValueError:
            return "program_crash"

        if sig_num == 11:  # SIGSEGV
            return "memory_corruption"
        elif sig_num == 6:  # SIGABRT
            return "assertion_failure"
        elif sig_num == 4:  # SIGILL
            return "illegal_instruction"
        elif sig_num == 8:  # SIGFPE
            return "arithmetic_error"
        else:
            return "program_crash"

    def _get_crash_recommendation(self, signal: str) -> str:
        """Generate recommendation based on crash signal"""
        if not signal:
            return "Analyze the crash input to reproduce and debug the issue."

        try:
            sig_num = int(signal)
        except ValueError:
            return "Analyze the crash input to reproduce and debug the issue."

        if sig_num == 11:  # SIGSEGV
            return "Segmentation fault detected. Check for buffer overflows, null pointer dereferences, use-after-free, or invalid memory access patterns."
        elif sig_num == 6:  # SIGABRT
            return "Program abort detected. Check for assertion failures, memory corruption detected by allocator, or explicit abort calls."
        elif sig_num == 4:  # SIGILL
            return "Illegal instruction detected. Check for code corruption, invalid function pointers, or architecture-specific issues."
        elif sig_num == 8:  # SIGFPE
            return "Floating point exception detected. Check for division by zero, arithmetic overflow, or invalid floating point operations."
        else:
            return f"Program terminated with signal {signal}. Analyze the crash input and use debugging tools to identify the root cause."

    def _create_summary(self, findings: List[ModuleFinding], output_dir: Path) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}
        signal_counts = {}

        for finding in findings:
            # Count by severity
            severity_counts[finding.severity] += 1

            # Count by category
            category = finding.category
            category_counts[category] = category_counts.get(category, 0) + 1

            # Count by signal
            signal = finding.metadata.get("signal", "unknown")
            signal_counts[signal] = signal_counts.get(signal, 0) + 1

        # Try to read AFL-RS statistics
        stats = self._read_aflrs_stats(output_dir)

        return {
            "total_findings": len(findings),
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "signal_counts": signal_counts,
            "unique_crashes": len(set(f.metadata.get("crash_id", "") for f in findings)),
            "aflrs_stats": stats
        }

    def _read_aflrs_stats(self, output_dir: Path) -> Dict[str, Any]:
        """Read AFL-RS fuzzer statistics"""
        stats = {}

        try:
            # Look for AFL-RS stats file
            stats_file = output_dir / "fuzzer_stats"
            if stats_file.exists():
                with open(stats_file, 'r') as f:
                    for line in f:
                        if ':' in line:
                            key, value = line.strip().split(':', 1)
                            stats[key.strip()] = value.strip()

            # Also look for AFL-RS specific files
            plot_data = output_dir / "plot_data"
            if plot_data.exists():
                stats["plot_data_available"] = True

        except Exception as e:
            logger.warning(f"Error reading AFL-RS stats: {e}")

        return stats