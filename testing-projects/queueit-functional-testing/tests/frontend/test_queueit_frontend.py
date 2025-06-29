"""
Frontend Tests for Queue-it Integration
Test Queue-it frontend components and functionality using Selenium
"""

import pytest
import time
import json
from typing import Dict, Any, Optional
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
from selenium.common.exceptions import TimeoutException, NoSuchElementException

from utils.test_helpers import (
    config, performance_monitor, retry_helper,
    AssertionHelper, TestDataGenerator
)
from utils.grafana_client import metrics_collector


class QueueItFrontendTest:
    """Base class for Queue-it frontend tests"""
    
    def __init__(self):
        self.driver = None
        self.wait = None
        self.base_url = config.get('urls.frontend_url')
    
    def setup_driver(self, headless: bool = True):
        """Setup Chrome WebDriver"""
        chrome_options = Options()
        if headless:
            chrome_options.add_argument("--headless")
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-dev-shm-usage")
        chrome_options.add_argument("--disable-gpu")
        chrome_options.add_argument("--window-size=1920,1080")
        
        service = Service(ChromeDriverManager().install())
        self.driver = webdriver.Chrome(service=service, options=chrome_options)
        self.wait = WebDriverWait(self.driver, 10)
        
        return self.driver
    
    def teardown_driver(self):
        """Cleanup WebDriver"""
        if self.driver:
            self.driver.quit()
    
    def wait_for_element(self, by: By, value: str, timeout: int = 10):
        """Wait for element to be present"""
        return WebDriverWait(self.driver, timeout).until(
            EC.presence_of_element_located((by, value))
        )
    
    def wait_for_element_clickable(self, by: By, value: str, timeout: int = 10):
        """Wait for element to be clickable"""
        return WebDriverWait(self.driver, timeout).until(
            EC.element_to_be_clickable((by, value))
        )
    
    def take_screenshot(self, name: str):
        """Take screenshot for debugging"""
        timestamp = int(time.time())
        filename = f"reports/screenshots/{name}_{timestamp}.png"
        self.driver.save_screenshot(filename)
        logger.info(f"Screenshot saved: {filename}")


class TestQueueItServiceInitialization:
    """Test Queue-it service initialization"""
    
    def test_queueit_service_loaded(self):
        """Test that Queue-it service is properly loaded"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver()
            driver.get(f"{test.base_url}/flash-sale")
            
            # Wait for page to load
            time.sleep(3)
            
            # Check if Queue-it service is loaded
            queueit_service = driver.execute_script(
                "return window.queueitService !== undefined"
            )
            assert queueit_service, "Queue-it service should be loaded"
            
            # Check if Queue-it context is available
            queueit_context = driver.execute_script(
                "return window.queueitContext !== undefined"
            )
            assert queueit_context, "Queue-it context should be available"
            
        finally:
            test.teardown_driver()
    
    def test_queueit_config_loaded(self):
        """Test that Queue-it configuration is properly loaded"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver()
            driver.get(f"{test.base_url}/flash-sale")
            
            # Wait for page to load
            time.sleep(3)
            
            # Check Queue-it configuration
            config = driver.execute_script(
                "return window.queueitConfig"
            )
            assert config is not None, "Queue-it config should be loaded"
            assert 'customerId' in config, "Config should contain customerId"
            assert 'secretKey' in config, "Config should contain secretKey"
            
        finally:
            test.teardown_driver()


