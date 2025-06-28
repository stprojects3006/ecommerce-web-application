"""
Base test class for PURELY E-commerce Load Testing
"""

import time
import pytest
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from webdriver_manager.chrome import ChromeDriverManager
from config import TestConfig
from latency_monitor import LatencyMonitor
import requests
import json
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class BaseTest:
    """Base test class with common functionality and latency monitoring"""
    
    def setup_method(self):
        """Setup method called before each test"""
        self.driver = self._setup_driver()
        self.wait = WebDriverWait(self.driver, TestConfig.BROWSER_CONFIG['implicit_wait'])
        self.session = requests.Session()
        self.test_start_time = time.time()
        
        # Initialize latency monitor
        self.latency_monitor = LatencyMonitor()
        
    def teardown_method(self):
        """Teardown method called after each test"""
        if hasattr(self, 'driver'):
            self.driver.quit()
            
        # Generate latency report if data exists
        if hasattr(self, 'latency_monitor') and self.latency_monitor.latency_data:
            self.latency_monitor.print_summary()
            
    def _setup_driver(self):
        """Setup Chrome WebDriver with optimal configuration"""
        chrome_options = Options()
        
        if TestConfig.BROWSER_CONFIG['headless']:
            chrome_options.add_argument('--headless')
            
        chrome_options.add_argument('--no-sandbox')
        chrome_options.add_argument('--disable-dev-shm-usage')
        chrome_options.add_argument('--disable-gpu')
        chrome_options.add_argument('--window-size=1920,1080')
        chrome_options.add_argument('--disable-blink-features=AutomationControlled')
        chrome_options.add_experimental_option("excludeSwitches", ["enable-automation"])
        chrome_options.add_experimental_option('useAutomationExtension', False)
        
        service = Service(ChromeDriverManager().install())
        driver = webdriver.Chrome(service=service, options=chrome_options)
        
        # Set timeouts
        driver.implicitly_wait(TestConfig.BROWSER_CONFIG['implicit_wait'])
        driver.set_page_load_timeout(TestConfig.BROWSER_CONFIG['page_load_timeout'])
        
        return driver
        
    def navigate_to(self, url):
        """Navigate to a specific URL with performance measurement"""
        start_time = time.time()
        self.driver.get(url)
        load_time = time.time() - start_time
        
        # Record page load latency
        self.latency_monitor.record_request("page_load", "GET", start_time)
        self.latency_monitor.record_response("page_load", "GET", time.time(), 200, 0)
        
        logger.info(f"Page load time for {url}: {load_time:.2f} seconds")
        
        if load_time > TestConfig.PERFORMANCE_THRESHOLDS['page_load_time']:
            logger.warning(f"Page load time {load_time:.2f}s exceeds threshold {TestConfig.PERFORMANCE_THRESHOLDS['page_load_time']}s")
            
        return load_time
        
    def wait_for_element(self, by, value, timeout=None):
        """Wait for element to be present and visible"""
        timeout = timeout or TestConfig.BROWSER_CONFIG['implicit_wait']
        try:
            element = WebDriverWait(self.driver, timeout).until(
                EC.presence_of_element_located((by, value))
            )
            return element
        except TimeoutException:
            logger.error(f"Element not found: {by}={value}")
            raise
            
    def wait_for_element_clickable(self, by, value, timeout=None):
        """Wait for element to be clickable"""
        timeout = timeout or TestConfig.BROWSER_CONFIG['implicit_wait']
        try:
            element = WebDriverWait(self.driver, timeout).until(
                EC.element_to_be_clickable((by, value))
            )
            return element
        except TimeoutException:
            logger.error(f"Element not clickable: {by}={value}")
            raise
            
    def safe_click(self, element):
        """Safely click an element with retry logic"""
        try:
            element.click()
            return True
        except Exception as e:
            logger.warning(f"Click failed, trying JavaScript click: {e}")
            try:
                self.driver.execute_script("arguments[0].click();", element)
                return True
            except Exception as e2:
                logger.error(f"JavaScript click also failed: {e2}")
                return False
                
    def safe_send_keys(self, element, text):
        """Safely send keys to an element"""
        try:
            element.clear()
            element.send_keys(text)
            return True
        except Exception as e:
            logger.error(f"Send keys failed: {e}")
            return False
            
    def get_element_text(self, by, value):
        """Get text from element safely"""
        try:
            element = self.driver.find_element(by, value)
            return element.text
        except NoSuchElementException:
            logger.warning(f"Element not found: {by}={value}")
            return None
            
    def is_element_present(self, by, value):
        """Check if element is present"""
        try:
            self.driver.find_element(by, value)
            return True
        except NoSuchElementException:
            return False
            
    def api_request(self, method, url, data=None, headers=None, expected_status=200):
        """Make API request with performance measurement and latency monitoring"""
        start_time = time.time()
        
        # Extract endpoint for latency monitoring
        endpoint = url.replace(TestConfig.API_BASE_URL, "").split("?")[0]
        
        # Record request start
        self.latency_monitor.record_request(endpoint, method, start_time)
        
        try:
            if method.upper() == 'GET':
                response = self.session.get(url, headers=headers)
            elif method.upper() == 'POST':
                response = self.session.post(url, json=data, headers=headers)
            elif method.upper() == 'PUT':
                response = self.session.put(url, json=data, headers=headers)
            elif method.upper() == 'DELETE':
                response = self.session.delete(url, headers=headers)
            else:
                raise ValueError(f"Unsupported HTTP method: {method}")
                
            end_time = time.time()
            response_time = end_time - start_time
            
            # Record response with detailed metrics
            response_size = len(response.content) if response.content else 0
            self.latency_monitor.record_response(endpoint, method, end_time, response.status_code, response_size)
            
            logger.info(f"API {method} {endpoint} - Status: {response.status_code}, Time: {response_time:.2f}s, Size: {response_size} bytes")
            
            if response.status_code != expected_status:
                logger.warning(f"API returned unexpected status: {response.status_code}, expected: {expected_status}")
                
            if response_time > TestConfig.PERFORMANCE_THRESHOLDS['api_response_time']:
                logger.warning(f"API response time {response_time:.2f}s exceeds threshold {TestConfig.PERFORMANCE_THRESHOLDS['api_response_time']}s")
                
            return response, response_time
            
        except Exception as e:
            end_time = time.time()
            response_time = end_time - start_time
            
            # Record error response
            self.latency_monitor.record_response(endpoint, method, end_time, 500, 0)
            
            logger.error(f"API request failed: {e}, Time: {response_time:.2f}s")
            raise
            
    def take_screenshot(self, name):
        """Take screenshot for debugging"""
        timestamp = time.strftime("%Y%m%d_%H%M%S")
        filename = f"screenshots/{name}_{timestamp}.png"
        self.driver.save_screenshot(filename)
        logger.info(f"Screenshot saved: {filename}")
        
    def get_performance_metrics(self):
        """Get browser performance metrics"""
        try:
            navigation_timing = self.driver.execute_script("""
                var performance = window.performance;
                var timing = performance.timing;
                return {
                    'domContentLoaded': timing.domContentLoadedEventEnd - timing.navigationStart,
                    'loadComplete': timing.loadEventEnd - timing.navigationStart,
                    'firstPaint': performance.getEntriesByType('paint')[0]?.startTime || 0
                };
            """)
            return navigation_timing
        except Exception as e:
            logger.warning(f"Could not get performance metrics: {e}")
            return {}
            
    def login_user(self, email, password):
        """Login a user and return authentication token"""
        try:
            response, response_time = self.api_request(
                'POST',
                TestConfig.API_ENDPOINTS['auth_signin'],
                data={'email': email, 'password': password}
            )
            
            if response.status_code == 200:
                token_data = response.json()
                token = token_data.get('response', {}).get('token')
                if token:
                    self.session.headers.update({'Authorization': f'Bearer {token}'})
                    logger.info(f"User logged in successfully: {email}")
                    return token
                    
            logger.error(f"Login failed for user: {email}")
            return None
            
        except Exception as e:
            logger.error(f"Login error: {e}")
            return None
            
    def create_test_user(self, username, email, password):
        """Create a test user account"""
        try:
            response, response_time = self.api_request(
                'POST',
                TestConfig.API_ENDPOINTS['auth_signup'],
                data={'userName': username, 'email': email, 'password': password}
            )
            
            if response.status_code == 200:
                logger.info(f"Test user created: {email}")
                return True
            else:
                logger.warning(f"User creation failed: {response.text}")
                return False
                
        except Exception as e:
            logger.error(f"User creation error: {e}")
            return False
            
    def cleanup_test_data(self):
        """Cleanup test data after tests"""
        # This can be implemented based on your cleanup requirements
        pass
        
    def assert_performance_threshold(self, actual_time, threshold_name):
        """Assert that performance meets threshold"""
        threshold = TestConfig.PERFORMANCE_THRESHOLDS.get(threshold_name)
        if threshold and actual_time > threshold:
            pytest.fail(f"Performance threshold exceeded: {actual_time:.2f}s > {threshold}s for {threshold_name}")
            
    def log_test_result(self, test_name, success, duration, additional_info=None):
        """Log test result with performance data"""
        status = "PASS" if success else "FAIL"
        logger.info(f"Test: {test_name} | Status: {status} | Duration: {duration:.2f}s")
        
        if additional_info:
            logger.info(f"Additional Info: {additional_info}")
            
    def get_latency_summary(self):
        """Get a summary of all latency data collected during the test"""
        return self.latency_monitor.get_all_stats()
        
    def generate_latency_report(self, test_name="test"):
        """Generate latency report for the current test"""
        timestamp = time.strftime("%Y%m%d_%H%M%S")
        report_file = f"reports/latency_report_{test_name}_{timestamp}.json"
        csv_file = f"reports/latency_data_{test_name}_{timestamp}.csv"
        
        # Generate JSON report
        report = self.latency_monitor.generate_latency_report(report_file)
        
        # Export CSV data
        self.latency_monitor.export_latency_data(csv_file)
        
        return report, report_file, csv_file 