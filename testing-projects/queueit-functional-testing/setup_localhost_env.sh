#!/bin/bash

# Queue-It Localhost Environment Setup Script
# This script helps set up the environment for Queue-It testing

set -e

echo "🚀 Queue-It Localhost Environment Setup"
echo "======================================="

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

# Function to check if a port is in use
port_in_use() {
    lsof -i :$1 >/dev/null 2>&1
}

print_status "Checking prerequisites..."

# Check Java
if command_exists java; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    print_success "Java found: $JAVA_VERSION"
else
    print_error "Java not found. Please install Java 17 or later."
    exit 1
fi

# Check Node.js
if command_exists node; then
    NODE_VERSION=$(node --version)
    print_success "Node.js found: $NODE_VERSION"
else
    print_warning "Node.js not found. Frontend tests will be skipped."
fi

# Check Maven
if command_exists mvn; then
    print_success "Maven found"
else
    print_warning "Maven not found. Using Maven wrapper."
fi

# Check if ports are available
print_status "Checking port availability..."

if port_in_use 8081; then
    print_warning "Port 8081 is in use. Backend service might already be running."
else
    print_success "Port 8081 is available"
fi

if port_in_use 3000; then
    print_warning "Port 3000 is in use. Frontend service might already be running."
else
    print_success "Port 3000 is available"
fi

# Create environment file template
print_status "Creating environment configuration..."

cat > .env.template << 'EOF'
# Queue-It Configuration Template
# Copy this file to .env and fill in your actual credentials

# Queue-It Credentials (Get these from https://go.queue-it.net/)
QUEUEIT_CUSTOMER_ID=your-customer-id
QUEUEIT_SECRET_KEY=your-secret-key-here
QUEUEIT_API_KEY=your-api-key-here
QUEUEIT_QUEUE_DOMAIN=your-customer-id.queue-it.net

# Test Configuration
BACKEND_URL=http://localhost:8081
FRONTEND_URL=https://localhost
TEST_TIMEOUT=10

# Example values (replace with your actual credentials):
# QUEUEIT_CUSTOMER_ID=futuraforge
# QUEUEIT_SECRET_KEY=12345678-1234-1234-1234-123456789012
# QUEUEIT_API_KEY=87654321-4321-4321-4321-210987654321
# QUEUEIT_QUEUE_DOMAIN=futuraforge.queue-it.net
EOF

print_success "Environment template created: .env.template"

# Create backend configuration template
print_status "Creating backend configuration template..."

cat > backend-config.template.properties << 'EOF'
# Queue-It Backend Configuration Template
# Copy these properties to microservice-backend/api-gateway/src/main/resources/application.properties

# Queue-It Configuration
queueit.customer-id=your-customer-id
queueit.secret-key=your-secret-key-here
queueit.api-key=your-api-key-here
queueit.queue-domain=your-customer-id.queue-it.net

# Queue-It Events
queueit.events.flash-sale=flash-sale-2024
queueit.events.black-friday=black-friday-2024
queueit.events.checkout=checkout-protection
queueit.events.high-traffic=high-traffic-protection

# Example values (replace with your actual credentials):
# queueit.customer-id=futuraforge
# queueit.secret-key=12345678-1234-1234-1234-123456789012
# queueit.api-key=87654321-4321-4321-4321-210987654321
# queueit.queue-domain=futuraforge.queue-it.net
EOF

print_success "Backend configuration template created: backend-config.template.properties"

# Create frontend configuration template
print_status "Creating frontend configuration template..."

cat > frontend-config.template.js << 'EOF'
// Queue-It Frontend Configuration Template
// Copy this to frontend/src/queueit/queueit-config.js

export const queueitConfig = {
    customerId: 'your-customer-id',
    secretKey: 'your-secret-key-here',
    apiKey: 'your-api-key-here',
    queueDomain: 'your-customer-id.queue-it.net',
    
    events: {
        flashSale: {
            eventId: 'flash-sale-2024',
            layoutId: 'your-layout-id',
            culture: 'en-US'
        },
        blackFriday: {
            eventId: 'black-friday-2024',
            layoutId: 'your-layout-id',
            culture: 'en-US'
        },
        checkout: {
            eventId: 'checkout-protection',
            layoutId: 'your-layout-id',
            culture: 'en-US'
        }
    }
};

