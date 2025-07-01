#!/bin/bash

# --- PURELY E-commerce Automated Production Setup Script ---
# Usage: sudo ./setup-ec2-prod.sh <domain> <email>
# Example: sudo ./setup-ec2-prod.sh affluenceit.com your-email@domain.com

set -e

DOMAIN=${1:-"affluenceit.com"}
EMAIL=${2:-"your-email@domain.com"}

if [ -z "$DOMAIN" ] || [ -z "$EMAIL" ]; then
  echo "Usage: $0 <domain> <email>"
  exit 1
fi

echo "🚀 Starting automated production setup for domain: $DOMAIN"

# --- Build Queue-it Connector if present ---
if [ -d "connector-jakarta-main" ]; then
  echo "🔨 Building Queue-it connector modules..."
  if [ -d "connector-jakarta-main/core" ]; then
    (cd connector-jakarta-main/core && mvn clean install)
  fi
  if [ -d "connector-jakarta-main/jakarta" ]; then
    (cd connector-jakarta-main/jakarta && mvn clean install)
  fi
  echo "✅ Queue-it connector build complete."
else
  echo "⚠️  connector-jakarta-main directory not found. Skipping Queue-it connector build."
fi

# --- Generate .env for production ---
echo "🔧 Generating .env for $DOMAIN"
cat > .env <<EOF
# PURELY E-commerce Application Environment Variables
MONGO_INITDB_ROOT_USERNAME=admin
MONGO_INITDB_ROOT_PASSWORD=your-strong-password
MONGO_HOST=mongodb
MONGO_PORT=27017
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
JWT_SECRET=your-super-secret-jwt-key-here
JWT_EXPIRATION=86400000
SERVICE_REGISTRY_PORT=8761
API_GATEWAY_PORT=8081
AUTH_SERVICE_PORT=8081
CATEGORY_SERVICE_PORT=8082
PRODUCT_SERVICE_PORT=8083
CART_SERVICE_PORT=8084
ORDER_SERVICE_PORT=8085
USER_SERVICE_PORT=8086
NOTIFICATION_SERVICE_PORT=8087
FRONTEND_PORT=5173
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
NGINX_PORT=80
NGINX_SSL_PORT=443
FRONTEND_URL=https://${DOMAIN}
API_BASE_URL=https://${DOMAIN}/api
SERVICE_REGISTRY_URL=http://service-registry:8761
AUTH_DB_NAME=purely_auth_service
CATEGORY_DB_NAME=purely_category_service
PRODUCT_DB_NAME=purely_product_service
CART_DB_NAME=purely_cart_service
ORDER_DB_NAME=purely_order_service
USER_DB_NAME=purely_user_service
LOG_LEVEL=INFO
LOG_FILE_PATH=/var/log/purely
CORS_ALLOWED_ORIGINS=https://${DOMAIN}
RATE_LIMIT_REQUESTS_PER_MINUTE=100
EOF

# --- Generate Nginx SSL config ---
echo "🔧 Generating Nginx SSL config for $DOMAIN"
cat > nginx-ssl-ec2-prod.conf <<EOF
server {
    listen 80;
    server_name ${DOMAIN};
    return 301 https://\$server_name\$request_uri;
}

server {
    listen 443 ssl http2;
    server_name ${DOMAIN};

    ssl_certificate /etc/letsencrypt/live/${DOMAIN}/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/${DOMAIN}/privkey.pem;

    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
    add_header X-Frame-Options DENY always;
    add_header X-Content-Type-Options nosniff always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    location / {
        root /usr/share/nginx/html;
        try_files \$uri \$uri/ /index.html;
        add_header Access-Control-Allow-Origin "https://${DOMAIN}" always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "*" always;
    }

    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }

    location /api/ {
        proxy_pass http://api-gateway:8081/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
        add_header Access-Control-Allow-Origin "https://${DOMAIN}" always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "*" always;
        add_header Access-Control-Allow-Credentials "true" always;
    }
}
EOF

