# AWS EC2 Deployment Guide - Production Environment

## 🔒 Overview

This guide provides comprehensive instructions for deploying the PURELY E-commerce application on AWS EC2 in a production environment with enhanced security, monitoring, and performance optimizations.

## 📋 Production Environment Details

### EC2 Instance Configuration
- **Public IP**: 18.217.148.69
- **Private IP**: 172.31.26.143
- **Region**: US East (Ohio) - us-east-2
- **Instance Type**: t3.medium (4GB RAM, 2 vCPUs)
- **Operating System**: Ubuntu 20.04 LTS
- **Storage**: 20GB EBS volume

### Production File Structure
The production deployment uses separate configuration files with the `-ec2-prod` suffix to isolate production settings from development:

```
ecommerce-web-application/
├── docker-compose-ec2-prod.yml          # Production Docker Compose
├── nginx-ssl-ec2-prod.conf              # Production Nginx SSL config
├── env-ec2-prod.example                 # Production environment template
├── deploy-ec2-prod.sh                   # Production deployment script
├── ssl-setup-ec2-prod.sh                # Production SSL setup
└── technical-notes/
    ├── AWS_EC2_DEPLOYMENT_GUIDE.md      # This guide
    ├── SSL_SETUP_GUIDE.md               # SSL configuration
    └── TROUBLESHOOTING_GUIDE.md         # Troubleshooting
```

## 🚀 Quick Production Deployment

### Step 1: Prepare Production Files
```bash
# Clone the repository
git clone <your-repository-url>
cd ecommerce-web-application

# Make scripts executable
chmod +x deploy-ec2-prod.sh
chmod +x ssl-setup-ec2-prod.sh
chmod +x generate-selfsigned-cert.sh
```

### Step 2: Set Up Environment
```bash
# Copy production environment template
cp env-ec2-prod.example .env

# Edit environment variables for production
nano .env
```

### Step 3: Build Application
```bash
# Build all services for production
./build.sh
```

### Step 4: Deploy to Production
```bash
# Deploy with production configuration
sudo ./deploy-ec2-prod.sh
```

## 🔧 Detailed Production Setup

### 1. EC2 Instance Prerequisites

#### Security Group Configuration
Configure your EC2 security group with the following inbound rules:

| Type | Protocol | Port Range | Source | Description |
|------|----------|------------|--------|-------------|
| SSH | TCP | 22 | Your IP | SSH access |
| HTTP | TCP | 80 | 0.0.0.0/0 | Web traffic |
| HTTPS | TCP | 443 | 0.0.0.0/0 | Secure web traffic |
| Custom TCP | TCP | 8081 | 0.0.0.0/0 | API Gateway |
| Custom TCP | TCP | 8761 | 0.0.0.0/0 | Service Registry |
| Custom TCP | TCP | 9090 | 0.0.0.0/0 | Prometheus |
| Custom TCP | TCP | 3000 | 0.0.0.0/0 | Grafana |

#### System Requirements
```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install required packages
sudo apt install -y docker.io docker-compose openjdk-17-jdk curl wget git

# Start and enable Docker
sudo systemctl start docker
sudo systemctl enable docker

# Add user to docker group
sudo usermod -aG docker $USER

# Install Node.js for frontend build
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Logout and login again for docker group to take effect
exit
# SSH back in
ssh -i your-key.pem ubuntu@18.217.148.69
```

### 2. Production File Setup

#### File Renaming Process
Before deployment, rename the production files:

```bash
# Rename production files for deployment
mv docker-compose-ec2-prod.yml docker-compose.yml
mv nginx-ssl-ec2-prod.conf nginx-ssl.conf
mv env-ec2-prod.example .env

# Or use symbolic links
ln -sf docker-compose-ec2-prod.yml docker-compose.yml
ln -sf nginx-ssl-ec2-prod.conf nginx-ssl.conf
```

#### Environment Configuration
Edit the `.env` file with your production values:

```bash
# MongoDB Configuration
MONGO_INITDB_ROOT_USERNAME=admin
MONGO_INITDB_ROOT_PASSWORD=your-secure-production-password

# Email Configuration
SPRING_MAIL_USERNAME=your-production-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-specific-password

# JWT Configuration
JWT_SECRET=your-super-secure-jwt-secret-key-for-production
JWT_EXPIRATION=86400000

# Application URLs
FRONTEND_URL=https://18.217.148.69
API_BASE_URL=https://18.217.148.69/api
SERVICE_REGISTRY_URL=http://18.217.148.69:8761

# Grafana Configuration
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=your-secure-grafana-password

# SSL Configuration
SSL_DOMAIN=18.217.148.69
SSL_EMAIL=your-ssl-email@example.com
```

### 3. SSL Certificate Setup

