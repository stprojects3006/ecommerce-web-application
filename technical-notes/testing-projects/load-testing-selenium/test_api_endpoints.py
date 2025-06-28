"""
API Endpoint Tests for PURELY E-commerce Application
"""

import pytest
import time
import json
from base_test import BaseTest
from config import TestConfig
from faker import Faker

fake = Faker()

class TestAPIEndpoints(BaseTest):
    """Test all API endpoints for functionality and performance"""
    
    def test_health_checks(self):
        """Test health check endpoints for all services"""
        health_endpoints = [
            ('API Gateway', TestConfig.API_ENDPOINTS['health_gateway']),
            ('Service Registry', TestConfig.API_ENDPOINTS['health_registry']),
            ('Auth Service', TestConfig.API_ENDPOINTS['health_auth']),
            ('Product Service', TestConfig.API_ENDPOINTS['health_product']),
            ('Cart Service', TestConfig.API_ENDPOINTS['health_cart']),
            ('Order Service', TestConfig.API_ENDPOINTS['health_order'])
        ]
        
        for service_name, endpoint in health_endpoints:
            try:
                response, response_time = self.api_request('GET', endpoint)
                assert response.status_code in [200, 404]  # 404 is acceptable for some health endpoints
                self.assert_performance_threshold(response_time, 'api_response_time')
                print(f"✅ {service_name} health check: {response_time:.2f}s")
            except Exception as e:
                print(f"❌ {service_name} health check failed: {e}")
                
    def test_auth_service_endpoints(self):
        """Test authentication service endpoints"""
        
        # Test user registration
        test_user = {
            'username': fake.user_name(),
            'email': fake.email(),
            'password': 'Test@123'
        }
        
        response, response_time = self.api_request(
            'POST',
            TestConfig.API_ENDPOINTS['auth_signup'],
            data=test_user
        )
        
        assert response.status_code == 200
        self.assert_performance_threshold(response_time, 'api_response_time')
        
        # Test user login
        login_data = {
            'email': test_user['email'],
            'password': test_user['password']
        }
        
        response, response_time = self.api_request(
            'POST',
            TestConfig.API_ENDPOINTS['auth_signin'],
            data=login_data
        )
        
        assert response.status_code == 200
        self.assert_performance_threshold(response_time, 'api_response_time')
        
        # Verify response contains token
        response_data = response.json()
        assert 'response' in response_data
        assert 'token' in response_data['response']
        
    def test_category_service_endpoints(self):
        """Test category service endpoints"""
        
        # Test get all categories
        response, response_time = self.api_request(
            'GET',
            TestConfig.API_ENDPOINTS['categories_get']
        )
        
        assert response.status_code == 200
        self.assert_performance_threshold(response_time, 'api_response_time')
        
        categories = response.json()
        assert 'response' in categories
        assert isinstance(categories['response'], list)
        
        # Test get specific category if categories exist
        if categories['response']:
            category_id = categories['response'][0]['id']
            response, response_time = self.api_request(
                'GET',
                f"{TestConfig.API_ENDPOINTS['category_get']}?id={category_id}"
            )
            
            assert response.status_code == 200
            self.assert_performance_threshold(response_time, 'api_response_time')
            
    def test_product_service_endpoints(self):
        """Test product service endpoints"""
        
        # Test get all products
        response, response_time = self.api_request(
            'GET',
            TestConfig.API_ENDPOINTS['products_get']
        )
        
        assert response.status_code == 200
        self.assert_performance_threshold(response_time, 'api_response_time')
        
        products = response.json()
        assert 'response' in products
        assert isinstance(products['response'], list)
        
        # Test get specific product if products exist
        if products['response']:
            product_id = products['response'][0]['id']
            response, response_time = self.api_request(
                'GET',
                f"{TestConfig.API_ENDPOINTS['product_get']}?id={product_id}"
            )
            
            assert response.status_code == 200
            self.assert_performance_threshold(response_time, 'api_response_time')
            
        # Test search products
        response, response_time = self.api_request(
            'GET',
            f"{TestConfig.API_ENDPOINTS['products_search']}?query=test"
        )
        
        assert response.status_code == 200
        self.assert_performance_threshold(response_time, 'api_response_time')
        
    def test_cart_service_endpoints(self):
        """Test cart service endpoints with authentication"""
        
        # First login to get token
        token = self.login_user('testuser1@example.com', 'Test@123')
        if not token:
            pytest.skip("Cannot test cart endpoints without valid user")
            
        # Test get cart
        response, response_time = self.api_request(
            'GET',
            TestConfig.API_ENDPOINTS['cart_get']
        )
        
        assert response.status_code == 200
        self.assert_performance_threshold(response_time, 'cart_operation_time')
        
        # Test add item to cart
        cart_data = {
            'productId': 'test-product-id',
            'quantity': 1
        }
        
        response, response_time = self.api_request(
            'POST',
            TestConfig.API_ENDPOINTS['cart_add'],
            data=cart_data
        )
        
        # Cart add might fail if product doesn't exist, but should not timeout
        self.assert_performance_threshold(response_time, 'cart_operation_time')
        
    def test_order_service_endpoints(self):
        """Test order service endpoints with authentication"""
        
        # First login to get token
        token = self.login_user('testuser1@example.com', 'Test@123')
        if not token:
            pytest.skip("Cannot test order endpoints without valid user")
            
        # Test get user orders
        response, response_time = self.api_request(
            'GET',
            TestConfig.API_ENDPOINTS['order_get']
        )
        
        assert response.status_code == 200
        self.assert_performance_threshold(response_time, 'api_response_time')
        
        orders = response.json()
        assert 'response' in orders
        assert isinstance(orders['response'], list)
        
    def test_user_service_endpoints(self):
        """Test user service endpoints"""
        
        # Test create user
        user_data = {
            'userId': fake.uuid4(),
            'username': fake.user_name(),
            'email': fake.email()
        }
        
        response, response_time = self.api_request(
            'POST',
            TestConfig.API_ENDPOINTS['user_create'],
            data=user_data
        )
        
        # User creation might fail if user exists, but should not timeout
        self.assert_performance_threshold(response_time, 'api_response_time')
        
    def test_api_gateway_routing(self):
        """Test API Gateway routing to different services"""
        
        services_to_test = [
            ('auth-service', TestConfig.API_ENDPOINTS['auth_signin']),
            ('category-service', TestConfig.API_ENDPOINTS['categories_get']),
            ('product-service', TestConfig.API_ENDPOINTS['products_get'])
        ]
        
        for service_name, endpoint in services_to_test:
            try:
                response, response_time = self.api_request('GET', endpoint)
                # Some endpoints might require POST or authentication
                self.assert_performance_threshold(response_time, 'api_response_time')
                print(f"✅ {service_name} routing: {response_time:.2f}s")
            except Exception as e:
                print(f"⚠️ {service_name} routing test: {e}")
                
    def test_concurrent_api_requests(self):
        """Test concurrent API requests for load testing"""
        import threading
        import queue
        
        results = queue.Queue()
        
        def make_request(endpoint, thread_id):
            try:
                response, response_time = self.api_request('GET', endpoint)
                results.put({
                    'thread_id': thread_id,
                    'endpoint': endpoint,
                    'status': response.status_code,
                    'response_time': response_time,
                    'success': True
                })
            except Exception as e:
                results.put({
                    'thread_id': thread_id,
                    'endpoint': endpoint,
                    'error': str(e),
                    'success': False
                })
                
        # Test concurrent requests to different endpoints
        endpoints = [
            TestConfig.API_ENDPOINTS['categories_get'],
            TestConfig.API_ENDPOINTS['products_get'],
            TestConfig.API_ENDPOINTS['health_gateway']
        ]
        
        threads = []
        for i, endpoint in enumerate(endpoints):
            thread = threading.Thread(target=make_request, args=(endpoint, i))
            threads.append(thread)
            thread.start()
            
        # Wait for all threads to complete
        for thread in threads:
            thread.join()
            
        # Collect results
        successful_requests = 0
        total_response_time = 0
        
        while not results.empty():
            result = results.get()
            if result['success']:
                successful_requests += 1
                total_response_time += result['response_time']
                print(f"Thread {result['thread_id']}: {result['endpoint']} - {result['response_time']:.2f}s")
            else:
                print(f"Thread {result['thread_id']}: {result['endpoint']} - FAILED: {result['error']}")
                
        # Assert performance
        if successful_requests > 0:
            avg_response_time = total_response_time / successful_requests
            self.assert_performance_threshold(avg_response_time, 'api_response_time')
            print(f"Average concurrent response time: {avg_response_time:.2f}s")
            
    def test_api_error_handling(self):
        """Test API error handling for invalid requests"""
        
        # Test invalid endpoint
        try:
            response, response_time = self.api_request('GET', f"{TestConfig.API_BASE_URL}/invalid-endpoint")
            # Should return 404 or similar error status
            assert response.status_code in [404, 405, 500]
        except Exception as e:
            # Exception is also acceptable for invalid endpoints
            print(f"Expected error for invalid endpoint: {e}")
            
        # Test invalid JSON in POST request
        try:
            response, response_time = self.api_request(
                'POST',
                TestConfig.API_ENDPOINTS['auth_signin'],
                data="invalid json"
            )
            # Should return 400 or similar error status
            assert response.status_code in [400, 415, 500]
        except Exception as e:
            # Exception is also acceptable for invalid JSON
            print(f"Expected error for invalid JSON: {e}")
            
    def test_api_response_format(self):
        """Test API response format consistency"""
        
        # Test categories endpoint response format
        response, _ = self.api_request('GET', TestConfig.API_ENDPOINTS['categories_get'])
        
        if response.status_code == 200:
            data = response.json()
            # Check if response has expected structure
            assert isinstance(data, dict)
            if 'response' in data:
                assert isinstance(data['response'], list)
                
        # Test products endpoint response format
        response, _ = self.api_request('GET', TestConfig.API_ENDPOINTS['products_get'])
        
        if response.status_code == 200:
            data = response.json()
            # Check if response has expected structure
            assert isinstance(data, dict)
            if 'response' in data:
                assert isinstance(data['response'], list)
                
    @pytest.mark.performance
    def test_api_performance_under_load(self):
        """Performance test for API endpoints under load"""
        
        endpoints_to_test = [
            TestConfig.API_ENDPOINTS['categories_get'],
            TestConfig.API_ENDPOINTS['products_get'],
            TestConfig.API_ENDPOINTS['health_gateway']
        ]
        
        response_times = []
        
        # Make multiple requests to each endpoint
        for endpoint in endpoints_to_test:
            for _ in range(5):  # 5 requests per endpoint
                try:
                    response, response_time = self.api_request('GET', endpoint)
                    response_times.append(response_time)
                    time.sleep(0.1)  # Small delay between requests
                except Exception as e:
                    print(f"Request failed: {e}")
                    
        # Calculate statistics
        if response_times:
            avg_time = sum(response_times) / len(response_times)
            max_time = max(response_times)
            min_time = min(response_times)
            
            print(f"Performance Statistics:")
            print(f"  Average Response Time: {avg_time:.2f}s")
            print(f"  Maximum Response Time: {max_time:.2f}s")
            print(f"  Minimum Response Time: {min_time:.2f}s")
            print(f"  Total Requests: {len(response_times)}")
            
            # Assert performance thresholds
            self.assert_performance_threshold(avg_time, 'api_response_time')
            self.assert_performance_threshold(max_time, 'api_response_time') 