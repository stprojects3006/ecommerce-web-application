# PURELY E-commerce Load Testing Framework

A comprehensive load testing suite for the PURELY E-commerce microservices application, including Selenium UI tests, API endpoint tests, Locust load testing, and **detailed latency monitoring**.

## 🚀 Features

- **Selenium UI Testing**: Complete user journey testing
- **API Endpoint Testing**: Microservices API validation
- **Locust Load Testing**: Scalable load and stress testing
- **🔍 Advanced Latency Monitoring**: Detailed request/response timing analysis
- **📊 Comprehensive Reporting**: HTML, JSON, and CSV reports with latency metrics
- **Performance Monitoring**: Response time and throughput analysis
- **Health Checks**: Service availability monitoring
- **Real-time Metrics**: P50, P90, P95, P99 latency percentiles

## 📋 Prerequisites

- Python 3.8+
- Chrome/Chromium browser
- Docker (for running the application)
- Node.js (for frontend)

## 🛠️ Installation

1. **Navigate to the load testing directory**:
   ```bash
   cd load-testing-selenium
   ```

2. **Create virtual environment**:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

3. **Install dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

4. **Install Chrome WebDriver** (automatically handled by webdriver-manager)

## 🏃‍♂️ Quick Start

### 1. Start the Application
Make sure your PURELY E-commerce application is running:
```bash
cd ..  # Go back to root directory
./deploy.sh
```

### 2. Run Basic Tests
```bash
cd load-testing-selenium
python run_tests.py --test-type smoke
```

### 3. Run Load Tests with Latency Monitoring
```bash
python run_tests.py --load-test --users 20 --run-time 120s
```

### 4. Run Dedicated Latency Testing
```bash
python test_latency.py
```

## 📖 Usage Guide

### Test Types

#### 1. Smoke Tests
Basic functionality tests to ensure the application is working:
```bash
python run_tests.py --test-type smoke
```

#### 2. API Tests with Latency Monitoring
Test all microservices API endpoints with detailed timing:
```bash
python run_tests.py --test-type api
```

#### 3. UI Tests
Test complete user interface flows:
```bash
python run_tests.py --test-type ui
```

#### 4. End-to-End Tests
Test complete user journeys:
```bash
python run_tests.py --test-type e2e
```

#### 5. Performance Tests
Test application performance under load:
```bash
python run_tests.py --test-type performance
```

#### 6. All Tests
Run all test types:
```bash
python run_tests.py --test-type all
```

#### 7. Latency-Only Testing
Run only latency monitoring tests:
```bash
python run_tests.py --latency-only
```

### Load Testing Options

#### Basic Load Test
```bash
python run_tests.py --load-test --users 10 --spawn-rate 2 --run-time 60s
```

#### Stress Test
```bash
python run_tests.py --load-test --users 100 --spawn-rate 10 --run-time 300s
```

#### Custom Host
```bash
python run_tests.py --load-test --host http://your-ec2-instance.com
```

### Advanced Options

#### Headless Mode
Run tests without browser UI:
```bash
python run_tests.py --test-type smoke --headless
```

#### Parallel Execution
Run tests in parallel for faster execution:
```bash
python run_tests.py --test-type regression --parallel
```

#### Health Checks Only
Check service health:
```bash
python run_tests.py --health-check
```

#### API Performance Only
Test API performance with detailed latency:
```bash
python run_tests.py --api-performance
```

## 🔍 Latency Monitoring Features

### What Gets Monitored
- **Request/Response Times**: Detailed timing for all API calls
- **Page Load Times**: Frontend page loading performance
- **Response Sizes**: Data transfer analysis
- **Success Rates**: Error tracking and analysis
- **Percentile Metrics**: P50, P90, P95, P99 latency measurements
- **Concurrent Load**: Performance under multiple simultaneous users

