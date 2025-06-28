#!/bin/bash

# JMeter Installation Script for E-Commerce Load Testing
# This script automatically downloads and installs Apache JMeter

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
JMETER_VERSION="5.6.3"
JMETER_HOME="/opt/apache-jmeter"
JMETER_DOWNLOAD_URL="https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz"
JMETER_CHECKSUM_URL="https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz.sha512"
TEMP_DIR="/tmp/jmeter-install"

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if running as root
check_root() {
    if [ "$EUID" -ne 0 ]; then
        print_error "This script must be run as root (use sudo)"
        exit 1
    fi
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check if directory exists
directory_exists() {
    [ -d "$1" ]
}

# Function to download file with progress
download_file() {
    local url=$1
    local output_file=$2
    
    print_status "Downloading $url..."
    
    if command_exists wget; then
        wget --progress=bar:force:noscroll -O "$output_file" "$url"
    elif command_exists curl; then
        curl -L -o "$output_file" "$url"
    else
        print_error "Neither wget nor curl is installed. Please install one of them."
        exit 1
    fi
}

# Function to verify checksum
verify_checksum() {
    local file=$1
    local checksum_file=$2
    
    print_status "Verifying checksum..."
    
    if command_exists sha512sum; then
        local expected_checksum=$(cat "$checksum_file" | awk '{print $1}')
        local actual_checksum=$(sha512sum "$file" | awk '{print $1}')
        
        if [ "$expected_checksum" = "$actual_checksum" ]; then
            print_success "Checksum verification passed"
            return 0
        else
            print_error "Checksum verification failed"
            return 1
        fi
    else
        print_warning "sha512sum not available, skipping checksum verification"
        return 0
    fi
}

# Function to install dependencies
install_dependencies() {
    print_status "Installing dependencies..."
    
    if command_exists apt-get; then
        # Debian/Ubuntu
        apt-get update
        apt-get install -y wget curl openjdk-17-jdk
    elif command_exists yum; then
        # CentOS/RHEL
        yum install -y wget curl java-17-openjdk-devel
    elif command_exists dnf; then
        # Fedora
        dnf install -y wget curl java-17-openjdk-devel
    else
        print_warning "Package manager not detected. Please install wget, curl, and Java 17 manually."
    fi
}

# Function to check Java installation
check_java() {
    if ! command_exists java; then
        print_error "Java is not installed. Please install Java 17 or later."
        exit 1
    fi
    
    local java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$java_version" -lt 11 ]; then
        print_error "Java version $java_version is too old. Please install Java 11 or later."
        exit 1
    fi
    
    print_success "Java version $(java -version 2>&1 | head -n 1) detected"
}

# Function to create directories
create_directories() {
    print_status "Creating directories..."
    
    mkdir -p "$TEMP_DIR"
    mkdir -p "$(dirname "$JMETER_HOME")"
    
    print_success "Directories created"
}

# Function to download JMeter
download_jmeter() {
    print_status "Downloading Apache JMeter $JMETER_VERSION..."
    
    local jmeter_archive="$TEMP_DIR/apache-jmeter-${JMETER_VERSION}.tgz"
    local checksum_file="$TEMP_DIR/apache-jmeter-${JMETER_VERSION}.tgz.sha512"
    
    # Download JMeter archive
    download_file "$JMETER_DOWNLOAD_URL" "$jmeter_archive"
    
    # Download checksum
    download_file "$JMETER_CHECKSUM_URL" "$checksum_file"
    
    # Verify checksum
    if ! verify_checksum "$jmeter_archive" "$checksum_file"; then
        print_error "Checksum verification failed. Installation aborted."
        exit 1
    fi
    
    print_success "JMeter downloaded successfully"
}

# Function to extract and install JMeter
install_jmeter() {
    print_status "Installing JMeter to $JMETER_HOME..."
    
    local jmeter_archive="$TEMP_DIR/apache-jmeter-${JMETER_VERSION}.tgz"
    
    # Remove existing installation if it exists
    if directory_exists "$JMETER_HOME"; then
        print_warning "Removing existing JMeter installation..."
        rm -rf "$JMETER_HOME"
    fi
    
    # Extract JMeter
    tar -xzf "$jmeter_archive" -C "$(dirname "$JMETER_HOME")"
    
    # Rename extracted directory
    mv "$(dirname "$JMETER_HOME")/apache-jmeter-${JMETER_VERSION}" "$JMETER_HOME"
    
    # Set permissions
    chmod +x "$JMETER_HOME/bin/jmeter"
    chmod +x "$JMETER_HOME/bin/jmeter-server"
    
    print_success "JMeter installed successfully"
}

# Function to create environment setup
setup_environment() {
    print_status "Setting up environment..."
    
    # Create environment file
    cat > /etc/profile.d/jmeter.sh << EOF
# JMeter Environment Setup
export JMETER_HOME=$JMETER_HOME
export PATH=\$PATH:\$JMETER_HOME/bin
EOF
    
    # Make it executable
    chmod +x /etc/profile.d/jmeter.sh
    
    # Source it for current session
    source /etc/profile.d/jmeter.sh
    
    print_success "Environment setup completed"
}

# Function to verify installation
verify_installation() {
    print_status "Verifying installation..."
    
    if [ ! -f "$JMETER_HOME/bin/jmeter" ]; then
        print_error "JMeter executable not found"
        return 1
    fi
    
    if ! "$JMETER_HOME/bin/jmeter" -v >/dev/null 2>&1; then
        print_error "JMeter version check failed"
        return 1
    fi
    
    print_success "Installation verified successfully"
    print_status "JMeter version: $("$JMETER_HOME/bin/jmeter" -v | head -n 1)"
}

# Function to cleanup temporary files
cleanup() {
    print_status "Cleaning up temporary files..."
    rm -rf "$TEMP_DIR"
    print_success "Cleanup completed"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -v, --version VERSION  Install specific JMeter version (default: $JMETER_VERSION)"
    echo "  -d, --directory DIR    Install to specific directory (default: $JMETER_HOME)"
    echo "  -h, --help             Show this help message"
    echo ""
    echo "Examples:"
    echo "  sudo $0                # Install JMeter with default settings"
    echo "  sudo $0 -v 5.5        # Install JMeter version 5.5"
    echo "  sudo $0 -d /usr/local/jmeter  # Install to custom directory"
}

# Function to test JMeter
test_jmeter() {
    print_status "Testing JMeter installation..."
    
    # Test basic functionality
    if "$JMETER_HOME/bin/jmeter" -n -t /dev/null -l /dev/null >/dev/null 2>&1; then
        print_success "JMeter test passed"
    else
        print_warning "JMeter test failed, but installation may still be functional"
    fi
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -v|--version)
            JMETER_VERSION="$2"
            shift 2
            ;;
        -d|--directory)
            JMETER_HOME="$2"
            shift 2
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Main execution
main() {
    print_status "Apache JMeter Installation Script"
    print_status "================================="
    
    # Check if running as root
    check_root
    
    # Install dependencies
    install_dependencies
    
    # Check Java installation
    check_java
    
    # Create directories
    create_directories
    
    # Download JMeter
    download_jmeter
    
    # Install JMeter
    install_jmeter
    
    # Setup environment
    setup_environment
    
    # Verify installation
    verify_installation
    
    # Test JMeter
    test_jmeter
    
    # Cleanup
    cleanup
    
    print_success "JMeter installation completed successfully!"
    print_status ""
    print_status "Next steps:"
    print_status "1. Restart your terminal or run: source /etc/profile.d/jmeter.sh"
    print_status "2. Test JMeter: jmeter -v"
    print_status "3. Run load tests: ./run_load_tests.sh"
    print_status ""
    print_status "JMeter is installed at: $JMETER_HOME"
    print_status "Environment variable JMETER_HOME is set to: $JMETER_HOME"
}

# Run main function
main "$@" 