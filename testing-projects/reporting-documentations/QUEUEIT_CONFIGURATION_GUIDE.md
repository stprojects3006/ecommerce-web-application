# Queue-It Official Connector Configuration Guide

This guide will help you set up Queue-It official connectors for localhost deployment and testing.

## 🎯 Prerequisites

1. **Queue-It Account**: You need a Queue-It account with access to the Go Queue-it Platform
2. **Event Configuration**: Events must be configured in the Queue-It platform
3. **API Credentials**: Customer ID, Secret Key, and API Key from Queue-It

## 📋 Required Queue-It Credentials

### 1. Get Your Queue-It Credentials

1. **Login to Queue-It Platform**: https://go.queue-it.net/
2. **Navigate to Integration**: Go to your account settings
3. **Get Credentials**:
   - **Customer ID**: Your unique customer identifier
   - **Secret Key**: Used for server-side validation
   - **API Key**: Used for API calls to Queue-It

### 2. Example Credentials (Replace with your actual values)

```properties
# Queue-It Configuration
queueit.customer-id=your-customer-id
queueit.secret-key=your-secret-key-here
queueit.api-key=your-api-key-here
queueit.queue-domain=your-customer-id.queue-it.net
```

## 🔧 Backend Configuration (Spring Boot)

### 1. Update API Gateway Properties

Edit `microservice-backend/api-gateway/src/main/resources/application.properties`:

```properties
# Queue-It Configuration
queueit.customer-id=your-customer-id
queueit.secret-key=your-secret-key-here
queueit.api-key=your-api-key-here
queueit.queue-domain=your-customer-id.queue-it.net

# Queue-It Events
queueit.events.flash-sale=flash-sale-2024
queueit.events.black-friday=black-friday-2024
queueit.events.checkout=checkout-protection
queueit.events.high-traffic=high-traffic-protection
```

### 2. Update API Gateway pom.xml

Make sure the Queue-It Java connector is added:

```xml
<!-- Queue-It Java Connector -->
<dependency>
    <groupId>com.queue-it</groupId>
    <artifactId>queueit-connector-java</artifactId>
    <version>4.3.2</version>
</dependency>
```

### 3. Environment Variables (Optional)

You can also set these as environment variables:

```bash
export QUEUEIT_CUSTOMER_ID="your-customer-id"
export QUEUEIT_SECRET_KEY="your-secret-key"
export QUEUEIT_API_KEY="your-api-key"
export QUEUEIT_QUEUE_DOMAIN="your-customer-id.queue-it.net"
```

## 🎨 Frontend Configuration (React)

### 1. Install Queue-It JavaScript Connector

```bash
cd frontend
npm install @queue-it/connector-javascript
```

### 2. Update Frontend Configuration

Create/update `frontend/src/queueit/queueit-config.js`:

```javascript
// Queue-It Configuration
export const queueitConfig = {
    customerId: 'your-customer-id',
    secretKey: 'your-secret-key',
    apiKey: 'your-api-key',
    queueDomain: 'your-customer-id.queue-it.net',
    
    // Event configurations
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

// Queue-It connector initialization
export const initializeQueueIt = () => {
    if (typeof window !== 'undefined' && window.QueueIt) {
        const connector = new window.QueueIt.Connector({
            customerId: queueitConfig.customerId,
            secretKey: queueitConfig.secretKey,
            apiKey: queueitConfig.apiKey
        });
        
        return connector;
    }
    return null;
};
```

### 3. Update Frontend Service

Update `frontend/src/queueit/queueit-service.js`:

