#!/bin/bash

# Setup Grafana Connection with Prometheus
# This script sets up the Prometheus data source and imports Queue-It dashboards

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

echo "🔗 Setting up Grafana Connection with Prometheus"
echo "================================================"
echo ""

print_step "1. Checking Grafana Status"
if curl -s http://localhost:3000/api/health >/dev/null 2>&1; then
    print_success "Grafana is running"
else
    print_error "Grafana is not running. Please start Grafana first."
    exit 1
fi

print_step "2. Checking Prometheus Status"
if curl -s http://localhost:9090/api/v1/status/config >/dev/null 2>&1; then
    print_success "Prometheus is running"
else
    print_error "Prometheus is not running. Please start Prometheus first."
    exit 1
fi

print_step "3. Creating Prometheus Data Source Configuration"
mkdir -p config/grafana_datasources

cat > config/grafana_datasources/prometheus.yml << 'EOF'
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
    jsonData:
      timeInterval: "15s"
      queryTimeout: "60s"
      httpMethod: "POST"
EOF

print_success "Created Prometheus data source configuration"

print_step "4. Creating Dashboard Configuration"
mkdir -p config/grafana_dashboards

cat > config/grafana_dashboards/dashboard.yml << 'EOF'
apiVersion: 1

providers:
  - name: 'Queue-It Dashboards'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    allowUiUpdates: true
    options:
      path: /etc/grafana/provisioning/dashboards
EOF

print_success "Created dashboard configuration"

print_step "5. Manual Setup Instructions"
echo ""
print_header "📋 Manual Grafana Setup Required"
echo ""

print_info "Since we can't automatically configure Grafana via API, please follow these steps:"
echo ""

echo "1. 🌐 Open Grafana in your browser:"
echo "   http://localhost:3000"
echo ""

echo "2. 🔐 Login with default credentials:"
echo "   Username: admin"
echo "   Password: admin"
echo "   (You may be prompted to change the password)"
echo ""

echo "3. 📊 Add Prometheus Data Source:"
echo "   - Go to Configuration (gear icon) → Data Sources"
echo "   - Click 'Add data source'"
echo "   - Select 'Prometheus'"
echo "   - Set URL to: http://prometheus:9090"
echo "   - Click 'Save & Test'"
echo ""

echo "4. 📈 Import Queue-It Dashboards:"
echo "   - Go to Dashboards → Import"
echo "   - Import each dashboard file:"
echo ""

print_info "Dashboard Files to Import:"
echo "   📊 config/grafana_dashboards/queueit-comprehensive-dashboard.json"
echo "   🔧 config/grafana_dashboards/queueit-api-performance.json"
echo "   🎨 config/grafana_dashboards/queueit-frontend-metrics.json"
echo "   ⚡ config/grafana_dashboards/queueit-load-testing.json"
echo ""

print_step "6. Alternative: Use Docker Compose with Pre-configured Grafana"
echo ""
print_info "If you want automatic setup, use the Docker Compose approach:"
echo ""

cat > docker-compose-grafana-setup.yml << 'EOF'
version: '3.8'

services:
  prometheus:
    image: prom/prometheus:latest
    container_name: queueit-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=200h'
      - '--web.enable-lifecycle'
    restart: unless-stopped

  grafana:
    image: grafana/grafana:latest
    container_name: queueit-grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin123
      - GF_USERS_ALLOW_SIGN_UP=false
    volumes:
      - grafana_data:/var/lib/grafana
      - ./config/grafana_dashboards:/etc/grafana/provisioning/dashboards
      - ./config/grafana_datasources:/etc/grafana/provisioning/datasources
    restart: unless-stopped
    depends_on:
      - prometheus

volumes:
  prometheus_data:
  grafana_data:
EOF

print_success "Created docker-compose-grafana-setup.yml"

print_step "7. Quick Setup Commands"
echo ""
print_info "To use the pre-configured setup:"
echo ""

echo "1. Stop current Grafana/Prometheus:"
echo "   docker stop queueit-grafana queueit-prometheus"
echo ""

echo "2. Start with pre-configured setup:"
echo "   docker-compose -f docker-compose-grafana-setup.yml up -d"
echo ""

echo "3. Access Grafana:"
echo "   http://localhost:3000"
echo "   Username: admin"
echo "   Password: admin123"
echo ""

print_step "8. Test Data Generation"
echo ""
print_info "To see metrics in the dashboards, run Queue-It tests:"
echo ""

echo "🧪 Run tests to generate metrics:"
echo "   ./test.sh"
echo "   ./run_queueit_tests.sh"
echo "   python3 simple_functional_test.py"
echo ""

print_success "Setup instructions complete!"
echo ""
print_info "Choose your preferred method:"
echo "   📋 Manual setup (steps 1-4 above)"
echo "   🐳 Docker Compose setup (steps 6-7 above)"
echo ""
print_info "After setup, run tests to see live metrics in Grafana!" 