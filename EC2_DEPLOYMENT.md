# AWS EC2 Deployment Guide

## Overview

This guide explains how to deploy the PURELY e-commerce application on an AWS EC2 instance.

## EC2 Instance Details

- **Public IP**: 18.217.148.69
- **Private IP**: 172.31.26.143
- **Region**: US East (Ohio) - us-east-2

## Prerequisites

### 1. EC2 Instance Setup

Ensure your EC2 instance has:
- **Operating System**: Ubuntu 20.04 LTS or later
- **Instance Type**: t3.medium or larger (minimum 4GB RAM)
- **Storage**: At least 20GB free space
- **Security Groups**: Configured to allow the following ports:
  - Port 22 (SSH)
  - Port 80 (HTTP)
  - Port 443 (HTTPS)
  - Port 8081 (API Gateway)
  - Port 8761 (Service Registry)
  - Port 9090 (Prometheus)
  - Port 3000 (Grafana)

### 2. Security Group Configuration

Create or update your security group with the following inbound rules:

| Type | Protocol | Port Range | Source | Description |
|------|----------|------------|--------|-------------|
| SSH | TCP | 22 | 0.0.0.0/0 | SSH access |
| HTTP | TCP | 80 | 0.0.0.0/0 | Web traffic |
| HTTPS | TCP | 443 | 0.0.0.0/0 | Secure web traffic |
| Custom TCP | TCP | 8081 | 0.0.0.0/0 | API Gateway |
| Custom TCP | TCP | 8761 | 0.0.0.0/0 | Service Registry |
| Custom TCP | TCP | 9090 | 0.0.0.0/0 | Prometheus |
| Custom TCP | TCP | 3000 | 0.0.0.0/0 | Grafana |

## Deployment Steps

### 1. Connect to EC2 Instance

```bash
# Connect via SSH
ssh -i your-key.pem ubuntu@18.217.148.69
```

### 2. Install Dependencies

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
sudo apt install -y docker.io docker-compose

# Start and enable Docker
sudo systemctl start docker
sudo systemctl enable docker

# Add user to docker group
sudo usermod -aG docker $USER

# Install Java 17
sudo apt install -y openjdk-17-jdk

# Install Node.js and npm
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Install Maven
sudo apt install -y maven

# Logout and login again for docker group to take effect
exit
# SSH back in
ssh -i your-key.pem ubuntu@18.217.148.69
```

### 3. Clone and Setup Application

```bash
# Clone the repository
git clone <your-repository-url>
cd ecommerce-web-application

# Make scripts executable
chmod +x build.sh deploy.sh

# Create environment file
cp env.example .env
nano .env
```

### 4. Configure Environment Variables

Edit the `.env` file with your production settings:

```bash
# MongoDB Configuration
MONGO_INITDB_ROOT_USERNAME=admin
MONGO_INITDB_ROOT_PASSWORD=your-secure-password

# Email Configuration (for notification service)
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password

# JWT Configuration
JWT_SECRET=your-super-secure-jwt-secret-key
JWT_EXPIRATION=86400000

# Application URLs
FRONTEND_URL=http://18.217.148.69
API_BASE_URL=http://18.217.148.69/api
SERVICE_REGISTRY_URL=http://18.217.148.69:8761
```

### 5. Build the Application

```bash
# Build all microservices and frontend
./build.sh
```

This will:
- Build all microservices using Maven
- Create JAR files in the `./jars/` directory
- Build the frontend for production
- Update API configuration for production

### 6. Deploy the Application

```bash
# Deploy using pre-built artifacts
./deploy.sh
```

This will:
- Check for required build artifacts
- Start all services in the correct order
- Verify service health
- Display access information

### 7. Verify Deployment

Check if all services are running:

```bash
# Check container status
docker-compose ps

# Check service logs
docker-compose logs -f

