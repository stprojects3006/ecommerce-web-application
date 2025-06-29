"""
Backend API Tests for Queue-it Integration
Test all Queue-it API endpoints and functionality
"""

import pytest
import time
import json
from typing import Dict, Any
from datetime import datetime, timedelta

from utils.test_helpers import (
    api_client, config, performance_monitor, retry_helper,
    AssertionHelper, TestDataGenerator
)
from utils.grafana_client import metrics_collector


class TestQueueItAPIHealth:
    """Test Queue-it API health endpoint"""
    
    def test_health_check(self):
        """Test API health check endpoint"""
        start_time = performance_monitor.start_timer()
        
        try:
            response = api_client.health_check()
            
            # Assert response
            AssertionHelper.assert_api_response(response, 200)
            
            # Verify health data
            health_data = response['data']
            assert 'status' in health_data, "Health response should contain status"
            assert health_data['status'] == 'UP', "Health status should be UP"
            assert 'timestamp' in health_data, "Health response should contain timestamp"
            
            # Record performance metric
            duration = performance_monitor.end_timer(start_time)
            performance_monitor.record_metric('api_health_check_duration', duration)
            
            # Send to Grafana
            metrics_collector.record_test_metric(
                'api_health_check_duration',
                duration,
                {'endpoint': 'health', 'status': 'success'}
            )
            
        except Exception as e:
            duration = performance_monitor.end_timer(start_time)
            performance_monitor.record_metric('api_health_check_duration', duration)
            metrics_collector.record_test_metric(
                'api_health_check_duration',
                duration,
                {'endpoint': 'health', 'status': 'error', 'error': str(e)}
            )
            raise
    
    def test_health_check_with_retry(self):
        """Test health check with retry mechanism"""
        def health_check_func():
            response = api_client.health_check()
            AssertionHelper.assert_api_response(response, 200)
            return response
        
        response = retry_helper.retry(health_check_func)
        assert response['status_code'] == 200


class TestQueueItQueueStatus:
    """Test Queue-it queue status functionality"""
    
    @pytest.mark.parametrize("event_id", [
        "flash-sale-2024",
        "black-friday-2024",
        "high-traffic-protection",
        "checkout-protection"
    ])
    def test_get_queue_status(self, event_id):
        """Test getting queue status for different events"""
        start_time = performance_monitor.start_timer()
        
        try:
            response = api_client.get_queue_status(event_id)
            
            # Assert response
            AssertionHelper.assert_api_response(response, 200)
            
            # Verify status data structure
            status_data = response['data']
            assert 'isActive' in status_data, "Status should contain isActive field"
            assert 'eventId' in status_data, "Status should contain eventId field"
            assert status_data['eventId'] == event_id, "Event ID should match"
            
            # Record performance metric
            duration = performance_monitor.end_timer(start_time)
            performance_monitor.record_metric('api_queue_status_duration', duration)
            
            metrics_collector.record_test_metric(
                'api_queue_status_duration',
                duration,
                {'endpoint': 'status', 'event_id': event_id, 'status': 'success'}
            )
            
        except Exception as e:
            duration = performance_monitor.end_timer(start_time)
            performance_monitor.record_metric('api_queue_status_duration', duration)
            metrics_collector.record_test_metric(
                'api_queue_status_duration',
                duration,
                {'endpoint': 'status', 'event_id': event_id, 'status': 'error', 'error': str(e)}
            )
            raise
    
    def test_get_queue_status_invalid_event(self):
        """Test getting queue status for invalid event ID"""
        invalid_event_id = "invalid-event-12345"
        
        response = api_client.get_queue_status(invalid_event_id)
        
        # Should return 404 for invalid event
        assert response['status_code'] == 404, "Should return 404 for invalid event"
        assert response['error'] is not None, "Should contain error message"


