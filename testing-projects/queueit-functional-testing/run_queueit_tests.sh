#!/bin/bash

# Queue-It Functional Test Runner
# Run this script to execute Queue-It tests and see reports

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_header() {
    echo -e "${PURPLE}$1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️ $1${NC}"
}

print_step() {
    echo -e "${CYAN}🔍 $1${NC}"
}

# Function to check if a service is running
check_service() {
    local url=$1
    local service_name=$2
    
    if curl -s "$url" >/dev/null 2>&1; then
        print_success "$service_name is running"
        return 0
    else
        print_error "$service_name is not running"
        return 1
    fi
}

# Function to display test results
display_results() {
    local results_file=$1
    
    if [ -f "$results_file" ]; then
        echo ""
        print_header "📊 DETAILED TEST RESULTS"
        echo "=================================================="
        
        # Parse JSON and display results
        python3 -c "
import json
import sys

try:
    with open('$results_file', 'r') as f:
        data = json.load(f)
    
    print(f'Timestamp: {data.get(\"timestamp\", \"N/A\")}')
    print(f'Status: {data.get(\"status\", \"N/A\")}')
    print(f'Tests Passed: {data.get(\"passed\", 0)}/{data.get(\"total\", 0)}')
    print(f'Success Rate: {data.get(\"success_rate\", \"N/A\")}')
    print()
    print('Individual Test Results:')
    print('-' * 50)
    
    for result in data.get('results', []):
        status = result.get('status', 'UNKNOWN')
        test = result.get('test', 'Unknown Test')
        details = result.get('details', '')
        
        if status == 'PASS':
            print(f'✅ {test}: PASS')
        elif status == 'FAIL':
            print(f'❌ {test}: FAIL')
        else:
            print(f'⚠️ {test}: {status}')
        
        if details:
            print(f'   Details: {details}')
        print()
        
except Exception as e:
    print(f'Error reading results: {e}')
    sys.exit(1)
"
    else
        print_error "Results file not found: $results_file"
    fi
}

# Function to run quick health check
quick_health_check() {
    print_step "Running quick health check..."
    
    # Check API Gateway
    if check_service "http://localhost:8081/api/queueit/health" "API Gateway"; then
        # Get Queue-It health info
        response=$(curl -s http://localhost:8081/api/queueit/health)
        customer_id=$(echo "$response" | python3 -c "import sys, json; print(json.load(sys.stdin).get('customerId', 'N/A'))")
        service_status=$(echo "$response" | python3 -c "import sys, json; print(json.load(sys.stdin).get('status', 'N/A'))")
        
        print_success "Queue-It Integration Status:"
        echo "   Customer ID: $customer_id"
        echo "   Status: $service_status"
        return 0
    else
        return 1
    fi
}

# Function to run comprehensive tests
run_comprehensive_tests() {
    print_step "Running comprehensive Queue-It tests..."
    
    if python3 simple_functional_test.py; then
        print_success "Comprehensive tests completed successfully!"
        return 0
    else
        print_error "Comprehensive tests failed!"
        return 1
    fi
}

# Function to run individual test categories
run_test_category() {
    local category=$1
    
    case $category in
        "health")
            print_step "Running health check tests..."
            python3 -c "
import requests
import json

try:
    response = requests.get('http://localhost:8081/api/queueit/health', timeout=5)
    if response.status_code == 200:
        data = response.json()
        print('✅ Health Check: PASS')
        print(f'   Service: {data.get(\"service\")}')
        print(f'   Customer ID: {data.get(\"customerId\")}')
        print(f'   Status: {data.get(\"status\")}')
    else:
        print(f'❌ Health Check: FAIL (HTTP {response.status_code})')
except Exception as e:
    print(f'❌ Health Check: FAIL ({str(e)})')
"
            ;;
        "status")
            print_step "Running queue status tests..."
            python3 -c "
import requests
import json