# Check specific service logs
docker-compose logs -f api-gateway
```

## Access Points

After successful deployment, you can access:

| Service | URL | Description |
|---------|-----|-------------|
| Frontend | http://18.217.148.69 | Main application |
| API Gateway | http://18.217.148.69:8081 | Direct API access |
| Service Registry | http://18.217.148.69:8761 | Eureka dashboard |
| Prometheus | http://18.217.148.69:9090 | Metrics collection |
| Grafana | http://18.217.148.69:3000 | Monitoring dashboard |
| Nginx Exporter | http://18.217.148.69:9113/metrics | Nginx metrics |
| Blackbox Exporter | http://18.217.148.69:9115 | HTTP monitoring |
| Node Exporter | http://18.217.148.69:9100/metrics | Host metrics |

## Monitoring Setup

### 1. Grafana Configuration

1. Access Grafana: http://18.217.148.69:3000
2. Login with: admin/admin
3. Add Prometheus as data source:
   - URL: http://prometheus:9090
   - Access: Server (default)

### 2. Import Dashboards

Import the following dashboards in Grafana:
- Spring Boot metrics
- Node Exporter metrics
- Nginx metrics
- Blackbox monitoring

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Check what's using the port
   sudo netstat -tulpn | grep :80
   
   # Stop conflicting services
   sudo systemctl stop apache2
   sudo systemctl stop nginx
   ```

2. **Docker Permission Issues**
   ```bash
   # Add user to docker group
   sudo usermod -aG docker $USER
   newgrp docker
   ```

3. **Memory Issues**
   ```bash
   # Check available memory
   free -h
   
   # Increase swap if needed
   sudo fallocate -l 2G /swapfile
   sudo chmod 600 /swapfile
   sudo mkswap /swapfile
   sudo swapon /swapfile
   ```

4. **Service Not Starting**
   ```bash
   # Check logs
   docker-compose logs -f service-name
   
   # Restart specific service
   docker-compose restart service-name
   ```

### Health Checks

```bash
# Check if services are responding
curl http://18.217.148.69/health
curl http://18.217.148.69:8081/actuator/health
curl http://18.217.148.69:8761

# Check MongoDB connection
docker exec -it purely_mongodb mongosh --eval "db.adminCommand('ping')"
```

## Maintenance

### 1. Update Application

```bash
# Pull latest changes
git pull origin main

# Rebuild and redeploy
./build.sh
./deploy.sh
```

### 2. Backup Database

```bash
# Create backup
docker exec purely_mongodb mongodump --out /backup

# Copy backup to host
docker cp purely_mongodb:/backup ./mongodb-backup
```

### 3. Monitor Resources

```bash
# Check disk usage
df -h

# Check memory usage
free -h

# Check CPU usage
top

# Check Docker resources
docker stats
```

## Security Considerations

1. **Change Default Passwords**
   - MongoDB admin password
   - Grafana admin password
   - JWT secret

2. **Enable HTTPS**
   - Configure SSL certificates
   - Update nginx configuration
   - Update CORS origins

3. **Firewall Configuration**
   - Restrict access to monitoring ports
   - Use security groups effectively
   - Consider using a bastion host

4. **Regular Updates**
   - Keep system packages updated
   - Update Docker images regularly
   - Monitor for security vulnerabilities

## Performance Optimization

1. **Resource Allocation**
   - Monitor resource usage
   - Scale instance type if needed
   - Optimize JVM settings

2. **Database Optimization**
   - Add database indexes
   - Configure connection pooling
   - Monitor query performance

3. **Caching**
   - Implement Redis for session storage
   - Configure CDN for static assets
   - Enable browser caching

## Support

For issues and questions:
1. Check the logs: `docker-compose logs -f`
2. Review this deployment guide
3. Check the main README.md
4. Open an issue in the repository

---

**Note**: This deployment guide assumes a single EC2 instance. For production environments, consider using multiple instances, load balancers, and managed services for better scalability and reliability. 