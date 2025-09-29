# Static Analysis Workflow Reference

The Static Analysis workflow in FuzzForge helps you find vulnerabilities, code quality issues, and compliance problems—before they reach production. This workflow uses multiple Static Application Security Testing (SAST) tools to analyze your source code without executing it, providing fast, actionable feedback in a standardized format.

---

## What Does This Workflow Do?

- **Workflow ID:** `static_analysis_scan`
- **Primary Tools:** Semgrep (multi-language), Bandit (Python)
- **Supported Languages:** Python, JavaScript, Java, Go, C/C++, PHP, Ruby, and more
- **Typical Duration:** 1–5 minutes (varies by codebase size)
- **Output Format:** SARIF 2.1.0 (industry standard)

---

## How Does It Work?

The workflow orchestrates multiple SAST tools in a containerized environment:

- **Semgrep:** Pattern-based static analysis for 30+ languages, with rule sets for OWASP Top 10, CWE Top 25, and more.
- **Bandit:** Python-specific security scanner, focused on issues like hardcoded secrets, injection, and unsafe code patterns.

Each tool runs independently, and their findings are merged and normalized into a single SARIF report.

---

## How to Use the Static Analysis Workflow

### Basic Usage

**CLI:**
```bash
fuzzforge runs submit static_analysis_scan /path/to/your/project
```

**API:**
```bash
curl -X POST "http://localhost:8000/workflows/static_analysis_scan/submit" \
     -H "Content-Type: application/json" \
     -d '{"target_path": "/path/to/your/project"}'
```

### Advanced Configuration

You can fine-tune the workflow by passing parameters for each tool:

**CLI:**
```bash
fuzzforge runs submit static_analysis_scan /path/to/project \
    --parameters '{
      "semgrep_config": {
        "rules": ["p/security-audit", "owasp-top-ten"],
        "severity": ["ERROR", "WARNING"],
        "exclude_patterns": ["test/*", "vendor/*", "node_modules/*"]
      },
      "bandit_config": {
        "confidence": "MEDIUM",
        "severity": "MEDIUM",
        "exclude_dirs": ["tests", "migrations"]
      }
    }'
```

**API:**
```json
{
  "target_path": "/path/to/project",
  "parameters": {
    "semgrep_config": {
      "rules": ["p/security-audit"],
      "languages": ["python", "javascript"],
      "severity": ["ERROR", "WARNING"],
      "exclude_patterns": ["*.test.js", "test_*.py", "vendor/*"]
    },
    "bandit_config": {
      "confidence": "MEDIUM",
      "severity": "LOW",
      "tests": ["B201", "B301"],
      "exclude_dirs": ["tests", ".git"]
    }
  }
}
```

---

## Configuration Reference

### Semgrep Parameters

| Parameter         | Type      | Default                       | Description                                 |
|-------------------|-----------|-------------------------------|---------------------------------------------|
| `rules`           | array     | `"auto"`                      | Rule sets to use (e.g., `"p/security-audit"`)|
| `languages`       | array     | `null`                        | Languages to analyze                        |
| `severity`        | array     | `["ERROR", "WARNING", "INFO"]`| Severities to include                       |
| `exclude_patterns`| array     | `[]`                          | File patterns to exclude                    |
| `include_patterns`| array     | `[]`                          | File patterns to include                    |
| `max_target_bytes`| integer   | `1000000`                     | Max file size to analyze (bytes)            |
| `timeout`         | integer   | `300`                         | Tool timeout (seconds)                      |

### Bandit Parameters

| Parameter         | Type      | Default                       | Description                                 |
|-------------------|-----------|-------------------------------|---------------------------------------------|
| `confidence`      | string    | `"LOW"`                       | Minimum confidence (`"LOW"`, `"MEDIUM"`, `"HIGH"`) |
| `severity`        | string    | `"LOW"`                       | Minimum severity (`"LOW"`, `"MEDIUM"`, `"HIGH"`)   |
| `tests`           | array     | `null`                        | Specific test IDs to run                    |
| `exclude_dirs`    | array     | `["tests", ".git"]`           | Directories to exclude                      |
| `aggregate`       | string    | `"file"`                      | Aggregation mode (`"file"`, `"vuln"`)       |
| `context_lines`   | integer   | `3`                           | Context lines around findings               |

