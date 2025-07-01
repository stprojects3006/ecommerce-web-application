#!/bin/bash

# Queue-It Grafana Reports Viewer
# Quick script to show testing reports in Grafana

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

echo "📊 Queue-It Testing Reports in Grafana"
echo "======================================"
echo ""

# Check if Grafana is running
print_step "Checking Grafana Status..."
if curl -s http://localhost:3000/api/health >/dev/null 2>&1; then
    print_success "Grafana is running!"
else
    print_warning "Grafana is not running. Starting it now..."
    
    # Check if setup script exists
    if [ -f "setup_grafana_dashboard.sh" ]; then
        print_info "Running Grafana setup..."
        ./setup_grafana_dashboard.sh
    else
        print_error "Grafana setup script not found. Please run setup_grafana_dashboard.sh first."
        exit 1
    fi
fi

echo ""
print_header "🎉 Access Your Queue-It Testing Reports"
echo ""

print_info "🌐 Grafana Dashboard URL:"
echo "   http://localhost:3000"
echo ""

print_info "👤 Login Credentials:"
echo "   Username: admin"
echo "   Password: admin123"
echo ""

print_info "📊 Available Queue-It Dashboards:"
echo ""

echo "┌─────────────────────────────────────────────────────────┐"
echo "│                Queue-It Dashboards                     │"
echo "├─────────────────────────────────────────────────────────┤"
echo "│                                                         │"
echo "│  📊 Queue-It Comprehensive Testing Dashboard           │"
echo "│     • Complete overview of all metrics                 │"
echo "│     • Test execution timeline                          │"
echo "│     • Performance trends                               │"
echo "│     • Error rates and alerts                           │"
echo "│                                                         │"
echo "│  🔧 Queue-It API Performance                           │"
echo "│     • API response times                               │"
echo "│     • Throughput metrics                               │"
echo "│     • Success rates                                    │
echo "│     • Error rates by endpoint                          │"
echo "│                                                         │"
echo "│  🎨 Queue-It Frontend Metrics                          │"
echo "│     • Queue trigger rates                              │"
echo "│     • Queue sizes and wait times                       │
echo "│     • User experience metrics                          │
echo "│     • Frontend interactions                            │
echo "│                                                         │"
echo "│  ⚡ Queue-It Load Testing                               │
echo "│     • Load test results                                │
echo "│     • Performance under stress                         │
echo "│     • Concurrent user metrics                          │
echo "│     • Resource utilization                             │
echo "│                                                         │"
echo "└─────────────────────────────────────────────────────────┘"
echo ""

print_step "How to View Reports:"
echo "1. Open http://localhost:3000 in your browser"
echo "2. Login with admin/admin123"
echo "3. Click 'Dashboards' in the left sidebar"
echo "4. Select a Queue-It dashboard"
echo "5. Run tests to see live metrics"
echo ""

print_step "Generate Test Data:"
echo "To see metrics in the dashboards, run Queue-It tests:"
echo ""

print_info "Quick Test Commands:"
echo "  ./test.sh                    # Run quick functional tests"
echo "  ./run_queueit_tests.sh       # Run comprehensive tests"
echo "  python3 simple_functional_test.py  # Run Python tests"
echo ""

print_step "Dashboard Features:"
echo "✅ Real-time metrics updates"
echo "✅ Interactive charts and graphs"
echo "✅ Performance thresholds and alerts"
echo "✅ Historical data trends"
echo "✅ Export capabilities"
echo "✅ Mobile-responsive design"
echo ""

print_info "Key Metrics You'll See:"
echo "• Test execution rates and success rates"
echo "• API response times and throughput"
echo "• Queue sizes and wait times"
echo "• User interaction patterns"
echo "• Error rates and performance alerts"
echo "• Load testing results"
echo ""

print_warning "Note: Metrics will appear once you run Queue-It tests"
print_warning "and the API Gateway is configured to expose Prometheus metrics."
echo ""

print_success "Ready to view your Queue-It testing reports!"
echo ""
echo "🔗 Quick Links:"
echo "   Grafana: http://localhost:3000"
echo "   API Health: http://localhost:8081/api/queueit/health"
echo "   Test Script: ./test.sh"
echo ""

# Open Grafana in browser if possible
if command -v open &> /dev/null; then
    print_info "Opening Grafana in your browser..."
    open http://localhost:3000
elif command -v xdg-open &> /dev/null; then
    print_info "Opening Grafana in your browser..."
    xdg-open http://localhost:3000
else
    print_info "Please manually open: http://localhost:3000"
fi 