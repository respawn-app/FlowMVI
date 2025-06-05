# Arch Linux base image
FROM archlinux:base

# Set non-interactive frontend for package installations
ENV DEBIAN_FRONTEND=noninteractive

# Update system and install core dependencies, build tools, and your specified packages
# Using --needed to avoid reinstalling packages that are already present
RUN pacman -Syu --noconfirm && \
    pacman -S --noconfirm --needed \
    base-devel \
    git \
    sudo \
    openjdk-21-jdk \
    libxcrypt-compat \
    wget \
    curl \
    ca-certificates \
    gnupg \
    jq \
    less \
    lsof \
    unzip \
    yarn \
    zip \
    nodejs \
    python

# Create a non-root user (e.g., vscode for devcontainers)
# Add user to wheel group for sudo privileges
# Configure passwordless sudo for this user
RUN useradd --create-home --gid users --groups wheel --shell /bin/bash vscode && \
    echo 'vscode ALL=(ALL) NOPASSWD: ALL' > /etc/sudoers.d/vscode

# Set Android SDK environment variables
ENV ANDROID_HOME /opt/android-sdk
ENV ANDROID_SDK_ROOT /opt/android-sdk
# Ensure cmdline-tools, platform-tools, and build-tools are in PATH
# The exact paths might vary slightly based on AUR package specifics,
# but AUR packages usually symlink tools to /usr/bin or the SDK structure is standard.
ENV PATH $PATH:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/emulator

# Switch to the non-root user for installing AUR helper and AUR packages
USER vscode
WORKDIR /home/vscode

# Install yay (AUR helper)
# makepkg must not be run as root
RUN git clone https://aur.archlinux.org/yay.git /tmp/yay && \
    cd /tmp/yay && \
    makepkg -si --noconfirm && \
    cd /home/vscode && \
    rm -rf /tmp/yay

# Update AUR packages database and install specified Android SDK components using yay
# This is where your AUR-based dependencies are installed
RUN yay -Syu --noconfirm && \
    yay -S --noconfirm \
    android-sdk \
    android-sdk-platform-tools \
    android-sdk-build-tools \
    android-sdk-cmdline-tools-latest

# Attempt to automatically accept Android SDK licenses
# This needs to run after the SDK tools are installed and PATH is effective.
# Ensure sdkmanager is executable and in PATH. The AUR package should handle this.
# This command might need to be run as root if it writes to $ANDROID_HOME owned by root,
# or $ANDROID_HOME permissions need to be adjusted.
# For now, running as vscode, assuming $ANDROID_HOME is writable by the user or sdkmanager handles it.
# If $ANDROID_HOME is /opt/android-sdk (common for AUR), it's typically root-owned.
# The `android-sdk` AUR package might set permissions, or this step might need adjustment.
# A common practice is to chown $ANDROID_HOME to the user.
USER root
RUN if [ -d "$ANDROID_HOME" ]; then chown -R vscode:users "$ANDROID_HOME"; fi && \
    echo "Android SDK Home: $ANDROID_HOME" && \
    ls -la "$ANDROID_HOME" || echo "ANDROID_HOME not found or not created yet."

USER vscode
RUN if [ -x "$(command -v sdkmanager)" ]; then \
        echo "Attempting to accept licenses..." && \
        yes | sdkmanager --licenses && \
        sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" "cmdline-tools;latest"; \
    else \
        echo "sdkmanager not found, skipping license acceptance and component installation." && \
        echo "Ensure android-sdk-cmdline-tools-latest is correctly installed and in PATH."; \
    fi


# Switch back to the non-root user for the devcontainer environment
USER vscode
WORKDIR /home/vscode

# Default command (optional, can be overridden by devcontainer.json)
CMD ["/bin/bash"]