```javascript
import { queueitConfig, initializeQueueIt } from './queueit-config.js';

class QueueItService {
    constructor() {
        this.connector = initializeQueueIt();
        this.apiBase = 'http://localhost:8081/api/queueit';
    }

    async validateToken(eventId, queueitToken, originalUrl) {
        try {
            const response = await fetch(`${this.apiBase}/validate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    eventId,
                    queueitToken,
                    originalUrl
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Queue-It token validation failed:', error);
            throw error;
        }
    }

    async enqueueUser(eventId, targetUrl) {
        try {
            const response = await fetch(`${this.apiBase}/enqueue`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    eventId,
                    targetUrl,
                    userAgent: navigator.userAgent,
                    ipAddress: '127.0.0.1' // Will be replaced by server
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Queue-It enqueue failed:', error);
            throw error;
        }
    }

    async getQueueStatus(eventId) {
        try {
            const response = await fetch(`${this.apiBase}/status/${eventId}`);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Queue-It status check failed:', error);
            throw error;
        }
    }

    async getQueuePosition(eventId, queueitToken) {
        try {
            const response = await fetch(
                `${this.apiBase}/position/${eventId}?queueitToken=${queueitToken}`
            );
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Queue-It position check failed:', error);
            throw error;
        }
    }
}

export default new QueueItService();
```

## 🧪 Test Configuration

### 1. Update Test Configuration

Create `testing-projects/queueit-functional-testing/config/test_config.json`:

```json
{
    "backend": {
        "base_url": "http://localhost:8081",
        "api_base": "http://localhost:8081/api/queueit"
    },
    "frontend": {
        "base_url": "http://localhost:3000"
    },
    "queueit": {
        "customer_id": "your-customer-id",
        "secret_key": "your-secret-key",
        "api_key": "your-api-key",
        "queue_domain": "your-customer-id.queue-it.net",
        "test_events": {
            "flash_sale": "flash-sale-2024",
            "black_friday": "black-friday-2024",
            "checkout": "checkout-protection",
            "high_traffic": "high-traffic-protection"
        }
    },
    "test_data": {
        "user_agent": "Mozilla/5.0 (Test Browser) QueueIt-Test-Suite/1.0",
        "ip_address": "127.0.0.1"
    }
}
```

### 2. Environment Variables for Testing

Create `testing-projects/queueit-functional-testing/.env`:

```bash
# Queue-It Test Configuration
QUEUEIT_CUSTOMER_ID=your-customer-id
QUEUEIT_SECRET_KEY=your-secret-key
QUEUEIT_API_KEY=your-api-key
QUEUEIT_QUEUE_DOMAIN=your-customer-id.queue-it.net

# Test URLs
BACKEND_URL=http://localhost:8081
FRONTEND_URL=http://localhost:3000
```

## 🚀 Running Tests Locally

### 1. Start Your Services

```bash
# Start backend services
cd microservice-backend
./mvnw spring-boot:run -pl api-gateway

# Start frontend (in another terminal)
cd frontend
npm start
```

### 2. Run Queue-It Tests

```bash
cd testing-projects/queueit-functional-testing

# Quick setup and test
./quick_setup_and_test.sh

# Or run individual tests
python tests/backend/test_queueit_official_connector.py
npm test  # Frontend tests
```

## 🔍 Troubleshooting

### Common Issues

1. **Connection Refused**: Make sure your services are running on localhost
2. **Invalid Credentials**: Verify your Queue-It credentials are correct
3. **CORS Errors**: Ensure CORS is configured for localhost
4. **Event Not Found**: Verify events are configured in Queue-It platform

### Debug Commands

```bash
# Test backend health
curl http://localhost:8081/api/queueit/health

# Test queue status
curl http://localhost:8081/api/queueit/status/flash-sale-2024

# Test frontend
curl http://localhost:3000
```

## 📊 Expected Test Results

When properly configured, you should see:

```
🚀 Quick Queue-It Official Connector Test
==================================================

🔍 Testing: Backend Health
✅ Backend Health: PASS

🔍 Testing: Queue Status
✅ Queue Status: PASS

🔍 Testing: Enqueue
✅ Enqueue: PASS

==================================================
📊 Results: 3/3 tests passed
🎯 Status: PASS
==================================================
```

## 🔐 Security Notes

1. **Never commit credentials** to version control
2. **Use environment variables** for production
3. **Rotate API keys** regularly
4. **Monitor API usage** in Queue-It platform

## 📞 Support

- **Queue-It Documentation**: https://queue-it.com/docs
- **Queue-It Support**: https://queue-it.com/support
- **Test Issues**: Check the test logs for detailed error messages 