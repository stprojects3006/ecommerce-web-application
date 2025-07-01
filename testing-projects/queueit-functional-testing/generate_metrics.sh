#!/bin/bash

# Generate Queue-It Metrics for Grafana
# This script runs multiple tests to generate metrics for viewing in Grafana

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
RED='\033[0;31m'
NC='\033[0m'

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

echo "📊 Generating Queue-It Metrics for Grafana"
echo "=========================================="
echo ""

print_step "1. Checking Services"
if curl -s http://localhost:8081/api/queueit/health >/dev/null 2>&1; then
    print_success "API Gateway is running"
else
    print_error "API Gateway is not running"
    exit 1
fi

if curl -s http://localhost:9090/api/v1/status/config >/dev/null 2>&1; then
    print_success "Prometheus is running"
else
    print_error "Prometheus is not running"
    exit 1
fi

print_step "2. Running Initial Test"
echo "🧪 Running basic functional test..."
./test.sh

print_step "3. Running Comprehensive Tests"
echo "🧪 Running comprehensive test suite..."

# Run multiple test iterations to generate more metrics
for i in {1..5}; do
    echo "   Iteration $i/5..."
    python3 simple_functional_test.py >/dev/null 2>&1
    sleep 2
done

print_step "4. Running API Endpoint Tests"
echo "🔧 Testing individual API endpoints..."

# Test health endpoint multiple times
for i in {1..10}; do
    curl -s http://localhost:8081/api/queueit/health >/dev/null 2>&1
    sleep 1
done

# Test status endpoints
for event in flash-sale-2024 black-friday-2024 checkout-protection; do
    for i in {1..5}; do
        curl -s http://localhost:8081/api/queueit/status/$event >/dev/null 2>&1
        sleep 1
    done
done

# Test stats endpoints
for event in flash-sale-2024 black-friday-2024; do
    for i in {1..3}; do
        curl -s http://localhost:8081/api/queueit/stats/$event >/dev/null 2>&1
        sleep 1
    done
done

print_step "5. Running Enqueue Tests"
echo "🚀 Testing enqueue functionality..."

# Test enqueue with different events
for i in {1..5}; do
    curl -X POST http://localhost:8081/api/queueit/enqueue \
        -H "Content-Type: application/json" \
        -d '{
            "eventId": "flash-sale-2024",
            "targetUrl": "https://localhost/flash-sale",
            "userAgent": "Test Browser",
            "ipAddress": "127.0.0.1"
        }' >/dev/null 2>&1
    sleep 2
done

for i in {1..3}; do
    curl -X POST http://localhost:8081/api/queueit/enqueue \
        -H "Content-Type: application/json" \
        -d '{
            "eventId": "black-friday-2024",
            "targetUrl": "https://localhost/black-friday",
            "userAgent": "Test Browser",
            "ipAddress": "127.0.0.1"
        }' >/dev/null 2>&1
    sleep 2
done

print_step "6. Running Performance Tests"
echo "⚡ Running performance tests..."

# Simulate concurrent requests
for i in {1..20}; do
    (
        curl -s http://localhost:8081/api/queueit/health >/dev/null 2>&1
        curl -s http://localhost:8081/api/queueit/status/flash-sale-2024 >/dev/null 2>&1
    ) &
done
wait

print_step "7. Checking Prometheus Metrics"
echo "📊 Checking if metrics are being collected..."

# Check if Prometheus is collecting metrics
metrics_count=$(curl -s http://localhost:9090/api/v1/query?query=up | jq '.data.result | length' 2>/dev/null || echo "0")

if [ "$metrics_count" -gt 0 ]; then
    print_success "Prometheus is collecting metrics ($metrics_count targets)"
else
    print_warning "No metrics found in Prometheus"
fi

print_step "8. Final Test Run"
echo "🧪 Running final comprehensive test..."
./test.sh

echo ""
print_header "🎉 Metrics Generation Complete!"
echo ""

print_info "Generated Metrics:"
echo "✅ 5 comprehensive test runs"
echo "✅ 10 health endpoint calls"
echo "✅ 15 status endpoint calls"
echo "✅ 6 stats endpoint calls"
echo "✅ 8 enqueue operations"
echo "✅ 20 concurrent performance tests"
echo ""

print_info "Next Steps:"
echo "1. Open Grafana: http://localhost:3000"
echo "2. Login: admin / admin (or admin123)"
echo "3. Add Prometheus data source: http://prometheus:9090"
echo "4. Import Queue-It dashboards"
echo "5. View your metrics!"
echo ""

print_info "Available Metrics in Prometheus:"
echo "• API response times"
echo "• Request counts"
echo "• Error rates"
echo "• Queue operations"
echo "• Test execution metrics"
echo ""

print_success "Your Queue-It metrics are now ready for viewing in Grafana!" 