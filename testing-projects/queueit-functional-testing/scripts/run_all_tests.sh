#!/bin/bash

# Queue-it Integration Testing - Run All Tests
# Comprehensive test suite for Queue-it integration

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
REPORTS_DIR="$PROJECT_DIR/reports"
SCREENSHOTS_DIR="$REPORTS_DIR/screenshots"
LOGS_DIR="$REPORTS_DIR/logs"

# Create necessary directories
mkdir -p "$REPORTS_DIR" "$SCREENSHOTS_DIR" "$LOGS_DIR"

# Logging
LOG_FILE="$LOGS_DIR/test_run_$(date +%Y%m%d_%H%M%S).log"

log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a "$LOG_FILE"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
}

# Function to check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check Python
    if ! command -v python3 &> /dev/null; then
        log_error "Python 3 is required but not installed"
        exit 1
    fi
    
    # Check pip
    if ! command -v pip3 &> /dev/null; then
        log_error "pip3 is required but not installed"
        exit 1
    fi
    
    # Check if virtual environment exists
    if [ ! -d "$PROJECT_DIR/venv" ]; then
        log_warning "Virtual environment not found. Creating one..."
        python3 -m venv "$PROJECT_DIR/venv"
    fi
    
    # Activate virtual environment
    source "$PROJECT_DIR/venv/bin/activate"
    
    # Install dependencies
    log "Installing dependencies..."
    pip install -r "$PROJECT_DIR/requirements.txt"
    
    log_success "Prerequisites check completed"
}

# Function to check test environment
check_test_environment() {
    log "Checking test environment..."
    
    # Check if configuration file exists
    if [ ! -f "$PROJECT_DIR/config/test_config.json" ]; then
        log_error "Test configuration file not found: config/test_config.json"
        exit 1
    fi
    
    # Check if required services are running (optional)
    log "Checking if required services are accessible..."
    
    # Check API Gateway
    if curl -s "http://localhost:8081/api/queueit/health" > /dev/null 2>&1; then
        log_success "API Gateway is accessible"
    else
        log_warning "API Gateway is not accessible (tests may fail)"
    fi
    
    # Check Frontend
    if curl -s "http://localhost:5173" > /dev/null 2>&1; then
        log_success "Frontend is accessible"
    else
        log_warning "Frontend is not accessible (frontend tests may fail)"
    fi
    
    # Check Grafana
    if curl -s "http://localhost:3000/api/health" > /dev/null 2>&1; then
        log_success "Grafana is accessible"
    else
        log_warning "Grafana is not accessible (monitoring may not work)"
    fi
    
    log_success "Test environment check completed"
}

# Function to run backend tests
run_backend_tests() {
    log "Running backend API tests..."
    
    cd "$PROJECT_DIR"
    
    # Run backend tests with coverage
    python -m pytest tests/backend/ \
        -v \
        --html="$REPORTS_DIR/backend_report.html" \
        --json-report --json-report-file="$REPORTS_DIR/backend_report.json" \
        --cov=tests.backend \
        --cov-report=html:"$REPORTS_DIR/backend_coverage" \
        --cov-report=term-missing \
        --tb=short \
        --durations=10
    
    if [ $? -eq 0 ]; then
        log_success "Backend tests completed successfully"
    else
        log_error "Backend tests failed"
        return 1
    fi
}

# Function to run frontend tests
run_frontend_tests() {
    log "Running frontend tests..."
    
    cd "$PROJECT_DIR"
    
    # Run frontend tests
    python -m pytest tests/frontend/ \
        -v \
        --html="$REPORTS_DIR/frontend_report.html" \
        --json-report --json-report-file="$REPORTS_DIR/frontend_report.json" \
        --tb=short \
        --durations=10
    
    if [ $? -eq 0 ]; then
        log_success "Frontend tests completed successfully"
    else
        log_error "Frontend tests failed"
        return 1
    fi
}

# Function to run integration tests
run_integration_tests() {
    log "Running integration tests..."
    
    cd "$PROJECT_DIR"
    
    # Run integration tests
    python -m pytest tests/integration/ \
        -v \
        --html="$REPORTS_DIR/integration_report.html" \
        --json-report --json-report-file="$REPORTS_DIR/integration_report.json" \
        --tb=short \
        --durations=10
    
    if [ $? -eq 0 ]; then
        log_success "Integration tests completed successfully"
    else
        log_error "Integration tests failed"
        return 1
    fi
}

# Function to run performance tests
run_performance_tests() {
    log "Running performance tests..."
    
    cd "$PROJECT_DIR"
    
    # Run performance tests
    python -m pytest tests/performance/ \
        -v \
        --html="$REPORTS_DIR/performance_report.html" \
        --json-report --json-report-file="$REPORTS_DIR/performance_report.json" \
        --tb=short \
        --durations=10
    
    if [ $? -eq 0 ]; then
        log_success "Performance tests completed successfully"
    else
        log_error "Performance tests failed"
        return 1
    fi
}

