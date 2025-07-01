#!/usr/bin/env python3
"""
Queue-It Official Connector Functional Tests
Tests the Queue-It integration using the official Java connector
"""

import requests
import json
import time
import pytest
from typing import Dict, Any
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class QueueItOfficialConnectorTests:
    """Test suite for Queue-It official connector integration"""
    
    def __init__(self, base_url: str = "http://localhost:8081"):
        self.base_url = base_url
        self.api_base = f"{base_url}/api/queueit"
        self.session = requests.Session()
        self.session.headers.update({
            'Content-Type': 'application/json',
            'User-Agent': 'QueueIt-Test-Suite/1.0'
        })
        
        # Test configuration
        self.test_events = {
            'flash_sale': 'flash-sale-2024',
            'black_friday': 'black-friday-2024',
            'high_traffic': 'high-traffic-protection',
            'checkout': 'checkout-protection'
        }
        
        # Test data - Updated to use HTTPS frontend URL
        self.test_user_data = {
            'eventId': 'flash-sale-2024',
            'targetUrl': 'https://localhost/flash-sale',
            'userAgent': 'Mozilla/5.0 (Test Browser)',
            'ipAddress': '127.0.0.1'
        }

    def test_health_check(self) -> Dict[str, Any]:
        """Test Queue-It health check endpoint"""
        logger.info("Testing Queue-It health check...")
        
        try:
            response = self.session.get(f"{self.api_base}/health")
            assert response.status_code == 200, f"Health check failed: {response.status_code}"
            
            data = response.json()
            assert data['status'] == 'healthy', f"Health status not healthy: {data['status']}"
            assert data['service'] == 'queueit-integration', f"Service name mismatch: {data['service']}"
            assert 'connector' in data, "Connector information missing"
            
            logger.info("✅ Health check passed")
            return {'status': 'PASS', 'data': data}
            
        except Exception as e:
            logger.error(f"❌ Health check failed: {str(e)}")
            return {'status': 'FAIL', 'error': str(e)}

    def test_queue_status_check(self, event_id: str = None) -> Dict[str, Any]:
        """Test queue status check for events"""
        event_id = event_id or self.test_events['flash_sale']
        logger.info(f"Testing queue status check for event: {event_id}")
        
        try:
            response = self.session.get(f"{self.api_base}/status/{event_id}")
            assert response.status_code == 200, f"Status check failed: {response.status_code}"
            
            data = response.json()
            assert 'isActive' in data, "isActive field missing"
            assert 'queueSize' in data, "queueSize field missing"
            assert 'estimatedWaitTime' in data, "estimatedWaitTime field missing"
            assert data['eventId'] == event_id, f"Event ID mismatch: {data['eventId']}"
            
            logger.info(f"✅ Queue status check passed for {event_id}")
            return {'status': 'PASS', 'data': data}
            
        except Exception as e:
            logger.error(f"❌ Queue status check failed for {event_id}: {str(e)}")
            return {'status': 'FAIL', 'error': str(e)}

    def test_enqueue_user(self, event_id: str = None) -> Dict[str, Any]:
        """Test user enqueue functionality"""
        event_id = event_id or self.test_events['flash_sale']
        logger.info(f"Testing user enqueue for event: {event_id}")
        
        try:
            enqueue_data = {
                'eventId': event_id,
                'targetUrl': f'https://localhost/{event_id}',
                'userAgent': 'Mozilla/5.0 (Test Browser)',
                'ipAddress': '127.0.0.1'
            }
            
            response = self.session.post(f"{self.api_base}/enqueue", json=enqueue_data)
            assert response.status_code == 200, f"Enqueue failed: {response.status_code}"
            
            data = response.json()
            assert 'redirectUrl' in data, "redirectUrl field missing"
            assert data['eventId'] == event_id, f"Event ID mismatch: {data['eventId']}"
            assert 'timestamp' in data, "timestamp field missing"
            
            # Validate redirect URL format
            redirect_url = data['redirectUrl']
            assert 'queue-it.net' in redirect_url, f"Invalid redirect URL: {redirect_url}"
            assert event_id in redirect_url, f"Event ID not in redirect URL: {redirect_url}"
            
            logger.info(f"✅ User enqueue passed for {event_id}")
            return {'status': 'PASS', 'data': data}
            
        except Exception as e:
            logger.error(f"❌ User enqueue failed for {event_id}: {str(e)}")
            return {'status': 'FAIL', 'error': str(e)}

    def test_token_validation(self, event_id: str = None) -> Dict[str, Any]:
        """Test Queue-It token validation"""
        event_id = event_id or self.test_events['flash_sale']
        logger.info(f"Testing token validation for event: {event_id}")
        
        try:
            # First enqueue to get a token
            enqueue_result = self.test_enqueue_user(event_id)
            if enqueue_result['status'] != 'PASS':
                return {'status': 'SKIP', 'reason': 'Enqueue failed, cannot test validation'}
            
            # Extract token from redirect URL (simulated)
            test_token = "test-queue-token-12345"
            
            validation_data = {
                'eventId': event_id,
                'queueitToken': test_token,
                'originalUrl': f'https://localhost/{event_id}'
            }
            
            response = self.session.post(f"{self.api_base}/validate", json=validation_data)
            assert response.status_code == 200, f"Token validation failed: {response.status_code}"
            
            data = response.json()
            assert 'redirect' in data, "redirect field missing"
            assert data['eventId'] == event_id, f"Event ID mismatch: {data['eventId']}"
            assert 'timestamp' in data, "timestamp field missing"
            
            logger.info(f"✅ Token validation passed for {event_id}")
            return {'status': 'PASS', 'data': data}
            
        except Exception as e:
            logger.error(f"❌ Token validation failed for {event_id}: {str(e)}")
            return {'status': 'FAIL', 'error': str(e)}

    def test_queue_position_check(self, event_id: str = None) -> Dict[str, Any]:
        """Test queue position checking"""
        event_id = event_id or self.test_events['flash_sale']
        logger.info(f"Testing queue position check for event: {event_id}")
        
        try:
            test_token = "test-queue-token-12345"
            
            response = self.session.get(
                f"{self.api_base}/position/{event_id}",
                params={'queueitToken': test_token}
            )
            assert response.status_code == 200, f"Position check failed: {response.status_code}"
            
            data = response.json()
            assert 'eventId' in data, "eventId field missing"
            assert 'position' in data, "position field missing"
            assert 'estimatedWaitTime' in data, "estimatedWaitTime field missing"
            assert data['eventId'] == event_id, f"Event ID mismatch: {data['eventId']}"
            
            logger.info(f"✅ Queue position check passed for {event_id}")
            return {'status': 'PASS', 'data': data}
            
        except Exception as e:
            logger.error(f"❌ Queue position check failed for {event_id}: {str(e)}")
            return {'status': 'FAIL', 'error': str(e)}

    def test_queue_stats(self, event_id: str = None) -> Dict[str, Any]:
        """Test queue statistics endpoint"""
        event_id = event_id or self.test_events['flash_sale']
        logger.info(f"Testing queue stats for event: {event_id}")
        
        try:
            response = self.session.get(f"{self.api_base}/stats/{event_id}")
            assert response.status_code == 200, f"Stats check failed: {response.status_code}"
            
            data = response.json()
            assert 'eventId' in data, "eventId field missing"
            assert 'queueSize' in data, "queueSize field missing"
            assert 'estimatedWaitTime' in data, "estimatedWaitTime field missing"
            assert 'isActive' in data, "isActive field missing"
            assert data['eventId'] == event_id, f"Event ID mismatch: {data['eventId']}"
            
            logger.info(f"✅ Queue stats check passed for {event_id}")
            return {'status': 'PASS', 'data': data}
            
        except Exception as e:
            logger.error(f"❌ Queue stats check failed for {event_id}: {str(e)}")
            return {'status': 'FAIL', 'error': str(e)}

    def test_all_events(self) -> Dict[str, Any]:
        """Test all configured events"""
        logger.info("Testing all configured events...")
        
        results = {}
        for event_name, event_id in self.test_events.items():
            logger.info(f"Testing event: {event_name} ({event_id})")
            
            event_results = {
                'status': self.test_queue_status_check(event_id),
                'enqueue': self.test_enqueue_user(event_id),
                'stats': self.test_queue_stats(event_id)
            }
            
            results[event_name] = event_results
            
            # Brief pause between events
            time.sleep(1)
        
        # Calculate overall status
        all_passed = all(
            all(result['status'] == 'PASS' for result in event_results.values())
            for event_results in results.values()
        )
        
        overall_status = 'PASS' if all_passed else 'FAIL'
        
        logger.info(f"✅ All events test completed: {overall_status}")
        return {'status': overall_status, 'results': results}

    def test_error_handling(self) -> Dict[str, Any]:
        """Test error handling scenarios"""
        logger.info("Testing error handling...")
        
        error_tests = [
            {
                'name': 'invalid_event_id',
                'endpoint': f"{self.api_base}/status/invalid-event-123",
                'method': 'GET',
                'expected_status': 500
            },
            {
                'name': 'missing_required_fields',
                'endpoint': f"{self.api_base}/enqueue",
                'method': 'POST',
                'data': {'eventId': 'test-event'},
                'expected_status': 500
            },
            {
                'name': 'invalid_token',
                'endpoint': f"{self.api_base}/position/test-event",
                'method': 'GET',
                'params': {'queueitToken': 'invalid-token'},
                'expected_status': 500
            }
        ]
        
        results = {}
        for test in error_tests:
            try:
                if test['method'] == 'GET':
                    response = self.session.get(
                        test['endpoint'],
                        params=test.get('params', {})
                    )
                else:
                    response = self.session.post(
                        test['endpoint'],
                        json=test.get('data', {})
                    )
                
                # Error endpoints should return error status
                assert response.status_code >= 400, f"Expected error status, got: {response.status_code}"
                
                results[test['name']] = {'status': 'PASS', 'status_code': response.status_code}
                logger.info(f"✅ Error test passed: {test['name']}")
                
            except Exception as e:
                results[test['name']] = {'status': 'FAIL', 'error': str(e)}
                logger.error(f"❌ Error test failed: {test['name']}: {str(e)}")
        
        all_passed = all(result['status'] == 'PASS' for result in results.values())
        overall_status = 'PASS' if all_passed else 'FAIL'
        
        logger.info(f"✅ Error handling test completed: {overall_status}")
        return {'status': overall_status, 'results': results}

    def run_comprehensive_test_suite(self) -> Dict[str, Any]:
        """Run the complete test suite"""
        logger.info("🚀 Starting Queue-It Official Connector Comprehensive Test Suite")
        
        test_results = {
            'health_check': self.test_health_check(),
            'all_events': self.test_all_events(),
            'token_validation': self.test_token_validation(),
            'queue_position': self.test_queue_position_check(),
            'error_handling': self.test_error_handling()
        }
        
        # Calculate overall results
        passed_tests = sum(1 for result in test_results.values() if result['status'] == 'PASS')
        total_tests = len(test_results)
        
        overall_status = 'PASS' if passed_tests == total_tests else 'FAIL'
        
        summary = {
            'overall_status': overall_status,
            'passed_tests': passed_tests,
            'total_tests': total_tests,
            'success_rate': f"{(passed_tests/total_tests)*100:.1f}%",
            'test_results': test_results,
            'timestamp': time.strftime('%Y-%m-%d %H:%M:%S')
        }
        
        logger.info(f"🎯 Test Suite Complete: {overall_status}")
        logger.info(f"📊 Results: {passed_tests}/{total_tests} tests passed ({summary['success_rate']})")
        
        return summary

def main():
    """Main test runner"""
    # Initialize test suite
    test_suite = QueueItOfficialConnectorTests()
    
    # Run comprehensive tests
    results = test_suite.run_comprehensive_test_suite()
    
    # Print detailed results
    print("\n" + "="*60)
    print("QUEUE-IT OFFICIAL CONNECTOR TEST RESULTS")
    print("="*60)
    print(f"Overall Status: {results['overall_status']}")
    print(f"Tests Passed: {results['passed_tests']}/{results['total_tests']}")
    print(f"Success Rate: {results['success_rate']}")
    print(f"Timestamp: {results['timestamp']}")
    print("="*60)
    
    # Print detailed results
    for test_name, result in results['test_results'].items():
        status_icon = "✅" if result['status'] == 'PASS' else "❌"
        print(f"{status_icon} {test_name}: {result['status']}")
    
    print("="*60)
    
    # Return exit code
    return 0 if results['overall_status'] == 'PASS' else 1

if __name__ == "__main__":
    exit(main()) 