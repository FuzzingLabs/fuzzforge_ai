"""
Setup utilities for FuzzForge infrastructure
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

logger = logging.getLogger(__name__)


async def setup_result_storage():
    """
    Setup result storage (MinIO).

    MinIO is used for both target upload and result storage.
    This is a placeholder for any MinIO-specific setup if needed.
    """
    logger.info("Result storage (MinIO) configured")
    # MinIO is configured via environment variables in docker-compose
    # No additional setup needed here
    return True


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

    # Validate network (hardcoded to fuzzforge for Temporal deployment)
    docker_network = "fuzzforge_default"

    try:
        await validate_docker_network(docker_network)
    except RuntimeError as e:
        logger.warning(f"Network validation failed: {e}")
        logger.warning("Workflows may not be able to connect to Temporal services")

    logger.info("Infrastructure validation completed")
