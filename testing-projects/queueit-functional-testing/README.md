# Queue-It Functional Testing

This directory contains comprehensive functional tests for the Queue-It integration in the e-commerce application.

## 🚀 Quick Start

### Prerequisites
- Python 3.x
- API Gateway running on localhost:8081
- Queue-It credentials configured

### One-Command Test
```bash
# From the queueit-functional-testing directory
./test.sh
```

This will:
- ✅ Check if API Gateway is running
- ✅ Activate virtual environment
- ✅ Run all Queue-It tests
- ✅ Display results summary

## 📋 Available Test Scripts

### 1. Quick Test (`test.sh`)
**Simple one-liner for basic testing:**
```bash
./test.sh
```

**Output:**
```
🧪 Queue-It Quick Test
======================
✅ Using virtual environment
✅ API Gateway is running
🚀 Simple Queue-It Functional Test
==================================================
🔍 Testing Queue-It health...
✅ Health Check: PASS
   Details: Service: queueit-integration, Customer: futuraforge
...
📊 Quick Results Summary:
==========================
Status: PASS
Tests Passed: 5/5
Success Rate: 100.0%
```

### 2. Comprehensive Test Runner (`run_queueit_tests.sh`)
**Advanced test runner with multiple options:**
```bash
# Run comprehensive tests (default)
./run_queueit_tests.sh

# Quick health check only
./run_queueit_tests.sh -q

# Run specific test category
./run_queueit_tests.sh -t health
./run_queueit_tests.sh -t status
./run_queueit_tests.sh -t enqueue

# Show last test results
./run_queueit_tests.sh -r

# Run all tests with detailed report
./run_queueit_tests.sh -a

# Show help
./run_queueit_tests.sh -h
```

### 3. Direct Python Test
**Run the test directly:**
```bash
# Activate virtual environment
source venv/bin/activate

# Run the test
python3 simple_functional_test.py
```

## 🧪 Test Categories

### Health Check Tests
- ✅ API Gateway connectivity
- ✅ Queue-It service status
- ✅ Customer ID validation

### Queue Status Tests
- ✅ Event status checking
- ✅ Active/inactive queue detection
- ✅ Multiple event support

### Enqueue Tests
- ✅ Queue entry creation
- ✅ Redirect URL generation
- ✅ Event ID validation

### API Endpoint Tests
- ✅ `/api/queueit/health` endpoint
- ✅ `/api/queueit/status/{eventId}` endpoint
- ✅ `/api/queueit/stats/{eventId}` endpoint

### Error Handling Tests
- ✅ Invalid event ID handling
- ✅ Network error handling
- ✅ Timeout handling

## 📊 Test Results

### Success Indicators
- ✅ **PASS**: Test completed successfully
- ❌ **FAIL**: Test failed with error
- ⚠️ **WARN**: Test completed but with warnings

### Results File
Tests generate `simple_test_results.json` with detailed results:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": "PASS",
  "passed": 5,
  "total": 5,
  "success_rate": "100.0%",
  "results": [
    {
      "test": "Health Check",
      "status": "PASS",
      "details": "Service: queueit-integration, Customer: futuraforge"
    }
  ]
}
```

## 🔧 Setup Instructions

### 1. Install Dependencies
```bash
# Create virtual environment
python3 -m venv venv

# Activate virtual environment
source venv/bin/activate

# Install required packages
pip install requests
```

### 2. Configure Queue-It Credentials
Ensure your Queue-It credentials are configured in the API Gateway:
- Customer ID: `futuraforge`
- Secret Key: Configured in application properties
- Event IDs: `flash-sale-2024`, `black-friday-2024`, `checkout-protection`

### 3. Start Services
```bash
# Start API Gateway
cd ../../microservice-backend/api-gateway
./mvnw spring-boot:run
```

## 🚨 Troubleshooting

### Common Issues

#### 1. API Gateway Not Running
```
❌ API Gateway is not running
```
**Solution:** Start the API Gateway service

#### 2. Module Not Found
```
ModuleNotFoundError: No module named 'requests'
```
**Solution:** Activate virtual environment and install requests
```bash
source venv/bin/activate
pip install requests
```

#### 3. Connection Refused
```
ConnectionError: HTTPConnectionPool
```
**Solution:** Check if API Gateway is running on port 8081

#### 4. Queue-It Service Unavailable
```
❌ Health Check: FAIL
```
**Solution:** Verify Queue-It credentials and network connectivity

### Debug Mode
For detailed debugging, run tests with verbose output:
```bash
# Activate virtual environment
source venv/bin/activate

# Run with debug output
python3 -u simple_functional_test.py
```

## 📈 Performance Monitoring

### Test Execution Time
- Quick test: ~2-3 seconds
- Comprehensive test: ~5-10 seconds
- Individual category tests: ~1-2 seconds

### Success Metrics
- **Target Success Rate**: 100%
- **Expected Response Time**: < 2 seconds
- **API Availability**: 99.9%

## 🔄 Continuous Testing

### Automated Test Execution
```bash
# Run tests every 5 minutes
watch -n 300 ./test.sh

# Run tests and log results
./test.sh >> test_logs.txt 2>&1
```

### Integration with CI/CD
Add to your CI/CD pipeline:
```yaml
- name: Queue-It Functional Tests
  run: |
    cd testing-projects/queueit-functional-testing
    ./test.sh
```

## 📚 Additional Resources

- [Queue-It Official Documentation](https://queue-it.com/docs)
- [API Gateway Configuration](../microservice-backend/api-gateway/README.md)
- [E-commerce Application Documentation](../../README.md)

## 🤝 Contributing

To add new tests:
1. Add test functions to `simple_functional_test.py`
2. Update the test runner scripts
3. Update this README with new test descriptions
4. Test thoroughly before committing

---

**Last Updated:** January 2024  
**Test Version:** 1.0.0  
**Compatibility:** Queue-It v3.x, Spring Boot 3.x 