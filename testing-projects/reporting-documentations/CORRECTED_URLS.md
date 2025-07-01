# 🔧 Corrected URLs for Queue-It Localhost Testing

## ✅ **URL Configuration Summary**

Your application uses **nginx as a reverse proxy** to serve the frontend over **HTTPS**, not directly on port 3000. Here are the corrected URLs:

### **Backend Services**
- **API Gateway**: `http://localhost:8081`
- **Service Registry**: `http://localhost:8761`
- **Auth Service**: `http://localhost:8088`
- **Product Service**: `http://localhost:8082`
- **Category Service**: `http://localhost:8083`
- **Cart Service**: `http://localhost:8084`
- **Order Service**: `http://localhost:8085`
- **User Service**: `http://localhost:8086`
- **Notification Service**: `http://localhost:8087`

### **Frontend (via nginx)**
- **Frontend URL**: `https://localhost` (port 443)
- **Flash Sale**: `https://localhost/flash-sale`
- **Products**: `https://localhost/products`
- **Cart**: `https://localhost/cart`
- **Checkout**: `https://localhost/checkout`

### **Queue-It API Endpoints**
- **Health Check**: `http://localhost:8081/api/queueit/health`
- **Queue Status**: `http://localhost:8081/api/queueit/status/{eventId}`
- **Enqueue**: `http://localhost:8081/api/queueit/enqueue`
- **Token Validation**: `http://localhost:8081/api/queueit/validate`
- **Position Check**: `http://localhost:8081/api/queueit/position/{eventId}`
- **Queue Stats**: `http://localhost:8081/api/queueit/stats/{eventId}`

## 🔄 **What Was Updated**

### **Test Files Updated**
1. ✅ `tests/backend/test_queueit_official_connector.py`
   - Changed `targetUrl` from `http://localhost:3000` to `https://localhost`
   - Updated all frontend URL references

2. ✅ `tests/frontend/test_queueit_frontend_official.js`
   - Changed base URL from `http://localhost:3000` to `https://localhost`

3. ✅ `run_localhost_tests.py`
   - Updated frontend URL to `https://localhost`

### **Configuration Files Updated**
1. ✅ `.env.template`
   - `FRONTEND_URL=https://localhost`

2. ✅ `config/localhost_config.json`
   - Frontend URLs updated to HTTPS

3. ✅ `quick_setup_and_test.sh`
   - All frontend URL references updated

4. ✅ `setup_localhost_env.sh`
   - Service startup scripts updated

5. ✅ `start_services.sh`
   - Service verification URLs updated

### **Documentation Updated**
1. ✅ `LOCALHOST_SETUP_GUIDE.md`
   - Updated to reflect nginx setup
   - Added SSL certificate handling
   - Updated troubleshooting section

## 🚀 **How to Start Services**

### **Option 1: Using Docker Compose (Recommended)**
```bash
# Start all services including nginx
docker-compose -f docker-compose-ec2-prod.yml up -d

# Or start specific services
docker-compose -f docker-compose-ec2-prod.yml up api-gateway nginx
```

### **Option 2: Manual Startup**
```bash
# Terminal 1: Start API Gateway
cd microservice-backend/api-gateway
./mvnw spring-boot:run

# Terminal 2: Build frontend
cd frontend
npm run build

# Terminal 3: Start nginx
# Use your nginx configuration
nginx -c /path/to/nginx-ssl-ec2-prod.conf
```

## 🧪 **Running Tests**

### **Quick Test**
```bash
cd testing-projects/queueit-functional-testing
python run_localhost_tests.py
```

### **Individual Tests**
```bash
# Backend tests
python tests/backend/test_queueit_official_connector.py

# Frontend tests (if Node.js available)
npm test
```

## 🔍 **Verification Commands**

### **Check Services**
```bash
# Backend health
curl http://localhost:8081/actuator/health

# Frontend (via nginx)
curl -k https://localhost

# Queue-It health
curl http://localhost:8081/api/queueit/health
```

### **Check Ports**
```bash
# Check if services are running
lsof -i :8081  # API Gateway
lsof -i :443   # nginx (HTTPS)
lsof -i :80    # nginx (HTTP redirect)
```

## ⚠️ **Important Notes**

1. **SSL Certificates**: For localhost testing, you might need to:
   - Accept self-signed certificates in your browser
   - Use `curl -k` for testing (ignores SSL verification)
   - Configure proper SSL certificates for production

2. **CORS Configuration**: Make sure your nginx configuration includes proper CORS headers for localhost

3. **Service Dependencies**: The frontend depends on nginx being running, not just the React dev server

4. **Port Conflicts**: Ensure ports 80, 443, and 8081 are available

## 📊 **Expected Test Results**

With the corrected URLs, your tests should now properly connect to:
- ✅ Backend API Gateway on `http://localhost:8081`
- ✅ Frontend via nginx on `https://localhost`
- ✅ Queue-It integration endpoints
- ✅ All service health checks

The tests will validate the complete Queue-It integration through your actual production-like setup with nginx and SSL. 