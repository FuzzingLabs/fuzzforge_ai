FROM prefecthq/prefect:3-python3.11

WORKDIR /app

# Install system dependencies for MobSF and Jadx
RUN apt-get update && apt-get install -y \
    git \
    default-jdk \
    wget \
    unzip \
    xfonts-75dpi \
    xfonts-base \
    && rm -rf /var/lib/apt/lists/* \
    && wget https://github.com/wkhtmltopdf/packaging/releases/download/0.12.6.1-3/wkhtmltox_0.12.6.1-3.bookworm_amd64.deb \
    && apt-get update \
    && apt-get install -y ./wkhtmltox_0.12.6.1-3.bookworm_amd64.deb \
    && rm wkhtmltox_0.12.6.1-3.bookworm_amd64.deb \
    && rm -rf /var/lib/apt/lists/*

# Install Jadx
RUN wget https://github.com/skylot/jadx/releases/download/v1.5.0/jadx-1.5.0.zip -O /tmp/jadx.zip \
    && unzip /tmp/jadx.zip -d /opt/jadx \
    && rm /tmp/jadx.zip \
    && ln -s /opt/jadx/bin/jadx /usr/local/bin/jadx

# The upstream OpenGrep CLI is not yet published on PyPI. Use semgrep (the
# engine that OpenGrep builds upon) and expose it under the `opengrep` name so
# the workflow module can invoke it transparently.
RUN pip install --no-cache-dir semgrep==1.45.0 \
    && ln -sf /usr/local/bin/semgrep /usr/local/bin/opengrep

# Clone and setup MobSF
RUN git clone https://github.com/MobSF/Mobile-Security-Framework-MobSF.git /app/mobsf \
    && cd /app/mobsf \
    && git checkout v3.9.7 \
    && ./setup.sh

# Force rebuild after this point
ARG CACHEBUST=2

# Copy the entire toolbox directory structure
COPY . /app/toolbox

# Copy Android custom rules to a well-known location
COPY ./modules/android/custom_rules /app/custom_opengrep_rules

ENV PYTHONPATH=/app/toolbox:$PYTHONPATH
ENV MOBSF_PORT=8877

# Create startup script to launch MobSF in background and then Prefect
RUN echo '#!/bin/bash\n\
cd /app/mobsf && ./run.sh 127.0.0.1:8877 &\n\
echo "Waiting for MobSF to start..."\n\
sleep 10\n\
echo "Starting Prefect engine..."\n\
exec python -m prefect.engine\n\
' > /app/start.sh && chmod +x /app/start.sh

CMD ["/app/start.sh"]