### Latency Metrics Collected
- **Average Response Time**: Mean latency across all requests
- **Minimum/Maximum**: Best and worst case scenarios
- **Percentiles**: P50, P90, P95, P99 for performance analysis
- **Standard Deviation**: Latency variance analysis
- **Error Rates**: Failed request percentages
- **Response Sizes**: Data transfer optimization insights

### Real-time Monitoring
The framework provides real-time latency feedback during test execution:
```
✅ categories: Avg=45.23ms, P95=67.89ms, P99=89.12ms, Success=100.0%
✅ products: Avg=123.45ms, P95=234.56ms, P99=345.67ms, Success=98.5%
✅ auth_health: Avg=12.34ms, P95=15.67ms, P99=18.90ms, Success=100.0%
```

## 📊 Test Reports

After running tests, check the `reports/` directory for:

### Comprehensive Reports
- `comprehensive_latency_report.json`: Complete latency analysis with recommendations
- `latency_summary.txt`: Human-readable latency summary
- `detailed_latency_report.json`: Detailed endpoint-by-endpoint analysis
- `selenium_report.html`: Selenium test results
- `locust_report.html`: Load test results

### Latency Data Files
- `latency_data_*.csv`: Raw latency data for further analysis
- `latency_report_*.json`: Individual test session reports

### Report Contents
Each latency report includes:
- **Overall Statistics**: Total requests, average latency, success rates
- **Endpoint Performance**: Individual endpoint analysis
- **Percentile Analysis**: P50, P90, P95, P99 metrics
- **Performance Recommendations**: Actionable optimization suggestions
- **Error Analysis**: Failed request patterns and causes

## 🧪 Test Structure

### Selenium Tests (`test_ui_flows.py`)
- **User Registration Flow**: Complete signup process with timing
- **User Login Flow**: Authentication testing with latency monitoring
- **Product Browsing**: Product listing and search with performance metrics
- **Cart Management**: Add/remove items with operation timing
- **Checkout Process**: Complete order placement with latency tracking
- **My Account**: User profile and order history with performance analysis
- **Navigation**: Page navigation and responsiveness testing
- **Error Handling**: Invalid inputs and edge cases with timing

### API Tests (`test_api_endpoints.py`)
- **Health Checks**: All microservices health endpoints with response times
- **Authentication**: Login, registration, token validation with latency
- **Product Service**: Product listing, search, details with timing analysis
- **Category Service**: Category listing and details with performance metrics
- **Cart Service**: Cart operations with authentication and timing
- **Order Service**: Order creation and retrieval with latency monitoring
- **User Service**: User management operations with performance tracking
- **Concurrent Testing**: Multiple simultaneous requests with timing analysis
- **Error Handling**: Invalid requests and error responses with timing

### Load Tests (`locustfile.py`)
- **EcommerceUser**: Simulates real user behavior with timing
- **APIUser**: API-only testing for microservices with latency
- **StressTestUser**: High-stress scenarios with performance monitoring

### Dedicated Latency Tests (`test_latency.py`)
- **Endpoint-by-Endpoint Analysis**: Detailed timing for each API
- **Concurrent Load Testing**: Performance under multiple users
- **Percentile Analysis**: P50, P90, P95, P99 measurements
- **Response Size Analysis**: Data transfer optimization
- **Error Rate Tracking**: Success/failure analysis

## ⚙️ Configuration

### Environment Variables
Create a `.env` file in the load-testing-selenium directory:

```env
BASE_URL=http://localhost
API_BASE_URL=http://localhost:8081
HEADLESS=false
LOAD_TEST_USERS=10
SPAWN_RATE=2
RUN_TIME=60s
```

### Performance Thresholds
Modify `config.py` to adjust performance thresholds:

```python
PERFORMANCE_THRESHOLDS = {
    'page_load_time': 3.0,      # seconds
    'api_response_time': 1.0,   # seconds
    'cart_operation_time': 2.0, # seconds
    'checkout_time': 5.0,       # seconds
}
```

### Latency Monitoring Configuration
The latency monitor can be configured in `latency_monitor.py`:

