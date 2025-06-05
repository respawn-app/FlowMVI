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
RUN yay -Syu --noconfirm && \
    yay -S --noconfirm \
    zulu-24-bin \
    android-sdk \
    android-sdk-platform-tools \
    android-sdk-build-tools \
    android-sdk-cmdline-tools-latest \

RUN if [ -x "$(command -v sdkmanager)" ]; then \
        echo "Running sdkmanager as root to accept licenses and install components..." && \
        yes | sdkmanager --licenses --sdk_root="$ANDROID_HOME" --verbose && \
        echo "Installing/updating SDK components as root..." && \
        sdkmanager --sdk_root="$ANDROID_HOME" --verbose "platform-tools" "platforms;android-36" "build-tools;36.0.0" "cmdline-tools;latest" && \
        echo "SDK components installation/update attempt finished as root."; \
    else \
        echo "CRITICAL ERROR: sdkmanager not found for root user. This indicates a problem with the android-sdk-cmdline-tools-latest AUR package installation or PATH." && \
        exit 1; \
    fi

ENV JAVA_HOME=/usr/lib/jvm/zulu-24-jdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

USER vscode
WORKDIR /home/vscode

RUN echo "Verifying Java installation for vscode user..." && \
 java -version && \
 echo "Java -version successful for vscode user." && \
 echo "Verifying sdkmanager access for vscode user..." && \
 sdkmanager --version && \
 echo "sdkmanager --version successful for vscode user." || \
 (echo "ERROR: Post-setup verification failed for vscode user." && exit 1)

CMD ["/bin/bash"]
