# Port Configuration Guide

## 🚀 Microservices Port Configuration

### **Service Registry (Eureka)**
- **Internal Port**: 8761
- **External Port**: 8761
- **Purpose**: Service discovery and registration
- **Access**: Internal only (backend network)
- **Health Check**: `http://localhost:8761/actuator/health`

### **API Gateway**
- **Internal Port**: 8081
- **External Port**: 8081
- **Purpose**: Central routing and load balancing
- **Access**: Frontend → API Gateway → Microservices
- **Health Check**: `http://localhost:8081/actuator/health`
- **Nginx Route**: `/api/*` → `api-gateway:8081`

### **Auth Service**
- **Internal Port**: 8081
- **External Port**: 8088 (mapped to internal 8081)
- **Purpose**: Authentication and authorization
- **Access**: Through API Gateway only
- **Health Check**: `http://localhost:8081/actuator/health`
- **API Gateway Route**: `/auth/*`

### **Category Service**
- **Internal Port**: 8082
- **External Port**: 8082
- **Purpose**: Product category management
- **Access**: Through API Gateway only
- **Health Check**: `http://localhost:8082/actuator/health`
- **API Gateway Route**: `/category/*`

### **Product Service**
- **Internal Port**: 8083
- **External Port**: 8083
- **Purpose**: Product management
- **Access**: Through API Gateway only
- **Health Check**: `http://localhost:8083/actuator/health`
- **API Gateway Route**: `/product/*`

### **Cart Service**
- **Internal Port**: 8084
- **External Port**: 8084
- **Purpose**: Shopping cart management
- **Access**: Through API Gateway only
- **Health Check**: `http://localhost:8084/actuator/health`
- **API Gateway Route**: `/cart/*`

### **Order Service**
- **Internal Port**: 8085
- **External Port**: 8085
- **Purpose**: Order management
- **Access**: Through API Gateway only
- **Health Check**: `http://localhost:8085/actuator/health`
- **API Gateway Route**: `/order/*`

### **User Service**
- **Internal Port**: 8086
- **External Port**: 8086
- **Purpose**: User profile management
- **Access**: Through API Gateway only
- **Health Check**: `http://localhost:8086/actuator/health`
- **API Gateway Route**: `/user/*`

### **Notification Service**
- **Internal Port**: 8087
- **External Port**: 8087
- **Purpose**: Email notifications
- **Access**: Through API Gateway only
- **Health Check**: `http://localhost:8087/actuator/health`
- **API Gateway Route**: `/notification/*`

## 🌐 Frontend and Proxy Configuration

### **Nginx (Reverse Proxy)**
- **Port**: 80 (HTTP), 443 (HTTPS)
- **Purpose**: Frontend serving and API routing
- **Frontend**: Serves static files from `/usr/share/nginx/html`
- **API Routing**: `/api/*` → `api-gateway:8081`
- **Health Check**: `http://localhost/health`
- **Status**: `http://localhost:8080/nginx_status`

### **Frontend Development**
- **Port**: 5173 (Vite dev server)
- **Purpose**: Development and hot reloading
- **Access**: Direct access during development

## 📊 Monitoring and Observability

### **Prometheus**
- **Port**: 9090
- **Purpose**: Metrics collection and storage
- **Access**: Internal monitoring network

### **Grafana**
- **Port**: 3000
- **Purpose**: Metrics visualization and dashboards
- **Access**: Internal monitoring network

### **Node Exporter**
- **Port**: 9100
- **Purpose**: System metrics collection
- **Access**: Internal monitoring network

### **Nginx Exporter**
- **Port**: 9113
- **Purpose**: Nginx metrics collection
- **Access**: Internal monitoring network

### **Blackbox Exporter**
- **Port**: 9115
- **Purpose**: Endpoint availability monitoring
- **Access**: Internal monitoring network

### **Promtail**
- **Port**: 9080
- **Purpose**: Log aggregation
- **Access**: Internal monitoring network

## 🔧 Database Configuration

### **MongoDB**
- **Port**: 27017
- **Purpose**: Primary database
- **Access**: Internal database network only
- **Authentication**: admin/password

## 🛡️ Security and Network Isolation

### **Network Segregation**
- **Frontend Network**: Nginx, API Gateway
- **Backend Network**: All microservices, API Gateway
- **Database Network**: MongoDB, microservices with DB access
- **Monitoring Network**: All monitoring tools

### **Port Security**
- **External Access**: Only ports 80, 443, 8761 (Eureka UI)
- **Internal Communication**: All microservices communicate via internal networks
- **Health Checks**: All services have health check endpoints

## 📋 API Gateway Routing

### **Route Configuration**
```
/api/auth/*     → Auth Service (8088)
/api/category/* → Category Service (8082)
/api/product/*  → Product Service (8083)
/api/cart/*     → Cart Service (8084)
/api/order/*    → Order Service (8085)
/api/user/*     → User Service (8086)
/api/notification/* → Notification Service (8087)
```

### **Load Balancing**
- API Gateway uses Eureka for service discovery
- Automatic load balancing across service instances
- Circuit breaker pattern for fault tolerance

## 🚨 Important Notes

### **Port Conflicts Resolved**
- ✅ Auth Service moved from 8081 to 8088 (external)
- ✅ API Gateway keeps 8081 for external access
- ✅ All other services have unique ports

### **Docker Image Updates**
- ✅ All services use `openjdk:21-jre-slim` for better performance
- ✅ Consistent image versions across all microservices

### **Health Check Endpoints**
- ✅ All services expose `/actuator/health`
- ✅ Docker health checks configured
- ✅ Nginx health check at `/health`

### **CORS Configuration**
- ✅ Frontend: `http://18.217.148.69`
- ✅ API Gateway handles CORS for all microservices
- ✅ Preflight request handling configured

## 🔍 Troubleshooting

### **Port Conflicts**
If you encounter port conflicts:
1. Check if ports are already in use: `netstat -tulpn | grep :<port>`
2. Verify Docker containers are not running: `docker ps`
3. Check service configuration files

### **Service Discovery Issues**
1. Verify Service Registry is running: `http://localhost:8761`
2. Check service registration in Eureka UI
3. Verify network connectivity between services

### **API Gateway Issues**
1. Check API Gateway logs: `docker logs purely_api_gateway`
2. Verify routing configuration
3. Test direct service access vs. through gateway

### **Nginx Issues**
1. Check nginx configuration: `nginx -t`
2. Verify proxy_pass configuration
3. Check nginx logs: `docker logs purely_nginx` 