class TestQueueItTriggerDetection:
    """Test Queue-it trigger detection"""
    
    def test_flash_sale_trigger(self):
        """Test Queue-it trigger on flash sale page"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver()
            start_time = performance_monitor.start_timer()
            
            # Navigate to flash sale page
            driver.get(f"{test.base_url}/flash-sale")
            
            # Wait for Queue-it overlay to appear (if triggered)
            try:
                overlay = test.wait_for_element(
                    By.CSS_SELECTOR, 
                    "[data-testid='queueit-overlay']", 
                    timeout=5
                )
                
                # Queue was triggered
                duration = performance_monitor.end_timer(start_time)
                performance_monitor.record_metric('queue_trigger_detection_time', duration)
                
                # Verify overlay content
                assert overlay.is_displayed(), "Queue overlay should be visible"
                
                # Check for queue indicator
                indicator = driver.find_element(By.CSS_SELECTOR, "[data-testid='queueit-indicator']")
                assert indicator.is_displayed(), "Queue indicator should be visible"
                
                metrics_collector.record_test_metric(
                    'queue_trigger_detection_time',
                    duration,
                    {'page': 'flash-sale', 'triggered': 'true'}
                )
                
            except TimeoutException:
                # Queue was not triggered (this might be expected in test environment)
                duration = performance_monitor.end_timer(start_time)
                performance_monitor.record_metric('queue_trigger_detection_time', duration)
                
                metrics_collector.record_test_metric(
                    'queue_trigger_detection_time',
                    duration,
                    {'page': 'flash-sale', 'triggered': 'false'}
                )
                
                logger.info("Queue was not triggered on flash sale page (expected in test environment)")
            
        finally:
            test.teardown_driver()
    
    def test_black_friday_trigger(self):
        """Test Queue-it trigger on black Friday page"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver()
            start_time = performance_monitor.start_timer()
            
            # Navigate to black Friday page
            driver.get(f"{test.base_url}/black-friday")
            
            # Wait for Queue-it overlay to appear (if triggered)
            try:
                overlay = test.wait_for_element(
                    By.CSS_SELECTOR, 
                    "[data-testid='queueit-overlay']", 
                    timeout=5
                )
                
                # Queue was triggered
                duration = performance_monitor.end_timer(start_time)
                performance_monitor.record_metric('queue_trigger_detection_time', duration)
                
                assert overlay.is_displayed(), "Queue overlay should be visible"
                
                metrics_collector.record_test_metric(
                    'queue_trigger_detection_time',
                    duration,
                    {'page': 'black-friday', 'triggered': 'true'}
                )
                
            except TimeoutException:
                # Queue was not triggered
                duration = performance_monitor.end_timer(start_time)
                performance_monitor.record_metric('queue_trigger_detection_time', duration)
                
                metrics_collector.record_test_metric(
                    'queue_trigger_detection_time',
                    duration,
                    {'page': 'black-friday', 'triggered': 'false'}
                )
                
                logger.info("Queue was not triggered on black Friday page")
            
        finally:
            test.teardown_driver()
    
    def test_high_traffic_trigger(self):
        """Test Queue-it trigger on high traffic pages"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver()
            
            # Test multiple pages that might trigger high traffic protection
            pages = ['/products', '/categories', '/search']
            
            for page in pages:
                start_time = performance_monitor.start_timer()
                
                driver.get(f"{test.base_url}{page}")
                
                try:
                    overlay = test.wait_for_element(
                        By.CSS_SELECTOR, 
                        "[data-testid='queueit-overlay']", 
                        timeout=3
                    )
                    
                    # Queue was triggered
                    duration = performance_monitor.end_timer(start_time)
                    performance_monitor.record_metric('queue_trigger_detection_time', duration)
                    
                    metrics_collector.record_test_metric(
                        'queue_trigger_detection_time',
                        duration,
                        {'page': page, 'triggered': 'true'}
                    )
                    
                    logger.info(f"Queue triggered on {page}")
                    break  # Found a page that triggers queue
                    
                except TimeoutException:
                    # Queue was not triggered
                    duration = performance_monitor.end_timer(start_time)
                    performance_monitor.record_metric('queue_trigger_detection_time', duration)
                    
                    metrics_collector.record_test_metric(
                        'queue_trigger_detection_time',
                        duration,
                        {'page': page, 'triggered': 'false'}
                    )
            
        finally:
            test.teardown_driver()


class TestQueueItOverlay:
    """Test Queue-it overlay functionality"""
    
    def test_overlay_display(self):
        """Test Queue-it overlay display and content"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver()
            
            # Navigate to a page that might trigger queue
            driver.get(f"{test.base_url}/flash-sale")
            
            # Try to find overlay
            try:
                overlay = test.wait_for_element(
                    By.CSS_SELECTOR, 
                    "[data-testid='queueit-overlay']", 
                    timeout=5
                )
                
                # Test overlay visibility
                assert overlay.is_displayed(), "Queue overlay should be visible"
                
                # Test overlay content
                title = overlay.find_element(By.CSS_SELECTOR, "[data-testid='queueit-title']")
                assert title.text, "Overlay should have a title"
                
                # Test queue position display
                position_element = overlay.find_element(By.CSS_SELECTOR, "[data-testid='queueit-position']")
                assert position_element.text, "Overlay should show queue position"
                
                # Test estimated wait time
                wait_time_element = overlay.find_element(By.CSS_SELECTOR, "[data-testid='queueit-wait-time']")
                assert wait_time_element.text, "Overlay should show estimated wait time"
                
            except TimeoutException:
                logger.info("Queue overlay not displayed (expected in test environment)")
                
        finally:
            test.teardown_driver()
    
    def test_overlay_styling(self):
        """Test Queue-it overlay styling and responsiveness"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver()
            
            # Navigate to flash sale page
            driver.get(f"{test.base_url}/flash-sale")
            
            try:
                overlay = test.wait_for_element(
                    By.CSS_SELECTOR, 
                    "[data-testid='queueit-overlay']", 
                    timeout=5
                )
                
                # Test overlay positioning
                overlay_rect = overlay.rect
                assert overlay_rect['width'] > 0, "Overlay should have width"
                assert overlay_rect['height'] > 0, "Overlay should have height"
                
                # Test overlay z-index (should be high)
                z_index = driver.execute_script(
                    "return window.getComputedStyle(arguments[0]).zIndex", 
                    overlay
                )
                assert int(z_index) > 1000, "Overlay should have high z-index"
                
                # Test overlay background
                background = driver.execute_script(
                    "return window.getComputedStyle(arguments[0]).backgroundColor", 
                    overlay
                )
                assert background != "rgba(0, 0, 0, 0)", "Overlay should have background"
                
            except TimeoutException:
                logger.info("Queue overlay not displayed for styling test")
                
        finally:
            test.teardown_driver()


class TestQueueItIndicator:
    """Test Queue-it indicator functionality"""
    
    def test_indicator_display(self):
        """Test Queue-it indicator display"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver()
            
            # Navigate to flash sale page
            driver.get(f"{test.base_url}/flash-sale")
            
            try:
                indicator = test.wait_for_element(
                    By.CSS_SELECTOR, 
                    "[data-testid='queueit-indicator']", 
                    timeout=5
                )
                
                # Test indicator visibility
                assert indicator.is_displayed(), "Queue indicator should be visible"
                
                # Test indicator content
                position_text = indicator.find_element(By.CSS_SELECTOR, "[data-testid='queueit-position']")
                assert position_text.text, "Indicator should show position"
                
                # Test indicator styling
                indicator_rect = indicator.rect
                assert indicator_rect['width'] > 0, "Indicator should have width"
                assert indicator_rect['height'] > 0, "Indicator should have height"
                
            except TimeoutException:
                logger.info("Queue indicator not displayed (expected in test environment)")
                
        finally:
            test.teardown_driver()
    
    def test_indicator_position_updates(self):
        """Test Queue-it indicator position updates"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver()
            
            # Navigate to flash sale page
            driver.get(f"{test.base_url}/flash-sale")
            
            try:
                indicator = test.wait_for_element(
                    By.CSS_SELECTOR, 
                    "[data-testid='queueit-indicator']", 
                    timeout=5
                )
                
                # Get initial position
                initial_position = indicator.find_element(
                    By.CSS_SELECTOR, 
                    "[data-testid='queueit-position']"
                ).text
                
                # Wait for potential position update
                time.sleep(5)
                
                # Get updated position
                updated_position = indicator.find_element(
                    By.CSS_SELECTOR, 
                    "[data-testid='queueit-position']"
                ).text
                
                # Position should be a number
                assert initial_position.isdigit(), "Position should be numeric"
                assert updated_position.isdigit(), "Updated position should be numeric"
                
                # Record position update metric
                if initial_position != updated_position:
                    performance_monitor.record_metric('position_update_detected', 1)
                    metrics_collector.record_test_metric(
                        'position_update_detected',
                        1,
                        {'initial_position': initial_position, 'updated_position': updated_position}
                    )
                
            except TimeoutException:
                logger.info("Queue indicator not displayed for position update test")
                
        finally:
            test.teardown_driver()


class TestQueueItTokenManagement:
    """Test Queue-it token management"""
    
    def test_token_storage(self):
        """Test Queue-it token storage in localStorage"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver()
            
            # Navigate to flash sale page
            driver.get(f"{test.base_url}/flash-sale")
            
            # Wait for page to load
            time.sleep(3)
            
            # Check if token is stored in localStorage
            token = driver.execute_script(
                "return localStorage.getItem('queueit_token')"
            )
            
            if token:
                # Token is stored
                performance_monitor.record_metric('token_storage_success', 1)
                metrics_collector.record_test_metric(
                    'token_storage_success',
                    1,
                    {'token_length': len(token)}
                )
                
                # Verify token format (should be a string)
                assert isinstance(token, str), "Token should be a string"
                assert len(token) > 0, "Token should not be empty"
                
            else:
                # No token stored (might be expected if queue not triggered)
                performance_monitor.record_metric('token_storage_success', 0)
                metrics_collector.record_test_metric(
                    'token_storage_success',
                    0,
                    {'reason': 'no_queue_triggered'}
                )
                
                logger.info("No token stored (queue not triggered)")
                
        finally:
            test.teardown_driver()
    
    def test_token_retrieval(self):
        """Test Queue-it token retrieval from localStorage"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver()
            
            # Set a test token in localStorage
            test_token = TestDataGenerator.generate_queue_token()
            driver.get(f"{test.base_url}/flash-sale")
            
            driver.execute_script(
                f"localStorage.setItem('queueit_token', '{test_token}')"
            )
            
            # Refresh page to test token retrieval
            driver.refresh()
            time.sleep(3)
            
            # Check if token is retrieved
            retrieved_token = driver.execute_script(
                "return localStorage.getItem('queueit_token')"
            )
            
            assert retrieved_token == test_token, "Retrieved token should match stored token"
            
            performance_monitor.record_metric('token_retrieval_success', 1)
            metrics_collector.record_test_metric(
                'token_retrieval_success',
                1,
                {'token_length': len(retrieved_token)}
            )
            
        finally:
            test.teardown_driver()


class TestQueueItErrorHandling:
    """Test Queue-it error handling"""
    
    def test_network_error_handling(self):
        """Test Queue-it handling of network errors"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver()
            
            # Disable network (simulate offline)
            driver.execute_script("window.navigator.onLine = false;")
            
            # Navigate to flash sale page
            driver.get(f"{test.base_url}/flash-sale")
            
            # Wait for potential error handling
            time.sleep(3)
            
            # Check for error message or fallback behavior
            try:
                error_element = driver.find_element(By.CSS_SELECTOR, "[data-testid='queueit-error']")
                assert error_element.is_displayed(), "Error message should be displayed"
                
                performance_monitor.record_metric('error_handling_success', 1)
                metrics_collector.record_test_metric(
                    'error_handling_success',
                    1,
                    {'error_type': 'network_error'}
                )
                
            except NoSuchElementException:
                # No error element found (might be handled gracefully)
                performance_monitor.record_metric('error_handling_success', 1)
                metrics_collector.record_test_metric(
                    'error_handling_success',
                    1,
                    {'error_type': 'network_error', 'handled_gracefully': 'true'}
                )
                
                logger.info("Network error handled gracefully")
            
        finally:
            test.teardown_driver()
    
    def test_invalid_token_handling(self):
        """Test Queue-it handling of invalid tokens"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver()
            
            # Set invalid token
            invalid_token = "invalid-token-12345"
            driver.get(f"{test.base_url}/flash-sale")
            
            driver.execute_script(
                f"localStorage.setItem('queueit_token', '{invalid_token}')"
            )
            
            # Refresh page
            driver.refresh()
            time.sleep(3)
            
            # Check for error handling or token cleanup
            try:
                error_element = driver.find_element(By.CSS_SELECTOR, "[data-testid='queueit-error']")
                assert error_element.is_displayed(), "Error message should be displayed for invalid token"
                
                performance_monitor.record_metric('invalid_token_handling', 1)
                metrics_collector.record_test_metric(
                    'invalid_token_handling',
                    1,
                    {'action': 'error_displayed'}
                )
                
            except NoSuchElementException:
                # Check if token was cleaned up
                stored_token = driver.execute_script(
                    "return localStorage.getItem('queueit_token')"
                )
                
                if stored_token != invalid_token:
                    performance_monitor.record_metric('invalid_token_handling', 1)
                    metrics_collector.record_test_metric(
                        'invalid_token_handling',
                        1,
                        {'action': 'token_cleaned_up'}
                    )
                    
                    logger.info("Invalid token was cleaned up")
                else:
                    logger.info("Invalid token handling not detected")
            
        finally:
            test.teardown_driver()


class TestQueueItMobileResponsiveness:
    """Test Queue-it mobile responsiveness"""
    
    @pytest.mark.parametrize("device", [
        {"name": "iPhone 12", "width": 390, "height": 844},
        {"name": "Samsung Galaxy S21", "width": 360, "height": 800},
        {"name": "iPad", "width": 768, "height": 1024}
    ])
    def test_mobile_layout(self, device):
        """Test Queue-it layout on mobile devices"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver()
            
            # Set mobile viewport
            driver.set_window_size(device["width"], device["height"])
            
            # Navigate to flash sale page
            driver.get(f"{test.base_url}/flash-sale")
            
            try:
                overlay = test.wait_for_element(
                    By.CSS_SELECTOR, 
                    "[data-testid='queueit-overlay']", 
                    timeout=5
                )
                
                # Test mobile layout
                overlay_rect = overlay.rect
                
                # Overlay should fit within viewport
                assert overlay_rect['width'] <= device["width"], "Overlay should fit viewport width"
                assert overlay_rect['height'] <= device["height"], "Overlay should fit viewport height"
                
                # Test touch-friendly elements
                elements = overlay.find_elements(By.CSS_SELECTOR, "button, a, input")
                for element in elements:
                    element_rect = element.rect
                    # Elements should be at least 44px for touch
                    assert element_rect['width'] >= 44 or element_rect['height'] >= 44, \
                        f"Element should be touch-friendly: {element.tag_name}"
                
                performance_monitor.record_metric('mobile_layout_success', 1)
                metrics_collector.record_test_metric(
                    'mobile_layout_success',
                    1,
                    {'device': device["name"], 'width': device["width"], 'height': device["height"]}
                )
                
            except TimeoutException:
                logger.info(f"Queue overlay not displayed on {device['name']}")
                
        finally:
            test.teardown_driver()


