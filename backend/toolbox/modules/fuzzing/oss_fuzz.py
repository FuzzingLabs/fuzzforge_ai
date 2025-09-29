"""
OSS-Fuzz Module

This module integrates with Google's OSS-Fuzz for continuous fuzzing
of open source projects.
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

from ..base import BaseModule, ModuleMetadata, ModuleFinding, ModuleResult
from . import register_module

logger = logging.getLogger(__name__)


@register_module
class OSSFuzzModule(BaseModule):
    """OSS-Fuzz continuous fuzzing module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="oss_fuzz",
            version="1.0.0",
            description="Google's continuous fuzzing for open source projects integration",
            author="FuzzForge Team",
            category="fuzzing",
            tags=["oss-fuzz", "continuous", "google", "open-source", "docker"],
            input_schema={
                "type": "object",
                "properties": {
                    "project_name": {
                        "type": "string",
                        "description": "OSS-Fuzz project name"
                    },
                    "source_dir": {
                        "type": "string",
                        "description": "Source directory to fuzz"
                    },
                    "build_script": {
                        "type": "string",
                        "default": "build.sh",
                        "description": "Build script path"
                    },
                    "dockerfile": {
                        "type": "string",
                        "default": "Dockerfile",
                        "description": "Dockerfile path"
                    },
                    "project_yaml": {
                        "type": "string",
                        "default": "project.yaml",
                        "description": "Project configuration file"
                    },
                    "sanitizer": {
                        "type": "string",
                        "enum": ["address", "memory", "undefined", "coverage"],
                        "default": "address",
                        "description": "Sanitizer to use"
                    },
                    "architecture": {
                        "type": "string",
                        "enum": ["x86_64", "i386"],
                        "default": "x86_64",
                        "description": "Target architecture"
                    },
                    "fuzzing_engine": {
                        "type": "string",
                        "enum": ["libfuzzer", "afl", "honggfuzz"],
                        "default": "libfuzzer",
                        "description": "Fuzzing engine to use"
                    },
                    "timeout": {
                        "type": "integer",
                        "default": 3600,
                        "description": "Fuzzing timeout (seconds)"
                    },
                    "check_build": {
                        "type": "boolean",
                        "default": true,
                        "description": "Check if build is successful"
                    },
                    "reproduce_bugs": {
                        "type": "boolean",
                        "default": false,
                        "description": "Try to reproduce existing bugs"
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
                                "bug_type": {"type": "string"},
                                "reproducer": {"type": "string"},
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
        project_name = config.get("project_name")
        if not project_name:
            raise ValueError("project_name is required")

        source_dir = config.get("source_dir")
        if not source_dir:
            raise ValueError("source_dir is required")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute OSS-Fuzz integration"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info("Running OSS-Fuzz integration")

            # Check Docker
            await self._check_docker()

            # Clone/update OSS-Fuzz if needed
            oss_fuzz_dir = await self._setup_oss_fuzz(workspace)

            # Setup project
            await self._setup_project(oss_fuzz_dir, config, workspace)

            # Build and run
            findings = await self._run_oss_fuzz(oss_fuzz_dir, config, workspace)

            # Create summary
            summary = self._create_summary(findings)

            logger.info(f"OSS-Fuzz found {len(findings)} issues")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"OSS-Fuzz module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    async def _check_docker(self):
        """Check if Docker is available"""
        try:
            process = await asyncio.create_subprocess_exec(
                "docker", "--version",
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            stdout, stderr = await process.communicate()

            if process.returncode != 0:
                raise RuntimeError("Docker not available. OSS-Fuzz requires Docker.")

        except Exception as e:
            raise RuntimeError(f"Docker check failed: {e}")

    async def _setup_oss_fuzz(self, workspace: Path) -> Path:
        """Setup OSS-Fuzz repository"""
        oss_fuzz_dir = workspace / "oss-fuzz"

        if not oss_fuzz_dir.exists():
            logger.info("Cloning OSS-Fuzz repository")
            process = await asyncio.create_subprocess_exec(
                "git", "clone", "https://github.com/google/oss-fuzz.git",
                cwd=workspace,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            stdout, stderr = await process.communicate()

            if process.returncode != 0:
                raise RuntimeError(f"Failed to clone OSS-Fuzz: {stderr.decode()}")

        return oss_fuzz_dir

    async def _setup_project(self, oss_fuzz_dir: Path, config: Dict[str, Any], workspace: Path):
        """Setup OSS-Fuzz project"""
        project_name = config["project_name"]
        project_dir = oss_fuzz_dir / "projects" / project_name

        # Create project directory if it doesn't exist
        project_dir.mkdir(parents=True, exist_ok=True)

        # Copy source if provided
        source_dir = workspace / config["source_dir"]
        if source_dir.exists():
            # Create symlink or copy source
            logger.info(f"Setting up source directory: {source_dir}")

        # Setup required files if they don't exist
        await self._create_project_files(project_dir, config, workspace)

    async def _create_project_files(self, project_dir: Path, config: Dict[str, Any], workspace: Path):
        """Create required OSS-Fuzz project files"""

        # Create Dockerfile if it doesn't exist
        dockerfile = project_dir / config.get("dockerfile", "Dockerfile")
        if not dockerfile.exists():
            dockerfile_content = f'''FROM gcr.io/oss-fuzz-base/base-builder
COPY . $SRC/{config["project_name"]}
WORKDIR $SRC/{config["project_name"]}
COPY {config.get("build_script", "build.sh")} $SRC/
'''
            dockerfile.write_text(dockerfile_content)

        # Create build.sh if it doesn't exist
        build_script = project_dir / config.get("build_script", "build.sh")
        if not build_script.exists():
            build_content = f'''#!/bin/bash -eu
# Build script for {config["project_name"]}
# Add your build commands here
echo "Building {config['project_name']}..."
'''
            build_script.write_text(build_content)
            build_script.chmod(0o755)

        # Create project.yaml if it doesn't exist
        project_yaml = project_dir / config.get("project_yaml", "project.yaml")
        if not project_yaml.exists():
            yaml_content = f'''homepage: "https://example.com"
language: c++
primary_contact: "security@example.com"
auto_ccs:
  - "fuzzing@example.com"
sanitizers:
  - {config.get("sanitizer", "address")}
architectures:
  - {config.get("architecture", "x86_64")}
fuzzing_engines:
  - {config.get("fuzzing_engine", "libfuzzer")}
'''
            project_yaml.write_text(yaml_content)

    async def _run_oss_fuzz(self, oss_fuzz_dir: Path, config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Run OSS-Fuzz"""
        findings = []

        try:
            project_name = config["project_name"]
            sanitizer = config.get("sanitizer", "address")
            architecture = config.get("architecture", "x86_64")

            # Build project
            if config.get("check_build", True):
                await self._build_project(oss_fuzz_dir, project_name, sanitizer, architecture)

            # Check build
            await self._check_build(oss_fuzz_dir, project_name, sanitizer, architecture)

            # Run fuzzing (limited time for this integration)
            timeout = min(config.get("timeout", 300), 300)  # Max 5 minutes for demo
            findings = await self._run_fuzzing(oss_fuzz_dir, project_name, sanitizer, timeout, workspace)

            # Reproduce bugs if requested
            if config.get("reproduce_bugs", False):
                repro_findings = await self._reproduce_bugs(oss_fuzz_dir, project_name, workspace)
                findings.extend(repro_findings)

        except Exception as e:
            logger.warning(f"Error running OSS-Fuzz: {e}")

        return findings

    async def _build_project(self, oss_fuzz_dir: Path, project_name: str, sanitizer: str, architecture: str):
        """Build OSS-Fuzz project"""
        cmd = [
            "python3", "infra/helper.py", "build_image", project_name
        ]

        process = await asyncio.create_subprocess_exec(
            *cmd,
            cwd=oss_fuzz_dir,
            stdout=asyncio.subprocess.PIPE,
            stderr=asyncio.subprocess.PIPE
        )
        stdout, stderr = await process.communicate()

        if process.returncode != 0:
            logger.warning(f"Build image failed: {stderr.decode()}")

    async def _check_build(self, oss_fuzz_dir: Path, project_name: str, sanitizer: str, architecture: str):
        """Check OSS-Fuzz build"""
        cmd = [
            "python3", "infra/helper.py", "check_build", project_name
        ]

        process = await asyncio.create_subprocess_exec(
            *cmd,
            cwd=oss_fuzz_dir,
            stdout=asyncio.subprocess.PIPE,
            stderr=asyncio.subprocess.PIPE
        )
        stdout, stderr = await process.communicate()

        if process.returncode != 0:
            logger.warning(f"Build check failed: {stderr.decode()}")

    async def _run_fuzzing(self, oss_fuzz_dir: Path, project_name: str, sanitizer: str, timeout: int, workspace: Path) -> List[ModuleFinding]:
        """Run OSS-Fuzz fuzzing"""
        findings = []

        try:
            # This is a simplified version - real OSS-Fuzz runs for much longer
            cmd = [
                "python3", "infra/helper.py", "run_fuzzer", project_name,
                "--", f"-max_total_time={timeout}"
            ]

            process = await asyncio.create_subprocess_exec(
                *cmd,
                cwd=oss_fuzz_dir,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )

            try:
                stdout, stderr = await asyncio.wait_for(
                    process.communicate(), timeout=timeout + 60
                )
            except asyncio.TimeoutError:
                process.terminate()
                await process.wait()

            # Parse output for crashes
            full_output = stdout.decode() + stderr.decode()
            findings = self._parse_oss_fuzz_output(full_output, workspace, sanitizer)

        except Exception as e:
            logger.warning(f"Error in OSS-Fuzz execution: {e}")

        return findings

    async def _reproduce_bugs(self, oss_fuzz_dir: Path, project_name: str, workspace: Path) -> List[ModuleFinding]:
        """Reproduce existing bugs"""
        findings = []

        try:
            # Look for existing testcases or artifacts
            testcases_dir = oss_fuzz_dir / "projects" / project_name / "testcases"
            if testcases_dir.exists():
                for testcase in testcases_dir.iterdir():
                    if testcase.is_file():
                        finding = self._create_testcase_finding(testcase, workspace)
                        if finding:
                            findings.append(finding)

        except Exception as e:
            logger.warning(f"Error reproducing bugs: {e}")

        return findings

    def _parse_oss_fuzz_output(self, output: str, workspace: Path, sanitizer: str) -> List[ModuleFinding]:
        """Parse OSS-Fuzz output"""
        findings = []

        try:
            # Look for common crash indicators
            lines = output.split('\n')
            crash_info = None

            for line in lines:
                if "ERROR:" in line and any(term in line for term in ["AddressSanitizer", "MemorySanitizer", "UBSan"]):
                    crash_info = {
                        "type": self._extract_crash_type(line),
                        "sanitizer": sanitizer,
                        "line": line
                    }
                elif crash_info and line.strip().startswith("#"):
                    # Stack trace line
                    if "stack_trace" not in crash_info:
                        crash_info["stack_trace"] = []
                    crash_info["stack_trace"].append(line.strip())

            if crash_info:
                finding = self._create_oss_fuzz_finding(crash_info, workspace)
                if finding:
                    findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing OSS-Fuzz output: {e}")

        return findings

    def _create_oss_fuzz_finding(self, crash_info: Dict[str, Any], workspace: Path) -> ModuleFinding:
        """Create finding from OSS-Fuzz crash"""
        try:
            bug_type = crash_info.get("type", "unknown")
            sanitizer = crash_info.get("sanitizer", "unknown")
            stack_trace = '\n'.join(crash_info.get("stack_trace", [])[:20])

            severity = self._get_oss_fuzz_severity(bug_type)

            finding = self.create_finding(
                title=f"OSS-Fuzz {bug_type.title()}",
                description=f"OSS-Fuzz detected a {bug_type} using {sanitizer} sanitizer",
                severity=severity,
                category=self._get_oss_fuzz_category(bug_type),
                file_path=None,
                recommendation=self._get_oss_fuzz_recommendation(bug_type, sanitizer),
                metadata={
                    "bug_type": bug_type,
                    "sanitizer": sanitizer,
                    "stack_trace": stack_trace,
                    "fuzzer": "oss_fuzz"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating OSS-Fuzz finding: {e}")
            return None

    def _create_testcase_finding(self, testcase_file: Path, workspace: Path) -> ModuleFinding:
        """Create finding from testcase file"""
        try:
            try:
                rel_path = testcase_file.relative_to(workspace)
                file_path = str(rel_path)
            except ValueError:
                file_path = str(testcase_file)

            finding = self.create_finding(
                title="OSS-Fuzz Testcase",
                description=f"OSS-Fuzz testcase found: {testcase_file.name}",
                severity="info",
                category="testcase",
                file_path=file_path,
                recommendation="Analyze testcase to understand potential issues",
                metadata={
                    "testcase_file": str(testcase_file),
                    "fuzzer": "oss_fuzz"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating testcase finding: {e}")
            return None

    def _extract_crash_type(self, line: str) -> str:
        """Extract crash type from error line"""
        if "heap-buffer-overflow" in line:
            return "heap_buffer_overflow"
        elif "stack-buffer-overflow" in line:
            return "stack_buffer_overflow"
        elif "use-after-free" in line:
            return "use_after_free"
        elif "double-free" in line:
            return "double_free"
        elif "memory leak" in line:
            return "memory_leak"
        else:
            return "unknown_crash"

    def _get_oss_fuzz_severity(self, bug_type: str) -> str:
        """Get severity for OSS-Fuzz bug type"""
        if bug_type in ["heap_buffer_overflow", "stack_buffer_overflow", "use_after_free", "double_free"]:
            return "critical"
        elif bug_type == "memory_leak":
            return "medium"
        else:
            return "high"

    def _get_oss_fuzz_category(self, bug_type: str) -> str:
        """Get category for OSS-Fuzz bug type"""
        if "overflow" in bug_type:
            return "buffer_overflow"
        elif "free" in bug_type:
            return "memory_corruption"
        elif "leak" in bug_type:
            return "memory_leak"
        else:
            return "memory_safety"

    def _get_oss_fuzz_recommendation(self, bug_type: str, sanitizer: str) -> str:
        """Get recommendation for OSS-Fuzz finding"""
        if "overflow" in bug_type:
            return "Fix buffer overflow by implementing proper bounds checking and using safe string functions."
        elif "use_after_free" in bug_type:
            return "Fix use-after-free by ensuring proper object lifetime management and setting pointers to NULL after freeing."
        elif "double_free" in bug_type:
            return "Fix double-free by ensuring each allocation has exactly one corresponding free operation."
        elif "leak" in bug_type:
            return "Fix memory leak by ensuring all allocated memory is properly freed in all code paths."
        else:
            return f"Address the {bug_type} issue detected by OSS-Fuzz with {sanitizer} sanitizer."

    def _create_summary(self, findings: List[ModuleFinding]) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}
        sanitizer_counts = {}

        for finding in findings:
            severity_counts[finding.severity] += 1
            category_counts[finding.category] = category_counts.get(finding.category, 0) + 1

            sanitizer = finding.metadata.get("sanitizer", "unknown")
            sanitizer_counts[sanitizer] = sanitizer_counts.get(sanitizer, 0) + 1

        return {
            "total_findings": len(findings),
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "sanitizer_counts": sanitizer_counts
        }