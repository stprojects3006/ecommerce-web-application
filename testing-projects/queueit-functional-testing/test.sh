#!/bin/bash

# Quick Queue-It Test Script
# Simple one-liner to test Queue-It integration

echo "🧪 Queue-It Quick Test"
echo "======================"

# Activate virtual environment if it exists
if [ -d "venv" ]; then
    source venv/bin/activate
    echo "✅ Using virtual environment"
fi

# Check if API Gateway is running
if curl -s http://localhost:8081/api/queueit/health >/dev/null 2>&1; then
    echo "✅ API Gateway is running"
    
    # Run the test
    python3 simple_functional_test.py
    
    # Show results
    if [ -f "simple_test_results.json" ]; then
        echo ""
        echo "📊 Quick Results Summary:"
        echo "=========================="
        python3 -c "
import json
with open('simple_test_results.json', 'r') as f:
    data = json.load(f)
print(f'Status: {data.get(\"status\")}')
print(f'Tests Passed: {data.get(\"passed\")}/{data.get(\"total\")}')
print(f'Success Rate: {data.get(\"success_rate\")}')
"
    fi
else
    echo "❌ API Gateway is not running"
    echo "Please start the API Gateway first:"
    echo "cd ../../microservice-backend/api-gateway && ./mvnw spring-boot:run"
    exit 1
fi 