# PURELY E-commerce Application - Queue-it Integration Guide

## 🚀 Overview

This guide covers the complete integration of Queue-it's virtual waiting room with the PURELY e-commerce application. Queue-it helps manage high-traffic events and prevents website crashes during peak loads.

## 🧪 Functional Test Cases

All test cases are located in `testing-projects/queueit-functional-testing/`.

| Category         | Test File/Location                                      | Description |
|-----------------|---------------------------------------------------------|-------------|
| **Backend**     | `tests/backend/test_queueit_api.py`                     | API health, status, enqueue, stats, error handling |
|                 | `tests/backend/test_queueit_official_connector.py`      | Official Queue-It Java connector validation |
| **Frontend**    | `tests/frontend/test_queueit_frontend.py`               | Service init, overlay, indicator, token, mobile, error |
|                 | `tests/frontend/test_queueit_frontend_official.js`      | Official JS connector, event handling, redirect, token validation |
| **Integration** | `tests/integration/test_queueit_integration.py`         | End-to-end queue flow, token lifecycle, error recovery |
| **Performance** | `tests/performance/test_queueit_performance.py`         | Load, stress, memory, response time, throughput |
| **Quick Test**  | `simple_functional_test.py`                             | Health, status, enqueue, endpoints, error handling |
| **Test Runner** | `test.sh`, `run_queueit_tests.sh`, `generate_metrics.sh`| One-command and comprehensive test runners |

**Descriptions:**
- **Health Check:** Verifies API Gateway and Queue-It service health
- **Queue Status:** Checks if queue is active for events
- **Enqueue:** Simulates user joining the queue
- **API Endpoints:** Validates all backend endpoints
- **Overlay/Indicator:** Ensures UI components display and update correctly
- **Mobile/Responsive:** Tests overlay on various devices
- **Performance:** Simulates high load and concurrent users
- **Error Handling:** Tests invalid events, network errors, and recovery

## 📋 Features Implemented

### ✅ Frontend Integration
- **React Context** for state management
- **Queue Overlay** component for user experience
- **Queue Indicator** for status display
- **Automatic queue detection** based on URL triggers
- **Real-time position updates** with polling
- **Responsive design** for all devices

### ✅ Backend Integration
- **API Gateway endpoints** for queue operations
- **Queue status checking** for events
- **User enqueueing** with token generation
- **Position polling** for real-time updates
- **Queue statistics** and monitoring
- **Health check endpoints**

### ✅ Configuration Management
- **Environment-based configuration**
- **Event-specific queue settings**
- **Development bypass options**
- **Production-ready security**

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │    │   Queue-it      │
│   (React)       │◄──►│   (Spring)      │◄──►│   (External)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
    ┌────▼────┐            ┌─────▼─────┐            ┌────▼────┐
    │ Queue   │            │ Queue     │            │ Queue   │
    │ Context │            │ Controller│            │ Service │
    └─────────┘            └───────────┘            └─────────┘
```

## 🔧 Configuration

### Environment Variables

#### Frontend (.env)
```bash
# Queue-it Configuration
REACT_APP_QUEUE_IT_ENABLED=true
REACT_APP_QUEUE_IT_CUSTOMER_ID=futuraforge
REACT_APP_QUEUE_IT_SECRET_KEY=your-secret-key
REACT_APP_QUEUE_IT_API_KEY=your-api-key
REACT_APP_QUEUE_IT_BYPASS=false
```

#### Backend (application.yml)
```yaml
queueit:
  customer-id: ${QUEUE_IT_CUSTOMER_ID:futuraforge}
  secret-key: ${QUEUE_IT_SECRET_KEY:your-secret-key}
  api-key: ${QUEUE_IT_API_KEY:your-api-key}
  queue-domain: ${QUEUE_IT_QUEUE_DOMAIN:futuraforge.queue-it.net}
  enabled: ${QUEUE_IT_ENABLED:true}
  debug: ${QUEUE_IT_DEBUG:false}