# --- Generate Prometheus config ---
echo "🔧 Generating Prometheus config for $DOMAIN"
mkdir -p prometheus
cat > prometheus/prometheus-ec2-prod.yml <<EOF
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    environment: 'production'
    instance: '${DOMAIN}'

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
    metrics_path: /metrics
    scrape_interval: 15s

  - job_name: 'service-registry'
    static_configs:
      - targets: ['service-registry:8761']
    metrics_path: /actuator/prometheus
    scrape_interval: 30s
    scrape_timeout: 10s

  - job_name: 'api-gateway'
    static_configs:
      - targets: ['api-gateway:8081']
    metrics_path: /actuator/prometheus
    scrape_interval: 15s
    scrape_timeout: 10s

  - job_name: 'auth-service'
    static_configs:
      - targets: ['auth-service:8081']
    metrics_path: /actuator/prometheus
    scrape_interval: 15s
    scrape_timeout: 10s

  - job_name: 'category-service'
    static_configs:
      - targets: ['category-service:8082']
    metrics_path: /actuator/prometheus
    scrape_interval: 15s
    scrape_timeout: 10s

  - job_name: 'product-service'
    static_configs:
      - targets: ['product-service:8083']
    metrics_path: /actuator/prometheus
    scrape_interval: 15s
    scrape_timeout: 10s

  - job_name: 'cart-service'
    static_configs:
      - targets: ['cart-service:8084']
    metrics_path: /actuator/prometheus
    scrape_interval: 15s
    scrape_timeout: 10s

  - job_name: 'user-service'
    static_configs:
      - targets: ['user-service:8085']
    metrics_path: /actuator/prometheus
    scrape_interval: 15s
    scrape_timeout: 10s

  - job_name: 'order-service'
    static_configs:
      - targets: ['order-service:8086']
    metrics_path: /actuator/prometheus
    scrape_interval: 15s
    scrape_timeout: 10s

  - job_name: 'notification-service'
    static_configs:
      - targets: ['notification-service:8087']
    metrics_path: /actuator/prometheus
    scrape_interval: 15s
    scrape_timeout: 10s

  - job_name: 'node-exporter'
    static_configs:
      - targets: ['node-exporter:9100']
    metrics_path: /metrics
    scrape_interval: 15s
    scrape_timeout: 10s

  - job_name: 'blackbox'
    metrics_path: /probe
    params:
      module: [http_2xx]
    static_configs:
      - targets:
        - https://${DOMAIN}/health
        - http://${DOMAIN}:8081/actuator/health
        - http://${DOMAIN}:8761/actuator/health
    relabel_configs:
      - source_labels: [__address__]
        target_label: __param_target
      - source_labels: [__param_target]
        target_label: instance
      - target_label: __address__
        replacement: blackbox-exporter:9115

  - job_name: 'application-endpoints'
    metrics_path: /probe
    params:
      module: [http_2xx]
    static_configs:
      - targets:
        - https://${DOMAIN}/api/product-service/products
        - https://${DOMAIN}/api/category-service/categories
        - https://${DOMAIN}/api/auth-service/health
    relabel_configs:
      - source_labels: [__address__]
        target_label: __param_target
      - source_labels: [__param_target]
        target_label: instance
      - target_label: __address__
        replacement: blackbox-exporter:9115
EOF

# --- Run SSL setup and deployment scripts ---
echo "🔒 Running SSL setup script"
sudo ./ssl-setup.sh $DOMAIN $EMAIL

echo "🚀 Running deployment script"
sudo ./deploy.sh

echo ""
echo "✅ Deployment complete! Visit https://$DOMAIN"
echo ""
echo "📁 Summary of generated/updated files:"
echo "  • .env"
echo "  • nginx-ssl-ec2-prod.conf"
echo "  • prometheus/prometheus-ec2-prod.yml"
echo ""
echo "🔑 Please review .env and fill in any secrets (passwords, JWT, mail, etc.) before going live."
