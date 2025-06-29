#!/bin/bash

# Queue-it Frontend Testing Script
# Run frontend-specific tests for Queue-it integration

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
LOGS_DIR="$REPORTS_DIR/logs"

# Create necessary directories
mkdir -p "$REPORTS_DIR" "$LOGS_DIR"

# Logging
LOG_FILE="$LOGS_DIR/frontend_tests_$(date +%Y%m%d_%H%M%S).log"

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
    log "Checking frontend test prerequisites..."
    
    # Check Python
    if ! command -v python3 &> /dev/null; then
        log_error "Python 3 is required but not installed"
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
    log "Installing frontend test dependencies..."
    pip install -r "$PROJECT_DIR/requirements.txt"
    
    log_success "Prerequisites check completed"
}

# Function to check frontend environment
check_frontend_environment() {
    log "Checking frontend test environment..."
    
    # Check if configuration file exists
    if [ ! -f "$PROJECT_DIR/config/test_config.json" ]; then
        log_error "Test configuration file not found: config/test_config.json"
        exit 1
    fi
    
    # Check if frontend is accessible
    log "Checking if frontend is accessible..."
    if curl -s "http://localhost:5173" > /dev/null 2>&1; then
        log_success "Frontend is accessible"
    else
        log_warning "Frontend is not accessible (tests may fail)"
    fi
    
    # Check if Chrome/Chromium is available
    if command -v google-chrome &> /dev/null || command -v chromium-browser &> /dev/null || command -v chrome &> /dev/null; then
        log_success "Chrome browser is available"
    else
        log_warning "Chrome browser not found. Installing webdriver-manager..."
        pip install webdriver-manager
    fi
    
    log_success "Frontend environment check completed"
}

# Function to run frontend tests
run_frontend_tests() {
    log "Running Queue-it frontend tests..."
    
    cd "$PROJECT_DIR"
    
    # Run frontend tests with detailed output
    python -m pytest tests/frontend/ \
        -v \
        --html="$REPORTS_DIR/frontend_report.html" \
        --json-report --json-report-file="$REPORTS_DIR/frontend_report.json" \
        --tb=short \
        --durations=10 \
        --capture=no \
        --color=yes
    
    if [ $? -eq 0 ]; then
        log_success "Frontend tests completed successfully"
        return 0
    else
        log_error "Frontend tests failed"
        return 1
    fi
}

# Function to run specific frontend test categories
run_test_category() {
    local category=$1
    log "Running frontend $category tests..."
    
    cd "$PROJECT_DIR"
    
    case $category in
        "service")
            python -m pytest tests/frontend/test_queueit_frontend.py::TestQueueItServiceInitialization -v
            ;;
        "trigger")
            python -m pytest tests/frontend/test_queueit_frontend.py::TestQueueItTriggerDetection -v
            ;;
        "overlay")
            python -m pytest tests/frontend/test_queueit_frontend.py::TestQueueItOverlay -v
            ;;
        "indicator")
            python -m pytest tests/frontend/test_queueit_frontend.py::TestQueueItIndicator -v
            ;;
        "token")
            python -m pytest tests/frontend/test_queueit_frontend.py::TestQueueItTokenManagement -v
            ;;
        "error")
            python -m pytest tests/frontend/test_queueit_frontend.py::TestQueueItErrorHandling -v
            ;;
        "mobile")
            python -m pytest tests/frontend/test_queueit_frontend.py::TestQueueItMobileResponsiveness -v
            ;;
        "browser")
            python -m pytest tests/frontend/test_queueit_frontend.py::TestQueueItCrossBrowser -v
            ;;
        *)
            log_error "Unknown test category: $category"
            return 1
            ;;
    esac
}

