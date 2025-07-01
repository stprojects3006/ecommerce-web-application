# Queue-It Localhost Setup and Testing Guide

This guide will walk you through setting up Queue-It official connectors for localhost testing.

## 🎯 Quick Start Checklist

- [ ] Get Queue-It credentials (Customer ID, Secret Key, API Key)
- [ ] Configure backend properties
- [ ] Install Queue-It dependencies
- [ ] Start backend services
- [ ] Start frontend service (via nginx)
- [ ] Run tests

## 📋 Step 1: Get Queue-It Credentials

### 1.1 Access Queue-It Platform
1. Go to https://go.queue-it.net/
2. Login to your account
3. Navigate to **Integration** section

### 1.2 Get Required Credentials
You need these three values:
- **Customer ID**: Your unique identifier (e.g., `futuraforge`)
- **Secret Key**: Used for server-side validation
- **API Key**: Used for API calls

### 1.3 Example Credentials (Replace with your actual values)
```properties
Customer ID: futuraforge
Secret Key: 12345678-1234-1234-1234-123456789012
API Key: 87654321-4321-4321-4321-210987654321
```

## 🔧 Step 2: Configure Backend

### 2.1 Update API Gateway Properties
Edit `microservice-backend/api-gateway/src/main/resources/application.properties`:

```properties
# Queue-It Configuration
queueit.customer-id=futuraforge
queueit.secret-key=12345678-1234-1234-1234-123456789012
queueit.api-key=87654321-4321-4321-4321-210987654321
queueit.queue-domain=futuraforge.queue-it.net

# Queue-It Events
queueit.events.flash-sale=flash-sale-2024
queueit.events.black-friday=black-friday-2024
queueit.events.checkout=checkout-protection
queueit.events.high-traffic=high-traffic-protection
```

### 2.2 Verify Dependencies
Make sure `microservice-backend/api-gateway/pom.xml` includes:

```xml
<!-- Queue-It Java Connector -->
<dependency>
    <groupId>com.queue-it</groupId>
    <artifactId>queueit-connector-java</artifactId>
    <version>4.3.2</version>
</dependency>
```

## 🎨 Step 3: Configure Frontend

### 3.1 Install Queue-It JavaScript Connector
```bash
cd frontend
npm install @queue-it/connector-javascript
```

### 3.2 Update Frontend Configuration
Create/update `frontend/src/queueit/queueit-config.js`:

```javascript
export const queueitConfig = {
    customerId: 'futuraforge',
    secretKey: '12345678-1234-1234-1234-123456789012',
    apiKey: '87654321-4321-4321-4321-210987654321',
    queueDomain: 'futuraforge.queue-it.net',
    
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
```

## 🚀 Step 4: Start Services

### 4.1 Start Backend Services
```bash
# Terminal 1: Start API Gateway
cd microservice-backend/api-gateway
./mvnw spring-boot:run

# Terminal 2: Start Service Registry (if needed)
cd microservice-backend/service-registry
./mvnw spring-boot:run

# Terminal 3: Start other services as needed
cd microservice-backend/auth-service
./mvnw spring-boot:run
```

### 4.2 Start Frontend (via nginx)
```bash
# Terminal 4: Build frontend
cd frontend
npm run build

# Terminal 5: Start nginx with frontend
# Use your docker-compose or nginx configuration
docker-compose up nginx
```

### 4.3 Verify Services
Check if services are running:
```bash
# Backend health
curl http://localhost:8081/actuator/health

# Frontend (via nginx)
curl https://localhost

# Queue-It health
curl http://localhost:8081/api/queueit/health
```

## 🧪 Step 5: Run Tests

### 5.1 Quick Test
```bash
cd testing-projects/queueit-functional-testing
python run_localhost_tests.py
```

### 5.2 Comprehensive Test Suite
```bash
# Run all tests
./quick_setup_and_test.sh

# Or run individual tests
python tests/backend/test_queueit_official_connector.py
```

## 🔍 Step 6: Verify Configuration

### 6.1 Test Queue-It Health
```bash
curl http://localhost:8081/api/queueit/health
```

Expected response:
```json
{
    "status": "healthy",
    "service": "queueit-integration",
    "connector": "official-java-connector",
    "customerId": "futuraforge",
    "timestamp": "2024-01-15T10:30:00Z"
}
```

### 6.2 Test Queue Status
```bash
curl http://localhost:8081/api/queueit/status/flash-sale-2024
```

Expected response:
```json
{
    "eventId": "flash-sale-2024",
    "isActive": true,
    "queueSize": 0,
    "estimatedWaitTime": 0,
    "timestamp": "2024-01-15T10:30:00Z"
}
```

### 6.3 Test Enqueue
```bash
curl -X POST http://localhost:8081/api/queueit/enqueue \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "flash-sale-2024",
    "targetUrl": "https://localhost/flash-sale",
    "userAgent": "Test Browser",
    "ipAddress": "127.0.0.1"
  }'
```

## 🛠️ Troubleshooting

### Common Issues

1. **Services Not Starting**
   ```bash
   # Check if ports are in use
   lsof -i :8081
   lsof -i :443
   
   # Kill processes if needed
   kill -9 <PID>
   ```

2. **Queue-It Connection Issues**
   - Verify credentials are correct
   - Check network connectivity
   - Ensure events are configured in Queue-It platform

3. **CORS Errors**
   - Make sure CORS is configured for localhost
   - Check browser console for errors

4. **SSL Certificate Issues**
   ```bash
   # For localhost testing, you might need to accept self-signed certificates
   curl -k https://localhost
   
   # Or add certificate to trusted store
   ```

5. **Dependency Issues**
   ```bash
   # Clean and rebuild
   cd microservice-backend/api-gateway
   ./mvnw clean install
   
   cd frontend
   npm install
   ```

### Debug Commands

```bash
# Check backend logs
tail -f microservice-backend/api-gateway/logs/application.log

# Check nginx logs
tail -f /var/log/nginx/error.log

# Test individual endpoints
curl -v http://localhost:8081/api/queueit/health
curl -k https://localhost
```

## 📊 Expected Test Results

When everything is configured correctly, you should see:

```
🚀 Starting Queue-It Localhost Test Suite
============================================================
🔍 Testing service availability...
✅ Service Availability: PASS
   Details: Both services are running

🔍 Testing Queue-It health...
✅ Queue-It Health: PASS
   Details: Connector: official-java-connector

🔍 Testing Queue-It configuration...
✅ Queue-It Configuration: PASS
   Details: Customer ID: futuraforge

🔍 Testing queue status...
✅ Queue Status - flash_sale: PASS
   Details: Active: true

🔍 Testing enqueue functionality...
✅ Enqueue Functionality: PASS
   Details: Redirect URL: https://futuraforge.queue-it.net/queue/flash-sale-2024...

============================================================
QUEUE-IT LOCALHOST TEST RESULTS
============================================================
Overall Status: PASS
Tests Passed: 7/7
Success Rate: 100.0%
============================================================
```

## 🔐 Security Notes

1. **Never commit real credentials** to version control
2. **Use environment variables** for production
3. **Rotate API keys** regularly
4. **Monitor API usage** in Queue-It platform

## 📞 Next Steps

1. **Configure Events**: Set up events in Queue-It platform
2. **Test Integration**: Run end-to-end tests
3. **Monitor Performance**: Check response times
4. **Deploy to Production**: Use production credentials

## 🆘 Need Help?

- **Queue-It Documentation**: https://queue-it.com/docs
- **Queue-It Support**: https://queue-it.com/support
- **Test Issues**: Check the test logs for detailed error messages 