---

## What Can It Detect?

### Vulnerability Categories

- **OWASP Top 10:** Broken Access Control, Injection, Security Misconfiguration, etc.
- **CWE Top 25:** SQL Injection, XSS, Command Injection, Information Exposure, etc.
- **Language-Specific:** Python (unsafe eval, Django/Flask issues), JavaScript (XSS, prototype pollution), Java (deserialization), Go (race conditions), C/C++ (buffer overflows).

### Example Detections

**SQL Injection (Python)**
```python
query = f"SELECT * FROM users WHERE id = {user_id}"  # CWE-89
```
*Recommendation: Use parameterized queries.*

**Command Injection (Python)**
```python
os.system(f"cp {filename} backup/")  # CWE-78
```
*Recommendation: Use subprocess with argument arrays.*

**XSS (JavaScript)**
```javascript
element.innerHTML = userInput;  // CWE-79
```
*Recommendation: Use textContent or sanitize input.*

---

## Output Format

All results are returned in SARIF 2.1.0 format, which is supported by many IDEs and security tools.

**Summary Example:**
```json
{
  "workflow": "static_analysis_scan",
  "status": "completed",
  "total_findings": 18,
  "severity_counts": {
    "critical": 0,
    "high": 6,
    "medium": 5,
    "low": 7
  },
  "tool_counts": {
    "semgrep": 12,
    "bandit": 6
  }
}
```

**Finding Example:**
```json
{
  "ruleId": "bandit.B608",
  "level": "error",
  "message": {
    "text": "Possible SQL injection vector through string-based query construction"
  },
  "locations": [
    {
      "physicalLocation": {
        "artifactLocation": {
          "uri": "src/database.py"
        },
        "region": {
          "startLine": 42,
          "startColumn": 15,
          "endLine": 42,
          "endColumn": 65
        }
      }
    }
  ],
  "properties": {
    "severity": "high",
    "category": "sql_injection",
    "cwe": "CWE-89",
    "confidence": "high",
    "tool": "bandit"
  }
}
```

---

## Performance Tips

- For large codebases, increase `max_target_bytes` and `timeout` as needed.
- Exclude large generated or dependency directories (`vendor/`, `node_modules/`, `dist/`).
- Run focused scans on changed files for faster CI/CD feedback.

---

## Integration Examples

### GitHub Actions

```yaml
- name: Run Static Analysis
  run: |
    curl -X POST "${{ secrets.FUZZFORGE_URL }}/workflows/static_analysis_scan/submit" \
         -H "Content-Type: application/json" \
         -d '{
           "target_path": "${{ github.workspace }}",
           "parameters": {
             "semgrep_config": {"severity": ["ERROR", "WARNING"]},
             "bandit_config": {"confidence": "MEDIUM"}
           }
         }'
```

### Pre-commit Hook

```bash
fuzzforge runs submit static_analysis_scan . --wait --json > /tmp/analysis.json
HIGH_ISSUES=$(jq '.sarif.severity_counts.high // 0' /tmp/analysis.json)
if [ "$HIGH_ISSUES" -gt 0 ]; then
    echo "❌ Found $HIGH_ISSUES high-severity security issues. Commit blocked."
    exit 1
fi
```

---

## Best Practices

- **Target the right code:** Focus on your main source directories, not dependencies or build artifacts.
- **Start broad, then refine:** Use default rule sets first, then add exclusions or custom rules as needed.
- **Triage findings:** Address high-severity issues first, and document false positives for future runs.
- **Monitor trends:** Track your security posture over time to measure improvement.
- **Optimize for speed:** Use file size limits and timeouts for very large projects.

---

## Troubleshooting

- **No Python files found:** Bandit will report zero findings if your project isn’t Python, this is normal.
- **High memory usage:** Exclude large files and directories, or increase Docker memory limits.
- **Slow scans:** Use inclusion/exclusion patterns and increase timeouts for big repos.
- **Workflow errors:** See the [Troubleshooting Guide](/how-to/troubleshooting.md) for help with registry, Docker, or workflow issues.
