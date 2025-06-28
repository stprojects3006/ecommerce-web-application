# E-Commerce Load Testing Suite

A comprehensive load testing solution for e-commerce applications using Spring Boot and Apache JMeter.

## Overview

This project provides a complete load testing framework that combines:
- **Spring Boot Application**: Custom load testing service with REST API
- **Apache JMeter**: Industry-standard load testing tool with pre-configured test plans
- **Automated Scripts**: Easy-to-use scripts for installation and execution

## Features

### Spring Boot Load Testing Application
- REST API for programmatic load testing
- Real-time test result collection and storage
- Performance metrics and statistics
- H2 in-memory database for test results
- Prometheus metrics integration
- Health checks and monitoring endpoints

### JMeter Test Plans
- **E-Commerce Load Test Plan**: Comprehensive testing of all e-commerce workflows
- **Stress Test Plan**: Identify system breaking points
- **Performance Test Plan**: Measure response times and throughput
- Pre-configured assertions and validations
- HTML reports generation

### Test Scenarios Covered
- User registration and authentication
- Product browsing and search
- Shopping cart operations
- Checkout and order processing
- API performance under various loads

## Prerequisites

- Java 17 or later
- Maven 3.6+
- Apache JMeter 5.6+
- Docker (optional, for running e-commerce application)

## Quick Start

### 1. Install JMeter

```bash
# Make script executable
chmod +x install_jmeter.sh

# Install JMeter (requires sudo)
sudo ./install_jmeter.sh
```

### 2. Build Spring Boot Application

```bash
# Build the application
mvn clean package
```

### 3. Run Load Tests

```bash
# Make runner script executable
chmod +x run_load_tests.sh

# Run all tests
./run_load_tests.sh

# Run specific components
./run_load_tests.sh --build-only      # Build only
./run_load_tests.sh --spring-only     # Run Spring Boot app only
./run_load_tests.sh --jmeter-only     # Run JMeter tests only
```

## Project Structure

```
load-testing-springboot/
├── src/
│   ├── main/
│   │   ├── java/com/dharshi/loadtesting/
│   │   │   ├── LoadTestingApplication.java
│   │   │   ├── controller/
│   │   │   │   └── LoadTestController.java
│   │   │   ├── service/
│   │   │   │   └── LoadTestService.java
│   │   │   ├── model/
│   │   │   │   └── TestResult.java
│   │   │   └── repository/
│   │   │       └── TestResultRepository.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── jmeter/
│   ├── E-Commerce_Load_Test_Plan.jmx
│   ├── Stress_Test_Plan.jmx
│   └── Performance_Test_Plan.jmx
├── test-reports/                    # Generated test reports
├── pom.xml
├── run_load_tests.sh
├── install_jmeter.sh
└── README.md
```

## Spring Boot Application

### API Endpoints

#### Execute Load Test
```http
POST /loadtest/api/loadtest/execute
Content-Type: application/json

{
  "testName": "API Performance Test",
  "url": "http://localhost:8080/api/products",
  "method": "GET",
  "payload": null,
  "authToken": "your-jwt-token",
  "concurrentUsers": 50,
  "timeoutSeconds": 30
}
```

#### Get Test Results
```http
GET /loadtest/api/loadtest/results/{testName}
```

#### Get Test Statistics
```http
GET /loadtest/api/loadtest/stats/{testName}
```

#### Clear Test Results
```http
DELETE /loadtest/api/loadtest/results/{testName}
```

### Configuration

The application runs on port 8081 by default. Key configuration options in `application.properties`:

```properties
# Server Configuration
server.port=8081
server.servlet.context-path=/loadtest

# Database Configuration
spring.datasource.url=jdbc:h2:mem:loadtestdb
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Metrics Configuration
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.metrics.export.prometheus.enabled=true
```

### H2 Database Console

Access the H2 database console at: http://localhost:8081/loadtest/h2-console
- JDBC URL: `jdbc:h2:mem:loadtestdb`
- Username: `sa`
- Password: `password`

## JMeter Test Plans

### 1. E-Commerce Load Test Plan

**Purpose**: Comprehensive testing of all e-commerce workflows

**Test Groups**:
- User Registration (50 users, 10 loops)
- User Login (100 users, 20 loops)
- Product Browsing (200 users, 30 loops)
- Cart Operations (75 users, 15 loops)
- Checkout Process (25 users, 5 loops)

**Features**:
- Realistic user behavior simulation
- Data parameterization
- Response assertions
- JSON extraction for tokens

### 2. Stress Test Plan

**Purpose**: Identify system breaking points

**Test Types**:
- **Gradual Load Increase**: 500 users over 10 minutes
- **Spike Test**: 1000 users in 10 seconds
- **Endurance Test**: 200 users for 30 minutes

