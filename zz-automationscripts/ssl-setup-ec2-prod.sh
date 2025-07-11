#!/bin/bash

# PURELY E-commerce Application - Production SSL Certificate Setup Script for EC2
# This script sets up SSL certificates using Let's Encrypt for production deployment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Production Configuration
DOMAIN=${1:-"18.217.148.69"}  # Default to EC2 IP if no domain provided
EMAIL=${2:-"admin@example.com"}  # Email for Let's Encrypt notifications
CERTBOT_PATH="/etc/letsencrypt"
WEBROOT_PATH="/var/www/html"

echo -e "${BLUE}🔒 PURELY E-commerce Production SSL Certificate Setup${NC}"
echo -e "${YELLOW}Domain: ${DOMAIN}${NC}"
echo -e "${YELLOW}Email: ${EMAIL}${NC}"
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

# Update system packages
print_status "Updating system packages..."
apt-get update -qq

# Install required packages
print_status "Installing required packages..."
apt-get install -y certbot python3-certbot-nginx nginx apache2-utils

# Create webroot directory for ACME challenge
print_status "Creating webroot directory for ACME challenge..."
mkdir -p ${WEBROOT_PATH}/.well-known/acme-challenge
chown -R www-data:www-data ${WEBROOT_PATH}

# Create temporary nginx configuration for ACME challenge
print_status "Creating temporary nginx configuration for ACME challenge..."
cat > /etc/nginx/sites-available/acme-challenge << 'EOF'
server {
    listen 80;
    server_name _;
    
    location /.well-known/acme-challenge/ {
        root /var/www/html;
        try_files $uri =404;
    }
    
    location / {
        return 404;
    }
}
EOF

# Enable the temporary site
ln -sf /etc/nginx/sites-available/acme-challenge /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default

# Test nginx configuration
print_status "Testing nginx configuration..."
nginx -t

# Start nginx
print_status "Starting nginx..."
systemctl start nginx
systemctl enable nginx

# Obtain SSL certificate
print_status "Obtaining SSL certificate from Let's Encrypt..."
if certbot certonly --webroot \
    --webroot-path=${WEBROOT_PATH} \
    --email ${EMAIL} \
    --agree-tos \
    --no-eff-email \
    --domains ${DOMAIN} \
    --non-interactive; then
    print_status "SSL certificate obtained successfully!"
else
    print_error "Failed to obtain SSL certificate"
    print_warning "Make sure your domain points to this server and port 80 is accessible"
    print_warning "For IP-based certificates, you may need to use a domain name"
    exit 1
fi

# Create SSL configuration
print_status "Creating SSL configuration..."
cat > /etc/nginx/sites-available/ssl-config << 'EOF'
# SSL Configuration for Production
ssl_protocols TLSv1.2 TLSv1.3;
ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-RSA-AES128-SHA256:ECDHE-RSA-AES256-SHA384;
ssl_prefer_server_ciphers off;
ssl_session_cache shared:SSL:10m;
ssl_session_timeout 10m;
ssl_stapling on;
ssl_stapling_verify on;

# Security headers for production
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
add_header X-Frame-Options DENY always;
add_header X-Content-Type-Options nosniff always;
add_header X-XSS-Protection "1; mode=block" always;
add_header Referrer-Policy "strict-origin-when-cross-origin" always;
add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self' https:; frame-ancestors 'none';" always;
EOF

# Create main SSL site configuration for production
print_status "Creating main SSL site configuration for production..."
cat > /etc/nginx/sites-available/purely-ssl-prod << EOF
# PURELY E-commerce Application - Production SSL Configuration
server {
    listen 80;
    server_name ${DOMAIN};
    
    # Redirect all HTTP traffic to HTTPS
    return 301 https://\$server_name\$request_uri;
}

