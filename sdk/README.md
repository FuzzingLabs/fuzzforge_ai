# FuzzForge SDK

A comprehensive Python SDK for the FuzzForge security testing workflow orchestration platform.

## Features

- **Complete API Coverage**: All FuzzForge API endpoints supported
- **Async & Sync**: Both synchronous and asynchronous client methods
- **Real-time Monitoring**: WebSocket and Server-Sent Events for live fuzzing updates
- **Type Safety**: Full Pydantic model validation for all data structures
- **Error Handling**: Comprehensive exception hierarchy with detailed error information
- **Utility Functions**: Helper functions for path validation, SARIF processing, and more

## Installation

Install using uv (recommended):

```bash
uv add fuzzforge-sdk
```

Or with pip:

```bash
pip install fuzzforge-sdk
```

## Quick Start

```python
from fuzzforge_sdk import FuzzForgeClient
from fuzzforge_sdk.utils import create_workflow_submission

# Initialize client
client = FuzzForgeClient(base_url="http://localhost:8000")

# List available workflows
workflows = client.list_workflows()

# Submit a workflow
submission = create_workflow_submission(
    target_path="/path/to/your/project",
    volume_mode="ro",
    timeout=300
)

response = client.submit_workflow("static-analysis", submission)

# Wait for completion and get results
final_status = client.wait_for_completion(response.run_id)
findings = client.get_run_findings(response.run_id)

client.close()
```

## Examples

The `examples/` directory contains complete working examples:

- **`basic_workflow.py`**: Simple workflow submission and monitoring
- **`fuzzing_monitor.py`**: Real-time fuzzing monitoring with WebSocket/SSE
- **`batch_analysis.py`**: Batch analysis of multiple projects

## Development

Install with development dependencies:

```bash
uv sync --extra dev
```