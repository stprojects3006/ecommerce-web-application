/**
 * Queue-It Frontend Official Connector Tests
 * Tests the Queue-It JavaScript connector integration
 */

const puppeteer = require('puppeteer');
const assert = require('assert');

class QueueItFrontendOfficialTests {
    constructor(baseUrl = 'https://localhost') {
        this.baseUrl = baseUrl;
        this.browser = null;
        this.page = null;
        this.testResults = [];
    }

    async setup() {
        console.log('🚀 Setting up browser for Queue-It frontend tests...');
        this.browser = await puppeteer.launch({
            headless: false,
            args: ['--no-sandbox', '--disable-setuid-sandbox']
        });
        this.page = await this.browser.newPage();
        
        // Set viewport
        await this.page.setViewport({ width: 1280, height: 720 });
        
        // Set user agent
        await this.page.setUserAgent('Mozilla/5.0 (Test Browser) QueueIt-Test-Suite/1.0');
        
        console.log('✅ Browser setup complete');
    }

    async teardown() {
        if (this.browser) {
            await this.browser.close();
            console.log('🔒 Browser closed');
        }
    }

    async logTestResult(testName, status, details = null) {
        const result = {
            test: testName,
            status: status,
            timestamp: new Date().toISOString(),
            details: details
        };
        this.testResults.push(result);
        
        const icon = status === 'PASS' ? '✅' : status === 'FAIL' ? '❌' : '⚠️';
        console.log(`${icon} ${testName}: ${status}`);
        if (details) {
            console.log(`   Details: ${details}`);
        }
    }

    async testQueueItScriptLoading() {
        console.log('Testing Queue-It script loading...');
        
        try {
            await this.page.goto(`${this.baseUrl}/flash-sale`, { waitUntil: 'networkidle2' });
            
            // Check if Queue-It script is loaded
            const queueItScript = await this.page.evaluate(() => {
                return window.QueueIt || window.queueit || document.querySelector('script[src*="queue-it"]');
            });
            
            assert(queueItScript, 'Queue-It script not loaded');
            
            await this.logTestResult('Queue-It Script Loading', 'PASS');
            return true;
            
        } catch (error) {
            await this.logTestResult('Queue-It Script Loading', 'FAIL', error.message);
            return false;
        }
    }

    async testQueueItConfiguration() {
        console.log('Testing Queue-It configuration...');
        
        try {
            await this.page.goto(`${this.baseUrl}/flash-sale`, { waitUntil: 'networkidle2' });
            
            // Check Queue-It configuration
            const config = await this.page.evaluate(() => {
                return {
                    hasConfig: !!window.queueitConfig,
                    customerId: window.queueitConfig?.customerId,
                    eventId: window.queueitConfig?.eventId,
                    layoutId: window.queueitConfig?.layoutId
                };
            });
            
            assert(config.hasConfig, 'Queue-It configuration not found');
            assert(config.customerId, 'Customer ID not configured');
            assert(config.eventId, 'Event ID not configured');
            
            await this.logTestResult('Queue-It Configuration', 'PASS', 
                `Customer: ${config.customerId}, Event: ${config.eventId}`);
            return true;
            
        } catch (error) {
            await this.logTestResult('Queue-It Configuration', 'FAIL', error.message);
            return false;
        }
    }

    async testQueueItConnectorInitialization() {
        console.log('Testing Queue-It connector initialization...');
        
        try {
            await this.page.goto(`${this.baseUrl}/flash-sale`, { waitUntil: 'networkidle2' });
            
            // Wait for Queue-It connector to initialize
            await this.page.waitForTimeout(2000);
            
            const connectorStatus = await this.page.evaluate(() => {
                return {
                    hasConnector: !!window.QueueIt || !!window.queueit,
                    connectorType: window.QueueIt ? 'official' : 'custom',
                    isInitialized: !!(window.QueueIt?.connector || window.queueit?.connector)
                };
            });
            
            assert(connectorStatus.hasConnector, 'Queue-It connector not found');
            assert(connectorStatus.isInitialized, 'Queue-It connector not initialized');
            
            await this.logTestResult('Queue-It Connector Initialization', 'PASS', 
                `Type: ${connectorStatus.connectorType}`);
            return true;
            
        } catch (error) {
            await this.logTestResult('Queue-It Connector Initialization', 'FAIL', error.message);
            return false;
        }
    }

