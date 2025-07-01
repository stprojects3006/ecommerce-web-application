# 🚀 Queue-It Testing - Quick Start Guide

## One-Command Setup & Test

```bash
# Navigate to testing directory
cd testing-projects/queueit-functional-testing

# Setup environment (first time only)
./setup.sh

# Run tests
./test.sh
```

## 📋 Available Commands

| Command | Description | Output |
|---------|-------------|---------|
| `./setup.sh` | Setup environment | ✅ Environment ready |
| `./test.sh` | Quick test | 📊 Results summary |
| `./run_queueit_tests.sh` | Advanced tests | 🔍 Detailed results |
| `./run_queueit_tests.sh -h` | Show help | 📚 All options |

## 🧪 Test Results Example

```
🧪 Queue-It Quick Test
======================
✅ Using virtual environment
✅ API Gateway is running
🚀 Simple Queue-It Functional Test
==================================================
🔍 Testing Queue-It health...
✅ Health Check: PASS
   Details: Service: queueit-integration, Customer: futuraforge
🔍 Testing queue status...
✅ Queue Status - flash-sale-2024: PASS
✅ Queue Status - black-friday-2024: PASS
✅ Queue Status - checkout-protection: PASS
🔍 Testing enqueue functionality...
✅ Enqueue: PASS
🔍 Testing API endpoints...
✅ API /health: PASS
✅ API /status/flash-sale-2024: PASS
✅ API /stats/flash-sale-2024: PASS
==================================================
📊 TEST RESULTS SUMMARY
==================================================
Tests Passed: 5/5
Success Rate: 100.0%
Status: PASS
==================================================
🎉 All tests passed! Queue-It integration is working correctly.
```

## 🔧 Prerequisites

- ✅ Python 3.x installed
- ✅ API Gateway running on localhost:8081
- ✅ Queue-It credentials configured

## 🚨 Troubleshooting

### API Gateway Not Running
```bash
cd ../../microservice-backend/api-gateway
./mvnw spring-boot:run
```

### Python Dependencies Missing
```bash
source venv/bin/activate
pip install requests
```

### Permission Denied
```bash
chmod +x *.sh
```

## 📊 Success Indicators

- ✅ **PASS**: Test successful
- ❌ **FAIL**: Test failed
- ⚠️ **WARN**: Test completed with warnings

## 🎯 What Gets Tested

1. **Health Check**: API Gateway connectivity
2. **Queue Status**: Event availability
3. **Enqueue**: Queue entry creation
4. **API Endpoints**: All Queue-It endpoints
5. **Error Handling**: Invalid requests

---

**Need Help?** See `README.md` for detailed documentation. 