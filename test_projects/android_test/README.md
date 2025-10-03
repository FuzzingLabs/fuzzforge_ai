# android_test

FuzzForge security testing project.

## Quick Start

```bash
# List available workflows
fuzzforge workflows

# Submit a workflow for analysis
fuzzforge workflow <workflow-name> /path/to/target

# Monitor run progress
fuzzforge monitor live <run-id>

# View findings
fuzzforge finding <run-id>
```

## Project Structure

- `.fuzzforge/` - Project data and configuration
- `.fuzzforge/config.yaml` - Project configuration
- `.fuzzforge/findings.db` - Local database for runs and findings
