"""
Generic Prefect Statistics Monitor Service

This service monitors ALL workflows for structured live data logging and
updates the appropriate statistics APIs. Works with any workflow that follows
the standard LIVE_STATS logging pattern.
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


import asyncio
import json
import logging
from datetime import datetime, timedelta, timezone
from typing import Dict, Any, Optional
from prefect.client.orchestration import get_client
from prefect.client.schemas.objects import FlowRun, TaskRun
from src.models.findings import FuzzingStats
from src.api.fuzzing import fuzzing_stats, initialize_fuzzing_tracking, active_connections

logger = logging.getLogger(__name__)


class PrefectStatsMonitor:
    """Monitors Prefect flows and tasks for live statistics from any workflow"""

    def __init__(self):
        self.monitoring = False
        self.monitor_task = None
        self.monitored_runs = set()
        self.last_log_ts: Dict[str, datetime] = {}
        self._client = None
        self._client_refresh_time = None
        self._client_refresh_interval = 300  # Refresh connection every 5 minutes

    async def start_monitoring(self):
        """Start the Prefect statistics monitoring service"""
        if self.monitoring:
            logger.warning("Prefect stats monitor already running")
            return

        self.monitoring = True
        self.monitor_task = asyncio.create_task(self._monitor_flows())
        logger.info("Started Prefect statistics monitor")

    async def stop_monitoring(self):
        """Stop the monitoring service"""
        self.monitoring = False
        if self.monitor_task:
            self.monitor_task.cancel()
            try:
                await self.monitor_task
            except asyncio.CancelledError:
                pass
        logger.info("Stopped Prefect statistics monitor")

    async def _get_or_refresh_client(self):
        """Get or refresh Prefect client with connection pooling."""
        now = datetime.now(timezone.utc)

        if (self._client is None or
            self._client_refresh_time is None or
            (now - self._client_refresh_time).total_seconds() > self._client_refresh_interval):

            if self._client:
                try:
                    await self._client.aclose()
                except Exception:
                    pass

            self._client = get_client()
            self._client_refresh_time = now
            await self._client.__aenter__()

        return self._client

    async def _monitor_flows(self):
        """Main monitoring loop that watches Prefect flows"""
        try:
            while self.monitoring:
                try:
                    # Use connection pooling for better performance
                    client = await self._get_or_refresh_client()

                    # Get recent flow runs (limit to reduce load)
                    flow_runs = await client.read_flow_runs(
                        limit=50,
                        sort="START_TIME_DESC",
                    )

                    # Only consider runs from the last 15 minutes
                    recent_cutoff = datetime.now(timezone.utc) - timedelta(minutes=15)
                    for flow_run in flow_runs:
                        created = getattr(flow_run, "created", None)
                        if created is None:
                            continue
                        try:
                            # Ensure timezone-aware comparison
                            if created.tzinfo is None:
                                created = created.replace(tzinfo=timezone.utc)
                            if created >= recent_cutoff:
                                await self._monitor_flow_run(client, flow_run)
                        except Exception:
                            # If comparison fails, attempt monitoring anyway
                            await self._monitor_flow_run(client, flow_run)

                    await asyncio.sleep(5)  # Check every 5 seconds

                except Exception as e:
                    logger.error(f"Error in Prefect monitoring: {e}")
                    await asyncio.sleep(10)

        except asyncio.CancelledError:
            logger.info("Prefect monitoring cancelled")
        except Exception as e:
            logger.error(f"Fatal error in Prefect monitoring: {e}")
        finally:
            # Clean up client on exit
            if self._client:
                try:
                    await self._client.__aexit__(None, None, None)
                except Exception:
                    pass
                self._client = None

    async def _monitor_flow_run(self, client, flow_run: FlowRun):
        """Monitor a specific flow run for statistics"""
        run_id = str(flow_run.id)
        workflow_name = flow_run.name or "unknown"

        try:
            # Initialize tracking if not exists - only for workflows that might have live stats
            if run_id not in fuzzing_stats:
                initialize_fuzzing_tracking(run_id, workflow_name)
                self.monitored_runs.add(run_id)

            # Skip corrupted entries (should not happen after startup cleanup, but defensive)
            elif not isinstance(fuzzing_stats[run_id], FuzzingStats):
                logger.warning(f"Skipping corrupted stats entry for {run_id}, reinitializing")
                initialize_fuzzing_tracking(run_id, workflow_name)
                self.monitored_runs.add(run_id)

            # Get task runs for this flow
            task_runs = await client.read_task_runs(
                flow_run_filter={"id": {"any_": [flow_run.id]}},
                limit=25,
            )

            # Check all tasks for live statistics logging
            for task_run in task_runs:
                await self._extract_stats_from_task(client, run_id, task_run, workflow_name)

            # Also scan flow-level logs as a fallback
            await self._extract_stats_from_flow_logs(client, run_id, flow_run, workflow_name)

        except Exception as e:
            logger.warning(f"Error monitoring flow run {run_id}: {e}")

    async def _extract_stats_from_task(self, client, run_id: str, task_run: TaskRun, workflow_name: str):
        """Extract statistics from any task that logs live stats"""
        try:
            # Get task run logs
            logs = await client.read_logs(
                log_filter={
                    "task_run_id": {"any_": [task_run.id]}
                },
                limit=100,
                sort="TIMESTAMP_ASC"
            )

            # Parse logs for LIVE_STATS entries (generic pattern for any workflow)
            latest_stats = None
            for log in logs:
                # Prefer structured extra field if present
                extra_data = getattr(log, "extra", None) or getattr(log, "extra_fields", None) or None
                if isinstance(extra_data, dict):
                    stat_type = extra_data.get("stats_type")
                    if stat_type in ["fuzzing_live_update", "scan_progress", "analysis_update", "live_stats"]:
                        latest_stats = extra_data
                        continue

                # Fallback to parsing from message text
                if ("FUZZ_STATS" in log.message or "LIVE_STATS" in log.message):
                    stats = self._parse_stats_from_log(log.message)
                    if stats:
                        latest_stats = stats

            # Update statistics if we found any
            if latest_stats:
                # Calculate elapsed time from task start
                elapsed_time = 0
                if task_run.start_time:
                    # Ensure timezone-aware arithmetic
                    now = datetime.now(timezone.utc)
                    try:
                        elapsed_time = int((now - task_run.start_time).total_seconds())
                    except Exception:
                        # Fallback to naive UTC if types mismatch
                        elapsed_time = int((datetime.utcnow() - task_run.start_time.replace(tzinfo=None)).total_seconds())

                updated_stats = FuzzingStats(
                    run_id=run_id,
                    workflow=workflow_name,
                    executions=latest_stats.get("executions", 0),
                    executions_per_sec=latest_stats.get("executions_per_sec", 0.0),
                    crashes=latest_stats.get("crashes", 0),
                    unique_crashes=latest_stats.get("unique_crashes", 0),
                    corpus_size=latest_stats.get("corpus_size", 0),
                    elapsed_time=elapsed_time
                )

                # Update the global stats
                previous = fuzzing_stats.get(run_id)
                fuzzing_stats[run_id] = updated_stats

                # Broadcast to any active WebSocket clients for this run
                if active_connections.get(run_id):
                    # Handle both Pydantic objects and plain dicts
                    if isinstance(updated_stats, dict):
                        stats_data = updated_stats
                    elif hasattr(updated_stats, 'model_dump'):
                        stats_data = updated_stats.model_dump()
                    elif hasattr(updated_stats, 'dict'):
                        stats_data = updated_stats.dict()
                    else:
                        stats_data = updated_stats.__dict__

                    message = {
                        "type": "stats_update",
                        "data": stats_data,
                    }
                    disconnected = []
                    for ws in active_connections[run_id]:
                        try:
                            await ws.send_text(json.dumps(message))
                        except Exception:
                            disconnected.append(ws)
                    # Clean up disconnected sockets
                    for ws in disconnected:
                        try:
                            active_connections[run_id].remove(ws)
                        except ValueError:
                            pass

                logger.debug(f"Updated Prefect stats for {run_id}: {updated_stats.executions} execs")

        except Exception as e:
            logger.warning(f"Error extracting stats from task {task_run.id}: {e}")

    async def _extract_stats_from_flow_logs(self, client, run_id: str, flow_run: FlowRun, workflow_name: str):
        """Extract statistics by scanning flow-level logs for LIVE/FUZZ stats"""
        try:
            logs = await client.read_logs(
                log_filter={
                    "flow_run_id": {"any_": [flow_run.id]}
                },
                limit=200,
                sort="TIMESTAMP_ASC"
            )

            latest_stats = None
            last_seen = self.last_log_ts.get(run_id)
            max_ts = last_seen

            for log in logs:
                # Skip logs we've already processed
                ts = getattr(log, "timestamp", None)
                if last_seen and ts and ts <= last_seen:
                    continue
                if ts and (max_ts is None or ts > max_ts):
                    max_ts = ts

                # Prefer structured extra field if available
                extra_data = getattr(log, "extra", None) or getattr(log, "extra_fields", None) or None
                if isinstance(extra_data, dict):
                    stat_type = extra_data.get("stats_type")
                    if stat_type in ["fuzzing_live_update", "scan_progress", "analysis_update", "live_stats"]:
                        latest_stats = extra_data
                        continue

                # Fallback to message parse
                if ("FUZZ_STATS" in log.message or "LIVE_STATS" in log.message):
                    stats = self._parse_stats_from_log(log.message)
                    if stats:
                        latest_stats = stats

            if max_ts:
                self.last_log_ts[run_id] = max_ts

            if latest_stats:
                # Use flow_run timestamps for elapsed time if available
                elapsed_time = 0
                start_time = getattr(flow_run, "start_time", None) or getattr(flow_run, "start_time", None)
                if start_time:
                    now = datetime.now(timezone.utc)
                    try:
                        if start_time.tzinfo is None:
                            start_time = start_time.replace(tzinfo=timezone.utc)
                        elapsed_time = int((now - start_time).total_seconds())
                    except Exception:
                        elapsed_time = int((datetime.utcnow() - start_time.replace(tzinfo=None)).total_seconds())

                updated_stats = FuzzingStats(
                    run_id=run_id,
                    workflow=workflow_name,
                    executions=latest_stats.get("executions", 0),
                    executions_per_sec=latest_stats.get("executions_per_sec", 0.0),
                    crashes=latest_stats.get("crashes", 0),
                    unique_crashes=latest_stats.get("unique_crashes", 0),
                    corpus_size=latest_stats.get("corpus_size", 0),
                    elapsed_time=elapsed_time
                )

                fuzzing_stats[run_id] = updated_stats

                # Broadcast if listeners exist
                if active_connections.get(run_id):
                    # Handle both Pydantic objects and plain dicts
                    if isinstance(updated_stats, dict):
                        stats_data = updated_stats
                    elif hasattr(updated_stats, 'model_dump'):
                        stats_data = updated_stats.model_dump()
                    elif hasattr(updated_stats, 'dict'):
                        stats_data = updated_stats.dict()
                    else:
                        stats_data = updated_stats.__dict__

                    message = {
                        "type": "stats_update",
                        "data": stats_data,
                    }
                    disconnected = []
                    for ws in active_connections[run_id]:
                        try:
                            await ws.send_text(json.dumps(message))
                        except Exception:
                            disconnected.append(ws)
                    for ws in disconnected:
                        try:
                            active_connections[run_id].remove(ws)
                        except ValueError:
                            pass

        except Exception as e:
            logger.warning(f"Error extracting stats from flow logs {run_id}: {e}")

    def _parse_stats_from_log(self, log_message: str) -> Optional[Dict[str, Any]]:
        """Parse statistics from a log message"""
        try:
            import re

            # Prefer explicit JSON after marker tokens
            m = re.search(r'(?:FUZZ_STATS|LIVE_STATS)\s+(\{.*\})', log_message)
            if m:
                try:
                    return json.loads(m.group(1))
                except Exception:
                    pass

            # Fallback: Extract the extra= dict and coerce to JSON
            stats_match = re.search(r'extra=({.*?})', log_message)
            if not stats_match:
                return None

            extra_str = stats_match.group(1)
            extra_str = extra_str.replace("'", '"')
            extra_str = extra_str.replace('None', 'null')
            extra_str = extra_str.replace('True', 'true')
            extra_str = extra_str.replace('False', 'false')

            stats_data = json.loads(extra_str)

            # Support multiple stat types for different workflows
            stat_type = stats_data.get("stats_type")
            if stat_type in ["fuzzing_live_update", "scan_progress", "analysis_update", "live_stats"]:
                return stats_data

        except Exception as e:
            logger.debug(f"Error parsing log stats: {e}")

        return None


# Global instance
prefect_stats_monitor = PrefectStatsMonitor()
