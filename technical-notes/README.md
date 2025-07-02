# PURELY E-commerce Application - Technical Documentation

This directory contains comprehensive technical documentation for the PURELY e-commerce application, covering deployment, configuration, troubleshooting, advanced features, and testing.

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

## 🧪 Testing

### **Functional Test Cases**
All test cases are in `testing-projects/queueit-functional-testing/`.

| Category         | Test File/Location                                      | Description |
|-----------------|---------------------------------------------------------|-------------|
| **Backend**     | `tests/backend/test_queueit_api.py`                     | API health, status, enqueue, stats, error handling |
|                 | `tests/backend/test_queueit_official_connector.py`      | Official Queue-It Java connector validation |
| **Frontend**    | `tests/frontend/test_queueit_frontend.py`               | Service init, overlay, indicator, token, mobile, error |
|                 | `tests/frontend/test_queueit_frontend_official.js`      | Official JS connector, event handling, redirect, token validation |
| **Integration** | `tests/integration/test_queueit_integration.py`         | End-to-end queue flow, token lifecycle, error recovery |
| **Performance** | `tests/performance/test_queueit_performance.py`         | Load, stress, memory, response time, throughput |
| **Quick Test**  | `simple_functional_test.py`                             | Health, status, enqueue, endpoints, error handling |
| **Test Runner** | `test.sh`, `run_queueit_tests.sh`, `generate_metrics.sh`| One-command and comprehensive test runners |

- See [QUEUE_IT_INTEGRATION_GUIDE.md](./QUEUE_IT_INTEGRATION_GUIDE.md) for full details and descriptions.

### **How to Run Tests**
- Quick test: `./test.sh`
- Comprehensive: `./run_queueit_tests.sh`
- Generate metrics: `./generate_metrics.sh`
- See results in `simple_test_results.json` and Grafana dashboards

## 📈 Grafana Dashboard Setup (Queue-It Monitoring)

### **Step-by-Step Guide**

1. **Start Prometheus and Grafana**
   - Use provided Docker Compose or manual setup
   - Ensure Prometheus is scraping API Gateway metrics