```

### Event Configuration

```javascript
// frontend/src/queueit/queueit-config.js
events: {
  flashSale: {
    eventId: 'flash-sale-2024',
    queueDomain: 'futuraforge.queue-it.net',
    cookieValidityMinute: 20,
    triggers: [
      { operator: 'Contains', valueToCompare: '/flash-sale', urlPart: 'PageUrl', validatorType: 'UrlValidator' }
    ]
  },
  blackFriday: {
    eventId: 'black-friday-2024',
    queueDomain: 'futuraforge.queue-it.net',
    cookieValidityMinute: 30,
    triggers: [
      { operator: 'Contains', valueToCompare: '/black-friday', urlPart: 'PageUrl', validatorType: 'UrlValidator' }
    ]
  },
  highTraffic: {
    eventId: 'high-traffic-protection',
    queueDomain: 'futuraforge.queue-it.net',
    cookieValidityMinute: 15,
    triggers: [
      { operator: 'Contains', valueToCompare: '/products', urlPart: 'PageUrl', validatorType: 'UrlValidator' }
    ]
  },
  checkout: {
    eventId: 'checkout-protection',
    queueDomain: 'futuraforge.queue-it.net',
    cookieValidityMinute: 10,
    triggers: [
      { operator: 'Contains', valueToCompare: '/order/checkout', urlPart: 'PageUrl', validatorType: 'UrlValidator' }
    ]
  }
}
```

## 🚀 Quick Start

### 1. Enable Queue-it Integration

```bash
# Set environment variables
export REACT_APP_QUEUE_IT_ENABLED=true
export QUEUE_IT_ENABLED=true

# Build and deploy
./build.sh
./deploy.sh
```

### 2. Test Queue Integration

```bash
# Test queue status
curl http://localhost:8081/api/queueit/health

# Test queue for flash sale
curl http://localhost:8081/api/queueit/status/flash-sale-2024

# Enqueue a user
curl -X POST http://localhost:8081/api/queueit/enqueue \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "flash-sale-2024",
    "targetUrl": "http://localhost/flash-sale",
    "userAgent": "Mozilla/5.0...",
    "ipAddress": "127.0.0.1"
  }'
```

### 3. Trigger Queue Manually

```javascript
// In your React component
import { useQueueActions } from '../queueit/queueit-context';

const { triggerQueue } = useQueueActions();

// Trigger flash sale queue
triggerQueue('flashSale');
```

## 📁 File Structure

```
frontend/src/queueit/
├── queueit-config.js          # Configuration and constants
├── queueit-service.js         # Queue-it service logic
├── queueit-context.jsx        # React context provider
└── components/
    ├── QueueOverlay.jsx       # Full-screen queue overlay
    ├── QueueOverlay.css       # Overlay styles
    ├── QueueIndicator.jsx     # Header queue indicator
    └── QueueIndicator.css     # Indicator styles