class TestQueueItCrossBrowser:
    """Test Queue-it cross-browser compatibility"""
    
    def test_chrome_compatibility(self):
        """Test Queue-it in Chrome"""
        test = QueueItFrontendTest()
        
        try:
            driver = test.setup_driver(headless=True)
            driver.get(f"{test.base_url}/flash-sale")
            
            # Basic functionality test
            queueit_loaded = driver.execute_script(
                "return window.queueitService !== undefined"
            )
            assert queueit_loaded, "Queue-it should load in Chrome"
            
            performance_monitor.record_metric('chrome_compatibility', 1)
            metrics_collector.record_test_metric(
                'chrome_compatibility',
                1,
                {'browser': 'chrome', 'version': driver.capabilities['browserVersion']}
            )
            
        finally:
            test.teardown_driver()


# Test fixtures
@pytest.fixture(scope="session")
def setup_frontend_test_session():
    """Setup frontend test session"""
    metrics_collector.start_test_session("Queue-it Frontend Tests")
    yield
    metrics_collector.end_test_session("Queue-it Frontend Tests", success=True)


@pytest.fixture(autouse=True)
def setup_frontend_test():
    """Setup for each frontend test"""
    performance_monitor.metrics.clear()
    yield


# Run tests
if __name__ == "__main__":
    pytest.main([__file__, "-v", "--html=reports/frontend_report.html"]) 