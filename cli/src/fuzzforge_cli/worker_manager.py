"""
Worker lifecycle management for FuzzForge CLI.

Manages on-demand startup and shutdown of Temporal workers using Docker Compose.
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
import subprocess
import time
from pathlib import Path
from typing import Optional, Dict, Any

from rich.console import Console

logger = logging.getLogger(__name__)
console = Console()


class WorkerManager:
    """
    Manages Temporal worker lifecycle using docker-compose.

    This class handles:
    - Checking if workers are running
    - Starting workers on demand
    - Waiting for workers to be ready
    - Stopping workers when done
    """

    def __init__(
        self,
        compose_file: Optional[Path] = None,
        startup_timeout: int = 60,
        health_check_interval: float = 2.0
    ):
        """
        Initialize WorkerManager.

        Args:
            compose_file: Path to docker-compose.yml (defaults to auto-detect)
            startup_timeout: Maximum seconds to wait for worker startup
            health_check_interval: Seconds between health checks
        """
        self.compose_file = compose_file or self._find_compose_file()
        self.startup_timeout = startup_timeout
        self.health_check_interval = health_check_interval

    def _find_compose_file(self) -> Path:
        """
        Auto-detect docker-compose.yml location.

        Searches upward from current directory to find the compose file.
        """
        current = Path.cwd()

        # Try current directory and parents
        for parent in [current] + list(current.parents):
            compose_path = parent / "docker-compose.yml"
            if compose_path.exists():
                return compose_path

        # Fallback to default location
        return Path("docker-compose.yml")

    def _run_docker_compose(self, *args: str) -> subprocess.CompletedProcess:
        """
        Run docker-compose command.

        Args:
            *args: Arguments to pass to docker-compose

        Returns:
            CompletedProcess with result

        Raises:
            subprocess.CalledProcessError: If command fails
        """
        cmd = ["docker-compose", "-f", str(self.compose_file)] + list(args)
        logger.debug(f"Running: {' '.join(cmd)}")

        return subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            check=True
        )

    def is_worker_running(self, container_name: str) -> bool:
        """
        Check if a worker container is running.

        Args:
            container_name: Name of the Docker container (e.g., "fuzzforge-worker-ossfuzz")

        Returns:
            True if container is running, False otherwise
        """
        try:
            result = subprocess.run(
                ["docker", "inspect", "-f", "{{.State.Running}}", container_name],
                capture_output=True,
                text=True,
                check=False
            )

            # Output is "true" or "false"
            return result.stdout.strip().lower() == "true"

        except Exception as e:
            logger.debug(f"Failed to check worker status: {e}")
            return False

    def start_worker(self, container_name: str) -> bool:
        """
        Start a worker container using docker.

        Args:
            container_name: Name of the Docker container to start

        Returns:
            True if started successfully, False otherwise
        """
        try:
            console.print(f"🚀 Starting worker: {container_name}")

            # Use docker start directly (works with container name)
            subprocess.run(
                ["docker", "start", container_name],
                capture_output=True,
                text=True,
                check=True
            )

            logger.info(f"Worker {container_name} started")
            return True

        except subprocess.CalledProcessError as e:
            logger.error(f"Failed to start worker {container_name}: {e.stderr}")
            console.print(f"❌ Failed to start worker: {e.stderr}", style="red")
            return False

        except Exception as e:
            logger.error(f"Unexpected error starting worker {container_name}: {e}")
            console.print(f"❌ Unexpected error: {e}", style="red")
            return False

    def wait_for_worker_ready(self, container_name: str, timeout: Optional[int] = None) -> bool:
        """
        Wait for a worker to be healthy and ready to process tasks.

        Args:
            container_name: Name of the Docker container
            timeout: Maximum seconds to wait (uses instance default if not specified)

        Returns:
            True if worker is ready, False if timeout reached

        Raises:
            TimeoutError: If worker doesn't become ready within timeout
        """
        timeout = timeout or self.startup_timeout
        start_time = time.time()

        console.print("⏳ Waiting for worker to be ready...")

        while time.time() - start_time < timeout:
            # Check if container is running
            if not self.is_worker_running(container_name):
                logger.debug(f"Worker {container_name} not running yet")
                time.sleep(self.health_check_interval)
                continue

            # Check container health status
            try:
                result = subprocess.run(
                    ["docker", "inspect", "-f", "{{.State.Health.Status}}", container_name],
                    capture_output=True,
                    text=True,
                    check=False
                )

                health_status = result.stdout.strip()

                # If no health check is defined, assume healthy after running
                if health_status == "<no value>" or health_status == "":
                    logger.info(f"Worker {container_name} is running (no health check)")
                    console.print(f"✅ Worker ready: {container_name}")
                    return True

                if health_status == "healthy":
                    logger.info(f"Worker {container_name} is healthy")
                    console.print(f"✅ Worker ready: {container_name}")
                    return True

                logger.debug(f"Worker {container_name} health: {health_status}")

            except Exception as e:
                logger.debug(f"Failed to check health: {e}")

            time.sleep(self.health_check_interval)

        elapsed = time.time() - start_time
        logger.warning(f"Worker {container_name} did not become ready within {elapsed:.1f}s")
        console.print(f"⚠️  Worker startup timeout after {elapsed:.1f}s", style="yellow")
        return False

    def stop_worker(self, container_name: str) -> bool:
        """
        Stop a worker container using docker.

        Args:
            container_name: Name of the Docker container to stop

        Returns:
            True if stopped successfully, False otherwise
        """
        try:
            console.print(f"🛑 Stopping worker: {container_name}")

            # Use docker stop directly (works with container name)
            subprocess.run(
                ["docker", "stop", container_name],
                capture_output=True,
                text=True,
                check=True
            )

            logger.info(f"Worker {container_name} stopped")
            return True

        except subprocess.CalledProcessError as e:
            logger.error(f"Failed to stop worker {container_name}: {e.stderr}")
            console.print(f"❌ Failed to stop worker: {e.stderr}", style="red")
            return False

        except Exception as e:
            logger.error(f"Unexpected error stopping worker {container_name}: {e}")
            console.print(f"❌ Unexpected error: {e}", style="red")
            return False

    def ensure_worker_running(
        self,
        worker_info: Dict[str, Any],
        auto_start: bool = True
    ) -> bool:
        """
        Ensure a worker is running, starting it if necessary.

        Args:
            worker_info: Worker information dict from API (contains worker_container, etc.)
            auto_start: Whether to automatically start the worker if not running

        Returns:
            True if worker is running, False otherwise
        """
        container_name = worker_info["worker_container"]
        vertical = worker_info["vertical"]

        # Check if already running
        if self.is_worker_running(container_name):
            console.print(f"✓ Worker already running: {vertical}")
            return True

        if not auto_start:
            console.print(
                f"⚠️  Worker not running: {vertical}. Use --auto-start to start automatically.",
                style="yellow"
            )
            return False

        # Start the worker
        if not self.start_worker(container_name):
            return False

        # Wait for it to be ready
        return self.wait_for_worker_ready(container_name)
