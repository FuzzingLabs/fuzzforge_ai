"""
SQLMap Penetration Testing Module

This module uses SQLMap for automatic SQL injection detection and exploitation.
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
from pathlib import Path
from typing import Dict, Any, List
import subprocess
import logging

from ..base import BaseModule, ModuleMetadata, ModuleFinding, ModuleResult
from . import register_module

logger = logging.getLogger(__name__)


@register_module
class SQLMapModule(BaseModule):
    """SQLMap automatic SQL injection detection and exploitation module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="sqlmap",
            version="1.7.11",
            description="Automatic SQL injection detection and exploitation tool",
            author="FuzzForge Team",
            category="penetration_testing",
            tags=["sql-injection", "web", "database", "vulnerability", "exploitation"],
            input_schema={
                "type": "object",
                "properties": {
                    "target_url": {
                        "type": "string",
                        "description": "Target URL to test for SQL injection"
                    },
                    "target_file": {
                        "type": "string",
                        "description": "File containing URLs to test"
                    },
                    "request_file": {
                        "type": "string",
                        "description": "Load HTTP request from file (Burp log, etc.)"
                    },
                    "data": {
                        "type": "string",
                        "description": "Data string to be sent through POST"
                    },
                    "cookie": {
                        "type": "string",
                        "description": "HTTP Cookie header value"
                    },
                    "user_agent": {
                        "type": "string",
                        "description": "HTTP User-Agent header value"
                    },
                    "referer": {
                        "type": "string",
                        "description": "HTTP Referer header value"
                    },
                    "headers": {
                        "type": "object",
                        "description": "Additional HTTP headers"
                    },
                    "method": {
                        "type": "string",
                        "enum": ["GET", "POST", "PUT", "DELETE", "PATCH"],
                        "default": "GET",
                        "description": "HTTP method to use"
                    },
                    "testable_parameters": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Comma-separated list of testable parameter(s)"
                    },
                    "skip_parameters": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Parameters to skip during testing"
                    },
                    "dbms": {
                        "type": "string",
                        "enum": ["mysql", "postgresql", "oracle", "mssql", "sqlite", "access", "firebird", "sybase", "db2", "hsqldb", "h2"],
                        "description": "Force back-end DBMS to provided value"
                    },
                    "level": {
                        "type": "integer",
                        "enum": [1, 2, 3, 4, 5],
                        "default": 1,
                        "description": "Level of tests to perform (1-5)"
                    },
                    "risk": {
                        "type": "integer",
                        "enum": [1, 2, 3],
                        "default": 1,
                        "description": "Risk of tests to perform (1-3)"
                    },
                    "technique": {
                        "type": "array",
                        "items": {"type": "string", "enum": ["B", "E", "U", "S", "T", "Q"]},
                        "description": "SQL injection techniques to use (B=Boolean, E=Error, U=Union, S=Stacked, T=Time, Q=Inline)"
                    },
                    "time_sec": {
                        "type": "integer",
                        "default": 5,
                        "description": "Seconds to delay DBMS response for time-based blind SQL injection"
                    },
                    "union_cols": {
                        "type": "string",
                        "description": "Range of columns to test for UNION query SQL injection"
                    },
                    "threads": {
                        "type": "integer",
                        "default": 1,
                        "description": "Maximum number of concurrent HTTP requests"
                    },
                    "timeout": {
                        "type": "integer",
                        "default": 30,
                        "description": "Seconds to wait before timeout connection"
                    },
                    "retries": {
                        "type": "integer",
                        "default": 3,
                        "description": "Retries when connection timeouts"
                    },
                    "randomize": {
                        "type": "boolean",
                        "default": True,
                        "description": "Randomly change value of given parameter(s)"
                    },
                    "safe_url": {
                        "type": "string",
                        "description": "URL to visit frequently during testing"
                    },
                    "safe_freq": {
                        "type": "integer",
                        "description": "Test requests between visits to safe URL"
                    },
                    "crawl": {
                        "type": "integer",
                        "description": "Crawl website starting from target URL (depth)"
                    },
                    "forms": {
                        "type": "boolean",
                        "default": False,
                        "description": "Parse and test forms on target URL"
                    },
                    "batch": {
                        "type": "boolean",
                        "default": True,
                        "description": "Never ask for user input, use default behavior"
                    },
                    "cleanup": {
                        "type": "boolean",
                        "default": True,
                        "description": "Clean up files used by SQLMap"
                    },
                    "check_waf": {
                        "type": "boolean",
                        "default": False,
                        "description": "Check for existence of WAF/IPS protection"
                    },
                    "tamper": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Use tamper scripts to modify requests"
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
                                "url": {"type": "string"},
                                "parameter": {"type": "string"},
                                "technique": {"type": "string"},
                                "dbms": {"type": "string"},
                                "payload": {"type": "string"}
                            }
                        }
                    }
                }
            }
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate configuration"""
        target_url = config.get("target_url")
        target_file = config.get("target_file")
        request_file = config.get("request_file")

        if not any([target_url, target_file, request_file]):
            raise ValueError("Either 'target_url', 'target_file', or 'request_file' must be specified")

        level = config.get("level", 1)
        if level not in [1, 2, 3, 4, 5]:
            raise ValueError("Level must be between 1 and 5")

        risk = config.get("risk", 1)
        if risk not in [1, 2, 3]:
            raise ValueError("Risk must be between 1 and 3")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute SQLMap SQL injection testing"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info("Running SQLMap SQL injection scan")

            # Run SQLMap scan
            findings = await self._run_sqlmap_scan(config, workspace)

            # Create summary
            summary = self._create_summary(findings)

            logger.info(f"SQLMap found {len(findings)} SQL injection vulnerabilities")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"SQLMap module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    async def _run_sqlmap_scan(self, config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Run SQLMap scan"""
        findings = []

        try:
            # Build sqlmap command
            cmd = ["sqlmap"]

            # Add target specification
            target_url = config.get("target_url")
            if target_url:
                cmd.extend(["-u", target_url])

            target_file = config.get("target_file")
            if target_file:
                target_path = workspace / target_file
                if target_path.exists():
                    cmd.extend(["-m", str(target_path)])
                else:
                    raise FileNotFoundError(f"Target file not found: {target_file}")

            request_file = config.get("request_file")
            if request_file:
                request_path = workspace / request_file
                if request_path.exists():
                    cmd.extend(["-r", str(request_path)])
                else:
                    raise FileNotFoundError(f"Request file not found: {request_file}")

            # Add HTTP options
            data = config.get("data")
            if data:
                cmd.extend(["--data", data])

            cookie = config.get("cookie")
            if cookie:
                cmd.extend(["--cookie", cookie])

            user_agent = config.get("user_agent")
            if user_agent:
                cmd.extend(["--user-agent", user_agent])

            referer = config.get("referer")
            if referer:
                cmd.extend(["--referer", referer])

            headers = config.get("headers", {})
            for key, value in headers.items():
                cmd.extend(["--header", f"{key}: {value}"])

            method = config.get("method")
            if method and method != "GET":
                cmd.extend(["--method", method])

            # Add parameter options
            testable_params = config.get("testable_parameters", [])
            if testable_params:
                cmd.extend(["-p", ",".join(testable_params)])

            skip_params = config.get("skip_parameters", [])
            if skip_params:
                cmd.extend(["--skip", ",".join(skip_params)])

            # Add injection options
            dbms = config.get("dbms")
            if dbms:
                cmd.extend(["--dbms", dbms])

            level = config.get("level", 1)
            cmd.extend(["--level", str(level)])

            risk = config.get("risk", 1)
            cmd.extend(["--risk", str(risk)])

            techniques = config.get("technique", [])
            if techniques:
                cmd.extend(["--technique", "".join(techniques)])

            time_sec = config.get("time_sec", 5)
            cmd.extend(["--time-sec", str(time_sec)])

            union_cols = config.get("union_cols")
            if union_cols:
                cmd.extend(["--union-cols", union_cols])

            # Add performance options
            threads = config.get("threads", 1)
            cmd.extend(["--threads", str(threads)])

            timeout = config.get("timeout", 30)
            cmd.extend(["--timeout", str(timeout)])

            retries = config.get("retries", 3)
            cmd.extend(["--retries", str(retries)])

            # Add request options
            if config.get("randomize", True):
                cmd.append("--randomize")

            safe_url = config.get("safe_url")
            if safe_url:
                cmd.extend(["--safe-url", safe_url])

                safe_freq = config.get("safe_freq")
                if safe_freq:
                    cmd.extend(["--safe-freq", str(safe_freq)])

            # Add crawling options
            crawl_depth = config.get("crawl")
            if crawl_depth:
                cmd.extend(["--crawl", str(crawl_depth)])

            if config.get("forms", False):
                cmd.append("--forms")

            # Add behavioral options
            if config.get("batch", True):
                cmd.append("--batch")

            if config.get("cleanup", True):
                cmd.append("--cleanup")

            if config.get("check_waf", False):
                cmd.append("--check-waf")

            # Add tamper scripts
            tamper_scripts = config.get("tamper", [])
            if tamper_scripts:
                cmd.extend(["--tamper", ",".join(tamper_scripts)])

            # Set output directory
            output_dir = workspace / "sqlmap_output"
            output_dir.mkdir(exist_ok=True)
            cmd.extend(["--output-dir", str(output_dir)])

            # Add format for easier parsing
            cmd.append("--flush-session")  # Start fresh
            cmd.append("--fresh-queries")  # Ignore previous results

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run sqlmap
            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace
            )

            stdout, stderr = await process.communicate()

            # Parse results from output directory
            findings = self._parse_sqlmap_output(output_dir, stdout.decode(), workspace)

            # Log results
            if findings:
                logger.info(f"SQLMap detected {len(findings)} SQL injection vulnerabilities")
            else:
                logger.info("No SQL injection vulnerabilities found")
                # Check for errors
                stderr_text = stderr.decode()
                if stderr_text:
                    logger.warning(f"SQLMap warnings/errors: {stderr_text}")

        except Exception as e:
            logger.warning(f"Error running SQLMap scan: {e}")

        return findings

    def _parse_sqlmap_output(self, output_dir: Path, stdout: str, workspace: Path) -> List[ModuleFinding]:
        """Parse SQLMap output into findings"""
        findings = []

        try:
            # Look for session files in output directory
            session_files = list(output_dir.glob("**/*.sqlite"))
            log_files = list(output_dir.glob("**/*.log"))

            # Parse stdout for injection information
            findings.extend(self._parse_stdout_output(stdout))

            # Parse log files for additional details
            for log_file in log_files:
                findings.extend(self._parse_log_file(log_file))

            # If we have session files, we can extract more detailed information
            # For now, we'll rely on stdout parsing

        except Exception as e:
            logger.warning(f"Error parsing SQLMap output: {e}")

        return findings

    def _parse_stdout_output(self, stdout: str) -> List[ModuleFinding]:
        """Parse SQLMap stdout for SQL injection findings"""
        findings = []

        try:
            lines = stdout.split('\n')
            current_url = None
            current_parameter = None
            current_technique = None
            current_dbms = None
            injection_found = False

            for line in lines:
                line = line.strip()

                # Extract URL being tested
                if "testing URL" in line or "testing connection to the target URL" in line:
                    # Extract URL from line
                    if "'" in line:
                        url_start = line.find("'") + 1
                        url_end = line.find("'", url_start)
                        if url_end > url_start:
                            current_url = line[url_start:url_end]

                # Extract parameter being tested
                elif "testing parameter" in line or "testing" in line and "parameter" in line:
                    if "'" in line:
                        param_parts = line.split("'")
                        if len(param_parts) >= 2:
                            current_parameter = param_parts[1]

                # Detect SQL injection found
                elif any(indicator in line.lower() for indicator in [
                    "parameter appears to be vulnerable",
                    "injectable",
                    "parameter is vulnerable"
                ]):
                    injection_found = True

                # Extract technique information
                elif "Type:" in line:
                    current_technique = line.replace("Type:", "").strip()

                # Extract database information
                elif "back-end DBMS:" in line.lower():
                    current_dbms = line.split(":")[-1].strip()

                # Extract payload information
                elif "Payload:" in line:
                    payload = line.replace("Payload:", "").strip()

                    # Create finding if we have injection
                    if injection_found and current_url and current_parameter:
                        finding = self._create_sqlmap_finding(
                            current_url, current_parameter, current_technique,
                            current_dbms, payload
                        )
                        if finding:
                            findings.append(finding)

                        # Reset state
                        injection_found = False
                        current_technique = None

        except Exception as e:
            logger.warning(f"Error parsing SQLMap stdout: {e}")

        return findings

    def _parse_log_file(self, log_file: Path) -> List[ModuleFinding]:
        """Parse SQLMap log file for additional findings"""
        findings = []

        try:
            with open(log_file, 'r') as f:
                content = f.read()

            # Look for injection indicators in log
            if "injectable" in content.lower() or "vulnerable" in content.lower():
                # Could parse more detailed information from log
                # For now, we'll rely on stdout parsing
                pass

        except Exception as e:
            logger.warning(f"Error parsing log file {log_file}: {e}")

        return findings

    def _create_sqlmap_finding(self, url: str, parameter: str, technique: str, dbms: str, payload: str) -> ModuleFinding:
        """Create a ModuleFinding for SQL injection"""
        try:
            # Map technique to readable description
            technique_map = {
                "boolean-based blind": "Boolean-based blind SQL injection",
                "time-based blind": "Time-based blind SQL injection",
                "error-based": "Error-based SQL injection",
                "UNION query": "UNION-based SQL injection",
                "stacked queries": "Stacked queries SQL injection",
                "inline query": "Inline query SQL injection"
            }

            technique_desc = technique_map.get(technique, technique or "SQL injection")

            # Create description
            description = f"SQL injection vulnerability detected in parameter '{parameter}' using {technique_desc}"
            if dbms:
                description += f" against {dbms} database"

            # Determine severity based on technique
            severity = self._get_injection_severity(technique, dbms)

            # Create finding
            finding = self.create_finding(
                title=f"SQL Injection: {parameter}",
                description=description,
                severity=severity,
                category="sql_injection",
                file_path=None,  # Web application testing
                recommendation=self._get_sqlinjection_recommendation(technique, dbms),
                metadata={
                    "url": url,
                    "parameter": parameter,
                    "technique": technique,
                    "dbms": dbms,
                    "payload": payload[:500] if payload else "",  # Limit payload length
                    "injection_type": technique_desc
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating SQLMap finding: {e}")
            return None

    def _get_injection_severity(self, technique: str, dbms: str) -> str:
        """Determine severity based on injection technique and database"""
        if not technique:
            return "high"  # Any SQL injection is serious

        technique_lower = technique.lower()

        # Critical severity for techniques that allow easy data extraction
        if any(term in technique_lower for term in ["union", "error-based"]):
            return "critical"

        # High severity for techniques that allow some data extraction
        elif any(term in technique_lower for term in ["boolean-based", "time-based"]):
            return "high"

        # Stacked queries are very dangerous as they allow multiple statements
        elif "stacked" in technique_lower:
            return "critical"

        else:
            return "high"

    def _get_sqlinjection_recommendation(self, technique: str, dbms: str) -> str:
        """Generate recommendation for SQL injection"""
        base_recommendation = "Implement parameterized queries/prepared statements and input validation to prevent SQL injection attacks."

        if technique:
            technique_lower = technique.lower()
            if "union" in technique_lower:
                base_recommendation += " The UNION-based injection allows direct data extraction - immediate remediation required."
            elif "error-based" in technique_lower:
                base_recommendation += " Error-based injection reveals database structure - disable error messages in production."
            elif "time-based" in technique_lower:
                base_recommendation += " Time-based injection allows blind data extraction - implement query timeout limits."
            elif "stacked" in technique_lower:
                base_recommendation += " Stacked queries injection allows multiple SQL statements - extremely dangerous, fix immediately."

        if dbms:
            dbms_lower = dbms.lower()
            if "mysql" in dbms_lower:
                base_recommendation += " For MySQL: disable LOAD_FILE and INTO OUTFILE if not needed."
            elif "postgresql" in dbms_lower:
                base_recommendation += " For PostgreSQL: review user privileges and disable unnecessary functions."
            elif "mssql" in dbms_lower:
                base_recommendation += " For SQL Server: disable xp_cmdshell and review extended stored procedures."

        return base_recommendation

    def _create_summary(self, findings: List[ModuleFinding]) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        technique_counts = {}
        dbms_counts = {}
        parameter_counts = {}
        url_counts = {}

        for finding in findings:
            # Count by severity
            severity_counts[finding.severity] += 1

            # Count by technique
            technique = finding.metadata.get("technique", "unknown")
            technique_counts[technique] = technique_counts.get(technique, 0) + 1

            # Count by DBMS
            dbms = finding.metadata.get("dbms", "unknown")
            if dbms != "unknown":
                dbms_counts[dbms] = dbms_counts.get(dbms, 0) + 1

            # Count by parameter
            parameter = finding.metadata.get("parameter", "unknown")
            parameter_counts[parameter] = parameter_counts.get(parameter, 0) + 1

            # Count by URL
            url = finding.metadata.get("url", "unknown")
            url_counts[url] = url_counts.get(url, 0) + 1

        return {
            "total_findings": len(findings),
            "severity_counts": severity_counts,
            "technique_counts": technique_counts,
            "dbms_counts": dbms_counts,
            "vulnerable_parameters": list(parameter_counts.keys()),
            "vulnerable_urls": len(url_counts),
            "most_common_techniques": dict(sorted(technique_counts.items(), key=lambda x: x[1], reverse=True)[:5]),
            "affected_databases": list(dbms_counts.keys())
        }