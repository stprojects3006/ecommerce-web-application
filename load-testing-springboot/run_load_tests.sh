#!/bin/bash

# E-Commerce Load Testing Runner Script
# This script runs the Spring Boot load testing application and JMeter tests

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SPRING_APP_PORT=8081
JMETER_HOME=${JMETER_HOME:-"/opt/apache-jmeter"}
TEST_REPORTS_DIR="test-reports"
JMETER_TEST_PLANS=(
    "jmeter/E-Commerce_Load_Test_Plan.jmx"
    "jmeter/Stress_Test_Plan.jmx"
    "jmeter/Performance_Test_Plan.jmx"
)

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

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check if port is in use
port_in_use() {
    lsof -i :$1 >/dev/null 2>&1
}

# Function to wait for service to be ready
wait_for_service() {
    local host=$1
    local port=$2
    local max_attempts=30
    local attempt=1
    
    print_status "Waiting for service at $host:$port to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "http://$host:$port/actuator/health" >/dev/null 2>&1; then
            print_success "Service is ready!"
            return 0
        fi
        
        print_status "Attempt $attempt/$max_attempts - Service not ready yet..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_error "Service failed to start within expected time"
    return 1
}

# Function to build Spring Boot application
build_spring_app() {
    print_status "Building Spring Boot load testing application..."
    
    if [ ! -f "pom.xml" ]; then
        print_error "pom.xml not found. Please run this script from the load-testing-springboot directory."
        exit 1
    fi
    
    if ! command_exists mvn; then
        print_error "Maven is not installed. Please install Maven first."
        exit 1
    fi
    
    mvn clean package -DskipTests
    print_success "Spring Boot application built successfully"
}

# Function to start Spring Boot application
start_spring_app() {
    print_status "Starting Spring Boot load testing application..."
    
    if port_in_use $SPRING_APP_PORT; then
        print_warning "Port $SPRING_APP_PORT is already in use. Stopping existing process..."
        pkill -f "load-testing-springboot" || true
        sleep 2
    fi
    
    nohup java -jar target/load-testing-springboot-1.0.0.jar > spring-app.log 2>&1 &
    SPRING_APP_PID=$!
    
    print_status "Spring Boot application started with PID: $SPRING_APP_PID"
    
    # Wait for application to be ready
    wait_for_service "localhost" $SPRING_APP_PORT
}

# Function to check JMeter installation
check_jmeter() {
    if [ ! -d "$JMETER_HOME" ]; then
        print_error "JMeter not found at $JMETER_HOME"
        print_status "Please install JMeter or set JMETER_HOME environment variable"
        print_status "You can download JMeter from: https://jmeter.apache.org/download_jmeter.cgi"
        exit 1
    fi
    
    if [ ! -f "$JMETER_HOME/bin/jmeter" ]; then
        print_error "JMeter executable not found at $JMETER_HOME/bin/jmeter"
        exit 1
    fi
    
    print_success "JMeter found at: $JMETER_HOME"
}

# Function to create test reports directory
create_reports_dir() {
    if [ ! -d "$TEST_REPORTS_DIR" ]; then
        mkdir -p "$TEST_REPORTS_DIR"
        print_status "Created test reports directory: $TEST_REPORTS_DIR"
    fi
}

# Function to run JMeter test
run_jmeter_test() {
    local test_plan=$1
    local test_name=$(basename "$test_plan" .jmx)
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local report_dir="$TEST_REPORTS_DIR/${test_name}_${timestamp}"
    
    print_status "Running JMeter test: $test_name"
    
    if [ ! -f "$test_plan" ]; then
        print_error "Test plan not found: $test_plan"
        return 1
    fi
    
    # Create report directory
    mkdir -p "$report_dir"
    
    # Run JMeter test
    "$JMETER_HOME/bin/jmeter" \
        -n \
        -t "$test_plan" \
        -l "$report_dir/results.jtl" \
        -e \
        -o "$report_dir/html-report" \
        -JBASE_URL=http://localhost:8080 \
        -JAPI_GATEWAY_URL=http://localhost:8080/api
    
    if [ $? -eq 0 ]; then
        print_success "JMeter test completed: $test_name"
        print_status "Results saved to: $report_dir"
        print_status "HTML report available at: $report_dir/html-report/index.html"
    else
        print_error "JMeter test failed: $test_name"
        return 1
    fi
}

# Function to run all JMeter tests
run_all_jmeter_tests() {
    print_status "Running all JMeter tests..."
    
    for test_plan in "${JMETER_TEST_PLANS[@]}"; do
        if [ -f "$test_plan" ]; then
            run_jmeter_test "$test_plan"
        else
            print_warning "Test plan not found: $test_plan"
        fi
    done
}

# Function to stop Spring Boot application
stop_spring_app() {
    if [ ! -z "$SPRING_APP_PID" ]; then
        print_status "Stopping Spring Boot application (PID: $SPRING_APP_PID)..."
        kill $SPRING_APP_PID 2>/dev/null || true
        sleep 2
        
        # Force kill if still running
        if kill -0 $SPRING_APP_PID 2>/dev/null; then
            print_warning "Force killing Spring Boot application..."
            kill -9 $SPRING_APP_PID 2>/dev/null || true
        fi
        
        print_success "Spring Boot application stopped"
    fi
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -b, --build-only      Build Spring Boot application only"
    echo "  -s, --spring-only     Run Spring Boot application only"
    echo "  -j, --jmeter-only     Run JMeter tests only"
    echo "  -a, --all             Run everything (default)"
    echo "  -h, --help            Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                    # Run all tests"
    echo "  $0 --build-only       # Build application only"
    echo "  $0 --jmeter-only      # Run JMeter tests only"
}

# Function to cleanup on exit
cleanup() {
    print_status "Cleaning up..."
    stop_spring_app
}

# Set trap to cleanup on script exit
trap cleanup EXIT

# Parse command line arguments
BUILD_ONLY=false
SPRING_ONLY=false
JMETER_ONLY=false
RUN_ALL=true

while [[ $# -gt 0 ]]; do
    case $1 in
        -b|--build-only)
            BUILD_ONLY=true
            RUN_ALL=false
            shift
            ;;
        -s|--spring-only)
            SPRING_ONLY=true
            RUN_ALL=false
            shift
            ;;
        -j|--jmeter-only)
            JMETER_ONLY=true
            RUN_ALL=false
            shift
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
    print_status "Starting E-Commerce Load Testing Suite"
    print_status "======================================"
    
    # Check prerequisites
    check_jmeter
    
    # Build application
    if [ "$BUILD_ONLY" = true ] || [ "$RUN_ALL" = true ]; then
        build_spring_app
        if [ "$BUILD_ONLY" = true ]; then
            print_success "Build completed successfully"
            exit 0
        fi
    fi
    
    # Start Spring Boot application
    if [ "$SPRING_ONLY" = true ] || [ "$RUN_ALL" = true ]; then
        start_spring_app
        if [ "$SPRING_ONLY" = true ]; then
            print_success "Spring Boot application is running"
            print_status "Press Ctrl+C to stop"
            wait
        fi
    fi
    
    # Run JMeter tests
    if [ "$JMETER_ONLY" = true ] || [ "$RUN_ALL" = true ]; then
        create_reports_dir
        run_all_jmeter_tests
    fi
    
    print_success "Load testing completed successfully!"
    print_status "Check the $TEST_REPORTS_DIR directory for detailed results"
}

# Run main function
main "$@" 