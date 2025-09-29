"""
AFL++ Fuzzing Module

This module uses AFL++ (Advanced American Fuzzy Lop) for coverage-guided
fuzzing with modern features and optimizations.
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
class AFLPlusPlusModule(BaseModule):
    """AFL++ advanced fuzzing module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="aflplusplus",
            version="4.09c",
            description="Advanced American Fuzzy Lop with modern features for coverage-guided fuzzing",
            author="FuzzForge Team",
            category="fuzzing",
            tags=["coverage-guided", "american-fuzzy-lop", "advanced", "mutation", "instrumentation"],
            input_schema={
                "type": "object",
                "properties": {
                    "target_binary": {
                        "type": "string",
                        "description": "Path to the target binary (compiled with afl-gcc/afl-clang)"
                    },
                    "input_dir": {
                        "type": "string",
                        "description": "Directory containing seed input files"
                    },
                    "output_dir": {
                        "type": "string",
                        "default": "afl_output",
                        "description": "Output directory for AFL++ results"
                    },
                    "dictionary": {
                        "type": "string",
                        "description": "Dictionary file for fuzzing keywords"
                    },
                    "timeout": {
                        "type": "integer",
                        "default": 1000,
                        "description": "Timeout for each execution (ms)"
                    },
                    "memory_limit": {
                        "type": "integer",
                        "default": 50,
                        "description": "Memory limit for child process (MB)"
                    },
                    "skip_deterministic": {
                        "type": "boolean",
                        "default": false,
                        "description": "Skip deterministic mutations"
                    },
                    "no_arith": {
                        "type": "boolean",
                        "default": false,
                        "description": "Skip arithmetic mutations"
                    },
                    "shuffle_queue": {
                        "type": "boolean",
                        "default": false,
                        "description": "Shuffle queue entries"
                    },
                    "max_total_time": {
                        "type": "integer",
                        "default": 3600,
                        "description": "Maximum total fuzzing time (seconds)"
                    },
                    "power_schedule": {
                        "type": "string",
                        "enum": ["explore", "fast", "coe", "lin", "quad", "exploit", "rare"],
                        "default": "fast",
                        "description": "Power schedule algorithm"
                    },
                    "mutation_mode": {
                        "type": "string",
                        "enum": ["default", "old", "mopt"],
                        "default": "default",
                        "description": "Mutation mode to use"
                    },
                    "parallel_fuzzing": {
                        "type": "boolean",
                        "default": false,
                        "description": "Enable parallel fuzzing with multiple instances"
                    },
                    "fuzzer_instances": {
                        "type": "integer",
                        "default": 1,
                        "description": "Number of parallel fuzzer instances"
                    },
                    "master_instance": {
                        "type": "string",
                        "default": "master",
                        "description": "Name for master fuzzer instance"
                    },
                    "slave_prefix": {
                        "type": "string",
                        "default": "slave",
                        "description": "Prefix for slave fuzzer instances"
                    },
                    "hang_timeout": {
                        "type": "integer",
                        "default": 1000,
                        "description": "Timeout for detecting hangs (ms)"
                    },
                    "crash_mode": {
                        "type": "boolean",
                        "default": false,
                        "description": "Run in crash exploration mode"
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
                    "ignore_finds": {
                        "type": "boolean",
                        "default": false,
                        "description": "Ignore existing findings and start fresh"
                    },
                    "force_deterministic": {
                        "type": "boolean",
                        "default": false,
                        "description": "Force deterministic mutations"
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
                                "crash_type": {"type": "string"},
                                "signal": {"type": "string"}
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
            raise ValueError("target_binary is required for AFL++")

        input_dir = config.get("input_dir")
        if not input_dir:
            raise ValueError("input_dir is required for AFL++")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute AFL++ fuzzing"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info("Running AFL++ fuzzing campaign")

            # Check prerequisites
            await self._check_afl_prerequisites(workspace)

            # Setup directories and files
            target_binary, input_dir, output_dir = self._setup_afl_directories(config, workspace)

            # Run AFL++ fuzzing
            findings = await self._run_afl_fuzzing(target_binary, input_dir, output_dir, config, workspace)

            # Create summary
            summary = self._create_summary(findings, output_dir)

            logger.info(f"AFL++ found {len(findings)} crashes")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"AFL++ module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    async def _check_afl_prerequisites(self, workspace: Path):
        """Check AFL++ prerequisites and system setup"""
        try:
            # Check if afl-fuzz exists
            process = await asyncio.create_subprocess_exec(
                "which", "afl-fuzz",
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            stdout, stderr = await process.communicate()

            if process.returncode != 0:
                raise RuntimeError("afl-fuzz not found. Please install AFL++")

            # Check core dump pattern (important for AFL)
            try:
                with open("/proc/sys/kernel/core_pattern", "r") as f:
                    core_pattern = f.read().strip()
                    if core_pattern != "core":
                        logger.warning(f"Core dump pattern is '{core_pattern}', AFL++ may not work optimally")
            except Exception:
                logger.warning("Could not check core dump pattern")

        except Exception as e:
            logger.warning(f"AFL++ prerequisite check failed: {e}")

    def _setup_afl_directories(self, config: Dict[str, Any], workspace: Path):
        """Setup AFL++ directories and validate files"""
        # Check target binary
        target_binary = workspace / config["target_binary"]
        if not target_binary.exists():
            raise FileNotFoundError(f"Target binary not found: {target_binary}")

        # Check input directory
        input_dir = workspace / config["input_dir"]
        if not input_dir.exists():
            raise FileNotFoundError(f"Input directory not found: {input_dir}")

        # Check if input directory has files
        input_files = list(input_dir.glob("*"))
        if not input_files:
            raise ValueError(f"Input directory is empty: {input_dir}")

        # Create output directory
        output_dir = workspace / config.get("output_dir", "afl_output")
        output_dir.mkdir(exist_ok=True)

        return target_binary, input_dir, output_dir

    async def _run_afl_fuzzing(self, target_binary: Path, input_dir: Path, output_dir: Path, config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Run AFL++ fuzzing"""
        findings = []

        try:
            if config.get("parallel_fuzzing", False):
                findings = await self._run_parallel_fuzzing(
                    target_binary, input_dir, output_dir, config, workspace
                )
            else:
                findings = await self._run_single_fuzzing(
                    target_binary, input_dir, output_dir, config, workspace
                )

        except Exception as e:
            logger.warning(f"Error running AFL++ fuzzing: {e}")

        return findings

    async def _run_single_fuzzing(self, target_binary: Path, input_dir: Path, output_dir: Path, config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Run single-instance AFL++ fuzzing"""
        findings = []

        try:
            # Build AFL++ command
            cmd = ["afl-fuzz"]

            # Add input and output directories
            cmd.extend(["-i", str(input_dir)])
            cmd.extend(["-o", str(output_dir)])

            # Add dictionary if specified
            dictionary = config.get("dictionary")
            if dictionary:
                dict_path = workspace / dictionary
                if dict_path.exists():
                    cmd.extend(["-x", str(dict_path)])

            # Add timeout
            timeout = config.get("timeout", 1000)
            cmd.extend(["-t", str(timeout)])

            # Add memory limit
            memory_limit = config.get("memory_limit", 50)
            cmd.extend(["-m", str(memory_limit)])

            # Add power schedule
            power_schedule = config.get("power_schedule", "fast")
            cmd.extend(["-p", power_schedule])

            # Add mutation options
            if config.get("skip_deterministic", False):
                cmd.append("-d")

            if config.get("no_arith", False):
                cmd.append("-a")

            if config.get("shuffle_queue", False):
                cmd.append("-Z")

            # Add hang timeout
            hang_timeout = config.get("hang_timeout", 1000)
            cmd.extend(["-T", str(hang_timeout)])

            # Add crash mode
            if config.get("crash_mode", False):
                cmd.append("-C")

            # Add ignore finds
            if config.get("ignore_finds", False):
                cmd.append("-f")

            # Add force deterministic
            if config.get("force_deterministic", False):
                cmd.append("-D")

            # Add target binary and arguments
            cmd.append("--")
            cmd.append(str(target_binary))

            target_args = config.get("target_args", [])
            cmd.extend(target_args)

            # Set up environment
            env = os.environ.copy()
            env_vars = config.get("env_vars", {})
            env.update(env_vars)

            # Set AFL environment variables
            env["AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES"] = "1"  # Avoid interactive prompts
            env["AFL_SKIP_CPUFREQ"] = "1"  # Skip CPU frequency checks

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run AFL++ with timeout
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
                except asyncio.TimeoutError:
                    logger.info(f"AFL++ fuzzing timed out after {max_total_time} seconds")
                    process.terminate()
                    try:
                        await asyncio.wait_for(process.wait(), timeout=10)
                    except asyncio.TimeoutError:
                        process.kill()
                        await process.wait()

                # Parse results from output directory
                findings = self._parse_afl_results(output_dir, workspace)

            except Exception as e:
                logger.warning(f"Error running AFL++ process: {e}")

        except Exception as e:
            logger.warning(f"Error in single fuzzing: {e}")

        return findings

    async def _run_parallel_fuzzing(self, target_binary: Path, input_dir: Path, output_dir: Path, config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Run parallel AFL++ fuzzing"""
        findings = []

        try:
            fuzzer_instances = config.get("fuzzer_instances", 2)
            master_name = config.get("master_instance", "master")
            slave_prefix = config.get("slave_prefix", "slave")

            processes = []

            # Start master instance
            master_cmd = await self._build_afl_command(
                target_binary, input_dir, output_dir, config, workspace,
                instance_name=master_name, is_master=True
            )

            master_process = await asyncio.create_subprocess_exec(
                *master_cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace,
                env=self._get_afl_env(config)
            )
            processes.append(master_process)

            # Start slave instances
            for i in range(1, fuzzer_instances):
                slave_name = f"{slave_prefix}{i:02d}"
                slave_cmd = await self._build_afl_command(
                    target_binary, input_dir, output_dir, config, workspace,
                    instance_name=slave_name, is_master=False
                )

                slave_process = await asyncio.create_subprocess_exec(
                    *slave_cmd,
                    stdout=asyncio.subprocess.PIPE,
                    stderr=asyncio.subprocess.PIPE,
                    cwd=workspace,
                    env=self._get_afl_env(config)
                )
                processes.append(slave_process)

            # Wait for specified time then terminate all
            max_total_time = config.get("max_total_time", 3600)

            try:
                await asyncio.sleep(max_total_time)
            finally:
                # Terminate all processes
                for process in processes:
                    if process.returncode is None:
                        process.terminate()
                        try:
                            await asyncio.wait_for(process.wait(), timeout=10)
                        except asyncio.TimeoutError:
                            process.kill()
                            await process.wait()

            # Parse results from output directory
            findings = self._parse_afl_results(output_dir, workspace)

        except Exception as e:
            logger.warning(f"Error in parallel fuzzing: {e}")

        return findings

    async def _build_afl_command(self, target_binary: Path, input_dir: Path, output_dir: Path, config: Dict[str, Any], workspace: Path, instance_name: str, is_master: bool) -> List[str]:
        """Build AFL++ command for a fuzzer instance"""
        cmd = ["afl-fuzz"]

        # Add input and output directories
        cmd.extend(["-i", str(input_dir)])
        cmd.extend(["-o", str(output_dir)])

        # Add instance name
        if is_master:
            cmd.extend(["-M", instance_name])
        else:
            cmd.extend(["-S", instance_name])

        # Add other options (same as single fuzzing)
        dictionary = config.get("dictionary")
        if dictionary:
            dict_path = workspace / dictionary
            if dict_path.exists():
                cmd.extend(["-x", str(dict_path)])

        cmd.extend(["-t", str(config.get("timeout", 1000))])
        cmd.extend(["-m", str(config.get("memory_limit", 50))])
        cmd.extend(["-p", config.get("power_schedule", "fast")])

        if config.get("skip_deterministic", False):
            cmd.append("-d")

        if config.get("no_arith", False):
            cmd.append("-a")

        # Add target
        cmd.append("--")
        cmd.append(str(target_binary))
        cmd.extend(config.get("target_args", []))

        return cmd

    def _get_afl_env(self, config: Dict[str, Any]) -> Dict[str, str]:
        """Get environment variables for AFL++"""
        env = os.environ.copy()
        env.update(config.get("env_vars", {}))
        env["AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES"] = "1"
        env["AFL_SKIP_CPUFREQ"] = "1"
        return env

    def _parse_afl_results(self, output_dir: Path, workspace: Path) -> List[ModuleFinding]:
        """Parse AFL++ results from output directory"""
        findings = []

        try:
            # Look for crashes directory
            crashes_dirs = []

            # Single instance
            crashes_dir = output_dir / "crashes"
            if crashes_dir.exists():
                crashes_dirs.append(crashes_dir)

            # Multiple instances
            for instance_dir in output_dir.iterdir():
                if instance_dir.is_dir():
                    instance_crashes = instance_dir / "crashes"
                    if instance_crashes.exists():
                        crashes_dirs.append(instance_crashes)

            # Process crash files
            for crashes_dir in crashes_dirs:
                crash_files = [f for f in crashes_dir.iterdir() if f.is_file() and f.name.startswith("id:")]

                for crash_file in crash_files:
                    finding = self._create_afl_crash_finding(crash_file, workspace)
                    if finding:
                        findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing AFL++ results: {e}")

        return findings

    def _create_afl_crash_finding(self, crash_file: Path, workspace: Path) -> ModuleFinding:
        """Create finding from AFL++ crash file"""
        try:
            # Parse crash filename for information
            filename = crash_file.name
            crash_info = self._parse_afl_filename(filename)

            # Try to read crash file (limited size)
            crash_content = ""
            try:
                crash_data = crash_file.read_bytes()[:1000]
                crash_content = crash_data.hex()[:200]  # Hex representation, limited
            except Exception:
                pass

            # Determine severity based on signal
            severity = self._get_crash_severity(crash_info.get("signal", ""))

            # Create relative path
            try:
                rel_path = crash_file.relative_to(workspace)
                file_path = str(rel_path)
            except ValueError:
                file_path = str(crash_file)

            finding = self.create_finding(
                title=f"AFL++ Crash: {crash_info.get('signal', 'Unknown')}",
                description=f"AFL++ discovered a crash with signal {crash_info.get('signal', 'unknown')} in the target program",
                severity=severity,
                category=self._get_crash_category(crash_info.get("signal", "")),
                file_path=file_path,
                recommendation=self._get_afl_crash_recommendation(crash_info.get("signal", "")),
                metadata={
                    "crash_id": crash_info.get("id", ""),
                    "signal": crash_info.get("signal", ""),
                    "src": crash_info.get("src", ""),
                    "crash_file": crash_file.name,
                    "crash_content_hex": crash_content,
                    "fuzzer": "afl++"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating AFL++ crash finding: {e}")
            return None

    def _parse_afl_filename(self, filename: str) -> Dict[str, str]:
        """Parse AFL++ crash filename for information"""
        info = {}

        try:
            # AFL++ crash filename format: id:XXXXXX,sig:XX,src:XXXXXX,op:XXX,rep:X
            parts = filename.split(',')

            for part in parts:
                if ':' in part:
                    key, value = part.split(':', 1)
                    info[key] = value

        except Exception:
            pass

        return info

    def _get_crash_severity(self, signal: str) -> str:
        """Determine severity based on crash signal"""
        if not signal:
            return "medium"

        signal_lower = signal.lower()

        # Critical signals indicating memory corruption
        if signal in ["11", "sigsegv", "segv"]:  # Segmentation fault
            return "critical"
        elif signal in ["6", "sigabrt", "abrt"]:  # Abort
            return "high"
        elif signal in ["4", "sigill", "ill"]:  # Illegal instruction
            return "high"
        elif signal in ["8", "sigfpe", "fpe"]:  # Floating point exception
            return "medium"
        elif signal in ["9", "sigkill", "kill"]:  # Kill signal
            return "medium"
        else:
            return "medium"

    def _get_crash_category(self, signal: str) -> str:
        """Determine category based on crash signal"""
        if not signal:
            return "program_crash"

        if signal in ["11", "sigsegv", "segv"]:
            return "memory_corruption"
        elif signal in ["6", "sigabrt", "abrt"]:
            return "assertion_failure"
        elif signal in ["4", "sigill", "ill"]:
            return "illegal_instruction"
        elif signal in ["8", "sigfpe", "fpe"]:
            return "arithmetic_error"
        else:
            return "program_crash"

    def _get_afl_crash_recommendation(self, signal: str) -> str:
        """Generate recommendation based on crash signal"""
        if signal in ["11", "sigsegv", "segv"]:
            return "Segmentation fault detected. Investigate memory access patterns, check for buffer overflows, null pointer dereferences, or use-after-free bugs."
        elif signal in ["6", "sigabrt", "abrt"]:
            return "Program abort detected. Check for assertion failures, memory allocation errors, or explicit abort() calls in the code."
        elif signal in ["4", "sigill", "ill"]:
            return "Illegal instruction detected. Check for code corruption, invalid function pointers, or architecture-specific instruction issues."
        elif signal in ["8", "sigfpe", "fpe"]:
            return "Floating point exception detected. Check for division by zero, arithmetic overflow, or invalid floating point operations."
        else:
            return f"Program crash with signal {signal} detected. Analyze the crash dump and input to identify the root cause."

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

        # Try to read AFL++ statistics
        stats = self._read_afl_stats(output_dir)

        return {
            "total_findings": len(findings),
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "signal_counts": signal_counts,
            "unique_crashes": len(set(f.metadata.get("crash_id", "") for f in findings)),
            "afl_stats": stats
        }

    def _read_afl_stats(self, output_dir: Path) -> Dict[str, Any]:
        """Read AFL++ fuzzer statistics"""
        stats = {}

        try:
            # Look for fuzzer_stats file in single or multiple instance setup
            stats_files = []

            # Single instance
            single_stats = output_dir / "fuzzer_stats"
            if single_stats.exists():
                stats_files.append(single_stats)

            # Multiple instances
            for instance_dir in output_dir.iterdir():
                if instance_dir.is_dir():
                    instance_stats = instance_dir / "fuzzer_stats"
                    if instance_stats.exists():
                        stats_files.append(instance_stats)

            # Read first stats file found
            if stats_files:
                with open(stats_files[0], 'r') as f:
                    for line in f:
                        if ':' in line:
                            key, value = line.strip().split(':', 1)
                            stats[key.strip()] = value.strip()

        except Exception as e:
            logger.warning(f"Error reading AFL++ stats: {e}")

        return stats