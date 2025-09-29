# How to Create a Custom Workflow in FuzzForge

This guide will walk you through the process of creating a custom security analysis workflow in FuzzForge. Workflows orchestrate modules, define the analysis pipeline, and enable you to automate complex security checks for your codebase or application.

---

## Prerequisites

Before you start, make sure you have:

- A working FuzzForge development environment (see [Contributing](/reference/contributing.md))
- Familiarity with Python (async/await), Docker, and Prefect 3
- At least one custom or built-in module to use in your workflow

---

## Step 1: Understand Workflow Architecture

A FuzzForge workflow is a Prefect 3 flow that:

- Runs in an isolated Docker container
- Orchestrates one or more analysis modules (scanner, analyzer, reporter, etc.)
- Handles secure volume mounting for code and results
- Produces standardized SARIF output
- Supports configurable parameters and resource limits

**Directory structure:**

```
backend/toolbox/workflows/{workflow_name}/
├── workflow.py          # Main workflow definition (Prefect flow)
├── Dockerfile           # Container image definition
├── metadata.yaml        # Workflow metadata and configuration
└── requirements.txt     # Additional Python dependencies (optional)
```

---

## Step 2: Define Workflow Metadata

Create a `metadata.yaml` file in your workflow directory. This file describes your workflow, its parameters, and resource requirements.

Example:

```yaml
name: dependency_analysis
version: "1.0.0"
description: "Analyzes project dependencies for security vulnerabilities"
author: "FuzzingLabs Security Team"
category: "comprehensive"
tags:
  - "dependency-scanning"
  - "vulnerability-analysis"
requirements:
  tools:
    - "dependency_scanner"
    - "vulnerability_analyzer"
    - "sarif_reporter"
  resources:
    memory: "512Mi"
    cpu: "1000m"
    timeout: 1800
parameters:
  type: object
  properties:
    target_path:
      type: string
      default: "/workspace"
      description: "Path to analyze"
    scan_dev_dependencies:
      type: boolean
      description: "Include development dependencies"
    vulnerability_threshold:
      type: string
      enum: ["low", "medium", "high", "critical"]
      description: "Minimum vulnerability severity to report"
output_schema:
  type: object
  properties:
    sarif:
      type: object
      description: "SARIF-formatted security findings"
    summary:
      type: object
      description: "Scan execution summary"
```

---

## Step 3: Add Live Statistics to Your Workflow 🚦

Want real-time progress and stats for your workflow? FuzzForge supports live statistics reporting using Prefect and structured logging. This lets users (and the platform) monitor workflow progress, see live updates, and stream stats via API or WebSocket.

### 1. Import Required Dependencies

```python
from prefect import task, get_run_context
import logging

logger = logging.getLogger(__name__)
```

### 2. Create a Statistics Callback Function

Add a callback that logs structured stats updates:

```python
@task(name="my_workflow_task")
async def my_workflow_task(workspace: Path, config: Dict[str, Any]) -> Dict[str, Any]:
    # Get run context for statistics reporting
    try:
        context = get_run_context()
        run_id = str(context.flow_run.id)
        logger.info(f"Running task for flow run: {run_id}")
    except Exception:
        run_id = None
        logger.warning("Could not get run context for statistics")

    # Define callback function for live statistics
    async def stats_callback(stats_data: Dict[str, Any]):
        """Callback to handle live statistics"""
        try:
            # Log structured statistics data for the backend to parse
            logger.info("LIVE_STATS", extra={
                "stats_type": "live_stats",           # Type of statistics
                "workflow_type": "my_workflow",       # Your workflow name
                "run_id": stats_data.get("run_id"),

                # Add your custom statistics fields here:
                "progress": stats_data.get("progress", 0),
                "items_processed": stats_data.get("items_processed", 0),
                "errors": stats_data.get("errors", 0),
                "elapsed_time": stats_data.get("elapsed_time", 0),
                "timestamp": stats_data.get("timestamp")
            })
        except Exception as e:
            logger.warning(f"Error in stats callback: {e}")

    # Pass callback to your module/processor
    processor = MyWorkflowModule()
    result = await processor.execute(config, workspace, stats_callback=stats_callback)
    return result.dict()
```

### 3. Update Your Module to Use the Callback

```python
class MyWorkflowModule:
    async def execute(self, config: Dict[str, Any], workspace: Path, stats_callback=None):
        # Your processing logic here

        # Periodically send statistics updates
        if stats_callback:
            await stats_callback({
                "run_id": run_id,
                "progress": current_progress,
                "items_processed": processed_count,
                "errors": error_count,
                "elapsed_time": elapsed_seconds,
                "timestamp": datetime.utcnow().isoformat()
            })
```

### 4. Supported Statistics Types

The monitor recognizes these `stats_type` values:

- `"fuzzing_live_update"` - For fuzzing workflows (uses FuzzingStats model)
- `"scan_progress"` - For security scanning workflows
- `"analysis_update"` - For code analysis workflows
- `"live_stats"` - Generic live statistics for any workflow

#### Example: Fuzzing Workflow Stats

```python
"stats_type": "fuzzing_live_update",
"executions": 12345,
"executions_per_sec": 1500.0,
"crashes": 2,
"unique_crashes": 2,
"corpus_size": 45,
"coverage": 78.5,
"elapsed_time": 120
```

#### Example: Scanning Workflow Stats

```python
"stats_type": "scan_progress",
"files_scanned": 150,
"vulnerabilities_found": 8,
"scan_percentage": 65.2,
"current_file": "/path/to/file.js",
"elapsed_time": 45
```

#### Example: Analysis Workflow Stats

