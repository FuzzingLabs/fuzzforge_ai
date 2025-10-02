"""
Atheris Fuzzing Workflow Activities

Activities specific to the Atheris fuzzing workflow.
"""

import logging
import sys
from datetime import datetime
from pathlib import Path
from typing import Dict, Any

from temporalio import activity

# Configure logging
logger = logging.getLogger(__name__)

# Add toolbox to path for module imports
sys.path.insert(0, '/app/toolbox')


@activity.defn(name="fuzz_with_atheris")
async def fuzz_activity(workspace_path: str, config: dict) -> dict:
    """
    Fuzzing activity using the AtherisFuzzer module on user code.

    This activity:
    1. Imports the reusable AtherisFuzzer module
    2. Sets up real-time stats callback
    3. Executes fuzzing on user's TestOneInput() function
    4. Returns findings as ModuleResult

    Args:
        workspace_path: Path to the workspace directory (user's uploaded code)
        config: Fuzzer configuration (target_file, max_iterations, timeout_seconds)

    Returns:
        Fuzzer results dictionary (findings, summary, metadata)
    """
    logger.info(f"Activity: fuzz_with_atheris (workspace={workspace_path})")

    try:
        # Import reusable AtherisFuzzer module
        from modules.fuzzer import AtherisFuzzer

        workspace = Path(workspace_path)
        if not workspace.exists():
            raise FileNotFoundError(f"Workspace not found: {workspace_path}")

        # Get activity info for real-time stats
        info = activity.info()
        run_id = info.workflow_id

        # Define stats callback for real-time monitoring
        async def stats_callback(stats_data: Dict[str, Any]):
            """Callback for live fuzzing statistics"""
            try:
                logger.info("LIVE_STATS", extra={
                    "stats_type": "fuzzing_live_update",
                    "workflow_type": "atheris_fuzzing",
                    "run_id": run_id,
                    "executions": stats_data.get("total_execs", 0),
                    "executions_per_sec": stats_data.get("execs_per_sec", 0.0),
                    "crashes": stats_data.get("crashes", 0),
                    "corpus_size": stats_data.get("corpus_size", 0),
                    "coverage": stats_data.get("coverage", 0.0),
                    "elapsed_time": stats_data.get("elapsed_time", 0),
                    "timestamp": datetime.utcnow().isoformat()
                })
            except Exception as e:
                logger.warning(f"Error in stats callback: {e}")

        # Add stats callback to config
        config["stats_callback"] = stats_callback

        # Execute the fuzzer module
        fuzzer = AtherisFuzzer()
        result = await fuzzer.execute(config, workspace)

        logger.info(
            f"✓ Fuzzing completed: "
            f"{result.summary.get('total_executions', 0)} executions, "
            f"{result.summary.get('crashes_found', 0)} crashes"
        )

        return result.dict()

    except Exception as e:
        logger.error(f"Fuzzing failed: {e}", exc_info=True)
        raise