#### Option A: Let's Encrypt (Recommended for Production)
```bash
# Set up SSL certificates
sudo ./ssl-setup-ec2-prod.sh 18.217.148.69 your-email@example.com
```

#### Option B: Self-Signed (Testing Only)
```bash
# Generate self-signed certificate for testing
sudo ./generate-selfsigned-cert.sh 18.217.148.69

# Copy to project directory
sudo cp /etc/nginx/ssl/nginx-selfsigned.* ssl/
sudo chown $USER:$USER ssl/*
chmod 600 ssl/nginx-selfsigned.key
chmod 644 ssl/nginx-selfsigned.crt
```

### 4. Application Deployment

#### Build Application
```bash
# Build all microservices and frontend
./build.sh
```

#### Deploy with Production Script
```bash
# Deploy using production configuration
sudo ./deploy-ec2-prod.sh
```

## 🔍 Production Configuration Details

### Docker Compose Production Settings
The production `docker-compose-ec2-prod.yml` includes:

- **Enhanced resource limits** for production workloads
- **Production SSL certificates** (Let's Encrypt)
- **Optimized JVM settings** for performance
- **Production logging** configuration
- **Health checks** for all services
- **Backup volumes** for data persistence

### Nginx Production Configuration
The production `nginx-ssl-ec2-prod.conf` includes:

- **Rate limiting** for API endpoints
- **Security headers** (HSTS, CSP, etc.)
- **Gzip compression** for performance
- **SSL optimization** with OCSP stapling
- **CORS configuration** for production domain
- **Error handling** and logging

### Monitoring and Observability
Production deployment includes:

- **Prometheus** for metrics collection
- **Grafana** for monitoring dashboards
- **Node Exporter** for host metrics
- **Blackbox Exporter** for HTTP monitoring
- **Log aggregation** with proper retention

## 📊 Access Points

After successful deployment, access your application at:

| Service | URL | Description |
|---------|-----|-------------|
| Frontend (HTTPS) | https://18.217.148.69 | Main application |
| Frontend (HTTP) | http://18.217.148.69 | Redirects to HTTPS |
| API Gateway | http://18.217.148.69:8081 | Direct API access |
| Service Registry | http://18.217.148.69:8761 | Eureka dashboard |
| Prometheus | http://18.217.148.69:9090 | Metrics collection |
| Grafana | http://18.217.148.69:3000 | Monitoring dashboard |
| Health Check | https://18.217.148.69/health | Application health |

## 🛠️ Production Management

### Service Management Commands
```bash
# View all services
docker-compose -f docker-compose-ec2-prod.yml ps

# View logs
docker-compose -f docker-compose-ec2-prod.yml logs -f

# Restart specific service
docker-compose -f docker-compose-ec2-prod.yml restart service-name

# Stop all services
docker-compose -f docker-compose-ec2-prod.yml down

# Update and redeploy
git pull origin main
./build.sh
sudo ./deploy-ec2-prod.sh
```

### Health Monitoring
```bash
# Check application health
curl -k https://18.217.148.69/health

# Check API Gateway health
curl http://18.217.148.69:8081/actuator/health

# Check Service Registry health
curl http://18.217.148.69:8761/actuator/health

# Check MongoDB health
docker exec purely_mongodb mongosh --eval "db.adminCommand('ping')"
```

### SSL Certificate Management
```bash
# Check SSL certificate status
sudo /usr/local/bin/ssl-status.sh 18.217.148.69

# Renew SSL certificates
sudo /usr/local/bin/renew-ssl.sh

# Troubleshoot SSL issues
sudo /usr/local/bin/ssl-troubleshoot.sh 18.217.148.69
```

## 🔒 Security Considerations

### Production Security Checklist
- [ ] Strong MongoDB passwords configured
- [ ] JWT secret key is secure and unique
- [ ] SSL certificates properly configured
- [ ] Security headers enabled in nginx
- [ ] Rate limiting configured for API endpoints
- [ ] Grafana admin password changed
- [ ] Firewall rules properly configured
- [ ] Regular security updates enabled

### Security Best Practices
1. **Change Default Passwords**
   - MongoDB admin password
   - Grafana admin password
   - JWT secret key

2. **Enable HTTPS Only**
   - Configure SSL certificates
   - Force HTTP to HTTPS redirect
   - Enable HSTS headers

3. **Implement Rate Limiting**
   - API endpoints: 20 requests/second
   - Login endpoints: 10 requests/second
   - General endpoints: 30 requests/second

4. **Monitor and Log**
   - Enable application logging
   - Monitor access logs
   - Set up alerting for security events

## 🔄 Backup and Recovery

### Automated Backups
```bash
# Create backup script
cat > /usr/local/bin/backup-production.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/backups/$(date +%Y%m%d_%H%M%S)"
mkdir -p $BACKUP_DIR

# Backup MongoDB
docker exec purely_mongodb mongodump --out /backup
docker cp purely_mongodb:/backup $BACKUP_DIR/mongodb

# Backup configuration files
cp docker-compose-ec2-prod.yml $BACKUP_DIR/
cp nginx-ssl-ec2-prod.conf $BACKUP_DIR/
cp .env $BACKUP_DIR/

# Backup SSL certificates
cp -r ssl/ $BACKUP_DIR/

echo "Backup completed: $BACKUP_DIR"
EOF

chmod +x /usr/local/bin/backup-production.sh

# Schedule daily backups
echo "0 2 * * * root /usr/local/bin/backup-production.sh" | sudo tee -a /etc/crontab
```

### Recovery Process
```bash
# Restore from backup
BACKUP_DIR="/opt/backups/20241201_020000"

# Restore MongoDB
docker cp $BACKUP_DIR/mongodb purely_mongodb:/backup
docker exec purely_mongodb mongorestore /backup

# Restore configuration
cp $BACKUP_DIR/docker-compose-ec2-prod.yml ./
cp $BACKUP_DIR/nginx-ssl-ec2-prod.conf ./
cp $BACKUP_DIR/.env ./

# Restart services
sudo ./deploy-ec2-prod.sh
```

## 📈 Performance Optimization

### Resource Monitoring
```bash
# Monitor system resources
htop
df -h
free -h

# Monitor Docker resources
docker stats

# Monitor application performance
curl http://18.217.148.69:9090/metrics
```

### Performance Tuning
1. **JVM Settings**: Optimized for production workloads
2. **Database Connections**: Configured connection pooling
3. **Caching**: Implemented at multiple levels
4. **Load Balancing**: Nginx handles traffic distribution
5. **Compression**: Gzip enabled for all text content

## 🚨 Troubleshooting

### Common Production Issues

#### 1. Service Not Starting
```bash
# Check service logs
docker-compose -f docker-compose-ec2-prod.yml logs -f service-name

# Check system resources
free -h
df -h

# Restart specific service
docker-compose -f docker-compose-ec2-prod.yml restart service-name
```

#### 2. SSL Certificate Issues
```bash
# Check certificate status
sudo /usr/local/bin/ssl-status.sh 18.217.148.69

# Renew certificates
sudo /usr/local/bin/renew-ssl.sh

# Check nginx configuration
sudo nginx -t
```

#### 3. Performance Issues
```bash
# Check resource usage
docker stats

# Check application metrics
curl http://18.217.148.69:9090/metrics

# Check database performance
docker exec purely_mongodb mongosh --eval "db.stats()"
```

#### 4. Network Connectivity
```bash
# Check port availability
sudo netstat -tulpn | grep :80
sudo netstat -tulpn | grep :443

# Check firewall rules
sudo ufw status
sudo iptables -L
```

## 📋 Production Deployment Checklist

### Pre-Deployment
- [ ] EC2 instance properly configured
- [ ] Security groups configured
- [ ] Production files renamed correctly
- [ ] Environment variables configured
- [ ] SSL certificates obtained
- [ ] Application built successfully

### Deployment
- [ ] Production deployment script executed
- [ ] All services started successfully
- [ ] Health checks passed
- [ ] SSL certificates working
- [ ] Monitoring configured
- [ ] Backups scheduled

### Post-Deployment
- [ ] Application accessible via HTTPS
- [ ] API endpoints responding correctly
- [ ] Monitoring dashboards working
- [ ] Logs being collected
- [ ] Performance metrics available
- [ ] Security headers enabled

## 🔗 Related Documentation

- [SSL_SETUP_GUIDE.md](./SSL_SETUP_GUIDE.md) - SSL certificate configuration
- [TROUBLESHOOTING_GUIDE.md](./TROUBLESHOOTING_GUIDE.md) - Production troubleshooting
- [COMPREHENSIVE_DEPLOYMENT_GUIDE.md](./COMPREHENSIVE_DEPLOYMENT_GUIDE.md) - General deployment guide

## 🆕 Latest Updates (December 2024)

### Production Enhancements
- ✅ Production-specific configuration files with `-ec2-prod` suffix
- ✅ Enhanced security with rate limiting and security headers
- ✅ Automated backup and recovery procedures
- ✅ Comprehensive monitoring and observability
- ✅ SSL certificate management for production
- ✅ Performance optimization for production workloads

### File Renaming Instructions
The deployment process includes automatic file renaming:
1. `docker-compose-ec2-prod.yml` → `docker-compose.yml`
2. `nginx-ssl-ec2-prod.conf` → `nginx-ssl.conf`
3. `env-ec2-prod.example` → `.env`

This ensures clean separation between development and production configurations.

---

**Note**: This production deployment guide assumes a single EC2 instance. For high-availability production environments, consider using multiple instances, load balancers, and managed services for better scalability and reliability. 