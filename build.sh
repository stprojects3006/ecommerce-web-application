#!/bin/bash

# PURELY E-commerce Application Build Script
# This script builds all microservices and frontend for deployment

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Function to check if Maven is available
check_maven() {
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven and try again."
        exit 1
    fi
    print_success "Maven is available"
}

# Function to check if Node.js is available
check_node() {
    if ! command -v node &> /dev/null; then
        print_error "Node.js is not installed. Please install Node.js and try again."
        exit 1
    fi
    print_success "Node.js is available"
}

# Function to create necessary directories
create_directories() {
    print_status "Creating necessary directories..."
    
    mkdir -p ./jars
    mkdir -p ./frontend/dist
    
    print_success "Directories created"
}

# Function to build microservices
build_microservices() {
    print_status "Building microservices..."
    
    services=(
        "service-registry"
        "api-gateway"
        "auth-service"
        "category-service"
        "product-service"
        "cart-service"
        "order-service"
        "user-service"
        "notification-service"
    )
    
    for service in "${services[@]}"; do
        print_status "Building $service..."
        
        cd "./microservice-backend/$service"
        
        # Clean and package
        mvn clean package -DskipTests
        
        # Copy JAR to jars directory
        cp "target/$service.jar" "../../jars/"
        
        cd ../..
        
        print_success "$service built successfully"
    done
    
    print_success "All microservices built"
}

# Function to build frontend
build_frontend() {
    print_status "Building frontend..."
    
    cd ./frontend
    
    # Install dependencies
    npm install
    
    # Build for production
    npm run build
    
    cd ..
    
    print_success "Frontend built successfully"
}

# Function to update frontend API configuration
update_frontend_config() {
    print_status "Updating frontend API configuration..."
    
    # Update the API base URL for production
    sed -i.bak 's|http://localhost/api|http://localhost/api|g' ./frontend/src/api-service/apiConfig.jsx
    
    print_success "Frontend configuration updated"
}

# Function to restore frontend config
restore_frontend_config() {
    print_status "Restoring frontend configuration..."
    
    if [ -f ./frontend/src/api-service/apiConfig.jsx.bak ]; then
        mv ./frontend/src/api-service/apiConfig.jsx.bak ./frontend/src/api-service/apiConfig.jsx
    fi
    
    print_success "Frontend configuration restored"
}

# Function to display build information
display_info() {
    echo ""
    echo "=========================================="
    echo "🚀 PURELY E-commerce Application Built!"
    echo "=========================================="
    echo ""
    echo "📦 Built Artifacts:"
    echo "   JAR files: ./jars/"
    echo "   Frontend: ./frontend/dist/"
    echo ""
    echo "📋 Next Steps:"
    echo "   1. Copy JAR files to deployment server"
    echo "   2. Copy frontend/dist to deployment server"
    echo "   3. Run ./deploy.sh to start the application"
    echo ""
    echo "📊 JAR Files Created:"
    ls -la ./jars/
    echo ""
}

# Function to handle cleanup on script exit
cleanup() {
    print_status "Cleaning up..."
    restore_frontend_config
}

# Set up trap for cleanup
trap cleanup EXIT

# Main build process
main() {
    echo "🔨 Starting PURELY E-commerce Application Build..."
    echo ""
    
    # Pre-build checks
    check_maven
    check_node
    
    # Setup
    create_directories
    
    # Build
    build_microservices
    update_frontend_config
    build_frontend
    
    # Display information
    display_info
    
    print_success "Build completed successfully!"
}

# Run main function
main "$@" 