"""
Test Helpers for Queue-it Integration Testing
Common utilities and helper functions for all test suites
"""

import json
import time
import requests
import random
import string
from typing import Dict, Any, Optional, List
from datetime import datetime, timedelta
from loguru import logger
import os


class TestConfig:
    """Configuration manager for test settings"""
    
    def __init__(self, config_file: str = "config/test_config.json"):
        self.config_file = config_file
        self.config = self._load_config()
    
    def _load_config(self) -> Dict[str, Any]:
        """Load test configuration from JSON file"""
        try:
            with open(self.config_file, 'r') as f:
                return json.load(f)
        except FileNotFoundError:
            logger.error(f"Configuration file not found: {self.config_file}")
            raise
        except json.JSONDecodeError as e:
            logger.error(f"Invalid JSON in configuration file: {e}")
            raise
    
    def get(self, key: str, default: Any = None) -> Any:
        """Get configuration value by key"""
        keys = key.split('.')
        value = self.config
        
        for k in keys:
            if isinstance(value, dict) and k in value:
                value = value[k]
            else:
                return default
        
        return value


class QueueItAPIClient:
    """Client for Queue-it API testing"""
    
    def __init__(self, base_url: str, timeout: int = 15):
        self.base_url = base_url.rstrip('/')
        self.timeout = timeout
        self.session = requests.Session()
        self.session.headers.update({
            'Content-Type': 'application/json',
            'User-Agent': 'QueueIt-Test-Client/1.0'
        })
    
    def health_check(self) -> Dict[str, Any]:
        """Check Queue-it API health"""
        url = f"{self.base_url}/api/queueit/health"
        response = self.session.get(url, timeout=self.timeout)
        return {
            'status_code': response.status_code,
            'data': response.json() if response.ok else None,
            'error': str(response.text) if not response.ok else None
        }
    
    def get_queue_status(self, event_id: str) -> Dict[str, Any]:
        """Get queue status for an event"""
        url = f"{self.base_url}/api/queueit/status/{event_id}"
        response = self.session.get(url, timeout=self.timeout)
        return {
            'status_code': response.status_code,
            'data': response.json() if response.ok else None,
            'error': str(response.text) if not response.ok else None
        }
    
    def enqueue_user(self, event_id: str, user_data: Dict[str, Any]) -> Dict[str, Any]:
        """Enqueue a user for an event"""
        url = f"{self.base_url}/api/queueit/enqueue"
        payload = {
            'eventId': event_id,
            'targetUrl': user_data.get('targetUrl', 'http://localhost/flash-sale'),
            'userAgent': user_data.get('userAgent', 'Mozilla/5.0 (Test Client)'),
            'ipAddress': user_data.get('ipAddress', '127.0.0.1')
        }
        
        response = self.session.post(url, json=payload, timeout=self.timeout)
        return {
            'status_code': response.status_code,
            'data': response.json() if response.ok else None,
            'error': str(response.text) if not response.ok else None
        }
    
    def check_position(self, event_id: str, queue_token: str) -> Dict[str, Any]:
        """Check user's position in queue"""
        url = f"{self.base_url}/api/queueit/position/{event_id}"
        headers = {'Authorization': f'Bearer {queue_token}'}
        response = self.session.get(url, headers=headers, timeout=self.timeout)
        return {
            'status_code': response.status_code,
            'data': response.json() if response.ok else None,
            'error': str(response.text) if not response.ok else None
        }
    
    def get_queue_stats(self, event_id: str) -> Dict[str, Any]:
        """Get queue statistics"""
        url = f"{self.base_url}/api/queueit/stats/{event_id}"
        response = self.session.get(url, timeout=self.timeout)
        return {
            'status_code': response.status_code,
            'data': response.json() if response.ok else None,
            'error': str(response.text) if not response.ok else None
        }


