#!/bin/bash

# PURELY E-commerce Application - Production Deployment Script for EC2
# This script deploys the application on EC2 with production settings

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Production Configuration
PRODUCTION_IP="18.217.148.69"
PRODUCTION_DOMAIN="18.217.148.69"
DOCKER_COMPOSE_FILE="docker-compose-ec2-prod.yml"
NGINX_CONFIG="nginx-ssl-ec2-prod.conf"

echo -e "${BLUE}🚀 PURELY E-commerce Production Deployment${NC}"
echo -e "${YELLOW}Production IP: ${PRODUCTION_IP}${NC}"
echo -e "${YELLOW}Docker Compose: ${DOCKER_COMPOSE_FILE}${NC}"
echo -e "${YELLOW}Nginx Config: ${NGINX_CONFIG}${NC}"
echo ""

# Function to print status messages
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if running as root
if [[ $EUID -ne 0 ]]; then
   print_error "This script must be run as root (use sudo)"
   exit 1
fi

# Check if production files exist
if [ ! -f "$DOCKER_COMPOSE_FILE" ]; then
    print_error "Production docker-compose file not found: $DOCKER_COMPOSE_FILE"
    print_warning "Please rename docker-compose-ec2-prod.yml to $DOCKER_COMPOSE_FILE"
    exit 1
fi

if [ ! -f "$NGINX_CONFIG" ]; then
    print_error "Production nginx config not found: $NGINX_CONFIG"
    print_warning "Please rename nginx-ssl-ec2-prod.conf to $NGINX_CONFIG"
    exit 1
fi

# Check if environment file exists
if [ ! -f ".env" ]; then
    print_warning "Environment file not found. Creating from example..."
    if [ -f "env-ec2-prod.example" ]; then
        cp env-ec2-prod.example .env
        print_warning "Please update .env file with your production values"
        print_warning "Then run this script again"
        exit 1
    else
        print_error "No environment file or example found"
        exit 1
    fi
fi

# Check if JAR files exist
print_status "Checking for required JAR files..."
JAR_FILES=(
    "jars/service-registry.jar"
    "jars/api-gateway.jar"
    "jars/auth-service.jar"
    "jars/cart-service.jar"
    "jars/category-service.jar"
    "jars/notification-service.jar"
    "jars/order-service.jar"
    "jars/product-service.jar"
    "jars/user-service.jar"
)

for jar_file in "${JAR_FILES[@]}"; do
    if [ ! -f "$jar_file" ]; then
        print_error "Missing JAR file: $jar_file"
        print_warning "Please run build.sh first to build all services"
        exit 1
    fi
done

# Check if frontend is built
if [ ! -d "frontend/dist" ]; then
    print_error "Frontend not built. Please run build.sh first"
    exit 1
fi

# Stop existing containers
print_status "Stopping existing containers..."
docker-compose -f $DOCKER_COMPOSE_FILE down --remove-orphans || true

# Clean up old containers and images
print_status "Cleaning up old containers and images..."
docker system prune -f || true

# Create necessary directories
print_status "Creating necessary directories..."
mkdir -p /var/log/purely-app
mkdir -p /opt/backups
mkdir -p ssl

# Set proper permissions
chmod 755 /var/log/purely-app
chmod 755 /opt/backups

