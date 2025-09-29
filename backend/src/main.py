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
import logging
import os
from uuid import UUID
from contextlib import AsyncExitStack, asynccontextmanager, suppress
from typing import Any, Dict, Optional, List

import uvicorn
from fastapi import FastAPI
from starlette.applications import Starlette
from starlette.routing import Mount

from fastmcp.server.http import create_sse_app

from src.core.prefect_manager import PrefectManager
from src.core.setup import setup_docker_pool, setup_result_storage, validate_infrastructure
from src.core.workflow_discovery import WorkflowDiscovery
from src.api import workflows, runs, fuzzing
from src.services.prefect_stats_monitor import prefect_stats_monitor

from fastmcp import FastMCP
from prefect.client.orchestration import get_client
from prefect.client.schemas.filters import (
    FlowRunFilter,
    FlowRunFilterDeploymentId,
    FlowRunFilterState,
    FlowRunFilterStateType,
)
from prefect.client.schemas.sorting import FlowRunSort
from prefect.states import StateType

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

prefect_mgr = PrefectManager()


class PrefectBootstrapState:
    """Tracks Prefect initialization progress for API and MCP consumers."""

    def __init__(self) -> None:
        self.ready: bool = False
        self.status: str = "not_started"
        self.last_error: Optional[str] = None
        self.task_running: bool = False

    def as_dict(self) -> Dict[str, Any]:
        return {
            "ready": self.ready,
            "status": self.status,
            "last_error": self.last_error,
            "task_running": self.task_running,
        }


prefect_bootstrap_state = PrefectBootstrapState()

# Configure retry strategy for bootstrapping Prefect + infrastructure
STARTUP_RETRY_SECONDS = max(1, int(os.getenv("FUZZFORGE_STARTUP_RETRY_SECONDS", "5")))
STARTUP_RETRY_MAX_SECONDS = max(
    STARTUP_RETRY_SECONDS,
    int(os.getenv("FUZZFORGE_STARTUP_RETRY_MAX_SECONDS", "60")),
)

prefect_bootstrap_task: Optional[asyncio.Task] = None

# ---------------------------------------------------------------------------
# FastAPI application (REST API remains unchanged)
# ---------------------------------------------------------------------------

app = FastAPI(
    title="FuzzForge API",
    description="Security testing workflow orchestration API with fuzzing support",
    version="0.6.0",
)

app.include_router(workflows.router)
app.include_router(runs.router)
app.include_router(fuzzing.router)


def get_prefect_status() -> Dict[str, Any]:
    """Return a snapshot of Prefect bootstrap state for diagnostics."""
    status = prefect_bootstrap_state.as_dict()
    status["workflows_loaded"] = len(prefect_mgr.workflows)
    status["deployments_tracked"] = len(prefect_mgr.deployments)
    status["bootstrap_task_running"] = (
        prefect_bootstrap_task is not None and not prefect_bootstrap_task.done()
    )
    return status


def _prefect_not_ready_status() -> Optional[Dict[str, Any]]:
    """Return status details if Prefect is not ready yet."""
    status = get_prefect_status()
    if status.get("ready"):
        return None
    return status


@app.get("/")
async def root() -> Dict[str, Any]:
    status = get_prefect_status()
    return {
        "name": "FuzzForge API",
        "version": "0.6.0",
        "status": "ready" if status.get("ready") else "initializing",
        "workflows_loaded": status.get("workflows_loaded", 0),
        "prefect": status,
    }


@app.get("/health")
async def health() -> Dict[str, str]:
    status = get_prefect_status()
    health_status = "healthy" if status.get("ready") else "initializing"
    return {"status": health_status}