server {
    listen 443 ssl http2;
    server_name ${DOMAIN};
    
    # SSL Configuration for Production
    ssl_certificate /etc/letsencrypt/live/${DOMAIN}/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/${DOMAIN}/privkey.pem;
    include /etc/nginx/sites-available/ssl-config;
    
    # Frontend static files with production caching
    location / {
        root /usr/share/nginx/html;
        try_files \$uri \$uri/ /index.html;
        
        # Cache control for static assets
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
            add_header Vary "Accept-Encoding";
        }
        
        # Cache control for HTML files
        location ~* \.html$ {
            expires 1h;
            add_header Cache-Control "public, must-revalidate";
        }
        
        # CORS headers for production frontend
        add_header Access-Control-Allow-Origin "https://${DOMAIN}" always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "*" always;
    }
    
    # Health check endpoint
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
    
    # API routes with production rate limiting
    location /api/ {
        # Rate limiting for production
        limit_req zone=api burst=30 nodelay;
        
        proxy_pass http://api-gateway:8081/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
        
        # CORS headers for production API
        add_header Access-Control-Allow-Origin "https://${DOMAIN}" always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "*" always;
        add_header Access-Control-Allow-Credentials "true" always;
        
        # Handle preflight requests
        if (\$request_method = 'OPTIONS') {
            add_header Access-Control-Allow-Origin "https://${DOMAIN}" always;
            add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
            add_header Access-Control-Allow-Headers "*" always;
            add_header Access-Control-Allow-Credentials "true" always;
            add_header Content-Type "text/plain charset=UTF-8";
            add_header Content-Length 0;
            return 204;
        }
    }
    
    # Error pages
    error_page 404 /404.html;
    error_page 500 502 503 504 /50x.html;
    
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
EOF

# Enable the SSL site
print_status "Enabling SSL site configuration..."
ln -sf /etc/nginx/sites-available/purely-ssl-prod /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/acme-challenge

# Test nginx configuration
print_status "Testing nginx configuration..."
nginx -t

# Reload nginx
print_status "Reloading nginx..."
systemctl reload nginx

# Set up automatic certificate renewal
print_status "Setting up automatic certificate renewal..."
cat > /etc/cron.d/certbot-renew-prod << EOF
# Certbot renewal for production
0 12 * * * root certbot renew --quiet --deploy-hook "systemctl reload nginx"
EOF

# Create certificate renewal script for production
cat > /usr/local/bin/renew-ssl-prod.sh << 'EOF'
#!/bin/bash
# Production SSL Certificate Renewal Script

echo "Renewing production SSL certificates..."
certbot renew --quiet --deploy-hook "systemctl reload nginx"

