# EC2 Deployment Guide - E-commerce Application

## Overview
This guide provides step-by-step instructions for deploying the e-commerce application to AWS EC2, avoiding the local development issues we encountered.

## Prerequisites
- AWS EC2 instance (t3.medium or larger recommended)
- Docker and Docker Compose installed on EC2
- Domain name (optional, for SSL)
- Security groups configured

## Deployment Steps

### 1. Instance Setup
```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Logout and login again for docker group to take effect
```

### 2. Application Deployment
```bash
# Clone repository
git clone <your-repo-url>
cd ecommerce-web-application

# Create production environment file
cp .env.example .env
# Edit .env with production values

# Build and start services
docker-compose up -d --build
```

### 3. Production Configuration

#### A. Nginx Configuration (Simplified)
Create `/etc/nginx/sites-available/ecommerce`:
```nginx
server {
    listen 80;
    server_name your-domain.com;  # or EC2 public IP

    # Frontend
    location / {
        proxy_pass http://localhost:5173;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # API Gateway
    location /api/ {
        proxy_pass http://localhost:8081/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### B. Environment Variables
Create `.env` file:
```env
# Database
MONGODB_URI=mongodb://admin:password@localhost:27017
MONGODB_DATABASE=purely_ecommerce

# JWT
JWT_SECRET=your-production-jwt-secret
JWT_EXPIRATION=86400000

# Email (for notifications)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password

# Application
APP_ENV=production
APP_PORT=8081
```

### 4. SSL Configuration (Optional)
```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx

# Get SSL certificate
sudo certbot --nginx -d your-domain.com

# Auto-renewal
sudo crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

### 5. Security Configuration

#### A. Security Groups
- **HTTP (80)**: Open for web access
- **HTTPS (443)**: Open if using SSL
- **SSH (22)**: Restricted to your IP
- **Application Ports**: Internal only (8081, 8082, etc.)

#### B. Firewall
```bash
# Configure UFW
sudo ufw allow ssh
sudo ufw allow 'Nginx Full'
sudo ufw enable
```

### 6. Monitoring and Logs
```bash
# View logs
docker-compose logs -f

# Monitor services
docker-compose ps

# Health checks
curl http://localhost:8081/actuator/health
```

## Key Differences from Local Development

### 1. **No HTTPS Complexity**
- Production uses standard HTTP or proper SSL
- No self-signed certificate issues
- No browser security restrictions

### 2. **Simplified Networking**
- Direct service communication
- No Docker port mapping conflicts
- Standard load balancer setup

### 3. **Production Environment**
- Proper environment variables
- Production database configuration
- Real email service integration

## Troubleshooting Guide

### Common Issues and Solutions

#### 1. **Services Not Starting**
```bash
# Check logs
docker-compose logs [service-name]

# Check resource usage
docker stats

# Restart services
docker-compose restart
```

#### 2. **Database Connection Issues**
```bash
# Check MongoDB status
docker-compose exec mongodb mongosh

# Verify connection string
docker-compose logs auth-service
```

#### 3. **Nginx Issues**
```bash
# Test nginx configuration
sudo nginx -t

# Reload nginx
sudo systemctl reload nginx

# Check nginx logs
sudo tail -f /var/log/nginx/error.log
```

#### 4. **Memory Issues**
```bash
# Monitor memory usage
free -h

# Check Docker memory limits
docker stats

# Increase swap if needed
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

## Performance Optimization

### 1. **Resource Allocation**
- **t3.medium**: Minimum for development
- **t3.large**: Recommended for production
- **t3.xlarge**: For high traffic

### 2. **Database Optimization**
```bash
# MongoDB optimization
docker-compose exec mongodb mongosh
use admin
db.runCommand({setParameter: 1, maxTransactionLockRequestTimeoutMillis: 5000})
```

### 3. **Application Optimization**
- Enable JVM optimizations for Java services
- Configure connection pooling
- Set appropriate memory limits

## Backup and Recovery

### 1. **Database Backup**
```bash
# Create backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
docker-compose exec mongodb mongodump --out /backup/$DATE
```

### 2. **Application Backup**
```bash
# Backup configuration
tar -czf backup_$(date +%Y%m%d).tar.gz .env docker-compose.yml nginx/
```

## Maintenance

### 1. **Regular Updates**
```bash
# Update application
git pull origin main
docker-compose down
docker-compose up -d --build
```

### 2. **Log Rotation**
```bash
# Configure log rotation
sudo nano /etc/logrotate.d/docker
```

### 3. **Health Monitoring**
```bash
# Create health check script
#!/bin/bash
if ! curl -f http://localhost:8081/actuator/health; then
    echo "Application is down!"
    # Send notification
fi
```

## Success Metrics

After deployment, verify:
- ✅ All services are running
- ✅ Frontend is accessible
- ✅ User registration works
- ✅ Product browsing works
- ✅ Cart functionality works
- ✅ Checkout process works
- ✅ SSL certificate is valid (if using)
- ✅ Monitoring is set up

## Support

If issues arise:
1. Check logs first: `docker-compose logs -f`
2. Verify configuration files
3. Test individual services
4. Check network connectivity
5. Review security group settings

This deployment should be much smoother than local development! 