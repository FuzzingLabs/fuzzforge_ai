"""
Prefect Manager - Core orchestration for workflow deployment and execution
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
import os
import platform
import re
from pathlib import Path
from typing import Dict, Optional, Any
from prefect import get_client
from prefect.docker import DockerImage
from prefect.client.schemas import FlowRun

from src.core.workflow_discovery import WorkflowDiscovery, WorkflowInfo

logger = logging.getLogger(__name__)


def get_registry_url(context: str = "default") -> str:
    """
    Get the container registry URL to use for a given operation context.

    Goals:
    - Work reliably across Linux and macOS Docker Desktop
    - Prefer in-network service discovery when running inside containers
    - Allow full override via env vars from docker-compose

    Env overrides:
      - FUZZFORGE_REGISTRY_PUSH_URL: used for image builds/pushes
      - FUZZFORGE_REGISTRY_PULL_URL: used for workers to pull images
    """
    # Normalize context
    ctx = (context or "default").lower()

    # Always honor explicit overrides first
    if ctx in ("push", "build"):
        push_url = os.getenv("FUZZFORGE_REGISTRY_PUSH_URL")
        if push_url:
            logger.debug("Using FUZZFORGE_REGISTRY_PUSH_URL: %s", push_url)
            return push_url
        # Default to host-published registry for Docker daemon operations
        return "localhost:5001"

    if ctx == "pull":
        pull_url = os.getenv("FUZZFORGE_REGISTRY_PULL_URL")
        if pull_url:
            logger.debug("Using FUZZFORGE_REGISTRY_PULL_URL: %s", pull_url)
            return pull_url
        # Prefect worker pulls via host Docker daemon as well
        return "localhost:5001"

    # Default/fallback
    return os.getenv("FUZZFORGE_REGISTRY_PULL_URL", os.getenv("FUZZFORGE_REGISTRY_PUSH_URL", "localhost:5001"))


def _compose_project_name(default: str = "fuzzforge_alpha") -> str:
    """Return the docker-compose project name used for network/volume naming.

    Honors COMPOSE_PROJECT_NAME if present; falls back to a sensible default.
    """
    return os.getenv("COMPOSE_PROJECT_NAME", default)


class PrefectManager:
    """
    Manages Prefect deployments and flow runs for discovered workflows.

    This class handles:
    - Workflow discovery and registration
    - Docker image building through Prefect
    - Deployment creation and management
    - Flow run submission with volume mounting
    - Findings retrieval from completed runs
    """

    def __init__(self, workflows_dir: Path = None):
        """
        Initialize the Prefect manager.

        Args:
            workflows_dir: Path to the workflows directory (default: toolbox/workflows)
        """
        if workflows_dir is None:
            workflows_dir = Path("toolbox/workflows")

        self.discovery = WorkflowDiscovery(workflows_dir)
        self.workflows: Dict[str, WorkflowInfo] = {}
        self.deployments: Dict[str, str] = {}  # workflow_name -> deployment_id

        # Security: Define allowed and forbidden paths for host mounting
        self.allowed_base_paths = [
            "/tmp",
            "/home",
            "/Users",  # macOS users
            "/opt",
            "/var/tmp",
            "/workspace",  # Common container workspace
            "/app"  # Container application directory (for test projects)
        ]

        self.forbidden_paths = [
            "/etc",
            "/root",
            "/var/run",
            "/sys",
            "/proc",
            "/dev",
            "/boot",
            "/var/lib/docker",  # Critical Docker data
            "/var/log",  # System logs
            "/usr/bin",  # System binaries
            "/usr/sbin",
            "/sbin",
            "/bin"
        ]

    @staticmethod
    def _parse_memory_to_bytes(memory_str: str) -> int:
        """
        Parse memory string (like '512Mi', '1Gi') to bytes.

        Args:
            memory_str: Memory string with unit suffix

        Returns:
            Memory in bytes

        Raises:
            ValueError: If format is invalid
        """
        if not memory_str:
            return 0

        match = re.match(r'^(\d+(?:\.\d+)?)\s*([GMK]i?)$', memory_str.strip())
        if not match:
            raise ValueError(f"Invalid memory format: {memory_str}. Expected format like '512Mi', '1Gi'")

        value, unit = match.groups()
        value = float(value)

        # Convert to bytes based on unit (binary units: Ki, Mi, Gi)
        if unit in ['K', 'Ki']:
            multiplier = 1024
        elif unit in ['M', 'Mi']:
            multiplier = 1024 * 1024
        elif unit in ['G', 'Gi']:
            multiplier = 1024 * 1024 * 1024
        else:
            raise ValueError(f"Unsupported memory unit: {unit}")

        return int(value * multiplier)

    @staticmethod
    def _parse_cpu_to_millicores(cpu_str: str) -> int:
        """
        Parse CPU string (like '500m', '1', '2.5') to millicores.

        Args:
            cpu_str: CPU string

        Returns:
            CPU in millicores (1 core = 1000 millicores)

        Raises:
            ValueError: If format is invalid
        """
        if not cpu_str:
            return 0

        cpu_str = cpu_str.strip()

        # Handle millicores format (e.g., '500m')
        if cpu_str.endswith('m'):
            try:
                return int(cpu_str[:-1])
            except ValueError:
                raise ValueError(f"Invalid CPU format: {cpu_str}")

        # Handle core format (e.g., '1', '2.5')
        try:
            cores = float(cpu_str)
            return int(cores * 1000)  # Convert to millicores
        except ValueError:
            raise ValueError(f"Invalid CPU format: {cpu_str}")

    def _extract_resource_requirements(self, workflow_info: WorkflowInfo) -> Dict[str, str]:
        """
        Extract resource requirements from workflow metadata.

        Args:
            workflow_info: Workflow information with metadata

        Returns:
            Dictionary with resource requirements in Docker format
        """
        metadata = workflow_info.metadata
        requirements = metadata.get("requirements", {})
        resources = requirements.get("resources", {})

        resource_config = {}

        # Extract memory requirement
        memory = resources.get("memory")
        if memory:
            try:
                # Validate memory format and store original string for Docker
                self._parse_memory_to_bytes(memory)
                resource_config["memory"] = memory
            except ValueError as e:
                logger.warning(f"Invalid memory requirement in {workflow_info.name}: {e}")

        # Extract CPU requirement
        cpu = resources.get("cpu")
        if cpu:
            try:
                # Validate CPU format and store original string for Docker
                self._parse_cpu_to_millicores(cpu)
                resource_config["cpus"] = cpu
            except ValueError as e:
                logger.warning(f"Invalid CPU requirement in {workflow_info.name}: {e}")

        # Extract timeout
        timeout = resources.get("timeout")
        if timeout and isinstance(timeout, int):
            resource_config["timeout"] = str(timeout)

        return resource_config

    async def initialize(self):
        """
        Initialize the manager by discovering and deploying all workflows.

        This method:
        1. Discovers all valid workflows in the workflows directory
        2. Validates their metadata
        3. Deploys each workflow to Prefect with Docker images
        """
        try:
            # Discover workflows
            self.workflows = await self.discovery.discover_workflows()

            if not self.workflows:
                logger.warning("No workflows discovered")
                return

            logger.info(f"Discovered {len(self.workflows)} workflows: {list(self.workflows.keys())}")

            # Deploy each workflow
            for name, info in self.workflows.items():
                try:
                    await self._deploy_workflow(name, info)
                except Exception as e:
                    logger.error(f"Failed to deploy workflow '{name}': {e}")

        except Exception as e:
            logger.error(f"Failed to initialize Prefect manager: {e}")
            raise

    async def _deploy_workflow(self, name: str, info: WorkflowInfo):
        """
        Deploy a single workflow to Prefect with Docker image.

        Args:
            name: Workflow name
            info: Workflow information including metadata and paths
        """
        logger.info(f"Deploying workflow '{name}'...")

        # Get the flow function from registry
        flow_func = self.discovery.get_flow_function(name)
        if not flow_func:
            logger.error(
                f"Failed to get flow function for '{name}' from registry. "
                f"Ensure the workflow is properly registered in toolbox/workflows/registry.py"
            )
            return

        # Use the mandatory Dockerfile with absolute paths for Docker Compose
        # Get absolute paths for build context and dockerfile
        toolbox_path = info.path.parent.parent.resolve()
        dockerfile_abs_path = info.dockerfile.resolve()

        # Calculate relative dockerfile path from toolbox context
        try:
            dockerfile_rel_path = dockerfile_abs_path.relative_to(toolbox_path)
        except ValueError:
            # If relative path fails, use the workflow-specific path
            dockerfile_rel_path = Path("workflows") / name / "Dockerfile"

        # Determine deployment strategy based on Dockerfile presence
        base_image = "prefecthq/prefect:3-python3.11"
        has_custom_dockerfile = info.has_docker and info.dockerfile.exists()

        logger.info(f"=== DEPLOYMENT DEBUG for '{name}' ===")
        logger.info(f"info.has_docker: {info.has_docker}")
        logger.info(f"info.dockerfile: {info.dockerfile}")
        logger.info(f"info.dockerfile.exists(): {info.dockerfile.exists()}")
        logger.info(f"has_custom_dockerfile: {has_custom_dockerfile}")
        logger.info(f"toolbox_path: {toolbox_path}")
        logger.info(f"dockerfile_rel_path: {dockerfile_rel_path}")

        if has_custom_dockerfile:
            logger.info(f"Workflow '{name}' has custom Dockerfile - building custom image")
            # Decide whether to use registry or keep images local to host engine
            import os
            # Default to using the local registry; set FUZZFORGE_USE_REGISTRY=false to bypass (not recommended)
            use_registry = os.getenv("FUZZFORGE_USE_REGISTRY", "true").lower() == "true"

            if use_registry:
                registry_url = get_registry_url(context="push")
                image_spec = DockerImage(
                    name=f"{registry_url}/fuzzforge/{name}",
                    tag="latest",
                    dockerfile=str(dockerfile_rel_path),
                    context=str(toolbox_path)
                )
                deploy_image = f"{registry_url}/fuzzforge/{name}:latest"
                build_custom = True
                push_custom = True
                logger.info(f"Using registry: {registry_url} for '{name}'")
            else:
                # Single-host mode: build into host engine cache; no push required
                image_spec = DockerImage(
                    name=f"fuzzforge/{name}",
                    tag="latest",
                    dockerfile=str(dockerfile_rel_path),
                    context=str(toolbox_path)
                )
                deploy_image = f"fuzzforge/{name}:latest"
                build_custom = True
                push_custom = False
                logger.info("Using single-host image (no registry push): %s", deploy_image)
        else:
            logger.info(f"Workflow '{name}' using base image - no custom dependencies needed")
            deploy_image = base_image
            build_custom = False
            push_custom = False

        # Pre-validate registry connectivity when pushing
        if push_custom:
            try:
                from .setup import validate_registry_connectivity
                await validate_registry_connectivity(registry_url)
                logger.info(f"Registry connectivity validated for {registry_url}")
            except Exception as e:
                logger.error(f"Registry connectivity validation failed for {registry_url}: {e}")
                raise RuntimeError(f"Cannot deploy workflow '{name}': Registry {registry_url} is not accessible. {e}")

        # Deploy the workflow
        try:
            # Ensure any previous deployment is removed so job variables are updated
            try:
                async with get_client() as client:
                    existing = await client.read_deployment_by_name(
                        f"{name}/{name}-deployment"
                    )
                    if existing:
                        logger.info(f"Removing existing deployment for '{name}' to refresh settings...")
                        await client.delete_deployment(existing.id)
            except Exception:
                # If not found or deletion fails, continue with deployment
                pass

            # Extract resource requirements from metadata
            workflow_resource_requirements = self._extract_resource_requirements(info)
            logger.info(f"Workflow '{name}' resource requirements: {workflow_resource_requirements}")

            # Build job variables with resource requirements
            job_variables = {
                "image": deploy_image,  # Use the worker-accessible registry name
                "volumes": [],  # Populated at run submission with toolbox mount
                "env": {
                    "PYTHONPATH": "/opt/prefect/toolbox:/opt/prefect/toolbox/workflows",
                    "WORKFLOW_NAME": name
                }
            }

            # Add resource requirements to job variables if present
            if workflow_resource_requirements:
                job_variables["resources"] = workflow_resource_requirements

            # Prepare deployment parameters
            deploy_params = {
                "name": f"{name}-deployment",
                "work_pool_name": "docker-pool",
                "image": image_spec if has_custom_dockerfile else deploy_image,
                "push": push_custom,
                "build": build_custom,
                "job_variables": job_variables
            }

            deployment = await flow_func.deploy(**deploy_params)

            self.deployments[name] = str(deployment.id) if hasattr(deployment, 'id') else name
            logger.info(f"Successfully deployed workflow '{name}'")

        except Exception as e:
            # Enhanced error reporting with more context
            import traceback
            logger.error(f"Failed to deploy workflow '{name}': {e}")
            logger.error(f"Deployment traceback: {traceback.format_exc()}")

            # Try to capture Docker-specific context
            error_context = {
                "workflow_name": name,
                "has_dockerfile": has_custom_dockerfile,
                "image_name": deploy_image if 'deploy_image' in locals() else "unknown",
                "registry_url": registry_url if 'registry_url' in locals() else "unknown",
                "error_type": type(e).__name__,
                "error_message": str(e)
            }

            # Check for specific error patterns with detailed categorization
            error_msg_lower = str(e).lower()
            if "registry" in error_msg_lower and ("no such host" in error_msg_lower or "connection" in error_msg_lower):
                error_context["category"] = "registry_connectivity_error"
                error_context["solution"] = f"Cannot reach registry at {error_context['registry_url']}. Check Docker network and registry service."
            elif "docker" in error_msg_lower:
                error_context["category"] = "docker_error"
                if "build" in error_msg_lower:
                    error_context["subcategory"] = "image_build_failed"
                    error_context["solution"] = "Check Dockerfile syntax and dependencies."
                elif "pull" in error_msg_lower:
                    error_context["subcategory"] = "image_pull_failed"
                    error_context["solution"] = "Check if image exists in registry and network connectivity."
                elif "push" in error_msg_lower:
                    error_context["subcategory"] = "image_push_failed"
                    error_context["solution"] = f"Check registry connectivity and push permissions to {error_context['registry_url']}."
            elif "registry" in error_msg_lower:
                error_context["category"] = "registry_error"
                error_context["solution"] = "Check registry configuration and accessibility."
            elif "prefect" in error_msg_lower:
                error_context["category"] = "prefect_error"
                error_context["solution"] = "Check Prefect server connectivity and deployment configuration."
            else:
                error_context["category"] = "unknown_deployment_error"
                error_context["solution"] = "Check logs for more specific error details."

            logger.error(f"Deployment error context: {error_context}")

            # Raise enhanced exception with context
            enhanced_error = Exception(f"Deployment failed for workflow '{name}': {str(e)} | Context: {error_context}")
            enhanced_error.original_error = e
            enhanced_error.context = error_context
            raise enhanced_error

    async def submit_workflow(
        self,
        workflow_name: str,
        target_path: str,
        volume_mode: str = "ro",
        parameters: Dict[str, Any] = None,
        resource_limits: Dict[str, str] = None,
        additional_volumes: list = None,
        timeout: int = None
    ) -> FlowRun:
        """
        Submit a workflow for execution with volume mounting.

        Args:
            workflow_name: Name of the workflow to execute
            target_path: Host path to mount as volume
            volume_mode: Volume mount mode ("ro" for read-only, "rw" for read-write)
            parameters: Workflow-specific parameters
            resource_limits: CPU/memory limits for container
            additional_volumes: List of additional volume mounts
            timeout: Timeout in seconds

        Returns:
            FlowRun object with run information

        Raises:
            ValueError: If workflow not found or volume mode not supported
        """
        if workflow_name not in self.workflows:
            raise ValueError(f"Unknown workflow: {workflow_name}")

        # Validate volume mode
        workflow_info = self.workflows[workflow_name]
        supported_modes = workflow_info.metadata.get("supported_volume_modes", ["ro", "rw"])

        if volume_mode not in supported_modes:
            raise ValueError(
                f"Workflow '{workflow_name}' doesn't support volume mode '{volume_mode}'. "
                f"Supported modes: {supported_modes}"
            )

        # Validate target path with security checks
        self._validate_target_path(target_path)

        # Validate additional volumes if provided
        if additional_volumes:
            for volume in additional_volumes:
                self._validate_target_path(volume.host_path)

        async with get_client() as client:
            # Get the deployment, auto-redeploy once if missing
            try:
                deployment = await client.read_deployment_by_name(
                    f"{workflow_name}/{workflow_name}-deployment"
                )
            except Exception as e:
                import traceback
                logger.error(f"Failed to find deployment for workflow '{workflow_name}': {e}")
                logger.error(f"Deployment lookup traceback: {traceback.format_exc()}")

                # Attempt a one-time auto-deploy to recover from startup races
                try:
                    logger.info(f"Auto-deploying missing workflow '{workflow_name}' and retrying...")
                    await self._deploy_workflow(workflow_name, workflow_info)
                    deployment = await client.read_deployment_by_name(
                        f"{workflow_name}/{workflow_name}-deployment"
                    )
                except Exception as redeploy_exc:
                    # Enhanced error with context
                    error_context = {
                        "workflow_name": workflow_name,
                        "error_type": type(e).__name__,
                        "error_message": str(e),
                        "redeploy_error": str(redeploy_exc),
                        "available_deployments": list(self.deployments.keys()),
                    }
                    enhanced_error = ValueError(
                        f"Deployment not found and redeploy failed for workflow '{workflow_name}': {e} | Context: {error_context}"
                    )
                    enhanced_error.context = error_context
                    raise enhanced_error

            # Determine the Docker Compose network name and volume names
            # Docker Compose creates networks with pattern: {project_name}_default
            import os
            compose_project = _compose_project_name('fuzzforge_alpha')
            docker_network = f"{compose_project}_default"

            # Build volume mounts
            # Add toolbox volume mount for workflow code access
            backend_toolbox_path = "/app/toolbox"  # Path in backend container

            # Use dynamic volume names based on Docker Compose project name
            prefect_storage_volume = f"{compose_project}_prefect_storage"
            toolbox_code_volume = f"{compose_project}_toolbox_code"

            volumes = [
                f"{target_path}:/workspace:{volume_mode}",
                f"{prefect_storage_volume}:/prefect-storage",  # Shared storage for results
                f"{toolbox_code_volume}:/opt/prefect/toolbox:ro"  # Mount workflow code
            ]

            # Add additional volumes if provided
            if additional_volumes:
                for volume in additional_volumes:
                    volume_spec = f"{volume.host_path}:{volume.container_path}:{volume.mode}"
                    volumes.append(volume_spec)

            # Build environment variables
            env_vars = {
                "PREFECT_API_URL": "http://prefect-server:4200/api",  # Use internal network hostname
                "PREFECT_LOGGING_LEVEL": "INFO",
                "PREFECT_LOCAL_STORAGE_PATH": "/prefect-storage",  # Use shared storage
                "PREFECT_RESULTS_PERSIST_BY_DEFAULT": "true",  # Enable result persistence
                "PREFECT_DEFAULT_RESULT_STORAGE_BLOCK": "local-file-system/fuzzforge-results",  # Use our storage block
                "WORKSPACE_PATH": "/workspace",
                "VOLUME_MODE": volume_mode,
                "WORKFLOW_NAME": workflow_name
            }

            # Add additional volume paths to environment for easy access
            if additional_volumes:
                for i, volume in enumerate(additional_volumes):
                    env_vars[f"ADDITIONAL_VOLUME_{i}_PATH"] = volume.container_path

            # Determine which image to use based on workflow configuration
            workflow_info = self.workflows[workflow_name]
            has_custom_dockerfile = workflow_info.has_docker and workflow_info.dockerfile.exists()
            # Use pull context for worker to pull from registry
            registry_url = get_registry_url(context="pull")
            workflow_image = f"{registry_url}/fuzzforge/{workflow_name}:latest" if has_custom_dockerfile else "prefecthq/prefect:3-python3.11"
            logger.debug(f"Worker will pull image: {workflow_image} (Registry: {registry_url})")

            # Configure job variables with volume mounting and network access
            job_variables = {
                # Use custom image if available, otherwise base Prefect image
                "image": workflow_image,
                "volumes": volumes,
                "networks": [docker_network],  # Connect to Docker Compose network
                "env": {
                    **env_vars,
                    "PYTHONPATH": "/opt/prefect/toolbox:/opt/prefect/toolbox/workflows",
                    "WORKFLOW_NAME": workflow_name
                }
            }

            # Apply resource requirements from workflow metadata and user overrides
            workflow_resource_requirements = self._extract_resource_requirements(workflow_info)
            final_resource_config = {}

            # Start with workflow requirements as base
            if workflow_resource_requirements:
                final_resource_config.update(workflow_resource_requirements)

            # Apply user-provided resource limits (overrides workflow defaults)
            if resource_limits:
                user_resource_config = {}
                if resource_limits.get("cpu_limit"):
                    user_resource_config["cpus"] = resource_limits["cpu_limit"]
                if resource_limits.get("memory_limit"):
                    user_resource_config["memory"] = resource_limits["memory_limit"]
                # Note: cpu_request and memory_request are not directly supported by Docker
                # but could be used for Kubernetes in the future

                # User overrides take precedence
                final_resource_config.update(user_resource_config)

            # Apply final resource configuration
            if final_resource_config:
                job_variables["resources"] = final_resource_config
                logger.info(f"Applied resource limits: {final_resource_config}")

            # Merge parameters with defaults from metadata
            default_params = workflow_info.metadata.get("default_parameters", {})
            final_params = {**default_params, **(parameters or {})}

            # Set flow parameters that match the flow signature
            final_params["target_path"] = "/workspace"  # Container path where volume is mounted
            final_params["volume_mode"] = volume_mode

            # Create and submit the flow run
            # Pass job_variables to ensure network, volumes, and environment are configured
            logger.info(f"Submitting flow with job_variables: {job_variables}")
            logger.info(f"Submitting flow with parameters: {final_params}")

            # Prepare flow run creation parameters
            flow_run_params = {
                "deployment_id": deployment.id,
                "parameters": final_params,
                "job_variables": job_variables
            }

            # Note: Timeout is handled through workflow-level configuration
            # Additional timeout configuration can be added to deployment metadata if needed

            flow_run = await client.create_flow_run_from_deployment(**flow_run_params)

            logger.info(
                f"Submitted workflow '{workflow_name}' with run_id: {flow_run.id}, "
                f"target: {target_path}, mode: {volume_mode}"
            )

            return flow_run

    async def get_flow_run_findings(self, run_id: str) -> Dict[str, Any]:
        """
        Retrieve findings from a completed flow run.

        Args:
            run_id: The flow run ID

        Returns:
            Dictionary containing SARIF-formatted findings

        Raises:
            ValueError: If run not completed or not found
        """
        async with get_client() as client:
            flow_run = await client.read_flow_run(run_id)

            if not flow_run.state.is_completed():
                raise ValueError(
                    f"Flow run {run_id} not completed. Current status: {flow_run.state.name}"
                )

            # Get the findings from the flow run result
            try:
                findings = await flow_run.state.result()
                return findings
            except Exception as e:
                logger.error(f"Failed to retrieve findings for run {run_id}: {e}")
                raise ValueError(f"Failed to retrieve findings: {e}")

    async def get_flow_run_status(self, run_id: str) -> Dict[str, Any]:
        """
        Get the current status of a flow run.

        Args:
            run_id: The flow run ID

        Returns:
            Dictionary with status information
        """
        async with get_client() as client:
            flow_run = await client.read_flow_run(run_id)

            return {
                "run_id": str(flow_run.id),
                "workflow": flow_run.deployment_id,
                "status": flow_run.state.name,
                "is_completed": flow_run.state.is_completed(),
                "is_failed": flow_run.state.is_failed(),
                "is_running": flow_run.state.is_running(),
                "created_at": flow_run.created,
                "updated_at": flow_run.updated
            }

    def _validate_target_path(self, target_path: str) -> None:
        """
        Validate target path for security before mounting as volume.

        Args:
            target_path: Host path to validate

        Raises:
            ValueError: If path is not allowed for security reasons
        """
        target = Path(target_path)

        # Path must be absolute
        if not target.is_absolute():
            raise ValueError(f"Target path must be absolute: {target_path}")

        # Resolve path to handle symlinks and relative components
        try:
            resolved_path = target.resolve()
        except (OSError, RuntimeError) as e:
            raise ValueError(f"Cannot resolve target path: {target_path} - {e}")

        resolved_str = str(resolved_path)

        # Check against forbidden paths first (more restrictive)
        for forbidden in self.forbidden_paths:
            if resolved_str.startswith(forbidden):
                raise ValueError(
                    f"Access denied: Path '{target_path}' resolves to forbidden directory '{forbidden}'. "
                    f"This path contains sensitive system files and cannot be mounted."
                )

        # Check if path starts with any allowed base path
        path_allowed = False
        for allowed in self.allowed_base_paths:
            if resolved_str.startswith(allowed):
                path_allowed = True
                break

        if not path_allowed:
            allowed_list = ", ".join(self.allowed_base_paths)
            raise ValueError(
                f"Access denied: Path '{target_path}' is not in allowed directories. "
                f"Allowed base paths: {allowed_list}"
            )

        # Additional security checks
        if resolved_str == "/":
            raise ValueError("Cannot mount root filesystem")

        # Warn if path doesn't exist (but don't block - it might be created later)
        if not resolved_path.exists():
            logger.warning(f"Target path does not exist: {target_path}")

        logger.info(f"Path validation passed for: {target_path} -> {resolved_str}")
