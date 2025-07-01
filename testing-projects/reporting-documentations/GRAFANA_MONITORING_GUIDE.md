# Grafana Monitoring Guide for Queue-It Testing

## Table of Contents
- [1. Available Dashboards](#1-available-dashboards)
- [2. Dashboard Setup](#2-dashboard-setup)
- [3. Key Metrics Explained](#3-key-metrics-explained)
- [4. Monitoring During Testing](#4-monitoring-during-testing)
- [5. Dashboard Navigation](#5-dashboard-navigation)
- [6. Alerting Configurations](#6-alerting-configurations)
- [7. Troubleshooting](#7-troubleshooting)
- [8. Best Practices](#8-best-practices)
- [9. Advanced Features](#9-advanced-features)

---

## 🎯 Overview

This guide provides comprehensive instructions for monitoring Queue-it integration testing using Grafana dashboards. It covers dashboard setup, metric collection, performance analysis, and troubleshooting.

## 1. Available Dashboards

| Dashboard Name                      | Features                                                                 | Use Cases                                 | Key Metrics                        | Primary Panels                      |
|-------------------------------------|--------------------------------------------------------------------------|-------------------------------------------|-------------------------------------|-------------------------------------|
| Queue-It API Performance Dashboard  | API response times, throughput, error rates                              | API tuning, error tracking, bottlenecks   | Response time, error rate, success  | 95th Percentile Latency, Error Rate |
| Queue-It Frontend Metrics Dashboard | Frontend queue interactions, user experience, overlay/indicator          | UX optimization, frontend debugging       | Queue size, wait time, overlay errors | Queue Size, Wait Time, Overlay Errors |
| Queue-It Load Testing Dashboard     | Load/stress, concurrent users, resource utilization, scalability         | Capacity planning, stress test analysis   | Max latency, throughput, users      | Max Latency, Users Served           |

## 🚀 Dashboard Setup

| Step | Action | Details |
|------|--------|---------|
| 1    | Access Grafana | Open browser → `http://localhost:3000` → Login with `admin`/`admin` |
| 2    | Import Dashboard | Click **+** → **Import** → Upload JSON from `config/grafana_dashboards/` |
| 3    | Configure Data Source | Ensure Prometheus is set to `http://prometheus:9090` |
| 4    | Set Variables | Go to Dashboard Settings → Variables → Set time range, refresh interval |
| 5    | Configure Panels | Ensure [Primary Panels](#available-dashboards) are present |
| 6    | Save Dashboard | Click Save to persist changes |

## 📈 Key Metrics Explained

| Metric                | Description                                 | Typical Threshold | Related Panel/Dashboard                |
|----------------------|---------------------------------------------|-------------------|----------------------------------------|
| 95th % Latency       | High-end response time                      | < 1s             | [95th Percentile Latency](#available-dashboards) |
| API Error Rate       | Non-2xx errors per second                   | < 1%              | [Error Rate](#available-dashboards)          |
| Queue Size           | Active users in queue                       | < 1000 (event)    | [Queue Size](#available-dashboards)             |
| Users Served         | Users exited queue                          | > 95% (event)     | [Users Served](#available-dashboards)           |
| Overlay Errors       | Frontend overlay display issues             | 0                 | [Overlay Errors](#available-dashboards)         |
| Max Latency          | Max observed latency                        | < 2s              | [Max Latency](#available-dashboards)            |
| Token Failures       | Token validation failures                   | 0                 | [Token Failures](#available-dashboards)         |

## 🔍 Monitoring During Testing

| Activity                | Dashboard(s)                        | Metric/Panel                | What to Watch For                |
|-------------------------|-------------------------------------|-----------------------------|----------------------------------|
| Run Functional Tests    | API Perf, Frontend, Load Testing    | Users Served, Error Rate    | Success > 95%, Errors < 1%       |
| Run Load Tests          | Load Testing                        | Max Latency, Users Served   | Max Latency < 2s, High throughput|
| Monitor Queue Events    | Frontend, API Perf                  | Queue Size, Wait Time       | Queue size, wait time trends     |
| Debug Frontend Issues   | Frontend Metrics                    | Overlay Errors              | Should be zero                   |
| API Endpoint Analysis   | API Performance                     | Error Rate, Request Rate    | Spikes/drops, error patterns     |

## 📊 Dashboard Navigation

| Action                | How-To Steps |
|-----------------------|--------------|
| Access Grafana        | Open browser → `http://localhost:3000` → Login |
| Find Dashboards       | Click "Dashboards" in sidebar → Browse or Search |
| Select Dashboard      | Click dashboard name from [Available Dashboards](#available-dashboards) |
| Change Time Range     | Use time picker (top right) |
| Filter Metrics        | Use panel legend or query editor |
| Export Data           | Click panel title → More → Export |
| Save Custom View      | Click Save (disk icon) |

## 🚨 Alerting Configuration

| Alert Name           | Dashboard                        | Metric/Panel                | Threshold/Condition | Action on Trigger |
|----------------------|-----------------------------------|-----------------------------|---------------------|-------------------|
| High Latency Alert   | API Perf, Load Testing            | 95th % Latency, Max Latency | > 1s, > 2s          | Email/Slack alert |
| High Error Rate      | API Performance                   | Error Rate                  | > 1%                | Email/Slack alert |
| Overlay Error Alert  | Frontend Metrics                  | Overlay Errors              | > 0                 | Email/Slack alert |
| Token Failure Alert  | API Performance                   | Token Failures              | > 0                 | Email/Slack alert |
| Low Users Served     | All                               | Users Served                | < 95%               | Email/Slack alert |

## 🔧 Troubleshooting

| Issue                | Symptoms                        | Steps to Resolve                                         | Related Dashboard/Panel         |
|----------------------|---------------------------------|----------------------------------------------------------|---------------------------------|
| No Data Showing      | Blank panels, no metrics        | 1. Run tests 2. Check API Gateway 3. Check Prometheus    | All                             |
| Dashboard Not Loading| 404/error, login fails          | 1. Check Grafana running 2. Check credentials 3. Restart | All                             |
| Metrics Not Updating | Stale data, no live updates     | 1. Check Prometheus 2. Check scrape interval 3. Restart  | All                             |
| High Error Rate      | Red alerts, error spikes        | 1. Check API logs 2. Debug endpoints 3. Fix/test         | [Error Rate](#key-metrics-explained)                  |
| Overlay Errors       | UI issues, missing overlays     | 1. Check frontend logs 2. Debug overlay code             | [Overlay Errors](#key-metrics-explained)        |
| Token Failures       | Users stuck, auth errors        | 1. Check token config 2. Debug backend                   | [Token Failures](#key-metrics-explained)        |

## 📋 Best Practices

| Practice                  | Description |
|---------------------------|-------------|
| Regular Monitoring        | Check dashboards daily, monitor trends, set up alerts |
| Performance Optimization  | Use time filters, focus on key metrics, export data   |
| Data Analysis             | Compare over time, identify bottlenecks, plan capacity|
| Mobile Access             | Use mobile browser, touch-friendly controls           |
| Save Custom Views         | Save dashboard states for team sharing                |

## 🛠️ Advanced Features

| Feature                  | Description | How-To |
|--------------------------|-------------|--------|
| Drilldown to Logs        | Click on graph spikes to view logs (requires Loki) | Enable Loki/Promtail, link metrics to logs |
| Log Filtering            | Filter logs by service, level, message             | Use Grafana Explore/Logs tab              |
| Alert Annotations        | Show alerts as graph annotations                   | Configure alert rules in Prometheus/Grafana|
| Custom Panel Creation    | Add panels for new metrics or events               | Use query editor, see [Key Metrics](#key-metrics-explained) |
| Dashboard Export/Import  | Share dashboards as JSON                           | Use dashboard settings menu                |

**For more details, see [Key Metrics Explained](#key-metrics-explained) and [Available Dashboards](#available-dashboards).**

## 📋 Consolidated Dashboard Reference Table

| Dashboard Name                      | Type        | Name/Panel/Metric/KPI/Test         | Description/Use Case                                                      | Value/Threshold/Default | Related Panel (if applicable)         | Steps/Expected Result/Impact                                  |
|-------------------------------------|------------|------------------------------------|---------------------------------------------------------------------------|-------------------------|---------------------------------------|--------------------------------------------------------------|
| Queue-It Comprehensive Dashboard    | Feature    | All-in-one overview                | Full system health, SLA, event analysis                                   | -                       | -                                     | Use for overall monitoring                                   |
| Queue-It Comprehensive Dashboard    | Metric     | 95th % Latency                     | High-end response time                                                    | < 1s                    | 95th Percentile Latency               | SLA, performance monitoring                                  |
| Queue-It Comprehensive Dashboard    | Panel      | 95th Percentile Latency            | SLA, performance monitoring                                               | -                       | 95th Percentile Latency               | Spikes may indicate bottlenecks                              |
| Queue-It Comprehensive Dashboard    | Metric     | Queue Size                         | Active users in queue                                                     | < 1000 (event)          | Queue Size                            | Monitor event demand                                        |
| Queue-It Comprehensive Dashboard    | Panel      | Queue Size                         | Demand, event monitoring                                                  | -                       | Queue Size                            | High values = high demand                                   |
| Queue-It Comprehensive Dashboard    | Metric     | Users Served                       | Users exited queue                                                        | > 95% (event)           | Users Served                          | Event success, throughput                                   |
| Queue-It Comprehensive Dashboard    | Panel      | Users Served                       | Event success, throughput                                                 | -                       | Users Served                          | High = good event flow                                      |
| Queue-It Comprehensive Dashboard    | KPI        | API Response Time                  | 95th Percentile Latency                                                   | < 1s                    | 95th Percentile Latency               | SLA compliance                                             |
| Queue-It Comprehensive Dashboard    | KPI        | Users Served                       | Users exited queue                                                        | > 95%                   | Users Served                          | Event success                                              |
| Queue-It Comprehensive Dashboard    | Config     | Time Range                         | Controls data window, affects trends                                      | Last 1 hour             | All                                   | Set for trend analysis                                     |
| Queue-It Comprehensive Dashboard    | Config     | Auto-refresh                       | Frequency of live updates                                                 | 30 seconds              | All                                   | Set for real-time data                                     |
| Queue-It Comprehensive Dashboard    | Config     | Data Source                        | Source of metrics                                                         | Prometheus              | All                                   | Ensure correct source                                      |
| Queue-It Comprehensive Dashboard    | Config     | Data Retention                     | How long historical data is kept                                          | 200 hours               | All                                   | Set for historical analysis                                |
| Queue-It Comprehensive Dashboard    | Config     | Alerting                           | Notifies on threshold breaches                                            | On (critical)           | All                                   | Set for proactive monitoring                               |
| Queue-It Comprehensive Dashboard    | Test       | Functional Test                    | Success Rate                                                              | > 95%                   | Users Served                          | Run ./test.sh, check panel                                 |
| Queue-It Comprehensive Dashboard    | Troubleshooting | No Data Showing                  | Blank panels, no metrics                                                  | -                       | All                                   | Run tests, check API Gateway, Prometheus                   |
| Queue-It API Performance Dashboard  | Feature    | Endpoint-level metrics             | API tuning, error tracking, bottlenecks                                   | -                       | -                                     | Use for API optimization                                   |
| Queue-It API Performance Dashboard  | Metric     | API Error Rate                     | Non-2xx errors per second                                                 | < 1%                    | API Error Rate                        | Error tracking, debugging                                  |
| Queue-It API Performance Dashboard  | Panel      | API Error Rate                     | Error tracking, debugging                                                 | -                       | API Error Rate                        | Persistent errors = investigate                            |
| Queue-It API Performance Dashboard  | Metric     | API Success Rate                   | Successful API responses                                                  | > 99%                   | API Error Rate                        | High = healthy API                                        |
| Queue-It API Performance Dashboard  | Panel      | Request Rate                       | API throughput                                                            | -                       | Request Rate                          | Drops = possible outage                                    |
| Queue-It API Performance Dashboard  | KPI        | API Success Rate                   | Successful API responses                                                  | > 99%                   | API Error Rate                        | Track for reliability                                     |
| Queue-It API Performance Dashboard  | KPI        | API Error Rate                     | Non-2xx errors per second                                                 | < 1%                    | API Error Rate                        | Track for reliability                                     |
| Queue-It API Performance Dashboard  | Config     | Time Range                         | Controls data window, affects trends                                      | Last 1 hour             | All                                   | Set for trend analysis                                     |
| Queue-It API Performance Dashboard  | Config     | Auto-refresh                       | Frequency of live updates                                                 | 30 seconds              | All                                   | Set for real-time data                                     |
| Queue-It API Performance Dashboard  | Test       | API Test                           | Error Rate                                                                | < 1%                    | API Error Rate                        | Run API tests, check panel                                 |
| Queue-It API Performance Dashboard  | Troubleshooting | High Error Rate                  | Red alerts, error spikes                                                  | -                       | API Error Rate                        | Check API logs, debug endpoints                            |
| Queue-It Frontend Metrics Dashboard | Feature    | User experience, overlay/indicator | UX optimization, frontend debugging                                       | -                       | -                                     | Use for frontend health                                    |
| Queue-It Frontend Metrics Dashboard | Metric     | Overlay Errors                     | Frontend overlay display issues                                           | 0                       | Overlay Errors                        | UX, frontend debugging                                     |
| Queue-It Frontend Metrics Dashboard | Panel      | Overlay Errors                     | UX, frontend debugging                                                    | -                       | Overlay Errors                        | Persistent errors = UI bug                                 |
| Queue-It Frontend Metrics Dashboard | Metric     | Queue Wait Time                    | Average time users wait in queue                                          | < 2 min                 | Avg Wait Time                         | UX, performance                                           |
| Queue-It Frontend Metrics Dashboard | Panel      | Avg Wait Time                      | UX, performance                                                           | -                       | Avg Wait Time                         | High = user frustration                                   |
| Queue-It Frontend Metrics Dashboard | KPI        | Queue Wait Time                    | Average time users wait in queue                                          | < 2 min                 | Avg Wait Time                         | Track for user experience                                 |
| Queue-It Frontend Metrics Dashboard | KPI        | Overlay Error Rate                 | Frontend overlay display issues                                           | 0                       | Overlay Errors                        | Track for UI health                                       |
| Queue-It Frontend Metrics Dashboard | Config     | Time Range                         | Controls data window, affects trends                                      | Last 1 hour             | All                                   | Set for trend analysis                                     |
| Queue-It Frontend Metrics Dashboard | Config     | Auto-refresh                       | Frequency of live updates                                                 | 30 seconds              | All                                   | Set for real-time data                                     |
| Queue-It Frontend Metrics Dashboard | Test       | Frontend Test                      | Overlay Errors                                                            | 0                       | Overlay Errors                        | Run frontend tests, check panel                            |
| Queue-It Frontend Metrics Dashboard | Troubleshooting | Overlay Errors                   | UI issues, missing overlays                                               | -                       | Overlay Errors                        | Check frontend logs, debug overlay code                    |
| Queue-It Load Testing Dashboard     | Feature    | Load/stress, concurrent users      | Capacity planning, stress test analysis                                   | -                       | -                                     | Use for scalability analysis                               |
| Queue-It Load Testing Dashboard     | Metric     | Max Latency                        | Max observed latency                                                      | < 2s                    | Max Latency                           | Stress/capacity analysis                                   |
| Queue-It Load Testing Dashboard     | Panel      | Max Latency                        | Stress/capacity analysis                                                  | -                       | Max Latency                           | Spikes = scaling issue                                    |
| Queue-It Load Testing Dashboard     | Metric     | Users Served                       | Users exited queue                                                        | > 95%                   | Users Served                          | Event success, throughput                                   |
| Queue-It Load Testing Dashboard     | Panel      | Users Served                       | Event success, throughput                                                 | -                       | Users Served                          | High = good event flow                                      |
| Queue-It Load Testing Dashboard     | KPI        | Max Latency (Load)                 | Max observed latency                                                      | < 2s                    | Max Latency                           | Track for scalability                                      |
| Queue-It Load Testing Dashboard     | KPI        | Users Served                       | Users exited queue                                                        | > 95%                   | Users Served                          | Track for event success                                    |
| Queue-It Load Testing Dashboard     | Config     | Time Range                         | Controls data window, affects trends                                      | Last 1 hour             | All                                   | Set for trend analysis                                     |
| Queue-It Load Testing Dashboard     | Config     | Auto-refresh                       | Frequency of live updates                                                 | 30 seconds              | All                                   | Set for real-time data                                     |
| Queue-It Load Testing Dashboard     | Test       | Load Test                          | Max Latency                                                               | < 2s                    | Max Latency                           | Run load tests, check panel                                 |
| Queue-It Load Testing Dashboard     | Troubleshooting | Metrics Not Updating              | Stale data, no live updates                                               | -                       | All                                   | Check Prometheus, scrape interval, restart                 |

---

**🎉 Your Queue-It testing reports are now available in Grafana!**

1. Open `http://localhost:3000`
2. Login: `admin` / `admin123`
3. Select a Queue-It dashboard
4. Run tests to see live metrics 

---

## 📞 Support

### Getting Help
1. **Check Logs**: Review Grafana and Prometheus logs
2. **Documentation**: Refer to Grafana and Prometheus docs
3. **Community**: Use Grafana community forums
4. **Metrics**: Verify metric collection with Prometheus UI

### Useful Commands
```bash
# Check Prometheus targets
curl http://localhost:9090/api/v1/targets

# Query metrics directly
curl "http://localhost:9090/api/v1/query?query=queueit_api_requests_total"

# Check Grafana health
curl http://localhost:3000/api/health
```

---

**Last Updated**: January 2025  
**Version**: 1.0.0 