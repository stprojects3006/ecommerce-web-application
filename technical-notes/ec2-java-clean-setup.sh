#!/bin/bash

echo "=== Java Cleanup and Installation Script ==="

# Step 1: Remove existing Java
echo "Removing existing Java installations..."
sudo apt-get remove --purge openjdk* oracle-java* java* -y
sudo apt-get autoremove -y
sudo apt-get autoclean -y

# Step 2: Clean directories
echo "Cleaning Java directories..."
sudo rm -rf /usr/lib/jvm/*
sudo rm -rf /usr/java/*
sudo rm -rf /opt/java*
sudo rm -rf /usr/local/java*

# Step 3: Clean environment
echo "Cleaning environment variables..."
sudo sed -i '/JAVA_HOME/d' /etc/environment
sudo sed -i '/java/d' /etc/environment
sed -i '/JAVA_HOME/d' ~/.bashrc
sed -i '/java/d' ~/.bashrc

# Step 4: Remove alternatives
echo "Removing Java alternatives..."
sudo update-alternatives --remove-all java
sudo update-alternatives --remove-all javac

# Step 5: Install Java 17
echo "Installing Java 17..."
sudo apt update
sudo apt install openjdk-17-jdk -y

# Step 6: Configure Java 17
echo "Configuring Java 17..."
sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-17-openjdk-amd64/bin/java 1
sudo update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/java-17-openjdk-amd64/bin/javac 1

# Step 7: Set up environment variables properly
echo "Setting up environment variables..."
JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"

# Add to /etc/environment for system-wide availability
echo "JAVA_HOME=$JAVA_HOME" | sudo tee -a /etc/environment

# Add to ~/.bashrc for current user
echo "export JAVA_HOME=$JAVA_HOME" >> ~/.bashrc
echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc

# Step 8: Install Maven
echo "Installing Maven..."
sudo apt install maven -y

# Step 9: Reload environment and verify installation
echo "Reloading environment and verifying installation..."
export JAVA_HOME=$JAVA_HOME
export PATH=$JAVA_HOME/bin:$PATH

# Ensure basic PATH includes standard directories
export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:$PATH

java -version
mvn -version

echo "=== Installation Complete ==="
echo ""
echo "IMPORTANT: Please run the following command to reload your shell environment:"
echo "source ~/.bashrc"
echo ""
echo "Or simply start a new terminal session."
