#!/bin/bash

# Queue-It Testing Environment Setup Script

set -e

echo "🔧 Setting up Queue-It Testing Environment"
echo "=========================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

# Check if Python 3 is available
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is required but not installed"
    exit 1
fi

print_success "Python 3 found: $(python3 --version)"

# Create virtual environment if it doesn't exist
if [ ! -d "venv" ]; then
    print_info "Creating virtual environment..."
    python3 -m venv venv
    print_success "Virtual environment created"
else
    print_success "Virtual environment already exists"
fi

# Activate virtual environment
print_info "Activating virtual environment..."
source venv/bin/activate

# Install required packages
print_info "Installing required packages..."
pip install requests

print_success "Dependencies installed"

# Make scripts executable
print_info "Making scripts executable..."
chmod +x test.sh
chmod +x run_queueit_tests.sh

print_success "Scripts made executable"

# Check if API Gateway is running
print_info "Checking API Gateway status..."
if curl -s http://localhost:8081/api/queueit/health >/dev/null 2>&1; then
    print_success "API Gateway is running"
else
    print_warning "API Gateway is not running"
    echo ""
    echo "To start the API Gateway:"
    echo "cd ../../microservice-backend/api-gateway"
    echo "./mvnw spring-boot:run"
    echo ""
fi

echo ""
print_success "Setup completed successfully!"
echo ""
echo "🚀 Quick Start Commands:"
echo "========================"
echo "  ./test.sh                    # Run quick test"
echo "  ./run_queueit_tests.sh       # Run comprehensive tests"
echo "  ./run_queueit_tests.sh -h    # Show all options"
echo ""
echo "📚 For more information, see README.md" 