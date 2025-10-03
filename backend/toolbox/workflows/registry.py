"""
Manual Workflow Registry for Prefect Deployment

This file contains the manual registry of all workflows that can be deployed.
Developers MUST add their workflows here after creating them.

This approach is required because:
1. Prefect cannot deploy dynamically imported flows
2. Docker deployment needs static flow references
3. Explicit registration provides better control and visibility
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

from typing import Dict, Any, Callable
import logging

logger = logging.getLogger(__name__)

# Import only essential workflows
# Import each workflow individually to handle failures gracefully
security_assessment_flow = None
secret_detection_flow = None
android_static_analysis_flow = None

# Try to import each workflow individually
try:
    from .security_assessment.workflow import main_flow as security_assessment_flow
except ImportError as e:
    logger.warning(f"Failed to import security_assessment workflow: {e}")

try:
    from .comprehensive.secret_detection_scan.workflow import main_flow as secret_detection_flow
except ImportError as e:
    logger.warning(f"Failed to import secret_detection_scan workflow: {e}")

try:
    from .android_static_analysis.workflow import main_flow as android_static_analysis_flow
except ImportError as e:
    logger.warning(f"Failed to import android_static_analysis workflow: {e}")


# Manual registry - developers add workflows here after creation
# Only include workflows that were successfully imported
WORKFLOW_REGISTRY: Dict[str, Dict[str, Any]] = {}

# Add workflows that were successfully imported
if security_assessment_flow is not None:
    WORKFLOW_REGISTRY["security_assessment"] = {
        "flow": security_assessment_flow,
        "module_path": "toolbox.workflows.security_assessment.workflow",
        "function_name": "main_flow",
        "description": "Comprehensive security assessment workflow that scans files, analyzes code for vulnerabilities, and generates SARIF reports",
        "version": "1.0.0",
        "author": "FuzzForge Team",
        "tags": ["security", "scanner", "analyzer", "static-analysis", "sarif"]
    }

if secret_detection_flow is not None:
    WORKFLOW_REGISTRY["secret_detection_scan"] = {
        "flow": secret_detection_flow,
        "module_path": "toolbox.workflows.comprehensive.secret_detection_scan.workflow",
        "function_name": "main_flow",
        "description": "Comprehensive secret detection using TruffleHog and Gitleaks for thorough credential scanning",
        "version": "1.0.0",
        "author": "FuzzForge Team",
        "tags": ["secrets", "credentials", "detection", "trufflehog", "gitleaks", "comprehensive"]
    }

if android_static_analysis_flow is not None:
    WORKFLOW_REGISTRY["android_static_analysis"] = {
        "flow": android_static_analysis_flow,
        "module_path": "toolbox.workflows.android_static_analysis.workflow",
        "function_name": "main_flow",
        "description": "Perform static analysis on Android applications using OpenGrep",
        "version": "1.0.0",
        "author": "FuzzForge Team",
        "tags": ["android", "static-analysis", "security", "opengrep", "semgrep"]
    }

#
# To add a new workflow, follow this pattern:
#
# "my_new_workflow": {
#     "flow": my_new_flow_function,  # Import the flow function above
#     "module_path": "toolbox.workflows.my_new_workflow.workflow",
#     "function_name": "my_new_flow_function",
#     "description": "Description of what this workflow does",
#     "version": "1.0.0",
#     "author": "Developer Name",
#     "tags": ["tag1", "tag2"]
# }


def get_workflow_flow(workflow_name: str) -> Callable:
    """
    Get the flow function for a workflow.

    Args:
        workflow_name: Name of the workflow

    Returns:
        Flow function

    Raises:
        KeyError: If workflow not found in registry
    """
    if workflow_name not in WORKFLOW_REGISTRY:
        available = list(WORKFLOW_REGISTRY.keys())
        raise KeyError(
            f"Workflow '{workflow_name}' not found in registry. "
            f"Available workflows: {available}. "
            f"Please add the workflow to toolbox/workflows/registry.py"
        )

    return WORKFLOW_REGISTRY[workflow_name]["flow"]


def get_workflow_info(workflow_name: str) -> Dict[str, Any]:
    """
    Get registry information for a workflow.

    Args:
        workflow_name: Name of the workflow

    Returns:
        Registry information dictionary

    Raises:
        KeyError: If workflow not found in registry
    """
    if workflow_name not in WORKFLOW_REGISTRY:
        available = list(WORKFLOW_REGISTRY.keys())
        raise KeyError(
            f"Workflow '{workflow_name}' not found in registry. "
            f"Available workflows: {available}"
        )

    return WORKFLOW_REGISTRY[workflow_name]


def list_registered_workflows() -> Dict[str, Dict[str, Any]]:
    """
    Get all registered workflows.

    Returns:
        Dictionary of all workflow registry entries
    """
    return WORKFLOW_REGISTRY.copy()


def validate_registry() -> bool:
    """
    Validate the workflow registry for consistency.

    Returns:
        True if valid, raises exceptions if not

    Raises:
        ValueError: If registry is invalid
    """
    if not WORKFLOW_REGISTRY:
        raise ValueError("Workflow registry is empty")

    required_fields = ["flow", "module_path", "function_name", "description"]

    for name, entry in WORKFLOW_REGISTRY.items():
        # Check required fields
        missing_fields = [field for field in required_fields if field not in entry]
        if missing_fields:
            raise ValueError(
                f"Workflow '{name}' missing required fields: {missing_fields}"
            )

        # Check if flow is callable
        if not callable(entry["flow"]):
            raise ValueError(f"Workflow '{name}' flow is not callable")

        # Check if flow has the required Prefect attributes
        if not hasattr(entry["flow"], "deploy"):
            raise ValueError(
                f"Workflow '{name}' flow is not a Prefect flow (missing deploy method)"
            )

    logger.info(f"Registry validation passed. {len(WORKFLOW_REGISTRY)} workflows registered.")
    return True


# Validate registry on import
try:
    validate_registry()
    logger.info(f"Workflow registry loaded successfully with {len(WORKFLOW_REGISTRY)} workflows")
except Exception as e:
    logger.error(f"Workflow registry validation failed: {e}")
    raise