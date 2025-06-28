#!/bin/bash

# Quick JAR Build Script - No Connectivity Checks
# This script builds all microservices JAR files without any tests or connectivity checks

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Create jars directory
mkdir -p ./jars

# Services to build
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

print_status "Building JAR files without tests or connectivity checks..."

for service in "${services[@]}"; do
    print_status "Building $service..."
    
    if [ ! -d "./microservice-backend/$service" ]; then
        print_error "Service directory not found: ./microservice-backend/$service"
        continue
    fi
    
    cd "./microservice-backend/$service"
    
    # Build with skip tests and no connectivity checks
    if mvn clean package -DskipTests -q; then
        # Find the JAR file
        jar_file=$(find ./target -name "*.jar" -type f | head -1)
        
        if [ -n "$jar_file" ]; then
            # Copy to jars directory
            cp "$jar_file" "../../jars/$service.jar"
            print_success "$service built successfully"
        else
            print_error "No JAR file found for $service"
        fi
    else
        print_error "Failed to build $service"
    fi
    
    cd ../..
done

print_status "Build completed. Checking JAR files..."
ls -la ./jars/

print_success "JAR build process completed!" 