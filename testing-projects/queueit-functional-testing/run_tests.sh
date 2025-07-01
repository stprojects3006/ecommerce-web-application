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

if ! curl -s "http://localhost:3000" >/dev/null 2>&1; then
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
