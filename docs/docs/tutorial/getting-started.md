# Getting Started

This tutorial will guide you through setting up FuzzForge and running your first security workflow. By the end of this tutorial, you'll have FuzzForge running locally and will have executed a complete static analysis workflow on a test project.

## Prerequisites

Before we begin, ensure you have the following installed:

- **Docker Desktop** or **Docker Engine** (20.10.0 or later)
- **Docker Compose** (2.0.0 or later)
- **Python 3.11+** (for CLI installation)
- **Git** (for cloning the repository)
- **4GB+ RAM** available for workflow execution

## Step 1: Clone and Setup FuzzForge

First, let's clone the FuzzForge repository:

```bash
git clone https://github.com/FuzzingLabs/fuzzforge.git
cd fuzzforge
```

Create the required environment configuration:

```bash
echo "COMPOSE_PROJECT_NAME=fuzzforge" > .env
```

## Step 2: Configure Docker (Critical Step)

**⚠️ This step is mandatory - workflows will fail without proper Docker configuration.**

FuzzForge uses a local Docker registry at `localhost:5001` for workflow images. You must configure Docker to allow this insecure registry.

### For Docker Desktop (macOS/Windows):

1. Open Docker Desktop
2. Go to Settings/Preferences → Docker Engine
3. Add the following to the JSON configuration:

```json
{
  "builder": {
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false,
  "insecure-registries": [
    "localhost:5001"
  ]
}
```

4. Click "Apply & Restart"

### For Docker Engine (Linux):

Edit `/etc/docker/daemon.json` (create if it doesn't exist):

```json
{
  "insecure-registries": ["localhost:5001"]
}
```

Restart Docker:
```bash
sudo systemctl restart docker
```

### Verify Docker Configuration

Test that Docker can access the insecure registry:

```bash
# After starting FuzzForge (next step), this should work without certificate errors
docker pull localhost:5001/hello-world 2>/dev/null || echo "Registry not accessible yet (normal before startup)"
```

## Step 3: Start FuzzForge

Start all FuzzForge services:

```bash
docker compose up -d
```

This will start 8 services:
- **prefect-server**: Workflow orchestration server
- **prefect-worker**: Executes workflows in Docker containers
- **fuzzforge-backend**: FastAPI backend and workflow management
- **postgres**: Metadata and workflow state storage
- **redis**: Message broker and caching
- **registry**: Local Docker registry for workflow images
- **docker-proxy**: Secure Docker socket proxy
- **prefect-services**: Additional Prefect services

Wait for all services to be healthy (this may take 2-3 minutes on first startup):

```bash
# Check service health
docker compose ps

# Verify FuzzForge is ready
curl http://localhost:8000/health
# Should return: {"status":"healthy"}
```

## Step 4: Install the CLI (Optional but Recommended)

Install the FuzzForge CLI for easier workflow management:

```bash
cd cli
pip install -e .
# or with uv:
uv tool install .
```

Verify CLI installation:

```bash
fuzzforge --version
```

Configure the CLI to connect to your local instance:

```bash
fuzzforge config set-server http://localhost:8000
```

## Step 5: Verify Available Workflows

List all available workflows to confirm the system is working:

```bash
# Using CLI
fuzzforge workflows list

# Using API
curl http://localhost:8000/workflows | jq .
```

You should see 6 production workflows:
- `static_analysis_scan`
- `secret_detection_scan`
- `infrastructure_scan`
- `penetration_testing_scan`
- `language_fuzzing`
- `security_assessment`

## Step 6: Run Your First Workflow

Let's run a static analysis workflow on one of the included vulnerable test projects.

### Using the CLI (Recommended):

```bash
# Navigate to a test project
cd /path/to/fuzzforge/test_projects/static_analysis_vulnerable

# Submit the workflow
fuzzforge runs submit static_analysis_scan .

# Monitor the workflow
fuzzforge runs status <run-id>

# View results when complete
fuzzforge findings get <run-id>
```

### Using the API:

```bash
# Submit workflow
curl -X POST "http://localhost:8000/workflows/static_analysis_scan/submit" \
     -H "Content-Type: application/json" \
     -d '{
       "target_path": "/path/to/your/project"
     }'

# Check status
curl "http://localhost:8000/runs/{run-id}/status"

# Get findings
curl "http://localhost:8000/runs/{run-id}/findings"
```

## Step 7: Understanding the Results

The workflow will complete in 30-60 seconds and return results in SARIF format. For the test project, you should see:

- **Total findings**: 15-20 security issues
- **Tools used**: OpenGrep (Semgrep) and Bandit
- **Severity levels**: High, Medium, and Low vulnerabilities
- **Categories**: SQL injection, command injection, hardcoded secrets, etc.

Example output:
```json
{
  "workflow": "static_analysis_scan",
  "status": "completed",
  "total_findings": 18,
  "scan_status": {
    "opengrep": "success",
    "bandit": "success"
  },
  "severity_counts": {
    "high": 6,
    "medium": 5,
    "low": 7
  }
}
```

## Step 8: Access the Prefect Dashboard

You can monitor workflow execution in real-time using the Prefect dashboard:

1. Open http://localhost:4200 in your browser
2. Navigate to "Flow Runs" to see workflow executions
3. Click on a run to see detailed logs and execution graph

## Next Steps

Congratulations! You've successfully:
- ✅ Installed and configured FuzzForge
- ✅ Configured Docker with the required insecure registry
- ✅ Started all FuzzForge services
- ✅ Run your first security workflow
- ✅ Retrieved and understood workflow results

### What to explore next:

1. **[Create Workflow Guide](../how-to/create-workflow.md)** - Create your own workflows !

### Common Issues:

If you encounter problems:

1. **Workflow crashes with registry errors**: Check Docker insecure registry configuration
2. **Services won't start**: Ensure ports 4200, 5001, 8000 are available
3. **No findings returned**: Verify the target path contains analyzable code files
4. **CLI not found**: Ensure Python/pip installation path is in your PATH

See the [Troubleshooting Guide](../how-to/troubleshooting.md) for detailed solutions.

---

**🎉 You're now ready to use FuzzForge for security analysis!**

The next tutorial will teach you about the different types of workflows and when to use each one.
