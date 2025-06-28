# PURELY E-commerce Application - Troubleshooting Guide

## 🚨 Quick Diagnostic Commands

### **System Health Check**
```bash
# Check all container status
docker-compose ps

# Check container health
docker-compose ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Check all logs
docker-compose logs --tail=50

# Check specific service logs
docker-compose logs --tail=100 api-gateway
```

---

## 🔗 Service Connectivity Issues

### **1. Service Not Starting**

#### **Symptoms:**
- Container exits immediately
- "Port already in use" errors
- "Connection refused" errors

#### **Diagnostic Commands:**
```bash
# Check if port is already in use
netstat -tulpn | grep :8081

# Check container logs
docker-compose logs api-gateway

# Check container status
docker inspect purely_api_gateway | grep -A 10 "State"

# Check if service is listening
docker exec purely_api_gateway netstat -tulpn
```

#### **Solutions:**
```bash
# Stop conflicting process
sudo fuser -k 8081/tcp

# Restart service
docker-compose restart api-gateway

# Rebuild service
docker-compose up -d --build api-gateway

# Clean restart
docker-compose down && docker-compose up -d
```

### **2. Service Discovery Issues (Eureka)**

#### **Symptoms:**
- Services not appearing in Eureka dashboard
- "Service not found" errors
- API Gateway routing failures

#### **Diagnostic Commands:**
```bash
# Check Eureka dashboard
curl -s http://localhost:8761/eureka/apps | grep -A 5 "PRODUCT-SERVICE"

# Check service registration
docker exec purely_api_gateway curl -s http://service-registry:8761/eureka/apps

# Check Eureka logs
docker-compose logs service-registry | grep -i "register\|deregister"

# Check network connectivity
docker exec purely_api_gateway ping service-registry
```

#### **Solutions:**
```bash
# Restart service registry
docker-compose restart service-registry

# Restart problematic service
docker-compose restart product-service

# Check Eureka configuration
docker exec purely_product_service cat /app/application.yml | grep eureka

# Force service re-registration
docker-compose restart api-gateway
```

### **3. Inter-Service Communication**

#### **Symptoms:**
- Timeout errors between services
- Connection refused errors
- Network unreachable errors

#### **Diagnostic Commands:**
```bash
# Test direct service communication
docker exec purely_api_gateway curl -v --max-time 10 http://product-service:8083/product/get/all

# Check network connectivity
docker exec purely_api_gateway ping product-service

# Check DNS resolution
docker exec purely_api_gateway nslookup product-service

# Check container networks
docker network inspect backend-network
```

#### **Solutions:**
```bash
# Restart network
docker-compose down
docker network prune -f
docker-compose up -d

# Check network configuration
docker network ls
docker network inspect backend-network

# Restart specific service
docker-compose restart product-service
```

---

## 🌐 CORS (Cross-Origin Resource Sharing) Issues

### **1. CORS Preflight Failures**

#### **Symptoms:**
- Browser console shows CORS errors
- "Access-Control-Allow-Origin" errors
- Preflight OPTIONS requests failing

#### **Diagnostic Commands:**
```bash
# Test CORS preflight
curl -X OPTIONS -H "Origin: http://localhost" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v http://localhost:8081/auth-service/auth/login

# Check API Gateway CORS headers
curl -H "Origin: http://localhost" -v http://localhost:8081/product-service/product/get/all

# Check nginx CORS headers
curl -H "Origin: http://localhost" -v http://localhost:8080/api/product-service/product/get/all
```

#### **Solutions:**
```bash
# Update API Gateway CORS configuration
# Edit: microservice-backend/api-gateway/src/main/resources/application.yml
# Add your domain to allowedOrigins

# Update nginx CORS configuration
# Edit: nginx.conf
# Add proper CORS headers

# Restart services
docker-compose restart api-gateway nginx
```

### **2. CORS Configuration Issues**

#### **Common CORS Configurations:**

**API Gateway (application.yml):**
```yaml
globalcors:
  corsConfigurations:
    '[/**]':
      allowedOrigins:
        - "http://localhost"
        - "http://localhost:80"
        - "http://localhost:5173"
        - "https://yourdomain.com"
        - "https://www.yourdomain.com"
      allowedMethods:
        - GET
        - POST
        - PUT
        - DELETE
        - OPTIONS
      allowedHeaders:
        - "*"
      allowCredentials: true
      maxAge: 3600
```

**Nginx CORS Headers:**
```nginx
add_header Access-Control-Allow-Origin "https://yourdomain.com" always;
add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
add_header Access-Control-Allow-Headers "*" always;
add_header Access-Control-Allow-Credentials "true" always;
```

---

## 🌍 HTTP Error Troubleshooting

### **1. 404 Not Found Errors**

#### **Symptoms:**
- API endpoints returning 404
- Frontend routes not working
- Static files not loading

#### **Diagnostic Commands:**
```bash
# Test API endpoint directly
curl -v http://localhost:8081/product-service/product/get/all

# Test through nginx
curl -v http://localhost:8080/api/product-service/product/get/all

# Check API Gateway routes
docker exec purely_api_gateway curl -s http://localhost:8081/actuator/mappings

# Check nginx configuration
docker exec purely_nginx nginx -t

# Check frontend files
docker exec purely_nginx ls -la /usr/share/nginx/html/
```

#### **Solutions:**
```bash
# Check API Gateway routing configuration
# Verify routes in application.yml

# Check nginx upstream configuration
# Verify proxy_pass settings

# Restart services
docker-compose restart api-gateway nginx

# Rebuild frontend
cd frontend && npm run build && cd ..
docker-compose restart nginx
```

### **2. 500 Internal Server Error**

#### **Symptoms:**
- Server errors in API responses
- Application crashes
- Database connection issues