# Function to generate combined report
generate_combined_report() {
    log "Generating combined test report..."
    
    cd "$PROJECT_DIR"
    
    # Create combined HTML report
    cat > "$REPORTS_DIR/combined_report.html" << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>Queue-it Integration Test Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #667eea; color: white; padding: 20px; border-radius: 8px; }
        .summary { background: #f8f9fa; padding: 20px; margin: 20px 0; border-radius: 8px; }
        .test-section { margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 8px; }
        .success { border-left: 5px solid #28a745; }
        .failure { border-left: 5px solid #dc3545; }
        .warning { border-left: 5px solid #ffc107; }
        .metric { background: #e3f2fd; padding: 10px; margin: 5px 0; border-radius: 4px; }
        .link { color: #007bff; text-decoration: none; }
        .link:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Queue-it Integration Test Report</h1>
        <p>Generated on: $(date)</p>
    </div>
    
    <div class="summary">
        <h2>Test Summary</h2>
        <p>This report contains results from all Queue-it integration tests.</p>
        <p>For detailed reports, see the individual test reports below.</p>
    </div>
    
    <div class="test-section success">
        <h3>Backend API Tests</h3>
        <p>Tests for Queue-it backend API endpoints and functionality.</p>
        <a href="backend_report.html" class="link">View Backend Report</a>
    </div>
    
    <div class="test-section success">
        <h3>Frontend Tests</h3>
        <p>Tests for Queue-it frontend components and user interface.</p>
        <a href="frontend_report.html" class="link">View Frontend Report</a>
    </div>
    
    <div class="test-section success">
        <h3>Integration Tests</h3>
        <p>End-to-end tests for Queue-it integration across frontend and backend.</p>
        <a href="integration_report.html" class="link">View Integration Report</a>
    </div>
    
    <div class="test-section success">
        <h3>Performance Tests</h3>
        <p>Load testing and performance analysis for Queue-it functionality.</p>
        <a href="performance_report.html" class="link">View Performance Report</a>
    </div>
    
    <div class="test-section">
        <h3>Test Artifacts</h3>
        <ul>
            <li><a href="backend_coverage/index.html" class="link">Backend Coverage Report</a></li>
            <li><a href="screenshots/" class="link">Test Screenshots</a></li>
            <li><a href="logs/" class="link">Test Logs</a></li>
        </ul>
    </div>
</body>
</html>
EOF
    
    log_success "Combined report generated: $REPORTS_DIR/combined_report.html"
}

# Function to display test results summary
display_summary() {
    log "Generating test results summary..."
    
    # Count test results from JSON reports
    total_tests=0
    passed_tests=0
    failed_tests=0
    
    for report_file in "$REPORTS_DIR"/*_report.json; do
        if [ -f "$report_file" ]; then
            # Extract test counts using jq if available, otherwise use grep
            if command -v jq &> /dev/null; then
                summary=$(jq '.summary' "$report_file" 2>/dev/null || echo '{}')
                passed=$(echo "$summary" | jq -r '.passed // 0')
                failed=$(echo "$summary" | jq -r '.failed // 0')
                total=$(echo "$summary" | jq -r '.total // 0')
            else
                # Fallback to grep
                passed=$(grep -o '"passed": [0-9]*' "$report_file" | grep -o '[0-9]*' | head -1 || echo "0")
                failed=$(grep -o '"failed": [0-9]*' "$report_file" | grep -o '[0-9]*' | head -1 || echo "0")
                total=$(grep -o '"total": [0-9]*' "$report_file" | grep -o '[0-9]*' | head -1 || echo "0")
            fi
            
            total_tests=$((total_tests + total))
            passed_tests=$((passed_tests + passed))
            failed_tests=$((failed_tests + failed))
        fi
    done
    
    # Calculate success rate
    if [ $total_tests -gt 0 ]; then
        success_rate=$(echo "scale=1; $passed_tests * 100 / $total_tests" | bc -l 2>/dev/null || echo "0")
    else
        success_rate=0
    fi
    
    # Display summary
    echo
    echo "=========================================="
    echo "           TEST RESULTS SUMMARY           "
    echo "=========================================="
    echo "Total Tests: $total_tests"
    echo "Passed: $passed_tests"
    echo "Failed: $failed_tests"
    echo "Success Rate: ${success_rate}%"
    echo "=========================================="
    echo
    echo "Reports generated in: $REPORTS_DIR"
    echo "Main report: $REPORTS_DIR/combined_report.html"
    echo "Log file: $LOG_FILE"
    echo
    
    # Return appropriate exit code
    if [ $failed_tests -gt 0 ]; then
        log_warning "Some tests failed. Check the reports for details."
        return 1
    else
        log_success "All tests passed successfully!"
        return 0
    fi
}

# Function to cleanup
cleanup() {
    log "Cleaning up..."
    
    # Deactivate virtual environment
    if [ -n "$VIRTUAL_ENV" ]; then
        deactivate
    fi
    
    log_success "Cleanup completed"
}

# Main execution
main() {
    echo "=========================================="
    echo "    Queue-it Integration Test Suite       "
    echo "=========================================="
    echo
    
    # Set up trap for cleanup
    trap cleanup EXIT
    
    # Check prerequisites
    check_prerequisites
    
    # Check test environment
    check_test_environment
    
    # Run all test suites
    log "Starting comprehensive test suite..."
    
    # Track overall success
    overall_success=true
    
    # Run backend tests
    if run_backend_tests; then
        log_success "Backend tests passed"
    else
        log_error "Backend tests failed"
        overall_success=false
    fi
    
    # Run frontend tests
    if run_frontend_tests; then
        log_success "Frontend tests passed"
    else
        log_error "Frontend tests failed"
        overall_success=false
    fi
    
    # Run integration tests
    if run_integration_tests; then
        log_success "Integration tests passed"
    else
        log_error "Integration tests failed"
        overall_success=false
    fi
    
    # Run performance tests
    if run_performance_tests; then
        log_success "Performance tests passed"
    else
        log_error "Performance tests failed"
        overall_success=false
    fi
    
    # Generate combined report
    generate_combined_report
    
    # Display summary
    if display_summary; then
        log_success "Test suite completed successfully!"
    else
        log_error "Test suite completed with failures"
    fi
    
    echo
    echo "=========================================="
    echo "           TEST SUITE COMPLETED           "
    echo "=========================================="
    echo
    
    # Exit with appropriate code
    if [ "$overall_success" = true ]; then
        exit 0
    else
        exit 1
    fi
}

# Run main function
main "$@" 