2. **Access Grafana**
   - Open your browser: [http://localhost:3000](http://localhost:3000)
   - Login: `admin` / `admin` (or `admin123`)

3. **Add Prometheus Data Source**
   - Go to Configuration (gear icon) → Data Sources
   - Click "Add data source"
   - Select "Prometheus"
   - Set URL: `http://prometheus:9090`
   - Click "Save & Test"

4. **Import Dashboards**
   - Go to Dashboards → Import
   - Upload JSON files from `config/grafana_dashboards/`:
     - `queueit-comprehensive-dashboard.json`
     - `queueit-api-performance.json`
     - `queueit-frontend-metrics.json`
     - `queueit-load-testing.json`

5. **View Metrics**
   - Open the imported dashboards
   - See real-time and historical test results, API performance, queue stats, and error rates

6. **Customize Panels**
   - Add new panels for custom queries (see below for example PromQL queries)
   - Adjust time ranges and refresh intervals as needed

### Example PromQL Queries for Queue-It Monitoring

To add a custom panel in Grafana:
1. Open your dashboard and click "Add panel" → "Add new panel".
2. In the "Query" section, select your Prometheus data source.
3. Copy and paste one of the example queries below.
4. Set visualization type (e.g., Time series, Gauge, Table).
5. Click "Apply" to save the panel.

**Common PromQL Queries:**

| Metric | PromQL Query | Description | Primary Panel (Default for Dashboard?) | Steps for How to Interpret the Dashboard Data                                                                                 |
|--------|--------------|-------------|----------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| 95th Percentile Request Latency | `histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le, service))` | Shows the 95th percentile latency per service | Yes | Focuses on API Gateway latency; spikes may indicate gateway bottlenecks.                                                     |
| Request Rate (req/s) | `sum by (service) (rate(http_server_requests_seconds_count[5m]))` | Requests per second per service | No | Requests per second per service |
| Error Rate (non-2xx) | `sum(rate(http_server_requests_seconds_count{status!~"2.."}[5m])) by (service)` | Error responses per second per service | No | Error responses per second per service |
| Queue Size (Active Users in Queue) | `queueit_active_queue_size{eventId="flash-sale-2024"}` | Number of users currently in the queue for a given event (replace eventId as needed) | No | Number of users currently in the queue for a given event (replace eventId as needed) |
| Service Health (Up/Down) | `up` | 1 if service is up, 0 if down (per instance) | Yes | Gaps or drops to 0 indicate downtime; correlate with other panels to diagnose impact. |
| Queue Wait Time (Average) | `avg_over_time(queueit_user_wait_time_seconds[5m])` | Average user wait time in queue over 5 minutes | No | Average user wait time in queue over 5 minutes |
| Queue Entrances (Total) | `sum(increase(queueit_users_enqueued_total[1h])) by (eventId)` | Number of users who entered the queue in the last hour | No | Number of users who entered the queue in the last hour |

**Tips:**
- You can filter by service, eventId, or status using curly braces, e.g. `{service="api-gateway"}`
- Adjust the time window (e.g., `[5m]`, `[1h]`) to match your needs
- Use "Legend" field to customize panel labels
- For more, see [Prometheus Query Documentation](https://prometheus.io/docs/prometheus/latest/querying/basics/)

### Grafana Version Compatibility

This guide is compatible with **Grafana 10.x** (recommended). UI options may differ slightly in other versions, but the core steps remain the same.

### Step-by-Step: Visualizing Metrics in Grafana (After Uploading a Dashboard)

1. **Open Grafana and Select Your Dashboard**
   - Go to the left sidebar and click the **four squares icon** (Dashboards).
   - Click **"Browse"** and select the dashboard you imported (e.g., "Queue-It Comprehensive Dashboard").

2. **Add a New Panel**
   - Click the **"Add panel"** button (top right, or via the "+" icon in the sidebar → "Dashboard" → "Add new panel").

3. **Configure the Query**
   - In the new panel editor, ensure the **data source** is set to **Prometheus**.
   - In the **Query** field, paste your desired PromQL query (see table above for examples).

   | Dashboard                        | Event Name           | Service Type      | PromQL Query                                                                                       | Visualization Type | Recommended Panel Name         | X-Axis Label      | Y-Axis Label         | Primary Panel (Default for Dashboard?) | Steps for How to Interpret the Dashboard Data                                                                                 |
   |-----------------------------------|----------------------|-------------------|----------------------------------------------------------------------------------------------------|--------------------|-------------------------------|-------------------|----------------------|----------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
   | queueit-comprehensive-dashboard   | All                  | api-gateway       | histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{service="api-gateway"}[5m])) by (le)) | Time series        | API Gateway 95th Percentile Latency | Time         | Latency (seconds)    | Yes                                    | Focuses on API Gateway latency; spikes may indicate gateway bottlenecks.                                                     |
   | queueit-comprehensive-dashboard   | flash-sale-2024      | All               | max_over_time(queueit_active_queue_size{eventId="flash-sale-2024"}[24h])                         | Gauge              | Max Flash Sale Queue Length    | Time              | Max Users in Queue   | No                                      | Shows the largest queue size for the flash sale event in the last 24h.                                                      |
   | queueit-comprehensive-dashboard   | black-friday         | All               | sum(rate(queueit_users_abandoned_total{eventId="black-friday"}[5m]))                             | Bar chart          | Black Friday Abandonment Rate  | Time              | Users Abandoned      | No                                      | High abandonment for Black Friday may indicate excessive wait times or technical issues.                                     |
   | queueit-comprehensive-dashboard   | flash-sale-2024      | All               | (sum(rate(queueit_users_served_total{eventId="flash-sale-2024"}[1m])) by (eventId))              | Time series        | Flash Sale Queue Throughput    | Time              | Users Served/min     | No                                      | Measures how many users are exiting the queue per minute; low throughput may indicate bottlenecks.                          |
   | queueit-comprehensive-dashboard   | All                  | All               | up                                                                                                | State timeline     | Service Health (Up/Down)      | Time              | Up (1) / Down (0)    | Yes                                    | Gaps or drops to 0 indicate downtime; correlate with other panels to diagnose impact.                                       |
   | queueit-api-performance          | All                  | product-service   | sum(rate(http_server_requests_seconds_count{service="product-service"}[5m]))                      | Time series        | Product Service Request Rate   | Time              | Requests per second  | No                                      | Monitors product-service traffic; drops may indicate product catalog issues.                                                 |
   | queueit-api-performance          | All                  | api-gateway       | sum(rate(http_server_requests_seconds_count{service="api-gateway",status!~"2.."}[5m]))          | Bar gauge          | API Gateway Error Rate         | Time              | Errors per second    | Yes                                    | Tracks non-2xx errors for API Gateway; persistent errors require investigation.                                              |
   | queueit-api-performance          | All                  | api-gateway       | sum(rate(http_server_requests_seconds_count{service="api-gateway"}[5m]))                          | Time series        | API Gateway Request Rate       | Time              | Requests per second  | Yes                                    | Drops may indicate outages; spikes may indicate traffic surges.                                                             |
   | queueit-api-performance          | All                  | api-gateway       | sum(rate(http_server_requests_seconds_count{service="api-gateway",status!~"2.."}[5m])) by (endpoint) | Table         | API Error Rate by Endpoint    | Endpoint          | Errors per second    | No                                      | Pinpoint which API endpoints are failing most; focus troubleshooting on high-error endpoints.                               |
   | queueit-api-performance          | All                  | api-gateway       | sum(rate(queueit_token_validation_failures_total{service="api-gateway"}[5m]))                    | Bar chart          | API Gateway Token Failures     | Time              | Failures per second  | No                                      | Token validation failures in API Gateway; may indicate integration or security issues.                                      |
   | queueit-frontend-metrics         | checkout-protection  | All               | avg_over_time(queueit_user_wait_time_seconds{eventId="checkout-protection"}[5m])                 | Time series        | Checkout Wait Time             | Time              | Wait Time (seconds)  | Yes                                    | Measures average wait time for checkout protection event; high values may impact conversions.                                |
   | queueit-frontend-metrics         | flash-sale-2024      | All               | queueit_active_queue_size{eventId="flash-sale-2024"}                                             | Gauge              | Flash Sale Queue Size          | Time              | Users in Queue       | Yes                                    | Real-time queue size for flash sale; use to monitor event demand.                                                           |
   | queueit-frontend-metrics         | All                  | frontend          | sum(rate(queueit_frontend_errors_total{service="frontend",errorType="overlay_display"}[5m]))    | Bar chart          | Frontend Overlay Errors        | Time              | Errors per second    | No                                      | Tracks overlay display errors in the frontend service; high rates may indicate UI bugs.                                      |
   | queueit-frontend-metrics         | flash-sale-2024      | All               | (sum(rate(queueit_users_served_total{eventId="flash-sale-2024",waitTimeSeconds<=120}[5m])) / sum(rate(queueit_users_served_total{eventId="flash-sale-2024"}[5m])) * 100 | Gauge | Flash Sale % Served <2min | Time | Percentage (%) | No | Shows percent of flash sale users served within 2 minutes; low values may require queue tuning.                             |
   | queueit-frontend-metrics         | All                  | frontend          | sum(rate(queueit_frontend_errors_total{service="frontend",errorType="token_expired"}[5m]))      | Bar chart          | Frontend Token Expiry Errors   | Time              | Errors per second    | No                                      | Tracks token expiry errors in the frontend; high rates may indicate session or clock issues.                                |
   | queueit-frontend-metrics         | All                  | frontend          | sum(rate(queueit_frontend_errors_total{service="frontend"}[5m])) by (errorType)                  | Bar chart          | Frontend Error Rate            | Error Type        | Errors per second    | No                                      | Identify frequent frontend error types; high rates may indicate UI or integration issues.                                   |
   | queueit-frontend-metrics         | flash-sale-2024      | All               | avg_over_time(queueit_user_wait_time_seconds{eventId="flash-sale-2024"}[5m])                     | Time series        | Flash Sale Avg Wait Time       | Time              | Wait Time (seconds)  | Yes                                    | Average wait time for flash sale event; monitor for user experience and adjust queue settings if needed.                    |
   | queueit-frontend-metrics         | All                  | All               | (sum(rate(queueit_users_served_total{waitTimeSeconds<=120}[5m])) / sum(rate(queueit_users_served_total[5m])) * 100 | Gauge | % Users Served Within SLA (2min) | Time | Percentage (%) | No | Shows what percent of users are served within 2 minutes; low values may require queue tuning.                               |
   | queueit-load-testing             | black-friday         | All               | sum(increase(queueit_users_enqueued_total{eventId="black-friday"}[1h]))                          | Bar chart          | Black Friday Entrances (1h)    | Time              | Users Enqueued       | Yes                                    | Number of users who joined the Black Friday queue in the last hour.                                                         |
   | queueit-load-testing             | flash-sale-2024      | api-gateway       | max_over_time(http_server_requests_seconds_max{service="api-gateway"}[1h])                        | Time series        | API Gateway Max Latency (1h)   | Time              | Max Latency (s)      | Yes                                    | Maximum observed latency for API Gateway during load test; spikes may indicate scaling issues.                              |
   | queueit-load-testing             | checkout-protection  | All               | min_over_time(timestamp(queueit_users_served_total{eventId="checkout-protection"}[5m] > 0)[1h:1m])| Table              | Checkout Time to First Served  | Event ID          | Timestamp            | No                                      | When the first user was served for checkout protection; long delays may indicate slow queue start.                          |
   | queueit-load-testing             | flash-sale-2024      | All               | sum(rate(queueit_users_served_total{eventId="flash-sale-2024"}[5m]))                             | Time series        | Flash Sale Users Served        | Time              | Users Served         | Yes                                    | Number of users who exited the flash sale queue; helps measure event throughput.                                            |
   | queueit-load-testing             | black-friday         | All               | sum(rate(queueit_users_served_total{eventId="black-friday"}[5m]))                                | Time series        | Black Friday Users Served      | Time              | Users Served         | Yes                                    | Number of users who exited the Black Friday queue; compare to flash sale for event analysis.                                |
   | queueit-load-testing             | All                  | api-gateway       | sum(rate(http_server_requests_seconds_count{service="api-gateway"}[1m]))                         | Time series        | API Gateway Request Rate (Load Test) | Time         | Requests per second  | Yes                                    | Monitor request rate during load tests; compare to baseline to assess system scalability.                                   |

4. **Set Visualization Type**
   - Choose the visualization type (e.g., **Time series**, **Gauge**, **Bar gauge**, **Table**) from the options above the query editor.

5. **Customize Panel**
   - Set the **Panel title** (e.g., "Queue Size for Flash Sale").
   - (Optional) Set the **Legend** field for custom labels (e.g., `{{service}}` or `{{eventId}}`).
   - Adjust units, thresholds, and display options as needed in the right-hand panel settings.

6. **Save the Panel**
   - Click **"Apply"** (top right) to add the panel to your dashboard.
   - Click **"Save dashboard"** (disk icon) to persist your changes.

7. **View and Interact**
   - Your new panel will now display real-time and historical data based on your query.
   - Use the time range selector (top right) to adjust the period shown.
   - Hover over data points for details, and use the legend to filter series.

**Note:**
- If you are using a different Grafana version, menu names or icons may vary slightly, but the workflow is similar.
- For more help, see the [Grafana documentation](https://grafana.com/docs/grafana/latest/).

## 🧭 Quick Navigation

- For new developers: Start with [QUICK_START_GUIDE.md](./QUICK_START_GUIDE.md)
- For production: See [AWS_EC2_DEPLOYMENT_GUIDE.md](./AWS_EC2_DEPLOYMENT_GUIDE.md) and [SSL_SETUP_GUIDE.md](./SSL_SETUP_GUIDE.md)
- For high-traffic events: See [QUEUE_IT_INTEGRATION_GUIDE.md](./QUEUE_IT_INTEGRATION_GUIDE.md)
- For troubleshooting: See [TROUBLESHOOTING_GUIDE.md](./TROUBLESHOOTING_GUIDE.md) and [PORT_SETUP_GUIDE.md](./PORT_SETUP_GUIDE.md)

## 🛠️ Environment-Specific Configurations

- **Development:** Local Docker, hot reloading, debug logging, Queue-it bypass enabled
- **Staging:** Production-like config, SSL, monitoring, load testing
- **Production:** Full SSL, Queue-it active, monitoring, backups

## 📚 References
- [Queue-It Integration Guide](./QUEUE_IT_INTEGRATION_GUIDE.md)
- [Grafana Documentation](https://grafana.com/docs/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Queue-It Official Docs](https://queue-it.com/docs)