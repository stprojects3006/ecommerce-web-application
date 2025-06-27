# CORS Configuration Guide

## Overview

This document explains the CORS (Cross-Origin Resource Sharing) configuration for the PURELY e-commerce application deployed on AWS EC2.

## EC2 Instance Details

- **Public IP**: 18.217.148.69
- **Private IP**: 172.31.26.143
- **Region**: US East (Ohio) - us-east-2

## Architecture

The application uses a microservices architecture with the following components:
- **Frontend**: React.js application served by Nginx
- **API Gateway**: Spring Cloud Gateway (port 8081)
- **Microservices**: Individual Spring Boot services
- **Nginx**: Reverse proxy serving frontend and routing API calls

## CORS Configuration Layers

### 1. Nginx (Primary CORS Handler)

Nginx acts as the primary CORS handler for the application:

```nginx
# API routes with CORS headers
location /api/ {
    proxy_pass http://api_gateway:8081/;
    
    # CORS headers for production
    add_header Access-Control-Allow-Origin "http://18.217.148.69" always;
    add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
    add_header Access-Control-Allow-Headers "*" always;
    add_header Access-Control-Allow-Credentials "true" always;
    
    # Handle preflight requests
    if ($request_method = 'OPTIONS') {
        add_header Access-Control-Allow-Origin "http://18.217.148.69" always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "*" always;
        add_header Access-Control-Allow-Credentials "true" always;
        add_header Content-Type "text/plain charset=UTF-8";
        add_header Content-Length 0;
        return 204;
    }
}
```

### 2. API Gateway CORS Configuration

The API Gateway has its own CORS configuration in `application.yml`:

```yaml
globalcors:
  corsConfigurations:
    '[/**]':
      allowedOrigins:
        - "http://18.217.148.69"
        - "http://18.217.148.69:80"
        - "http://18.217.148.69:8080"
        - "http://localhost"
        - "http://localhost:80"
        - "http://localhost:5173"
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

### 3. Individual Microservices CORS

Each microservice has its own CORS configuration in `WebSecurityConfig.java`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOrigin("http://18.217.148.69");
    configuration.addAllowedOrigin("http://18.217.148.69:80");
    configuration.addAllowedOrigin("http://18.217.148.69:8080");
    configuration.addAllowedOrigin("http://localhost");
    configuration.addAllowedOrigin("http://localhost:80");
    configuration.addAllowedOrigin("http://localhost:5173");
    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

## Request Flow

1. **Frontend Request**: `http://18.217.148.69/api/auth/signin`
2. **Nginx Proxy**: Routes to `http://api-gateway:8081/auth/signin`
3. **API Gateway**: Routes to `http://auth-service:8081/auth/signin`
4. **Auth Service**: Processes the request

## Allowed Origins

### Production (EC2)
- `http://18.217.148.69`
- `http://18.217.148.69:80`
- `http://18.217.148.69:8080`

### Development
- `http://localhost`
- `http://localhost:80`
- `http://localhost:5173`

## Allowed Methods
- GET
- POST
- PUT
- DELETE
- OPTIONS (for preflight requests)

## Allowed Headers
- All headers (`*`)

## Credentials
- `allowCredentials: true` - Allows cookies and authorization headers

## Service Discovery Configuration

### Eureka Service Registry
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://172.31.26.143:8761/eureka/
  instance:
    hostname: 172.31.26.143
    prefer-ip-address: true
```

### API Gateway Service Discovery
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://172.31.26.143:8761/eureka/
  instance:
    hostname: 172.31.26.143
    prefer-ip-address: true
```

## Frontend Configuration

### Production API Configuration
```javascript
// frontend/src/api-service/apiConfig.jsx
const API_BASE_URL = "http://18.217.148.69/api"
```

### Development API Configuration
```javascript
// frontend/src/api-service/apiConfig.jsx
const API_BASE_URL = "http://localhost/api"
```

## Troubleshooting CORS Issues

### Common Issues

1. **Port Mismatch**
   - Ensure API Gateway runs on port 8081
   - Ensure nginx proxies to the correct port

2. **Origin Not Allowed**
   - Check if the frontend origin is in the allowed origins list
   - Verify the protocol (http/https) matches

3. **Preflight Request Failing**
   - Ensure OPTIONS method is allowed
   - Check that preflight response has correct headers

4. **Credentials Not Sent**
   - Ensure `allowCredentials: true` is set
   - Check that `Access-Control-Allow-Credentials` header is present

### Debugging Steps

1. **Check Browser Console**
   ```javascript
   // Look for CORS errors in browser console
   ```

2. **Check Network Tab**
   - Look for failed OPTIONS requests
   - Check response headers for CORS headers

3. **Test with curl**
   ```bash
   # Test preflight request
   curl -X OPTIONS -H "Origin: http://18.217.148.69" \
        -H "Access-Control-Request-Method: POST" \
        -H "Access-Control-Request-Headers: Content-Type" \
        -v http://18.217.148.69/api/auth/signin
   ```

4. **Check Service Logs**
   ```bash
   # Check nginx logs
   docker-compose logs nginx
   
   # Check API Gateway logs
   docker-compose logs api-gateway
   
   # Check specific service logs
   docker-compose logs auth-service
   ```

## Security Considerations

1. **Origin Validation**: Only allow trusted origins
2. **Method Restriction**: Only allow necessary HTTP methods
3. **Header Validation**: Be specific about allowed headers
4. **Credentials**: Only allow credentials when necessary

## Environment-Specific Configuration

### Production (EC2)
```javascript
// frontend/src/api-service/apiConfig.jsx
const API_BASE_URL = "http://18.217.148.69/api"
```

### Development
```javascript
// frontend/src/api-service/apiConfig.jsx
const API_BASE_URL = "http://localhost/api"
```

## Testing CORS Configuration

1. **Start the application**
   ```bash
   ./build.sh
   ./deploy.sh
   ```

2. **Access the frontend**
   - Open http://18.217.148.69 in browser

3. **Test API calls**
   - Try to login/register
   - Check browser console for CORS errors

4. **Verify monitoring**
   - Check Prometheus: http://18.217.148.69:9090
   - Check Grafana: http://18.217.148.69:3000

## EC2-Specific Considerations

### Security Groups
Ensure the following ports are open in your EC2 security group:
- Port 80 (HTTP)
- Port 8081 (API Gateway)
- Port 8761 (Service Registry)
- Port 9090 (Prometheus)
- Port 3000 (Grafana)

### Network Configuration
- Use private IP (172.31.26.143) for internal service communication
- Use public IP (18.217.148.69) for external access
- Configure proper DNS if using a domain name

### SSL/HTTPS
For production, consider:
- Setting up SSL certificates
- Configuring HTTPS
- Updating CORS origins to include https://

## Summary

The CORS configuration is implemented at multiple layers to ensure proper cross-origin request handling:

1. **Nginx**: Primary CORS handler with preflight support
2. **API Gateway**: Secondary CORS configuration for direct access
3. **Microservices**: Individual CORS configurations for service-specific needs

This multi-layered approach ensures robust CORS handling while maintaining security and flexibility for both development and production deployment scenarios. 