# Map FastAPI OpenAPI operationIds to readable MCP tool names
FASTAPI_MCP_NAME_OVERRIDES: Dict[str, str] = {
    "list_workflows_workflows__get": "api_list_workflows",
    "get_metadata_schema_workflows_metadata_schema_get": "api_get_metadata_schema",
    "get_workflow_metadata_workflows__workflow_name__metadata_get": "api_get_workflow_metadata",
    "submit_workflow_workflows__workflow_name__submit_post": "api_submit_workflow",
    "get_workflow_parameters_workflows__workflow_name__parameters_get": "api_get_workflow_parameters",
    "get_run_status_runs__run_id__status_get": "api_get_run_status",
    "get_run_findings_runs__run_id__findings_get": "api_get_run_findings",
    "get_workflow_findings_runs__workflow_name__findings__run_id__get": "api_get_workflow_findings",
    "get_fuzzing_stats_fuzzing__run_id__stats_get": "api_get_fuzzing_stats",
    "update_fuzzing_stats_fuzzing__run_id__stats_post": "api_update_fuzzing_stats",
    "get_crash_reports_fuzzing__run_id__crashes_get": "api_get_crash_reports",
    "report_crash_fuzzing__run_id__crash_post": "api_report_crash",
    "stream_fuzzing_updates_fuzzing__run_id__stream_get": "api_stream_fuzzing_updates",
    "cleanup_fuzzing_run_fuzzing__run_id__delete": "api_cleanup_fuzzing_run",
    "root__get": "api_root",
    "health_health_get": "api_health",
}


# Create an MCP adapter exposing all FastAPI endpoints via OpenAPI parsing
FASTAPI_MCP_ADAPTER = FastMCP.from_fastapi(
    app,
    name="FuzzForge FastAPI",
    mcp_names=FASTAPI_MCP_NAME_OVERRIDES,
)
_fastapi_mcp_imported = False


# ---------------------------------------------------------------------------
# FastMCP server (runs on dedicated port outside FastAPI)
# ---------------------------------------------------------------------------

mcp = FastMCP(name="FuzzForge MCP")


async def _bootstrap_prefect_with_retries() -> None:
    """Initialize Prefect infrastructure with exponential backoff retries."""

    attempt = 0

    while True:
        attempt += 1
        prefect_bootstrap_state.task_running = True
        prefect_bootstrap_state.status = "starting"
        prefect_bootstrap_state.ready = False
        prefect_bootstrap_state.last_error = None

        try:
            logger.info("Bootstrapping Prefect infrastructure...")
            await validate_infrastructure()
            await setup_docker_pool()
            await setup_result_storage()
            await prefect_mgr.initialize()
            await prefect_stats_monitor.start_monitoring()

            prefect_bootstrap_state.ready = True
            prefect_bootstrap_state.status = "ready"
            prefect_bootstrap_state.task_running = False
            logger.info("Prefect infrastructure ready")
            return

        except asyncio.CancelledError:
            prefect_bootstrap_state.status = "cancelled"
            prefect_bootstrap_state.task_running = False
            logger.info("Prefect bootstrap task cancelled")
            raise

        except Exception as exc:  # pragma: no cover - defensive logging on infra startup
            logger.exception("Prefect bootstrap failed")
            prefect_bootstrap_state.ready = False
            prefect_bootstrap_state.status = "error"
            prefect_bootstrap_state.last_error = str(exc)

            # Ensure partial initialization does not leave stale state behind
            prefect_mgr.workflows.clear()
            prefect_mgr.deployments.clear()
            await prefect_stats_monitor.stop_monitoring()

            wait_time = min(
                STARTUP_RETRY_SECONDS * (2 ** (attempt - 1)),
                STARTUP_RETRY_MAX_SECONDS,
            )
            logger.info("Retrying Prefect bootstrap in %s second(s)", wait_time)

            try:
                await asyncio.sleep(wait_time)
            except asyncio.CancelledError:
                prefect_bootstrap_state.status = "cancelled"
                prefect_bootstrap_state.task_running = False
                raise


def _lookup_workflow(workflow_name: str):
    info = prefect_mgr.workflows.get(workflow_name)
    if not info:
        return None
    metadata = info.metadata
    defaults = metadata.get("default_parameters", {})
    default_target_path = metadata.get("default_target_path") or defaults.get("target_path")
    supported_modes = metadata.get("supported_volume_modes") or ["ro", "rw"]
    if not isinstance(supported_modes, list) or not supported_modes:
        supported_modes = ["ro", "rw"]
    default_volume_mode = (
        metadata.get("default_volume_mode")
        or defaults.get("volume_mode")
        or supported_modes[0]
    )
    return {
        "name": workflow_name,
        "version": metadata.get("version", "0.6.0"),
        "description": metadata.get("description", ""),
        "author": metadata.get("author"),
        "tags": metadata.get("tags", []),
        "parameters": metadata.get("parameters", {}),
        "default_parameters": metadata.get("default_parameters", {}),
        "required_modules": metadata.get("required_modules", []),
        "supported_volume_modes": supported_modes,
        "default_target_path": default_target_path,
        "default_volume_mode": default_volume_mode,
        "has_custom_docker": bool(info.has_docker),
    }


