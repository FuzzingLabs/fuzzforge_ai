"""
API endpoints for workflow run management and findings retrieval
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

import logging
from typing import Dict, Any
from fastapi import APIRouter, HTTPException, Depends

from src.models.findings import WorkflowFindings, WorkflowStatus

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/runs", tags=["runs"])


def get_prefect_manager():
    """Dependency to get the Prefect manager instance"""
    from src.main import prefect_mgr
    return prefect_mgr


@router.get("/{run_id}/status", response_model=WorkflowStatus)
async def get_run_status(
    run_id: str,
    prefect_mgr=Depends(get_prefect_manager)
) -> WorkflowStatus:
    """
    Get the current status of a workflow run.

    Args:
        run_id: The flow run ID

    Returns:
        Status information including state, timestamps, and completion flags

    Raises:
        HTTPException: 404 if run not found
    """
    try:
        status = await prefect_mgr.get_flow_run_status(run_id)

        # Find workflow name from deployment
        workflow_name = "unknown"
        workflow_deployment_id = status.get("workflow", "")
        for name, deployment_id in prefect_mgr.deployments.items():
            if str(deployment_id) == str(workflow_deployment_id):
                workflow_name = name
                break

        return WorkflowStatus(
            run_id=status["run_id"],
            workflow=workflow_name,
            status=status["status"],
            is_completed=status["is_completed"],
            is_failed=status["is_failed"],
            is_running=status["is_running"],
            created_at=status["created_at"],
            updated_at=status["updated_at"]
        )

    except Exception as e:
        logger.error(f"Failed to get status for run {run_id}: {e}")
        raise HTTPException(
            status_code=404,
            detail=f"Run not found: {run_id}"
        )


@router.get("/{run_id}/findings", response_model=WorkflowFindings)
async def get_run_findings(
    run_id: str,
    prefect_mgr=Depends(get_prefect_manager)
) -> WorkflowFindings:
    """
    Get the findings from a completed workflow run.

    Args:
        run_id: The flow run ID

    Returns:
        SARIF-formatted findings from the workflow execution

    Raises:
        HTTPException: 404 if run not found, 400 if run not completed
    """
    try:
        # Get run status first
        status = await prefect_mgr.get_flow_run_status(run_id)

        if not status["is_completed"]:
            if status["is_running"]:
                raise HTTPException(
                    status_code=400,
                    detail=f"Run {run_id} is still running. Current status: {status['status']}"
                )
            elif status["is_failed"]:
                raise HTTPException(
                    status_code=400,
                    detail=f"Run {run_id} failed. Status: {status['status']}"
                )
            else:
                raise HTTPException(
                    status_code=400,
                    detail=f"Run {run_id} not completed. Status: {status['status']}"
                )

        # Get the findings
        findings = await prefect_mgr.get_flow_run_findings(run_id)

        # Find workflow name
        workflow_name = "unknown"
        workflow_deployment_id = status.get("workflow", "")
        for name, deployment_id in prefect_mgr.deployments.items():
            if str(deployment_id) == str(workflow_deployment_id):
                workflow_name = name
                break

        # Get workflow version if available
        metadata = {
            "completion_time": status["updated_at"],
            "workflow_version": "unknown"
        }

        if workflow_name in prefect_mgr.workflows:
            workflow_info = prefect_mgr.workflows[workflow_name]
            metadata["workflow_version"] = workflow_info.metadata.get("version", "unknown")

        return WorkflowFindings(
            workflow=workflow_name,
            run_id=run_id,
            sarif=findings,
            metadata=metadata
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Failed to get findings for run {run_id}: {e}")
        raise HTTPException(
            status_code=500,
            detail=f"Failed to retrieve findings: {str(e)}"
        )


@router.get("/{workflow_name}/findings/{run_id}", response_model=WorkflowFindings)
async def get_workflow_findings(
    workflow_name: str,
    run_id: str,
    prefect_mgr=Depends(get_prefect_manager)
) -> WorkflowFindings:
    """
    Get findings for a specific workflow run.

    Alternative endpoint that includes workflow name in the path for clarity.

    Args:
        workflow_name: Name of the workflow
        run_id: The flow run ID

    Returns:
        SARIF-formatted findings from the workflow execution

    Raises:
        HTTPException: 404 if workflow or run not found, 400 if run not completed
    """
    if workflow_name not in prefect_mgr.workflows:
        raise HTTPException(
            status_code=404,
            detail=f"Workflow not found: {workflow_name}"
        )

    # Delegate to the main findings endpoint
    return await get_run_findings(run_id, prefect_mgr)