```python
"stats_type": "analysis_update",
"functions_analyzed": 89,
"issues_found": 12,
"complexity_score": 7.8,
"current_module": "authentication",
"elapsed_time": 30
```

### 5. API Integration

Live statistics automatically appear in:

- **REST API**: `GET /fuzzing/{run_id}/stats` (for fuzzing workflows)
- **WebSocket**: Real-time updates via WebSocket connections
- **Server-Sent Events**: Live streaming at `/fuzzing/{run_id}/stream`

### 6. Best Practices

1. **Update Frequency**: Send statistics every 5-10 seconds for optimal performance.
2. **Error Handling**: Always wrap stats callbacks in try-catch blocks.
3. **Meaningful Data**: Include workflow-specific metrics that users care about.
4. **Consistent Naming**: Use consistent field names across similar workflow types.
5. **Backwards Compatibility**: Keep existing stats types when updating workflows.

#### Example: Adding Stats to a Security Scanner

```python
async def security_scan_task(workspace: Path, config: Dict[str, Any]):
    context = get_run_context()
    run_id = str(context.flow_run.id)

    async def stats_callback(stats_data):
        logger.info("LIVE_STATS", extra={
            "stats_type": "scan_progress",
            "workflow_type": "security_scan",
            "run_id": stats_data.get("run_id"),
            "files_scanned": stats_data.get("files_scanned", 0),
            "vulnerabilities_found": stats_data.get("vulnerabilities_found", 0),
            "scan_percentage": stats_data.get("scan_percentage", 0.0),
            "current_file": stats_data.get("current_file", ""),
            "elapsed_time": stats_data.get("elapsed_time", 0)
        })

    scanner = SecurityScannerModule()
    return await scanner.execute(config, workspace, stats_callback=stats_callback)
```

With these steps, your workflow will provide rich, real-time feedback to users and the FuzzForge platform—making automation more transparent and interactive!

---

## Step 4: Implement the Workflow Logic

Create a `workflow.py` file. This is where you define your Prefect flow and tasks.

Example (simplified):

```python
from pathlib import Path
from typing import Dict, Any
from prefect import flow, task
from src.toolbox.modules.dependency_scanner import DependencyScanner
from src.toolbox.modules.vulnerability_analyzer import VulnerabilityAnalyzer
from src.toolbox.modules.reporter import SARIFReporter

@task
async def scan_dependencies(workspace: Path, config: Dict[str, Any]) -> Dict[str, Any]:
    scanner = DependencyScanner()
    return (await scanner.execute(config, workspace)).dict()

@task
async def analyze_vulnerabilities(dependencies: Dict[str, Any], workspace: Path, config: Dict[str, Any]) -> Dict[str, Any]:
    analyzer = VulnerabilityAnalyzer()
    analyzer_config = {**config, 'dependencies': dependencies.get('findings', [])}
    return (await analyzer.execute(analyzer_config, workspace)).dict()

@task
async def generate_report(dep_results: Dict[str, Any], vuln_results: Dict[str, Any], config: Dict[str, Any], workspace: Path) -> Dict[str, Any]:
    reporter = SARIFReporter()
    all_findings = dep_results.get("findings", []) + vuln_results.get("findings", [])
    reporter_config = {**config, "findings": all_findings}
    return (await reporter.execute(reporter_config, workspace)).dict().get("sarif", {})

@flow(name="dependency_analysis")
async def main_flow(
    target_path: str = "/workspace",
    scan_dev_dependencies: bool = True,
    vulnerability_threshold: str = "medium"
) -> Dict[str, Any]:
    workspace = Path(target_path)
    scanner_config = {"scan_dev_dependencies": scan_dev_dependencies}
    analyzer_config = {"vulnerability_threshold": vulnerability_threshold}
    reporter_config = {}

    dep_results = await scan_dependencies(workspace, scanner_config)
    vuln_results = await analyze_vulnerabilities(dep_results, workspace, analyzer_config)
    sarif_report = await generate_report(dep_results, vuln_results, reporter_config, workspace)
    return sarif_report
```

---

## Step 5: Create the Dockerfile

Your workflow runs in a container. Create a `Dockerfile`:

```dockerfile
FROM python:3.11-slim
WORKDIR /app
RUN apt-get update && apt-get install -y git curl && rm -rf /var/lib/apt/lists/*
COPY ../../../pyproject.toml ./
COPY ../../../uv.lock ./
RUN pip install uv && uv sync --no-dev
COPY requirements.txt ./
RUN uv pip install -r requirements.txt
COPY ../../../ .
RUN mkdir -p /workspace
CMD ["uv", "run", "python", "-m", "src.toolbox.workflows.dependency_analysis.workflow"]
```

---

## Step 6: Register and Test Your Workflow

- Add your workflow to the registry (e.g., `backend/toolbox/workflows/registry.py`)
- Write a test script or use the CLI to submit a workflow run
- Check that SARIF results are produced and stored as expected

Example test:

```python
import asyncio
from backend.src.toolbox.workflows.dependency_analysis.workflow import main_flow

async def test_workflow():
    result = await main_flow(target_path="/tmp/test-project", scan_dev_dependencies=True)
    print(result)

if __name__ == "__main__":
    asyncio.run(test_workflow())
```

---

## Best Practices

- **Parameterize everything:** Use metadata.yaml to define all configurable options.
- **Validate inputs:** Check that paths, configs, and parameters are valid before running analysis.
- **Handle errors gracefully:** Catch exceptions in tasks and return partial results if possible.
- **Document your workflow:** Add docstrings and comments to explain each step.
- **Test with real and edge-case projects:** Ensure your workflow is robust and reliable.
