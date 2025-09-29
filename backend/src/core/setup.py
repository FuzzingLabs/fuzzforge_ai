"""
Setup utilities for Prefect infrastructure
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
from prefect import get_client
from prefect.client.schemas.actions import WorkPoolCreate
from prefect.client.schemas.objects import WorkPool
from .prefect_manager import get_registry_url

logger = logging.getLogger(__name__)


async def setup_docker_pool():
    """
    Create or update the Docker work pool for container execution.

    This work pool is configured to:
    - Connect to the local Docker daemon
    - Support volume mounting at runtime
    - Clean up containers after execution
    - Use bridge networking by default
    """
    import os

    async with get_client() as client:
        pool_name = "docker-pool"

        # Add force recreation flag for debugging fresh install issues
        force_recreate = os.getenv('FORCE_RECREATE_WORK_POOL', 'false').lower() == 'true'
        debug_setup = os.getenv('DEBUG_WORK_POOL_SETUP', 'false').lower() == 'true'

        if force_recreate:
            logger.warning(f"FORCE_RECREATE_WORK_POOL=true - Will recreate work pool regardless of existing configuration")
        if debug_setup:
            logger.warning(f"DEBUG_WORK_POOL_SETUP=true - Enhanced logging enabled")
            # Temporarily set logging level to DEBUG for this function
            original_level = logger.level
            logger.setLevel(logging.DEBUG)

        try:
            # Check if pool already exists and supports custom images
            existing_pools = await client.read_work_pools()
            existing_pool = None
            for pool in existing_pools:
                if pool.name == pool_name:
                    existing_pool = pool
                    break

            if existing_pool and not force_recreate:
                logger.info(f"Found existing work pool '{pool_name}' - validating configuration...")

                # Check if the existing pool has the correct configuration
                base_template = existing_pool.base_job_template or {}
                logger.debug(f"Base template keys: {list(base_template.keys())}")

                job_config = base_template.get("job_configuration", {})
                logger.debug(f"Job config keys: {list(job_config.keys())}")

                image_config = job_config.get("image", "")
                has_image_variable = "{{ image }}" in str(image_config)
                logger.debug(f"Image config: '{image_config}' -> has_image_variable: {has_image_variable}")

                # Check if volume defaults include toolbox mount
                variables = base_template.get("variables", {})
                properties = variables.get("properties", {})
                volume_config = properties.get("volumes", {})
                volume_defaults = volume_config.get("default", [])
                has_toolbox_volume = any("toolbox_code" in str(vol) for vol in volume_defaults) if volume_defaults else False
                logger.debug(f"Volume defaults: {volume_defaults}")
                logger.debug(f"Has toolbox volume: {has_toolbox_volume}")

                # Check if environment defaults include required settings
                env_config = properties.get("env", {})
                env_defaults = env_config.get("default", {})
                has_api_url = "PREFECT_API_URL" in env_defaults
                has_storage_path = "PREFECT_LOCAL_STORAGE_PATH" in env_defaults
                has_results_persist = "PREFECT_RESULTS_PERSIST_BY_DEFAULT" in env_defaults
                has_required_env = has_api_url and has_storage_path and has_results_persist
                logger.debug(f"Environment defaults: {env_defaults}")
                logger.debug(f"Has API URL: {has_api_url}, Has storage path: {has_storage_path}, Has results persist: {has_results_persist}")
                logger.debug(f"Has required env: {has_required_env}")

                # Log the full validation result
                logger.info(f"Work pool validation - Image: {has_image_variable}, Toolbox: {has_toolbox_volume}, Environment: {has_required_env}")

                if has_image_variable and has_toolbox_volume and has_required_env:
                    logger.info(f"Docker work pool '{pool_name}' already exists with correct configuration")
                    return
                else:
                    reasons = []
                    if not has_image_variable:
                        reasons.append("missing image template")
                    if not has_toolbox_volume:
                        reasons.append("missing toolbox volume mount")
                    if not has_required_env:
                        if not has_api_url:
                            reasons.append("missing PREFECT_API_URL")
                        if not has_storage_path:
                            reasons.append("missing PREFECT_LOCAL_STORAGE_PATH")
                        if not has_results_persist:
                            reasons.append("missing PREFECT_RESULTS_PERSIST_BY_DEFAULT")

                    logger.warning(f"Docker work pool '{pool_name}' exists but lacks: {', '.join(reasons)}. Recreating...")
                    # Delete the old pool and recreate it
                    try:
                        await client.delete_work_pool(pool_name)
                        logger.info(f"Deleted old work pool '{pool_name}'")
                    except Exception as e:
                        logger.warning(f"Failed to delete old work pool: {e}")
            elif force_recreate and existing_pool:
                logger.warning(f"Force recreation enabled - deleting existing work pool '{pool_name}'")
                try:
                    await client.delete_work_pool(pool_name)
                    logger.info(f"Deleted existing work pool for force recreation")
                except Exception as e:
                    logger.warning(f"Failed to delete work pool for force recreation: {e}")

            logger.info(f"Creating Docker work pool '{pool_name}' with custom image support...")

            # Create the work pool with proper Docker configuration
            work_pool = WorkPoolCreate(
                name=pool_name,
                type="docker",
                description="Docker work pool for FuzzForge workflows with custom image support",
                base_job_template={
                    "job_configuration": {
                        "image": "{{ image }}",  # Template variable for custom images
                        "volumes": "{{ volumes }}",  # List of volume mounts
                        "env": "{{ env }}",  # Environment variables
                        "networks": "{{ networks }}",  # Docker networks
                        "stream_output": True,
                        "auto_remove": True,
                        "privileged": False,
                        "network_mode": None,  # Use networks instead
                        "labels": {},
                        "command": None  # Let the image's CMD/ENTRYPOINT run
                    },
                    "variables": {
                        "type": "object",
                        "properties": {
                            "image": {
                                "type": "string",
                                "title": "Docker Image",
                                "default": "prefecthq/prefect:3-python3.11",
                                "description": "Docker image for the flow run"
                            },
                            "volumes": {
                                "type": "array",
                                "title": "Volume Mounts",
                                "default": [
                                    f"{get_actual_compose_project_name()}_prefect_storage:/prefect-storage",
                                    f"{get_actual_compose_project_name()}_toolbox_code:/opt/prefect/toolbox:ro"
                                ],
                                "description": "Volume mounts in format 'host:container:mode'",
                                "items": {
                                    "type": "string"
                                }
                            },
                            "networks": {
                                "type": "array",
                                "title": "Docker Networks",
                                "default": [f"{get_actual_compose_project_name()}_default"],
                                "description": "Docker networks to connect container to",
                                "items": {
                                    "type": "string"
                                }
                            },
                            "env": {
                                "type": "object",
                                "title": "Environment Variables",
                                "default": {
                                    "PREFECT_API_URL": "http://prefect-server:4200/api",
                                    "PREFECT_LOCAL_STORAGE_PATH": "/prefect-storage",
                                    "PREFECT_RESULTS_PERSIST_BY_DEFAULT": "true"
                                },
                                "description": "Environment variables for the container",
                                "additionalProperties": {
                                    "type": "string"
                                }
                            }
                        }
                    }
                }
            )

            await client.create_work_pool(work_pool)
            logger.info(f"Created Docker work pool '{pool_name}'")

        except Exception as e:
            logger.error(f"Failed to setup Docker work pool: {e}")
            raise
        finally:
            # Restore original logging level if debug mode was enabled
            if debug_setup and 'original_level' in locals():
                logger.setLevel(original_level)


def get_actual_compose_project_name():
    """
    Return the hardcoded compose project name for FuzzForge.

    Always returns 'fuzzforge_alpha' as per system requirements.
    """
    logger.info("Using hardcoded compose project name: fuzzforge_alpha")
    return "fuzzforge_alpha"


async def setup_result_storage():
    """
    Create or update Prefect result storage block for findings persistence.

    This sets up a LocalFileSystem storage block pointing to the shared
    /prefect-storage volume for result persistence.
    """
    from prefect.filesystems import LocalFileSystem

    storage_name = "fuzzforge-results"

    try:
        # Create the storage block, overwrite if it exists
        logger.info(f"Setting up storage block '{storage_name}'...")
        storage = LocalFileSystem(basepath="/prefect-storage")

        block_doc_id = await storage.save(name=storage_name, overwrite=True)
        logger.info(f"Storage block '{storage_name}' configured successfully")
        return str(block_doc_id)

    except Exception as e:
        logger.error(f"Failed to setup result storage: {e}")
        # Don't raise the exception - continue without storage block
        logger.warning("Continuing without result storage block - findings may not persist")
        return None


async def validate_docker_connection():
    """
    Validate that Docker is accessible and running.

    Note: In containerized deployments with Docker socket proxy,
    the backend doesn't need direct Docker access.

    Raises:
        RuntimeError: If Docker is not accessible
    """
    import os

    # Skip Docker validation if running in container without socket access
    if os.path.exists("/.dockerenv") and not os.path.exists("/var/run/docker.sock"):
        logger.info("Running in container without Docker socket - skipping Docker validation")
        return

    try:
        import docker
        client = docker.from_env()
        client.ping()
        logger.info("Docker connection validated")
    except Exception as e:
        logger.error(f"Docker is not accessible: {e}")
        raise RuntimeError(
            "Docker is not running or not accessible. "
            "Please ensure Docker is installed and running."
        )


async def validate_registry_connectivity(registry_url: str = None):
    """
    Validate that the Docker registry is accessible.

    Args:
        registry_url: URL of the Docker registry to validate (auto-detected if None)

    Raises:
        RuntimeError: If registry is not accessible
    """
    # Resolve a reachable test URL from within this process
    if registry_url is None:
        # If not specified, prefer internal service name in containers, host port on host
        import os
        if os.path.exists('/.dockerenv'):
            registry_url = "registry:5000"
        else:
            registry_url = "localhost:5001"

    # If we're running inside a container and asked to probe localhost:PORT,
    # the probe would hit the container, not the host. Use host.docker.internal instead.
    import os
    try:
        host_part, port_part = registry_url.split(":", 1)
    except ValueError:
        host_part, port_part = registry_url, "80"

    if os.path.exists('/.dockerenv') and host_part in ("localhost", "127.0.0.1"):
        test_host = "host.docker.internal"
    else:
        test_host = host_part
    test_url = f"http://{test_host}:{port_part}/v2/"

    import aiohttp
    import asyncio

    logger.info(f"Validating registry connectivity to {registry_url}...")

    try:
        async with aiohttp.ClientSession(timeout=aiohttp.ClientTimeout(total=10)) as session:
            async with session.get(test_url) as response:
                if response.status == 200:
                    logger.info(f"Registry at {registry_url} is accessible (tested via {test_host})")
                    return
                else:
                    raise RuntimeError(f"Registry returned status {response.status}")
    except asyncio.TimeoutError:
        raise RuntimeError(f"Registry at {registry_url} is not responding (timeout)")
    except aiohttp.ClientError as e:
        raise RuntimeError(f"Registry at {registry_url} is not accessible: {e}")
    except Exception as e:
        raise RuntimeError(f"Failed to validate registry connectivity: {e}")


async def validate_docker_network(network_name: str):
    """
    Validate that the specified Docker network exists.

    Args:
        network_name: Name of the Docker network to validate

    Raises:
        RuntimeError: If network doesn't exist
    """
    import os

    # Skip network validation if running in container without Docker socket
    if os.path.exists("/.dockerenv") and not os.path.exists("/var/run/docker.sock"):
        logger.info("Running in container without Docker socket - skipping network validation")
        return

    try:
        import docker
        client = docker.from_env()

        # List all networks
        networks = client.networks.list(names=[network_name])

        if not networks:
            # Try to find networks with similar names
            all_networks = client.networks.list()
            similar_networks = [n.name for n in all_networks if "fuzzforge" in n.name.lower()]

            error_msg = f"Docker network '{network_name}' not found."
            if similar_networks:
                error_msg += f" Available networks: {similar_networks}"
            else:
                error_msg += " Please ensure Docker Compose is running."

            raise RuntimeError(error_msg)

        logger.info(f"Docker network '{network_name}' validated")

    except Exception as e:
        if isinstance(e, RuntimeError):
            raise
        logger.error(f"Network validation failed: {e}")
        raise RuntimeError(f"Failed to validate Docker network: {e}")


async def validate_infrastructure():
    """
    Validate all required infrastructure components.

    This should be called during startup to ensure everything is ready.
    """
    logger.info("Validating infrastructure...")

    # Validate Docker connection
    await validate_docker_connection()

    # Validate registry connectivity for custom image building
    await validate_registry_connectivity()

    # Validate network (check for default network pattern)
    import os
    compose_project = os.getenv('COMPOSE_PROJECT_NAME', 'fuzzforge_alpha')
    docker_network = f"{compose_project}_default"

    try:
        await validate_docker_network(docker_network)
    except RuntimeError as e:
        logger.warning(f"Network validation failed: {e}")
        logger.warning("Workflows may not be able to connect to Prefect services")

    logger.info("Infrastructure validation completed")
