"""
Locust Load Testing for PURELY E-commerce Application
"""

from locust import HttpUser, task, between, events
import json
import random
from faker import Faker

fake = Faker()

class EcommerceUser(HttpUser):
    """Simulates a user browsing and purchasing on the e-commerce site"""
    
    wait_time = between(1, 3)  # Wait 1-3 seconds between tasks
    
    def on_start(self):
        """Called when a user starts"""
        self.user_data = {
            'username': fake.user_name(),
            'email': fake.email(),
            'password': 'Test@123'
        }
        self.auth_token = None
        self.cart_id = None
        
    @task(3)
    def view_home_page(self):
        """View home page - high frequency"""
        with self.client.get("/", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Home page failed: {response.status_code}")
                
    @task(2)
    def view_products(self):
        """View products page"""
        with self.client.get("/products", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Products page failed: {response.status_code}")
                
    @task(1)
    def view_categories(self):
        """View categories via API"""
        with self.client.get("/category-service/category/get/all", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Categories API failed: {response.status_code}")
                
    @task(2)
    def search_products(self):
        """Search for products"""
        search_terms = ['phone', 'laptop', 'book', 'shirt', 'shoes']
        search_term = random.choice(search_terms)
        
        with self.client.get(f"/search?query={search_term}", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Search failed: {response.status_code}")
                
    @task(1)
    def register_user(self):
        """Register a new user"""
        user_data = {
            'userName': fake.user_name(),
            'email': fake.email(),
            'password': 'Test@123'
        }
        
        with self.client.post("/auth-service/auth/signup", 
                             json=user_data, 
                             catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Registration failed: {response.status_code}")
                
    @task(2)
    def login_user(self):
        """Login user"""
        login_data = {
            'email': 'testuser1@example.com',
            'password': 'Test@123'
        }
        
        with self.client.post("/auth-service/auth/signin", 
                             json=login_data, 
                             catch_response=True) as response:
            if response.status_code == 200:
                response_data = response.json()
                if 'response' in response_data and 'token' in response_data['response']:
                    self.auth_token = response_data['response']['token']
                    response.success()
                else:
                    response.failure("No token in response")
            else:
                response.failure(f"Login failed: {response.status_code}")
                
    @task(1)
    def add_to_cart(self):
        """Add item to cart (requires authentication)"""
        if not self.auth_token:
            return
            
        cart_data = {
            'productId': f'product_{random.randint(1, 100)}',
            'quantity': random.randint(1, 3)
        }
        
        headers = {'Authorization': f'Bearer {self.auth_token}'}
        
        with self.client.post("/cart-service/cart/add", 
                             json=cart_data, 
                             headers=headers,
                             catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Add to cart failed: {response.status_code}")
                
    @task(1)
    def view_cart(self):
        """View cart (requires authentication)"""
        if not self.auth_token:
            return
            
        headers = {'Authorization': f'Bearer {self.auth_token}'}
        
        with self.client.get("/cart-service/cart/get/byUser", 
                            headers=headers,
                            catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"View cart failed: {response.status_code}")
                
    @task(1)
    def place_order(self):
        """Place an order (requires authentication)"""
        if not self.auth_token:
            return
            
        order_data = {
            'firstName': fake.first_name(),
            'lastName': fake.last_name(),
            'addressLine1': fake.street_address(),
            'addressLine2': fake.secondary_address(),
            'city': fake.city(),
            'phoneNo': fake.phone_number(),
            'cartId': f'cart_{random.randint(1, 1000)}'
        }
        
        headers = {'Authorization': f'Bearer {self.auth_token}'}
        
        with self.client.post("/order-service/order/create", 
                             json=order_data, 
                             headers=headers,
                             catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Place order failed: {response.status_code}")
                
    @task(1)
    def view_orders(self):
        """View user orders (requires authentication)"""
        if not self.auth_token:
            return
            
        headers = {'Authorization': f'Bearer {self.auth_token}'}
        
        with self.client.get("/order-service/order/get/byUser", 
                            headers=headers,
                            catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"View orders failed: {response.status_code}")
                
    @task(1)
    def health_check(self):
        """Health check for API Gateway"""
        with self.client.get("/actuator/health", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Health check failed: {response.status_code}")


class APIUser(HttpUser):
    """Simulates API-only usage (for microservices testing)"""
    
    wait_time = between(0.1, 0.5)  # Faster requests for API testing
    
    def on_start(self):
        """Called when a user starts"""
        self.auth_token = None
        
    @task(5)
    def get_products(self):
        """Get all products"""
        with self.client.get("/product-service/product/get/all", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Get products failed: {response.status_code}")
                
    @task(3)
    def get_categories(self):
        """Get all categories"""
        with self.client.get("/category-service/category/get/all", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Get categories failed: {response.status_code}")
                
    @task(2)
    def search_products(self):
        """Search products"""
        search_terms = ['phone', 'laptop', 'book', 'shirt', 'shoes']
        search_term = random.choice(search_terms)
        
        with self.client.get(f"/product-service/product/search?query={search_term}", 
                            catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Search products failed: {response.status_code}")
                
    @task(1)
    def create_user(self):
        """Create user in user service"""
        user_data = {
            'userId': fake.uuid4(),
            'username': fake.user_name(),
            'email': fake.email()
        }
        
        with self.client.post("/user-service/user/create", 
                             json=user_data, 
                             catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Create user failed: {response.status_code}")


class StressTestUser(HttpUser):
    """Simulates high-stress scenarios"""
    
    wait_time = between(0.05, 0.2)  # Very fast requests for stress testing
    
    @task(10)
    def rapid_requests(self):
        """Make rapid requests to test system stability"""
        endpoints = [
            "/",
            "/products",
            "/category-service/category/get/all",
            "/product-service/product/get/all",
            "/actuator/health"
        ]
        
        endpoint = random.choice(endpoints)
        
        with self.client.get(endpoint, catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Rapid request failed: {response.status_code}")


# Event handlers for monitoring
@events.request.add_listener
def my_request_handler(request_type, name, response_time, response_length, response, context, exception, start_time, url, **kwargs):
    """Custom request handler for monitoring"""
    if exception:
        print(f"Request failed: {name} - {exception}")
    elif response.status_code >= 400:
        print(f"Request error: {name} - Status: {response.status_code}")


@events.test_start.add_listener
def on_test_start(environment, **kwargs):
    """Called when test starts"""
    print("Load test starting...")


@events.test_stop.add_listener
def on_test_stop(environment, **kwargs):
    """Called when test stops"""
    print("Load test completed.") 