@mcp.tool
async def list_workflows_mcp() -> Dict[str, Any]:
    """List all discovered workflows and their metadata summary."""
    not_ready = _prefect_not_ready_status()
    if not_ready:
        return {
            "workflows": [],
            "prefect": not_ready,
            "message": "Prefect infrastructure is still initializing",
        }

    workflows_summary = []
    for name, info in prefect_mgr.workflows.items():
        metadata = info.metadata
        defaults = metadata.get("default_parameters", {})
        workflows_summary.append({
            "name": name,
            "version": metadata.get("version", "0.6.0"),
            "description": metadata.get("description", ""),
            "author": metadata.get("author"),
            "tags": metadata.get("tags", []),
            "supported_volume_modes": metadata.get("supported_volume_modes", ["ro", "rw"]),
            "default_volume_mode": metadata.get("default_volume_mode")
            or defaults.get("volume_mode")
            or "ro",
            "default_target_path": metadata.get("default_target_path")
            or defaults.get("target_path"),
            "has_custom_docker": bool(info.has_docker),
        })
    return {"workflows": workflows_summary, "prefect": get_prefect_status()}


@mcp.tool
async def get_workflow_metadata_mcp(workflow_name: str) -> Dict[str, Any]:
    """Fetch detailed metadata for a workflow."""
    not_ready = _prefect_not_ready_status()
    if not_ready:
        return {
            "error": "Prefect infrastructure not ready",
            "prefect": not_ready,
        }

    data = _lookup_workflow(workflow_name)
    if not data:
        return {"error": f"Workflow not found: {workflow_name}"}
    return data


@mcp.tool
async def get_workflow_parameters_mcp(workflow_name: str) -> Dict[str, Any]:
    """Return the parameter schema and defaults for a workflow."""
    not_ready = _prefect_not_ready_status()
    if not_ready:
        return {
            "error": "Prefect infrastructure not ready",
            "prefect": not_ready,
        }

    data = _lookup_workflow(workflow_name)
    if not data:
        return {"error": f"Workflow not found: {workflow_name}"}
    return {
        "parameters": data.get("parameters", {}),
        "defaults": data.get("default_parameters", {}),
    }


@mcp.tool
async def get_workflow_metadata_schema_mcp() -> Dict[str, Any]:
    """Return the JSON schema describing workflow metadata files."""
    return WorkflowDiscovery.get_metadata_schema()