if [ $? -eq 0 ]; then
    echo "Production SSL certificates renewed successfully"
    # Copy renewed certificates to project directory
    sudo cp /etc/letsencrypt/live/18.217.148.69/fullchain.pem /opt/ecommerce-web-application/ssl/nginx-selfsigned.crt
    sudo cp /etc/letsencrypt/live/18.217.148.69/privkey.pem /opt/ecommerce-web-application/ssl/nginx-selfsigned.key
    sudo chown $USER:$USER /opt/ecommerce-web-application/ssl/*
    chmod 600 /opt/ecommerce-web-application/ssl/nginx-selfsigned.key
    chmod 644 /opt/ecommerce-web-application/ssl/nginx-selfsigned.crt
else
    echo "Production SSL certificate renewal failed"
    exit 1
fi
EOF

chmod +x /usr/local/bin/renew-ssl-prod.sh

# Test certificate renewal
print_status "Testing certificate renewal..."
certbot renew --dry-run

# Create SSL status check script for production
cat > /usr/local/bin/ssl-status-prod.sh << 'EOF'
#!/bin/bash
# Production SSL Certificate Status Check Script

DOMAIN=${1:-"18.217.148.69"}
CERT_PATH="/etc/letsencrypt/live/${DOMAIN}/fullchain.pem"

if [ -f "$CERT_PATH" ]; then
    echo "Production SSL Certificate Status for ${DOMAIN}:"
    echo "================================================"
    echo "Certificate file: $CERT_PATH"
    echo "Issuer: $(openssl x509 -in $CERT_PATH -noout -issuer | cut -d= -f2-)"
    echo "Valid from: $(openssl x509 -in $CERT_PATH -noout -startdate | cut -d= -f2-)"
    echo "Valid until: $(openssl x509 -in $CERT_PATH -noout -enddate | cut -d= -f2-)"
    echo "Days until expiry: $(echo $(( ($(date -d "$(openssl x509 -in $CERT_PATH -noout -enddate | cut -d= -f2-)" +%s) - $(date +%s)) / 86400 )))"
    echo ""
    echo "Certificate Chain:"
    openssl x509 -in $CERT_PATH -text -noout | grep -A1 "Subject Alternative Name"
else
    echo "Production SSL certificate not found at $CERT_PATH"
fi
EOF

chmod +x /usr/local/bin/ssl-status-prod.sh

# Create SSL troubleshooting script for production
cat > /usr/local/bin/ssl-troubleshoot-prod.sh << 'EOF'
#!/bin/bash
# Production SSL Troubleshooting Script

DOMAIN=${1:-"18.217.148.69"}

echo "Production SSL Troubleshooting for ${DOMAIN}"
echo "============================================="

# Check certificate files
echo "1. Checking certificate files..."
if [ -f "/etc/letsencrypt/live/${DOMAIN}/fullchain.pem" ]; then
    echo "   ✅ Certificate file exists"
else
    echo "   ❌ Certificate file missing"
fi

if [ -f "/etc/letsencrypt/live/${DOMAIN}/privkey.pem" ]; then
    echo "   ✅ Private key file exists"
else
    echo "   ❌ Private key file missing"
fi

# Check nginx configuration
echo "2. Checking nginx configuration..."
if nginx -t > /dev/null 2>&1; then
    echo "   ✅ Nginx configuration is valid"
else
    echo "   ❌ Nginx configuration has errors"
    nginx -t
fi

# Check nginx status
echo "3. Checking nginx status..."
if systemctl is-active --quiet nginx; then
    echo "   ✅ Nginx is running"
else
    echo "   ❌ Nginx is not running"
fi

# Test SSL connection
echo "4. Testing SSL connection..."
if curl -k -s -o /dev/null -w "%{http_code}" https://${DOMAIN} | grep -q "200\|301\|302"; then
    echo "   ✅ SSL connection successful"
else
    echo "   ❌ SSL connection failed"
fi

# Check certificate expiry
echo "5. Checking certificate expiry..."
DAYS_LEFT=$(echo $(( ($(date -d "$(openssl x509 -in /etc/letsencrypt/live/${DOMAIN}/fullchain.pem -noout -enddate | cut -d= -f2-)" +%s) - $(date +%s)) / 86400 )))
if [ $DAYS_LEFT -gt 30 ]; then
    echo "   ✅ Certificate expires in $DAYS_LEFT days"
elif [ $DAYS_LEFT -gt 0 ]; then
    echo "   ⚠️  Certificate expires in $DAYS_LEFT days (renewal recommended)"
else
    echo "   ❌ Certificate has expired"
fi

# Check SSL configuration
echo "6. Checking SSL configuration..."
SSL_CONFIG=$(nginx -T 2>/dev/null | grep -A 20 "ssl_protocols")
if echo "$SSL_CONFIG" | grep -q "TLSv1.2\|TLSv1.3"; then
    echo "   ✅ SSL protocols configured correctly"
else
    echo "   ❌ SSL protocols not configured"
fi
EOF

chmod +x /usr/local/bin/ssl-troubleshoot-prod.sh

# Display final status
echo ""
echo -e "${GREEN}🎉 Production SSL Setup Complete!${NC}"
echo ""
echo -e "${BLUE}📋 Summary:${NC}"
echo -e "  • SSL certificate obtained for: ${YELLOW}${DOMAIN}${NC}"
echo -e "  • Certificate location: ${YELLOW}/etc/letsencrypt/live/${DOMAIN}/${NC}"
echo -e "  • Automatic renewal: ${YELLOW}Daily at 12:00 PM${NC}"
echo -e "  • Nginx configuration: ${YELLOW}/etc/nginx/sites-available/purely-ssl-prod${NC}"
echo ""
echo -e "${BLUE}🔧 Available Commands:${NC}"
echo -e "  • Check SSL status: ${YELLOW}sudo /usr/local/bin/ssl-status-prod.sh${NC}"
echo -e "  • Troubleshoot SSL: ${YELLOW}sudo /usr/local/bin/ssl-troubleshoot-prod.sh${NC}"
echo -e "  • Manual renewal: ${YELLOW}sudo /usr/local/bin/renew-ssl-prod.sh${NC}"
echo ""
echo -e "${BLUE}🌐 Access URLs:${NC}"
echo -e "  • HTTPS: ${YELLOW}https://${DOMAIN}${NC}"
echo -e "  • HTTP (redirects to HTTPS): ${YELLOW}http://${DOMAIN}${NC}"
echo ""
echo -e "${YELLOW}⚠️  Important Notes:${NC}"
echo -e "  • Make sure your domain DNS points to this server"
echo -e "  • Port 80 and 443 must be open in your firewall"
echo -e "  • Certificates auto-renew 30 days before expiry"
echo -e "  • Check renewal logs: ${YELLOW}sudo journalctl -u certbot.timer${NC}"
echo -e "  • For production deployment, copy certificates to project directory"
echo ""

print_status "Production SSL setup completed successfully!" 