class TestQueueItEnqueue:
    """Test Queue-it user enqueueing functionality"""
    
    def test_enqueue_user_success(self):
        """Test successful user enqueueing"""
        start_time = performance_monitor.start_timer()
        
        try:
            # Generate test user data
            user_data = TestDataGenerator.generate_user_data()
            event_id = "flash-sale-2024"
            
            response = api_client.enqueue_user(event_id, user_data)
            
            # Assert response
            AssertionHelper.assert_api_response(response, 200)
            
            # Verify enqueue data
            enqueue_data = response['data']
            assert 'queueToken' in enqueue_data, "Response should contain queue token"
            assert 'eventId' in enqueue_data, "Response should contain event ID"
            assert enqueue_data['eventId'] == event_id, "Event ID should match"
            assert 'position' in enqueue_data, "Response should contain position"
            assert enqueue_data['position'] >= 0, "Position should be non-negative"
            
            # Record performance metric
            duration = performance_monitor.end_timer(start_time)
            performance_monitor.record_metric('api_enqueue_duration', duration)
            
            metrics_collector.record_test_metric(
                'api_enqueue_duration',
                duration,
                {'endpoint': 'enqueue', 'event_id': event_id, 'status': 'success'}
            )
            
            return enqueue_data['queueToken']
            
        except Exception as e:
            duration = performance_monitor.end_timer(start_time)
            performance_monitor.record_metric('api_enqueue_duration', duration)
            metrics_collector.record_test_metric(
                'api_enqueue_duration',
                duration,
                {'endpoint': 'enqueue', 'event_id': event_id, 'status': 'error', 'error': str(e)}
            )
            raise
    
    def test_enqueue_user_invalid_event(self):
        """Test enqueueing user for invalid event"""
        user_data = TestDataGenerator.generate_user_data()
        invalid_event_id = "invalid-event-12345"
        
        response = api_client.enqueue_user(invalid_event_id, user_data)
        
        # Should return 404 for invalid event
        assert response['status_code'] == 404, "Should return 404 for invalid event"
        assert response['error'] is not None, "Should contain error message"
    
    def test_enqueue_user_missing_data(self):
        """Test enqueueing user with missing required data"""
        event_id = "flash-sale-2024"
        
        # Test with missing targetUrl
        user_data = {
            'userAgent': 'Mozilla/5.0 (Test Client)',
            'ipAddress': '127.0.0.1'
        }
        
        response = api_client.enqueue_user(event_id, user_data)
        
        # Should return 400 for missing required data
        assert response['status_code'] == 400, "Should return 400 for missing data"
        assert response['error'] is not None, "Should contain error message"


class TestQueueItPosition:
    """Test Queue-it position checking functionality"""
    
    def test_check_position_success(self):
        """Test successful position checking"""
        start_time = performance_monitor.start_timer()
        
        try:
            # First enqueue a user to get a token
            user_data = TestDataGenerator.generate_user_data()
            event_id = "flash-sale-2024"
            
            enqueue_response = api_client.enqueue_user(event_id, user_data)
            AssertionHelper.assert_api_response(enqueue_response, 200)
            
            queue_token = enqueue_response['data']['queueToken']
            
            # Now check position
            response = api_client.check_position(event_id, queue_token)
            
            # Assert response
            AssertionHelper.assert_api_response(response, 200)
            
            # Verify position data
            position_data = response['data']
            assert 'position' in position_data, "Response should contain position"
            assert 'eventId' in position_data, "Response should contain event ID"
            assert position_data['eventId'] == event_id, "Event ID should match"
            assert position_data['position'] >= 0, "Position should be non-negative"
            
            # Record performance metric
            duration = performance_monitor.end_timer(start_time)
            performance_monitor.record_metric('api_position_check_duration', duration)
            
            metrics_collector.record_test_metric(
                'api_position_check_duration',
                duration,
                {'endpoint': 'position', 'event_id': event_id, 'status': 'success'}
            )
            
        except Exception as e:
            duration = performance_monitor.end_timer(start_time)
            performance_monitor.record_metric('api_position_check_duration', duration)
            metrics_collector.record_test_metric(
                'api_position_check_duration',
                duration,
                {'endpoint': 'position', 'event_id': event_id, 'status': 'error', 'error': str(e)}
            )
            raise
    
    def test_check_position_invalid_token(self):
        """Test position checking with invalid token"""
        event_id = "flash-sale-2024"
        invalid_token = TestDataGenerator.generate_queue_token()
        
        response = api_client.check_position(event_id, invalid_token)
        
        # Should return 401 for invalid token
        assert response['status_code'] == 401, "Should return 401 for invalid token"
        assert response['error'] is not None, "Should contain error message"
    
    def test_check_position_invalid_event(self):
        """Test position checking for invalid event"""
        event_id = "invalid-event-12345"
        token = TestDataGenerator.generate_queue_token()
        
        response = api_client.check_position(event_id, token)
        
        # Should return 404 for invalid event
        assert response['status_code'] == 404, "Should return 404 for invalid event"
        assert response['error'] is not None, "Should contain error message"


