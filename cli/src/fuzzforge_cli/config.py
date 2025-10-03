"""
Configuration management for FuzzForge CLI.

Extends project configuration with Cognee integration metadata
and provides helpers for AI modules.
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


from __future__ import annotations

import hashlib
import os
from pathlib import Path
from typing import Any, Dict, Optional, Literal
import asyncio

try:  # Optional dependency; fall back if not installed
    from dotenv import load_dotenv
except ImportError:  # pragma: no cover - optional dependency
    load_dotenv = None

import yaml
from pydantic import BaseModel, Field


def _generate_project_id(project_dir: Path, project_name: str) -> str:
    """Generate a deterministic project identifier based on path and name."""
    resolved_path = str(project_dir.resolve())
    hash_input = f"{resolved_path}:{project_name}".encode()
    return hashlib.sha256(hash_input).hexdigest()[:16]


class ProjectConfig(BaseModel):
    """Project configuration model."""

    name: str = "fuzzforge-project"
    api_url: str = "http://localhost:8000"
    default_timeout: int = 3600
    default_workflow: Optional[str] = None
    id: Optional[str] = None
    tenant_id: Optional[str] = None


class RetentionConfig(BaseModel):
    """Data retention configuration."""

    max_runs: int = 100
    keep_findings_days: int = 90


class PreferencesConfig(BaseModel):
    """User preferences."""

    auto_save_findings: bool = True
    show_progress_bars: bool = True
    table_style: str = "rich"
    color_output: bool = True


class CogneeConfig(BaseModel):
    """Cognee integration metadata."""

    enabled: bool = True
    graph_database_provider: str = "kuzu"
    data_directory: Optional[str] = None
    system_directory: Optional[str] = None
    backend_access_control: bool = True
    project_id: Optional[str] = None
    tenant_id: Optional[str] = None
    mode: Literal["embedded", "service"] = "embedded"
    storage_backend: Literal["filesystem", "s3"] = "filesystem"
    s3_bucket: Optional[str] = None
    s3_prefix: Optional[str] = None
    service_env_dir: Optional[str] = None
    service_user_email: Optional[str] = None
    service_user_password: Optional[str] = None
    service_url: Optional[str] = None


class FuzzForgeConfig(BaseModel):
    """Complete FuzzForge CLI configuration."""

    project: ProjectConfig = Field(default_factory=ProjectConfig)
    retention: RetentionConfig = Field(default_factory=RetentionConfig)
    preferences: PreferencesConfig = Field(default_factory=PreferencesConfig)
    cognee: CogneeConfig = Field(default_factory=CogneeConfig)

    @classmethod
    def from_file(cls, config_path: Path) -> "FuzzForgeConfig":
        """Load configuration from YAML file."""
        if not config_path.exists():
            return cls()

        try:
            with open(config_path, "r", encoding="utf-8") as fh:
                data = yaml.safe_load(fh) or {}
            return cls(**data)
        except Exception as exc:  # pragma: no cover - defensive fallback
            print(f"Warning: Failed to load config from {config_path}: {exc}")
            return cls()

    def save_to_file(self, config_path: Path) -> None:
        """Save configuration to YAML file."""
        config_path.parent.mkdir(parents=True, exist_ok=True)
        with open(config_path, "w", encoding="utf-8") as fh:
            yaml.dump(
                self.model_dump(),
                fh,
                default_flow_style=False,
                sort_keys=False,
            )

    # ------------------------------------------------------------------
    # Convenience helpers used by CLI and AI modules
    # ------------------------------------------------------------------
    def ensure_project_metadata(self, project_dir: Path) -> bool:
        """Ensure project id/tenant metadata is populated."""
        changed = False
        project = self.project
        if not project.id:
            project.id = _generate_project_id(project_dir, project.name)
            changed = True
        if not project.tenant_id:
            project.tenant_id = f"fuzzforge_project_{project.id}"
            changed = True
        return changed

    def ensure_cognee_defaults(self, project_dir: Path) -> bool:
        """Ensure Cognee configuration and directories exist."""
        self.ensure_project_metadata(project_dir)
        changed = False

        cognee = self.cognee
        if not cognee.project_id:
            cognee.project_id = self.project.id
            changed = True
        if not cognee.tenant_id:
            cognee.tenant_id = self.project.tenant_id
            changed = True
        if not cognee.service_user_email or cognee.service_user_email.endswith("@cognee.local"):
            cognee.service_user_email = f"project_{self.project.id}@cognee.dev"
            changed = True
        if not cognee.service_user_password:
            cognee.service_user_password = f"{self.project.id[:8]}_C0gn33!"
            changed = True

        mode_env = (os.getenv("COGNEE_STORAGE_MODE") or os.getenv("COGNEE_SERVICE_MODE"))
        if mode_env:
            normalized = mode_env.strip().lower()
            if normalized in {"embedded", "service"} and normalized != cognee.mode:
                cognee.mode = normalized  # type: ignore[assignment]
                changed = True

        service_url_env = os.getenv("COGNEE_SERVICE_URL") or os.getenv("COGNEE_API_URL")
        if service_url_env and cognee.service_url != service_url_env:
            cognee.service_url = service_url_env
            changed = True

        if cognee.mode not in {"embedded", "service"}:
            cognee.mode = "service"
            changed = True

        if cognee.mode != "service":
            cognee.mode = "service"
            changed = True

        if cognee.mode == "service":
            bucket = cognee.s3_bucket or os.getenv("COGNEE_S3_BUCKET") or os.getenv("S3_BUCKET")
            if bucket and cognee.s3_bucket != bucket:
                cognee.s3_bucket = bucket
                changed = True

            prefix = cognee.s3_prefix or os.getenv("COGNEE_S3_PREFIX")
            if not prefix:
                prefix = f"cognee/projects/{self.project.id}"
            if cognee.s3_prefix != prefix:
                cognee.s3_prefix = prefix
                changed = True

            if bucket:
                data_dir = f"s3://{bucket}/{prefix}/data"
                system_dir = f"s3://{bucket}/{prefix}/system"

                if cognee.data_directory != data_dir:
                    cognee.data_directory = data_dir
                    changed = True
                if cognee.system_directory != system_dir:
                    cognee.system_directory = system_dir
                    changed = True
                if cognee.storage_backend != "s3":
                    cognee.storage_backend = "s3"
                    changed = True

                service_dir = (
                    project_dir
                    / ".fuzzforge"
                    / "cognee"
                    / "service"
                    / f"project_{self.project.id}"
                )
                if cognee.service_env_dir != str(service_dir):
                    cognee.service_env_dir = str(service_dir)
                    changed = True
                service_dir.mkdir(parents=True, exist_ok=True)
                self._write_cognee_service_env(project_dir)

        if cognee.mode == "embedded":
            base_dir = project_dir / ".fuzzforge" / "cognee" / f"project_{self.project.id}"
            data_dir = base_dir / "data"
            system_dir = base_dir / "system"

            for path in (
                base_dir,
                data_dir,
                system_dir,
                system_dir / "kuzu_db",
                system_dir / "lancedb",
            ):
                if not path.exists():
                    path.mkdir(parents=True, exist_ok=True)

            if cognee.data_directory != str(data_dir):
                cognee.data_directory = str(data_dir)
                changed = True
            if cognee.system_directory != str(system_dir):
                cognee.system_directory = str(system_dir)
                changed = True
            if cognee.storage_backend != "filesystem":
                cognee.storage_backend = "filesystem"
                changed = True
            if cognee.service_env_dir is not None:
                cognee.service_env_dir = None
                changed = True
            if cognee.service_url is not None:
                cognee.service_url = None
                changed = True

        return changed

    def _write_cognee_service_env(self, project_dir: Path) -> None:
        """Generate a service .env file for Cognee containers."""
        cognee = self.cognee
        if cognee.mode != "service" or not cognee.service_env_dir:
            return

        service_dir = Path(cognee.service_env_dir)
        service_dir.mkdir(parents=True, exist_ok=True)
        env_path = service_dir / ".env"

        def _env(*names: str) -> str:
            for name in names:
                value = os.getenv(name)
                if value:
                    return value
            return ""

        bucket = cognee.s3_bucket or _env("COGNEE_S3_BUCKET", "S3_BUCKET")
        prefix = cognee.s3_prefix or ""
        backend_flag = "true" if cognee.backend_access_control else "false"
        data_root = cognee.data_directory or ""
        system_root = cognee.system_directory or ""

        aws_key = _env("COGNEE_AWS_ACCESS_KEY_ID", "AWS_ACCESS_KEY_ID", "S3_ACCESS_KEY")
        aws_secret = _env(
            "COGNEE_AWS_SECRET_ACCESS_KEY",
            "AWS_SECRET_ACCESS_KEY",
            "S3_SECRET_KEY",
        )
        aws_endpoint = _env("COGNEE_AWS_ENDPOINT_URL", "AWS_ENDPOINT_URL", "S3_ENDPOINT")
        aws_region = _env("COGNEE_AWS_REGION", "AWS_REGION", "S3_REGION")

        llm_provider = _env("LLM_COGNEE_PROVIDER", "COGNEE_LLM_PROVIDER", "LLM_PROVIDER")
        llm_model = _env("LLM_COGNEE_MODEL", "COGNEE_LLM_MODEL", "LITELLM_MODEL")
        llm_api_key = _env("LLM_COGNEE_API_KEY", "COGNEE_LLM_API_KEY", "LLM_API_KEY")

        service_password = os.getenv("COGNEE_SERVICE_USER_PASSWORD", "")

        lines = [
            "# Auto-generated by FuzzForge. Updates when project config changes.",
            "COGNEE_STORAGE_MODE=service",
            "STORAGE_BACKEND=s3",
        ]

        if bucket:
            lines.append(f"STORAGE_BUCKET_NAME={bucket}")
        if prefix:
            lines.append(f"COGNEE_S3_PREFIX={prefix}")
        if data_root:
            lines.append(f"COGNEE_DATA_ROOT={data_root}")
        if system_root:
            lines.append(f"COGNEE_SYSTEM_ROOT={system_root}")

        lines.extend(
            [
                f"ENABLE_BACKEND_ACCESS_CONTROL={backend_flag}",
                f"GRAPH_DATABASE_PROVIDER={cognee.graph_database_provider}",
                f"COGNEE_PROJECT_ID={self.project.id}",
                f"COGNEE_TENANT_KEY={self.project.tenant_id}",
            ]
        )

        if cognee.service_user_email:
            lines.append(f"COGNEE_SERVICE_USER_EMAIL={cognee.service_user_email}")
            lines.append(f"DEFAULT_USER_EMAIL={cognee.service_user_email}")
        else:
            lines.append("COGNEE_SERVICE_USER_EMAIL=")
            lines.append("DEFAULT_USER_EMAIL=")

        if cognee.service_user_password:
            service_password = cognee.service_user_password
            lines.append(f"COGNEE_SERVICE_USER_PASSWORD={service_password}")
            lines.append(f"DEFAULT_USER_PASSWORD={service_password}")
        else:
            lines.append("COGNEE_SERVICE_USER_PASSWORD=")
            lines.append("DEFAULT_USER_PASSWORD=")

        if aws_key:
            lines.append(f"AWS_ACCESS_KEY_ID={aws_key}")
        if aws_secret:
            lines.append(f"AWS_SECRET_ACCESS_KEY={aws_secret}")
        if aws_endpoint:
            lines.append(f"AWS_ENDPOINT_URL={aws_endpoint}")
        if aws_region:
            lines.append(f"AWS_REGION={aws_region}")
            lines.append(f"AWS_DEFAULT_REGION={aws_region}")

        if llm_provider:
            lines.append(f"LLM_PROVIDER={llm_provider}")
        if llm_model:
            lines.append(f"LLM_MODEL={llm_model}")
        if llm_api_key:
            lines.append(f"LLM_API_KEY={llm_api_key}")

        service_url = cognee.service_url or _env("COGNEE_SERVICE_URL", "COGNEE_API_URL")
        if service_url:
            lines.append(f"COGNEE_SERVICE_URL={service_url}")

        env_path.write_text("\n".join(lines) + "\n", encoding="utf-8")

    def _provision_cognee_service_account(self, project_dir: Path) -> dict[str, str]:
        """Ensure the hosted Cognee service has a user/tenant/dataset for this project.

        Returns:
            Dictionary with status info: {"status": "success|error|skipped", "message": "..."}
        """
        # Lazy import to avoid circular dependency
        try:
            from fuzzforge_ai.cognee_api_client import CogneeAPIClient
        except ImportError:
            return {"status": "skipped", "message": "CogneeAPIClient not available"}

        if self.cognee.mode != "service":
            return {"status": "skipped", "message": "Cognee mode is not 'service'"}

        service_url = os.getenv("COGNEE_SERVICE_URL") or self.cognee.service_url
        if not service_url:
            return {"status": "skipped", "message": "No service URL configured"}

        email = self.cognee.service_user_email
        password = self.cognee.service_user_password
        if not email or not password:
            return {"status": "error", "message": "Missing service credentials"}

        tenant_name = self.project.tenant_id or f"fuzzforge_project_{self.project.id}"
        dataset_name = f"{self.project.name}_codebase"

        async def _bootstrap():
            client = CogneeAPIClient(service_url)
            try:
                profile = await client.ensure_user(email, password, tenant_name)
                dataset_info = await client.ensure_dataset(dataset_name)
                return {
                    "status": "success",
                    "message": f"Provisioned user/tenant/dataset on Cognee service",
                    "user": profile.get("email"),
                    "tenant": tenant_name,
                    "dataset": dataset_name,
                    "dataset_id": dataset_info.get("id"),
                }
            except Exception as exc:
                return {
                    "status": "error",
                    "message": f"Failed to provision Cognee service: {exc}",
                    "error": str(exc),
                }
            finally:
                await client.close()

        try:
            return asyncio.run(_bootstrap())
        except RuntimeError as exc:  # pragma: no cover - fallback where loop already running
            if "event loop" in str(exc):
                loop = asyncio.get_event_loop()
                return loop.run_until_complete(_bootstrap())
            raise
        except Exception as exc:
            return {
                "status": "error",
                "message": f"Failed to run bootstrap: {exc}",
                "error": str(exc),
            }

    def get_api_url(self) -> str:
        """Get API URL with environment variable override."""
        return os.getenv("FUZZFORGE_API_URL", self.project.api_url)

    def get_timeout(self) -> int:
        """Get timeout with environment variable override."""
        env_timeout = os.getenv("FUZZFORGE_TIMEOUT")
        if env_timeout and env_timeout.isdigit():
            return int(env_timeout)
        return self.project.default_timeout

    def get_project_context(self, project_dir: Path) -> Dict[str, str]:
        """Return project metadata for AI integrations."""
        self.ensure_cognee_defaults(project_dir)
        return {
            "project_id": self.project.id or "unknown_project",
            "project_name": self.project.name,
            "tenant_id": self.project.tenant_id or "fuzzforge_tenant",
            "data_directory": self.cognee.data_directory,
            "system_directory": self.cognee.system_directory,
            "project_dir": str(project_dir),
            "cognee_mode": self.cognee.mode,
            "cognee_storage_backend": self.cognee.storage_backend,
            "cognee_s3_bucket": self.cognee.s3_bucket,
            "cognee_s3_prefix": self.cognee.s3_prefix,
            "cognee_service_user_email": self.cognee.service_user_email,
            "cognee_service_user_password": self.cognee.service_user_password,
            "cognee_service_env_dir": self.cognee.service_env_dir,
            "cognee_service_url": self.cognee.service_url,
        }

    def get_cognee_config(self, project_dir: Path) -> Dict[str, Any]:
        """Expose Cognee configuration as a plain dictionary."""
        self.ensure_cognee_defaults(project_dir)
        return self.cognee.model_dump()


# ----------------------------------------------------------------------
# Project-level helpers used across the CLI
# ----------------------------------------------------------------------

def _get_project_paths(project_dir: Path) -> Dict[str, Path]:
    config_dir = project_dir / ".fuzzforge"
    return {
        "config_dir": config_dir,
        "config_path": config_dir / "config.yaml",
    }


def get_project_config(project_dir: Optional[Path] = None) -> Optional[FuzzForgeConfig]:
    """Get configuration for the current project."""
    project_dir = Path(project_dir or Path.cwd())
    paths = _get_project_paths(project_dir)
    config_path = paths["config_path"]

    if not config_path.exists():
        return None

    config = FuzzForgeConfig.from_file(config_path)
    if config.ensure_cognee_defaults(project_dir):
        config.save_to_file(config_path)
    return config


def ensure_project_config(
    project_dir: Optional[Path] = None,
    project_name: Optional[str] = None,
    api_url: Optional[str] = None,
) -> FuzzForgeConfig:
    """Ensure project configuration exists, creating defaults if needed."""
    project_dir = Path(project_dir or Path.cwd())
    paths = _get_project_paths(project_dir)
    config_dir = paths["config_dir"]
    config_path = paths["config_path"]

    config_dir.mkdir(parents=True, exist_ok=True)

    if config_path.exists():
        config = FuzzForgeConfig.from_file(config_path)
    else:
        config = FuzzForgeConfig()

    if project_name:
        config.project.name = project_name
    if api_url:
        config.project.api_url = api_url

    if config.ensure_cognee_defaults(project_dir):
        config.save_to_file(config_path)
    else:
        # Still ensure latest values persisted (e.g., updated name/url)
        config.save_to_file(config_path)

    return config


def provision_cognee_service_for_project(
    project_dir: Optional[Path] = None,
) -> dict[str, str]:
    """Provision user/tenant/dataset on the Cognee service for this project.

    Args:
        project_dir: Project directory path

    Returns:
        Dictionary with provisioning status
    """
    project_dir = Path(project_dir or Path.cwd())
    config = get_project_config(project_dir)
    if config is None:
        return {
            "status": "error",
            "message": "Project not initialized. Run 'ff init project' first.",
        }

    return config._provision_cognee_service_account(project_dir)


def get_global_config() -> FuzzForgeConfig:
    """Get global user configuration."""
    home = Path.home()
    global_config_dir = home / ".config" / "fuzzforge"
    global_config_path = global_config_dir / "config.yaml"

    if global_config_path.exists():
        return FuzzForgeConfig.from_file(global_config_path)

    return FuzzForgeConfig()


def save_global_config(config: FuzzForgeConfig) -> None:
    """Save global user configuration."""
    home = Path.home()
    global_config_dir = home / ".config" / "fuzzforge"
    global_config_path = global_config_dir / "config.yaml"
    config.save_to_file(global_config_path)


# ----------------------------------------------------------------------
# Compatibility layer for AI modules
# ----------------------------------------------------------------------

class ProjectConfigManager:
    """Lightweight wrapper mimicking the legacy Config class used by the AI module."""

    def __init__(self, project_dir: Optional[Path] = None):
        self.project_dir = Path(project_dir or Path.cwd())
        paths = _get_project_paths(self.project_dir)
        self.config_path = paths["config_dir"]
        self.file_path = paths["config_path"]
        self._config = get_project_config(self.project_dir)
        if self._config is None:
            raise FileNotFoundError(
                f"FuzzForge project not initialized in {self.project_dir}. Run 'ff init'."
            )

    # Legacy API ------------------------------------------------------
    def is_initialized(self) -> bool:
        return self.file_path.exists()

    def get_project_context(self) -> Dict[str, str]:
        return self._config.get_project_context(self.project_dir)

    def get_cognee_config(self) -> Dict[str, Any]:
        return self._config.get_cognee_config(self.project_dir)

    def setup_cognee_environment(self) -> None:
        cognee = self.get_cognee_config()
        if not cognee.get("enabled", True):
            return

        # Load project-specific environment overrides from .fuzzforge/.env if available
        env_file = self.project_dir / ".fuzzforge" / ".env"
        if env_file.exists():
            if load_dotenv:
                load_dotenv(env_file, override=False)
            else:
                try:
                    for line in env_file.read_text(encoding="utf-8").splitlines():
                        stripped = line.strip()
                        if not stripped or stripped.startswith("#"):
                            continue
                        if "=" not in stripped:
                            continue
                        key, value = stripped.split("=", 1)
                        os.environ.setdefault(key.strip(), value.strip())
                except Exception:  # pragma: no cover - best effort fallback
                    pass

        def _env(*names: str, default: str | None = None) -> str | None:
            for name in names:
                value = os.getenv(name)
                if value:
                    return value
            return default

        storage_mode = cognee.get("mode", "embedded")
        os.environ["COGNEE_STORAGE_MODE"] = storage_mode

        storage_backend = cognee.get("storage_backend", "filesystem")
        os.environ["STORAGE_BACKEND"] = storage_backend

        if storage_backend == "s3":
            bucket = cognee.get("s3_bucket") or _env("COGNEE_S3_BUCKET", "S3_BUCKET")
            if bucket:
                os.environ["STORAGE_BUCKET_NAME"] = bucket

            prefix = cognee.get("s3_prefix") or _env("COGNEE_S3_PREFIX")
            if prefix:
                os.environ["COGNEE_S3_PREFIX"] = prefix

            aws_key = _env("COGNEE_AWS_ACCESS_KEY_ID", "AWS_ACCESS_KEY_ID", "S3_ACCESS_KEY")
            if aws_key:
                os.environ["AWS_ACCESS_KEY_ID"] = aws_key

            aws_secret = _env(
                "COGNEE_AWS_SECRET_ACCESS_KEY",
                "AWS_SECRET_ACCESS_KEY",
                "S3_SECRET_KEY",
            )
            if aws_secret:
                os.environ["AWS_SECRET_ACCESS_KEY"] = aws_secret

            aws_endpoint = _env(
                "COGNEE_AWS_ENDPOINT_URL",
                "AWS_ENDPOINT_URL",
                "S3_ENDPOINT",
            )
            if aws_endpoint:
                os.environ["AWS_ENDPOINT_URL"] = aws_endpoint

            aws_region = _env("COGNEE_AWS_REGION", "AWS_REGION", "S3_REGION")
            if aws_region:
                os.environ["AWS_REGION"] = aws_region
                os.environ["AWS_DEFAULT_REGION"] = aws_region

        backend_access = "true" if cognee.get("backend_access_control", True) else "false"
        os.environ["ENABLE_BACKEND_ACCESS_CONTROL"] = backend_access
        os.environ["GRAPH_DATABASE_PROVIDER"] = cognee.get("graph_database_provider", "kuzu")

        data_dir = cognee.get("data_directory")
        system_dir = cognee.get("system_directory")
        tenant_id = cognee.get("tenant_id", "fuzzforge_tenant")

        if data_dir:
            os.environ["COGNEE_DATA_ROOT"] = data_dir
        if system_dir:
            os.environ["COGNEE_SYSTEM_ROOT"] = system_dir
        os.environ["COGNEE_USER_ID"] = tenant_id
        os.environ["COGNEE_TENANT_ID"] = tenant_id

        os.environ["COGNEE_PROJECT_ID"] = cognee.get("project_id", self._config.project.id)
        service_user_email = cognee.get("service_user_email")
        if service_user_email:
            os.environ["COGNEE_SERVICE_USER_EMAIL"] = service_user_email
            os.environ["DEFAULT_USER_EMAIL"] = service_user_email
        service_password = _env(
            "COGNEE_SERVICE_USER_PASSWORD",
            "DEFAULT_USER_PASSWORD",
            default=self._config.cognee.service_user_password,
        )
        if service_password:
            os.environ["COGNEE_SERVICE_USER_PASSWORD"] = service_password
            os.environ["DEFAULT_USER_PASSWORD"] = service_password
        if cognee.get("service_env_dir"):
            os.environ["COGNEE_SERVICE_ENV_PATH"] = cognee["service_env_dir"]

        if cognee.get("service_url"):
            os.environ["COGNEE_SERVICE_URL"] = cognee["service_url"]
        if os.getenv("COGNEE_SERVICE_PORT"):
            os.environ["COGNEE_SERVICE_PORT"] = os.getenv("COGNEE_SERVICE_PORT")

        # Configure LLM provider defaults for Cognee. Values prefixed with COGNEE_
        # take precedence so users can segregate credentials.

        provider = _env(
            "LLM_COGNEE_PROVIDER",
            "COGNEE_LLM_PROVIDER",
            "LLM_PROVIDER",
            default="openai",
        )
        model = _env(
            "LLM_COGNEE_MODEL",
            "COGNEE_LLM_MODEL",
            "LLM_MODEL",
            "LITELLM_MODEL",
            default="gpt-4o-mini",
        )
        api_key = _env(
            "LLM_COGNEE_API_KEY",
            "COGNEE_LLM_API_KEY",
            "LLM_API_KEY",
            "OPENAI_API_KEY",
        )
        endpoint = _env("LLM_COGNEE_ENDPOINT", "COGNEE_LLM_ENDPOINT", "LLM_ENDPOINT")
        api_version = _env(
            "LLM_COGNEE_API_VERSION",
            "COGNEE_LLM_API_VERSION",
            "LLM_API_VERSION",
        )
        max_tokens = _env(
            "LLM_COGNEE_MAX_TOKENS",
            "COGNEE_LLM_MAX_TOKENS",
            "LLM_MAX_TOKENS",
        )

        if provider:
            os.environ["LLM_PROVIDER"] = provider
        if model:
            os.environ["LLM_MODEL"] = model
            # Maintain backwards compatibility with components expecting LITELLM_MODEL
            os.environ.setdefault("LITELLM_MODEL", model)
        if api_key:
            os.environ["LLM_API_KEY"] = api_key
            # Provide OPENAI_API_KEY fallback when using OpenAI-compatible providers
            if provider and provider.lower() in {"openai", "azure_openai", "custom"}:
                os.environ.setdefault("OPENAI_API_KEY", api_key)
        if endpoint:
            os.environ["LLM_ENDPOINT"] = endpoint
        if api_version:
            os.environ["LLM_API_VERSION"] = api_version
        if max_tokens:
            os.environ["LLM_MAX_TOKENS"] = str(max_tokens)

        # Provide a default MCP endpoint for local FuzzForge backend access when unset
        if not os.getenv("FUZZFORGE_MCP_URL"):
            os.environ["FUZZFORGE_MCP_URL"] = os.getenv(
                "FUZZFORGE_DEFAULT_MCP_URL",
                "http://localhost:8010/mcp",
            )

    def refresh(self) -> None:
        """Reload configuration from disk."""
        self._config = get_project_config(self.project_dir)
        if self._config is None:
            raise FileNotFoundError(
                f"FuzzForge project not initialized in {self.project_dir}. Run 'ff init'."
            )

    # Convenience accessors ------------------------------------------
    @property
    def fuzzforge_dir(self) -> Path:
        return self.config_path

    def get_api_url(self) -> str:
        return self._config.get_api_url()

    def get_timeout(self) -> int:
        return self._config.get_timeout()
