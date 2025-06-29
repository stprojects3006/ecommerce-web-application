# Grafana Monitoring Guide for Queue-it Testing

## 🎯 Overview

This guide provides comprehensive instructions for monitoring Queue-it integration testing using Grafana dashboards. It covers dashboard setup, metric collection, performance analysis, and troubleshooting.

## 📊 Available Dashboards

### 1. Queue-it API Performance Dashboard
**Purpose**: Monitor API response times, throughput, and error rates
**Key Metrics**:
- API Response Time (95th percentile, mean)
- API Throughput (requests per second)
- Error Rate (errors per second)
- Success Rate (percentage)
- Average Response Time

### 2. Queue-it Frontend Metrics Dashboard
**Purpose**: Track frontend queue interactions and user experience
**Key Metrics**:
- Queue Trigger Rate
- Queue Size (users in queue)
- Wait Time (average wait time)
- User Experience Metrics (joins/exits)
- Queue Completion Rate
- Active Queues

### 3. Queue-it Load Testing Dashboard
**Purpose**: Monitor performance under load testing conditions
**Key Metrics**:
- Concurrent Users
- Test Response Times (95th, 50th, 99th percentile)
- Test Success Rate
- Test Throughput
- Error Rate
- Resource Utilization

## 🚀 Dashboard Setup

### Step 1: Access Grafana
1. Open your browser and navigate to: `http://localhost:3000`
2. Login with default credentials:
   - Username: `admin`
   - Password: `admin`

### Step 2: Import Dashboards
1. Click on the **+** icon in the sidebar
2. Select **Import**
3. For each dashboard, click **Upload JSON file** and select the corresponding file:
   - `config/grafana_dashboards/queueit-api-performance.json`
   - `config/grafana_dashboards/queueit-frontend-metrics.json`
   - `config/grafana_dashboards/queueit-load-testing.json`

### Step 3: Configure Data Source
1. Ensure Prometheus data source is configured
2. Go to **Configuration** → **Data Sources**
3. Verify Prometheus is set to: `http://prometheus:9090`

### Step 4: Set Dashboard Variables
1. Open each dashboard
2. Go to **Dashboard Settings** → **Variables**
3. Configure time range: `now-1h` to `now`
4. Set refresh interval to `30s`

## 📈 Key Metrics Explained

### API Performance Metrics

#### Response Time
```promql
rate(queueit_api_response_time_seconds_sum[5m]) / rate(queueit_api_response_time_seconds_count[5m])
```
- **What it measures**: Average response time for API calls
- **Thresholds**: 
  - Green: < 1s
  - Yellow: 1-3s
  - Red: > 3s

#### Throughput
```promql
rate(queueit_api_requests_total[5m])
```
- **What it measures**: Number of API requests per second
- **Expected range**: 10-100 req/s during normal load

#### Error Rate
```promql
rate(queueit_api_errors_total[5m])
```
- **What it measures**: Number of failed API requests per second
- **Target**: < 1% error rate

### Frontend Metrics

#### Queue Size
```promql
queueit_queue_size
```
- **What it measures**: Number of users currently in queue
- **Monitoring**: Watch for sudden spikes or drops

#### Wait Time
```promql
queueit_wait_time_seconds
```
- **What it measures**: Average time users wait in queue
- **Thresholds**:
  - Green: < 30s
  - Yellow: 30-60s
  - Red: > 60s

#### Completion Rate
```promql
rate(queueit_user_exits_total[5m]) / rate(queueit_user_joins_total[5m]) * 100
```
- **What it measures**: Percentage of users who complete the queue
- **Target**: > 95%

### Load Testing Metrics

#### Concurrent Users
```promql
queueit_concurrent_users
```
- **What it measures**: Number of active users during load test
- **Use**: Monitor system behavior under load

#### Test Success Rate
```promql
rate(queueit_test_success_total[5m]) / rate(queueit_test_total[5m]) * 100
```
- **What it measures**: Percentage of successful test executions
- **Target**: > 98%

## 🔍 Monitoring During Testing

### Before Running Tests
1. **Check System Health**:
   - Verify all services are running
   - Check Grafana and Prometheus connectivity
   - Ensure dashboards are loaded

2. **Baseline Metrics**:
   - Record baseline response times
   - Note current queue sizes
   - Document system resource usage

### During Test Execution
1. **Real-time Monitoring**:
   - Watch API response times for degradation
   - Monitor error rates for spikes
   - Track queue sizes and wait times

2. **Key Alerts to Watch**:
   - Response time > 5s
   - Error rate > 5%
   - Queue size > 1000 users
   - Success rate < 95%

