# FuzzForge Vulnerable Test Project

This directory contains a comprehensive vulnerable test application designed to validate FuzzForge's security workflows. The project contains multiple categories of security vulnerabilities to test both the `security_assessment` and `secret_detection_scan` workflows.

## Test Project Overview

### Vulnerable Application (`vulnerable_app/`)
**Purpose**: Comprehensive vulnerable application for testing security workflows

**Supported Workflows**:
- `security_assessment` - General security scanning and analysis
- `secret_detection_scan` - Detection of secrets, credentials, and sensitive data

**Vulnerabilities Included**:
- SQL injection vulnerabilities
- Command injection
- Hardcoded secrets and credentials
- Path traversal vulnerabilities
- Weak cryptographic functions
- Server-side template injection (SSTI)
- Pickle deserialization attacks
- CSRF missing protection
- Information disclosure
- API keys and tokens
- Database connection strings
- Private keys and certificates

**Files**:
- Multiple source code files with various vulnerability types
- Configuration files with embedded secrets
- Dependencies with known vulnerabilities

**Expected Detections**: 30+ findings across both security assessment and secret detection workflows

---

## Usage Instructions

### Testing with FuzzForge Workflows

The vulnerable application can be tested with both essential workflows:

```bash
# Test security assessment workflow
curl -X POST http://localhost:8000/workflows/security_assessment/submit \
  -H "Content-Type: application/json" \
  -d '{
    "target_path": "/path/to/test_projects/vulnerable_app",
    "volume_mode": "ro"
  }'

# Test secret detection workflow
curl -X POST http://localhost:8000/workflows/secret_detection_scan/submit \
  -H "Content-Type: application/json" \
  -d '{
    "target_path": "/path/to/test_projects/vulnerable_app",
    "volume_mode": "ro"
  }'
```

### Expected Results

Each workflow should produce SARIF-formatted results with:
- High-severity findings for critical vulnerabilities
- Medium-severity findings for moderate risks
- Detailed descriptions and remediation guidance
- Code flow information where applicable

### Validation Criteria

A successful test should detect:
- **Security Assessment**: At least 20 various security vulnerabilities
- **Secret Detection**: At least 10 different types of secrets and credentials

---

## Security Notice

⚠️ **WARNING**: This project contains intentional security vulnerabilities and should NEVER be deployed in production environments or exposed to public networks. It is designed solely for security testing and validation purposes.

## File Structure
```
test_projects/
├── README.md
└── vulnerable_app/
    ├── [Multiple vulnerable source files]
    ├── [Configuration files with secrets]
    └── [Dependencies with known issues]
```