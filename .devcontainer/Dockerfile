FROM mcr.microsoft.com/devcontainers/base:debian-12

ENV DEBIAN_FRONTEND=noninteractive

ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=${ANDROID_HOME}
ENV JAVA_HOME=/usr/lib/jvm/java-24-openjdk-amd64
ENV PATH="${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/emulator:${JAVA_HOME}/bin"
ENV CMDLINE_TOOLS_VERSION="13114758"

RUN sudo apt-get update && sudo apt-get install -y --no-install-recommends \
    git \
    openjdk-24-jdk \
    wget \
    curl \
    ca-certificates \
    gnupg \
    jq \
    less \
    lsof \
    unzip \
    zip \
    nodejs \
    npm \
    python3 \
    python3-pip \
    libxtst6 \
    && sudo rm -rf /var/lib/apt/lists/*

# Install Yarn using corepack (recommended way with modern Node.js)
RUN sudo corepack enable && sudo corepack prepare yarn@stable --activate

RUN sudo mkdir -p "${ANDROID_HOME}/cmdline-tools" && \
    sudo wget -q "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip" -O /tmp/cmdline-tools.zip && \
    sudo unzip -q /tmp/cmdline-tools.zip -d /tmp/android_cmdline_tools_temp && \
    # The unzipped folder is 'cmdline-tools', move its contents to 'latest'
    sudo mv /tmp/android_cmdline_tools_temp/cmdline-tools "${ANDROID_HOME}/cmdline-tools/latest" && \
    sudo rm -rf /tmp/cmdline-tools.zip /tmp/android_cmdline_tools_temp

# Accept Android SDK licenses and install required components
# Note: The user running sdkmanager needs write access to ANDROID_HOME
# The base image often runs as 'vscode' or 'node' user who has passwordless sudo.
# We ensure ANDROID_HOME is owned by the container user after creation if not already.
RUN sudo mkdir -p "${ANDROID_HOME}" && \
    sudo chown -R "$(whoami)":"$(whoami)" "${ANDROID_HOME}" && \
    yes | sdkmanager --licenses --sdk_root="${ANDROID_HOME}" && \
    sdkmanager --sdk_root="${ANDROID_HOME}" "platform-tools" "platforms;android-34" "build-tools;34.0.0" "cmdline-tools;latest" && \
    echo "Android SDK setup complete."
