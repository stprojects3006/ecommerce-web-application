"""
Configuration file for PURELY E-commerce Load Testing
"""

import os
from dotenv import load_dotenv

load_dotenv()

class TestConfig:
    """Test configuration settings"""
    
    # Base URLs
    BASE_URL = os.getenv('BASE_URL', 'http://localhost')
    API_BASE_URL = os.getenv('API_BASE_URL', 'http://localhost:8081')
    
    # Frontend URLs
    FRONTEND_URLS = {
        'home': f'{BASE_URL}/',
        'login': f'{BASE_URL}/auth/login',
        'register': f'{BASE_URL}/auth/register',
        'products': f'{BASE_URL}/products',
        'cart': f'{BASE_URL}/cart',
        'checkout': f'{BASE_URL}/checkout',
        'my_account': f'{BASE_URL}/my-account',
        'search': f'{BASE_URL}/search'
    }
    
    # API Endpoints
    API_ENDPOINTS = {
        # Auth Service
        'auth_signup': f'{API_BASE_URL}/auth-service/auth/signup',
        'auth_signin': f'{API_BASE_URL}/auth-service/auth/signin',
        'auth_verify': f'{API_BASE_URL}/auth-service/auth/signup/verify',
        'auth_resend': f'{API_BASE_URL}/auth-service/auth/signup/resend',
        
        # User Service
        'user_create': f'{API_BASE_URL}/user-service/user/create',
        'user_get': f'{API_BASE_URL}/user-service/user/get',
        'user_update': f'{API_BASE_URL}/user-service/user/update',
        
        # Category Service
        'categories_get': f'{API_BASE_URL}/category-service/category/get/all',
        'category_get': f'{API_BASE_URL}/category-service/category/get',
        
        # Product Service
        'products_get': f'{API_BASE_URL}/product-service/product/get/all',
        'product_get': f'{API_BASE_URL}/product-service/product/get',
        'products_by_category': f'{API_BASE_URL}/product-service/product/get/byCategory',
        'products_search': f'{API_BASE_URL}/product-service/product/search',
        
        # Cart Service
        'cart_add': f'{API_BASE_URL}/cart-service/cart/add',
        'cart_get': f'{API_BASE_URL}/cart-service/cart/get/byUser',
        'cart_update': f'{API_BASE_URL}/cart-service/cart/update',
        'cart_remove': f'{API_BASE_URL}/cart-service/cart/remove',
        'cart_clear': f'{API_BASE_URL}/cart-service/cart/clear/byId',
        
        # Order Service
        'order_create': f'{API_BASE_URL}/order-service/order/create',
        'order_get': f'{API_BASE_URL}/order-service/order/get/byUser',
        'order_get_by_id': f'{API_BASE_URL}/order-service/order/get',
        
        # Health Checks
        'health_gateway': f'{API_BASE_URL}/actuator/health',
        'health_registry': 'http://localhost:8761',
        'health_auth': 'http://localhost:8082/actuator/health',
        'health_product': 'http://localhost:8084/actuator/health',
        'health_cart': 'http://localhost:8085/actuator/health',
        'health_order': 'http://localhost:8086/actuator/health'
    }
    
    # Test Data
    TEST_USERS = [
        {
            'username': 'testuser1',
            'email': 'testuser1@example.com',
            'password': 'Test@123'
        },
        {
            'username': 'testuser2',
            'email': 'testuser2@example.com',
            'password': 'Test@123'
        },
        {
            'username': 'testuser3',
            'email': 'testuser3@example.com',
            'password': 'Test@123'
        }
    ]
    
    # Browser Configuration
    BROWSER_CONFIG = {
        'headless': os.getenv('HEADLESS', 'false').lower() == 'true',
        'implicit_wait': 10,
        'page_load_timeout': 30,
        'window_size': (1920, 1080)
    }
    
    # Load Test Configuration
    LOAD_TEST_CONFIG = {
        'users': int(os.getenv('LOAD_TEST_USERS', '10')),
        'spawn_rate': int(os.getenv('SPAWN_RATE', '2')),
        'run_time': os.getenv('RUN_TIME', '60s'),
        'host': BASE_URL
    }
    
    # Performance Thresholds
    PERFORMANCE_THRESHOLDS = {
        'page_load_time': 3.0,  # seconds
        'api_response_time': 1.0,  # seconds
        'cart_operation_time': 2.0,  # seconds
        'checkout_time': 5.0,  # seconds
    }
    
    # Test Categories
    TEST_CATEGORIES = {
        'smoke': 'Basic functionality tests',
        'regression': 'Full application flow tests',
        'performance': 'Load and stress tests',
        'api': 'API endpoint tests',
        'ui': 'User interface tests',
        'e2e': 'End-to-end user journey tests'
    } 