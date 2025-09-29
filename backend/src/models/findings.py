"""
Models for workflow findings and submissions
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

from pydantic import BaseModel, Field, field_validator
from typing import Dict, Any, Optional, Literal, List
from datetime import datetime
from pathlib import Path


class WorkflowFindings(BaseModel):
    """Findings from a workflow execution in SARIF format"""
    workflow: str = Field(..., description="Workflow name")
    run_id: str = Field(..., description="Unique run identifier")
    sarif: Dict[str, Any] = Field(..., description="SARIF formatted findings")
    metadata: Dict[str, Any] = Field(default_factory=dict, description="Additional metadata")


class ResourceLimits(BaseModel):
    """Resource limits for workflow execution"""
    cpu_limit: Optional[str] = Field(None, description="CPU limit (e.g., '2' for 2 cores, '500m' for 0.5 cores)")
    memory_limit: Optional[str] = Field(None, description="Memory limit (e.g., '1Gi', '512Mi')")
    cpu_request: Optional[str] = Field(None, description="CPU request (guaranteed)")
    memory_request: Optional[str] = Field(None, description="Memory request (guaranteed)")


class VolumeMount(BaseModel):
    """Volume mount specification"""
    host_path: str = Field(..., description="Host path to mount")
    container_path: str = Field(..., description="Container path for mount")
    mode: Literal["ro", "rw"] = Field(default="ro", description="Mount mode")

    @field_validator("host_path")
    @classmethod
    def validate_host_path(cls, v):
        """Validate that the host path is absolute (existence checked at runtime)"""
        path = Path(v)
        if not path.is_absolute():
            raise ValueError(f"Host path must be absolute: {v}")
        # Note: Path existence is validated at workflow runtime
        # We can't validate existence here as this runs inside Docker container
        return str(path)

    @field_validator("container_path")
    @classmethod
    def validate_container_path(cls, v):
        """Validate that the container path is absolute"""
        if not v.startswith('/'):
            raise ValueError(f"Container path must be absolute: {v}")
        return v


class WorkflowSubmission(BaseModel):
    """Submit a workflow with configurable settings"""
    target_path: str = Field(..., description="Absolute path to analyze")
    volume_mode: Literal["ro", "rw"] = Field(
        default="ro",
        description="Volume mount mode: read-only (ro) or read-write (rw)"
    )
    parameters: Dict[str, Any] = Field(
        default_factory=dict,
        description="Workflow-specific parameters"
    )
    timeout: Optional[int] = Field(
        default=None,  # Allow workflow-specific defaults
        description="Timeout in seconds (None for workflow default)",
        ge=1,
        le=604800  # Max 7 days to support fuzzing campaigns
    )
    resource_limits: Optional[ResourceLimits] = Field(
        None,
        description="Resource limits for workflow container"
    )
    additional_volumes: List[VolumeMount] = Field(
        default_factory=list,
        description="Additional volume mounts (e.g., for corpus, output directories)"
    )

    @field_validator("target_path")
    @classmethod
    def validate_path(cls, v):
        """Validate that the target path is absolute (existence checked at runtime)"""
        path = Path(v)
        if not path.is_absolute():
            raise ValueError(f"Path must be absolute: {v}")
        # Note: Path existence is validated at workflow runtime when volumes are mounted
        # We can't validate existence here as this runs inside Docker container
        return str(path)


class WorkflowStatus(BaseModel):
    """Status of a workflow run"""
    run_id: str = Field(..., description="Unique run identifier")
    workflow: str = Field(..., description="Workflow name")
    status: str = Field(..., description="Current status")
    is_completed: bool = Field(..., description="Whether the run is completed")
    is_failed: bool = Field(..., description="Whether the run failed")
    is_running: bool = Field(..., description="Whether the run is currently running")
    created_at: datetime = Field(..., description="Run creation time")
    updated_at: datetime = Field(..., description="Last update time")


class WorkflowMetadata(BaseModel):
    """Complete metadata for a workflow"""
    name: str = Field(..., description="Workflow name")
    version: str = Field(..., description="Semantic version")
    description: str = Field(..., description="Workflow description")
    author: Optional[str] = Field(None, description="Workflow author")
    tags: List[str] = Field(default_factory=list, description="Workflow tags")
    parameters: Dict[str, Any] = Field(..., description="Parameters schema")
    default_parameters: Dict[str, Any] = Field(
        default_factory=dict,
        description="Default parameter values"
    )
    required_modules: List[str] = Field(
        default_factory=list,
        description="Required module names"
    )
    supported_volume_modes: List[Literal["ro", "rw"]] = Field(
        default=["ro", "rw"],
        description="Supported volume mount modes"
    )
    has_custom_docker: bool = Field(
        default=False,
        description="Whether workflow has custom Dockerfile"
    )


class WorkflowListItem(BaseModel):
    """Summary information for a workflow in list views"""
    name: str = Field(..., description="Workflow name")
    version: str = Field(..., description="Semantic version")
    description: str = Field(..., description="Workflow description")
    author: Optional[str] = Field(None, description="Workflow author")
    tags: List[str] = Field(default_factory=list, description="Workflow tags")


class RunSubmissionResponse(BaseModel):
    """Response after submitting a workflow"""
    run_id: str = Field(..., description="Unique run identifier")
    status: str = Field(..., description="Initial status")
    workflow: str = Field(..., description="Workflow name")
    message: str = Field(default="Workflow submitted successfully")


class FuzzingStats(BaseModel):
    """Real-time fuzzing statistics"""
    run_id: str = Field(..., description="Unique run identifier")
    workflow: str = Field(..., description="Workflow name")
    executions: int = Field(default=0, description="Total executions")
    executions_per_sec: float = Field(default=0.0, description="Current execution rate")
    crashes: int = Field(default=0, description="Total crashes found")
    unique_crashes: int = Field(default=0, description="Unique crashes")
    coverage: Optional[float] = Field(None, description="Code coverage percentage")
    corpus_size: int = Field(default=0, description="Current corpus size")
    elapsed_time: int = Field(default=0, description="Elapsed time in seconds")
    last_crash_time: Optional[datetime] = Field(None, description="Time of last crash")


class CrashReport(BaseModel):
    """Individual crash report from fuzzing"""
    run_id: str = Field(..., description="Run identifier")
    crash_id: str = Field(..., description="Unique crash identifier")
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    signal: Optional[str] = Field(None, description="Crash signal (SIGSEGV, etc.)")
    crash_type: Optional[str] = Field(None, description="Type of crash")
    stack_trace: Optional[str] = Field(None, description="Stack trace")
    input_file: Optional[str] = Field(None, description="Path to crashing input")
    reproducer: Optional[str] = Field(None, description="Minimized reproducer")
    severity: str = Field(default="medium", description="Crash severity")
    exploitability: Optional[str] = Field(None, description="Exploitability assessment")