/**
 * Frontend Queue-it Integration Test
 * Run this with Node.js to test the frontend Queue-it service
 */

const API_BASE_URL = process.env.FRONTEND_API_BASE_URL || 'https://localhost:8081';

// Test data
const TEST_DATA = {
  eventId: 'test-event-2024',
  queueitToken: 'test-token-123',
  originalUrl: 'https://localhost/test-page',
  queueId: 'test-queue-123'
};

// Colors for console output
const colors = {
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  reset: '\x1b[0m'
};

let testsPassed = 0;
let testsFailed = 0;

function log(message, color = 'reset') {
  console.log(`${colors[color]}${message}${colors.reset}`);
}

function testResult(passed, testName, details = '') {
  if (passed) {
    log(`✅ PASS: ${testName}`, 'green');
    testsPassed++;
  } else {
    log(`❌ FAIL: ${testName}`, 'red');
    if (details) log(`   Details: ${details}`, 'red');
    testsFailed++;
  }
}

async function testEndpoint(method, endpoint, data = null, testName) {
  try {
    log(`\n${colors.blue}Testing:${colors.reset} ${testName}`);
    log(`Endpoint: ${method} ${endpoint}`);
    
    const options = {
      method,
      headers: {
        'Content-Type': 'application/json'
      }
    };
    
    if (data) {
      options.body = JSON.stringify(data);
      log(`Data: ${JSON.stringify(data, null, 2)}`);
    }
    
    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
    const responseData = await response.text();
    
    log(`Status: ${response.status}`);
    log(`Response: ${responseData}`);
    
    const isSuccess = response.status >= 200 && response.status < 300;
    testResult(isSuccess, testName, isSuccess ? '' : `Status: ${response.status}`);
    
    return { success: isSuccess, status: response.status, data: responseData };
  } catch (error) {
    log(`Error: ${error.message}`, 'red');
    testResult(false, testName, error.message);
    return { success: false, error: error.message };
  }
}

async function runTests() {
  log(`Frontend API Base URL: ${API_BASE_URL}`, 'blue');
  log('🚀 Frontend Queue-it Integration Test Suite', 'yellow');
  log('============================================', 'yellow');
  
  // Test 1: Health Check
  await testEndpoint('GET', '/api/queueit/health', null, 'Health Check');
  
  // Test 2: Status Check
  await testEndpoint('GET', '/api/queueit/status', null, 'Status Check');
  
  // Test 3: Validate Queue Token
  await testEndpoint('POST', '/api/queueit/validate', {
    eventId: TEST_DATA.eventId,
    queueitToken: TEST_DATA.queueitToken,
    originalUrl: TEST_DATA.originalUrl
  }, 'Queue Token Validation');
  
  // Test 4: Cancel Queue Session
  await testEndpoint('POST', '/api/queueit/cancel', {
    eventId: TEST_DATA.eventId,
    queueitToken: TEST_DATA.queueitToken
  }, 'Queue Session Cancellation');
  
  // Test 5: Extend Queue Cookie
  await testEndpoint('POST', '/api/queueit/extend-cookie', {
    eventId: TEST_DATA.eventId,
    queueId: TEST_DATA.queueId,
    cookieValidityMinutes: 30,
    cookieDomain: 'localhost',
    isCookieHttpOnly: true,
    isCookieSecure: false
  }, 'Queue Cookie Extension');
  
  // Test 6: Invalid Data
  await testEndpoint('POST', '/api/queueit/validate', {
    eventId: '',
    queueitToken: '',
    originalUrl: ''
  }, 'Invalid Data Handling');
  
  // Test 7: Missing Required Fields
  await testEndpoint('POST', '/api/queueit/validate', {
    eventId: 'test-event'
  }, 'Missing Required Fields');
  
  // Test Summary
  log('\n📊 Test Summary', 'yellow');
  log('==============', 'yellow');
  log(`Tests Passed: ${testsPassed}`, 'green');
  log(`Tests Failed: ${testsFailed}`, 'red');
  log(`Total Tests: ${testsPassed + testsFailed}`);
  
  if (testsFailed === 0) {
    log('\n🎉 All tests passed! Frontend integration is ready.', 'green');
  } else {
    log('\n⚠️  Some tests failed. Check the backend logs for details.', 'red');
  }
  
  log('\n📋 Next Steps:', 'blue');
  log('1. Start the React development server: npm start');
  log('2. Import and use QueueItExample component');
  log('3. Test the UI interactions');
  log('4. Configure real Queue-it event IDs');
}

// Check if fetch is available (Node.js 18+ or browser)
if (typeof fetch === 'undefined') {
  log('❌ Fetch API not available. Please use Node.js 18+ or run in a browser.', 'red');
  process.exit(1);
}

// Run the tests
runTests().catch(error => {
  log(`❌ Test suite failed: ${error.message}`, 'red');
  process.exit(1);
}); 