@mcp.tool
async def submit_security_scan_mcp(
    workflow_name: str,
    target_path: str | None = None,
    volume_mode: str | None = None,
    parameters: Dict[str, Any] | None = None,
) -> Dict[str, Any] | Dict[str, str]:
    """Submit a Prefect workflow via MCP."""
    try:
        not_ready = _prefect_not_ready_status()
        if not_ready:
            return {
                "error": "Prefect infrastructure not ready",
                "prefect": not_ready,
            }

        workflow_info = prefect_mgr.workflows.get(workflow_name)
        if not workflow_info:
            return {"error": f"Workflow '{workflow_name}' not found"}

        metadata = workflow_info.metadata or {}
        defaults = metadata.get("default_parameters", {})

        resolved_target_path = target_path or metadata.get("default_target_path") or defaults.get("target_path")
        if not resolved_target_path:
            return {
                "error": (
                    "target_path is required and no default_target_path is defined in metadata"
                ),
                "metadata": {
                    "workflow": workflow_name,
                    "default_target_path": metadata.get("default_target_path"),
                },
            }

        requested_volume_mode = volume_mode or metadata.get("default_volume_mode") or defaults.get("volume_mode")
        if not requested_volume_mode:
            requested_volume_mode = "ro"

        normalised_volume_mode = (
            str(requested_volume_mode).strip().lower().replace("-", "_")
        )
        if normalised_volume_mode in {"read_only", "readonly", "ro"}:
            normalised_volume_mode = "ro"
        elif normalised_volume_mode in {"read_write", "readwrite", "rw"}:
            normalised_volume_mode = "rw"
        else:
            supported_modes = metadata.get("supported_volume_modes", ["ro", "rw"])
            if isinstance(supported_modes, list) and normalised_volume_mode in supported_modes:
                pass
            else:
                normalised_volume_mode = "ro"

        parameters = parameters or {}

        cleaned_parameters: Dict[str, Any] = {**defaults, **parameters}

        # Ensure *_config structures default to dicts so Prefect validation passes.
        for key, value in list(cleaned_parameters.items()):
            if isinstance(key, str) and key.endswith("_config") and value is None:
                cleaned_parameters[key] = {}

        # Some workflows expect configuration dictionaries even when omitted.
        parameter_definitions = (
            metadata.get("parameters", {}).get("properties", {})
            if isinstance(metadata.get("parameters"), dict)
            else {}
        )
        for key, definition in parameter_definitions.items():
            if not isinstance(key, str) or not key.endswith("_config"):
                continue
            if key not in cleaned_parameters:
                default_value = definition.get("default") if isinstance(definition, dict) else None
                cleaned_parameters[key] = default_value if default_value is not None else {}
            elif cleaned_parameters[key] is None:
                cleaned_parameters[key] = {}

        flow_run = await prefect_mgr.submit_workflow(
            workflow_name=workflow_name,
            target_path=resolved_target_path,
            volume_mode=normalised_volume_mode,
            parameters=cleaned_parameters,
        )

        return {
            "run_id": str(flow_run.id),
            "status": flow_run.state.name if flow_run.state else "PENDING",
            "workflow": workflow_name,
            "message": f"Workflow '{workflow_name}' submitted successfully",
            "target_path": resolved_target_path,
            "volume_mode": normalised_volume_mode,
            "parameters": cleaned_parameters,
            "mcp_enabled": True,
        }
    except Exception as exc:  # pragma: no cover - defensive logging
        logger.exception("MCP submit failed")
        return {"error": f"Failed to submit workflow: {exc}"}


@mcp.tool
async def get_comprehensive_scan_summary(run_id: str) -> Dict[str, Any] | Dict[str, str]:
    """Return a summary for the given flow run via MCP."""
    try:
        not_ready = _prefect_not_ready_status()
        if not_ready:
            return {
                "error": "Prefect infrastructure not ready",
                "prefect": not_ready,
            }

        status = await prefect_mgr.get_flow_run_status(run_id)
        findings = await prefect_mgr.get_flow_run_findings(run_id)

        workflow_name = "unknown"
        deployment_id = status.get("workflow", "")
        for name, deployment in prefect_mgr.deployments.items():
            if str(deployment) == str(deployment_id):
                workflow_name = name
                break

        total_findings = 0
        severity_summary = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}

        if findings and "sarif" in findings:
            sarif = findings["sarif"]
            if isinstance(sarif, dict):
                total_findings = sarif.get("total_findings", 0)

        return {
            "run_id": run_id,
            "workflow": workflow_name,
            "status": status.get("status", "unknown"),
            "is_completed": status.get("is_completed", False),
            "total_findings": total_findings,
            "severity_summary": severity_summary,
            "scan_duration": status.get("updated_at", "")
            if status.get("is_completed")
            else "In progress",
            "recommendations": (
                [
                    "Review high and critical severity findings first",
                    "Implement security fixes based on finding recommendations",
                    "Re-run scan after applying fixes to verify remediation",
                ]
                if total_findings > 0
                else ["No security issues found"]
            ),
            "mcp_analysis": True,
        }
    except Exception as exc:  # pragma: no cover
        logger.exception("MCP summary failed")
        return {"error": f"Failed to summarize run: {exc}"}


