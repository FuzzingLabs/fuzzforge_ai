# Secret Detection Scan Workflow

This workflow performs comprehensive secret detection using multiple industry-standard tools:

- **TruffleHog**: Comprehensive secret detection with verification capabilities
- **Gitleaks**: Git-specific secret scanning and leak detection

## Features

- **Parallel Execution**: Runs TruffleHog and Gitleaks concurrently for faster results
- **Deduplication**: Automatically removes duplicate findings across tools
- **SARIF Output**: Generates standardized SARIF reports for integration with security tools
- **Configurable**: Supports extensive configuration for both tools

## Dependencies

### Required Modules
- `toolbox.modules.secret_detection.trufflehog`
- `toolbox.modules.secret_detection.gitleaks`
- `toolbox.modules.reporter` (SARIF reporter)
- `toolbox.modules.base` (Base module interface)

### External Tools
- TruffleHog v3.63.2+
- Gitleaks v8.18.0+

## Docker Deployment

This workflow provides two Docker deployment approaches:

### 1. Volume-Based Approach (Default: `Dockerfile`)

**Advantages:**
- Live code updates without rebuilding images
- Smaller image sizes
- Consistent module versions across workflows
- Faster development iteration

**How it works:**
- Docker image contains only external tools (TruffleHog, Gitleaks)
- Python modules are mounted at runtime from the backend container
- Backend manages code synchronization via shared volumes

### 2. Self-Contained Approach (`Dockerfile.self-contained`)

**Advantages:**
- Complete isolation and reproducibility
- No runtime dependencies on backend code
- Can run independently of FuzzForge platform
- Better for CI/CD integration

**How it works:**
- All required Python modules are copied into the Docker image
- Image is completely self-contained
- Larger image size but fully portable

## Configuration

### TruffleHog Configuration

```json
{
  "trufflehog_config": {
    "verify": true,                    // Verify discovered secrets
    "concurrency": 10,                 // Number of concurrent workers
    "max_depth": 10,                   // Maximum directory depth
    "include_detectors": [],           // Specific detectors to include
    "exclude_detectors": []            // Specific detectors to exclude
  }
}
```

### Gitleaks Configuration

```json
{
  "gitleaks_config": {
    "scan_mode": "detect",             // "detect" or "protect"
    "redact": true,                    // Redact secrets in output
    "max_target_megabytes": 100,       // Maximum file size (MB)
    "no_git": false,                   // Scan without Git context
    "config_file": "",                 // Custom Gitleaks config
    "baseline_file": ""                // Baseline file for known findings
  }
}
```

## Usage Example

```bash
curl -X POST "http://localhost:8000/workflows/secret_detection_scan/submit" \
  -H "Content-Type: application/json" \
  -d '{
    "target_path": "/path/to/scan",
    "volume_mode": "ro",
    "parameters": {
      "trufflehog_config": {
        "verify": true,
        "concurrency": 15
      },
      "gitleaks_config": {
        "scan_mode": "detect",
        "max_target_megabytes": 200
      }
    }
  }'
```

## Output Format

The workflow generates a SARIF report containing:
- All unique findings from both tools
- Severity levels mapped to standard scale
- File locations and line numbers
- Detailed descriptions and recommendations
- Tool-specific metadata

## Performance Considerations

- **TruffleHog**: CPU-intensive with verification enabled
- **Gitleaks**: Memory-intensive for large repositories
- **Recommended Resources**: 512Mi memory, 500m CPU
- **Typical Runtime**: 1-5 minutes for small repos, 10-30 minutes for large ones

## Security Notes

- Secrets are redacted in output by default
- Verified secrets are marked with higher severity
- Both tools support custom rules and exclusions
- Consider using baseline files for known false positives