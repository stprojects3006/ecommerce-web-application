# PURELY E-commerce Application - Technical Documentation

This directory contains comprehensive documentation for the PURELY E-commerce microservices application.

## 📚 Documentation Guide

### 🚀 Quick Start
- **`QUICK_START_GUIDE.md`** - Fast deployment guide for developers
  - Essential commands and steps
  - Service architecture overview
  - Access points and monitoring setup
  - Perfect for getting up and running quickly

### 📖 Comprehensive Reference
- **`COMPREHENSIVE_DEPLOYMENT_GUIDE.md`** - Complete deployment documentation
  - Detailed system architecture
  - All fixes and configurations implemented
  - Troubleshooting guide
  - Service configuration details
  - Use this for complete understanding and troubleshooting

### ☁️ AWS Deployment
- **`AWS_EC2_DEPLOYMENT_GUIDE.md`** - Production deployment on AWS EC2
  - EC2 instance setup and configuration
  - Security group configuration
  - Production environment setup
  - Monitoring and scaling considerations

### ⚙️ Configuration Guides
- **`CORS_SETUP_GUIDE.md`** - Cross-Origin Resource Sharing configuration
  - API Gateway CORS settings
  - Nginx CORS configuration
  - Frontend API configuration
  - Domain-specific CORS setup

- **`PORT_SETUP_GUIDE.md`** - Port configuration and networking
  - Service port assignments
  - Docker network configuration
  - Firewall and security group setup
  - Load balancer configuration
  - **Port conflict troubleshooting commands**

- **`SSL_SETUP_GUIDE.md`** - SSL Certificate configuration
  - Self-signed certificates for testing
  - Let's Encrypt certificates for production
  - Automatic certificate renewal
  - SSL troubleshooting and management
  - Security best practices

### 🔧 Troubleshooting & Support
- **`TROUBLESHOOTING_GUIDE.md`** - Comprehensive troubleshooting guide
  - Service connectivity issues
  - CORS configuration problems
  - HTTP error diagnostics
  - Database connection issues
  - Network and DNS problems
  - Emergency recovery procedures
  - **Command examples and solutions for each issue**

### 📊 Monitoring & Observability
- **`graphana-dashboard-details.txt`** - Grafana dashboard configuration
- **`graphana-advanced-drilldowns.txt`** - Advanced Grafana features

### 🛠️ Utility Scripts
- **`ec2-java-clean-setup.sh`** - Clean Java installation on EC2
- **`create_pdf_documentation.sh`** - Generate PDF documentation

## 🎯 Which Documentation Should You Use?

| Use Case | Recommended Documentation |
|----------|---------------------------|
| **First-time setup** | Start with `QUICK_START_GUIDE.md` |
| **Production deployment** | Use `AWS_EC2_DEPLOYMENT_GUIDE.md` |
| **Troubleshooting issues** | Use `TROUBLESHOOTING_GUIDE.md` |
| **CORS configuration** | Use `CORS_SETUP_GUIDE.md` |
| **Port/networking issues** | Use `PORT_SETUP_GUIDE.md` |
| **SSL certificate setup** | Use `SSL_SETUP_GUIDE.md` |
| **Complete reference** | Use `COMPREHENSIVE_DEPLOYMENT_GUIDE.md` |

## 📋 Documentation Maintenance

- All documentation is kept up-to-date with the latest fixes and configurations
- Configuration guides are updated when new features are added
- AWS deployment guide includes current best practices
- Quick start guide is optimized for fast deployment
- Troubleshooting guide includes real-world solutions and commands

## 🔗 Related Files

- **Root directory**: Contains the actual application code and configuration
- **`docker-compose.yml`**: Main deployment configuration
- **`build.sh`** & **`deploy.sh`**: Automated deployment scripts
- **`nginx.conf`**: Reverse proxy configuration
- **`prometheus/`**: Monitoring configuration files

---

*Last updated: June 2025*
*For issues or questions, refer to the troubleshooting section in the comprehensive guide.* 