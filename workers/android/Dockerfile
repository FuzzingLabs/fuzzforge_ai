# FuzzForge Vertical Worker: Android Security
#
# Pre-installed tools for Android security analysis:
# - Android SDK (adb, aapt)
# - apktool (APK decompilation)
# - jadx (Dex to Java decompiler)
# - Frida (dynamic instrumentation)
# - androguard (Python APK analysis)
# - MobSF dependencies

FROM python:3.11-slim-bookworm

# Set working directory
WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    # Build essentials
    build-essential \
    git \
    curl \
    wget \
    unzip \
    # Java (required for Android tools)
    openjdk-17-jdk \
    # Android tools dependencies
    lib32stdc++6 \
    lib32z1 \
    # Frida dependencies
    libc6-dev \
    # XML/Binary analysis
    libxml2-dev \
    libxslt-dev \
    # Network tools
    netcat-openbsd \
    tcpdump \
    # Cleanup
    && rm -rf /var/lib/apt/lists/*

# Install Android SDK Command Line Tools
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}"

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    cd ${ANDROID_HOME}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip && \
    unzip -q commandlinetools-linux-9477386_latest.zip && \
    mv cmdline-tools latest && \
    rm commandlinetools-linux-9477386_latest.zip && \
    # Accept licenses
    yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --licenses && \
    # Install platform tools (adb, fastboot)
    ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager "platform-tools" "build-tools;33.0.0"

# Install apktool
RUN wget -q https://raw.githubusercontent.com/iBotPeaches/Apktool/master/scripts/linux/apktool -O /usr/local/bin/apktool && \
    wget -q https://bitbucket.org/iBotPeaches/apktool/downloads/apktool_2.9.3.jar -O /usr/local/bin/apktool.jar && \
    chmod +x /usr/local/bin/apktool

# Install jadx (Dex to Java decompiler)
RUN wget -q https://github.com/skylot/jadx/releases/download/v1.4.7/jadx-1.4.7.zip -O /tmp/jadx.zip && \
    unzip -q /tmp/jadx.zip -d /opt/jadx && \
    ln -s /opt/jadx/bin/jadx /usr/local/bin/jadx && \
    ln -s /opt/jadx/bin/jadx-gui /usr/local/bin/jadx-gui && \
    rm /tmp/jadx.zip

# Install Python dependencies for Android security tools
COPY requirements.txt /tmp/requirements.txt
RUN pip3 install --no-cache-dir -r /tmp/requirements.txt && \
    rm /tmp/requirements.txt

# Install androguard (Python APK analysis framework)
RUN pip3 install --no-cache-dir androguard pyaxmlparser

# Install Frida
RUN pip3 install --no-cache-dir frida-tools frida

# Create cache directory
RUN mkdir -p /cache && chmod 755 /cache

# Copy worker entrypoint (generic, works for all verticals)
COPY worker.py /app/worker.py

# Add toolbox to Python path (mounted at runtime)
ENV PYTHONPATH="/app:/app/toolbox:${PYTHONPATH}"
ENV PYTHONUNBUFFERED=1
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD python3 -c "import sys; sys.exit(0)"

# Run worker
CMD ["python3", "/app/worker.py"]
