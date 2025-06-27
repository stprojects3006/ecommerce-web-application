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
    # CORS headers for production
    add_header Access-Control-Allow-Origin "http://18.217.148.69" always;
    add_header Access-Control-Allow-Origin "https://18.217.148.69" always;
    add_header Access-Control-Allow-Origin "http://localhost" always;
    add_header Access-Control-Allow-Origin "http://localhost:80" always;
    add_header Access-Control-Allow-Origin "http://localhost:5173" always;
    add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
    add_header Access-Control-Allow-Headers "*" always;
    add_header Access-Control-Allow-Credentials "true" always;
}
```

### 2. API Gateway CORS Configuration

The API Gateway has its own CORS configuration in `application.yml`:

```yaml
globalcors:
  corsConfigurations:
    '[/**]':
      allowedOrigins:
        # Local development origins
        - "http://localhost"
        - "http://localhost:80"
        - "http://localhost:5173"
        # Production origins (HTTP and HTTPS)
        - "http://18.217.148.69"
        - "http://18.217.148.69:80"
        - "http://18.217.148.69:8080"
        - "http://18.217.148.69:5173"
        - "https://18.217.148.69"
        - "https://18.217.148.69:80"
        - "https://18.217.148.69:8080"
        - "https://18.217.148.69:5173"
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

    // Local development origins
    configuration.addAllowedOrigin("http://localhost");
    configuration.addAllowedOrigin("http://localhost:80");
    configuration.addAllowedOrigin("http://localhost:8080");
    configuration.addAllowedOrigin("http://localhost:5173");
    
    // Production origins (HTTP and HTTPS)
    configuration.addAllowedOrigin("http://18.217.148.69");
    configuration.addAllowedOrigin("http://18.217.148.69:80");
    configuration.addAllowedOrigin("http://18.217.148.69:8080");
    configuration.addAllowedOrigin("http://18.217.148.69:5173");
    configuration.addAllowedOrigin("https://18.217.148.69");
    configuration.addAllowedOrigin("https://18.217.148.69:80");
    configuration.addAllowedOrigin("https://18.217.148.69:8080");
    configuration.addAllowedOrigin("https://18.217.148.69:5173");
    
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

### Local Development
- `http://localhost`
- `http://localhost:80`
- `http://localhost:8080`
- `http://localhost:5173`

### Production (HTTP)
- `http://18.217.148.69`
- `http://18.217.148.69:80`
- `http://18.217.148.69:8080`
- `http://18.217.148.69:5173`

### Production (HTTPS)
- `https://18.217.148.69`
- `https://18.217.148.69:80`
- `https://18.217.148.69:8080`
- `https://18.217.148.69:5173`

## Allowed Methods
- GET
- POST
- PUT
- DELETE
- OPTIONS (for preflight requests)

## Allowed Headers
- All headers (`*`)

## Credentials
- `allowCredentials: true` (for JWT token transmission)

## Max Age
- `3600` seconds (1 hour) for preflight caching

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

The frontend is configured to use the API Gateway:

```javascript
const API_BASE_URL = "http://18.217.148.69/api"
//const API_BASE_URL = "http://localhost/api" //for local development
//const API_BASE_URL = "http://18.217.148.69:8081" //for direct API Gateway access
```

## Troubleshooting CORS Issues

### Common Issues

1. **Preflight Request Failures**
   - Ensure OPTIONS method is allowed
   - Check that preflight headers are properly configured

2. **Origin Not Allowed**
   - Verify the requesting origin is in the allowed origins list
   - Check for typos in origin URLs

3. **Credentials Issues**
   - Ensure `allowCredentials: true` is set
   - Check that the frontend includes credentials in requests

### Debugging Steps

1. **Check Browser Console**
   ```javascript
   // Look for CORS errors in browser console
   console.error('CORS Error:', error);
   ```

2. **Verify Response Headers**
   ```bash
   # Check response headers for CORS headers
   curl -H "Origin: http://18.217.148.69" \
        -H "Access-Control-Request-Method: POST" \
        -H "Access-Control-Request-Headers: Content-Type" \
        -X OPTIONS \
        http://18.217.148.69/api/auth/login
   ```

3. **Test Individual Services**
   ```bash
   # Test direct service access
   curl -H "Origin: http://18.217.148.69" \
        http://18.217.148.69:8081/actuator/health
   ```

4. **Check Nginx Logs**
   ```bash
   # Check nginx access logs
   docker logs purely_nginx
   ```

5. **Check API Gateway Logs**
   ```bash
   # Check API Gateway logs
   docker logs purely_api_gateway
   ```

## Testing CORS Configuration

### Manual Testing

1. **Frontend to API Test**
   ```javascript
   fetch('http://18.217.148.69/api/auth/login', {
     method: 'POST',
     headers: {
       'Content-Type': 'application/json',
     },
     credentials: 'include',
     body: JSON.stringify({
       username: 'test@example.com',
       password: 'password'
     })
   })
   .then(response => response.json())
   .then(data => console.log(data))
   .catch(error => console.error('CORS Error:', error));
   ```

2. **Preflight Request Test**
   ```bash
   curl -X OPTIONS \
        -H "Origin: http://18.217.148.69" \
        -H "Access-Control-Request-Method: POST" \
        -H "Access-Control-Request-Headers: Content-Type" \
        -v \
        http://18.217.148.69/api/auth/login
   ```

### Automated Testing

1. **CORS Test Script**
   ```bash
   # Test all origins
   for origin in "http://localhost" "http://18.217.148.69" "https://18.217.148.69"; do
     echo "Testing origin: $origin"
     curl -H "Origin: $origin" \
          -H "Access-Control-Request-Method: GET" \
          -X OPTIONS \
          http://18.217.148.69/api/health
   done
   ```

## Security Considerations

1. **Origin Validation**
   - Only allow specific origins, not wildcards
   - Validate origins against a whitelist

2. **HTTPS Support**
   - Include HTTPS origins for production
   - Consider redirecting HTTP to HTTPS

3. **Credential Handling**
   - Use `allowCredentials: true` only when necessary
   - Ensure secure transmission of credentials

## Future Enhancements

1. **Environment-based Configuration**
   - Use environment variables for origins
   - Separate development and production configs

2. **Dynamic Origin Management**
   - Implement origin validation service
   - Add origin management API

3. **Enhanced Security**
   - Implement origin validation middleware
   - Add rate limiting for CORS requests

## Summary

The CORS configuration is implemented at multiple layers to ensure proper cross-origin request handling:

1. **Nginx**: Primary CORS handler with preflight support
2. **API Gateway**: Secondary CORS configuration for direct access
3. **Microservices**: Individual CORS configurations for service-specific needs

This multi-layered approach ensures robust CORS handling while maintaining security and flexibility for both development and production deployment scenarios. 