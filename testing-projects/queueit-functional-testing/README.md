# Queue-it Integration Functional Testing Project

## 🎯 Overview

This testing project provides comprehensive functional testing for the Queue-it integration in the PURELY e-commerce application. It includes automated tests, manual testing procedures, and Grafana dashboard monitoring for performance analysis.

## 📋 Test Coverage

### ✅ Frontend Testing
- Queue-it service initialization
- Queue trigger detection
- Queue overlay functionality
- Queue indicator display
- Token management
- Error handling and recovery

### ✅ Backend Testing
- Queue-it API endpoints
- Queue status checking
- User enqueueing
- Position polling
- Queue statistics
- Health monitoring

### ✅ Integration Testing
- End-to-end queue flow
- Cross-browser compatibility
- Mobile responsiveness
- Performance under load
- Error scenarios

### ✅ Performance Testing
- Queue response times
- API latency monitoring
- Concurrent user simulation
- Memory usage analysis
- Network performance

## 🏗️ Project Structure

```
testing-projects/queueit-functional-testing/
├── README.md                           # This documentation
├── requirements.txt                    # Python dependencies
├── config/
│   ├── test_config.json               # Test configuration
│   └── grafana_dashboards/            # Grafana dashboard templates
├── tests/
│   ├── frontend/                      # Frontend test suites
│   ├── backend/                       # Backend test suites
│   ├── integration/                   # Integration test suites
│   └── performance/                   # Performance test suites
├── utils/
│   ├── test_helpers.py                # Common test utilities
│   ├── grafana_client.py              # Grafana API client
│   └── queueit_simulator.py           # Queue-it behavior simulator
├── reports/                           # Test reports and results
├── scripts/
│   ├── run_all_tests.sh              # Run all test suites
│   ├── run_frontend_tests.sh         # Run frontend tests only
│   ├── run_backend_tests.sh          # Run backend tests only
│   └── generate_report.sh            # Generate test reports
└── docker/
    ├── Dockerfile                     # Test environment container
    └── docker-compose.yml             # Test services
```

## 🚀 Quick Start

### 1. Setup Test Environment

```bash
# Navigate to testing project
cd testing-projects/queueit-functional-testing

# Install dependencies
pip install -r requirements.txt

# Setup test configuration
cp config/test_config.json.example config/test_config.json
# Edit config/test_config.json with your settings
```

### 2. Run All Tests

```bash
# Run complete test suite
./scripts/run_all_tests.sh

# Or run specific test suites
./scripts/run_frontend_tests.sh
./scripts/run_backend_tests.sh
./scripts/run_integration_tests.sh
./scripts/run_performance_tests.sh
```

### 3. View Results

```bash
# Generate test report
./scripts/generate_report.sh

# Open test report
open reports/test_report.html
```

## 📊 Grafana Dashboard Monitoring

### Dashboard Setup

