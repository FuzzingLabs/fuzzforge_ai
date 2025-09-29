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
from datetime import datetime, timezone, timedelta


from src.services.prefect_stats_monitor import PrefectStatsMonitor
from src.api import fuzzing


class FakeLog:
    def __init__(self, message: str):
        self.message = message


class FakeClient:
    def __init__(self, logs):
        self._logs = logs

    async def read_logs(self, log_filter=None, limit=100, sort="TIMESTAMP_ASC"):
        return self._logs


class FakeTaskRun:
    def __init__(self):
        self.id = "task-1"
        self.start_time = datetime.now(timezone.utc) - timedelta(seconds=5)


def test_parse_stats_from_log_fuzzing():
    mon = PrefectStatsMonitor()
    msg = (
        "INFO LIVE_STATS extra={'stats_type': 'fuzzing_live_update', "
        "'executions': 42, 'executions_per_sec': 3.14, 'crashes': 1, 'unique_crashes': 1, 'corpus_size': 9}"
    )
    stats = mon._parse_stats_from_log(msg)
    assert stats is not None
    assert stats["stats_type"] == "fuzzing_live_update"
    assert stats["executions"] == 42


def test_extract_stats_updates_and_broadcasts():
    mon = PrefectStatsMonitor()
    run_id = "run-123"
    workflow = "wf"
    fuzzing.initialize_fuzzing_tracking(run_id, workflow)

    # Prepare a fake websocket to capture messages
    sent = []

    class FakeWS:
        async def send_text(self, text: str):
            sent.append(text)

    fuzzing.active_connections[run_id] = [FakeWS()]

    # Craft a log line the parser understands
    msg = (
        "INFO LIVE_STATS extra={'stats_type': 'fuzzing_live_update', "
        "'executions': 10, 'executions_per_sec': 1.5, 'crashes': 0, 'unique_crashes': 0, 'corpus_size': 2}"
    )
    fake_client = FakeClient([FakeLog(msg)])
    task_run = FakeTaskRun()

    asyncio.run(mon._extract_stats_from_task(fake_client, run_id, task_run, workflow))

    # Verify stats updated
    stats = fuzzing.fuzzing_stats[run_id]
    assert stats.executions == 10
    assert stats.executions_per_sec == 1.5

    # Verify a message was sent to WebSocket
    assert sent, "Expected a stats_update message to be sent"
