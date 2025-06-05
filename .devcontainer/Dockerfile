FROM mcr.microsoft.com/devcontainers/base:debian-12

ENV DEBIAN_FRONTEND=noninteractive

ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=${ANDROID_HOME}
ENV JAVA_HOME=/usr/lib/jvm/zulu-23-amd64
ENV PATH="${JAVA_HOME}/bin:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/emulator:${PATH}"
ENV CMDLINE_TOOLS_VERSION="13114758"

RUN sudo apt-get update && sudo apt-get install -y --no-install-recommends \
    git \
    wget \
    curl \
    ca-certificates \
    gnupg \
    software-properties-common \
    apt-transport-https \
    jq \
    less \
    lsof \
    sudo \
    unzip \
    zip \
    nodejs \
    npm \
    python3 \
    python3-pip \
    libxtst6 \
    && sudo rm -rf /var/lib/apt/lists/*

RUN sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 0xB1998361219BD9C9 && \
    sudo curl -fsSL -O https://cdn.azul.com/zulu/bin/zulu-repo_1.0.0-3_all.deb && \
    sudo apt-get install -y ./zulu-repo_1.0.0-3_all.deb && \
    sudo rm ./zulu-repo_1.0.0-3_all.deb && \
    sudo apt-get update && \
    sudo apt-get install -y --no-install-recommends zulu23-jdk && \
    sudo rm -rf /var/lib/apt/lists/* # Clean up apt lists again after adding repo and installing

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

# Default command can be overridden by devcontainer.json or docker-compose.yml
# CMD ["/bin/bash"]
