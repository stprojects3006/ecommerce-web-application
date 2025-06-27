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

# Function to check if npm is available
check_npm() {
    if ! command -v npm &> /dev/null; then
        print_error "npm is not installed. Please install npm and try again."
        exit 1
    fi
    print_success "npm is available"
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
        
        # Check if service directory exists
        if [ ! -d "./microservice-backend/$service" ]; then
            print_error "Service directory not found: ./microservice-backend/$service"
            exit 1
        fi
        
        cd "./microservice-backend/$service"
        print_status "Current directory: $(pwd)"
        
        # Clean and package
        print_status "Running Maven build for $service..."
        if ! mvn clean package -DskipTests; then
            print_error "Failed to build $service"
            exit 1
        fi
        
        # List target directory to see what JAR was created
        print_status "Checking target directory for $service..."
        if [ ! -d "./target" ]; then
            print_error "Target directory not found for $service"
            exit 1
        fi
        
        ls -la "./target/"
        
        # Find the JAR file (handle different naming patterns)
        jar_file=$(find ./target -name "*.jar" -type f | head -1)
        
        if [ -z "$jar_file" ]; then
            print_error "No JAR file found for $service in target directory"
            exit 1
        fi
        
        print_status "Found JAR file: $jar_file"
        
        # Copy JAR to jars directory with service name
        print_status "Copying JAR file to jars directory..."
        cp "$jar_file" "../../jars/$service.jar"
        
        if [ $? -eq 0 ]; then
            print_success "Copied $jar_file to ../../jars/$service.jar"
        else
            print_error "Failed to copy JAR file for $service"
            exit 1
        fi
        
        cd ../..
        
        print_success "$service built successfully"
        echo ""
    done
    
    print_success "All microservices built"
}

# Function to build frontend
build_frontend() {
    print_status "Building frontend..."
    
    if [ ! -d "./frontend" ]; then
        print_error "Frontend directory not found: ./frontend"
        exit 1
    fi
    
    cd ./frontend
    print_status "Current directory: $(pwd)"
    
    # Check if package.json exists
    if [ ! -f "package.json" ]; then
        print_error "package.json not found in frontend directory"
        exit 1
    fi
    
    # Install dependencies
    print_status "Installing frontend dependencies..."
    if ! npm install; then
        print_error "Failed to install frontend dependencies"
        exit 1
    fi
    
    # Build for production
    print_status "Building frontend for production..."
    if ! npm run build; then
        print_error "Failed to build frontend"
        exit 1
    fi
    
    # Check if build was successful
    if [ ! -d "dist" ]; then
        print_error "Frontend build failed - dist directory not found"
        exit 1
    fi
    
    cd ..
    
    print_success "Frontend built successfully"
}

# Function to update frontend API configuration for production
update_frontend_config() {
    print_status "Updating frontend API configuration for production..."
    
    if [ ! -f "./frontend/src/api-service/apiConfig.jsx" ]; then
        print_warning "Frontend API config file not found, skipping configuration update"
        return
    fi
    
    # Create backup
    cp ./frontend/src/api-service/apiConfig.jsx ./frontend/src/api-service/apiConfig.jsx.bak
    
    # Update the API base URL for production (using localhost for local deployment)
    sed -i.bak 's|http://localhost:8081|http://localhost/api|g' ./frontend/src/api-service/apiConfig.jsx
    
    print_success "Frontend configuration updated for production"
}

# Function to restore frontend config
restore_frontend_config() {
    print_status "Restoring frontend configuration..."
    
    if [ -f ./frontend/src/api-service/apiConfig.jsx.bak ]; then
        mv ./frontend/src/api-service/apiConfig.jsx.bak ./frontend/src/api-service/apiConfig.jsx
        print_success "Frontend configuration restored"
    fi
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
    if [ -d "./jars" ]; then
        ls -la ./jars/
    else
        echo "   No JAR files found"
    fi
    echo ""
    echo "🌐 Frontend Build:"
    if [ -d "./frontend/dist" ]; then
        ls -la ./frontend/dist/
    else
        echo "   Frontend build not found"
    fi
    echo ""
}

# Function to handle cleanup on script exit
cleanup() {
    print_status "Cleaning up..."
    restore_frontend_config
}

# Set up trap for cleanup
trap cleanup EXIT

# Function to validate build artifacts
validate_build() {
    print_status "Validating build artifacts..."
    
    # Check JAR files
    expected_jars=(
        "service-registry.jar"
        "api-gateway.jar"
        "auth-service.jar"
        "category-service.jar"
        "product-service.jar"
        "cart-service.jar"
        "order-service.jar"
        "user-service.jar"
        "notification-service.jar"
    )
    
    missing_jars=()
    for jar in "${expected_jars[@]}"; do
        if [ ! -f "./jars/$jar" ]; then
            missing_jars+=("$jar")
        fi
    done
    
    if [ ${#missing_jars[@]} -gt 0 ]; then
        print_warning "Missing JAR files: ${missing_jars[*]}"
    else
        print_success "All expected JAR files are present"
    fi
    
    # Check frontend build
    if [ ! -d "./frontend/dist" ]; then
        print_warning "Frontend build directory not found"
    else
        print_success "Frontend build directory is present"
    fi
}

# Main build process
main() {
    echo "🔨 Starting PURELY E-commerce Application Build..."
    echo ""
    
    # Pre-build checks
    print_status "Running pre-build checks..."
    check_maven
    check_node
    check_npm
    echo ""
    
    # Setup
    create_directories
    echo ""
    
    # Build
    build_microservices
    echo ""
    
    update_frontend_config
    build_frontend
    echo ""
    
    # Validate build
    validate_build
    echo ""
    
    # Display information
    display_info
    
    print_success "Build completed successfully!"
}

# Run main function
main "$@"