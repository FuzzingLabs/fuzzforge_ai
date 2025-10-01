"""
Rust Test Workflow

Simple test workflow to verify:
1. Temporal worker discovery works
2. MinIO storage integration works
3. Activities execute correctly
4. Results are properly returned

This workflow:
- Downloads a target from MinIO
- Performs a simple analysis (file inspection)
- Returns results
- Cleans up cache
"""

from datetime import timedelta
from typing import Optional

from temporalio import workflow
from temporalio.common import RetryPolicy

# Import activity interfaces (will be executed by worker)
with workflow.unsafe.imports_passed_through():
    import logging

logger = logging.getLogger(__name__)


@workflow.defn
class RustTestWorkflow:
    """
    Simple test workflow for Rust vertical.

    This demonstrates the basic workflow pattern:
    1. Download target from MinIO
    2. Execute activities
    3. Return results
    4. Cleanup
    """

    @workflow.run
    async def run(
        self,
        target_id: str,
        test_message: Optional[str] = "Hello from Rust workflow!"
    ) -> dict:
        """
        Main workflow execution.

        Args:
            target_id: UUID of the uploaded target in MinIO
            test_message: Optional test message to include in results

        Returns:
            Dictionary containing workflow results
        """
        workflow_id = workflow.info().workflow_id

        workflow.logger.info(
            f"Starting RustTestWorkflow "
            f"(workflow_id={workflow_id}, target_id={target_id})"
        )

        results = {
            "workflow_id": workflow_id,
            "target_id": target_id,
            "message": test_message,
            "steps": []
        }

        try:
            # Step 1: Download target from MinIO
            workflow.logger.info("Step 1: Downloading target from MinIO")
            target_path = await workflow.execute_activity(
                "get_target",
                target_id,
                start_to_close_timeout=timedelta(minutes=5),
                retry_policy=RetryPolicy(
                    initial_interval=timedelta(seconds=1),
                    maximum_interval=timedelta(seconds=30),
                    maximum_attempts=3
                )
            )
            results["steps"].append({
                "step": "download_target",
                "status": "success",
                "target_path": target_path
            })
            workflow.logger.info(f"✓ Target downloaded to: {target_path}")

            # Step 2: Perform simple analysis (inline for testing)
            workflow.logger.info("Step 2: Performing simple analysis")
            # In a real workflow, this would be an activity that uses
            # AFL++, cargo-fuzz, or other Rust tools

            analysis_result = {
                "file_path": target_path,
                "analysis_type": "test",
                "findings": [
                    {
                        "type": "info",
                        "message": "Test workflow executed successfully",
                        "test_message": test_message
                    }
                ]
            }

            results["steps"].append({
                "step": "analysis",
                "status": "success",
                "analysis": analysis_result
            })
            workflow.logger.info("✓ Analysis completed")

            # Step 3: Upload results to MinIO (optional)
            workflow.logger.info("Step 3: Uploading results")
            try:
                results_url = await workflow.execute_activity(
                    "upload_results",
                    args=[workflow_id, results, "json"],
                    start_to_close_timeout=timedelta(minutes=2)
                )
                results["results_url"] = results_url
                workflow.logger.info(f"✓ Results uploaded to: {results_url}")
            except Exception as e:
                workflow.logger.warning(f"Failed to upload results: {e}")
                # Don't fail workflow if upload fails
                results["results_url"] = None

            # Step 4: Cleanup cache
            workflow.logger.info("Step 4: Cleaning up cache")
            try:
                await workflow.execute_activity(
                    "cleanup_cache",
                    target_path,
                    start_to_close_timeout=timedelta(minutes=1)
                )
                workflow.logger.info("✓ Cache cleaned up")
            except Exception as e:
                workflow.logger.warning(f"Cache cleanup failed: {e}")
                # Don't fail workflow if cleanup fails

            # Mark workflow as successful
            results["status"] = "success"
            workflow.logger.info(f"✓ Workflow completed successfully: {workflow_id}")

            return results

        except Exception as e:
            workflow.logger.error(f"Workflow failed: {e}")
            results["status"] = "error"
            results["error"] = str(e)
            results["steps"].append({
                "step": "error",
                "status": "failed",
                "error": str(e)
            })
            raise
