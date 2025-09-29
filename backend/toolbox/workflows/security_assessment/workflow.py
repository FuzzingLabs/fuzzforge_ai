"""
Security Assessment Workflow - Comprehensive security analysis using multiple modules
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

import sys
import logging
from pathlib import Path
from typing import Dict, Any, Optional
from prefect import flow, task
import json

# Add modules to path
sys.path.insert(0, '/app')

# Import modules
from toolbox.modules.scanner import FileScanner
from toolbox.modules.analyzer import SecurityAnalyzer
from toolbox.modules.reporter import SARIFReporter

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@task(name="file_scanning")
async def scan_files_task(workspace: Path, config: Dict[str, Any]) -> Dict[str, Any]:
    """
    Task to scan files in the workspace.

    Args:
        workspace: Path to the workspace
        config: Scanner configuration

    Returns:
        Scanner results
    """
    logger.info(f"Starting file scanning in {workspace}")
    scanner = FileScanner()

    result = await scanner.execute(config, workspace)

    logger.info(f"File scanning completed: {result.summary.get('total_files', 0)} files found")
    return result.dict()


@task(name="security_analysis")
async def analyze_security_task(workspace: Path, config: Dict[str, Any]) -> Dict[str, Any]:
    """
    Task to analyze security vulnerabilities.

    Args:
        workspace: Path to the workspace
        config: Analyzer configuration

    Returns:
        Analysis results
    """
    logger.info("Starting security analysis")
    analyzer = SecurityAnalyzer()

    result = await analyzer.execute(config, workspace)

    logger.info(
        f"Security analysis completed: {result.summary.get('total_findings', 0)} findings"
    )
    return result.dict()


@task(name="report_generation")
async def generate_report_task(
    scan_results: Dict[str, Any],
    analysis_results: Dict[str, Any],
    config: Dict[str, Any],
    workspace: Path
) -> Dict[str, Any]:
    """
    Task to generate SARIF report from all findings.

    Args:
        scan_results: Results from scanner
        analysis_results: Results from analyzer
        config: Reporter configuration
        workspace: Path to the workspace

    Returns:
        SARIF report
    """
    logger.info("Generating SARIF report")
    reporter = SARIFReporter()

    # Combine findings from all modules
    all_findings = []

    # Add scanner findings (only sensitive files, not all files)
    scanner_findings = scan_results.get("findings", [])
    sensitive_findings = [f for f in scanner_findings if f.get("severity") != "info"]
    all_findings.extend(sensitive_findings)

    # Add analyzer findings
    analyzer_findings = analysis_results.get("findings", [])
    all_findings.extend(analyzer_findings)

    # Prepare reporter config
    reporter_config = {
        **config,
        "findings": all_findings,
        "tool_name": "FuzzForge Security Assessment",
        "tool_version": "1.0.0"
    }

    result = await reporter.execute(reporter_config, workspace)

    # Extract SARIF from result
    sarif = result.dict().get("sarif", {})

    logger.info(f"Report generated with {len(all_findings)} total findings")
    return sarif


@flow(name="security_assessment", log_prints=True)
async def main_flow(
    target_path: str = "/workspace",
    volume_mode: str = "ro",
    scanner_config: Optional[Dict[str, Any]] = None,
    analyzer_config: Optional[Dict[str, Any]] = None,
    reporter_config: Optional[Dict[str, Any]] = None
) -> Dict[str, Any]:
    """
    Main security assessment workflow.

    This workflow:
    1. Scans files in the workspace
    2. Analyzes code for security vulnerabilities
    3. Generates a SARIF report with all findings

    Args:
        target_path: Path to the mounted workspace (default: /workspace)
        volume_mode: Volume mount mode (ro/rw)
        scanner_config: Configuration for file scanner
        analyzer_config: Configuration for security analyzer
        reporter_config: Configuration for SARIF reporter

    Returns:
        SARIF-formatted findings report
    """
    logger.info(f"Starting security assessment workflow")
    logger.info(f"Workspace: {target_path}, Mode: {volume_mode}")

    # Set workspace path
    workspace = Path(target_path)

    if not workspace.exists():
        logger.error(f"Workspace does not exist: {workspace}")
        return {
            "error": f"Workspace not found: {workspace}",
            "sarif": None
        }

    # Default configurations
    if not scanner_config:
        scanner_config = {
            "patterns": ["*"],
            "check_sensitive": True,
            "calculate_hashes": False,
            "max_file_size": 10485760  # 10MB
        }

    if not analyzer_config:
        analyzer_config = {
            "file_extensions": [".py", ".js", ".java", ".php", ".rb", ".go"],
            "check_secrets": True,
            "check_sql": True,
            "check_dangerous_functions": True
        }

    if not reporter_config:
        reporter_config = {
            "include_code_flows": False
        }

    try:
        # Execute workflow tasks
        logger.info("Phase 1: File scanning")
        scan_results = await scan_files_task(workspace, scanner_config)

        logger.info("Phase 2: Security analysis")
        analysis_results = await analyze_security_task(workspace, analyzer_config)

        logger.info("Phase 3: Report generation")
        sarif_report = await generate_report_task(
            scan_results,
            analysis_results,
            reporter_config,
            workspace
        )

        # Log summary
        if sarif_report and "runs" in sarif_report:
            results_count = len(sarif_report["runs"][0].get("results", []))
            logger.info(f"Workflow completed successfully with {results_count} findings")
        else:
            logger.info("Workflow completed successfully")

        return sarif_report

    except Exception as e:
        logger.error(f"Workflow failed: {e}")
        # Return error in SARIF format
        return {
            "$schema": "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
            "version": "2.1.0",
            "runs": [
                {
                    "tool": {
                        "driver": {
                            "name": "FuzzForge Security Assessment",
                            "version": "1.0.0"
                        }
                    },
                    "results": [],
                    "invocations": [
                        {
                            "executionSuccessful": False,
                            "exitCode": 1,
                            "exitCodeDescription": str(e)
                        }
                    ]
                }
            ]
        }


if __name__ == "__main__":
    # For local testing
    import asyncio

    asyncio.run(main_flow(
        target_path="/tmp/test",
        scanner_config={"patterns": ["*.py"]},
        analyzer_config={"check_secrets": True}
    ))