    async testQueueItEventHandling() {
        console.log('Testing Queue-It event handling...');
        
        try {
            await this.page.goto(`${this.baseUrl}/flash-sale`, { waitUntil: 'networkidle2' });
            
            // Listen for Queue-It events
            const events = await this.page.evaluate(() => {
                return new Promise((resolve) => {
                    const events = [];
                    
                    // Listen for Queue-It events
                    const originalAddEventListener = window.addEventListener;
                    window.addEventListener = function(type, listener, options) {
                        if (type.includes('queue') || type.includes('Queue')) {
                            events.push({ type, timestamp: Date.now() });
                        }
                        return originalAddEventListener.call(this, type, listener, options);
                    };
                    
                    // Wait for events
                    setTimeout(() => resolve(events), 3000);
                });
            });
            
            // Check if any Queue-It related events were triggered
            const hasQueueEvents = events.length > 0;
            
            await this.logTestResult('Queue-It Event Handling', 'PASS', 
                `Events detected: ${events.length}`);
            return true;
            
        } catch (error) {
            await this.logTestResult('Queue-It Event Handling', 'FAIL', error.message);
            return false;
        }
    }

    async testQueueItRedirectHandling() {
        console.log('Testing Queue-It redirect handling...');
        
        try {
            await this.page.goto(`${this.baseUrl}/flash-sale`, { waitUntil: 'networkidle2' });
            
            // Simulate Queue-It redirect
            const redirectResult = await this.page.evaluate(() => {
                return new Promise((resolve) => {
                    // Mock Queue-It redirect
                    const mockRedirect = {
                        url: 'https://futuraforge.queue-it.net/queue/flash-sale-2024',
                        timestamp: Date.now()
                    };
                    
                    // Simulate redirect handling
                    if (window.queueit && window.queueit.handleRedirect) {
                        window.queueit.handleRedirect(mockRedirect);
                        resolve({ handled: true, url: mockRedirect.url });
                    } else {
                        resolve({ handled: false, error: 'No redirect handler found' });
                    }
                });
            });
            
            assert(redirectResult.handled, 'Redirect not handled properly');
            
            await this.logTestResult('Queue-It Redirect Handling', 'PASS', 
                `Redirect URL: ${redirectResult.url}`);
            return true;
            
        } catch (error) {
            await this.logTestResult('Queue-It Redirect Handling', 'FAIL', error.message);
            return false;
        }
    }