events = ['flash-sale-2024', 'black-friday-2024', 'checkout-protection']
for event_id in events:
    try:
        response = requests.get(f'http://localhost:8081/api/queueit/status/{event_id}', timeout=5)
        if response.status_code == 200:
            data = response.json()
            print(f'✅ {event_id}: PASS (Active: {data.get(\"isActive\", False)})')
        else:
            print(f'❌ {event_id}: FAIL (HTTP {response.status_code})')
    except Exception as e:
        print(f'❌ {event_id}: FAIL ({str(e)})')
"
            ;;
        "enqueue")
            print_step "Running enqueue tests..."
            python3 -c "
import requests
import json

test_data = {
    'eventId': 'flash-sale-2024',
    'targetUrl': 'https://localhost/flash-sale',
    'userAgent': 'Test Browser',
    'ipAddress': '127.0.0.1'
}

try:
    response = requests.post('http://localhost:8081/api/queueit/enqueue', json=test_data, timeout=5)
    if response.status_code == 200:
        data = response.json()
        print('✅ Enqueue Test: PASS')
        print(f'   Event ID: {data.get(\"eventId\")}')
        print(f'   Redirect URL: {data.get(\"redirectUrl\", \"N/A\")[:50]}...')
    else:
        print(f'❌ Enqueue Test: FAIL (HTTP {response.status_code})')
except Exception as e:
    print(f'❌ Enqueue Test: FAIL ({str(e)})')
"
            ;;
        *)
            print_error "Unknown test category: $category"
            return 1
            ;;
    esac
}

# Function to show help
show_help() {
    echo "Queue-It Functional Test Runner"
    echo "================================"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -h, --help              Show this help message"
    echo "  -q, --quick             Run quick health check only"
    echo "  -c, --comprehensive     Run comprehensive tests (default)"
    echo "  -t, --test CATEGORY     Run specific test category"
    echo "  -r, --report            Show last test results"
    echo "  -a, --all               Run all tests and show detailed report"
    echo ""
    echo "Test Categories:"
    echo "  health                  Health check tests"
    echo "  status                  Queue status tests"
    echo "  enqueue                 Enqueue functionality tests"
    echo ""
    echo "Examples:"
    echo "  $0                      Run comprehensive tests"
    echo "  $0 -q                   Quick health check"
    echo "  $0 -t health            Run health tests only"
    echo "  $0 -a                   Run all tests with detailed report"
    echo "  $0 -r                   Show last test results"
}

# Main script logic
main() {
    print_header "🚀 Queue-It Functional Test Runner"
    echo "=================================================="
    
    # Check if Python is available
    if ! command -v python3 &> /dev/null; then
        print_error "Python 3 is required but not installed"
        exit 1
    fi
    
    # Check if requests module is available
    if ! python3 -c "import requests" &> /dev/null; then
        print_error "Python requests module is required. Install with: pip install requests"
        exit 1
    fi
    
    # Parse command line arguments
    case "${1:-comprehensive}" in
        -h|--help)
            show_help
            exit 0
            ;;
        -q|--quick)
            print_info "Running quick health check..."
            if quick_health_check; then
                print_success "Quick health check completed!"
                exit 0
            else
                print_error "Quick health check failed!"
                exit 1
            fi
            ;;
        -c|--comprehensive)
            print_info "Running comprehensive tests..."
            if run_comprehensive_tests; then
                display_results "simple_test_results.json"
                exit 0
            else
                exit 1
            fi
            ;;
        -t|--test)
            if [ -z "$2" ]; then
                print_error "Test category required. Use -h for help."
                exit 1
            fi
            print_info "Running test category: $2"
            run_test_category "$2"
            ;;
        -r|--report)
            print_info "Showing last test results..."
            display_results "simple_test_results.json"
            ;;
        -a|--all)
            print_info "Running all tests with detailed report..."
            if run_comprehensive_tests; then
                display_results "simple_test_results.json"
                print_success "All tests completed successfully!"
                exit 0
            else
                print_error "Some tests failed!"
                exit 1
            fi
            ;;
        *)
            print_info "Running comprehensive tests (default)..."
            if run_comprehensive_tests; then
                display_results "simple_test_results.json"
                exit 0
            else
                exit 1
            fi
            ;;
    esac
}

# Run main function with all arguments
main "$@" 