// Example values (replace with your actual credentials):
// customerId: 'futuraforge',
// secretKey: '12345678-1234-1234-1234-123456789012',
// apiKey: '87654321-4321-4321-4321-210987654321',
// queueDomain: 'futuraforge.queue-it.net',
EOF

print_success "Frontend configuration template created: frontend-config.template.js"

# Create startup script
print_status "Creating startup script..."

cat > start_services.sh << 'EOF'
#!/bin/bash

# Queue-It Services Startup Script
# This script starts all required services for Queue-It testing

set -e

echo "🚀 Starting Queue-It Services"
echo "============================="

# Function to check if a port is in use
port_in_use() {
    lsof -i :$1 >/dev/null 2>&1
}

# Function to wait for service to be ready
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    echo "⏳ Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" >/dev/null 2>&1; then
            echo "✅ $service_name is ready!"
            return 0
        fi
        
        echo "   Attempt $attempt/$max_attempts..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo "❌ $service_name failed to start"
    return 1
}

# Start API Gateway
if ! port_in_use 8081; then
    echo "🔧 Starting API Gateway..."
    cd ../../microservice-backend/api-gateway
    ./mvnw spring-boot:run > api-gateway.log 2>&1 &
    API_GATEWAY_PID=$!
    echo "API Gateway started with PID: $API_GATEWAY_PID"
    
    # Wait for API Gateway to be ready
    wait_for_service "http://localhost:8081/actuator/health" "API Gateway"
else
    echo "⚠️ API Gateway already running on port 8081"
fi

# Start Frontend
if ! port_in_use 3000; then
    echo "🎨 Starting Frontend..."
    cd ../../frontend
    npm start > frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo "Frontend started with PID: $FRONTEND_PID"
    
    # Wait for Frontend to be ready
    wait_for_service "https://localhost" "Frontend"
else
    echo "⚠️ Frontend already running on port 3000"
fi

echo ""
echo "🎯 All services started successfully!"
echo "📊 Service Status:"
echo "   Backend:  http://localhost:8081"
echo "   Frontend: https://localhost"
echo "   Queue-It: http://localhost:8081/api/queueit/health"
echo ""
echo "🧪 Run tests with: python run_localhost_tests.py"
echo ""
echo "🛑 To stop services, run: pkill -f 'spring-boot:run' && pkill -f 'npm start'"
EOF

chmod +x start_services.sh
print_success "Startup script created: start_services.sh"

# Create test runner
print_status "Creating test runner..."

cat > run_tests.sh << 'EOF'
#!/bin/bash

# Queue-It Test Runner
# This script runs all Queue-It tests

set -e

echo "🧪 Queue-It Test Runner"
echo "======================="

# Check if services are running
echo "🔍 Checking service availability..."

if ! curl -s "http://localhost:8081/actuator/health" >/dev/null 2>&1; then
    echo "❌ Backend service not running. Please start services first:"
    echo "   ./start_services.sh"
    exit 1
fi

if ! curl -s "https://localhost" >/dev/null 2>&1; then
    echo "❌ Frontend service not running. Please start services first:"
    echo "   ./start_services.sh"
    exit 1
fi

echo "✅ Services are running"

# Run tests
echo "🚀 Running Queue-It tests..."
python run_localhost_tests.py

echo ""
echo "📊 Test results saved to: localhost_test_results.json"
EOF

chmod +x run_tests.sh
print_success "Test runner created: run_tests.sh"

print_status "Setup complete! Next steps:"
echo ""
echo "1. 📋 Get your Queue-It credentials from https://go.queue-it.net/"
echo "2. 🔧 Configure your credentials:"
echo "   - Copy .env.template to .env and fill in your credentials"
echo "   - Copy backend-config.template.properties to microservice-backend/api-gateway/src/main/resources/application.properties"
echo "   - Copy frontend-config.template.js to frontend/src/queueit/queueit-config.js"
echo "3. 🚀 Start services: ./start_services.sh"
echo "4. 🧪 Run tests: ./run_tests.sh"
echo ""
echo "📖 For detailed instructions, see: LOCALHOST_SETUP_GUIDE.md"

print_success "Queue-It localhost environment setup complete!" 