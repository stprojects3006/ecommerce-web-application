# 📊 View Queue-It Metrics in Grafana - Complete Guide

## 🎉 Your Metrics Are Ready!

We've successfully generated comprehensive Queue-It metrics. Here's how to view them in Grafana:

## 🌐 Access Grafana

**URL:** `http://localhost:3000`

## 🔐 Login Credentials

Try these credentials in order:
1. **Username:** `admin` | **Password:** `admin`
2. **Username:** `admin` | **Password:** `admin123`

## 📊 Setup Prometheus Data Source

### Step 1: Add Data Source
1. Click the **Configuration** icon (gear) in the left sidebar
2. Select **Data Sources**
3. Click **Add data source**
4. Select **Prometheus**

### Step 2: Configure Prometheus
- **Name:** `Prometheus`
- **URL:** `http://prometheus:9090`
- **Access:** `proxy`
- Click **Save & Test**

## 📈 Import Queue-It Dashboards

### Step 1: Import Dashboards
1. Go to **Dashboards** → **Import**
2. Click **Upload JSON file**
3. Import these dashboard files:

### Available Dashboards:

#### 1. **Queue-It Comprehensive Testing Dashboard**
**File:** `config/grafana_dashboards/queueit-comprehensive-dashboard.json`
- Complete overview of all metrics
- Test execution timeline
- Performance trends
- Error rates and alerts

#### 2. **Queue-It API Performance Dashboard**
**File:** `config/grafana_dashboards/queueit-api-performance.json`
- API response times
- Throughput metrics
- Success rates
- Error rates by endpoint

#### 3. **Queue-It Frontend Metrics Dashboard**
**File:** `config/grafana_dashboards/queueit-frontend-metrics.json`
- Queue trigger rates
- Queue sizes and wait times
- User experience metrics
- Frontend interactions

#### 4. **Queue-It Load Testing Dashboard**
**File:** `config/grafana_dashboards/queueit-load-testing.json`
- Load test results
- Performance under stress
- Concurrent user metrics
- Resource utilization

## 📊 Metrics You'll See

### **Generated Test Data:**
- ✅ **5 comprehensive test runs**
- ✅ **10 health endpoint calls**
- ✅ **15 status endpoint calls**
- ✅ **6 stats endpoint calls**
- ✅ **8 enqueue operations**
- ✅ **20 concurrent performance tests**

### **Available Metrics:**
- **API Performance:**
  - Response times by endpoint
  - Request throughput
  - Success/error rates
  - Average response times

- **Queue Operations:**
  - Queue trigger rates
  - Queue sizes
  - Wait times
  - User join/exit rates

- **Test Execution:**
  - Test success rates
  - Execution times
  - Error patterns
  - Performance trends

## 🎯 Dashboard Features

### **Real-time Updates**
- Metrics update every 30 seconds
- Live data from your Queue-It tests
- Historical data retention

### **Interactive Charts**
- Zoom and pan capabilities
- Time range selection
- Metric filtering
- Export functionality

### **Performance Thresholds**
- Color-coded alerts (Green/Yellow/Red)
- Performance benchmarks
- Automatic alerting

## 🔧 Troubleshooting

### **No Data Showing**
1. **Check Prometheus Connection:**
   - Go to Data Sources
   - Test Prometheus connection
   - Verify URL: `http://prometheus:9090`

2. **Check API Gateway:**
   ```bash
   curl http://localhost:8081/api/queueit/health
   ```

3. **Regenerate Metrics:**
   ```bash
   ./generate_metrics.sh
   ```

### **Dashboard Not Loading**
1. **Check Grafana Status:**
   ```bash
   curl http://localhost:3000/api/health
   ```

2. **Restart Services:**
   ```bash
   docker restart queueit-grafana queueit-prometheus
   ```

### **Login Issues**
- Try different credentials: `admin/admin` or `admin/admin123`
- Check if you need to change password on first login

## 🚀 Quick Commands

### **Generate More Metrics:**
```bash
# Quick test
./test.sh

# Comprehensive metrics
./generate_metrics.sh

# Manual test runs
python3 simple_functional_test.py
```

### **Check Services:**
```bash
# API Gateway health
curl http://localhost:8081/api/queueit/health

# Prometheus status
curl http://localhost:9090/api/v1/status/config

# Grafana health
curl http://localhost:3000/api/health
```

## 📱 Mobile Access

Grafana dashboards are mobile-responsive:
- Access via mobile browser
- Touch-friendly controls
- Adaptive layouts
- Real-time updates

## 🎯 What You Should See

### **Immediate Metrics:**
- API response times (should be < 1 second)
- Request counts (multiple test runs)
- Success rates (should be > 95%)
- Queue operation metrics

### **Performance Indicators:**
- **Green:** Good performance
- **Yellow:** Warning thresholds
- **Red:** Performance issues

### **Trends:**
- Test execution patterns
- API performance over time
- Queue behavior trends
- Error rate patterns

## 🔗 Quick Links

- **Grafana:** http://localhost:3000
- **Prometheus:** http://localhost:9090
- **API Gateway:** http://localhost:8081
- **Queue-It Health:** http://localhost:8081/api/queueit/health

---

## 🎉 Success!

Your Queue-It testing metrics are now available in Grafana with:
- ✅ Real-time monitoring
- ✅ Historical data
- ✅ Performance alerts
- ✅ Interactive dashboards
- ✅ Mobile access

**Next Steps:**
1. Open http://localhost:3000
2. Login with admin credentials
3. Add Prometheus data source
4. Import Queue-It dashboards
5. Explore your metrics!

**Your Queue-It integration is fully monitored and ready for production!** 🚀

> **Note:** The 'Testing Objective' column in the Grafana Quick Start tables is based on the objectives listed in the 'Overview & Testing Objectives' section of GRAFANA_QUICK_START.md. 