@mcp.tool
async def get_run_status_mcp(run_id: str) -> Dict[str, Any]:
    """Return current status information for a Prefect run."""
    try:
        not_ready = _prefect_not_ready_status()
        if not_ready:
            return {
                "error": "Prefect infrastructure not ready",
                "prefect": not_ready,
            }

        status = await prefect_mgr.get_flow_run_status(run_id)
        workflow_name = "unknown"
        deployment_id = status.get("workflow", "")
        for name, deployment in prefect_mgr.deployments.items():
            if str(deployment) == str(deployment_id):
                workflow_name = name
                break

        return {
            "run_id": status["run_id"],
            "workflow": workflow_name,
            "status": status["status"],
            "is_completed": status["is_completed"],
            "is_failed": status["is_failed"],
            "is_running": status["is_running"],
            "created_at": status["created_at"],
            "updated_at": status["updated_at"],
        }
    except Exception as exc:
        logger.exception("MCP run status failed")
        return {"error": f"Failed to get run status: {exc}"}


@mcp.tool
async def get_run_findings_mcp(run_id: str) -> Dict[str, Any]:
    """Return SARIF findings for a completed run."""
    try:
        not_ready = _prefect_not_ready_status()
        if not_ready:
            return {
                "error": "Prefect infrastructure not ready",
                "prefect": not_ready,
            }

        status = await prefect_mgr.get_flow_run_status(run_id)
        if not status.get("is_completed"):
            return {"error": f"Run {run_id} not completed. Status: {status.get('status')}"}

        findings = await prefect_mgr.get_flow_run_findings(run_id)

        workflow_name = "unknown"
        deployment_id = status.get("workflow", "")
        for name, deployment in prefect_mgr.deployments.items():
            if str(deployment) == str(deployment_id):
                workflow_name = name
                break

        metadata = {
            "completion_time": status.get("updated_at"),
            "workflow_version": "unknown",
        }
        info = prefect_mgr.workflows.get(workflow_name)
        if info:
            metadata["workflow_version"] = info.metadata.get("version", "unknown")

        return {
            "workflow": workflow_name,
            "run_id": run_id,
            "sarif": findings,
            "metadata": metadata,
        }
    except Exception as exc:
        logger.exception("MCP findings failed")
        return {"error": f"Failed to retrieve findings: {exc}"}


@mcp.tool
async def list_recent_runs_mcp(
    limit: int = 10,
    workflow_name: str | None = None,
    states: List[str] | None = None,
) -> Dict[str, Any]:
    """List recent Prefect runs with optional workflow/state filters."""

    not_ready = _prefect_not_ready_status()
    if not_ready:
        return {
            "runs": [],
            "prefect": not_ready,
            "message": "Prefect infrastructure is still initializing",
        }

    try:
        limit_value = int(limit)
    except (TypeError, ValueError):
        limit_value = 10
    limit_value = max(1, min(limit_value, 100))

    deployment_map = {
        str(deployment_id): workflow
        for workflow, deployment_id in prefect_mgr.deployments.items()
    }

    deployment_filter_value = None
    if workflow_name:
        deployment_id = prefect_mgr.deployments.get(workflow_name)
        if not deployment_id:
            return {
                "runs": [],
                "prefect": get_prefect_status(),
                "error": f"Workflow '{workflow_name}' has no registered deployment",
            }
        try:
            deployment_filter_value = UUID(str(deployment_id))
        except ValueError:
            return {
                "runs": [],
                "prefect": get_prefect_status(),
                "error": (
                    f"Deployment id '{deployment_id}' for workflow '{workflow_name}' is invalid"
                ),
            }

    desired_state_types: List[StateType] = []
    if states:
        for raw_state in states:
            if not raw_state:
                continue
            normalised = raw_state.strip().upper()
            if normalised == "ALL":
                desired_state_types = []
                break
            try:
                desired_state_types.append(StateType[normalised])
            except KeyError:
                continue
    if not desired_state_types:
        desired_state_types = [
            StateType.RUNNING,
            StateType.COMPLETED,
            StateType.FAILED,
            StateType.CANCELLED,
        ]

    flow_filter = FlowRunFilter()
    if desired_state_types:
        flow_filter.state = FlowRunFilterState(
            type=FlowRunFilterStateType(any_=desired_state_types)
        )
    if deployment_filter_value:
        flow_filter.deployment_id = FlowRunFilterDeploymentId(
            any_=[deployment_filter_value]
        )

    async with get_client() as client:
        flow_runs = await client.read_flow_runs(
            limit=limit_value,
            flow_run_filter=flow_filter,
            sort=FlowRunSort.START_TIME_DESC,
        )

    results: List[Dict[str, Any]] = []
    for flow_run in flow_runs:
        deployment_id = getattr(flow_run, "deployment_id", None)
        workflow = deployment_map.get(str(deployment_id), "unknown")
        state = getattr(flow_run, "state", None)
        state_name = getattr(state, "name", None) if state else None
        state_type = getattr(state, "type", None) if state else None

        results.append(
            {
                "run_id": str(flow_run.id),
                "workflow": workflow,
                "deployment_id": str(deployment_id) if deployment_id else None,
                "state": state_name or (state_type.name if state_type else None),
                "state_type": state_type.name if state_type else None,
                "is_completed": bool(getattr(state, "is_completed", lambda: False)()),
                "is_running": bool(getattr(state, "is_running", lambda: False)()),
                "is_failed": bool(getattr(state, "is_failed", lambda: False)()),
                "created_at": getattr(flow_run, "created", None),
                "updated_at": getattr(flow_run, "updated", None),
                "expected_start_time": getattr(flow_run, "expected_start_time", None),
                "start_time": getattr(flow_run, "start_time", None),
            }
        )

    # Normalise datetimes to ISO 8601 strings for serialization
    for entry in results:
        for key in ("created_at", "updated_at", "expected_start_time", "start_time"):
            value = entry.get(key)
            if value is None:
                continue
            try:
                entry[key] = value.isoformat()
            except AttributeError:
                entry[key] = str(value)

    return {"runs": results, "prefect": get_prefect_status()}


