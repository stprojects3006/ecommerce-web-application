#!/bin/bash

# PURELY E-commerce Application Deployment Script
# This script builds and deploys the entire microservices stack

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

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Function to check if Docker Compose is available
check_docker_compose() {
    if ! command -v docker-compose >/dev/null 2>&1; then
        print_error "Docker Compose is not installed. Please install it and try again."
        exit 1
    fi
    print_success "Docker Compose is available"
}

# Function to check if Maven is available
check_maven() {
    if ! command -v mvn >/dev/null 2>&1; then
        print_error "Maven is not installed. Please install it and try again."
        exit 1
    fi
    print_success "Maven is available"
}

# Function to build all microservices
build_microservices() {
    print_status "Building all microservices..."
    
    # Check if build script exists
    if [ ! -f "zz-automationscripts/build.sh" ]; then
        print_error "Build script not found: zz-automationscripts/build.sh"
        exit 1
    fi
    
    # Make build script executable
    chmod +x zz-automationscripts/build.sh
    
    # Run the build script
    zz-automationscripts/build.sh
    
    print_success "All microservices built successfully"
}

# Function to check for required build artifacts
check_build_artifacts() {
    print_status "Checking for required build artifacts..."
    
    # Check for JAR files
    if [ ! -d "./jars" ]; then
        print_error "JAR files directory not found. Please run the build process first."
        exit 1
    fi
    
    required_jars="service-registry.jar api-gateway.jar auth-service.jar category-service.jar product-service.jar cart-service.jar order-service.jar user-service.jar notification-service.jar"
    
    for jar in $required_jars; do
        if [ ! -f "./jars/$jar" ]; then
            print_error "Required JAR file not found: $jar"
            print_error "Please run the build process to build all microservices."
            exit 1
        fi
    done
    
    # Check for frontend dist
    if [ ! -d "./frontend/dist" ]; then
        print_warning "Frontend dist directory not found. Building frontend..."
        build_frontend
    fi
    
    print_success "All build artifacts found"
}

# Function to build frontend
build_frontend() {
    print_status "Building frontend..."
    
    if [ ! -d "./frontend" ]; then
        print_error "Frontend directory not found."
        exit 1
    fi
    
    cd frontend
    
    # Check if node_modules exists, if not install dependencies
    if [ ! -d "./node_modules" ]; then
        print_status "Installing frontend dependencies..."
        npm install
    fi
    
    # Build the frontend
    print_status "Building frontend for production..."
    npm run build
    
    cd ..
    
    print_success "Frontend built successfully"
}

# Function to create necessary directories and configs
create_directories() {
    print_status "Creating necessary directories..."
    
    mkdir -p ./prometheus
    mkdir -p ./logs
    
    # Ensure prometheus configs are in the right place
    if [ ! -f ./prometheus/prometheus.yml ]; then
        print_error "Prometheus config not found: ../prometheus/prometheus.yml"
        exit 1
    fi
    
    if [ ! -f ./prometheus/blackbox.yml ]; then
        print_error "Blackbox config not found: ./prometheus/blackbox.yml"
        exit 1
    fi
    
    if [ ! -f ./prometheus/promtail-config.yml ]; then
        print_error "Promtail config not found: ./prometheus/promtail-config.yml"
        exit 1
    fi
    
    if [ ! -f ./prometheus/blackbox-rules.yml ]; then
        print_error "Blackbox rules not found: ./prometheus/blackbox-rules.yml"
        exit 1
    fi
    
    print_success "Directories and configs verified"
}

# Function to stop existing containers
stop_containers() {
    print_status "Stopping existing containers..."
    docker-compose down --remove-orphans || true
    print_success "Existing containers stopped"
}

# Function to wait for service registry to be healthy
wait_for_service_registry() {
    print_status "Waiting for service registry to be fully healthy..."
    
    local max_attempts=30
    local attempt=1
    local wait_time=10
    
    while [ $attempt -le $max_attempts ]; do
        print_status "Checking service registry health (attempt $attempt/$max_attempts)..."
        
        # Check if service registry is responding
        if curl -f -s "http://localhost:8761/actuator/health" > /dev/null 2>&1; then
            print_success "Service registry is healthy and responding"
            
            # Additional wait to ensure it's fully initialized
            print_status "Waiting additional 10 seconds for service registry to fully initialize..."
            sleep 10
            
            # Double-check health status
            if curl -f -s "http://localhost:8761/actuator/health" > /dev/null 2>&1; then
                print_success "Service registry is fully ready for service registration"
                return 0
            else
                print_warning "Service registry health check failed after additional wait"
            fi
        else
            print_warning "Service registry not ready yet (attempt $attempt/$max_attempts)"
        fi
        
        attempt=$((attempt + 1))
        sleep $wait_time
    done
    
    print_error "Service registry failed to become healthy after $max_attempts attempts"
    print_error "Please check service registry logs: docker-compose logs service-registry"
    exit 1
}

