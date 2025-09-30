# Secret Detection Workflow Dockerfile
FROM prefecthq/prefect:3-python3.11

# Install system dependencies
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    git \
    ca-certificates \
    gnupg \
    && rm -rf /var/lib/apt/lists/*

# Install TruffleHog (use direct binary download to avoid install script issues)
RUN curl -sSfL "https://github.com/trufflesecurity/trufflehog/releases/download/v3.63.2/trufflehog_3.63.2_linux_amd64.tar.gz" -o trufflehog.tar.gz \
    && tar -xzf trufflehog.tar.gz \
    && mv trufflehog /usr/local/bin/ \
    && rm trufflehog.tar.gz

# Install Gitleaks (use specific version to avoid API rate limiting)
RUN wget https://github.com/gitleaks/gitleaks/releases/download/v8.18.2/gitleaks_8.18.2_linux_x64.tar.gz \
    && tar -xzf gitleaks_8.18.2_linux_x64.tar.gz \
    && mv gitleaks /usr/local/bin/ \
    && rm gitleaks_8.18.2_linux_x64.tar.gz

# Verify installations
RUN trufflehog --version && gitleaks version

# Set working directory
WORKDIR /opt/prefect

# Create toolbox directory structure
RUN mkdir -p /opt/prefect/toolbox

# Set environment variables
ENV PYTHONPATH=/opt/prefect/toolbox:/opt/prefect/toolbox/workflows
ENV WORKFLOW_NAME=secret_detection_scan

# The toolbox code will be mounted at runtime from the backend container
# This includes:
# - /opt/prefect/toolbox/modules/base.py
# - /opt/prefect/toolbox/modules/secret_detection/ (TruffleHog, Gitleaks modules)
# - /opt/prefect/toolbox/modules/reporter/ (SARIF reporter)
# - /opt/prefect/toolbox/workflows/comprehensive/secret_detection_scan/
VOLUME /opt/prefect/toolbox

# Set working directory for execution
WORKDIR /opt/prefect