@mcp.tool
async def get_fuzzing_stats_mcp(run_id: str) -> Dict[str, Any]:
    """Return fuzzing statistics for a run if available."""
    not_ready = _prefect_not_ready_status()
    if not_ready:
        return {
            "error": "Prefect infrastructure not ready",
            "prefect": not_ready,
        }

    stats = fuzzing.fuzzing_stats.get(run_id)
    if not stats:
        return {"error": f"Fuzzing run not found: {run_id}"}
    # Be resilient if a plain dict slipped into the cache
    if isinstance(stats, dict):
        return stats
    if hasattr(stats, "model_dump"):
        return stats.model_dump()
    if hasattr(stats, "dict"):
        return stats.dict()
    # Last resort
    return getattr(stats, "__dict__", {"run_id": run_id})


@mcp.tool
async def get_fuzzing_crash_reports_mcp(run_id: str) -> Dict[str, Any]:
    """Return crash reports collected for a fuzzing run."""
    not_ready = _prefect_not_ready_status()
    if not_ready:
        return {
            "error": "Prefect infrastructure not ready",
            "prefect": not_ready,
        }

    reports = fuzzing.crash_reports.get(run_id)
    if reports is None:
        return {"error": f"Fuzzing run not found: {run_id}"}
    return {"run_id": run_id, "crashes": [report.model_dump() for report in reports]}


@mcp.tool
async def get_backend_status_mcp() -> Dict[str, Any]:
    """Expose backend readiness, workflows, and registered MCP tools."""

    status = get_prefect_status()
    response: Dict[str, Any] = {"prefect": status}

    if status.get("ready"):
        response["workflows"] = list(prefect_mgr.workflows.keys())

    try:
        tools = await mcp._tool_manager.list_tools()
        response["mcp_tools"] = sorted(tool.name for tool in tools)
    except Exception as exc:  # pragma: no cover - defensive logging
        logger.debug("Failed to enumerate MCP tools: %s", exc)

    return response


