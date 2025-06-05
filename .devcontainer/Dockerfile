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

USER root

# sdkmanager needs to write to $ANDROID_HOME (e.g., to create $ANDROID_HOME/licenses).
RUN if [ -x "$(command -v sdkmanager)" ]; then \
        echo "Running sdkmanager as root to accept licenses and install components..." && \
        # Ensure the licenses directory can be created/written to by root, if sdkmanager doesn't.
        # This is a common point of failure if the directory itself is missing or not writable.
        mkdir -p "$ANDROID_HOME/licenses" && \
        # Use yes to accept licenses; adding --verbose for more detailed output from sdkmanager
        yes | sdkmanager --licenses --sdk_root="$ANDROID_HOME" --verbose && \
        echo "Finished license acceptance attempt by root." && \
        echo "Installing/updating SDK components as root..." && \
        sdkmanager --sdk_root="$ANDROID_HOME" --verbose "platform-tools" "platforms;android-36" "build-tools;36.0.0" "cmdline-tools;latest" && \
        echo "SDK components installation/update attempt finished as root."; \
    else \
        echo "CRITICAL ERROR: sdkmanager not found for root user. This indicates a problem with the android-sdk-cmdline-tools-latest AUR package installation or PATH." && \
        exit 1; \
    fi

# After all SDK modifications by root, change ownership of the entire $ANDROID_HOME to the vscode user.
# This is critical for the non-root user to be able to use the SDK tools and for file operations within $ANDROID_HOME.
RUN if [ -d "$ANDROID_HOME" ]; then \
        echo "Attempting to change ownership of $ANDROID_HOME to vscode:users..." && \
        chown -R vscode:users "$ANDROID_HOME" && \
        echo "Ownership of $ANDROID_HOME changed to vscode:users." && \
        echo "Verifying ownership (as root, showing owner for $ANDROID_HOME itself):" && \
        ls -ld "$ANDROID_HOME" && \
        echo "Listing contents and permissions of $ANDROID_HOME/licenses (if it exists after root operations):" && \
        (ls -lA "$ANDROID_HOME/licenses" || echo "Warning: $ANDROID_HOME/licenses directory not found or not listable after root operations.") ; \
    else \
        echo "CRITICAL ERROR: $ANDROID_HOME directory does not exist after SDK setup by root." && \
        exit 1; \
    fi

# Set JAVA_HOME explicitly. Your main Dockerfile's yay command installs `zulu-24-bin`.
# The typical installation path for Zulu OpenJDK packages from AUR might be /usr/lib/jvm/zulu-24-jdk
# Ensure this path matches where zulu-24-bin actually installs.
ENV JAVA_HOME=/usr/lib/jvm/zulu-24-jdk
# Verify if this is the correct path for zulu-24-bin. If not, adjust.
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
