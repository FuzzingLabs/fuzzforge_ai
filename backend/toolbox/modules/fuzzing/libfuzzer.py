"""
LibFuzzer Fuzzing Module

This module uses LibFuzzer (LLVM's coverage-guided fuzzing engine) to find
bugs and security vulnerabilities in C/C++ code.
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
class LibFuzzerModule(BaseModule):
    """LibFuzzer coverage-guided fuzzing module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="libfuzzer",
            version="17.0.0",
            description="LLVM's coverage-guided fuzzing engine for finding bugs in C/C++ code",
            author="FuzzForge Team",
            category="fuzzing",
            tags=["coverage-guided", "c", "cpp", "llvm", "sanitizers", "memory-safety"],
            input_schema={
                "type": "object",
                "properties": {
                    "target_binary": {
                        "type": "string",
                        "description": "Path to the fuzz target binary (compiled with -fsanitize=fuzzer)"
                    },
                    "corpus_dir": {
                        "type": "string",
                        "description": "Directory containing initial corpus files"
                    },
                    "dict_file": {
                        "type": "string",
                        "description": "Dictionary file for fuzzing keywords"
                    },
                    "max_total_time": {
                        "type": "integer",
                        "default": 600,
                        "description": "Maximum total time to run fuzzing (seconds)"
                    },
                    "max_len": {
                        "type": "integer",
                        "default": 4096,
                        "description": "Maximum length of test input"
                    },
                    "timeout": {
                        "type": "integer",
                        "default": 25,
                        "description": "Timeout for individual test cases (seconds)"
                    },
                    "runs": {
                        "type": "integer",
                        "default": -1,
                        "description": "Number of individual test runs (-1 for unlimited)"
                    },
                    "jobs": {
                        "type": "integer",
                        "default": 1,
                        "description": "Number of fuzzing jobs to run in parallel"
                    },
                    "workers": {
                        "type": "integer",
                        "default": 1,
                        "description": "Number of workers for parallel fuzzing"
                    },
                    "reload": {
                        "type": "integer",
                        "default": 1,
                        "description": "Reload the main corpus periodically"
                    },
                    "print_final_stats": {
                        "type": "boolean",
                        "default": true,
                        "description": "Print final statistics"
                    },
                    "print_pcs": {
                        "type": "boolean",
                        "default": false,
                        "description": "Print newly covered PCs"
                    },
                    "print_funcs": {
                        "type": "boolean",
                        "default": false,
                        "description": "Print newly covered functions"
                    },
                    "print_coverage": {
                        "type": "boolean",
                        "default": true,
                        "description": "Print coverage information"
                    },
                    "shrink": {
                        "type": "boolean",
                        "default": true,
                        "description": "Try to shrink the corpus"
                    },
                    "reduce_inputs": {
                        "type": "boolean",
                        "default": true,
                        "description": "Try to reduce the size of inputs"
                    },
                    "use_value_profile": {
                        "type": "boolean",
                        "default": false,
                        "description": "Use value profile for fuzzing"
                    },
                    "sanitizers": {
                        "type": "array",
                        "items": {"type": "string", "enum": ["address", "memory", "undefined", "thread", "leak"]},
                        "default": ["address"],
                        "description": "Sanitizers to use during fuzzing"
                    },
                    "artifact_prefix": {
                        "type": "string",
                        "default": "crash-",
                        "description": "Prefix for artifact files"
                    },
                    "exact_artifact_path": {
                        "type": "string",
                        "description": "Exact path for artifact files"
                    },
                    "fork": {
                        "type": "integer",
                        "default": 0,
                        "description": "Fork mode (number of simultaneous processes)"
                    },
                    "ignore_crashes": {
                        "type": "boolean",
                        "default": false,
                        "description": "Ignore crashes and continue fuzzing"
                    },
                    "ignore_timeouts": {
                        "type": "boolean",
                        "default": false,
                        "description": "Ignore timeouts and continue fuzzing"
                    },
                    "ignore_ooms": {
                        "type": "boolean",
                        "default": false,
                        "description": "Ignore out-of-memory and continue fuzzing"
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
                                "crash_file": {"type": "string"},
                                "stack_trace": {"type": "string"},
                                "sanitizer": {"type": "string"}
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
            raise ValueError("target_binary is required for LibFuzzer")

        max_total_time = config.get("max_total_time", 600)
        if max_total_time <= 0:
            raise ValueError("max_total_time must be positive")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute LibFuzzer fuzzing"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info("Running LibFuzzer fuzzing campaign")

            # Check if target binary exists
            target_binary = workspace / config["target_binary"]
            if not target_binary.exists():
                raise FileNotFoundError(f"Target binary not found: {target_binary}")

            # Run LibFuzzer
            findings = await self._run_libfuzzer(target_binary, config, workspace)

            # Create summary
            summary = self._create_summary(findings)

            logger.info(f"LibFuzzer found {len(findings)} issues")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"LibFuzzer module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    async def _run_libfuzzer(self, target_binary: Path, config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Run LibFuzzer fuzzing"""
        findings = []

        try:
            # Create output directory for artifacts
            output_dir = workspace / "libfuzzer_output"
            output_dir.mkdir(exist_ok=True)

            # Build LibFuzzer command
            cmd = [str(target_binary)]

            # Add corpus directory
            corpus_dir = config.get("corpus_dir")
            if corpus_dir:
                corpus_path = workspace / corpus_dir
                if corpus_path.exists():
                    cmd.append(str(corpus_path))
                else:
                    logger.warning(f"Corpus directory not found: {corpus_path}")

            # Add dictionary file
            dict_file = config.get("dict_file")
            if dict_file:
                dict_path = workspace / dict_file
                if dict_path.exists():
                    cmd.append(f"-dict={dict_path}")

            # Add fuzzing parameters
            cmd.append(f"-max_total_time={config.get('max_total_time', 600)}")
            cmd.append(f"-max_len={config.get('max_len', 4096)}")
            cmd.append(f"-timeout={config.get('timeout', 25)}")
            cmd.append(f"-runs={config.get('runs', -1)}")

            if config.get("jobs", 1) > 1:
                cmd.append(f"-jobs={config['jobs']}")

            if config.get("workers", 1) > 1:
                cmd.append(f"-workers={config['workers']}")

            cmd.append(f"-reload={config.get('reload', 1)}")

            # Add output options
            if config.get("print_final_stats", True):
                cmd.append("-print_final_stats=1")

            if config.get("print_pcs", False):
                cmd.append("-print_pcs=1")

            if config.get("print_funcs", False):
                cmd.append("-print_funcs=1")

            if config.get("print_coverage", True):
                cmd.append("-print_coverage=1")

            # Add corpus management options
            if config.get("shrink", True):
                cmd.append("-shrink=1")

            if config.get("reduce_inputs", True):
                cmd.append("-reduce_inputs=1")

            if config.get("use_value_profile", False):
                cmd.append("-use_value_profile=1")

            # Add artifact options
            artifact_prefix = config.get("artifact_prefix", "crash-")
            cmd.append(f"-artifact_prefix={output_dir / artifact_prefix}")

            exact_artifact_path = config.get("exact_artifact_path")
            if exact_artifact_path:
                cmd.append(f"-exact_artifact_path={output_dir / exact_artifact_path}")

            # Add fork mode
            fork = config.get("fork", 0)
            if fork > 0:
                cmd.append(f"-fork={fork}")

            # Add ignore options
            if config.get("ignore_crashes", False):
                cmd.append("-ignore_crashes=1")

            if config.get("ignore_timeouts", False):
                cmd.append("-ignore_timeouts=1")

            if config.get("ignore_ooms", False):
                cmd.append("-ignore_ooms=1")

            # Set up environment for sanitizers
            env = os.environ.copy()
            sanitizers = config.get("sanitizers", ["address"])
            self._setup_sanitizer_environment(env, sanitizers)

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run LibFuzzer
            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace,
                env=env
            )

            stdout, stderr = await process.communicate()

            # Parse results
            findings = self._parse_libfuzzer_output(
                stdout.decode(), stderr.decode(), output_dir, workspace, sanitizers
            )

            # Look for crash files
            crash_findings = self._parse_crash_files(output_dir, workspace, sanitizers)
            findings.extend(crash_findings)

        except Exception as e:
            logger.warning(f"Error running LibFuzzer: {e}")

        return findings

    def _setup_sanitizer_environment(self, env: Dict[str, str], sanitizers: List[str]):
        """Set up environment variables for sanitizers"""
        if "address" in sanitizers:
            env["ASAN_OPTIONS"] = env.get("ASAN_OPTIONS", "") + ":halt_on_error=0:abort_on_error=1"

        if "memory" in sanitizers:
            env["MSAN_OPTIONS"] = env.get("MSAN_OPTIONS", "") + ":halt_on_error=0:abort_on_error=1"

        if "undefined" in sanitizers:
            env["UBSAN_OPTIONS"] = env.get("UBSAN_OPTIONS", "") + ":halt_on_error=0:abort_on_error=1"

        if "thread" in sanitizers:
            env["TSAN_OPTIONS"] = env.get("TSAN_OPTIONS", "") + ":halt_on_error=0:abort_on_error=1"

        if "leak" in sanitizers:
            env["LSAN_OPTIONS"] = env.get("LSAN_OPTIONS", "") + ":halt_on_error=0:abort_on_error=1"

    def _parse_libfuzzer_output(self, stdout: str, stderr: str, output_dir: Path, workspace: Path, sanitizers: List[str]) -> List[ModuleFinding]:
        """Parse LibFuzzer output for crashes and issues"""
        findings = []

        try:
            # Combine stdout and stderr for analysis
            full_output = stdout + "\n" + stderr

            # Look for crash indicators
            crash_patterns = [
                r"ERROR: AddressSanitizer: (.+)",
                r"ERROR: MemorySanitizer: (.+)",
                r"ERROR: UndefinedBehaviorSanitizer: (.+)",
                r"ERROR: ThreadSanitizer: (.+)",
                r"ERROR: LeakSanitizer: (.+)",
                r"SUMMARY: (.+Sanitizer): (.+)",
                r"==\d+==ERROR: libFuzzer: (.+)"
            ]

            for pattern in crash_patterns:
                matches = re.finditer(pattern, full_output, re.MULTILINE)
                for match in matches:
                    finding = self._create_crash_finding(
                        match, full_output, output_dir, sanitizers
                    )
                    if finding:
                        findings.append(finding)

            # Look for timeout and OOM issues
            if "TIMEOUT" in full_output:
                finding = self._create_timeout_finding(full_output, output_dir)
                if finding:
                    findings.append(finding)

            if "out-of-memory" in full_output.lower() or "oom" in full_output.lower():
                finding = self._create_oom_finding(full_output, output_dir)
                if finding:
                    findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing LibFuzzer output: {e}")

        return findings

    def _parse_crash_files(self, output_dir: Path, workspace: Path, sanitizers: List[str]) -> List[ModuleFinding]:
        """Parse crash artifact files"""
        findings = []

        try:
            # Look for crash files
            crash_patterns = ["crash-*", "leak-*", "timeout-*", "oom-*"]
            for pattern in crash_patterns:
                crash_files = list(output_dir.glob(pattern))
                for crash_file in crash_files:
                    finding = self._create_artifact_finding(crash_file, workspace, sanitizers)
                    if finding:
                        findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing crash files: {e}")

        return findings

    def _create_crash_finding(self, match, full_output: str, output_dir: Path, sanitizers: List[str]) -> ModuleFinding:
        """Create finding from crash match"""
        try:
            crash_type = match.group(1) if match.groups() else "Unknown crash"

            # Extract stack trace
            stack_trace = self._extract_stack_trace(full_output, match.start())

            # Determine sanitizer
            sanitizer = self._identify_sanitizer(match.group(0), sanitizers)

            # Determine severity based on crash type
            severity = self._get_crash_severity(crash_type, sanitizer)

            # Create finding
            finding = self.create_finding(
                title=f"LibFuzzer Crash: {crash_type}",
                description=f"LibFuzzer detected a crash with {sanitizer}: {crash_type}",
                severity=severity,
                category=self._get_crash_category(crash_type),
                file_path=None,  # LibFuzzer doesn't always provide specific files
                recommendation=self._get_crash_recommendation(crash_type, sanitizer),
                metadata={
                    "crash_type": crash_type,
                    "sanitizer": sanitizer,
                    "stack_trace": stack_trace[:2000] if stack_trace else "",  # Limit size
                    "fuzzer": "libfuzzer"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating crash finding: {e}")
            return None

    def _create_timeout_finding(self, output: str, output_dir: Path) -> ModuleFinding:
        """Create finding for timeout issues"""
        try:
            finding = self.create_finding(
                title="LibFuzzer Timeout",
                description="LibFuzzer detected a timeout during fuzzing, indicating potential infinite loop or performance issue",
                severity="medium",
                category="performance_issues",
                file_path=None,
                recommendation="Review the code for potential infinite loops, excessive computation, or blocking operations that could cause timeouts.",
                metadata={
                    "issue_type": "timeout",
                    "fuzzer": "libfuzzer"
                }
            )
            return finding

        except Exception as e:
            logger.warning(f"Error creating timeout finding: {e}")
            return None

    def _create_oom_finding(self, output: str, output_dir: Path) -> ModuleFinding:
        """Create finding for out-of-memory issues"""
        try:
            finding = self.create_finding(
                title="LibFuzzer Out-of-Memory",
                description="LibFuzzer detected an out-of-memory condition during fuzzing, indicating potential memory leak or excessive allocation",
                severity="medium",
                category="memory_management",
                file_path=None,
                recommendation="Review memory allocation patterns, check for memory leaks, and consider implementing proper bounds checking.",
                metadata={
                    "issue_type": "out_of_memory",
                    "fuzzer": "libfuzzer"
                }
            )
            return finding

        except Exception as e:
            logger.warning(f"Error creating OOM finding: {e}")
            return None

    def _create_artifact_finding(self, crash_file: Path, workspace: Path, sanitizers: List[str]) -> ModuleFinding:
        """Create finding from crash artifact file"""
        try:
            crash_type = crash_file.name.split('-')[0]  # e.g., "crash", "leak", "timeout"

            # Try to read crash file content (limited)
            crash_content = ""
            try:
                crash_content = crash_file.read_bytes()[:1000].decode('utf-8', errors='ignore')
            except Exception:
                pass

            # Determine severity
            severity = self._get_artifact_severity(crash_type)

            finding = self.create_finding(
                title=f"LibFuzzer Artifact: {crash_type}",
                description=f"LibFuzzer generated a {crash_type} artifact file indicating a potential issue",
                severity=severity,
                category=self._get_crash_category(crash_type),
                file_path=str(crash_file.relative_to(workspace)),
                recommendation=self._get_artifact_recommendation(crash_type),
                metadata={
                    "artifact_type": crash_type,
                    "artifact_file": str(crash_file.name),
                    "crash_content_preview": crash_content,
                    "fuzzer": "libfuzzer"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating artifact finding: {e}")
            return None

    def _extract_stack_trace(self, output: str, start_pos: int) -> str:
        """Extract stack trace from output"""
        try:
            lines = output[start_pos:].split('\n')
            stack_lines = []

            for line in lines[:50]:  # Limit to first 50 lines
                if any(indicator in line for indicator in ["#0", "#1", "#2", "at ", "in "]):
                    stack_lines.append(line.strip())
                elif stack_lines and not line.strip():
                    break

            return '\n'.join(stack_lines)

        except Exception:
            return ""

    def _identify_sanitizer(self, crash_line: str, sanitizers: List[str]) -> str:
        """Identify which sanitizer detected the issue"""
        crash_lower = crash_line.lower()

        if "addresssanitizer" in crash_lower:
            return "AddressSanitizer"
        elif "memorysanitizer" in crash_lower:
            return "MemorySanitizer"
        elif "undefinedbehaviorsanitizer" in crash_lower:
            return "UndefinedBehaviorSanitizer"
        elif "threadsanitizer" in crash_lower:
            return "ThreadSanitizer"
        elif "leaksanitizer" in crash_lower:
            return "LeakSanitizer"
        elif "libfuzzer" in crash_lower:
            return "LibFuzzer"
        else:
            return "Unknown"

    def _get_crash_severity(self, crash_type: str, sanitizer: str) -> str:
        """Determine severity based on crash type and sanitizer"""
        crash_lower = crash_type.lower()

        # Critical issues
        if any(term in crash_lower for term in ["heap-buffer-overflow", "stack-buffer-overflow", "use-after-free", "double-free"]):
            return "critical"

        # High severity issues
        elif any(term in crash_lower for term in ["heap-use-after-free", "stack-use-after-return", "global-buffer-overflow"]):
            return "high"

        # Medium severity issues
        elif any(term in crash_lower for term in ["uninitialized", "leak", "race", "deadlock"]):
            return "medium"

        # Default to high for any crash
        else:
            return "high"

    def _get_crash_category(self, crash_type: str) -> str:
        """Determine category based on crash type"""
        crash_lower = crash_type.lower()

        if any(term in crash_lower for term in ["buffer-overflow", "heap-buffer", "stack-buffer", "global-buffer"]):
            return "buffer_overflow"
        elif any(term in crash_lower for term in ["use-after-free", "double-free", "invalid-free"]):
            return "memory_corruption"
        elif any(term in crash_lower for term in ["uninitialized", "uninit"]):
            return "uninitialized_memory"
        elif any(term in crash_lower for term in ["leak"]):
            return "memory_leak"
        elif any(term in crash_lower for term in ["race", "data-race"]):
            return "race_condition"
        elif any(term in crash_lower for term in ["timeout"]):
            return "performance_issues"
        elif any(term in crash_lower for term in ["oom", "out-of-memory"]):
            return "memory_management"
        else:
            return "memory_safety"

    def _get_artifact_severity(self, artifact_type: str) -> str:
        """Determine severity for artifact types"""
        if artifact_type == "crash":
            return "high"
        elif artifact_type == "leak":
            return "medium"
        elif artifact_type in ["timeout", "oom"]:
            return "medium"
        else:
            return "low"

    def _get_crash_recommendation(self, crash_type: str, sanitizer: str) -> str:
        """Generate recommendation based on crash type"""
        crash_lower = crash_type.lower()

        if "buffer-overflow" in crash_lower:
            return "Fix buffer overflow by implementing proper bounds checking, using safe string functions, and validating array indices."
        elif "use-after-free" in crash_lower:
            return "Fix use-after-free by setting pointers to NULL after freeing, using smart pointers, or redesigning object lifetime management."
        elif "double-free" in crash_lower:
            return "Fix double-free by ensuring each allocation has exactly one corresponding free, or use RAII patterns."
        elif "uninitialized" in crash_lower:
            return "Initialize all variables before use and ensure proper constructor implementation."
        elif "leak" in crash_lower:
            return "Fix memory leak by ensuring all allocated memory is properly freed, use smart pointers, or implement proper cleanup routines."
        elif "race" in crash_lower:
            return "Fix data race by using proper synchronization mechanisms like mutexes, atomic operations, or lock-free data structures."
        else:
            return f"Address the {crash_type} issue detected by {sanitizer}. Review code for memory safety and proper resource management."

    def _get_artifact_recommendation(self, artifact_type: str) -> str:
        """Generate recommendation for artifact types"""
        if artifact_type == "crash":
            return "Analyze the crash artifact file to reproduce the issue and identify the root cause. Fix the underlying bug that caused the crash."
        elif artifact_type == "leak":
            return "Investigate the memory leak by analyzing allocation patterns and ensuring proper cleanup of resources."
        elif artifact_type == "timeout":
            return "Optimize code performance to prevent timeouts, check for infinite loops, and implement reasonable time limits."
        elif artifact_type == "oom":
            return "Reduce memory usage, implement proper memory management, and add bounds checking for allocations."
        else:
            return f"Analyze the {artifact_type} artifact to understand and fix the underlying issue."

    def _create_summary(self, findings: List[ModuleFinding]) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}
        sanitizer_counts = {}
        crash_type_counts = {}

        for finding in findings:
            # Count by severity
            severity_counts[finding.severity] += 1

            # Count by category
            category = finding.category
            category_counts[category] = category_counts.get(category, 0) + 1

            # Count by sanitizer
            sanitizer = finding.metadata.get("sanitizer", "unknown")
            sanitizer_counts[sanitizer] = sanitizer_counts.get(sanitizer, 0) + 1

            # Count by crash type
            crash_type = finding.metadata.get("crash_type", finding.metadata.get("issue_type", "unknown"))
            crash_type_counts[crash_type] = crash_type_counts.get(crash_type, 0) + 1

        return {
            "total_findings": len(findings),
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "sanitizer_counts": sanitizer_counts,
            "crash_type_counts": crash_type_counts,
            "memory_safety_issues": category_counts.get("memory_safety", 0) +
                                   category_counts.get("buffer_overflow", 0) +
                                   category_counts.get("memory_corruption", 0),
            "performance_issues": category_counts.get("performance_issues", 0)
        }