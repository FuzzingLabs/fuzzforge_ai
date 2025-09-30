"""
Secret Detection Scan Workflow

This workflow performs comprehensive secret detection using multiple tools:
- TruffleHog: Comprehensive secret detection with verification
- Gitleaks: Git-specific secret scanning
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
from typing import Dict, Any, List, Optional
from prefect import flow, task
from prefect.artifacts import create_markdown_artifact, create_table_artifact
import asyncio
import json

# Add modules to path
sys.path.insert(0, '/app')

# Import modules
from toolbox.modules.secret_detection.trufflehog import TruffleHogModule
from toolbox.modules.secret_detection.gitleaks import GitleaksModule
from toolbox.modules.reporter import SARIFReporter

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@task(name="trufflehog_scan")
async def run_trufflehog_task(workspace: Path, config: Dict[str, Any]) -> Dict[str, Any]:
    """
    Task to run TruffleHog secret detection.

    Args:
        workspace: Path to the workspace
        config: TruffleHog configuration

    Returns:
        TruffleHog results
    """
    logger.info("Running TruffleHog secret detection")
    module = TruffleHogModule()
    result = await module.execute(config, workspace)
    logger.info(f"TruffleHog completed: {result.summary.get('total_secrets', 0)} secrets found")
    return result.dict()


@task(name="gitleaks_scan")
async def run_gitleaks_task(workspace: Path, config: Dict[str, Any]) -> Dict[str, Any]:
    """
    Task to run Gitleaks secret detection.

    Args:
        workspace: Path to the workspace
        config: Gitleaks configuration

    Returns:
        Gitleaks results
    """
    logger.info("Running Gitleaks secret detection")
    module = GitleaksModule()
    result = await module.execute(config, workspace)
    logger.info(f"Gitleaks completed: {result.summary.get('total_leaks', 0)} leaks found")
    return result.dict()


@task(name="aggregate_findings")
async def aggregate_findings_task(
    trufflehog_results: Dict[str, Any],
    gitleaks_results: Dict[str, Any],
    config: Dict[str, Any],
    workspace: Path
) -> Dict[str, Any]:
    """
    Task to aggregate findings from all secret detection tools.

    Args:
        trufflehog_results: Results from TruffleHog
        gitleaks_results: Results from Gitleaks
        config: Reporter configuration
        workspace: Path to workspace

    Returns:
        Aggregated SARIF report
    """
    logger.info("Aggregating secret detection findings")

    # Combine all findings
    all_findings = []

    # Add TruffleHog findings
    trufflehog_findings = trufflehog_results.get("findings", [])
    all_findings.extend(trufflehog_findings)

    # Add Gitleaks findings
    gitleaks_findings = gitleaks_results.get("findings", [])
    all_findings.extend(gitleaks_findings)

    # Deduplicate findings based on file path and line number
    unique_findings = []
    seen_signatures = set()

    for finding in all_findings:
        # Create signature for deduplication
        signature = (
            finding.get("file_path", ""),
            finding.get("line_start", 0),
            finding.get("title", "").lower()[:50]  # First 50 chars of title
        )

        if signature not in seen_signatures:
            seen_signatures.add(signature)
            unique_findings.append(finding)
        else:
            logger.debug(f"Deduplicated finding: {signature}")

    logger.info(f"Aggregated {len(unique_findings)} unique findings from {len(all_findings)} total")

    # Generate SARIF report
    reporter = SARIFReporter()
    reporter_config = {
        **config,
        "findings": unique_findings,
        "tool_name": "FuzzForge Secret Detection",
        "tool_version": "1.0.0",
        "tool_description": "Comprehensive secret detection using TruffleHog and Gitleaks"
    }

    result = await reporter.execute(reporter_config, workspace)
    return result.dict().get("sarif", {})


@flow(name="secret_detection_scan", log_prints=True)
async def main_flow(
    target_path: str = "/workspace",
    volume_mode: str = "ro",
    trufflehog_config: Optional[Dict[str, Any]] = None,
    gitleaks_config: Optional[Dict[str, Any]] = None,
    reporter_config: Optional[Dict[str, Any]] = None
) -> Dict[str, Any]:
    """
    Main secret detection workflow.

    This workflow:
    1. Runs TruffleHog for comprehensive secret detection
    2. Runs Gitleaks for Git-specific secret detection
    3. Aggregates and deduplicates findings
    4. Generates a unified SARIF report

    Args:
        target_path: Path to the mounted workspace (default: /workspace)
        volume_mode: Volume mount mode (ro/rw)
        trufflehog_config: Configuration for TruffleHog
        gitleaks_config: Configuration for Gitleaks
        reporter_config: Configuration for SARIF reporter

    Returns:
        SARIF-formatted findings report
    """
    logger.info("Starting comprehensive secret detection workflow")
    logger.info(f"Workspace: {target_path}, Mode: {volume_mode}")

    # Set workspace path
    workspace = Path(target_path)

    if not workspace.exists():
        logger.error(f"Workspace does not exist: {workspace}")
        return {
            "error": f"Workspace not found: {workspace}",
            "sarif": None
        }

    # Default configurations - merge with provided configs to ensure defaults are always applied
    default_trufflehog_config = {
        "verify": False,
        "concurrency": 10,
        "max_depth": 10,
        "no_git": True  # Add no_git for filesystem scanning
    }
    trufflehog_config = {**default_trufflehog_config, **(trufflehog_config or {})}

    default_gitleaks_config = {
        "scan_mode": "detect",
        "redact": True,
        "max_target_megabytes": 100,
        "no_git": True  # Critical for non-git directories
    }
    gitleaks_config = {**default_gitleaks_config, **(gitleaks_config or {})}

    default_reporter_config = {
        "include_code_flows": False
    }
    reporter_config = {**default_reporter_config, **(reporter_config or {})}

    try:
        # Run secret detection tools in parallel
        logger.info("Phase 1: Running secret detection tools")

        # Create tasks for parallel execution
        trufflehog_task_result = run_trufflehog_task(workspace, trufflehog_config)
        gitleaks_task_result = run_gitleaks_task(workspace, gitleaks_config)

        # Wait for both to complete
        trufflehog_results, gitleaks_results = await asyncio.gather(
            trufflehog_task_result,
            gitleaks_task_result,
            return_exceptions=True
        )

        # Handle any exceptions
        if isinstance(trufflehog_results, Exception):
            logger.error(f"TruffleHog failed: {trufflehog_results}")
            trufflehog_results = {"findings": [], "status": "failed"}

        if isinstance(gitleaks_results, Exception):
            logger.error(f"Gitleaks failed: {gitleaks_results}")
            gitleaks_results = {"findings": [], "status": "failed"}

        # Aggregate findings
        logger.info("Phase 2: Aggregating findings")
        sarif_report = await aggregate_findings_task(
            trufflehog_results,
            gitleaks_results,
            reporter_config,
            workspace
        )

        # Log summary
        if sarif_report and "runs" in sarif_report:
            results_count = len(sarif_report["runs"][0].get("results", []))
            logger.info(f"Workflow completed successfully with {results_count} unique secret findings")

            # Log tool-specific stats
            trufflehog_count = len(trufflehog_results.get("findings", []))
            gitleaks_count = len(gitleaks_results.get("findings", []))
            logger.info(f"Tool results - TruffleHog: {trufflehog_count}, Gitleaks: {gitleaks_count}")
        else:
            logger.info("Workflow completed successfully with no findings")

        return sarif_report

    except Exception as e:
        logger.error(f"Secret detection workflow failed: {e}")
        # Return error in SARIF format
        return {
            "$schema": "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
            "version": "2.1.0",
            "runs": [
                {
                    "tool": {
                        "driver": {
                            "name": "FuzzForge Secret Detection",
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
        trufflehog_config={"verify": True, "max_depth": 5},
        gitleaks_config={"scan_mode": "detect"}
    ))