microservice-backend/api-gateway/src/main/java/com/dharshi/apigateway/controllers/
└── QueueItController.java     # Backend queue endpoints
```

## 🔌 API Endpoints

### Queue Status
```http
GET /api/queueit/status/{eventId}
```

**Response:**
```json
{
  "isActive": true,
  "queueSize": 1500,
  "estimatedWaitTime": 15,
  "eventId": "flash-sale-2024",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Enqueue User
```http
POST /api/queueit/enqueue
```

**Request:**
```json
{
  "eventId": "flash-sale-2024",
  "targetUrl": "https://example.com/flash-sale",
  "userAgent": "Mozilla/5.0...",
  "ipAddress": "192.168.1.1"
}
```

**Response:**
```json
{
  "queueToken": "uuid-token",
  "position": 150,
  "estimatedWaitTime": 5,
  "eventId": "flash-sale-2024",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Check Position
```http
GET /api/queueit/position/{eventId}
Authorization: Bearer {queueToken}
```

**Response:**
```json
{
  "position": 45,
  "estimatedWaitTime": 3,
  "status": "queued",
  "eventId": "flash-sale-2024",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Queue Statistics
```http
GET /api/queueit/stats/{eventId}
```

**Response:**
```json
{
  "totalUsers": 2500,
  "activeUsers": 1800,
  "averageWaitTime": 12,
  "queueThroughput": 120,
  "eventId": "flash-sale-2024",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 🎯 Event Types

### 1. Flash Sale Events
- **Trigger**: `/flash-sale` URL
- **Queue Time**: 20 minutes
- **High Priority**: Yes
- **Use Case**: Limited-time sales

### 2. Black Friday Events
- **Trigger**: `/black-friday` URL
- **Queue Time**: 30 minutes
- **High Priority**: Yes
- **Use Case**: Major sales events

### 3. High Traffic Protection
- **Trigger**: `/products` URL
- **Queue Time**: 15 minutes
- **High Priority**: No
- **Use Case**: General traffic management

### 4. Checkout Protection
- **Trigger**: `/order/checkout` URL
- **Queue Time**: 10 minutes
- **High Priority**: Yes
- **Use Case**: Payment processing protection

## 🔄 Queue Flow

### 1. User Access
```
User visits protected URL
         ↓
Check if queue is active
         ↓
If active → Join queue
If inactive → Allow access
```

### 2. Queue Experience
```
User joins queue
         ↓
Show queue overlay
         ↓
Poll position every 5s
         ↓
Update progress bar
         ↓
When position = 0 → Redirect to site
```

### 3. Queue Exit
```
User reaches front of queue
         ↓
Clear queue token
         ↓
Redirect to target URL
         ↓
Allow normal site access
```

## 🛠️ Development Features

### Bypass Queue (Development Only)
```javascript
// In development environment
const { bypassQueue } = useQueueActions();
bypassQueue(); // Skips queue entirely
```

### Manual Queue Trigger
```javascript
// Trigger queue programmatically
const { triggerQueue } = useQueueActions();
triggerQueue('flashSale');
```

### Queue Status Monitoring
```javascript
// Monitor queue status
const { isQueuing, isQueued, position, estimatedWaitTime } = useQueueStatus();
```

## 📊 Monitoring and Analytics

### Queue Metrics
- **Total users in queue**
- **Average wait time**
- **Queue throughput**
- **Event-specific statistics**

### Health Monitoring
```bash
# Check queue service health
curl http://localhost:8081/api/queueit/health

# Monitor queue statistics
curl http://localhost:8081/api/queueit/stats/flash-sale-2024
```

### Logging
```bash
# View queue-related logs
docker-compose logs api-gateway | grep -i queueit

# Monitor queue events
docker-compose logs api-gateway | grep "Queue-it"
```

## 🔒 Security Considerations

### Token Management
- **Secure token generation** using UUID
- **Token expiration** after 20 minutes
- **Token validation** on each request
- **Automatic token cleanup**

### Rate Limiting
- **API rate limiting** on queue endpoints
- **Polling frequency** limited to 5 seconds
- **Request validation** for all inputs

### CORS Configuration
```yaml
allowedOrigins:
  - "https://yourdomain.com"
  - "https://www.yourdomain.com"
allowedMethods:
  - GET
  - POST
  - OPTIONS
```

## 🚨 Troubleshooting

### Common Issues

#### 1. Queue Not Triggering
```bash
# Check configuration
echo $REACT_APP_QUEUE_IT_ENABLED
echo $QUEUE_IT_ENABLED

# Check API health
curl http://localhost:8081/api/queueit/health
```

#### 2. Queue Stuck
```bash
# Clear queue tokens
localStorage.removeItem('queueit_token');
localStorage.removeItem('queueit_timestamp');

# Refresh page
window.location.reload();
```

#### 3. API Errors
```bash
# Check API Gateway logs
docker-compose logs api-gateway

# Test API endpoints
curl -v http://localhost:8081/api/queueit/status/flash-sale-2024
```

### Debug Mode
```bash
# Enable debug logging
export QUEUE_IT_DEBUG=true
export REACT_APP_QUEUE_IT_DEBUG=true

# Restart services
docker-compose restart api-gateway
```

## 📈 Performance Optimization

### Frontend Optimizations
- **Lazy loading** of queue components
- **Debounced polling** to reduce API calls
- **Efficient state management** with React context
- **Minimal re-renders** with proper memoization

### Backend Optimizations
- **Caching** of queue status
- **Connection pooling** for external API calls
- **Async processing** for queue operations
- **Rate limiting** to prevent abuse

## 🔄 Production Deployment

### 1. Environment Setup
```bash
# Production environment variables
export QUEUE_IT_CUSTOMER_ID=your-production-customer-id
export QUEUE_IT_SECRET_KEY=your-production-secret-key
export QUEUE_IT_API_KEY=your-production-api-key
export QUEUE_IT_ENABLED=true
export QUEUE_IT_DEBUG=false
```

### 2. SSL Configuration
```nginx
# Ensure HTTPS for queue operations
location /api/queueit/ {
    proxy_pass http://api-gateway:8081/api/queueit/;
    proxy_set_header X-Forwarded-Proto https;
}
```

### 3. Monitoring Setup
```bash
# Set up monitoring alerts
# Monitor queue health endpoints
# Set up log aggregation
# Configure performance monitoring
```

## 📋 Testing Checklist

### Frontend Testing
- [ ] Queue overlay displays correctly
- [ ] Position updates work
- [ ] Progress bar animates
- [ ] Bypass button works (dev only)
- [ ] Responsive design works
- [ ] Error handling works

### Backend Testing
- [ ] Queue status endpoint works
- [ ] Enqueue endpoint works
- [ ] Position polling works
- [ ] Statistics endpoint works
- [ ] Health check works
- [ ] Error handling works

### Integration Testing
- [ ] End-to-end queue flow works
- [ ] Token management works
- [ ] CORS configuration works
- [ ] SSL integration works
- [ ] Performance under load

## 📈 Monitoring & Test Result Visualization

### **Test Results**
- All test scripts output JSON and console summaries
- Results are stored in `simple_test_results.json` and logs
- Success rate, error rate, and detailed results are available

### **Grafana Dashboard Integration**

#### **Step-by-Step Guide to Setup Grafana Dashboards**

1. **Start Prometheus and Grafana**
   - Use provided Docker Compose or manual setup
   - Ensure Prometheus is scraping API Gateway metrics

2. **Access Grafana**
   - Open your browser: [http://localhost:3000](http://localhost:3000)
   - Login: `admin` / `admin` (or `admin123`)

3. **Add Prometheus Data Source**
   - Go to Configuration (gear icon) → Data Sources
   - Click "Add data source"
   - Select "Prometheus"
   - Set URL: `http://prometheus:9090`
   - Click "Save & Test"

4. **Import Dashboards**
   - Go to Dashboards → Import
   - Upload JSON files from `config/grafana_dashboards/`:
     - `queueit-comprehensive-dashboard.json`
     - `queueit-api-performance.json`
     - `queueit-frontend-metrics.json`
     - `queueit-load-testing.json`

5. **View Metrics**
   - Open the imported dashboards
   - See real-time and historical test results, API performance, queue stats, and error rates

6. **Customize Panels**
   - Add new panels for custom queries (see README for example PromQL queries)
   - Adjust time ranges and refresh intervals as needed

## 🛠️ Troubleshooting & Best Practices

- Check API Gateway and Prometheus health endpoints
- Use `./test.sh` and `./generate_metrics.sh` to generate test data
- Review logs and JSON reports for errors
- Use Grafana dashboards for real-time monitoring
- Keep environment variables and event configuration up to date
- For production, disable bypass and enable all security features

## 📚 References
- [Queue-It Official Documentation](https://queue-it.com/docs)
- [Grafana Documentation](https://grafana.com/docs/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- See `testing-projects/queueit-functional-testing/README.md` for more

---

*For additional help with Queue-it integration, refer to the troubleshooting guide or check the Queue-it documentation.* 