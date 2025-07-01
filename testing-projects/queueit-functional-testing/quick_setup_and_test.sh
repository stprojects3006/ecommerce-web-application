#!/bin/bash

# Queue-It Official Connector Quick Setup and Test Script
# This script quickly sets up and runs Queue-It official connector tests

set -e

echo "🚀 Queue-It Official Connector Quick Setup and Test"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
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

# Check if we're in the right directory
if [ ! -f "requirements.txt" ]; then
    print_error "Please run this script from the queueit-functional-testing directory"
    exit 1
fi

print_status "Setting up Python environment..."

# Install Python dependencies
if [ ! -d "venv" ]; then
    print_status "Creating virtual environment..."
    python3 -m venv venv
fi

source venv/bin/activate
pip install -r requirements.txt

print_success "Python environment ready"

# Install Node.js dependencies if needed
if command -v node &> /dev/null; then
    print_status "Setting up Node.js environment..."
    
    if [ ! -f "package.json" ]; then
        print_status "Creating package.json for frontend tests..."
        cat > package.json << EOF
{
  "name": "queueit-functional-testing",
  "version": "1.0.0",
  "description": "Queue-It Official Connector Functional Tests",
  "main": "index.js",
  "scripts": {
    "test": "node tests/frontend/test_queueit_frontend_official.js",
    "test:backend": "python tests/backend/test_queueit_official_connector.py",
    "test:all": "python run_official_connector_tests.py"
  },
  "dependencies": {
    "puppeteer": "^21.0.0",
    "assert": "^2.0.0"
  },
  "devDependencies": {},
  "keywords": ["queue-it", "testing", "functional"],
  "author": "Test Suite",
  "license": "MIT"
}
EOF
    fi
    
    npm install
    print_success "Node.js environment ready"
else
    print_warning "Node.js not found - frontend tests will be skipped"
fi

# Create quick test runner
print_status "Creating quick test runner..."

cat > quick_test.py << 'EOF'
#!/usr/bin/env python3
"""
Quick Queue-It Official Connector Test Runner
"""

import requests
import time
import json
from typing import Dict, Any

def test_backend_health():
    """Quick backend health check"""
    try:
        response = requests.get("http://localhost:8081/api/queueit/health", timeout=10)
        if response.status_code == 200:
            data = response.json()
            return {
                'status': 'PASS',
                'connector': data.get('connector', 'unknown'),
                'service': data.get('service', 'unknown')
            }
        else:
            return {'status': 'FAIL', 'error': f'HTTP {response.status_code}'}
    except Exception as e:
        return {'status': 'FAIL', 'error': str(e)}

def test_queue_status():
    """Quick queue status test"""
    try:
        response = requests.get("http://localhost:8081/api/queueit/status/flash-sale-2024", timeout=10)
        if response.status_code == 200:
            data = response.json()
            return {
                'status': 'PASS',
                'event_id': data.get('eventId'),
                'is_active': data.get('isActive'),
                'queue_size': data.get('queueSize')
            }
        else:
            return {'status': 'FAIL', 'error': f'HTTP {response.status_code}'}
    except Exception as e:
        return {'status': 'FAIL', 'error': str(e)}

def test_enqueue():
    """Quick enqueue test"""
    try:
        data = {
            'eventId': 'flash-sale-2024',
            'targetUrl': 'https://localhost/flash-sale',
            'userAgent': 'Test Browser',
            'ipAddress': '127.0.0.1'
        }
        response = requests.post("http://localhost:8081/api/queueit/enqueue", 
                               json=data, timeout=10)
        if response.status_code == 200:
            result = response.json()
            return {
                'status': 'PASS',
                'redirect_url': result.get('redirectUrl', 'N/A'),
                'event_id': result.get('eventId')
            }
        else:
            return {'status': 'FAIL', 'error': f'HTTP {response.status_code}'}
    except Exception as e:
        return {'status': 'FAIL', 'error': str(e)}

def main():
    print("🚀 Quick Queue-It Official Connector Test")
    print("=" * 50)
    
    tests = [
        ('Backend Health', test_backend_health),
        ('Queue Status', test_queue_status),
        ('Enqueue', test_enqueue)
    ]
    
    results = {}
    passed = 0
    total = len(tests)
    
    for test_name, test_func in tests:
        print(f"\n🔍 Testing: {test_name}")
        result = test_func()
        results[test_name] = result
        
        if result['status'] == 'PASS':
            print(f"✅ {test_name}: PASS")
            passed += 1
        else:
            print(f"❌ {test_name}: FAIL - {result.get('error', 'Unknown error')}")
    
    print("\n" + "=" * 50)
    print(f"📊 Results: {passed}/{total} tests passed")
    print(f"🎯 Status: {'PASS' if passed == total else 'FAIL'}")
    print("=" * 50)
    
    # Save results
    with open('quick_test_results.json', 'w') as f:
        json.dump({
            'timestamp': time.strftime('%Y-%m-%d %H:%M:%S'),
            'passed': passed,
            'total': total,
            'status': 'PASS' if passed == total else 'FAIL',
            'results': results
        }, f, indent=2)
    
    return 0 if passed == total else 1

if __name__ == "__main__":
    exit(main())
EOF

chmod +x quick_test.py

print_success "Quick test runner created"

# Run quick test
print_status "Running quick Queue-It official connector test..."
python quick_test.py

if [ $? -eq 0 ]; then
    print_success "Quick test completed successfully!"
else
    print_warning "Quick test had some issues - check results"
fi

print_status "Setup complete! You can now run:"
echo "  python quick_test.py                    # Quick test"
echo "  python tests/backend/test_queueit_official_connector.py  # Full backend test"
echo "  npm test                                # Frontend test (if Node.js available)"
echo "  python run_official_connector_tests.py  # Comprehensive test suite"

print_success "Queue-It Official Connector testing environment ready!" 