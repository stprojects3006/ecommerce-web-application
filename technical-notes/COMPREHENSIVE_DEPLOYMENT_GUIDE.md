# PURELY E-commerce Application - Deployment Documentation

## Overview
This document provides comprehensive documentation for the PURELY E-commerce microservices application, including all fixes, configurations, and deployment procedures implemented during development and troubleshooting.

## Table of Contents
1. [System Architecture](#system-architecture)
2. [Prerequisites](#prerequisites)
3. [Build and Deployment](#build-and-deployment)
4. [Key Fixes and Configurations](#key-fixes-and-configurations)
5. [Service Configuration Details](#service-configuration-details)
6. [Monitoring Setup](#monitoring-setup)
7. [Troubleshooting Guide](#troubleshooting-guide)
8. [AWS EC2 Deployment](#aws-ec2-deployment)

## System Architecture

### Microservices Stack
- **Service Registry**: Eureka Server (Port 8761)
- **API Gateway**: Spring Cloud Gateway (Port 8081)
- **Auth Service**: User authentication and JWT management
- **User Service**: User profile management
- **Category Service**: Product category management
- **Product Service**: Product catalog management
- **Cart Service**: Shopping cart functionality
- **Order Service**: Order processing and management
- **Notification Service**: Email notifications

### Frontend
- **React Application**: Modern SPA with Vite build system
- **Nginx**: Reverse proxy and static file serving

### Monitoring Stack
- **Prometheus**: Metrics collection
- **Grafana**: Metrics visualization
- **Promtail**: Log aggregation
- **Blackbox Exporter**: Endpoint monitoring
- **Node Exporter**: System metrics

### Database
- **MongoDB**: Primary data store

## Prerequisites

### Required Software
- Java 17 (OpenJDK)
- Maven 3.8+
- Docker & Docker Compose
- Node.js 18+ (for frontend build)

### System Requirements
- Minimum 4GB RAM
- 20GB free disk space
- Ports: 80, 8081, 8761, 27017, 9090, 3000, 9113, 9115, 9100

## Build and Deployment

## first edit the API_BASE_URL in frontend/src/api-service/apiConfig.jsx with ip address of API server

### Automated Deployment
The application includes comprehensive deployment scripts:

```bash
# Full deployment (builds and starts everything)
./deploy.sh

# Build JARs only
./build-jars-only.sh

# Build frontend only
cd frontend && npm run build
```

### Manual Deployment Steps
1. **Build Microservices**:
   ```bash
   ./build-jars-only.sh
   ```

2. **Build Frontend**:
   ```bash
   cd frontend
   npm install
   npm run build
   cd ..
   ```

3. **Start Services**:
   ```bash
   docker-compose up -d
   ```

## Key Fixes and Configurations

### 1. Java Version Compatibility
**Issue**: Java version conflicts between Java 24 and project requirements
**Solution**: 
- Uninstalled all Java versions
- Installed OpenJDK 17
- Updated all `pom.xml` files to use Java 17 and Maven compiler plugin 3.12.1

### 2. API Gateway Configuration
**Issue**: Missing web dependencies and CORS configuration
**Solution**:
- Added `spring.main.web-application-type=reactive` to application.yml
- Replaced servlet-based `CorsFilter` with reactive `CorsWebFilter`
- Removed conflicting `spring-boot-starter-web` dependency

### 3. Eureka Service Registration
**Issue**: Services not registering with Eureka due to hardcoded IP addresses
**Solution**:
- Updated all microservices to use Docker service names instead of IP addresses
- Configured Eureka client URLs to use service names:
  ```yaml
  eureka:
    client:
      serviceUrl:
        defaultZone: http://service-registry:8761/eureka/
  ```

### 4. Frontend API Configuration
**Issue**: Frontend configured for production API URLs
**Solution**:
- Updated `frontend/src/api-service/apiConfig.jsx` to use localhost URLs
- Configured for development environment

### 5. Nginx Configuration
**Issue**: Static file serving and API routing problems
**Solution**:
- Fixed MIME type configuration for JavaScript files
- Corrected API upstream service names
- Disabled caching to prevent stale content
- Added proper proxy_pass configurations

### 6. Authentication and User Management
**Issue**: User creation not automated after signup
**Solution**:
- Added user creation endpoint in user service
- Automated user creation in frontend after successful verification
- Fixed token handling in cart and order services

### 7. Cart Service Token Handling
**Issue**: "Cannot read properties of null (reading 'token')" error
**Solution**:
- Updated cart and order services to get user data dynamically from localStorage
- Fixed static user variable initialization that caused stale data issues

## Service Configuration Details

### API Gateway (application.yml)
```yaml
spring:
  main:
    web-application-type: reactive
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/auth-service/**
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/user-service/**
        # ... other service routes
```

### Auth Service Security
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()  // Health checks
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

### Frontend API Configuration
```javascript
// frontend/src/api-service/apiConfig.jsx
const API_BASE_URL = 'http://localhost:8081';
export default API_BASE_URL;
```

### Nginx Configuration
```nginx
# Static file serving with correct MIME types
location ~* \.(js|mjs)$ {
    add_header Content-Type application/javascript;
}

# API routing
location /api/ {
    proxy_pass http://api-gateway:8081/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
```

## Monitoring Setup

### Prometheus Configuration
- **Location**: `prometheus/prometheus.yml`
- **Targets**: All microservices, nginx, blackbox exporter
- **Scrape interval**: 15s

### Grafana Configuration
- **Default credentials**: admin/admin
- **Dashboards**: Pre-configured for microservices monitoring
- **Data source**: Prometheus

### Log Aggregation
- **Promtail**: Collects application logs
- **Configuration**: `prometheus/promtail-config.yml`

## Troubleshooting Guide

### Common Issues and Solutions

1. **Services Not Starting**
   - Check Docker logs: `docker-compose logs [service-name]`
   - Verify port availability
   - Check service dependencies

2. **Frontend Blank Page**
   - Clear browser cache
   - Check nginx logs: `docker-compose logs nginx`
   - Verify static files are served correctly

3. **API Gateway 404 Errors**
   - Check Eureka registration: http://localhost:8761
   - Verify service health endpoints
   - Check route configurations

4. **Authentication Issues**
   - Verify JWT token format
   - Check auth service logs
   - Ensure user exists in both auth and user services

5. **Cart/Order Issues**
   - Check user session in localStorage
   - Verify token is valid and not expired
   - Check service communication logs

### Health Check Endpoints
- Service Registry: http://localhost:8761
- API Gateway: http://localhost:8081/actuator/health
- Auth Service: http://localhost:8082/actuator/health
- Product Service: http://localhost:8084/actuator/health
- Cart Service: http://localhost:8085/actuator/health
- Order Service: http://localhost:8086/actuator/health

## AWS EC2 Deployment

### EC2 Instance Setup
1. **Launch EC2 Instance**:
   - AMI: Amazon Linux 2 or Ubuntu 20.04+
   - Instance Type: t3.medium or larger
   - Security Groups: Open ports 80, 443, 22, 8081, 8761

2. **Install Dependencies**:
   ```bash
   # Update system
   sudo yum update -y  # Amazon Linux
   # or
   sudo apt update && sudo apt upgrade -y  # Ubuntu

   # Install Java 17
   sudo yum install java-17-amazon-corretto -y  # Amazon Linux
   # or
   sudo apt install openjdk-17-jdk -y  # Ubuntu

   # Install Docker
   sudo yum install docker -y  # Amazon Linux
   # or
   sudo apt install docker.io docker-compose -y  # Ubuntu

   # Start and enable Docker
   sudo systemctl start docker
   sudo systemctl enable docker
   sudo usermod -aG docker $USER
   ```

3. **Deploy Application**:
   ```bash
   # Clone repository
   git clone <repository-url>
   cd ecommerce-web-application

   # Run deployment script
   ./deploy.sh
   ```

### Production Considerations
1. **SSL/TLS**: Configure HTTPS with Let's Encrypt
2. **Domain**: Point domain to EC2 public IP
3. **Database**: Consider using managed MongoDB service
4. **Monitoring**: Set up CloudWatch for additional monitoring
5. **Backup**: Implement automated backup strategies

### Environment Variables
For production deployment, consider using environment variables for:
- Database connection strings
- JWT secrets
- Email service credentials
- API keys

## Sample Data Import

The application includes sample data for testing:
```bash
# Import categories
mongoimport --db purely_category_service --collection categories --file "sample data/purely_category_service.categories.json"

# Import products
mongoimport --db purely_product_service --collection products --file "sample data/purely_product_service.products.json"
```

## Performance Optimization

### Frontend
- Vite build system for fast development and optimized production builds
- Code splitting and lazy loading
- Optimized static asset serving

### Backend
- Connection pooling for database connections
- Caching strategies for frequently accessed data
- Load balancing through Eureka service discovery

### Monitoring
- Real-time metrics collection
- Automated alerting for service health
- Performance dashboards

## Security Considerations

1. **JWT Token Management**: Secure token storage and validation
2. **CORS Configuration**: Properly configured for production domains
3. **Input Validation**: Server-side validation for all inputs
4. **HTTPS**: SSL/TLS encryption for all communications
5. **Database Security**: Secure MongoDB configuration

## Support and Maintenance

### Regular Maintenance Tasks
1. **Log Rotation**: Configure log rotation for application logs
2. **Database Maintenance**: Regular MongoDB maintenance
3. **Security Updates**: Keep dependencies updated
4. **Backup Verification**: Test backup and restore procedures

### Monitoring Alerts
- Service health monitoring
- Error rate tracking
- Performance metrics
- Resource utilization

---

**Document Version**: 1.0  
**Last Updated**: December 2024  
**Contact**: stprojects3006@gmail.com

This documentation covers all the fixes, configurations, and deployment procedures implemented during the development and troubleshooting of the PURELY E-commerce application. 