def create_mcp_transport_app() -> Starlette:
    """Build a Starlette app serving HTTP + SSE transports on one port."""

    http_app = mcp.http_app(path="/", transport="streamable-http")
    sse_app = create_sse_app(
        server=mcp,
        message_path="/messages",
        sse_path="/",
        auth=mcp.auth,
    )

    routes = [
        Mount("/mcp", app=http_app),
        Mount("/mcp/sse", app=sse_app),
    ]

    @asynccontextmanager
    async def lifespan(app: Starlette):  # pragma: no cover - integration wiring
        async with AsyncExitStack() as stack:
            await stack.enter_async_context(
                http_app.router.lifespan_context(http_app)
            )
            await stack.enter_async_context(
                sse_app.router.lifespan_context(sse_app)
            )
            yield

    combined_app = Starlette(routes=routes, lifespan=lifespan)
    combined_app.state.fastmcp_server = mcp
    combined_app.state.http_app = http_app
    combined_app.state.sse_app = sse_app
    return combined_app


# ---------------------------------------------------------------------------
# Combined lifespan: Prefect init + dedicated MCP transports
# ---------------------------------------------------------------------------

@asynccontextmanager
async def combined_lifespan(app: FastAPI):
    global prefect_bootstrap_task, _fastapi_mcp_imported

    logger.info("Starting FuzzForge backend...")

    # Ensure FastAPI endpoints are exposed via MCP once
    if not _fastapi_mcp_imported:
        try:
            await mcp.import_server(FASTAPI_MCP_ADAPTER)
            _fastapi_mcp_imported = True
            logger.info("Mounted FastAPI endpoints as MCP tools")
        except Exception as exc:
            logger.exception("Failed to import FastAPI endpoints into MCP", exc_info=exc)

    # Kick off Prefect bootstrap in the background if needed
    if prefect_bootstrap_task is None or prefect_bootstrap_task.done():
        prefect_bootstrap_task = asyncio.create_task(_bootstrap_prefect_with_retries())
        logger.info("Prefect bootstrap task started")
    else:
        logger.info("Prefect bootstrap task already running")

    # Start MCP transports on shared port (HTTP + SSE)
    mcp_app = create_mcp_transport_app()
    mcp_config = uvicorn.Config(
        app=mcp_app,
        host="0.0.0.0",
        port=8010,
        log_level="info",
        lifespan="on",
    )
    mcp_server = uvicorn.Server(mcp_config)
    mcp_server.install_signal_handlers = lambda: None  # type: ignore[assignment]
    mcp_task = asyncio.create_task(mcp_server.serve())

    async def _wait_for_uvicorn_startup() -> None:
        started_attr = getattr(mcp_server, "started", None)
        if hasattr(started_attr, "wait"):
            await asyncio.wait_for(started_attr.wait(), timeout=10)
            return

        # Fallback for uvicorn versions where "started" is a bool
        poll_interval = 0.1
        checks = int(10 / poll_interval)
        for _ in range(checks):
            if getattr(mcp_server, "started", False):
                return
            await asyncio.sleep(poll_interval)
        raise asyncio.TimeoutError

    try:
        await _wait_for_uvicorn_startup()
    except asyncio.TimeoutError:  # pragma: no cover - defensive logging
        if mcp_task.done():
            raise RuntimeError("MCP server failed to start") from mcp_task.exception()
        logger.warning("Timed out waiting for MCP server startup; continuing anyway")

    logger.info("MCP HTTP available at http://0.0.0.0:8010/mcp")
    logger.info("MCP SSE available at http://0.0.0.0:8010/mcp/sse")

    try:
        yield
    finally:
        logger.info("Shutting down MCP transports...")
        mcp_server.should_exit = True
        mcp_server.force_exit = True
        await asyncio.gather(mcp_task, return_exceptions=True)

        if prefect_bootstrap_task and not prefect_bootstrap_task.done():
            prefect_bootstrap_task.cancel()
            with suppress(asyncio.CancelledError):
                await prefect_bootstrap_task
        prefect_bootstrap_state.task_running = False
        if not prefect_bootstrap_state.ready:
            prefect_bootstrap_state.status = "stopped"
            prefect_bootstrap_state.next_retry_seconds = None
        prefect_bootstrap_task = None

        logger.info("Shutting down Prefect statistics monitor...")
        await prefect_stats_monitor.stop_monitoring()
        logger.info("Shutting down FuzzForge backend...")


app.router.lifespan_context = combined_lifespan
