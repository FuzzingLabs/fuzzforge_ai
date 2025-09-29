"""
Docker log integration for enhanced error reporting.

This module provides functionality to fetch and parse Docker container logs
to provide better context for deployment and workflow execution errors.
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
import re
import subprocess
import json
from typing import Dict, Any, List, Optional, Tuple
from datetime import datetime, timezone
from dataclasses import dataclass

logger = logging.getLogger(__name__)


@dataclass
class ContainerLogEntry:
    """A single log entry from a container."""
    timestamp: datetime
    level: str
    message: str
    stream: str  # 'stdout' or 'stderr'
    raw: str


@dataclass
class ContainerDiagnostics:
    """Complete diagnostics for a container."""
    container_id: Optional[str]
    status: str
    exit_code: Optional[int]
    error: Optional[str]
    logs: List[ContainerLogEntry]
    resource_usage: Dict[str, Any]
    volume_mounts: List[Dict[str, str]]


class DockerLogIntegration:
    """
    Integration with Docker to fetch container logs and diagnostics.

    This class provides methods to fetch container logs, parse common error
    patterns, and extract meaningful diagnostic information from Docker
    containers related to FuzzForge workflow execution.
    """

    def __init__(self):
        self.docker_available = self._check_docker_availability()

        # Common error patterns in container logs
        self.error_patterns = {
            'permission_denied': [
                r'permission denied',
                r'operation not permitted',
                r'cannot access.*permission denied'
            ],
            'out_of_memory': [
                r'out of memory',
                r'oom killed',
                r'cannot allocate memory'
            ],
            'image_pull_failed': [
                r'failed to pull image',
                r'pull access denied',
                r'image not found'
            ],
            'volume_mount_failed': [
                r'invalid mount config',
                r'mount denied',
                r'no such file or directory.*mount'
            ],
            'network_error': [
                r'network is unreachable',
                r'connection refused',
                r'timeout.*connect'
            ],
            'prefect_error': [
                r'prefect.*error',
                r'flow run failed',
                r'task.*failed'
            ]
        }

    def _check_docker_availability(self) -> bool:
        """Check if Docker is available and accessible."""
        try:
            result = subprocess.run(['docker', 'version', '--format', 'json'],
                                  capture_output=True, text=True, timeout=5)
            return result.returncode == 0
        except (subprocess.TimeoutExpired, FileNotFoundError, subprocess.SubprocessError):
            return False

    def get_container_logs(self, container_name_or_id: str, tail: int = 100) -> List[ContainerLogEntry]:
        """
        Fetch logs from a Docker container.

        Args:
            container_name_or_id: Container name or ID
            tail: Number of log lines to retrieve

        Returns:
            List of parsed log entries
        """
        if not self.docker_available:
            logger.warning("Docker not available, cannot fetch container logs")
            return []

        try:
            cmd = ['docker', 'logs', '--timestamps', '--tail', str(tail), container_name_or_id]
            result = subprocess.run(cmd, capture_output=True, text=True, timeout=10)

            if result.returncode != 0:
                logger.error(f"Failed to fetch logs for container {container_name_or_id}: {result.stderr}")
                return []

            return self._parse_docker_logs(result.stdout + result.stderr)

        except subprocess.TimeoutExpired:
            logger.error(f"Timeout fetching logs for container {container_name_or_id}")
            return []
        except Exception as e:
            logger.error(f"Error fetching container logs: {e}")
            return []

    def _parse_docker_logs(self, raw_logs: str) -> List[ContainerLogEntry]:
        """Parse raw Docker logs into structured entries."""
        entries = []

        for line in raw_logs.strip().split('\n'):
            if not line.strip():
                continue

            entry = self._parse_log_line(line)
            if entry:
                entries.append(entry)

        return entries

    def _parse_log_line(self, line: str) -> Optional[ContainerLogEntry]:
        """Parse a single log line with timestamp."""
        # Docker log format: 2023-10-01T12:00:00.000000000Z message
        timestamp_match = re.match(r'^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+Z)\s+(.*)', line)

        if timestamp_match:
            timestamp_str, message = timestamp_match.groups()
            try:
                timestamp = datetime.fromisoformat(timestamp_str.replace('Z', '+00:00'))
            except ValueError:
                timestamp = datetime.now(timezone.utc)
        else:
            timestamp = datetime.now(timezone.utc)
            message = line

        # Determine log level from message content
        level = self._extract_log_level(message)

        # Determine stream (simplified - Docker doesn't clearly separate in combined output)
        stream = 'stderr' if any(keyword in message.lower() for keyword in ['error', 'failed', 'exception']) else 'stdout'

        return ContainerLogEntry(
            timestamp=timestamp,
            level=level,
            message=message.strip(),
            stream=stream,
            raw=line
        )

    def _extract_log_level(self, message: str) -> str:
        """Extract log level from message content."""
        message_lower = message.lower()

        if any(keyword in message_lower for keyword in ['error', 'failed', 'exception', 'fatal']):
            return 'ERROR'
        elif any(keyword in message_lower for keyword in ['warning', 'warn']):
            return 'WARNING'
        elif any(keyword in message_lower for keyword in ['info', 'information']):
            return 'INFO'
        elif any(keyword in message_lower for keyword in ['debug']):
            return 'DEBUG'
        else:
            return 'INFO'

    def get_container_diagnostics(self, container_name_or_id: str) -> ContainerDiagnostics:
        """
        Get complete diagnostics for a container including logs, status, and resource usage.

        Args:
            container_name_or_id: Container name or ID

        Returns:
            Complete container diagnostics
        """
        if not self.docker_available:
            return ContainerDiagnostics(
                container_id=None,
                status="unknown",
                exit_code=None,
                error="Docker not available",
                logs=[],
                resource_usage={},
                volume_mounts=[]
            )

        # Get container inspect data
        inspect_data = self._get_container_inspect(container_name_or_id)

        # Get logs
        logs = self.get_container_logs(container_name_or_id)

        # Extract key information
        if inspect_data:
            state = inspect_data.get('State', {})
            config = inspect_data.get('Config', {})
            host_config = inspect_data.get('HostConfig', {})

            status = state.get('Status', 'unknown')
            exit_code = state.get('ExitCode')
            error = state.get('Error', '')

            # Get volume mounts
            mounts = inspect_data.get('Mounts', [])
            volume_mounts = [
                {
                    'source': mount.get('Source', ''),
                    'destination': mount.get('Destination', ''),
                    'mode': mount.get('Mode', ''),
                    'type': mount.get('Type', '')
                }
                for mount in mounts
            ]

            # Get resource limits
            resource_usage = {
                'memory_limit': host_config.get('Memory', 0),
                'cpu_limit': host_config.get('CpuQuota', 0),
                'cpu_period': host_config.get('CpuPeriod', 0)
            }

        else:
            status = "not_found"
            exit_code = None
            error = f"Container {container_name_or_id} not found"
            volume_mounts = []
            resource_usage = {}

        return ContainerDiagnostics(
            container_id=container_name_or_id,
            status=status,
            exit_code=exit_code,
            error=error,
            logs=logs,
            resource_usage=resource_usage,
            volume_mounts=volume_mounts
        )

    def _get_container_inspect(self, container_name_or_id: str) -> Optional[Dict[str, Any]]:
        """Get container inspection data."""
        try:
            cmd = ['docker', 'inspect', container_name_or_id]
            result = subprocess.run(cmd, capture_output=True, text=True, timeout=5)

            if result.returncode != 0:
                return None

            data = json.loads(result.stdout)
            return data[0] if data else None

        except (subprocess.TimeoutExpired, json.JSONDecodeError, Exception) as e:
            logger.debug(f"Failed to inspect container {container_name_or_id}: {e}")
            return None

    def analyze_error_patterns(self, logs: List[ContainerLogEntry]) -> Dict[str, List[str]]:
        """
        Analyze logs for common error patterns.

        Args:
            logs: List of log entries to analyze

        Returns:
            Dictionary mapping error types to matching log messages
        """
        detected_errors = {}

        for error_type, patterns in self.error_patterns.items():
            matches = []

            for log_entry in logs:
                for pattern in patterns:
                    if re.search(pattern, log_entry.message, re.IGNORECASE):
                        matches.append(log_entry.message)
                        break  # Don't match the same message multiple times

            if matches:
                detected_errors[error_type] = matches

        return detected_errors

    def get_container_names_by_label(self, label_filter: str) -> List[str]:
        """
        Get container names that match a specific label filter.

        Args:
            label_filter: Label filter (e.g., "prefect.flow-run-id=12345")

        Returns:
            List of container names
        """
        if not self.docker_available:
            return []

        try:
            cmd = ['docker', 'ps', '-a', '--filter', f'label={label_filter}', '--format', '{{.Names}}']
            result = subprocess.run(cmd, capture_output=True, text=True, timeout=5)

            if result.returncode != 0:
                return []

            return [name.strip() for name in result.stdout.strip().split('\n') if name.strip()]

        except Exception as e:
            logger.debug(f"Failed to get containers by label {label_filter}: {e}")
            return []

    def suggest_fixes(self, error_analysis: Dict[str, List[str]]) -> List[str]:
        """
        Suggest fixes based on detected error patterns.

        Args:
            error_analysis: Result from analyze_error_patterns()

        Returns:
            List of suggested fixes
        """
        suggestions = []

        if 'permission_denied' in error_analysis:
            suggestions.extend([
                "Check file permissions on the target path",
                "Ensure the Docker daemon has access to the mounted volumes",
                "Try running with elevated privileges or adjust volume ownership"
            ])

        if 'out_of_memory' in error_analysis:
            suggestions.extend([
                "Increase memory limits for the workflow",
                "Check if the target files are too large for available memory",
                "Consider using streaming processing for large datasets"
            ])

        if 'image_pull_failed' in error_analysis:
            suggestions.extend([
                "Check network connectivity to Docker registry",
                "Verify image name and tag are correct",
                "Ensure Docker registry credentials are configured"
            ])

        if 'volume_mount_failed' in error_analysis:
            suggestions.extend([
                "Verify the target path exists and is accessible",
                "Check volume mount syntax and permissions",
                "Ensure the path is not already in use by another process"
            ])

        if 'network_error' in error_analysis:
            suggestions.extend([
                "Check network connectivity",
                "Verify backend services are running (docker-compose up -d)",
                "Check firewall settings and port availability"
            ])

        if 'prefect_error' in error_analysis:
            suggestions.extend([
                "Check Prefect server connectivity",
                "Verify workflow deployment is successful",
                "Review workflow-specific parameters and requirements"
            ])

        if not suggestions:
            suggestions.append("Review the container logs above for specific error details")

        return suggestions


# Global instance for easy access
docker_integration = DockerLogIntegration()