1. **Import Queue-it Test Dashboards**
   - Navigate to Grafana (http://localhost:3000)
   - Go to Dashboards → Import
   - Import the dashboard templates from `config/grafana_dashboards/`

2. **Available Dashboards**
   - **Queue-it API Performance**: Monitor API response times and throughput
   - **Queue-it Frontend Metrics**: Track frontend queue interactions
   - **Queue-it Error Tracking**: Monitor errors and failures
   - **Queue-it Load Testing**: Performance under load

### Key Metrics to Monitor

#### API Performance Metrics
- **Response Time**: Average, 95th percentile, 99th percentile
- **Throughput**: Requests per second
- **Error Rate**: Percentage of failed requests
- **Queue Size**: Number of users in queue
- **Wait Time**: Average queue wait time

#### Frontend Metrics
- **Queue Trigger Rate**: How often queues are activated
- **User Experience**: Time to join queue, position updates
- **Error Recovery**: Success rate of error handling
- **Mobile Performance**: Mobile-specific metrics

#### System Metrics
- **Memory Usage**: Application memory consumption
- **CPU Usage**: System resource utilization
- **Network Latency**: API call network performance
- **Database Performance**: Queue data operations

## 🧪 Test Suites

### Frontend Test Suite

```bash
# Run frontend tests
python -m pytest tests/frontend/ -v --html=reports/frontend_report.html
```

**Test Cases:**
- Queue-it service initialization
- URL trigger detection
- Queue overlay rendering
- Queue indicator functionality
- Token storage and retrieval
- Error handling scenarios
- Mobile responsiveness
- Cross-browser compatibility

### Backend Test Suite

```bash
# Run backend tests
python -m pytest tests/backend/ -v --html=reports/backend_report.html
```

**Test Cases:**
- Queue status endpoint
- User enqueueing
- Position polling
- Queue statistics
- Health check endpoint
- Error handling
- Rate limiting
- Authentication

### Integration Test Suite

```bash
# Run integration tests
python -m pytest tests/integration/ -v --html=reports/integration_report.html
```

**Test Cases:**
- End-to-end queue flow
- Frontend-backend communication
- Token lifecycle management
- Error recovery scenarios
- Performance under load
- Concurrent user handling

### Performance Test Suite

```bash
# Run performance tests
python -m pytest tests/performance/ -v --html=reports/performance_report.html
```

**Test Cases:**
- Load testing with multiple users
- Stress testing under high load
- Memory leak detection
- Response time analysis
- Throughput measurement
- Resource utilization

## 📈 Monitoring and Analysis

### Real-time Monitoring

1. **Grafana Dashboards**
   - Monitor test execution in real-time
   - Track performance metrics
   - Analyze error patterns
   - Compare test results

2. **Prometheus Metrics**
   - Queue-it API metrics
   - Frontend interaction metrics
   - System performance metrics
   - Custom business metrics

3. **Log Analysis**
   - Test execution logs
   - Application logs
   - Error logs
   - Performance logs

### Performance Analysis

#### Latency Analysis
```python
# Example: Analyze API response times
import requests
import time

def measure_api_latency():
    start_time = time.time()
    response = requests.get('http://localhost:8081/api/queueit/health')
    end_time = time.time()
    
    latency = (end_time - start_time) * 1000  # Convert to milliseconds
    return latency
```

#### Throughput Analysis
```python
# Example: Measure requests per second
import concurrent.futures
import time

def measure_throughput():
    start_time = time.time()
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
        futures = [executor.submit(make_request) for _ in range(100)]
        concurrent.futures.wait(futures)
    
    end_time = time.time()
    duration = end_time - start_time
    throughput = 100 / duration  # Requests per second
    
    return throughput
```

## 🔧 Configuration

### Test Configuration

Edit `config/test_config.json`:

```json
{
  "base_url": "http://localhost:8081",
  "frontend_url": "http://localhost:5173",
  "grafana_url": "http://localhost:3000",
  "grafana_api_key": "your-grafana-api-key",
  "test_timeout": 30,
  "retry_attempts": 3,
  "concurrent_users": 10,
  "test_duration": 300,
  "queue_events": [
    "flash-sale-2024",
    "black-friday-2024",
    "high-traffic-protection"
  ]
}
```

### Environment Variables

```bash
# Test environment
export QUEUEIT_TEST_ENV=development
export QUEUEIT_TEST_BASE_URL=http://localhost:8081
export QUEUEIT_TEST_FRONTEND_URL=http://localhost:5173

# Grafana monitoring
export GRAFANA_URL=http://localhost:3000
export GRAFANA_API_KEY=your-api-key

# Test reporting
export TEST_REPORT_DIR=./reports
export TEST_LOG_LEVEL=INFO
```

## 📊 Test Reports

### HTML Reports

Test results are generated in HTML format with:
- Test execution summary
- Pass/fail statistics
- Performance metrics
- Error details
- Screenshots (for UI tests)
- Performance graphs

### JSON Reports

Detailed test data in JSON format for:
- Automated analysis
- CI/CD integration
- Custom reporting
- Data visualization

### Grafana Integration

Test metrics are automatically sent to Grafana for:
- Real-time monitoring
- Historical analysis
- Performance trending
- Alert configuration

## 🚨 Troubleshooting

### Common Issues

1. **Tests Failing Due to Timeout**
   ```bash
   # Increase timeout in config
   "test_timeout": 60
   ```

2. **Grafana Connection Issues**
   ```bash
   # Check Grafana service
   docker-compose ps grafana
   
   # Verify API key
   curl -H "Authorization: Bearer your-api-key" http://localhost:3000/api/health
   ```

3. **Queue-it Service Not Responding**
   ```bash
   # Check API Gateway
   curl http://localhost:8081/api/queueit/health
   
   # Check logs
   docker-compose logs api-gateway
   ```

### Debug Mode

```bash
# Enable debug logging
export TEST_LOG_LEVEL=DEBUG

# Run tests with verbose output
python -m pytest tests/ -v -s --log-cli-level=DEBUG
```

## 📋 Test Checklist

### Pre-Testing Checklist
- [ ] Queue-it integration is deployed and running
- [ ] Grafana dashboards are imported
- [ ] Test configuration is updated
- [ ] Test environment is clean
- [ ] Dependencies are installed

### Test Execution Checklist
- [ ] Frontend tests pass
- [ ] Backend tests pass
- [ ] Integration tests pass
- [ ] Performance tests meet requirements
- [ ] Grafana metrics are being collected
- [ ] Error rates are within acceptable limits

### Post-Testing Checklist
- [ ] Test reports are generated
- [ ] Performance analysis is complete
- [ ] Issues are documented
- [ ] Recommendations are provided
- [ ] Results are shared with team

## 🔄 Continuous Integration

### GitHub Actions

```yaml
# .github/workflows/queueit-tests.yml
name: Queue-it Integration Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Setup Python
      uses: actions/setup-python@v2
      with:
        python-version: 3.9
    
    - name: Install dependencies
      run: |
        pip install -r testing-projects/queueit-functional-testing/requirements.txt
    
    - name: Run tests
      run: |
        cd testing-projects/queueit-functional-testing
        ./scripts/run_all_tests.sh
    
    - name: Upload test results
      uses: actions/upload-artifact@v2
      with:
        name: test-results
        path: testing-projects/queueit-functional-testing/reports/
```

## 📞 Support

For issues or questions:
1. Check the troubleshooting section
2. Review test logs in `reports/`
3. Check Grafana dashboards for metrics
4. Consult the main Queue-it integration guide

---

**Last Updated:** January 2025  
**Version:** 1.0.0 