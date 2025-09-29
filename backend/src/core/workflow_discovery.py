"""
Workflow Discovery - Registry-based discovery and loading of workflows
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
import yaml
from pathlib import Path
from typing import Dict, Optional, Any, Callable
from pydantic import BaseModel, Field, ConfigDict

logger = logging.getLogger(__name__)


class WorkflowInfo(BaseModel):
    """Information about a discovered workflow"""
    name: str = Field(..., description="Workflow name")
    path: Path = Field(..., description="Path to workflow directory")
    workflow_file: Path = Field(..., description="Path to workflow.py file")
    dockerfile: Path = Field(..., description="Path to Dockerfile")
    has_docker: bool = Field(..., description="Whether workflow has custom Dockerfile")
    metadata: Dict[str, Any] = Field(..., description="Workflow metadata from YAML")
    flow_function_name: str = Field(default="main_flow", description="Name of the flow function")

    model_config = ConfigDict(arbitrary_types_allowed=True)


class WorkflowDiscovery:
    """
    Discovers workflows from the filesystem and validates them against the registry.

    This system:
    1. Scans for workflows with metadata.yaml files
    2. Cross-references them with the manual registry
    3. Provides registry-based flow functions for deployment

    Workflows must have:
    - workflow.py: Contains the Prefect flow
    - metadata.yaml: Mandatory metadata file
    - Entry in toolbox/workflows/registry.py: Manual registration
    - Dockerfile (optional): Custom container definition
    - requirements.txt (optional): Python dependencies
    """

    def __init__(self, workflows_dir: Path):
        """
        Initialize workflow discovery.

        Args:
            workflows_dir: Path to the workflows directory
        """
        self.workflows_dir = workflows_dir
        if not self.workflows_dir.exists():
            self.workflows_dir.mkdir(parents=True, exist_ok=True)
            logger.info(f"Created workflows directory: {self.workflows_dir}")

        # Import registry - this validates it on import
        try:
            from toolbox.workflows.registry import WORKFLOW_REGISTRY, list_registered_workflows
            self.registry = WORKFLOW_REGISTRY
            logger.info(f"Loaded workflow registry with {len(self.registry)} registered workflows")
        except ImportError as e:
            logger.error(f"Failed to import workflow registry: {e}")
            self.registry = {}
        except Exception as e:
            logger.error(f"Registry validation failed: {e}")
            self.registry = {}

        # Cache for discovered workflows
        self._workflow_cache: Optional[Dict[str, WorkflowInfo]] = None
        self._cache_timestamp: Optional[float] = None
        self._cache_ttl = 60.0  # Cache TTL in seconds

    async def discover_workflows(self) -> Dict[str, WorkflowInfo]:
        """
        Discover workflows by cross-referencing filesystem with registry.
        Uses caching to avoid frequent filesystem scans.

        Returns:
            Dictionary mapping workflow names to their information
        """
        # Check cache validity
        import time
        current_time = time.time()

        if (self._workflow_cache is not None and
            self._cache_timestamp is not None and
            (current_time - self._cache_timestamp) < self._cache_ttl):
            # Return cached results
            logger.debug(f"Returning cached workflow discovery ({len(self._workflow_cache)} workflows)")
            return self._workflow_cache
        workflows = {}
        discovered_dirs = set()
        registry_names = set(self.registry.keys())

        if not self.workflows_dir.exists():
            logger.warning(f"Workflows directory does not exist: {self.workflows_dir}")
            return workflows

        # Recursively scan all directories and subdirectories
        await self._scan_directory_recursive(self.workflows_dir, workflows, discovered_dirs)

        # Check for registry entries without corresponding directories
        missing_dirs = registry_names - discovered_dirs
        if missing_dirs:
            logger.warning(
                f"Registry contains workflows without filesystem directories: {missing_dirs}. "
                f"These workflows cannot be deployed."
            )

        logger.info(
            f"Discovery complete: {len(workflows)} workflows ready for deployment, "
            f"{len(missing_dirs)} registry entries missing directories, "
            f"{len(discovered_dirs - registry_names)} filesystem workflows not registered"
        )

        # Update cache
        self._workflow_cache = workflows
        self._cache_timestamp = current_time

        return workflows

    async def _scan_directory_recursive(self, directory: Path, workflows: Dict[str, WorkflowInfo], discovered_dirs: set):
        """
        Recursively scan directory for workflows.

        Args:
            directory: Directory to scan
            workflows: Dictionary to populate with discovered workflows
            discovered_dirs: Set to track discovered workflow names
        """
        for item in directory.iterdir():
            if not item.is_dir():
                continue

            if item.name.startswith('_') or item.name.startswith('.'):
                continue  # Skip hidden or private directories

            # Check if this directory contains workflow files (workflow.py and metadata.yaml)
            workflow_file = item / "workflow.py"
            metadata_file = item / "metadata.yaml"

            if workflow_file.exists() and metadata_file.exists():
                # This is a workflow directory
                workflow_name = item.name
                discovered_dirs.add(workflow_name)

                # Only process workflows that are in the registry
                if workflow_name not in self.registry:
                    logger.warning(
                        f"Workflow '{workflow_name}' found in filesystem but not in registry. "
                        f"Add it to toolbox/workflows/registry.py to enable deployment."
                    )
                    continue

                try:
                    workflow_info = await self._load_workflow(item)
                    if workflow_info:
                        workflows[workflow_info.name] = workflow_info
                        logger.info(f"Discovered and registered workflow: {workflow_info.name}")
                except Exception as e:
                    logger.error(f"Failed to load workflow from {item}: {e}")
            else:
                # This is a category directory, recurse into it
                await self._scan_directory_recursive(item, workflows, discovered_dirs)

    async def _load_workflow(self, workflow_dir: Path) -> Optional[WorkflowInfo]:
        """
        Load and validate a single workflow.

        Args:
            workflow_dir: Path to the workflow directory

        Returns:
            WorkflowInfo if valid, None otherwise
        """
        workflow_name = workflow_dir.name

        # Check for mandatory files
        workflow_file = workflow_dir / "workflow.py"
        metadata_file = workflow_dir / "metadata.yaml"

        if not workflow_file.exists():
            logger.warning(f"Workflow {workflow_name} missing workflow.py")
            return None

        if not metadata_file.exists():
            logger.error(f"Workflow {workflow_name} missing mandatory metadata.yaml")
            return None

        # Load and validate metadata
        try:
            metadata = self._load_metadata(metadata_file)
            if not self._validate_metadata(metadata, workflow_name):
                return None
        except Exception as e:
            logger.error(f"Failed to load metadata for {workflow_name}: {e}")
            return None

        # Check for mandatory Dockerfile
        dockerfile = workflow_dir / "Dockerfile"
        if not dockerfile.exists():
            logger.error(f"Workflow {workflow_name} missing mandatory Dockerfile")
            return None

        has_docker = True  # Always True since Dockerfile is mandatory

        # Get flow function name from metadata or use default
        flow_function_name = metadata.get("flow_function", "main_flow")

        return WorkflowInfo(
            name=workflow_name,
            path=workflow_dir,
            workflow_file=workflow_file,
            dockerfile=dockerfile,
            has_docker=has_docker,
            metadata=metadata,
            flow_function_name=flow_function_name
        )

    def _load_metadata(self, metadata_file: Path) -> Dict[str, Any]:
        """
        Load metadata from YAML file.

        Args:
            metadata_file: Path to metadata.yaml

        Returns:
            Dictionary containing metadata
        """
        with open(metadata_file, 'r') as f:
            metadata = yaml.safe_load(f)

        if metadata is None:
            raise ValueError("Empty metadata file")

        return metadata

    def _validate_metadata(self, metadata: Dict[str, Any], workflow_name: str) -> bool:
        """
        Validate that metadata contains all required fields.

        Args:
            metadata: Metadata dictionary
            workflow_name: Name of the workflow for logging

        Returns:
            True if valid, False otherwise
        """
        required_fields = ["name", "version", "description", "author", "category", "parameters", "requirements"]

        missing_fields = []
        for field in required_fields:
            if field not in metadata:
                missing_fields.append(field)

        if missing_fields:
            logger.error(
                f"Workflow {workflow_name} metadata missing required fields: {missing_fields}"
            )
            return False

        # Validate version format (semantic versioning)
        version = metadata.get("version", "")
        if not self._is_valid_version(version):
            logger.error(f"Workflow {workflow_name} has invalid version format: {version}")
            return False

        # Validate parameters structure
        parameters = metadata.get("parameters", {})
        if not isinstance(parameters, dict):
            logger.error(f"Workflow {workflow_name} parameters must be a dictionary")
            return False

        return True

    def _is_valid_version(self, version: str) -> bool:
        """
        Check if version follows semantic versioning (x.y.z).

        Args:
            version: Version string

        Returns:
            True if valid semantic version
        """
        try:
            parts = version.split('.')
            if len(parts) != 3:
                return False
            for part in parts:
                int(part)  # Check if each part is a number
            return True
        except (ValueError, AttributeError):
            return False

    def invalidate_cache(self) -> None:
        """
        Invalidate the workflow discovery cache.
        Useful when workflows are added or modified.
        """
        self._workflow_cache = None
        self._cache_timestamp = None
        logger.debug("Workflow discovery cache invalidated")

    def get_flow_function(self, workflow_name: str) -> Optional[Callable]:
        """
        Get the flow function from the registry.

        Args:
            workflow_name: Name of the workflow

        Returns:
            The flow function if found in registry, None otherwise
        """
        if workflow_name not in self.registry:
            logger.error(
                f"Workflow '{workflow_name}' not found in registry. "
                f"Available workflows: {list(self.registry.keys())}"
            )
            return None

        try:
            from toolbox.workflows.registry import get_workflow_flow
            flow_func = get_workflow_flow(workflow_name)
            logger.debug(f"Retrieved flow function for '{workflow_name}' from registry")
            return flow_func
        except Exception as e:
            logger.error(f"Failed to get flow function for '{workflow_name}': {e}")
            return None

    def get_registry_info(self, workflow_name: str) -> Optional[Dict[str, Any]]:
        """
        Get registry information for a workflow.

        Args:
            workflow_name: Name of the workflow

        Returns:
            Registry information if found, None otherwise
        """
        if workflow_name not in self.registry:
            return None

        try:
            from toolbox.workflows.registry import get_workflow_info
            return get_workflow_info(workflow_name)
        except Exception as e:
            logger.error(f"Failed to get registry info for '{workflow_name}': {e}")
            return None

    @staticmethod
    def get_metadata_schema() -> Dict[str, Any]:
        """
        Get the JSON schema for workflow metadata.

        Returns:
            JSON schema dictionary
        """
        return {
            "type": "object",
            "required": ["name", "version", "description", "author", "category", "parameters", "requirements"],
            "properties": {
                "name": {
                    "type": "string",
                    "description": "Workflow name"
                },
                "version": {
                    "type": "string",
                    "pattern": "^\\d+\\.\\d+\\.\\d+$",
                    "description": "Semantic version (x.y.z)"
                },
                "description": {
                    "type": "string",
                    "description": "Workflow description"
                },
                "author": {
                    "type": "string",
                    "description": "Workflow author"
                },
                "category": {
                    "type": "string",
                    "enum": ["comprehensive", "specialized", "fuzzing", "focused"],
                    "description": "Workflow category"
                },
                "tags": {
                    "type": "array",
                    "items": {"type": "string"},
                    "description": "Workflow tags for categorization"
                },
                "requirements": {
                    "type": "object",
                    "required": ["tools", "resources"],
                    "properties": {
                        "tools": {
                            "type": "array",
                            "items": {"type": "string"},
                            "description": "Required security tools"
                        },
                        "resources": {
                            "type": "object",
                            "required": ["memory", "cpu", "timeout"],
                            "properties": {
                                "memory": {
                                    "type": "string",
                                    "pattern": "^\\d+[GMK]i$",
                                    "description": "Memory limit (e.g., 1Gi, 512Mi)"
                                },
                                "cpu": {
                                    "type": "string",
                                    "pattern": "^\\d+m?$",
                                    "description": "CPU limit (e.g., 1000m, 2)"
                                },
                                "timeout": {
                                    "type": "integer",
                                    "minimum": 60,
                                    "maximum": 7200,
                                    "description": "Workflow timeout in seconds"
                                }
                            }
                        }
                    }
                },
                "parameters": {
                    "type": "object",
                    "description": "Workflow parameters schema"
                },
                "default_parameters": {
                    "type": "object",
                    "description": "Default parameter values"
                },
                "required_modules": {
                    "type": "array",
                    "items": {"type": "string"},
                    "description": "Required module names"
                },
                "supported_volume_modes": {
                    "type": "array",
                    "items": {"enum": ["ro", "rw"]},
                    "default": ["ro", "rw"],
                    "description": "Supported volume mount modes"
                },
                "flow_function": {
                    "type": "string",
                    "default": "main_flow",
                    "description": "Name of the flow function in workflow.py"
                }
            }
        }