# Check SSL certificates
print_status "Checking SSL certificates..."
if [ -f "/etc/letsencrypt/live/${PRODUCTION_DOMAIN}/fullchain.pem" ]; then
    print_status "Let's Encrypt certificates found"
    # Copy certificates to project directory for Docker
    sudo cp /etc/letsencrypt/live/${PRODUCTION_DOMAIN}/fullchain.pem ssl/nginx-selfsigned.crt
    sudo cp /etc/letsencrypt/live/${PRODUCTION_DOMAIN}/privkey.pem ssl/nginx-selfsigned.key
    sudo chown $USER:$USER ssl/*
    chmod 600 ssl/nginx-selfsigned.key
    chmod 644 ssl/nginx-selfsigned.crt
else
    print_warning "Let's Encrypt certificates not found"
    if [ ! -f "ssl/nginx-selfsigned.crt" ]; then
        print_warning "Self-signed certificates not found. Generating..."
        ./generate-selfsigned-cert.sh $PRODUCTION_IP
        sudo cp /etc/nginx/ssl/nginx-selfsigned.* ssl/
        sudo chown $USER:$USER ssl/*
        chmod 600 ssl/nginx-selfsigned.key
        chmod 644 ssl/nginx-selfsigned.crt
    fi
fi

# Start services
print_status "Starting services with production configuration..."
docker-compose -f $DOCKER_COMPOSE_FILE up -d

# Wait for services to start
print_status "Waiting for services to start..."
sleep 30

# Check service health
print_status "Checking service health..."
HEALTH_CHECK_RETRIES=10
HEALTH_CHECK_INTERVAL=10

for i in $(seq 1 $HEALTH_CHECK_RETRIES); do
    print_status "Health check attempt $i/$HEALTH_CHECK_RETRIES"
    
    # Check nginx health
    if curl -f -k https://$PRODUCTION_IP/health > /dev/null 2>&1; then
        print_status "Nginx is healthy"
    else
        print_warning "Nginx health check failed"
    fi
    
    # Check API Gateway health
    if curl -f http://$PRODUCTION_IP:8081/actuator/health > /dev/null 2>&1; then
        print_status "API Gateway is healthy"
    else
        print_warning "API Gateway health check failed"
    fi
    
    # Check Service Registry health
    if curl -f http://$PRODUCTION_IP:8761/actuator/health > /dev/null 2>&1; then
        print_status "Service Registry is healthy"
    else
        print_warning "Service Registry health check failed"
    fi
    
    # Check MongoDB health
    if docker exec purely_mongodb mongosh --eval "db.adminCommand('ping')" > /dev/null 2>&1; then
        print_status "MongoDB is healthy"
    else
        print_warning "MongoDB health check failed"
    fi
    
    sleep $HEALTH_CHECK_INTERVAL
done

# Display container status
print_status "Container status:"
docker-compose -f $DOCKER_COMPOSE_FILE ps

# Display access information
echo ""
echo -e "${GREEN}🎉 Production Deployment Complete!${NC}"
echo ""
echo -e "${BLUE}📋 Access URLs:${NC}"
echo -e "  • Frontend (HTTPS): ${YELLOW}https://${PRODUCTION_IP}${NC}"
echo -e "  • Frontend (HTTP): ${YELLOW}http://${PRODUCTION_IP}${NC}"
echo -e "  • API Gateway: ${YELLOW}http://${PRODUCTION_IP}:8081${NC}"
echo -e "  • Service Registry: ${YELLOW}http://${PRODUCTION_IP}:8761${NC}"
echo -e "  • Prometheus: ${YELLOW}http://${PRODUCTION_IP}:9090${NC}"
echo -e "  • Grafana: ${YELLOW}http://${PRODUCTION_IP}:3000${NC}"
echo ""
echo -e "${BLUE}🔧 Management Commands:${NC}"
echo -e "  • View logs: ${YELLOW}docker-compose -f ${DOCKER_COMPOSE_FILE} logs -f${NC}"
echo -e "  • Stop services: ${YELLOW}docker-compose -f ${DOCKER_COMPOSE_FILE} down${NC}"
echo -e "  • Restart services: ${YELLOW}docker-compose -f ${DOCKER_COMPOSE_FILE} restart${NC}"
echo -e "  • Check status: ${YELLOW}docker-compose -f ${DOCKER_COMPOSE_FILE} ps${NC}"
echo ""
echo -e "${BLUE}🔍 Health Checks:${NC}"
echo -e "  • Nginx: ${YELLOW}curl -k https://${PRODUCTION_IP}/health${NC}"
echo -e "  • API Gateway: ${YELLOW}curl http://${PRODUCTION_IP}:8081/actuator/health${NC}"
echo -e "  • Service Registry: ${YELLOW}curl http://${PRODUCTION_IP}:8761/actuator/health${NC}"
echo ""
echo -e "${YELLOW}⚠️  Important Notes:${NC}"
echo -e "  • SSL certificates are configured for production"
echo -e "  • Rate limiting is enabled for API endpoints"
echo -e "  • Monitoring and logging are configured"
echo -e "  • Regular backups are scheduled"
echo -e "  • Check logs for any issues: ${YELLOW}docker-compose -f ${DOCKER_COMPOSE_FILE} logs${NC}"
echo ""

print_status "Production deployment completed successfully!" 