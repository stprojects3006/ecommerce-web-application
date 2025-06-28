# Quick Start Guide - E-Commerce Load Testing

This guide will get you up and running with load testing in 5 minutes.

## Prerequisites Check

First, ensure you have the required software:

```bash
# Check Java version (should be 17+)
java -version

# Check Maven version (should be 3.6+)
mvn -version

# Check if you're in the right directory
pwd  # Should show: .../load-testing-springboot
```

## Step 1: Install JMeter (2 minutes)

```bash
# Install JMeter automatically
sudo ./install_jmeter.sh

# Verify installation
jmeter -v
```

## Step 2: Build Spring Boot Application (1 minute)

```bash
# Build the application
mvn clean package

# Verify build
ls -la target/load-testing-springboot-1.0.0.jar
```

## Step 3: Start Your E-Commerce Application

Make sure your e-commerce application is running:

```bash
# If using Docker Compose
docker-compose up -d

# Or start individual services
# ... start your microservices
```

## Step 4: Run Load Tests (2 minutes)

```bash
# Run all tests automatically
./run_load_tests.sh

# Or run specific tests
./run_load_tests.sh --jmeter-only
```

## Step 5: View Results

### JMeter Reports
- Open: `test-reports/[TestName]_[Timestamp]/html-report/index.html`
- View detailed performance metrics

### Spring Boot Metrics
- Health: http://localhost:8081/loadtest/actuator/health
- Metrics: http://localhost:8081/loadtest/actuator/metrics
- H2 Console: http://localhost:8081/loadtest/h2-console

## Quick Test Examples

### Test 1: Basic Load Test
```bash
# Run basic e-commerce load test
$JMETER_HOME/bin/jmeter -n -t jmeter/E-Commerce_Load_Test_Plan.jmx \
  -l test-reports/basic-test.jtl \
  -e -o test-reports/basic-html \
  -JBASE_URL=http://localhost:8080
```

### Test 2: Stress Test
```bash
# Run stress test to find breaking point
$JMETER_HOME/bin/jmeter -n -t jmeter/Stress_Test_Plan.jmx \
  -l test-reports/stress-test.jtl \
  -e -o test-reports/stress-html \
  -JBASE_URL=http://localhost:8080
```

### Test 3: API Performance Test
```bash
# Test specific API endpoints
curl -X POST http://localhost:8081/loadtest/api/loadtest/execute \
  -H "Content-Type: application/json" \
  -d '{
    "testName": "Products API Test",
    "url": "http://localhost:8080/api/products",
    "method": "GET",
    "concurrentUsers": 50,
    "timeoutSeconds": 30
  }'
```

## Common Issues & Solutions

### Issue: JMeter not found
```bash
# Set JMETER_HOME
export JMETER_HOME=/opt/apache-jmeter
export PATH=$PATH:$JMETER_HOME/bin
```

### Issue: Port 8081 in use
```bash
# Kill existing process
pkill -f "load-testing-springboot"
```

### Issue: Java version too old
```bash
# Install Java 17
sudo apt install openjdk-17-jdk  # Ubuntu/Debian
# or
brew install openjdk@17          # macOS
```

## Next Steps

1. **Customize Test Plans**: Modify JMeter files for your specific needs
2. **Add More Scenarios**: Create additional test cases
3. **Set Up Monitoring**: Integrate with Prometheus/Grafana
4. **Automate**: Add to CI/CD pipeline

## Support

- Check the main README.md for detailed documentation
- Review JMeter logs in test-reports directory
- Check Spring Boot logs: `tail -f spring-app.log`

---

**You're all set!** Your load testing environment is ready to go. 🚀 