```python
class LatencyMonitor:
    def __init__(self, max_samples=1000):  # Adjust sample size
        # Configuration options
        self.max_samples = max_samples
```

## 🔧 Customization

### Adding New Tests

1. **UI Tests**: Add methods to `TestUIFlows` class in `test_ui_flows.py`
2. **API Tests**: Add methods to `TestAPIEndpoints` class in `test_api_endpoints.py`
3. **Load Tests**: Add new user classes to `locustfile.py`
4. **Latency Tests**: Add new endpoints to `test_latency.py`

### Example: Adding a Custom Latency Test
```python
def test_custom_endpoint_latency(self):
    """Test latency for a custom endpoint"""
    endpoint_name = "Custom API"
    url = f"{TestConfig.API_BASE_URL}/custom-endpoint"
    
    # Test with 20 iterations for statistical significance
    stats = self.test_endpoint_latency(endpoint_name, url, "GET", iterations=20)
    
    # Assert performance requirements
    assert stats['latency_stats']['p95'] < 1000  # P95 under 1 second
    assert stats['success_rate'] > 95  # Success rate above 95%
```

## 🐛 Troubleshooting

### Common Issues

1. **Chrome WebDriver Issues**:
   ```bash
   # Update Chrome browser
   # WebDriver is automatically managed by webdriver-manager
   ```

2. **Connection Refused**:
   - Ensure application is running: `./deploy.sh`
   - Check service health: `python run_tests.py --health-check`

3. **Test Failures**:
   - Check screenshots in `screenshots/` directory
   - Review logs in `logs/` directory
   - Verify test data in `test_data/` directory

4. **Performance Issues**:
   - Adjust performance thresholds in `config.py`
   - Reduce load test parameters
   - Check system resources

5. **Latency Monitoring Issues**:
   - Check network connectivity
   - Verify endpoint URLs in `config.py`
   - Review latency reports for specific endpoint issues

### Debug Mode
Run tests with verbose output:
```bash
pytest -v test_ui_flows.py::TestUIFlows::test_home_page_load
```

## 📈 Performance Monitoring

### Key Metrics
- **Response Time**: API and page load times with percentiles
- **Throughput**: Requests per second with timing breakdown
- **Error Rate**: Failed requests percentage with detailed analysis
- **Resource Usage**: CPU, memory, network with latency correlation

### Monitoring Dashboard
Access Prometheus and Grafana for real-time monitoring:
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

### Latency Alerts
The framework automatically generates alerts for:
- High latency endpoints (>1 second average)
- Low success rates (<95%)
- High P95 latency (>2 seconds)
- High latency variance (inconsistent performance)

## 🚀 AWS EC2 Deployment Testing

### Pre-deployment Testing
```bash
# Test locally first
python run_tests.py --test-type all

# Test with production-like load
python run_tests.py --load-test --users 50 --run-time 300s

# Run dedicated latency testing
python test_latency.py
```

### Post-deployment Testing
```bash
# Test deployed application
python run_tests.py --test-type smoke --host http://your-ec2-ip

# Load test production with latency monitoring
python run_tests.py --load-test --host http://your-ec2-ip --users 100 --run-time 600s

# Comprehensive latency analysis
python test_latency.py
```

## 📝 Test Data Management

### Sample Data
The framework includes test users and sample data:
- Test users in `config.py`
- Sample products and categories (imported via MongoDB)

### Data Cleanup
Implement cleanup in `base_test.py`:
```python
def cleanup_test_data(self):
    """Cleanup test data after tests"""
    # Add cleanup logic here
    pass
```

## 🤝 Contributing

1. Follow the existing test structure
2. Add appropriate error handling
3. Include performance assertions
4. Update documentation
5. Test thoroughly before committing

## 📞 Support

For issues and questions:
- Check the troubleshooting section
- Review test logs and reports
- Contact: stprojects3006@gmail.com

---

**Happy Testing with Advanced Latency Monitoring! 🎉** 