#### **Diagnostic Commands:**
```bash
# Check service logs
docker-compose logs product-service --tail=50

# Check database connectivity
docker exec purely_product_service curl -v http://localhost:8083/actuator/health

# Check MongoDB connection
docker exec purely_mongodb mongosh --eval "db.adminCommand('ping')"

# Check JVM memory
docker exec purely_product_service jstat -gc 1 1000
```

#### **Solutions:**
```bash
# Restart problematic service
docker-compose restart product-service

# Check database connection string
# Verify MongoDB credentials and connection

# Check application properties
docker exec purely_product_service cat /app/application.yml

# Increase memory if needed
# Add to docker-compose.yml:
# environment:
#   JAVA_OPTS: "-Xmx1g -Xms512m"
```

### **3. 401 Unauthorized Errors**

#### **Symptoms:**
- Authentication failures
- JWT token issues
- Authorization problems

#### **Diagnostic Commands:**
```bash
# Test authentication endpoint
curl -X POST -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}' \
  http://localhost:8081/auth-service/auth/login

# Check JWT token
# Decode JWT token at jwt.io

# Check auth service logs
docker-compose logs auth-service | grep -i "auth\|jwt\|token"
```

#### **Solutions:**
```bash
# Check JWT configuration
# Verify JWT_SECRET in environment

# Restart auth service
docker-compose restart auth-service

# Check user creation
# Verify user exists in database
```

### **4. 503 Service Unavailable**

#### **Symptoms:**
- Service temporarily unavailable
- Health check failures
- Load balancer issues

#### **Diagnostic Commands:**
```bash
# Check service health
curl -v http://localhost:8081/actuator/health

# Check all service health endpoints
for service in api-gateway product-service auth-service; do
  echo "=== $service ==="
  docker exec purely_${service} curl -s http://localhost:8081/actuator/health
done

# Check health check configuration
docker inspect purely_api_gateway | grep -A 10 "Health"
```

#### **Solutions:**
```bash
# Restart unhealthy service
docker-compose restart api-gateway

# Check health check logs
docker-compose logs api-gateway | grep -i "health"

# Verify health check endpoint
# Ensure /actuator/health is accessible
```

---

## 🔧 Advanced Troubleshooting

### **1. Database Connection Issues**

#### **Diagnostic Commands:**
```bash
# Check MongoDB status
docker-compose logs mongodb

# Test MongoDB connection
docker exec purely_mongodb mongosh --eval "db.adminCommand('ping')"

# Check MongoDB authentication
docker exec purely_mongodb mongosh -u admin -p password --authenticationDatabase admin

# Check service database connection
docker exec purely_product_service curl -s http://localhost:8083/actuator/health
```

#### **Solutions:**
```bash
# Restart MongoDB
docker-compose restart mongodb

# Check MongoDB credentials
# Verify connection strings in application.yml

# Reset MongoDB if needed
docker-compose down
docker volume rm ecommerce-web-application_mongodb_data
docker-compose up -d
```

### **2. Memory and Performance Issues**

#### **Diagnostic Commands:**
```bash
# Check container resource usage
docker stats --no-stream

# Check JVM memory
docker exec purely_api_gateway jstat -gc 1 1000

# Check system memory
free -h

# Check disk space
df -h
```

#### **Solutions:**
```bash
# Increase container memory
# Add to docker-compose.yml:
# deploy:
#   resources:
#     limits:
#       memory: 1G

# Restart with more memory
docker-compose down
docker-compose up -d

# Clean up Docker
docker system prune -f
```

### **3. Network and DNS Issues**

#### **Diagnostic Commands:**
```bash
# Check Docker networks
docker network ls
docker network inspect backend-network

# Test inter-container communication
docker exec purely_api_gateway ping product-service

# Check DNS resolution
docker exec purely_api_gateway nslookup product-service

# Check container IPs
docker inspect purely_api_gateway | grep IPAddress
```

#### **Solutions:**
```bash
# Recreate networks
docker-compose down
docker network prune -f
docker-compose up -d

# Check network configuration
# Verify services are on correct networks
```

---

## 📋 Troubleshooting Checklist

### **Before Starting:**
- [ ] Check all containers are running: `docker-compose ps`
- [ ] Verify ports are not in use: `netstat -tulpn | grep :8081`
- [ ] Check Docker daemon is running: `docker info`

### **Service Issues:**
- [ ] Check service logs: `docker-compose logs <service-name>`
- [ ] Verify service health: `curl http://localhost:<port>/actuator/health`
- [ ] Check service registration in Eureka: `http://localhost:8761`

### **Network Issues:**
- [ ] Test inter-service communication
- [ ] Check Docker networks
- [ ] Verify DNS resolution

### **CORS Issues:**
- [ ] Check browser console for CORS errors
- [ ] Verify CORS configuration in API Gateway
- [ ] Test preflight requests

### **Database Issues:**
- [ ] Check MongoDB status
- [ ] Verify connection strings
- [ ] Test database connectivity

---

## 🆘 Emergency Recovery

### **Complete Reset:**
```bash
# Stop everything
docker-compose down

# Remove all containers and networks
docker-compose down --remove-orphans
docker system prune -f

# Rebuild everything
./build.sh
./deploy.sh
```

### **Service-Specific Reset:**
```bash
# Reset specific service
docker-compose stop api-gateway
docker-compose rm -f api-gateway
docker-compose up -d --build api-gateway
```

### **Database Reset:**
```bash
# Reset MongoDB (WARNING: Data loss)
docker-compose down
docker volume rm ecommerce-web-application_mongodb_data
docker-compose up -d
```

---

*For additional help, refer to the COMPREHENSIVE_DEPLOYMENT_GUIDE.md or check the service-specific logs.*
