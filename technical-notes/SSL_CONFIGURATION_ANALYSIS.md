# SSL Configuration Analysis for EC2 Deployment

## Current SSL Setup (Local Development)

### 1. **Nginx SSL Configuration**
```yaml
# Current docker-compose.yml (lines 295-310)
nginx:
  ports:
    - "80:80"
    - "443:443"  # HTTPS port
  volumes:
    - ./nginx-ssl.conf:/etc/nginx/nginx.conf
    # SSL Certificates (commented out)
    # - /etc/letsencrypt:/etc/letsencrypt:ro
    # - /var/www/html:/var/www/html:ro
    # Self-signed certificates for testing
    - ./ssl:/etc/nginx/ssl:ro
```

### 2. **Current SSL Certificate**
- **Type**: Self-signed certificate (for local development)
- **Location**: `./ssl/` directory
- **Purpose**: Local HTTPS testing

## Required Changes for Let's Encrypt (EC2)

### 1. **Update docker-compose.yml**

```yaml
nginx:
  image: nginx:alpine
  container_name: purely_nginx
  restart: unless-stopped
  ports:
    - "80:80"
    - "443:443"
  volumes:
    - ./nginx-ssl.conf:/etc/nginx/nginx.conf
    - ./frontend/dist:/usr/share/nginx/html
    # Let's Encrypt certificates (UNCOMMENT THESE)
    - /etc/letsencrypt:/etc/letsencrypt:ro
    - /var/www/html:/var/www/html:ro
    # Remove self-signed certificates
    # - ./ssl:/etc/nginx/ssl:ro
  networks:
    - frontend-network
    - backend-network
```

### 2. **Update nginx-ssl.conf**

**Current Configuration (Self-signed):**
```nginx
ssl_certificate /etc/nginx/ssl/cert.pem;
ssl_certificate_key /etc/nginx/ssl/key.pem;
```

**Let's Encrypt Configuration:**
```nginx
ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
```

### 3. **EC2 Deployment Steps**

#### Step 1: Install Certbot
```bash
# Install Certbot
sudo apt update
sudo apt install certbot python3-certbot-nginx

# Stop nginx temporarily
sudo systemctl stop nginx
```

#### Step 2: Get SSL Certificate
```bash
# Get certificate (replace with your domain)
sudo certbot certonly --standalone -d your-domain.com

# Or use nginx plugin
sudo certbot --nginx -d your-domain.com
```

#### Step 3: Update Nginx Configuration
```bash
# Update nginx config to use Let's Encrypt certificates
sudo nano /etc/nginx/sites-available/ecommerce

# Add SSL configuration
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    # SSL configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # Your existing location blocks here
    location / {
        proxy_pass http://localhost:5173;
        # ... other proxy settings
    }
    
    location /api/ {
        proxy_pass http://localhost:8081/;
        # ... other proxy settings
    }
}

# HTTP to HTTPS redirect
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

#### Step 4: Auto-renewal Setup
```bash
# Add to crontab for auto-renewal
sudo crontab -e

# Add this line
0 12 * * * /usr/bin/certbot renew --quiet
```

### 4. **Security Group Configuration**

**EC2 Security Groups:**
- **HTTP (80)**: Open for Let's Encrypt verification
- **HTTPS (443)**: Open for secure traffic
- **SSH (22)**: Restricted to your IP

### 5. **Environment Variables**

**Update .env file:**
```env
# SSL Configuration
SSL_ENABLED=true
DOMAIN_NAME=your-domain.com
LETS_ENCRYPT_EMAIL=your-email@example.com

# Remove local SSL settings
# SSL_CERT_PATH=./ssl/cert.pem
# SSL_KEY_PATH=./ssl/key.pem
```

## Key Differences

### **Local Development:**
- ✅ Self-signed certificates
- ✅ Local file paths (`./ssl/`)
- ✅ No domain verification needed
- ✅ Browser security warnings (acceptable for dev)

### **EC2 Production:**
- ✅ Let's Encrypt certificates
- ✅ System file paths (`/etc/letsencrypt/`)
- ✅ Domain ownership verification required
- ✅ Valid SSL certificates (no browser warnings)

## Monitoring Impact

### **Node Exporter (ENABLED):**
- ✅ Full system metrics available
- ✅ CPU, memory, disk, network monitoring
- ✅ No impact on SSL configuration

### **Prometheus Configuration:**
- ✅ Will automatically detect HTTPS endpoints
- ✅ SSL certificate expiry monitoring available
- ✅ No changes needed for monitoring

## Testing Checklist

After EC2 deployment:
- [ ] SSL certificate is valid
- [ ] HTTPS redirects work
- [ ] All services accessible via HTTPS
- [ ] SSL certificate auto-renewal works
- [ ] Monitoring dashboards show SSL metrics
- [ ] No mixed content warnings in browser

## Troubleshooting

### **Common Issues:**
1. **Certificate not found**: Check file paths and permissions
2. **Domain verification failed**: Ensure DNS is properly configured
3. **Nginx won't start**: Check SSL certificate syntax
4. **Auto-renewal fails**: Verify crontab and permissions

### **Commands:**
```bash
# Check certificate status
sudo certbot certificates

# Test nginx configuration
sudo nginx -t

# Check SSL certificate
openssl s_client -connect your-domain.com:443 -servername your-domain.com

# Monitor certificate expiry
sudo certbot renew --dry-run
``` 