class TestQueueItStats:
    """Test Queue-it statistics functionality"""
    
    @pytest.mark.parametrize("event_id", [
        "flash-sale-2024",
        "black-friday-2024",
        "high-traffic-protection"
    ])
    def test_get_queue_stats(self, event_id):
        """Test getting queue statistics for different events"""
        start_time = performance_monitor.start_timer()
        
        try:
            response = api_client.get_queue_stats(event_id)
            
            # Assert response
            AssertionHelper.assert_api_response(response, 200)
            
            # Verify stats data structure
            stats_data = response['data']
            assert 'eventId' in stats_data, "Stats should contain eventId field"
            assert stats_data['eventId'] == event_id, "Event ID should match"
            assert 'totalUsers' in stats_data, "Stats should contain totalUsers field"
            assert 'activeUsers' in stats_data, "Stats should contain activeUsers field"
            assert 'averageWaitTime' in stats_data, "Stats should contain averageWaitTime field"
            
            # Verify data types and ranges
            assert isinstance(stats_data['totalUsers'], int), "totalUsers should be integer"
            assert isinstance(stats_data['activeUsers'], int), "activeUsers should be integer"
            assert isinstance(stats_data['averageWaitTime'], (int, float)), "averageWaitTime should be numeric"
            assert stats_data['totalUsers'] >= 0, "totalUsers should be non-negative"
            assert stats_data['activeUsers'] >= 0, "activeUsers should be non-negative"
            assert stats_data['averageWaitTime'] >= 0, "averageWaitTime should be non-negative"
            
            # Record performance metric
            duration = performance_monitor.end_timer(start_time)
            performance_monitor.record_metric('api_stats_duration', duration)
            
            metrics_collector.record_test_metric(
                'api_stats_duration',
                duration,
                {'endpoint': 'stats', 'event_id': event_id, 'status': 'success'}
            )
            
        except Exception as e:
            duration = performance_monitor.end_timer(start_time)
            performance_monitor.record_metric('api_stats_duration', duration)
            metrics_collector.record_test_metric(
                'api_stats_duration',
                duration,
                {'endpoint': 'stats', 'event_id': event_id, 'status': 'error', 'error': str(e)}
            )
            raise
    
    def test_get_queue_stats_invalid_event(self):
        """Test getting stats for invalid event"""
        invalid_event_id = "invalid-event-12345"
        
        response = api_client.get_queue_stats(invalid_event_id)
        
        # Should return 404 for invalid event
        assert response['status_code'] == 404, "Should return 404 for invalid event"
        assert response['error'] is not None, "Should contain error message"


