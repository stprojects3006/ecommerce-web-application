#!/bin/bash

# Queue-It Grafana Dashboard Setup Script
# This script sets up Grafana to view Queue-It testing reports

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

echo "📊 Queue-It Grafana Dashboard Setup"
echo "==================================="
echo ""

print_step "1. Checking Docker and Docker Compose"
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

print_success "Docker and Docker Compose are available"

print_step "2. Creating Grafana Docker Compose Configuration"
cat > docker-compose-grafana.yml << 'EOF'
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

print_success "Created docker-compose-grafana.yml"

print_step "3. Creating Prometheus Configuration"
cat > prometheus.yml << 'EOF'
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "queueit_rules.yml"

scrape_configs:
  - job_name: 'queueit-api-gateway'
    static_configs:
      - targets: ['host.docker.internal:8081']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s

  - job_name: 'queueit-frontend'
    static_configs:
      - targets: ['host.docker.internal:443']
    metrics_path: '/metrics'
    scrape_interval: 5s

  - job_name: 'queueit-testing'
    static_configs:
      - targets: ['host.docker.internal:8081']
    metrics_path: '/api/queueit/metrics'
    scrape_interval: 10s
EOF

print_success "Created prometheus.yml"

print_step "4. Creating Grafana Data Source Configuration"
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
EOF

print_success "Created Grafana data source configuration"

print_step "5. Creating Grafana Dashboard Configuration"
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

print_success "Created Grafana dashboard configuration"

print_step "6. Starting Grafana and Prometheus"
docker-compose -f docker-compose-grafana.yml up -d

print_success "Grafana and Prometheus started"

print_step "7. Waiting for services to be ready"
echo "⏳ Waiting for Grafana to be ready..."
sleep 10

# Check if Grafana is ready
max_attempts=30
attempt=1
while [ $attempt -le $max_attempts ]; do
    if curl -s http://localhost:3000/api/health >/dev/null 2>&1; then
        print_success "Grafana is ready!"
        break
    fi
    
    echo "   Attempt $attempt/$max_attempts..."
    sleep 2
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    print_error "Grafana failed to start"
    exit 1
fi

print_step "8. Importing Queue-It Dashboards"
echo "📊 Available Dashboards:"
echo ""

# Import API Performance Dashboard
echo "🔧 Importing API Performance Dashboard..."
curl -X POST http://admin:admin123@localhost:3000/api/dashboards/db \
  -H "Content-Type: application/json" \
  -d @config/grafana_dashboards/queueit-api-performance.json >/dev/null 2>&1

# Import Frontend Metrics Dashboard
echo "🎨 Importing Frontend Metrics Dashboard..."
curl -X POST http://admin:admin123@localhost:3000/api/dashboards/db \
  -H "Content-Type: application/json" \
  -d @config/grafana_dashboards/queueit-frontend-metrics.json >/dev/null 2>&1

# Import Load Testing Dashboard
echo "⚡ Importing Load Testing Dashboard..."
curl -X POST http://admin:admin123@localhost:3000/api/dashboards/db \
  -H "Content-Type: application/json" \
  -d @config/grafana_dashboards/queueit-load-testing.json >/dev/null 2>&1

print_success "All dashboards imported successfully!"

echo ""
print_header "🎉 Grafana Dashboard Setup Complete!"
echo ""

print_info "Access Your Dashboards:"
echo "🌐 Grafana URL: http://localhost:3000"
echo "👤 Username: admin"
echo "🔑 Password: admin123"
echo ""

print_info "Available Queue-It Dashboards:"
echo "📊 Queue-it API Performance"
echo "   - API response times"
echo "   - Throughput metrics"
echo "   - Error rates"
echo "   - Success rates"
echo ""

echo "🎨 Queue-it Frontend Metrics"
echo "   - Queue trigger rates"
echo "   - Queue sizes"
echo "   - Wait times"
echo "   - User experience metrics"
echo ""

echo "⚡ Queue-it Load Testing"
echo "   - Load test results"
echo "   - Performance under stress"
echo "   - Concurrent user metrics"
echo "   - Resource utilization"
echo ""

print_step "9. Running Test to Generate Metrics"
echo "🧪 Running Queue-It tests to generate metrics..."
./test.sh

print_info "Test completed! Check Grafana for real-time metrics."
echo ""

print_step "10. Next Steps"
echo "1. Open http://localhost:3000 in your browser"
echo "2. Login with admin/admin123"
echo "3. Navigate to Dashboards"
echo "4. Select a Queue-It dashboard"
echo "5. Run more tests to see live metrics"
echo ""

print_warning "Note: Metrics will appear once your Queue-It tests are running"
print_warning "and the API Gateway is configured to expose Prometheus metrics."
echo ""

print_success "Setup complete! Your Queue-It testing reports are ready in Grafana." 