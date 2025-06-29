# PURELY E-commerce Application - Technical Documentation

This directory contains comprehensive technical documentation for the PURELY e-commerce application, covering deployment, configuration, troubleshooting, and advanced features.

## 📚 Documentation Structure

### 🚀 Core Deployment Guides

1. **[COMPREHENSIVE_DEPLOYMENT_GUIDE.md](./COMPREHENSIVE_DEPLOYMENT_GUIDE.md)**
   - Complete deployment instructions for all environments
   - Step-by-step setup for development, staging, and production
   - Docker and Docker Compose configuration
   - Environment-specific configurations

2. **[AWS_EC2_DEPLOYMENT_GUIDE.md](./AWS_EC2_DEPLOYMENT_GUIDE.md)**
   - AWS EC2 specific deployment instructions
   - Instance setup and configuration
   - Security group configuration
   - Production deployment best practices

3. **[QUICK_START_GUIDE.md](./QUICK_START_GUIDE.md)**
   - Fast setup for development environment
   - Essential commands and configurations
   - Quick troubleshooting steps

### 🔧 Configuration Guides

4. **[CORS_SETUP_GUIDE.md](./CORS_SETUP_GUIDE.md)**
   - Cross-Origin Resource Sharing configuration
   - Frontend-backend communication setup
   - Security considerations and best practices

5. **[PORT_SETUP_GUIDE.md](./PORT_SETUP_GUIDE.md)**
   - Port configuration for all services
   - Port conflict resolution
   - Network troubleshooting commands

6. **[SSL_SETUP_GUIDE.md](./SSL_SETUP_GUIDE.md)**
   - SSL certificate configuration
   - Let's Encrypt setup
   - Self-signed certificates for development
   - HTTPS enforcement

### 🚨 Troubleshooting and Support

7. **[TROUBLESHOOTING_GUIDE.md](./TROUBLESHOOTING_GUIDE.md)**
   - Comprehensive troubleshooting for common issues
   - Service connectivity problems
   - Database and network issues
   - Emergency recovery procedures

### 🎯 Advanced Features

8. **[QUEUE_IT_INTEGRATION_GUIDE.md](./QUEUE_IT_INTEGRATION_GUIDE.md)**
   - Virtual waiting room integration
   - High-traffic event management
   - Queue-it configuration and setup
   - Frontend and backend integration

### 📊 Testing and Performance

9. **[testing-projects/](./testing-projects/)**
   - Load testing frameworks and configurations
   - Performance testing tools
   - JMeter test plans
   - Selenium-based testing

### 🔍 Observability and Monitoring

10. **[00-gitrepo-sample-full-observability-demo/](./00-gitrepo-sample-full-observability-demo/)**
    - Complete observability stack setup
    - Prometheus, Grafana, and Promtail configuration
    - Monitoring dashboards
    - Log aggregation and analysis

## 🎯 Quick Navigation

### For New Developers
1. Start with **[QUICK_START_GUIDE.md](./QUICK_START_GUIDE.md)**
2. Review **[COMPREHENSIVE_DEPLOYMENT_GUIDE.md](./COMPREHENSIVE_DEPLOYMENT_GUIDE.md)**
3. Check **[CORS_SETUP_GUIDE.md](./CORS_SETUP_GUIDE.md)** for frontend-backend setup

### For Production Deployment
1. Follow **[AWS_EC2_DEPLOYMENT_GUIDE.md](./AWS_EC2_DEPLOYMENT_GUIDE.md)**
2. Configure SSL with **[SSL_SETUP_GUIDE.md](./SSL_SETUP_GUIDE.md)**
3. Set up monitoring from **[00-gitrepo-sample-full-observability-demo/](./00-gitrepo-sample-full-observability-demo/)**

### For High-Traffic Events
1. Implement Queue-it with **[QUEUE_IT_INTEGRATION_GUIDE.md](./QUEUE_IT_INTEGRATION_GUIDE.md)**
2. Set up load testing from **[testing-projects/](./testing-projects/)**
3. Monitor performance with observability tools

### For Troubleshooting
1. Check **[TROUBLESHOOTING_GUIDE.md](./TROUBLESHOOTING_GUIDE.md)** for common issues
2. Review **[PORT_SETUP_GUIDE.md](./PORT_SETUP_GUIDE.md)** for network problems
3. Use monitoring tools for diagnostics

## 🔧 Environment-Specific Configurations

### Development Environment
- Local Docker setup
- Hot reloading enabled
- Debug logging active
- Queue-it bypass enabled

### Staging Environment
- Production-like configuration
- SSL certificates configured
- Monitoring enabled
- Load testing performed

### Production Environment
- Full SSL enforcement
- Queue-it integration active
- Performance monitoring
- Automated backups

## 📋 Prerequisites

Before using these guides, ensure you have:

- **Docker and Docker Compose** installed
- **Java 17** or higher
- **Node.js 18** or higher
- **Git** for version control
- **AWS CLI** (for EC2 deployment)
- **Basic networking knowledge**

## 🚀 Getting Started

1. **Clone the repository** and navigate to the project root
2. **Choose your deployment path** based on your needs
3. **Follow the appropriate guide** from the list above
4. **Test your setup** using the provided testing tools
5. **Monitor performance** with the observability stack

## 📞 Support

If you encounter issues:

1. **Check the troubleshooting guide** first
2. **Review relevant configuration guides**
3. **Use the monitoring tools** for diagnostics
4. **Check service logs** for detailed error information

## 🔄 Updates and Maintenance

- **Regular updates** to deployment guides
- **Security patches** and configuration updates
- **Performance optimizations** and best practices
- **New feature integrations** and documentation

---

*For the most up-to-date information, always refer to the latest version of these guides and check the main project README for any recent changes.*

## 🛠️ Utility Scripts

- **`ec2-java-clean-setup.sh`