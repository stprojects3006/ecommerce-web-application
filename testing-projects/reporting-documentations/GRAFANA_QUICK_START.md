# 📊 Queue-It Testing Reports - Grafana Quick Start

## Table of Contents
- [Quick Access](#quick-access)
- [Quick Commands](#quick-commands)
- [Key Metrics & Panel Configuration Table](#key-metrics--panel-configuration-table)
- [Overview & Testing Objectives](#overview--testing-objectives)
- [Monitoring Activities & Metrics by Testing Objective](#monitoring-activities--metrics-by-testing-objective)
- [🛠️ Detailed Steps: Dashboard & Panel Setup](#️-detailed-steps-dashboard--panel-setup)
- [Consolidated Dashboard Reference Table](#consolidated-dashboard-reference-table)
- [Advanced Monitoring: Drilldown, Logging, and Alerting](#advanced-monitoring-drilldown-logging-and-alerting)
- [References & Resources](#references--resources)

## 🚀 Quick Access

**Grafana Dashboard URL:**
```
http://localhost:3000
```
**Login Credentials:**
- **Username:** `admin`
- **Password:** `admin123`

---

## 🔗 Quick Commands

| Action           | Command                          |
|------------------|----------------------------------|
| Start Grafana    | ./setup_grafana_dashboard.sh     |
| View Reports     | ./show_grafana_reports.sh        |
| Run Tests        | ./test.sh                        |
| Check Health     | curl http://localhost:8081/api/queueit/health |

---

## Overview & Testing Objectives

This guide details how to monitor and analyze the Queue-It integration using Grafana dashboards, aligned with the testing objectives from the functional testing project:
- **Functional Validation**: Ensure Queue-It works as intended
- **Performance Testing**: Monitor system under load
- **Integration Testing**: Validate end-to-end flows
- **Error Handling**: Detect and analyze failures
- **Monitoring**: Real-time health and performance

## Monitoring Activities & Metrics by Testing Objective

### 1. Functional Validation
- **Service Health**: Uptime, downtime, restarts
- **Queue Status**: Active queue size, users served
- **Primary Panels**: Service Health (Up/Down), Flash Sale Users Served

### 2. Performance Testing
- **Request Latency**: 95th percentile, max latency
- **Throughput**: Request rate, users served/min
- **Queue Wait Time**: Average, SLA compliance
- **Primary Panels**: API Gateway 95th Percentile Latency, Flash Sale Avg Wait Time, API Gateway Max Latency (1h)

### 3. Integration Testing
- **End-to-End Flow**: Queue entry, token validation, users served
- **Panel Examples**: API Gateway Token Failures, Checkout Time to First Served

### 4. Error Handling
- **Error Rates**: API errors, frontend overlay/token errors, abandonment
- **Panel Examples**: API Gateway Error Rate, Frontend Overlay Errors, Black Friday Abandonment Rate

### 5. Monitoring (Real-Time & Advanced)
- **Dashboards**: Comprehensive, API Performance, Frontend Metrics, Load Testing
- **Drilldown**: Use Loki/Promtail for log aggregation and root cause analysis
- **Alerting**: Set up Prometheus/Grafana alerts for high latency, error rates, downtime
---

## Key Metrics & Panel Configuration Table

| Dashboard                        | Event Name           | Service Type      | PromQL Query                                                                                       | Visualization Type | Recommended Panel Name         | X-Axis Label      | Y-Axis Label         | Primary Panel (Default for Dashboard?) | Steps for How to Interpret the Dashboard Data                                                                                 | Testing Objective |
|-----------------------------------|----------------------|-------------------|----------------------------------------------------------------------------------------------------|--------------------|-------------------------------|-------------------|----------------------|----------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|-------------------|
| queueit-comprehensive-dashboard   | All                  | api-gateway       | histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{service="api-gateway"}[5m])) by (le)) | Time series        | API Gateway 95th Percentile Latency | Time         | Latency (seconds)    | Yes                                   | Focuses on API Gateway latency; spikes may indicate gateway bottlenecks.                                                     | Performance Testing |
| queueit-comprehensive-dashboard   | flash-sale-2024      | All               | max_over_time(queueit_active_queue_size{eventId="flash-sale-2024"}[24h])                         | Gauge              | Max Flash Sale Queue Length    | Time              | Max Users in Queue   | No                                     | Shows the largest queue size for the flash sale event in the last 24h.                                                      | Functional Validation |
| queueit-comprehensive-dashboard   | black-friday         | All               | sum(rate(queueit_users_abandoned_total{eventId="black-friday"}[5m]))                             | Bar chart          | Black Friday Abandonment Rate  | Time              | Users Abandoned      | No                                     | High abandonment for Black Friday may indicate excessive wait times or technical issues.                                     | Functional Validation |
| queueit-comprehensive-dashboard   | flash-sale-2024      | All               | (sum(rate(queueit_users_served_total{eventId="flash-sale-2024"}[1m])) by (eventId))              | Time series        | Flash Sale Queue Throughput    | Time              | Users Served/min     | No                                     | Measures how many users are exiting the queue per minute; low throughput may indicate bottlenecks.                          | Functional Validation |
| queueit-comprehensive-dashboard   | All                  | All               | up                                                                                                | State timeline     | Service Health (Up/Down)      | Time              | Up (1) / Down (0)    | Yes                                   | Gaps or drops to 0 indicate downtime; correlate with other panels to diagnose impact.                                       | Functional Validation |
| queueit-api-performance          | All                  | product-service   | sum(rate(http_server_requests_seconds_count{service="product-service"}[5m]))                      | Time series        | Product Service Request Rate   | Time              | Requests per second  | No                                     | Monitors product-service traffic; drops may indicate product catalog issues.                                                 | Performance Testing |
| queueit-api-performance          | All                  | api-gateway       | sum(rate(http_server_requests_seconds_count{service="api-gateway",status!~"2.."}[5m]))          | Bar gauge          | API Gateway Error Rate         | Time              | Errors per second    | Yes                                   | Tracks non-2xx errors for API Gateway; persistent errors require investigation.                                              | Performance Testing |
| queueit-api-performance          | All                  | api-gateway       | sum(rate(http_server_requests_seconds_count{service="api-gateway"}[5m]))                          | Time series        | API Gateway Request Rate       | Time              | Requests per second  | Yes                                   | Drops may indicate outages; spikes may indicate traffic surges.                                                             | Performance Testing |
| queueit-api-performance          | All                  | api-gateway       | sum(rate(http_server_requests_seconds_count{service="api-gateway",status!~"2.."}[5m])) by (endpoint) | Table         | API Error Rate by Endpoint    | Endpoint          | Errors per second    | No                                     | Pinpoint which API endpoints are failing most; focus troubleshooting on high-error endpoints.                               | Performance Testing |
| queueit-api-performance          | All                  | api-gateway       | sum(rate(queueit_token_validation_failures_total{service="api-gateway"}[5m]))                    | Bar chart          | API Gateway Token Failures     | Time              | Failures per second  | No                                     | Token validation failures in API Gateway; may indicate integration or security issues.                                      | Performance Testing |
| queueit-frontend-metrics         | checkout-protection  | All               | avg_over_time(queueit_user_wait_time_seconds{eventId="checkout-protection"}[5m])                 | Time series        | Checkout Wait Time             | Time              | Wait Time (seconds)  | Yes                                   | Measures average wait time for checkout protection event; high values may impact conversions.                                | Functional Validation |
| queueit-frontend-metrics         | flash-sale-2024      | All               | queueit_active_queue_size{eventId="flash-sale-2024"}                                             | Gauge              | Flash Sale Queue Size          | Time              | Users in Queue       | Yes                                   | Real-time queue size for flash sale; use to monitor event demand.                                                           | Functional Validation |
| queueit-frontend-metrics         | All                  | frontend          | sum(rate(queueit_frontend_errors_total{service="frontend",errorType="overlay_display"}[5m]))    | Bar chart          | Frontend Overlay Errors        | Time              | Errors per second    | No                                     | Tracks overlay display errors in the frontend service; high rates may indicate UI bugs.                                      | Functional Validation |
| queueit-frontend-metrics         | flash-sale-2024      | All               | (sum(rate(queueit_users_served_total{eventId="flash-sale-2024",waitTimeSeconds<=120}[5m])) / sum(rate(queueit_users_served_total{eventId="flash-sale-2024"}[5m])) * 100 | Gauge | Flash Sale % Served <2min | Time | Percentage (%) | No | Shows percent of flash sale users served within 2 minutes; low values may require queue tuning.                             | Functional Validation |
| queueit-frontend-metrics         | All                  | frontend          | sum(rate(queueit_frontend_errors_total{service="frontend",errorType="token_expired"}[5m]))      | Bar chart          | Frontend Token Expiry Errors   | Time              | Errors per second    | No                                     | Tracks token expiry errors in the frontend; high rates may indicate session or clock issues.                                | Functional Validation |
| queueit-frontend-metrics         | All                  | frontend          | sum(rate(queueit_frontend_errors_total{service="frontend"}[5m])) by (errorType)                  | Bar chart          | Frontend Error Rate            | Error Type        | Errors per second    | No                                     | Identify frequent frontend error types; high rates may indicate UI or integration issues.                                   | Functional Validation |
| queueit-frontend-metrics         | flash-sale-2024      | All               | avg_over_time(queueit_user_wait_time_seconds{eventId="flash-sale-2024"}[5m])                     | Time series        | Flash Sale Avg Wait Time       | Time              | Wait Time (seconds)  | Yes                                   | Average wait time for flash sale event; monitor for user experience and adjust queue settings if needed.                    | Functional Validation |
| queueit-frontend-metrics         | All                  | All               | (sum(rate(queueit_users_served_total{waitTimeSeconds<=120}[5m])) / sum(rate(queueit_users_served_total[5m])) * 100 | Gauge | % Users Served Within SLA (2min) | Time | Percentage (%) | No | Shows what percent of users are served within 2 minutes; low values may require queue tuning.                               | Functional Validation |
| queueit-load-testing             | black-friday         | All               | sum(increase(queueit_users_enqueued_total{eventId="black-friday"}[1h]))                          | Bar chart          | Black Friday Entrances (1h)    | Time              | Users Enqueued       | Yes                                   | Number of users who joined the Black Friday queue in the last hour.                                                         | Functional Validation |
| queueit-load-testing             | flash-sale-2024      | api-gateway       | max_over_time(http_server_requests_seconds_max{service="api-gateway"}[1h])                        | Time series        | API Gateway Max Latency (1h)   | Time              | Max Latency (s)      | Yes                                   | Maximum observed latency for API Gateway during load test; spikes may indicate scaling issues.                              | Performance Testing |
| queueit-load-testing             | checkout-protection  | All               | min_over_time(timestamp(queueit_users_served_total{eventId="checkout-protection"}[5m] > 0)[1h:1m])| Table              | Checkout Time to First Served  | Event ID          | Timestamp            | No                                     | When the first user was served for checkout protection; long delays may indicate slow queue start.                          | Functional Validation |
| queueit-load-testing             | flash-sale-2024      | All               | sum(rate(queueit_users_served_total{eventId="flash-sale-2024"}[5m]))                             | Time series        | Flash Sale Users Served        | Time              | Users Served         | Yes                                   | Number of users who exited the flash sale queue; helps measure event throughput.                                            | Functional Validation |
| queueit-load-testing             | black-friday         | All               | sum(rate(queueit_users_served_total{eventId="black-friday"}[5m]))                                | Time series        | Black Friday Users Served      | Time              | Users Served         | Yes                                   | Number of users who exited the Black Friday queue; compare to flash sale for event analysis.                                | Functional Validation |
| queueit-load-testing             | All                  | api-gateway       | sum(rate(http_server_requests_seconds_count{service="api-gateway"}[1m]))                         | Time series        | API Gateway Request Rate (Load Test) | Time         | Requests per second  | Yes                                   | Monitor request rate during load tests; compare to baseline to assess system scalability.                                   | Performance Testing |

---

## 🛠️ Detailed Steps: Dashboard & Panel Setup

### 1. Import a Dashboard
1. Open Grafana and log in (see Quick Access above).
2. Click the **+** icon in the left sidebar, then select **Import**.
3. Upload the relevant JSON file (markd as primary) from `config/grafana_dashboards/` (see [Consolidated Dashboard Reference Table](#consolidated-dashboard-reference-table)).
4. Select your Prometheus data source and click **Import**.

### 2. Add a New Panel
1. Open the imported dashboard.
2. Click **Add panel** (top right) or the **+** icon → **Add new panel**.
3. In the Query section, select **Prometheus** as the data source.
4. Copy the PromQL query for your desired metric/panel from the [Consolidated Dashboard Reference Table](#consolidated-dashboard-reference-table) (see the "Steps/Expected Result/Impact" column for guidance).
5. Paste the query into the Query field.

### 3. Configure Panel Settings
1. Set the **Visualization Type** (e.g., Time series, Gauge, Bar chart) as recommended in the table.
2. Set the **Panel Name** (see "Name/Panel/Metric/KPI/Test" in the table).
3. Set the **X-Axis** and **Y-Axis** labels as per the table.
4. Set thresholds, units, and legend as needed (see "Value/Threshold/Default").
5. (Optional) Add alert rules or annotations as described in the table.

### 4. Save and View
1. Click **Apply** to add the panel to your dashboard.
2. Click **Save dashboard** (disk icon) to persist your changes.
3. Use the time picker and refresh controls to view real-time and historical data.

### 5. Cross-Reference for Details
- For queries, settings, and expected results for each panel/metric/configuration, see the [Consolidated Dashboard Reference Table](#consolidated-dashboard-reference-table) below.
- For troubleshooting, see the Troubleshooting rows in the same table.

---

## 📋 Consolidated Dashboard Reference Table

| Dashboard Name                      | Type        | Name/Panel/Metric/KPI/Test         | Description/Use Case                                                      | Value/Threshold/Default | Related Panel (if applicable)         | Steps/Expected Result/Impact                                  | Testing Objective |
|-------------------------------------|------------|------------------------------------|---------------------------------------------------------------------------|-------------------------|---------------------------------------|--------------------------------------------------------------|-------------------|
| Queue-It Comprehensive Dashboard    | Feature    | All-in-one overview                | Full system health, SLA, event analysis                                   | -                       | -                                     | Use for overall monitoring                                   | Monitoring (Real-Time & Advanced) |
| Queue-It Comprehensive Dashboard    | Metric     | 95th % Latency                     | High-end response time                                                    | < 1s                    | 95th Percentile Latency               | SLA, performance monitoring                                  | Performance Testing |
| Queue-It Comprehensive Dashboard    | Panel      | 95th Percentile Latency            | SLA, performance monitoring                                               | -                       | 95th Percentile Latency               | Spikes may indicate bottlenecks                              | Performance Testing |
| Queue-It Comprehensive Dashboard    | Metric     | Queue Size                         | Active users in queue                                                     | < 1000 (event)          | Queue Size                            | Monitor event demand                                        | Functional Validation |
| Queue-It Comprehensive Dashboard    | Panel      | Queue Size                         | Demand, event monitoring                                                  | -                       | Queue Size                            | High values = high demand                                   | Functional Validation |
| Queue-It Comprehensive Dashboard    | Metric     | Users Served                       | Users exited queue                                                        | > 95% (event)           | Users Served                          | Event success, throughput                                   | Functional Validation |
| Queue-It Comprehensive Dashboard    | Panel      | Users Served                       | Event success, throughput                                                 | -                       | Users Served                          | High = good event flow                                      | Functional Validation |
| Queue-It Comprehensive Dashboard    | KPI        | API Response Time                  | 95th Percentile Latency                                                   | < 1s                    | 95th Percentile Latency               | SLA compliance                                             | Performance Testing |
| Queue-It Comprehensive Dashboard    | KPI        | Users Served                       | Users exited queue                                                        | > 95%                   | Users Served                          | Event success                                              | Functional Validation |
| Queue-It Comprehensive Dashboard    | Config     | Time Range                         | Controls data window, affects trends                                      | Last 1 hour             | All                                   | Set for trend analysis                                     | Monitoring (Real-Time & Advanced) |
| Queue-It Comprehensive Dashboard    | Config     | Auto-refresh                       | Frequency of live updates                                                 | 30 seconds              | All                                   | Set for real-time data                                     | Monitoring (Real-Time & Advanced) |
| Queue-It Comprehensive Dashboard    | Config     | Data Source                        | Source of metrics                                                         | Prometheus              | All                                   | Ensure correct source                                      | Monitoring (Real-Time & Advanced) |
| Queue-It Comprehensive Dashboard    | Config     | Data Retention                     | How long historical data is kept                                          | 200 hours               | All                                   | Set for historical analysis                                | Monitoring (Real-Time & Advanced) |
| Queue-It Comprehensive Dashboard    | Config     | Alerting                           | Notifies on threshold breaches                                            | On (critical)           | All                                   | Set for proactive monitoring                               | Monitoring (Real-Time & Advanced) |
| Queue-It Comprehensive Dashboard    | Test       | Functional Test                    | Success Rate                                                              | > 95%                   | Users Served                          | Run ./test.sh, check panel                                 | Monitoring (Real-Time & Advanced) |
| Queue-It Comprehensive Dashboard    | Troubleshooting | No Data Showing                  | Blank panels, no metrics                                                  | -                       | All                                   | Run tests, check API Gateway, Prometheus                   | Monitoring (Real-Time & Advanced) |
| Queue-It API Performance Dashboard  | Feature    | Endpoint-level metrics             | API tuning, error tracking, bottlenecks                                   | -                       | -                                     | Use for API optimization                                   | Performance Testing |
| Queue-It API Performance Dashboard  | Metric     | API Error Rate                     | Non-2xx errors per second                                                 | < 1%                    | API Error Rate                        | Error tracking, debugging                                  | Performance Testing |
| Queue-It API Performance Dashboard  | Panel      | API Error Rate                     | Error tracking, debugging                                                 | -                       | API Error Rate                        | Persistent errors = investigate                            | Performance Testing |
| Queue-It API Performance Dashboard  | Metric     | API Success Rate                   | Successful API responses                                                  | > 99%                   | API Error Rate                        | High = healthy API                                        | Performance Testing |
| Queue-It API Performance Dashboard  | Panel      | Request Rate                       | API throughput                                                            | -                       | Request Rate                          | Drops = possible outage                                    | Performance Testing |
| Queue-It API Performance Dashboard  | KPI        | API Success Rate                   | Successful API responses                                                  | > 99%                   | API Error Rate                        | Track for reliability                                     | Performance Testing |
| Queue-It API Performance Dashboard  | KPI        | API Error Rate                     | Non-2xx errors per second                                                 | < 1%                    | API Error Rate                        | Track for reliability                                     | Performance Testing |
| Queue-It API Performance Dashboard  | Config     | Time Range                         | Controls data window, affects trends                                      | Last 1 hour             | All                                   | Set for trend analysis                                     | Monitoring (Real-Time & Advanced) |
| Queue-It API Performance Dashboard  | Config     | Auto-refresh                       | Frequency of live updates                                                 | 30 seconds              | All                                   | Set for real-time data                                     | Monitoring (Real-Time & Advanced) |
| Queue-It API Performance Dashboard  | Test       | API Test                           | Error Rate                                                                | < 1%                    | API Error Rate                        | Run API tests, check panel                                 | Performance Testing |
| Queue-It API Performance Dashboard  | Troubleshooting | High Error Rate                  | Red alerts, error spikes                                                  | -                       | API Error Rate                        | Check API logs, debug endpoints                            | Performance Testing |
| Queue-It Frontend Metrics Dashboard | Feature    | User experience, overlay/indicator | UX optimization, frontend debugging                                       | -                       | -                                     | Use for frontend health                                    | Functional Validation |
| Queue-It Frontend Metrics Dashboard | Metric     | Overlay Errors                     | Frontend overlay display issues                                           | 0                       | Overlay Errors                        | UX, frontend debugging                                     | Functional Validation |
| Queue-It Frontend Metrics Dashboard | Panel      | Overlay Errors                     | UX, frontend debugging                                                    | -                       | Overlay Errors                        | Persistent errors = UI bug                                 | Functional Validation |
| Queue-It Frontend Metrics Dashboard | Metric     | Queue Wait Time                    | Average time users wait in queue                                          | < 2 min                 | Avg Wait Time                         | UX, performance                                           | Functional Validation |
| Queue-It Frontend Metrics Dashboard | Panel      | Avg Wait Time                      | UX, performance                                                           | -                       | Avg Wait Time                         | High = user frustration                                   | Functional Validation |
| Queue-It Frontend Metrics Dashboard | KPI        | Queue Wait Time                    | Average time users wait in queue                                          | < 2 min                 | Avg Wait Time                         | Track for user experience                                 | Functional Validation |
| Queue-It Frontend Metrics Dashboard | KPI        | Overlay Error Rate                 | Frontend overlay display issues                                           | 0                       | Overlay Errors                        | Track for UI health                                       | Functional Validation |
| Queue-It Frontend Metrics Dashboard | Config     | Time Range                         | Controls data window, affects trends                                      | Last 1 hour             | All                                   | Set for trend analysis                                     | Monitoring (Real-Time & Advanced) |
| Queue-It Frontend Metrics Dashboard | Config     | Auto-refresh                       | Frequency of live updates                                                 | 30 seconds              | All                                   | Set for real-time data                                     | Monitoring (Real-Time & Advanced) |
| Queue-It Frontend Metrics Dashboard | Test       | Frontend Test                      | Overlay Errors                                                            | 0                       | Overlay Errors                        | Run frontend tests, check panel                            | Functional Validation |
| Queue-It Frontend Metrics Dashboard | Troubleshooting | Overlay Errors                   | UI issues, missing overlays                                               | -                       | Overlay Errors                        | Check frontend logs, debug overlay code                    | Functional Validation |
| Queue-It Load Testing Dashboard     | Feature    | Load/stress, concurrent users      | Capacity planning, stress test analysis                                   | -                       | -                                     | Use for scalability analysis                               | Performance Testing |
| Queue-It Load Testing Dashboard     | Metric     | Max Latency                        | Max observed latency                                                      | < 2s                    | Max Latency                           | Stress/capacity analysis                                   | Performance Testing |
| Queue-It Load Testing Dashboard     | Panel      | Max Latency                        | Stress/capacity analysis                                                  | -                       | Max Latency                           | Spikes = scaling issue                                    | Performance Testing |
| Queue-It Load Testing Dashboard     | Metric     | Users Served                       | Users exited queue                                                        | > 95%                   | Users Served                          | Event success, throughput                                   | Functional Validation |
| Queue-It Load Testing Dashboard     | Panel      | Users Served                       | Event success, throughput                                                 | -                       | Users Served                          | High = good event flow                                      | Functional Validation |
| Queue-It Load Testing Dashboard     | KPI        | Max Latency (Load)                 | Max observed latency                                                      | < 2s                    | Max Latency                           | Track for scalability                                      | Performance Testing |
| Queue-It Load Testing Dashboard     | KPI        | Users Served                       | Users exited queue                                                        | > 95%                   | Users Served                          | Track for event success                                    | Functional Validation |
| Queue-It Load Testing Dashboard     | Config     | Time Range                         | Controls data window, affects trends                                      | Last 1 hour             | All                                   | Set for trend analysis                                     | Monitoring (Real-Time & Advanced) |
| Queue-It Load Testing Dashboard     | Config     | Auto-refresh                       | Frequency of live updates                                                 | 30 seconds              | All                                   | Set for real-time data                                     | Monitoring (Real-Time & Advanced) |
| Queue-It Load Testing Dashboard     | Test       | Load Test                          | Max Latency                                                               | < 2s                    | Max Latency                           | Run load tests, check panel                                 | Performance Testing |
| Queue-It Load Testing Dashboard     | Troubleshooting | Metrics Not Updating              | Stale data, no live updates                                               | -                       | All                                   | Check Prometheus, scrape interval, restart                 | Monitoring (Real-Time & Advanced) |

---

**🎉 Your Queue-It testing reports are now available in Grafana!**

1. Open `http://localhost:3000`
2. Login: `admin` / `admin123`
3. Select a Queue-It dashboard
4. Run tests to see live metrics 

---
## Advanced Monitoring: Drilldown, Logging, and Alerting

- **Log Aggregation**: Use Loki/Promtail for collecting and searching logs
- **Link Metrics to Logs**: Enable "Explore" or "Logs" links in panels for root cause analysis
- **Alerting**: Set up Prometheus/Grafana alerts for key metrics (latency, errors, downtime)
- **Example Workflow**:
  - See a spike in latency or downtime in a panel
  - Click to view logs for the relevant service and time window
  - Filter logs by service, error type, or message
  - Investigate and resolve issues quickly

---

## References & Resources
- [Grafana Documentation](https://grafana.com/docs/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Loki Documentation](https://grafana.com/oss/loki/)
- [Queue-It Integration Guide](../technical-notes/QUEUE_IT_INTEGRATION_GUIDE.md)
- [Project Summary & Testing Objectives](../../testing-projects/PROJECT_SUMMARY.md)
