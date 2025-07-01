#!/usr/bin/env python3
"""
Queue-It Localhost Test Runner
Runs Queue-It official connector tests against localhost deployment
"""

import requests
import time
import json
import os
from typing import Dict, Any

class QueueItLocalhostTests:
    """Test suite for Queue-It localhost deployment"""
    
    def __init__(self):
        self.backend_url = "http://localhost:8081"
        self.frontend_url = "https://localhost"
        self.api_base = f"{self.backend_url}/api/queueit"
        self.test_results = []
        
        # Load configuration from environment or use defaults
        self.customer_id = os.getenv('QUEUEIT_CUSTOMER_ID', 'your-customer-id')
        self.secret_key = os.getenv('QUEUEIT_SECRET_KEY', 'your-secret-key')
        self.api_key = os.getenv('QUEUEIT_API_KEY', 'your-api-key')
        
        self.test_events = {
            'flash_sale': 'flash-sale-2024',
            'black_friday': 'black-friday-2024',
            'checkout': 'checkout-protection',
            'high_traffic': 'high-traffic-protection'
        }

    def log_test_result(self, test_name: str, status: str, details: str = None):
        """Log test result"""
        result = {
            'test': test_name,
            'status': status,
            'timestamp': time.strftime('%Y-%m-%d %H:%M:%S'),
            'details': details
        }
        self.test_results.append(result)
        
        icon = "✅" if status == 'PASS' else "❌" if status == 'FAIL' else "⚠️"
        print(f"{icon} {test_name}: {status}")
        if details:
            print(f"   Details: {details}")

    def test_service_availability(self) -> bool:
        """Test if services are running"""
        print("🔍 Testing service availability...")
        
        # Test backend
        try:
            response = requests.get(f"{self.backend_url}/actuator/health", timeout=5)
            if response.status_code != 200:
                self.log_test_result("Backend Service", "FAIL", f"Backend not responding: {response.status_code}")
                return False
        except Exception as e:
            self.log_test_result("Backend Service", "FAIL", f"Backend not available: {str(e)}")
            return False
        
        # Test frontend
        try:
            response = requests.get(self.frontend_url, timeout=5)
            if response.status_code != 200:
                self.log_test_result("Frontend Service", "FAIL", f"Frontend not responding: {response.status_code}")
                return False
        except Exception as e:
            self.log_test_result("Frontend Service", "FAIL", f"Frontend not available: {str(e)}")
            return False
        
        self.log_test_result("Service Availability", "PASS", "Both services are running")
        return True

    def test_queueit_health(self) -> bool:
        """Test Queue-It health endpoint"""
        print("🔍 Testing Queue-It health...")
        
        try:
            response = requests.get(f"{self.api_base}/health", timeout=10)
            if response.status_code == 200:
                data = response.json()
                connector_type = data.get('connector', 'unknown')
                self.log_test_result("Queue-It Health", "PASS", f"Connector: {connector_type}")
                return True
            else:
                self.log_test_result("Queue-It Health", "FAIL", f"HTTP {response.status_code}")
                return False
        except Exception as e:
            self.log_test_result("Queue-It Health", "FAIL", str(e))
            return False

    def test_queueit_configuration(self) -> bool:
        """Test Queue-It configuration"""
        print("🔍 Testing Queue-It configuration...")
        
        # Check if credentials are configured
        if self.customer_id == 'your-customer-id':
            self.log_test_result("Queue-It Configuration", "WARN", "Using default credentials - update with real values")
            return False
        
        self.log_test_result("Queue-It Configuration", "PASS", f"Customer ID: {self.customer_id}")
        return True

    def test_queue_status(self) -> bool:
        """Test queue status endpoint"""
        print("🔍 Testing queue status...")
        
        for event_name, event_id in self.test_events.items():
            try:
                response = requests.get(f"{self.api_base}/status/{event_id}", timeout=10)
                if response.status_code == 200:
                    data = response.json()
                    self.log_test_result(f"Queue Status - {event_name}", "PASS", 
                                        f"Active: {data.get('isActive', False)}")
                else:
                    self.log_test_result(f"Queue Status - {event_name}", "FAIL", 
                                        f"HTTP {response.status_code}")
            except Exception as e:
                self.log_test_result(f"Queue Status - {event_name}", "FAIL", str(e))
        
        return True

    def test_enqueue_functionality(self) -> bool:
        """Test enqueue functionality"""
        print("🔍 Testing enqueue functionality...")
        
        test_data = {
            'eventId': 'flash-sale-2024',
            'targetUrl': f'{self.frontend_url}/flash-sale',
            'userAgent': 'Test Browser',
            'ipAddress': '127.0.0.1'
        }
        
        try:
            response = requests.post(f"{self.api_base}/enqueue", json=test_data, timeout=10)
            if response.status_code == 200:
                data = response.json()
                redirect_url = data.get('redirectUrl', 'N/A')
                self.log_test_result("Enqueue Functionality", "PASS", 
                                   f"Redirect URL: {redirect_url[:50]}...")
                return True
            else:
                self.log_test_result("Enqueue Functionality", "FAIL", f"HTTP {response.status_code}")
                return False
        except Exception as e:
            self.log_test_result("Enqueue Functionality", "FAIL", str(e))
            return False

    def test_token_validation(self) -> bool:
        """Test token validation"""
        print("🔍 Testing token validation...")
        
        test_data = {
            'eventId': 'flash-sale-2024',
            'queueitToken': 'test-token-12345',
            'originalUrl': f'{self.frontend_url}/flash-sale'
        }
        
        try:
            response = requests.post(f"{self.api_base}/validate", json=test_data, timeout=10)
            if response.status_code == 200:
                data = response.json()
                self.log_test_result("Token Validation", "PASS", 
                                   f"Redirect: {data.get('redirect', False)}")
                return True
            else:
                self.log_test_result("Token Validation", "FAIL", f"HTTP {response.status_code}")
                return False
        except Exception as e:
            self.log_test_result("Token Validation", "FAIL", str(e))
            return False

    def test_error_handling(self) -> bool:
        """Test error handling"""
        print("🔍 Testing error handling...")
        
        # Test with invalid event
        try:
            response = requests.get(f"{self.api_base}/status/invalid-event-123", timeout=10)
            if response.status_code >= 400:
                self.log_test_result("Error Handling", "PASS", f"Proper error status: {response.status_code}")
                return True
            else:
                self.log_test_result("Error Handling", "FAIL", f"Expected error, got: {response.status_code}")
                return False
        except Exception as e:
            self.log_test_result("Error Handling", "FAIL", str(e))
            return False

    def test_performance(self) -> bool:
        """Test performance"""
        print("🔍 Testing performance...")
        
        start_time = time.time()
        try:
            response = requests.get(f"{self.api_base}/health", timeout=5)
            response_time = (time.time() - start_time) * 1000  # Convert to milliseconds
            
            if response_time < 1000:  # Less than 1 second
                self.log_test_result("Performance", "PASS", f"Response time: {response_time:.2f}ms")
                return True
            else:
                self.log_test_result("Performance", "WARN", f"Slow response: {response_time:.2f}ms")
                return False
        except Exception as e:
            self.log_test_result("Performance", "FAIL", str(e))
            return False

    def run_comprehensive_tests(self) -> Dict[str, Any]:
        """Run all tests"""
        print("🚀 Starting Queue-It Localhost Test Suite")
        print("=" * 60)
        
        # Check if services are running first
        if not self.test_service_availability():
            print("❌ Services not available. Please start your backend and frontend services.")
            print("   Backend: http://localhost:8081")
            print("   Frontend: https://localhost")
            return {'status': 'FAIL', 'reason': 'Services not available'}
        
        # Run all tests
        tests = [
            self.test_queueit_health(),
            self.test_queueit_configuration(),
            self.test_queue_status(),
            self.test_enqueue_functionality(),
            self.test_token_validation(),
            self.test_error_handling(),
            self.test_performance()
        ]
        
        # Calculate results
        passed_tests = sum(1 for result in self.test_results if result['status'] == 'PASS')
        total_tests = len(self.test_results)
        success_rate = (passed_tests / total_tests) * 100 if total_tests > 0 else 0
        
        summary = {
            'overall_status': 'PASS' if passed_tests == total_tests else 'FAIL',
            'passed_tests': passed_tests,
            'total_tests': total_tests,
            'success_rate': f"{success_rate:.1f}%",
            'test_results': self.test_results,
            'timestamp': time.strftime('%Y-%m-%d %H:%M:%S')
        }
        
        # Print summary
        print("\n" + "=" * 60)
        print("QUEUE-IT LOCALHOST TEST RESULTS")
        print("=" * 60)
        print(f"Overall Status: {summary['overall_status']}")
        print(f"Tests Passed: {passed_tests}/{total_tests}")
        print(f"Success Rate: {summary['success_rate']}")
        print(f"Timestamp: {summary['timestamp']}")
        print("=" * 60)
        
        # Save results
        with open('localhost_test_results.json', 'w') as f:
            json.dump(summary, f, indent=2)
        
        return summary

def main():
    """Main function"""
    print("🔧 Queue-It Localhost Test Runner")
    print("=" * 40)
    
    # Check if services are running
    print("📋 Prerequisites:")
    print("   1. Backend running on http://localhost:8081")
    print("   2. Frontend running on https://localhost")
    print("   3. Queue-It credentials configured")
    print()
    
    # Run tests
    test_suite = QueueItLocalhostTests()
    results = test_suite.run_comprehensive_tests()
    
    # Exit with appropriate code
    exit_code = 0 if results['overall_status'] == 'PASS' else 1
    sys.exit(exit_code)

if __name__ == "__main__":
    import sys
    main() 