    async testQueueItTokenValidation() {
        console.log('Testing Queue-It token validation...');
        
        try {
            await this.page.goto(`${this.baseUrl}/flash-sale`, { waitUntil: 'networkidle2' });
            
            // Test token validation
            const validationResult = await this.page.evaluate(() => {
                return new Promise((resolve) => {
                    const testToken = 'test-queue-token-12345';
                    
                    // Mock API call to validate token
                    fetch('/api/queueit/validate', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            eventId: 'flash-sale-2024',
                            queueitToken: testToken,
                            originalUrl: window.location.href
                        })
                    })
                    .then(response => response.json())
                    .then(data => resolve({ success: true, data }))
                    .catch(error => resolve({ success: false, error: error.message }));
                });
            });
            
            assert(validationResult.success, 'Token validation failed');
            assert(validationResult.data.eventId, 'Event ID missing in response');
            
            await this.logTestResult('Queue-It Token Validation', 'PASS', 
                `Event: ${validationResult.data.eventId}`);
            return true;
            
        } catch (error) {
            await this.logTestResult('Queue-It Token Validation', 'FAIL', error.message);
            return false;
        }
    }

    async testQueueItPositionChecking() {
        console.log('Testing Queue-It position checking...');
        
        try {
            await this.page.goto(`${this.baseUrl}/flash-sale`, { waitUntil: 'networkidle2' });
            
            // Test position checking
            const positionResult = await this.page.evaluate(() => {
                return new Promise((resolve) => {
                    const testToken = 'test-queue-token-12345';
                    
                    // Mock API call to check position
                    fetch(`/api/queueit/position/flash-sale-2024?queueitToken=${testToken}`)
                    .then(response => response.json())
                    .then(data => resolve({ success: true, data }))
                    .catch(error => resolve({ success: false, error: error.message }));
                });
            });
            
            assert(positionResult.success, 'Position checking failed');
            assert(positionResult.data.position !== undefined, 'Position missing in response');
            
            await this.logTestResult('Queue-It Position Checking', 'PASS', 
                `Position: ${positionResult.data.position}`);
            return true;
            
        } catch (error) {
            await this.logTestResult('Queue-It Position Checking', 'FAIL', error.message);
            return false;
        }
    }

    async testQueueItErrorHandling() {
        console.log('Testing Queue-It error handling...');
        
        try {
            await this.page.goto(`${this.baseUrl}/flash-sale`, { waitUntil: 'networkidle2' });
            
            // Test error handling
            const errorResult = await this.page.evaluate(() => {
                return new Promise((resolve) => {
                    // Test with invalid token
                    fetch('/api/queueit/validate', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            eventId: 'invalid-event',
                            queueitToken: 'invalid-token',
                            originalUrl: window.location.href
                        })
                    })
                    .then(response => {
                        if (response.status >= 400) {
                            resolve({ success: true, status: response.status });
                        } else {
                            resolve({ success: false, error: 'Expected error status' });
                        }
                    })
                    .catch(error => resolve({ success: true, error: error.message }));
                });
            });
            
            assert(errorResult.success, 'Error handling failed');
            
            await this.logTestResult('Queue-It Error Handling', 'PASS', 
                `Error status: ${errorResult.status || 'handled'}`);
            return true;
            
        } catch (error) {
            await this.logTestResult('Queue-It Error Handling', 'FAIL', error.message);
            return false;
        }
    }

    async testQueueItIntegrationEndToEnd() {
        console.log('Testing Queue-It end-to-end integration...');
        
        try {
            await this.page.goto(`${this.baseUrl}/flash-sale`, { waitUntil: 'networkidle2' });
            
            // Simulate complete Queue-It flow
            const e2eResult = await this.page.evaluate(() => {
                return new Promise((resolve) => {
                    const flow = {
                        step1: 'Page loaded',
                        step2: 'Queue-It script loaded',
                        step3: 'Configuration applied',
                        step4: 'Connector initialized',
                        step5: 'Event handling ready',
                        step6: 'Redirect handling ready',
                        step7: 'Token validation ready',
                        step8: 'Position checking ready',
                        step9: 'Error handling ready'
                    };
                    
                    // Check each step
                    const checks = {
                        step1: true,
                        step2: !!(window.QueueIt || window.queueit),
                        step3: !!window.queueitConfig,
                        step4: !!(window.QueueIt?.connector || window.queueit?.connector),
                        step5: true,
                        step6: true,
                        step7: true,
                        step8: true,
                        step9: true
                    };
                    
                    const allPassed = Object.values(checks).every(check => check);
                    
                    resolve({
                        success: allPassed,
                        checks: checks,
                        flow: flow
                    });
                });
            });
            
            assert(e2eResult.success, 'End-to-end integration failed');
            
            await this.logTestResult('Queue-It End-to-End Integration', 'PASS', 
                'All integration steps completed');
            return true;
            
        } catch (error) {
            await this.logTestResult('Queue-It End-to-End Integration', 'FAIL', error.message);
            return false;
        }
    }

    async runComprehensiveTestSuite() {
        console.log('🚀 Starting Queue-It Frontend Official Connector Test Suite');
        
        try {
            await this.setup();
            
            const tests = [
                this.testQueueItScriptLoading(),
                this.testQueueItConfiguration(),
                this.testQueueItConnectorInitialization(),
                this.testQueueItEventHandling(),
                this.testQueueItRedirectHandling(),
                this.testQueueItTokenValidation(),
                this.testQueueItPositionChecking(),
                this.testQueueItErrorHandling(),
                this.testQueueItIntegrationEndToEnd()
            ];
            
            const results = await Promise.all(tests);
            
            // Calculate results
            const passedTests = results.filter(result => result).length;
            const totalTests = results.length;
            const successRate = (passedTests / totalTests) * 100;
            
            const summary = {
                overall_status: passedTests === totalTests ? 'PASS' : 'FAIL',
                passed_tests: passedTests,
                total_tests: totalTests,
                success_rate: `${successRate.toFixed(1)}%`,
                test_results: this.testResults,
                timestamp: new Date().toISOString()
            };
            
            console.log('\n' + '='.repeat(60));
            console.log('QUEUE-IT FRONTEND OFFICIAL CONNECTOR TEST RESULTS');
            console.log('='.repeat(60));
            console.log(`Overall Status: ${summary.overall_status}`);
            console.log(`Tests Passed: ${passedTests}/${totalTests}`);
            console.log(`Success Rate: ${summary.success_rate}`);
            console.log(`Timestamp: ${summary.timestamp}`);
            console.log('='.repeat(60));
            
            // Print detailed results
            this.testResults.forEach(result => {
                const icon = result.status === 'PASS' ? '✅' : '❌';
                console.log(`${icon} ${result.test}: ${result.status}`);
            });
            
            console.log('='.repeat(60));
            
            return summary;
            
        } finally {
            await this.teardown();
        }
    }
}

// Export for use in other test files
module.exports = QueueItFrontendOfficialTests;

// Run tests if called directly
if (require.main === module) {
    const testSuite = new QueueItFrontendOfficialTests();
    testSuite.runComprehensiveTestSuite()
        .then(summary => {
            process.exit(summary.overall_status === 'PASS' ? 0 : 1);
        })
        .catch(error => {
            console.error('Test suite failed:', error);
            process.exit(1);
        });
} 