**Features**:
- Scheduler-based execution
- Response time monitoring
- Error rate tracking

### 3. Performance Test Plan

**Purpose**: Measure response times and throughput

**Test Scenarios**:
- Response Time Measurement (50 users, 50 loops)
- Throughput Testing (100 users, 10 minutes)

**Features**:
- Response time assertions
- Throughput calculations
- Performance benchmarks

## Running Tests

### Manual JMeter Execution

```bash
# Set JMETER_HOME if not already set
export JMETER_HOME=/opt/apache-jmeter

# Run specific test plan
$JMETER_HOME/bin/jmeter -n -t jmeter/E-Commerce_Load_Test_Plan.jmx \
  -l test-reports/results.jtl \
  -e -o test-reports/html-report \
  -JBASE_URL=http://localhost:8080 \
  -JAPI_GATEWAY_URL=http://localhost:8080/api
```

### Automated Execution

```bash
# Run all tests with automated reporting
./run_load_tests.sh

# Run only JMeter tests
./run_load_tests.sh --jmeter-only

# Run only Spring Boot application
./run_load_tests.sh --spring-only
```

## Test Reports

### JMeter Reports

Test reports are generated in the `test-reports/` directory:
- **JTL files**: Raw test results
- **HTML reports**: Interactive web-based reports
- **Summary reports**: Key metrics and statistics

### Spring Boot Metrics

Access metrics at: http://localhost:8081/loadtest/actuator/metrics

Key metrics available:
- `http.server.requests`: HTTP request metrics
- `jvm.memory.used`: JVM memory usage
- `process.cpu.usage`: CPU usage
- Custom load testing metrics

## Configuration

### Environment Variables

```bash
# JMeter home directory
export JMETER_HOME=/opt/apache-jmeter

# Target application URL
export BASE_URL=http://localhost:8080
export API_GATEWAY_URL=http://localhost:8080/api

# Test configuration
export CONCURRENT_USERS=100
export TEST_DURATION=300
```

### Customizing Test Plans

1. **Modify Thread Groups**: Adjust user count, ramp-up time, and loops
2. **Update URLs**: Change target endpoints in test plans
3. **Add Assertions**: Include custom response validations
4. **Parameterize Data**: Use CSV files for test data

## Monitoring and Analysis

### Real-time Monitoring

```bash
# Monitor Spring Boot application logs
tail -f spring-app.log

# Check application health
curl http://localhost:8081/loadtest/actuator/health

# View metrics
curl http://localhost:8081/loadtest/actuator/metrics
```

### Performance Analysis

1. **Response Time Analysis**: Check 90th and 95th percentiles
2. **Throughput Analysis**: Monitor requests per second
3. **Error Rate Analysis**: Track failed requests
4. **Resource Utilization**: Monitor CPU, memory, and network

## Troubleshooting

### Common Issues

1. **JMeter not found**
   ```bash
   # Install JMeter
   sudo ./install_jmeter.sh
   
   # Set JMETER_HOME
   export JMETER_HOME=/opt/apache-jmeter
   ```

2. **Port conflicts**
   ```bash
   # Check port usage
   lsof -i :8081
   
   # Kill existing process
   pkill -f "load-testing-springboot"
   ```

3. **Java version issues**
   ```bash
   # Check Java version
   java -version
   
   # Install Java 17 if needed
   sudo apt install openjdk-17-jdk
   ```

4. **Permission issues**
   ```bash
   # Make scripts executable
   chmod +x *.sh
   ```

### Debug Mode

```bash
# Run Spring Boot in debug mode
java -Ddebug=true -jar target/load-testing-springboot-1.0.0.jar

# Run JMeter in debug mode
$JMETER_HOME/bin/jmeter -Djmeter.debug=true -n -t test-plan.jmx
```

## Best Practices

### Load Testing Best Practices

1. **Start Small**: Begin with low user counts and gradually increase
2. **Monitor Resources**: Watch CPU, memory, and network usage
3. **Use Realistic Data**: Parameterize test data for realistic scenarios
4. **Set Baselines**: Establish performance baselines before optimization
5. **Test in Isolation**: Ensure no other load affects test results

### JMeter Best Practices

1. **Use Listeners Sparingly**: Disable listeners in production tests
2. **Parameterize URLs**: Use variables for flexible configuration
3. **Add Think Time**: Include realistic delays between requests
4. **Use Assertions**: Validate responses for functional correctness
5. **Monitor Memory**: JMeter can be memory-intensive with large tests

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review JMeter documentation
3. Create an issue in the repository
4. Contact the development team

## Changelog

### Version 1.0.0
- Initial release
- Spring Boot load testing application
- JMeter test plans for e-commerce
- Automated installation and execution scripts
- Comprehensive documentation 