class TestQueueItPerformance:
    """Test Queue-it API performance under load"""
    
    def test_concurrent_enqueue_requests(self):
        """Test multiple concurrent enqueue requests"""
        import concurrent.futures
        
        event_id = "flash-sale-2024"
        num_requests = 10
        results = []
        
        def make_enqueue_request():
            user_data = TestDataGenerator.generate_user_data()
            start_time = performance_monitor.start_timer()
            
            try:
                response = api_client.enqueue_user(event_id, user_data)
                duration = performance_monitor.end_timer(start_time)
                
                return {
                    'success': response['status_code'] == 200,
                    'duration': duration,
                    'status_code': response['status_code']
                }
            except Exception as e:
                duration = performance_monitor.end_timer(start_time)
                return {
                    'success': False,
                    'duration': duration,
                    'error': str(e)
                }
        
        # Execute concurrent requests
        with concurrent.futures.ThreadPoolExecutor(max_workers=num_requests) as executor:
            futures = [executor.submit(make_enqueue_request) for _ in range(num_requests)]
            results = [future.result() for future in concurrent.futures.as_completed(futures)]
        
        # Analyze results
        successful_requests = [r for r in results if r['success']]
        failed_requests = [r for r in results if not r['success']]
        
        # Record metrics
        if successful_requests:
            avg_duration = sum(r['duration'] for r in successful_requests) / len(successful_requests)
            max_duration = max(r['duration'] for r in successful_requests)
            min_duration = min(r['duration'] for r in successful_requests)
            
            performance_monitor.record_metric('concurrent_enqueue_avg_duration', avg_duration)
            performance_monitor.record_metric('concurrent_enqueue_max_duration', max_duration)
            performance_monitor.record_metric('concurrent_enqueue_min_duration', min_duration)
            
            metrics_collector.record_test_metric(
                'concurrent_enqueue_avg_duration',
                avg_duration,
                {'test_type': 'concurrent', 'num_requests': num_requests}
            )
        
        # Assertions
        success_rate = len(successful_requests) / num_requests
        assert success_rate >= 0.8, f"Success rate should be >= 80%, got {success_rate * 100}%"
        
        if successful_requests:
            avg_duration = sum(r['duration'] for r in successful_requests) / len(successful_requests)
            assert avg_duration <= 5.0, f"Average response time should be <= 5s, got {avg_duration}s"
    
    def test_api_response_time_thresholds(self):
        """Test API response times meet performance thresholds"""
        thresholds = {
            'health': 1.0,
            'status': 2.0,
            'enqueue': 3.0,
            'position': 2.0,
            'stats': 2.0
        }
        
        for endpoint, threshold in thresholds.items():
            start_time = performance_monitor.start_timer()
            
            try:
                if endpoint == 'health':
                    response = api_client.health_check()
                elif endpoint == 'status':
                    response = api_client.get_queue_status("flash-sale-2024")
                elif endpoint == 'enqueue':
                    user_data = TestDataGenerator.generate_user_data()
                    response = api_client.enqueue_user("flash-sale-2024", user_data)
                elif endpoint == 'position':
                    # First enqueue to get token
                    user_data = TestDataGenerator.generate_user_data()
                    enqueue_response = api_client.enqueue_user("flash-sale-2024", user_data)
                    if enqueue_response['status_code'] == 200:
                        token = enqueue_response['data']['queueToken']
                        response = api_client.check_position("flash-sale-2024", token)
                    else:
                        continue
                elif endpoint == 'stats':
                    response = api_client.get_queue_stats("flash-sale-2024")
                
                duration = performance_monitor.end_timer(start_time)
                
                # Assert performance threshold
                AssertionHelper.assert_performance_metric(
                    duration, threshold, f"{endpoint} response time"
                )
                
                # Record metric
                performance_monitor.record_metric(f'{endpoint}_response_time', duration)
                
            except Exception as e:
                duration = performance_monitor.end_timer(start_time)
                performance_monitor.record_metric(f'{endpoint}_response_time', duration)
                logger.error(f"Error testing {endpoint}: {e}")


class TestQueueItErrorHandling:
    """Test Queue-it API error handling"""
    
    def test_malformed_request_data(self):
        """Test API behavior with malformed request data"""
        event_id = "flash-sale-2024"
        
        # Test with malformed JSON
        malformed_data = {
            'eventId': event_id,
            'targetUrl': None,  # Invalid null value
            'userAgent': '',    # Empty string
            'ipAddress': 'invalid-ip'  # Invalid IP format
        }
        
        response = api_client.enqueue_user(event_id, malformed_data)
        
        # Should return 400 for malformed data
        assert response['status_code'] == 400, "Should return 400 for malformed data"
        assert response['error'] is not None, "Should contain error message"
    
    def test_rate_limiting(self):
        """Test API rate limiting behavior"""
        event_id = "flash-sale-2024"
        
        # Make rapid requests to trigger rate limiting
        responses = []
        for i in range(20):  # Make 20 rapid requests
            user_data = TestDataGenerator.generate_user_data()
            response = api_client.enqueue_user(event_id, user_data)
            responses.append(response)
        
        # Check if any requests were rate limited (429 status)
        rate_limited = [r for r in responses if r['status_code'] == 429]
        
        # Record rate limiting metrics
        rate_limit_rate = len(rate_limited) / len(responses)
        performance_monitor.record_metric('rate_limit_rate', rate_limit_rate)
        
        # Note: Rate limiting behavior may vary based on configuration
        logger.info(f"Rate limited requests: {len(rate_limited)} out of {len(responses)}")


# Test fixtures
@pytest.fixture(scope="session")
def setup_test_session():
    """Setup test session and record start"""
    metrics_collector.start_test_session("Queue-it Backend API Tests")
    yield
    metrics_collector.end_test_session("Queue-it Backend API Tests", success=True)


@pytest.fixture(autouse=True)
def setup_test():
    """Setup for each test"""
    # Reset performance monitor for each test
    performance_monitor.metrics.clear()
    yield


# Run tests with performance monitoring
if __name__ == "__main__":
    pytest.main([__file__, "-v", "--html=reports/backend_api_report.html"]) 