class PerformanceMonitor:
    """Monitor and collect performance metrics"""
    
    def __init__(self):
        self.metrics = []
    
    def start_timer(self) -> float:
        """Start a performance timer"""
        return time.time()
    
    def end_timer(self, start_time: float) -> float:
        """End a performance timer and return duration"""
        return time.time() - start_time
    
    def record_metric(self, name: str, value: float, tags: Dict[str, str] = None):
        """Record a performance metric"""
        metric = {
            'name': name,
            'value': value,
            'timestamp': datetime.now().isoformat(),
            'tags': tags or {}
        }
        self.metrics.append(metric)
        logger.debug(f"Recorded metric: {name} = {value}")
    
    def get_metrics(self, name: str = None) -> List[Dict[str, Any]]:
        """Get recorded metrics, optionally filtered by name"""
        if name:
            return [m for m in self.metrics if m['name'] == name]
        return self.metrics
    
    def calculate_statistics(self, name: str) -> Dict[str, float]:
        """Calculate statistics for a metric"""
        metrics = self.get_metrics(name)
        if not metrics:
            return {}
        
        values = [m['value'] for m in metrics]
        return {
            'count': len(values),
            'min': min(values),
            'max': max(values),
            'mean': sum(values) / len(values),
            'median': sorted(values)[len(values) // 2]
        }


class TestDataGenerator:
    """Generate test data for Queue-it testing"""
    
    @staticmethod
    def generate_user_data() -> Dict[str, str]:
        """Generate random user data"""
        return {
            'targetUrl': f'http://localhost/flash-sale?user={random.randint(1000, 9999)}',
            'userAgent': f'Mozilla/5.0 (Test Client {random.randint(1, 100)})',
            'ipAddress': f'192.168.1.{random.randint(1, 254)}'
        }
    
    @staticmethod
    def generate_queue_token() -> str:
        """Generate a random queue token"""
        return ''.join(random.choices(string.ascii_letters + string.digits, k=32))
    
    @staticmethod
    def generate_event_id() -> str:
        """Generate a random event ID"""
        events = ['flash-sale', 'black-friday', 'high-traffic', 'checkout']
        event = random.choice(events)
        timestamp = int(time.time())
        return f"{event}-{timestamp}"


class RetryHelper:
    """Helper for retrying failed operations"""
    
    def __init__(self, max_retries: int = 3, delay: float = 1.0, backoff_factor: float = 2.0):
        self.max_retries = max_retries
        self.delay = delay
        self.backoff_factor = backoff_factor
    
    def retry(self, func, *args, **kwargs):
        """Retry a function with exponential backoff"""
        last_exception = None
        
        for attempt in range(self.max_retries + 1):
            try:
                return func(*args, **kwargs)
            except Exception as e:
                last_exception = e
                if attempt < self.max_retries:
                    wait_time = self.delay * (self.backoff_factor ** attempt)
                    logger.warning(f"Attempt {attempt + 1} failed: {e}. Retrying in {wait_time}s...")
                    time.sleep(wait_time)
                else:
                    logger.error(f"All {self.max_retries + 1} attempts failed. Last error: {e}")
        
        raise last_exception


class AssertionHelper:
    """Helper for common test assertions"""
    
    @staticmethod
    def assert_api_response(response: Dict[str, Any], expected_status: int = 200):
        """Assert API response is successful"""
        assert response['status_code'] == expected_status, \
            f"Expected status {expected_status}, got {response['status_code']}"
        assert response['data'] is not None, "Response data should not be None"
        assert response['error'] is None, f"Unexpected error: {response['error']}"
    
    @staticmethod
    def assert_queue_status(status_data: Dict[str, Any], expected_active: bool = True):
        """Assert queue status is as expected"""
        assert 'isActive' in status_data, "Queue status should contain 'isActive' field"
        assert status_data['isActive'] == expected_active, \
            f"Expected queue active={expected_active}, got {status_data['isActive']}"
    
    @staticmethod
    def assert_performance_metric(metric_value: float, threshold: float, metric_name: str):
        """Assert performance metric meets threshold"""
        assert metric_value <= threshold, \
            f"{metric_name} ({metric_value}) exceeds threshold ({threshold})"


class ReportGenerator:
    """Generate test reports"""
    
    def __init__(self, output_dir: str = "reports"):
        self.output_dir = output_dir
        os.makedirs(output_dir, exist_ok=True)
    
    def generate_html_report(self, test_results: List[Dict[str, Any]], filename: str = "test_report.html"):
        """Generate HTML test report"""
        template = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Queue-it Integration Test Report</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; }
                .header { background: #667eea; color: white; padding: 20px; border-radius: 8px; }
                .summary { background: #f8f9fa; padding: 20px; margin: 20px 0; border-radius: 8px; }
                .test-result { margin: 10px 0; padding: 10px; border-radius: 4px; }
                .pass { background: #d4edda; border: 1px solid #c3e6cb; }
                .fail { background: #f8d7da; border: 1px solid #f5c6cb; }
                .metric { background: #e3f2fd; padding: 10px; margin: 5px 0; border-radius: 4px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Queue-it Integration Test Report</h1>
                <p>Generated on: {timestamp}</p>
            </div>
            
            <div class="summary">
                <h2>Test Summary</h2>
                <p>Total Tests: {total_tests}</p>
                <p>Passed: {passed_tests}</p>
                <p>Failed: {failed_tests}</p>
                <p>Success Rate: {success_rate}%</p>
            </div>
            
            <h2>Test Results</h2>
            {test_results_html}
        </body>
        </html>
        """
        
        # Calculate summary statistics
        total_tests = len(test_results)
        passed_tests = sum(1 for result in test_results if result.get('status') == 'PASS')
        failed_tests = total_tests - passed_tests
        success_rate = (passed_tests / total_tests * 100) if total_tests > 0 else 0
        
        # Generate test results HTML
        test_results_html = ""
        for result in test_results:
            status_class = "pass" if result.get('status') == 'PASS' else "fail"
            test_results_html += f"""
            <div class="test-result {status_class}">
                <h3>{result.get('name', 'Unknown Test')}</h3>
                <p><strong>Status:</strong> {result.get('status', 'UNKNOWN')}</p>
                <p><strong>Duration:</strong> {result.get('duration', 0):.2f}s</p>
                <p><strong>Message:</strong> {result.get('message', '')}</p>
            </div>
            """
        
        # Fill template
        html_content = template.format(
            timestamp=datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            total_tests=total_tests,
            passed_tests=passed_tests,
            failed_tests=failed_tests,
            success_rate=f"{success_rate:.1f}",
            test_results_html=test_results_html
        )
        
        # Write to file
        filepath = os.path.join(self.output_dir, filename)
        with open(filepath, 'w') as f:
            f.write(html_content)
        
        logger.info(f"HTML report generated: {filepath}")
        return filepath
    
    def generate_json_report(self, test_results: List[Dict[str, Any]], filename: str = "test_report.json"):
        """Generate JSON test report"""
        report = {
            'metadata': {
                'generated_at': datetime.now().isoformat(),
                'total_tests': len(test_results),
                'passed_tests': sum(1 for r in test_results if r.get('status') == 'PASS'),
                'failed_tests': sum(1 for r in test_results if r.get('status') == 'FAIL')
            },
            'results': test_results
        }
        
        filepath = os.path.join(self.output_dir, filename)
        with open(filepath, 'w') as f:
            json.dump(report, f, indent=2)
        
        logger.info(f"JSON report generated: {filepath}")
        return filepath


# Global instances
config = TestConfig()
api_client = QueueItAPIClient(config.get('urls.base_url'))
performance_monitor = PerformanceMonitor()
retry_helper = RetryHelper(
    max_retries=config.get('retry_config.max_retries', 3),
    delay=config.get('retry_config.retry_delay', 2),
    backoff_factor=config.get('retry_config.backoff_factor', 2)
)
report_generator = ReportGenerator(config.get('reporting.output_dir', './reports')) 