#!/bin/bash

# PURELY E-commerce Application - Self-Signed Certificate Generator
# This script generates self-signed SSL certificates for testing purposes

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DOMAIN=${1:-"18.217.148.69"}
SSL_DIR="/etc/nginx/ssl"
CERT_FILE="${SSL_DIR}/nginx-selfsigned.crt"
KEY_FILE="${SSL_DIR}/nginx-selfsigned.key"

echo -e "${BLUE}🔒 PURELY E-commerce Self-Signed Certificate Generator${NC}"
echo -e "${YELLOW}Domain: ${DOMAIN}${NC}"
echo -e "${YELLOW}Certificate: ${CERT_FILE}${NC}"
echo -e "${YELLOW}Private Key: ${KEY_FILE}${NC}"
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

# Create SSL directory
print_status "Creating SSL directory..."
mkdir -p ${SSL_DIR}
chmod 700 ${SSL_DIR}

# Generate self-signed certificate
print_status "Generating self-signed certificate..."
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout ${KEY_FILE} \
    -out ${CERT_FILE} \
    -subj "/C=US/ST=State/L=City/O=PURELY/OU=IT/CN=${DOMAIN}" \
    -addext "subjectAltName=DNS:${DOMAIN},DNS:localhost,IP:${DOMAIN},IP:127.0.0.1"

# Set proper permissions
print_status "Setting certificate permissions..."
chmod 600 ${KEY_FILE}
chmod 644 ${CERT_FILE}
chown root:root ${KEY_FILE} ${CERT_FILE}

# Verify certificate
print_status "Verifying certificate..."
openssl x509 -in ${CERT_FILE} -text -noout | head -20

# Create certificate info script
cat > /usr/local/bin/cert-info.sh << 'EOF'
#!/bin/bash
# Certificate Information Script

DOMAIN=${1:-"18.217.148.69"}
CERT_FILE="/etc/nginx/ssl/nginx-selfsigned.crt"

if [ -f "$CERT_FILE" ]; then
    echo "Self-Signed Certificate Information:"
    echo "===================================="
    echo "Certificate file: $CERT_FILE"
    echo "Subject: $(openssl x509 -in $CERT_FILE -noout -subject | cut -d= -f2-)"
    echo "Issuer: $(openssl x509 -in $CERT_FILE -noout -issuer | cut -d= -f2-)"
    echo "Valid from: $(openssl x509 -in $CERT_FILE -noout -startdate | cut -d= -f2-)"
    echo "Valid until: $(openssl x509 -in $CERT_FILE -noout -enddate | cut -d= -f2-)"
    echo "Days until expiry: $(echo $(( ($(date -d "$(openssl x509 -in $CERT_FILE -noout -enddate | cut -d= -f2-)" +%s) - $(date +%s)) / 86400 )))"
    echo ""
    echo "SAN (Subject Alternative Names):"
    openssl x509 -in $CERT_FILE -text -noout | grep -A1 "Subject Alternative Name" | tail -1
else
    echo "Certificate not found at $CERT_FILE"
fi
EOF

chmod +x /usr/local/bin/cert-info.sh

# Display final status
echo ""
echo -e "${GREEN}🎉 Self-Signed Certificate Generated Successfully!${NC}"
echo ""
echo -e "${BLUE}📋 Summary:${NC}"
echo -e "  • Certificate: ${YELLOW}${CERT_FILE}${NC}"
echo -e "  • Private Key: ${YELLOW}${KEY_FILE}${NC}"
echo -e "  • Domain: ${YELLOW}${DOMAIN}${NC}"
echo -e "  • Validity: ${YELLOW}365 days${NC}"
echo ""
echo -e "${BLUE}🔧 Available Commands:${NC}"
echo -e "  • View certificate info: ${YELLOW}sudo /usr/local/bin/cert-info.sh${NC}"
echo -e "  • Test SSL connection: ${YELLOW}curl -k https://${DOMAIN}${NC}"
echo ""
echo -e "${YELLOW}⚠️  Important Notes:${NC}"
echo -e "  • This is a self-signed certificate for testing only"
echo -e "  • Browsers will show security warnings"
echo -e "  • For production, use Let's Encrypt certificates"
echo -e "  • Certificate expires in 365 days"
echo ""

print_status "Self-signed certificate setup completed!"
print_warning "Remember to update nginx configuration to use these certificates" 