# Function to generate frontend test report
generate_frontend_report() {
    log "Generating frontend test report..."
    
    # Check if report files exist
    if [ -f "$REPORTS_DIR/frontend_report.json" ]; then
        # Extract test statistics
        total_tests=$(python3 -c "
import json
with open('$REPORTS_DIR/frontend_report.json') as f:
    data = json.load(f)
    summary = data.get('summary', {})
    print(summary.get('total', 0))
" 2>/dev/null || echo "0")
        
        passed_tests=$(python3 -c "
import json
with open('$REPORTS_DIR/frontend_report.json') as f:
    data = json.load(f)
    summary = data.get('summary', {})
    print(summary.get('passed', 0))
" 2>/dev/null || echo "0")
        
        failed_tests=$(python3 -c "
import json
with open('$REPORTS_DIR/frontend_report.json') as f:
    data = json.load(f)
    summary = data.get('summary', {})
    print(summary.get('failed', 0))
" 2>/dev/null || echo "0")
        
        # Calculate success rate
        if [ $total_tests -gt 0 ]; then
            success_rate=$(echo "scale=1; $passed_tests * 100 / $total_tests" | bc -l 2>/dev/null || echo "0")
        else
            success_rate=0
        fi
        
        # Display summary
        echo
        echo "=========================================="
        echo "        FRONTEND TEST RESULTS             "
        echo "=========================================="
        echo "Total Tests: $total_tests"
        echo "Passed: $passed_tests"
        echo "Failed: $failed_tests"
        echo "Success Rate: ${success_rate}%"
        echo "=========================================="
        echo
        echo "Reports generated in: $REPORTS_DIR"
        echo "HTML Report: $REPORTS_DIR/frontend_report.html"
        echo "JSON Report: $REPORTS_DIR/frontend_report.json"
        echo "Log file: $LOG_FILE"
        echo
        
        # Return appropriate exit code
        if [ $failed_tests -gt 0 ]; then
            log_warning "Some frontend tests failed. Check the reports for details."
            return 1
        else
            log_success "All frontend tests passed successfully!"
            return 0
        fi
    else
        log_error "Frontend test report not found"
        return 1
    fi
}

# Function to cleanup
cleanup() {
    log "Cleaning up frontend test environment..."
    
    # Deactivate virtual environment
    if [ -n "$VIRTUAL_ENV" ]; then
        deactivate
    fi
    
    log_success "Cleanup completed"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo
    echo "Options:"
    echo "  -h, --help              Show this help message"
    echo "  -c, --category CATEGORY Run specific test category"
    echo "  -v, --verbose           Enable verbose output"
    echo "  --no-setup              Skip environment setup"
    echo
    echo "Test Categories:"
    echo "  service                 Queue-it service initialization tests"
    echo "  trigger                 Queue trigger detection tests"
    echo "  overlay                 Queue overlay functionality tests"
    echo "  indicator               Queue indicator tests"
    echo "  token                   Token management tests"
    echo "  error                   Error handling tests"
    echo "  mobile                  Mobile responsiveness tests"
    echo "  browser                 Cross-browser compatibility tests"
    echo
    echo "Examples:"
    echo "  $0                      Run all frontend tests"
    echo "  $0 -c mobile            Run only mobile tests"
    echo "  $0 -c overlay -v        Run overlay tests with verbose output"
}

# Main execution
main() {
    # Parse command line arguments
    CATEGORY=""
    VERBOSE=false
    SKIP_SETUP=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_usage
                exit 0
                ;;
            -c|--category)
                CATEGORY="$2"
                shift 2
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            --no-setup)
                SKIP_SETUP=true
                shift
                ;;
            *)
                log_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    echo "=========================================="
    echo "    Queue-it Frontend Test Suite          "
    echo "=========================================="
    echo
    
    # Set up trap for cleanup
    trap cleanup EXIT
    
    # Check prerequisites (unless skipped)
    if [ "$SKIP_SETUP" = false ]; then
        check_prerequisites
        check_frontend_environment
    fi
    
    # Run tests
    if [ -n "$CATEGORY" ]; then
        log "Running specific test category: $CATEGORY"
        if run_test_category "$CATEGORY"; then
            log_success "Category '$CATEGORY' tests completed"
        else
            log_error "Category '$CATEGORY' tests failed"
            exit 1
        fi
    else
        log "Running all frontend tests..."
        if run_frontend_tests; then
            log_success "All frontend tests completed successfully"
        else
            log_error "Frontend tests failed"
            exit 1
        fi
    fi
    
    # Generate report
    generate_frontend_report
    
    echo
    echo "=========================================="
    echo "        FRONTEND TESTS COMPLETED          "
    echo "=========================================="
    echo
}

# Run main function
main "$@" 