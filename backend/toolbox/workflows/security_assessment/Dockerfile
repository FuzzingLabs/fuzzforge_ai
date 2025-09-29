FROM prefecthq/prefect:3-python3.11

WORKDIR /app

# Create toolbox directory structure to match expected import paths
RUN mkdir -p /app/toolbox/workflows /app/toolbox/modules

# Copy base module infrastructure
COPY modules/__init__.py /app/toolbox/modules/
COPY modules/base.py /app/toolbox/modules/

# Copy only required modules (manual selection)
COPY modules/scanner /app/toolbox/modules/scanner
COPY modules/analyzer /app/toolbox/modules/analyzer
COPY modules/reporter /app/toolbox/modules/reporter

# Copy this workflow
COPY workflows/security_assessment /app/toolbox/workflows/security_assessment

# Install workflow-specific requirements if they exist
RUN if [ -f /app/toolbox/workflows/security_assessment/requirements.txt ]; then pip install --no-cache-dir -r /app/toolbox/workflows/security_assessment/requirements.txt; fi

# Install common requirements
RUN pip install --no-cache-dir pyyaml

# Set Python path
ENV PYTHONPATH=/app:$PYTHONPATH

# Create workspace directory
RUN mkdir -p /workspace
