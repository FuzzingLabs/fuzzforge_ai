"""Jadx APK Decompilation Module"""

import asyncio
import shutil
from pathlib import Path
from typing import Dict, Any
import logging

from ..base import BaseModule, ModuleMetadata, ModuleResult
from . import register_module

logger = logging.getLogger(__name__)


@register_module
class JadxModule(BaseModule):
    """Module responsible for decompiling APK files with Jadx"""

    def get_metadata(self) -> ModuleMetadata:
        return ModuleMetadata(
            name="jadx",
            version="1.5.0",
            description="Android APK decompilation using Jadx",
            author="FuzzForge Team",
            category="android",
            tags=["android", "jadx", "decompilation", "reverse"],
            input_schema={
                "type": "object",
                "properties": {
                    "apk_path": {
                        "type": "string",
                        "description": "Path to the APK to decompile (absolute or relative to workspace)",
                    },
                    "output_dir": {
                        "type": "string",
                        "description": "Directory (relative to workspace) where Jadx output should be written",
                        "default": "jadx_output",
                    },
                    "overwrite": {
                        "type": "boolean",
                        "description": "Overwrite existing output directory if present",
                        "default": True,
                    },
                    "threads": {
                        "type": "integer",
                        "description": "Number of Jadx decompilation threads",
                        "default": 4,
                    },
                    "decompiler_args": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Additional arguments passed directly to Jadx",
                    },
                },
                "required": ["apk_path"],
            },
            output_schema={
                "type": "object",
                "properties": {
                    "output_dir": {"type": "string"},
                    "source_dir": {"type": "string"},
                    "resource_dir": {"type": "string"},
                },
            },
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        apk_path = config.get("apk_path")
        if not apk_path:
            raise ValueError("'apk_path' must be provided for Jadx decompilation")

        threads = config.get("threads", 4)
        if not isinstance(threads, int) or threads < 1 or threads > 32:
            raise ValueError("threads must be between 1 and 32")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        self.start_timer()

        try:
            self.validate_config(config)

            workspace = workspace.resolve()
            if not workspace.exists():
                raise ValueError(f"Workspace does not exist: {workspace}")

            apk_path = Path(config["apk_path"])
            if not apk_path.is_absolute():
                apk_path = (workspace / apk_path).resolve()

            if not apk_path.exists():
                raise ValueError(f"APK not found: {apk_path}")

            if apk_path.is_dir():
                raise ValueError(f"APK path must be a file, not a directory: {apk_path}")

            output_dir = Path(config.get("output_dir", "jadx_output"))
            if not output_dir.is_absolute():
                output_dir = (workspace / output_dir).resolve()

            if output_dir.exists():
                if config.get("overwrite", True):
                    shutil.rmtree(output_dir)
                else:
                    raise ValueError(
                        f"Output directory already exists: {output_dir}. Set overwrite=true to replace it."
                    )

            output_dir.mkdir(parents=True, exist_ok=True)

            threads = str(config.get("threads", 4))
            extra_args = config.get("decompiler_args", []) or []

            cmd = [
                "jadx",
                "--threads-count",
                threads,
                "--deobf",
                "--output-dir",
                str(output_dir),
            ]
            cmd.extend(extra_args)
            cmd.append(str(apk_path))

            logger.info("Running Jadx decompilation: %s", " ".join(cmd))

            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=str(workspace),
            )

            stdout, stderr = await process.communicate()
            stdout_str = stdout.decode(errors="ignore") if stdout else ""
            stderr_str = stderr.decode(errors="ignore") if stderr else ""

            if stdout_str:
                logger.debug("Jadx stdout: %s", stdout_str[:200])
            if stderr_str:
                logger.debug("Jadx stderr: %s", stderr_str[:200])

            if process.returncode != 0:
                error_output = stderr_str or stdout_str or "No error output"
                raise RuntimeError(
                    f"Jadx failed with exit code {process.returncode}: {error_output[:500]}"
                )

            logger.debug("Jadx stdout: %s", stdout.decode(errors="ignore")[:200])

            source_dir = output_dir / "sources"
            resource_dir = output_dir / "resources"

            if not source_dir.exists():
                logger.warning("Jadx sources directory not found at expected path: %s", source_dir)
            else:
                sample_files = []
                for idx, file_path in enumerate(source_dir.rglob("*.java")):
                    sample_files.append(str(file_path))
                    if idx >= 4:
                        break
                logger.info("Sample Jadx Java files: %s", sample_files or "<none>")

            java_files = 0
            if source_dir.exists():
                java_files = sum(1 for _ in source_dir.rglob("*.java"))

            summary = {
                "output_dir": str(output_dir),
                "source_dir": str(source_dir if source_dir.exists() else output_dir),
                "resource_dir": str(resource_dir if resource_dir.exists() else output_dir),
                "java_files": java_files,
            }

            metadata = {
                "apk_path": str(apk_path),
                "output_dir": str(output_dir),
                "source_dir": summary["source_dir"],
                "resource_dir": summary["resource_dir"],
                "threads": threads,
            }

            return self.create_result(
                findings=[],
                status="success",
                summary=summary,
                metadata=metadata,
            )

        except Exception as exc:
            logger.error("Jadx module failed: %s", exc)
            return self.create_result(
                findings=[],
                status="failed",
                error=str(exc),
            )