# Function to start service registry first
start_service_registry() {
    print_status "Starting service registry first..."
    
    # Start only the service registry
    docker-compose up -d service-registry
    
    print_success "Service registry started"
}

# Function to start remaining services
start_remaining_services() {
    print_status "Starting remaining services..."
    
    # Start all other services except service-registry
    docker-compose up -d api-gateway auth-service category-service product-service cart-service order-service user-service notification-service nginx prometheus grafana promtail blackbox-exporter node-exporter
    
    print_success "Remaining services started"
}

# Function to build and start services
start_services() {
    print_status "Building and starting services with staged deployment..."
    
    # Stage 1: Start service registry first
    start_service_registry
    
    # Stage 2: Wait for service registry to be healthy
    wait_for_service_registry
    
    # Stage 3: Start remaining services
    start_remaining_services
    
    print_success "All services started with staged deployment"
}

# Function to check service health
check_services() {
    print_status "Checking service health..."
    
    # Wait for services to be ready (shorter wait since service registry is already healthy)
    print_status "Waiting 30 seconds for all services to initialize..."
    sleep 30
    
    # Check if services are responding with more detailed feedback
    services=(
        "Service Registry:http://localhost:8761/actuator/health"
        "API Gateway:http://localhost:8081/actuator/health"
        "Frontend:http://localhost"
        "Prometheus:http://localhost:9090"
        "Grafana:http://localhost:3000"
        "Nginx Exporter:http://localhost:9113/metrics"
        "Blackbox Exporter:http://localhost:9115"
        "Node Exporter:http://localhost:9100/metrics"
    )
    
    local healthy_count=0
    local total_services=${#services[@]}
    
    for service_info in "${services[@]}"; do
        IFS=':' read -r service_name service_url <<< "$service_info"
        
        print_status "Checking $service_name..."
        
        if curl -f -s "$service_url" > /dev/null 2>&1; then
            print_success "$service_name is healthy"
            ((healthy_count++))
        else
            print_warning "$service_name may not be ready yet"
        fi
    done
    
    print_status "Health check summary: $healthy_count/$total_services services are healthy"
    
    if [ $healthy_count -eq $total_services ]; then
        print_success "All services are healthy and ready!"
    else
        print_warning "Some services may still be starting up. This is normal for the first deployment."
        print_status "You can check service status with: docker-compose ps"
        print_status "View logs with: docker-compose logs -f"
    fi
}

# Function to display access information
display_info() {
    echo ""
    echo "=========================================="
    echo "🚀 PURELY E-commerce Application Deployed!"
    echo "=========================================="
    echo ""
    echo "📱 Frontend Application:"
    echo "   URL: http://localhost"
    echo ""
    echo "🔧 Backend Services:"
    echo "   API Gateway: http://localhost:8081"
    echo "   Service Registry: http://localhost:8761"
    echo ""
    echo "📊 Monitoring:"
    echo "   Prometheus: http://localhost:9090"
    echo "   Grafana: http://localhost:3000 (admin/admin)"
    echo "   Nginx Exporter: http://localhost:9113/metrics"
    echo "   Blackbox Exporter: http://localhost:9115"
    echo "   Node Exporter: http://localhost:9100/metrics"
    echo ""
    echo "🗄️  Database:"
    echo "   MongoDB: localhost:27017"
    echo ""
    echo "📝 Useful Commands:"
    echo "   View logs: docker-compose logs -f"
    echo "   Stop services: docker-compose down"
    echo "   Restart services: docker-compose restart"
    echo "   Restart specific service: docker-compose restart api-gateway"
    echo ""
    echo "⚠️  Note: Make sure to configure email settings in notification-service"
    echo "   for order confirmation emails to work properly."
    echo ""
}

# Function to handle cleanup on script exit
cleanup() {
    print_status "Cleaning up..."
    # Add any cleanup tasks here if needed
}

# Set up trap for cleanup
trap cleanup EXIT

# Main deployment process
main() {
    echo "🚀 Starting PURELY E-commerce Application Deployment..."
    echo ""
    
    # Pre-deployment checks
    check_docker
    check_docker_compose
    check_maven
    
    # Build process
    build_microservices
    check_build_artifacts
    
    # Setup
    create_directories
    stop_containers
    
    # Deploy
    start_services
    check_services

    # Import sample data
    print_status "Importing sample data into MongoDB..."
    chmod +x zz-automationscripts/import_sample_data.sh
    zz-automationscripts/import_sample_data.sh

    # Display information
    display_info
    
    print_success "Deployment completed successfully!"
}

# Run main function
main "$@" 