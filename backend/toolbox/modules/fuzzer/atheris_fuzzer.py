"""
Atheris Fuzzer Module

Reusable module for fuzzing Python code using Atheris.
Discovers and fuzzes user-provided Python targets with TestOneInput() function.
"""

import asyncio
import base64
import importlib.util
import logging
import sys
import time
import traceback
from pathlib import Path
from typing import Dict, Any, List, Optional, Callable
import uuid

from modules.base import BaseModule, ModuleMetadata, ModuleResult, ModuleFinding

logger = logging.getLogger(__name__)


class AtherisFuzzer(BaseModule):
    """
    Atheris fuzzing module - discovers and fuzzes Python code.

    This module can be used by any workflow to fuzz Python targets.
    """

    def __init__(self):
        super().__init__()
        self.crashes = []
        self.total_executions = 0
        self.start_time = None
        self.last_stats_time = 0

    def get_metadata(self) -> ModuleMetadata:
        """Return module metadata"""
        return ModuleMetadata(
            name="atheris_fuzzer",
            version="1.0.0",
            description="Python fuzzing using Atheris - discovers and fuzzes TestOneInput() functions",
            author="FuzzForge Team",
            category="fuzzer",
            tags=["fuzzing", "atheris", "python", "coverage"],
            input_schema={
                "type": "object",
                "properties": {
                    "target_file": {
                        "type": "string",
                        "description": "Python file with TestOneInput() function (auto-discovered if not specified)"
                    },
                    "max_iterations": {
                        "type": "integer",
                        "description": "Maximum fuzzing iterations",
                        "default": 100000
                    },
                    "timeout_seconds": {
                        "type": "integer",
                        "description": "Fuzzing timeout in seconds",
                        "default": 300
                    },
                    "stats_callback": {
                        "description": "Optional callback for real-time statistics"
                    }
                }
            },
            requires_workspace=True
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate fuzzing configuration"""
        max_iterations = config.get("max_iterations", 100000)
        if not isinstance(max_iterations, int) or max_iterations <= 0:
            raise ValueError(f"max_iterations must be positive integer, got: {max_iterations}")

        timeout = config.get("timeout_seconds", 300)
        if not isinstance(timeout, int) or timeout <= 0:
            raise ValueError(f"timeout_seconds must be positive integer, got: {timeout}")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """
        Execute Atheris fuzzing on user code.

        Args:
            config: Fuzzing configuration
            workspace: Path to user's uploaded code

        Returns:
            ModuleResult with crash findings
        """
        self.start_timer()
        self.start_time = time.time()

        # Validate configuration
        self.validate_config(config)
        self.validate_workspace(workspace)

        # Extract config
        target_file = config.get("target_file")
        max_iterations = config.get("max_iterations", 100000)
        timeout_seconds = config.get("timeout_seconds", 300)
        stats_callback = config.get("stats_callback")

        logger.info(
            f"Starting Atheris fuzzing (max_iterations={max_iterations}, "
            f"timeout={timeout_seconds}s, target={target_file or 'auto-discover'})"
        )

        try:
            # Step 1: Discover or load target
            target_path = self._discover_target(workspace, target_file)
            logger.info(f"Using fuzz target: {target_path}")

            # Step 2: Load target module
            test_one_input = self._load_target_module(target_path)
            logger.info(f"Loaded TestOneInput function from {target_path}")

            # Step 3: Run fuzzing
            await self._run_fuzzing(
                test_one_input=test_one_input,
                target_path=target_path,
                max_iterations=max_iterations,
                timeout_seconds=timeout_seconds,
                stats_callback=stats_callback
            )

            # Step 4: Generate findings from crashes
            findings = self._generate_findings(target_path)

            logger.info(
                f"Fuzzing completed: {self.total_executions} executions, "
                f"{len(self.crashes)} crashes found"
            )

            return self.create_result(
                findings=findings,
                status="success",
                summary={
                    "total_executions": self.total_executions,
                    "crashes_found": len(self.crashes),
                    "execution_time": self.get_execution_time(),
                    "target_file": str(target_path.relative_to(workspace))
                },
                metadata={
                    "max_iterations": max_iterations,
                    "timeout_seconds": timeout_seconds
                }
            )

        except Exception as e:
            logger.error(f"Fuzzing failed: {e}", exc_info=True)
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    def _discover_target(self, workspace: Path, target_file: Optional[str]) -> Path:
        """
        Discover fuzz target in workspace.

        Args:
            workspace: Path to workspace
            target_file: Explicit target file or None for auto-discovery

        Returns:
            Path to target file
        """
        if target_file:
            # Use specified target
            target_path = workspace / target_file
            if not target_path.exists():
                raise FileNotFoundError(f"Target file not found: {target_file}")
            return target_path

        # Auto-discover: look for fuzz_*.py or *_fuzz.py
        logger.info("Auto-discovering fuzz targets...")

        candidates = []
        # Use rglob for recursive search (searches all subdirectories)
        for pattern in ["fuzz_*.py", "*_fuzz.py", "fuzz_target.py"]:
            matches = list(workspace.rglob(pattern))
            candidates.extend(matches)

        if not candidates:
            raise FileNotFoundError(
                "No fuzz targets found. Expected files matching: fuzz_*.py, *_fuzz.py, or fuzz_target.py"
            )

        # Use first candidate
        target = candidates[0]
        if len(candidates) > 1:
            logger.warning(
                f"Multiple fuzz targets found: {[str(c) for c in candidates]}. "
                f"Using: {target.name}"
            )

        return target

    def _load_target_module(self, target_path: Path) -> Callable:
        """
        Load target module and get TestOneInput function.

        Args:
            target_path: Path to Python file with TestOneInput

        Returns:
            TestOneInput function
        """
        # Add target directory to sys.path
        target_dir = target_path.parent
        if str(target_dir) not in sys.path:
            sys.path.insert(0, str(target_dir))

        # Load module dynamically
        module_name = target_path.stem
        spec = importlib.util.spec_from_file_location(module_name, target_path)
        if spec is None or spec.loader is None:
            raise ImportError(f"Cannot load module from {target_path}")

        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)

        # Get TestOneInput function
        if not hasattr(module, "TestOneInput"):
            raise AttributeError(
                f"Module {module_name} does not have TestOneInput() function. "
                "Atheris requires a TestOneInput(data: bytes) function."
            )

        return module.TestOneInput

    async def _run_fuzzing(
        self,
        test_one_input: Callable,
        target_path: Path,
        max_iterations: int,
        timeout_seconds: int,
        stats_callback: Optional[Callable] = None
    ):
        """
        Run Atheris fuzzing with real-time monitoring.

        Args:
            test_one_input: TestOneInput function to fuzz
            target_path: Path to target file
            max_iterations: Max iterations
            timeout_seconds: Timeout in seconds
            stats_callback: Optional callback for stats
        """
        import atheris

        self.crashes = []
        self.total_executions = 0
        corpus_size = 0

        # Wrapper to track executions and crashes
        def fuzz_wrapper(data):
            self.total_executions += 1

            try:
                test_one_input(data)
            except Exception as e:
                # Capture crash
                crash_info = {
                    "input": data,
                    "exception": e,
                    "exception_type": type(e).__name__,
                    "stack_trace": traceback.format_exc(),
                    "execution": self.total_executions
                }
                self.crashes.append(crash_info)
                logger.warning(
                    f"Crash found (execution {self.total_executions}): "
                    f"{type(e).__name__}: {str(e)}"
                )
                # Re-raise so Atheris detects it
                raise

        # Configure Atheris
        atheris.Setup(
            [
                "atheris_fuzzer",
                f"-runs={max_iterations}",
                f"-max_total_time={timeout_seconds}",
                "-print_final_stats=1"
            ],
            fuzz_wrapper
        )

        logger.info(f"Starting Atheris fuzzer (max_runs={max_iterations}, timeout={timeout_seconds}s)...")

        # Run fuzzing in a separate task with monitoring
        async def monitor_stats():
            """Monitor and report stats every 5 seconds"""
            while True:
                await asyncio.sleep(5)

                if stats_callback:
                    elapsed = time.time() - self.start_time
                    execs_per_sec = self.total_executions / elapsed if elapsed > 0 else 0

                    await stats_callback({
                        "total_execs": self.total_executions,
                        "execs_per_sec": execs_per_sec,
                        "crashes": len(self.crashes),
                        "corpus_size": corpus_size,
                        "coverage": 0.0,  # Atheris doesn't expose coverage easily
                        "elapsed_time": int(elapsed)
                    })

        # Start monitoring task
        monitor_task = None
        if stats_callback:
            monitor_task = asyncio.create_task(monitor_stats())

        try:
            # Run fuzzing (blocking)
            atheris.Fuzz()
        except SystemExit:
            # Atheris exits when done
            pass
        finally:
            # Stop monitoring
            if monitor_task:
                monitor_task.cancel()
                try:
                    await monitor_task
                except asyncio.CancelledError:
                    pass

    def _generate_findings(self, target_path: Path) -> List[ModuleFinding]:
        """
        Generate ModuleFinding objects from crashes.

        Args:
            target_path: Path to target file

        Returns:
            List of findings
        """
        findings = []

        for crash in self.crashes:
            # Encode crash input for storage
            crash_input_b64 = base64.b64encode(crash["input"]).decode()

            finding = self.create_finding(
                title=f"Crash: {crash['exception_type']}",
                description=(
                    f"Atheris found crash during fuzzing:\n"
                    f"Exception: {crash['exception_type']}\n"
                    f"Message: {str(crash['exception'])}\n"
                    f"Execution: {crash['execution']}"
                ),
                severity="critical",
                category="crash",
                file_path=str(target_path),
                metadata={
                    "crash_input_base64": crash_input_b64,
                    "crash_input_hex": crash["input"].hex(),
                    "exception_type": crash["exception_type"],
                    "stack_trace": crash["stack_trace"],
                    "execution_number": crash["execution"]
                },
                recommendation=(
                    "Review the crash stack trace and input to identify the vulnerability. "
                    "The crash input is provided in base64 and hex formats for reproduction."
                )
            )
            findings.append(finding)

        return findings
