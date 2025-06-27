#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Starting E-commerce Application Deployment${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose is not installed. Please install it first.${NC}"
    exit 1
fi

# Stop any existing containers
echo -e "${YELLOW}🛑 Stopping existing containers...${NC}"
docker-compose down --remove-orphans

# Remove old images to ensure fresh build
echo -e "${YELLOW}🧹 Cleaning up old images...${NC}"
docker system prune -f

# Build all services
echo -e "${YELLOW}🔨 Building all services...${NC}"
docker-compose build --no-cache

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Build failed. Please check the error messages above.${NC}"
    exit 1
fi

# Start services in order
echo -e "${YELLOW}🚀 Starting services...${NC}"

# Start MongoDB first
echo -e "${YELLOW}📦 Starting MongoDB...${NC}"
docker-compose up -d mongodb
sleep 10

# Start Service Registry
echo -e "${YELLOW}📋 Starting Service Registry...${NC}"
docker-compose up -d service-registry
sleep 15

# Start API Gateway
echo -e "${YELLOW}🚪 Starting API Gateway...${NC}"
docker-compose up -d api-gateway
sleep 10

# Start all microservices
echo -e "${YELLOW}🔧 Starting Microservices...${NC}"
docker-compose up -d auth-service user-service product-service category-service cart-service order-service notification-service
sleep 20

# Start Frontend with Nginx
echo -e "${YELLOW}🌐 Starting Frontend with Nginx...${NC}"
docker-compose up -d frontend
sleep 10

# Start monitoring stack
echo -e "${YELLOW}📊 Starting Monitoring Stack...${NC}"
docker-compose up -d prometheus grafana node-exporter blackbox-exporter promtail loki
sleep 15

# Check service health
echo -e "${YELLOW}🏥 Checking service health...${NC}"
sleep 30

# Health checks
echo -e "${YELLOW}🔍 Performing health checks...${NC}"

# Check if services are running
services=("service-registry" "api-gateway" "auth-service" "user-service" "product-service" "category-service" "cart-service" "order-service" "notification-service" "frontend" "mongodb" "prometheus" "grafana")

for service in "${services[@]}"; do
    if docker-compose ps | grep -q "$service.*Up"; then
        echo -e "${GREEN}✅ $service is running${NC}"
    else
        echo -e "${RED}❌ $service is not running${NC}"
    fi
done

# Check API Gateway health
echo -e "${YELLOW}🔍 Checking API Gateway health...${NC}"
if curl -f http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ API Gateway is healthy${NC}"
else
    echo -e "${RED}❌ API Gateway health check failed${NC}"
fi

# Check Frontend health
echo -e "${YELLOW}🔍 Checking Frontend health...${NC}"
if curl -f http://localhost/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Frontend is healthy${NC}"
else
    echo -e "${RED}❌ Frontend health check failed${NC}"
fi

# Check Prometheus
echo -e "${YELLOW}🔍 Checking Prometheus...${NC}"
if curl -f http://localhost:9090/-/healthy > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Prometheus is healthy${NC}"
else
    echo -e "${RED}❌ Prometheus health check failed${NC}"
fi

# Check Grafana
echo -e "${YELLOW}🔍 Checking Grafana...${NC}"
if curl -f http://localhost:3000/api/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Grafana is healthy${NC}"
else
    echo -e "${RED}❌ Grafana health check failed${NC}"
fi

echo -e "${GREEN}🎉 Deployment completed!${NC}"
echo -e "${GREEN}📱 Frontend: http://localhost${NC}"
echo -e "${GREEN}🔌 API Gateway: http://localhost:8081${NC}"
echo -e "${GREEN}📊 Grafana: http://localhost:3000 (admin/admin123)${NC}"
echo -e "${GREEN}📈 Prometheus: http://localhost:9090${NC}"
echo -e "${GREEN}📋 Service Registry: http://localhost:8761${NC}"

echo -e "${YELLOW}💡 To view logs: docker-compose logs -f [service-name]${NC}"
echo -e "${YELLOW}💡 To stop all services: docker-compose down${NC}"
echo -e "${YELLOW}💡 To restart a service: docker-compose restart [service-name]${NC}" 