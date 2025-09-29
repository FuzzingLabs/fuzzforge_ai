"""
Atheris Fuzzing Module

This module uses Atheris for fuzzing Python code to find bugs and security
vulnerabilities in Python applications and libraries.
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
import sys
from pathlib import Path
from typing import Dict, Any, List
import subprocess
import logging
import traceback

from ..base import BaseModule, ModuleMetadata, ModuleFinding, ModuleResult
from . import register_module

logger = logging.getLogger(__name__)


@register_module
class AtherisModule(BaseModule):
    """Atheris Python fuzzing module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="atheris",
            version="2.3.0",
            description="Coverage-guided Python fuzzing engine for finding bugs in Python code",
            author="FuzzForge Team",
            category="fuzzing",
            tags=["python", "coverage-guided", "native", "sanitizers", "libfuzzer"],
            input_schema={
                "type": "object",
                "properties": {
                    "target_script": {
                        "type": "string",
                        "description": "Path to the Python script containing the fuzz target function"
                    },
                    "target_function": {
                        "type": "string",
                        "default": "TestOneInput",
                        "description": "Name of the target function to fuzz"
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
                    "print_coverage": {
                        "type": "boolean",
                        "default": true,
                        "description": "Print coverage information"
                    },
                    "artifact_prefix": {
                        "type": "string",
                        "default": "crash-",
                        "description": "Prefix for artifact files"
                    },
                    "seed": {
                        "type": "integer",
                        "description": "Random seed for reproducibility"
                    },
                    "python_path": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Additional Python paths to add to sys.path"
                    },
                    "enable_sanitizers": {
                        "type": "boolean",
                        "default": true,
                        "description": "Enable Python-specific sanitizers and checks"
                    },
                    "detect_leaks": {
                        "type": "boolean",
                        "default": true,
                        "description": "Detect memory leaks in native extensions"
                    },
                    "detect_stack_use_after_return": {
                        "type": "boolean",
                        "default": false,
                        "description": "Detect stack use-after-return"
                    },
                    "setup_code": {
                        "type": "string",
                        "description": "Python code to execute before fuzzing starts"
                    },
                    "enable_value_profile": {
                        "type": "boolean",
                        "default": false,
                        "description": "Enable value profiling for better mutation"
                    },
                    "shrink": {
                        "type": "boolean",
                        "default": true,
                        "description": "Try to shrink the corpus"
                    },
                    "only_ascii": {
                        "type": "boolean",
                        "default": false,
                        "description": "Only generate ASCII inputs"
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
                                "exception_type": {"type": "string"},
                                "exception_message": {"type": "string"},
                                "stack_trace": {"type": "string"},
                                "crash_input": {"type": "string"}
                            }
                        }
                    }
                }
            }
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate configuration"""
        target_script = config.get("target_script")
        if not target_script:
            raise ValueError("target_script is required for Atheris")

        max_total_time = config.get("max_total_time", 600)
        if max_total_time <= 0:
            raise ValueError("max_total_time must be positive")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute Atheris Python fuzzing"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info("Running Atheris Python fuzzing")

            # Check Atheris installation
            await self._check_atheris_installation()

            # Validate target script
            target_script = workspace / config["target_script"]
            if not target_script.exists():
                raise FileNotFoundError(f"Target script not found: {target_script}")

            # Run Atheris fuzzing
            findings = await self._run_atheris_fuzzing(target_script, config, workspace)

            # Create summary
            summary = self._create_summary(findings)

            logger.info(f"Atheris found {len(findings)} issues")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"Atheris module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    async def _check_atheris_installation(self):
        """Check if Atheris is installed"""
        try:
            process = await asyncio.create_subprocess_exec(
                sys.executable, "-c", "import atheris; print(atheris.__version__)",
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            stdout, stderr = await process.communicate()

            if process.returncode != 0:
                raise RuntimeError("Atheris not installed. Install with: pip install atheris")

            version = stdout.decode().strip()
            logger.info(f"Using Atheris version: {version}")

        except Exception as e:
            raise RuntimeError(f"Atheris installation check failed: {e}")

    async def _run_atheris_fuzzing(self, target_script: Path, config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Run Atheris fuzzing"""
        findings = []

        try:
            # Create output directory for artifacts
            output_dir = workspace / "atheris_output"
            output_dir.mkdir(exist_ok=True)

            # Create wrapper script for fuzzing
            wrapper_script = await self._create_atheris_wrapper(target_script, config, workspace, output_dir)

            # Build Atheris command
            cmd = [sys.executable, str(wrapper_script)]

            # Add corpus directory
            corpus_dir = config.get("corpus_dir")
            if corpus_dir:
                corpus_path = workspace / corpus_dir
                if corpus_path.exists():
                    cmd.append(str(corpus_path))

            # Set up environment
            env = self._setup_atheris_environment(config)

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run Atheris with timeout
            max_total_time = config.get("max_total_time", 600)

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
                    logger.info(f"Atheris fuzzing timed out after {max_total_time} seconds")
                    process.terminate()
                    try:
                        await asyncio.wait_for(process.wait(), timeout=10)
                    except asyncio.TimeoutError:
                        process.kill()
                        await process.wait()

                # Parse results
                findings = self._parse_atheris_output(
                    stdout.decode(), stderr.decode(), output_dir, workspace
                )

                # Look for crash files
                crash_findings = self._parse_crash_files(output_dir, workspace)
                findings.extend(crash_findings)

            except Exception as e:
                logger.warning(f"Error running Atheris process: {e}")

        except Exception as e:
            logger.warning(f"Error in Atheris fuzzing: {e}")

        return findings

    async def _create_atheris_wrapper(self, target_script: Path, config: Dict[str, Any], workspace: Path, output_dir: Path) -> Path:
        """Create wrapper script for Atheris fuzzing"""
        wrapper_path = workspace / "atheris_wrapper.py"

        wrapper_code = f'''#!/usr/bin/env python3
import sys
import os
import atheris
import traceback

# Add Python paths
python_paths = {config.get("python_path", [])}
for path in python_paths:
    if path not in sys.path:
        sys.path.insert(0, path)

# Add workspace to Python path
sys.path.insert(0, r"{workspace}")

# Setup code
setup_code = """{config.get("setup_code", "")}"""
if setup_code:
    exec(setup_code)

# Import target script
target_module_name = "{target_script.stem}"
sys.path.insert(0, r"{target_script.parent}")

try:
    target_module = __import__(target_module_name)
    target_function = getattr(target_module, "{config.get("target_function", "TestOneInput")}")
except Exception as e:
    print(f"Failed to import target: {{e}}")
    sys.exit(1)

# Wrapper function to catch exceptions
original_target = target_function

def wrapped_target(data):
    try:
        return original_target(data)
    except Exception as e:
        # Write crash information
        crash_info = {{
            "exception_type": type(e).__name__,
            "exception_message": str(e),
            "stack_trace": traceback.format_exc(),
            "input_data": data[:1000].hex() if isinstance(data, bytes) else str(data)[:1000]
        }}

        crash_file = r"{output_dir}" + "/crash_" + type(e).__name__ + ".txt"
        with open(crash_file, "a") as f:
            f.write(f"Exception: {{type(e).__name__}}\\n")
            f.write(f"Message: {{str(e)}}\\n")
            f.write(f"Stack trace:\\n{{traceback.format_exc()}}\\n")
            f.write(f"Input data (first 1000 chars/bytes): {{crash_info['input_data']}}\\n")
            f.write("-" * 80 + "\\n")

        # Re-raise to let Atheris handle it
        raise

if __name__ == "__main__":
    # Configure Atheris
    atheris.Setup(sys.argv, wrapped_target)

    # Set Atheris options
    options = []

    options.append(f"-max_total_time={{config.get('max_total_time', 600)}}")
    options.append(f"-max_len={{config.get('max_len', 4096)}}")
    options.append(f"-timeout={{config.get('timeout', 25)}}")
    options.append(f"-runs={{config.get('runs', -1)}}")

    if {config.get('jobs', 1)} > 1:
        options.append(f"-jobs={{config.get('jobs', 1)}}")

    if {config.get('print_final_stats', True)}:
        options.append("-print_final_stats=1")
    else:
        options.append("-print_final_stats=0")

    if {config.get('print_pcs', False)}:
        options.append("-print_pcs=1")

    if {config.get('print_coverage', True)}:
        options.append("-print_coverage=1")

    artifact_prefix = "{config.get('artifact_prefix', 'crash-')}"
    options.append(f"-artifact_prefix={{r'{output_dir}'}}/" + artifact_prefix)

    seed = {config.get('seed')}
    if seed is not None:
        options.append(f"-seed={{seed}}")

    if {config.get('enable_value_profile', False)}:
        options.append("-use_value_profile=1")

    if {config.get('shrink', True)}:
        options.append("-shrink=1")

    if {config.get('only_ascii', False)}:
        options.append("-only_ascii=1")

    dict_file = "{config.get('dict_file', '')}"
    if dict_file:
        dict_path = r"{workspace}" + "/" + dict_file
        if os.path.exists(dict_path):
            options.append(f"-dict={{dict_path}}")

    # Add options to sys.argv
    sys.argv.extend(options)

    # Start fuzzing
    atheris.Fuzz()
'''

        with open(wrapper_path, 'w') as f:
            f.write(wrapper_code)

        return wrapper_path

    def _setup_atheris_environment(self, config: Dict[str, Any]) -> Dict[str, str]:
        """Setup environment variables for Atheris"""
        env = os.environ.copy()

        # Enable sanitizers if requested
        if config.get("enable_sanitizers", True):
            env["ASAN_OPTIONS"] = env.get("ASAN_OPTIONS", "") + ":detect_leaks=1:halt_on_error=1"

        if config.get("detect_leaks", True):
            env["ASAN_OPTIONS"] = env.get("ASAN_OPTIONS", "") + ":detect_leaks=1"

        if config.get("detect_stack_use_after_return", False):
            env["ASAN_OPTIONS"] = env.get("ASAN_OPTIONS", "") + ":detect_stack_use_after_return=1"

        return env

    def _parse_atheris_output(self, stdout: str, stderr: str, output_dir: Path, workspace: Path) -> List[ModuleFinding]:
        """Parse Atheris output for crashes and issues"""
        findings = []

        try:
            # Combine stdout and stderr
            full_output = stdout + "\n" + stderr

            # Look for Python exceptions in output
            exception_patterns = [
                r"Traceback \(most recent call last\):(.*?)(?=\n\w|\nDONE|\n=|\Z)",
                r"Exception: (\w+).*?\nMessage: (.*?)\nStack trace:\n(.*?)(?=\n-{20,}|\Z)"
            ]

            for pattern in exception_patterns:
                import re
                matches = re.findall(pattern, full_output, re.DOTALL | re.MULTILINE)
                for match in matches:
                    finding = self._create_exception_finding(match, full_output, output_dir)
                    if finding:
                        findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing Atheris output: {e}")

        return findings

    def _parse_crash_files(self, output_dir: Path, workspace: Path) -> List[ModuleFinding]:
        """Parse crash files created by wrapper"""
        findings = []

        try:
            # Look for crash files
            crash_files = list(output_dir.glob("crash_*.txt"))

            for crash_file in crash_files:
                findings.extend(self._parse_crash_file(crash_file, workspace))

            # Also look for Atheris artifact files
            artifact_files = list(output_dir.glob("crash-*"))
            for artifact_file in artifact_files:
                finding = self._create_artifact_finding(artifact_file, workspace)
                if finding:
                    findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing crash files: {e}")

        return findings

    def _parse_crash_file(self, crash_file: Path, workspace: Path) -> List[ModuleFinding]:
        """Parse individual crash file"""
        findings = []

        try:
            content = crash_file.read_text()

            # Split by separator
            crash_entries = content.split("-" * 80)

            for entry in crash_entries:
                if not entry.strip():
                    continue

                finding = self._parse_crash_entry(entry, crash_file, workspace)
                if finding:
                    findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing crash file {crash_file}: {e}")

        return findings

    def _parse_crash_entry(self, entry: str, crash_file: Path, workspace: Path) -> ModuleFinding:
        """Parse individual crash entry"""
        try:
            lines = entry.strip().split('\n')

            exception_type = ""
            exception_message = ""
            stack_trace = ""
            input_data = ""

            current_section = None
            stack_lines = []

            for line in lines:
                if line.startswith("Exception: "):
                    exception_type = line.replace("Exception: ", "")
                elif line.startswith("Message: "):
                    exception_message = line.replace("Message: ", "")
                elif line.startswith("Stack trace:"):
                    current_section = "stack"
                elif line.startswith("Input data"):
                    current_section = "input"
                    input_data = line.split(":", 1)[1].strip() if ":" in line else ""
                elif current_section == "stack":
                    stack_lines.append(line)

            stack_trace = '\n'.join(stack_lines)

            if not exception_type:
                return None

            # Determine severity based on exception type
            severity = self._get_exception_severity(exception_type)

            # Create relative path
            try:
                rel_path = crash_file.relative_to(workspace)
                file_path = str(rel_path)
            except ValueError:
                file_path = str(crash_file)

            finding = self.create_finding(
                title=f"Atheris Exception: {exception_type}",
                description=f"Atheris discovered a Python exception: {exception_type}{': ' + exception_message if exception_message else ''}",
                severity=severity,
                category=self._get_exception_category(exception_type),
                file_path=file_path,
                recommendation=self._get_exception_recommendation(exception_type, exception_message),
                metadata={
                    "exception_type": exception_type,
                    "exception_message": exception_message,
                    "stack_trace": stack_trace[:2000] if stack_trace else "",  # Limit size
                    "crash_input_preview": input_data[:500] if input_data else "",
                    "fuzzer": "atheris"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error parsing crash entry: {e}")
            return None

    def _create_exception_finding(self, match, full_output: str, output_dir: Path) -> ModuleFinding:
        """Create finding from exception match"""
        try:
            if isinstance(match, tuple) and len(match) >= 1:
                # Handle different match formats
                if len(match) == 3:  # Exception format
                    exception_type, exception_message, stack_trace = match
                else:
                    stack_trace = match[0]
                    exception_type = "Unknown"
                    exception_message = ""
            else:
                stack_trace = str(match)
                exception_type = "Unknown"
                exception_message = ""

            # Try to extract exception type from stack trace
            if not exception_type or exception_type == "Unknown":
                lines = stack_trace.split('\n')
                for line in reversed(lines):
                    if ':' in line and any(exc in line for exc in ['Error', 'Exception', 'Warning']):
                        exception_type = line.split(':')[0].strip()
                        exception_message = line.split(':', 1)[1].strip() if ':' in line else ""
                        break

            severity = self._get_exception_severity(exception_type)

            finding = self.create_finding(
                title=f"Atheris Exception: {exception_type}",
                description=f"Atheris discovered a Python exception during fuzzing: {exception_type}",
                severity=severity,
                category=self._get_exception_category(exception_type),
                file_path=None,
                recommendation=self._get_exception_recommendation(exception_type, exception_message),
                metadata={
                    "exception_type": exception_type,
                    "exception_message": exception_message,
                    "stack_trace": stack_trace[:2000] if stack_trace else "",
                    "fuzzer": "atheris"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating exception finding: {e}")
            return None

    def _create_artifact_finding(self, artifact_file: Path, workspace: Path) -> ModuleFinding:
        """Create finding from Atheris artifact file"""
        try:
            # Try to read artifact content (limited)
            artifact_content = ""
            try:
                content_bytes = artifact_file.read_bytes()[:1000]
                artifact_content = content_bytes.hex()
            except Exception:
                pass

            # Create relative path
            try:
                rel_path = artifact_file.relative_to(workspace)
                file_path = str(rel_path)
            except ValueError:
                file_path = str(artifact_file)

            finding = self.create_finding(
                title="Atheris Crash Artifact",
                description=f"Atheris generated a crash artifact file: {artifact_file.name}",
                severity="medium",
                category="program_crash",
                file_path=file_path,
                recommendation="Analyze the crash artifact to reproduce and debug the issue. The artifact contains the input that caused the crash.",
                metadata={
                    "artifact_type": "crash",
                    "artifact_file": artifact_file.name,
                    "artifact_content_hex": artifact_content,
                    "fuzzer": "atheris"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating artifact finding: {e}")
            return None

    def _get_exception_severity(self, exception_type: str) -> str:
        """Determine severity based on exception type"""
        if not exception_type:
            return "medium"

        exception_lower = exception_type.lower()

        # Critical security issues
        if any(term in exception_lower for term in ["segmentationfault", "accessviolation", "memoryerror"]):
            return "critical"

        # High severity exceptions
        elif any(term in exception_lower for term in ["attributeerror", "typeerror", "indexerror", "keyerror", "valueerror"]):
            return "high"

        # Medium severity exceptions
        elif any(term in exception_lower for term in ["assertionerror", "runtimeerror", "ioerror", "oserror"]):
            return "medium"

        # Lower severity exceptions
        elif any(term in exception_lower for term in ["warning", "deprecation"]):
            return "low"

        else:
            return "medium"

    def _get_exception_category(self, exception_type: str) -> str:
        """Determine category based on exception type"""
        if not exception_type:
            return "python_exception"

        exception_lower = exception_type.lower()

        if any(term in exception_lower for term in ["memory", "segmentation", "access"]):
            return "memory_corruption"
        elif any(term in exception_lower for term in ["attribute", "type"]):
            return "type_error"
        elif any(term in exception_lower for term in ["index", "key", "value"]):
            return "data_error"
        elif any(term in exception_lower for term in ["io", "os", "file"]):
            return "io_error"
        elif any(term in exception_lower for term in ["assertion"]):
            return "assertion_failure"
        else:
            return "python_exception"

    def _get_exception_recommendation(self, exception_type: str, exception_message: str) -> str:
        """Generate recommendation based on exception type"""
        if not exception_type:
            return "Analyze the exception and fix the underlying code issue."

        exception_lower = exception_type.lower()

        if "attributeerror" in exception_lower:
            return "Fix AttributeError by ensuring objects have the expected attributes before accessing them. Add proper error handling and validation."
        elif "typeerror" in exception_lower:
            return "Fix TypeError by ensuring correct data types are used. Add type checking and validation for function parameters."
        elif "indexerror" in exception_lower:
            return "Fix IndexError by adding bounds checking before accessing list/array elements. Validate indices are within valid range."
        elif "keyerror" in exception_lower:
            return "Fix KeyError by checking if keys exist in dictionaries before accessing them. Use .get() method or proper key validation."
        elif "valueerror" in exception_lower:
            return "Fix ValueError by validating input values before processing. Add proper input sanitization and validation."
        elif "memoryerror" in exception_lower:
            return "Fix MemoryError by optimizing memory usage, processing data in chunks, or increasing available memory."
        elif "assertionerror" in exception_lower:
            return "Fix AssertionError by reviewing assertion conditions and ensuring they properly validate the expected state."
        else:
            return f"Fix the {exception_type} exception by analyzing the root cause and implementing appropriate error handling and validation."

    def _create_summary(self, findings: List[ModuleFinding]) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}
        exception_counts = {}

        for finding in findings:
            # Count by severity
            severity_counts[finding.severity] += 1

            # Count by category
            category = finding.category
            category_counts[category] = category_counts.get(category, 0) + 1

            # Count by exception type
            exception_type = finding.metadata.get("exception_type", "unknown")
            exception_counts[exception_type] = exception_counts.get(exception_type, 0) + 1

        return {
            "total_findings": len(findings),
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "exception_counts": exception_counts,
            "unique_exceptions": len(exception_counts),
            "python_specific_issues": sum(category_counts.get(cat, 0) for cat in ["type_error", "data_error", "python_exception"])
        }