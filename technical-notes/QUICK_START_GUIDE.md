# PURELY E-commerce Application - Deployment Guide

## Quick Start

### Prerequisites
- Docker and Docker Compose installed
- Maven installed
- Node.js installed
- At least 4GB of available RAM
- Git installed

### 1. Clone and Setup
```bash
git clone <repository-url>
cd ecommerce-web-application
chmod +x build.sh deploy.sh
```

### 2. Environment Configuration
```bash
cp env.example .env
nano .env
```

### 3. Build the Application
```bash
# Build all microservices and frontend
./zz-automationscripts/build.sh
```

### 4. Deploy the Application
```bash
# Deploy using pre-built artifacts
./zz-automationscripts/deploy.sh
```

## Service Architecture

| Service | Port | Description |
|---------|------|-------------|
| Service Registry | 8761 | Eureka service discovery |
| API Gateway | 8081 | Centralized API entry point |
| Auth Service | 8081 | Authentication & authorization |
| Category Service | 8082 | Product category management |
| Product Service | 8083 | Product catalog management |
| Cart Service | 8084 | Shopping cart operations |
| Order Service | 8085 | Order processing |
| User Service | 8086 | User profile management |
| Notification Service | 8087 | Email notifications |
| Frontend | 80 | React.js web application (via Nginx) |
| MongoDB | 27017 | Database |
| Nginx | 80 | Reverse proxy |
| Prometheus | 9090 | Metrics collection |
| Grafana | 3000 | Monitoring dashboard |
| Nginx Exporter | 9113 | Nginx metrics |
| Blackbox Exporter | 9115 | HTTP endpoint monitoring |
| Node Exporter | 9100 | Host metrics |

## Access Points

- **Frontend**: http://localhost
- **API Gateway**: http://localhost:8081
- **Service Registry**: http://localhost:8761
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Nginx Exporter**: http://localhost:9113/metrics
- **Blackbox Exporter**: http://localhost:9115
- **Node Exporter**: http://localhost:9100/metrics

## Build Process

### Building Microservices
The build script automatically:
1. Builds all microservices using Maven
2. Copies JAR files to `./jars/` directory
3. Builds frontend for production
4. Updates API configuration for production

### Required JAR Files
- service-registry.jar
- api-gateway.jar
- auth-service.jar
- category-service.jar
- product-service.jar
- cart-service.jar
- order-service.jar
- user-service.jar
- notification-service.jar

## Deployment Process

### Pre-deployment Checks
- Docker and Docker Compose availability
- Required JAR files presence
- Frontend dist directory presence

### Service Startup Order
1. MongoDB
2. Service Registry
3. All other microservices
4. Nginx with static frontend
5. Monitoring stack

## Useful Commands

```bash
# Build the application
./zz-automationscripts/build.sh

# Deploy the application
./zz-automationscripts/deploy.sh

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Restart services
docker-compose restart

# Restart specific service
docker-compose restart api-gateway

# Check status
docker-compose ps
```

## Monitoring Setup

### Grafana Dashboards
1. Access Grafana at http://localhost:3000
2. Login with admin/admin (default)
   changed pwd is = password
3. Add Prometheus as data source (http://prometheus:9090)
4. Import dashboards for:
   - Spring Boot metrics
   - Node Exporter metrics
   - Nginx metrics
   - Blackbox monitoring

### Available Metrics
- Application metrics via Spring Boot Actuator
- Nginx request/response metrics
- Host system metrics
- HTTP endpoint health checks

## Troubleshooting

1. **Build failures**: Check Maven and Node.js installation
2. **JAR files missing**: Run `./zz-automationscripts/build.sh` first
3. **Services not starting**: Check logs with `docker-compose logs service-name`
4. **Database issues**: Ensure MongoDB is running and accessible
5. **Email notifications**: Configure email settings in .env file
6. **Frontend issues**: Check if all backend services are healthy

## Production Considerations

1. **Security**: Change default passwords and enable HTTPS
2. **Scaling**: Use external databases and load balancers
3. **Monitoring**: Set up alerting rules in Prometheus
4. **Backup**: Implement regular database backups
5. **Logging**: Configure centralized logging with ELK stack

For more detailed information, see the main README.md file. 