### After Test Completion
1. **Performance Analysis**:
   - Compare metrics to baselines
   - Identify performance bottlenecks
   - Document any anomalies

2. **Report Generation**:
   - Export dashboard snapshots
   - Save performance data
   - Generate test summary

## 📊 Dashboard Navigation

### Dashboard Overview
- **Time Range**: Use the time picker to select relevant time periods
- **Refresh Rate**: Set to 30s for real-time monitoring
- **Variables**: Use dashboard variables to filter data

### Panel Interactions
- **Hover**: View detailed metric values
- **Click**: Drill down into specific time ranges
- **Legend**: Toggle visibility of specific metrics

### Export Options
- **Screenshot**: Save dashboard as image
- **JSON Export**: Export dashboard configuration
- **CSV Data**: Export metric data for analysis

## 🚨 Alerting Configuration

### Setting Up Alerts
1. **Create Alert Rules**:
   ```yaml
   groups:
   - name: queueit_alerts
     rules:
     - alert: HighResponseTime
       expr: rate(queueit_api_response_time_seconds_sum[5m]) / rate(queueit_api_response_time_seconds_count[5m]) > 5
       for: 2m
       labels:
         severity: warning
       annotations:
         summary: "High API response time detected"
   ```

2. **Configure Notification Channels**:
   - Email notifications
   - Slack integration
   - PagerDuty alerts

### Recommended Alerts
- **High Response Time**: > 5s for 2 minutes
- **High Error Rate**: > 5% for 1 minute
- **Queue Overflow**: > 1000 users for 5 minutes
- **Low Success Rate**: < 95% for 2 minutes

## 🔧 Troubleshooting

### Common Issues

#### No Data in Dashboards
**Symptoms**: Empty graphs, no metrics displayed
**Solutions**:
1. Check Prometheus data source connection
2. Verify metrics are being collected
3. Check time range settings
4. Restart Prometheus if needed

#### Missing Metrics
**Symptoms**: Some panels show "No data"
**Solutions**:
1. Verify metric names in Prometheus
2. Check if tests are generating metrics
3. Validate PromQL queries
4. Check metric labels

#### Dashboard Loading Slowly
**Symptoms**: Dashboards take long to load
**Solutions**:
1. Reduce time range
2. Increase refresh interval
3. Optimize PromQL queries
4. Check system resources

### Performance Optimization

#### Query Optimization
```promql
# Instead of this (slow):
rate(queueit_api_response_time_seconds_sum[1h]) / rate(queueit_api_response_time_seconds_count[1h])

# Use this (faster):
rate(queueit_api_response_time_seconds_sum[5m]) / rate(queueit_api_response_time_seconds_count[5m])
```

#### Dashboard Optimization
1. **Reduce Panel Count**: Limit to essential metrics
2. **Optimize Refresh Rate**: Use 30s instead of 10s
3. **Use Appropriate Time Ranges**: 1h for real-time, 24h for trends

## 📋 Best Practices

### Dashboard Design
1. **Organize by Function**: Group related metrics together
2. **Use Consistent Colors**: Green for good, yellow for warning, red for critical
3. **Include Thresholds**: Show acceptable ranges
4. **Add Context**: Include descriptions and units

### Monitoring Strategy
1. **Set Baselines**: Establish normal operating ranges
2. **Monitor Trends**: Watch for gradual degradation
3. **Track Anomalies**: Document unusual patterns
4. **Regular Reviews**: Update dashboards based on findings

### Data Retention
1. **Prometheus Retention**: Set to 15-30 days for testing
2. **Backup Important Data**: Export critical metrics
3. **Archive Old Data**: Move historical data to long-term storage

## 🛠️ Advanced Features

### Custom Queries
Create custom PromQL queries for specific analysis:

```promql
# Queue efficiency over time
rate(queueit_user_exits_total[5m]) / rate(queueit_user_joins_total[5m])

# Peak usage times
topk(5, rate(queueit_api_requests_total[1h]))

# Error distribution by endpoint
sum by (endpoint) (rate(queueit_api_errors_total[5m]))
```

### Dashboard Variables
Use variables for dynamic filtering:

```yaml
variables:
  - name: event_id
    query: label_values(queueit_queue_size, event_id)
    refresh: 2
```

### Annotations
Add test events to dashboards:

```javascript
// Add annotation when test starts
grafanaAPI.createAnnotation({
  text: "Load test started",
  tags: ["queueit", "load-test"],
  time: Date.now()
});
```

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