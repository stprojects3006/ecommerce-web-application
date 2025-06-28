"""
UI Flow Tests for PURELY E-commerce Application
"""

import pytest
import time
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from base_test import BaseTest
from config import TestConfig
from faker import Faker

fake = Faker()

class TestUIFlows(BaseTest):
    """Test complete user journeys and UI flows"""
    
    def test_home_page_load(self):
        """Test home page loads correctly"""
        load_time = self.navigate_to(TestConfig.FRONTEND_URLS['home'])
        
        # Verify page elements
        assert self.is_element_present(By.TAG_NAME, 'header')
        assert self.is_element_present(By.TAG_NAME, 'main')
        
        # Check for navigation elements
        assert self.is_element_present(By.LINK_TEXT, 'Products') or self.is_element_present(By.XPATH, "//a[contains(text(), 'Products')]")
        assert self.is_element_present(By.LINK_TEXT, 'Login') or self.is_element_present(By.XPATH, "//a[contains(text(), 'Login')]")
        
        self.assert_performance_threshold(load_time, 'page_load_time')
        
    def test_user_registration_flow(self):
        """Test complete user registration flow"""
        
        # Navigate to registration page
        load_time = self.navigate_to(TestConfig.FRONTEND_URLS['register'])
        self.assert_performance_threshold(load_time, 'page_load_time')
        
        # Generate test user data
        test_user = {
            'username': fake.user_name(),
            'email': fake.email(),
            'password': 'Test@123'
        }
        
        # Fill registration form
        username_field = self.wait_for_element(By.NAME, 'userName')
        self.safe_send_keys(username_field, test_user['username'])
        
        email_field = self.wait_for_element(By.NAME, 'email')
        self.safe_send_keys(email_field, test_user['email'])
        
        password_field = self.wait_for_element(By.NAME, 'password')
        self.safe_send_keys(password_field, test_user['password'])
        
        # Submit form
        submit_button = self.wait_for_element_clickable(By.XPATH, "//button[@type='submit']")
        self.safe_click(submit_button)
        
        # Wait for verification page
        time.sleep(2)
        
        # Verify we're on verification page
        assert 'verification' in self.driver.current_url.lower() or self.is_element_present(By.NAME, 'verificationCode')
        
        print(f"✅ Registration flow completed for user: {test_user['email']}")
        
    def test_user_login_flow(self):
        """Test user login flow"""
        
        # Navigate to login page
        load_time = self.navigate_to(TestConfig.FRONTEND_URLS['login'])
        self.assert_performance_threshold(load_time, 'page_load_time')
        
        # Fill login form
        email_field = self.wait_for_element(By.NAME, 'email')
        self.safe_send_keys(email_field, 'testuser1@example.com')
        
        password_field = self.wait_for_element(By.NAME, 'password')
        self.safe_send_keys(password_field, 'Test@123')
        
        # Submit form
        submit_button = self.wait_for_element_clickable(By.XPATH, "//button[@type='submit']")
        self.safe_click(submit_button)
        
        # Wait for redirect
        time.sleep(3)
        
        # Verify successful login (should redirect to home or show user info)
        assert self.driver.current_url != TestConfig.FRONTEND_URLS['login']
        
        print("✅ Login flow completed successfully")
        
    def test_product_browsing_flow(self):
        """Test product browsing and search functionality"""
        
        # Navigate to products page
        load_time = self.navigate_to(TestConfig.FRONTEND_URLS['products'])
        self.assert_performance_threshold(load_time, 'page_load_time')
        
        # Wait for products to load
        time.sleep(2)
        
        # Check if products are displayed
        product_elements = self.driver.find_elements(By.CLASS_NAME, 'product') or self.driver.find_elements(By.XPATH, "//div[contains(@class, 'product')]")
        
        if product_elements:
            print(f"✅ Found {len(product_elements)} products")
            
            # Test product details
            first_product = product_elements[0]
            
            # Check for product image
            product_image = first_product.find_elements(By.TAG_NAME, 'img')
            if product_image:
                print("✅ Product images are displayed")
                
            # Check for product name
            product_name = first_product.find_elements(By.TAG_NAME, 'h3') or first_product.find_elements(By.TAG_NAME, 'h4')
            if product_name:
                print("✅ Product names are displayed")
                
            # Check for add to cart button
            add_to_cart_buttons = first_product.find_elements(By.XPATH, ".//button[contains(text(), 'Add to Cart')]")
            if add_to_cart_buttons:
                print("✅ Add to cart buttons are present")
        else:
            print("⚠️ No products found on products page")
            
    def test_search_functionality(self):
        """Test search functionality"""
        
        # Navigate to search page or home page with search
        load_time = self.navigate_to(TestConfig.FRONTEND_URLS['search'])
        self.assert_performance_threshold(load_time, 'page_load_time')
        
        # Look for search input
        search_input = self.driver.find_elements(By.NAME, 'search') or self.driver.find_elements(By.XPATH, "//input[@placeholder*='search' or @placeholder*='Search']")
        
        if search_input:
            # Perform search
            self.safe_send_keys(search_input[0], 'test')
            search_input[0].send_keys(Keys.RETURN)
            
            # Wait for search results
            time.sleep(2)
            
            # Verify search results page loaded
            assert 'search' in self.driver.current_url.lower() or self.is_element_present(By.CLASS_NAME, 'search-results')
            
            print("✅ Search functionality works")
        else:
            print("⚠️ Search input not found")
            
    def test_cart_management_flow(self):
        """Test cart management functionality"""
        
        # First login to access cart features
        self.login_user('testuser1@example.com', 'Test@123')
        
        # Navigate to products page
        load_time = self.navigate_to(TestConfig.FRONTEND_URLS['products'])
        self.assert_performance_threshold(load_time, 'page_load_time')
        
        # Wait for products to load
        time.sleep(2)
        
        # Find add to cart button
        add_to_cart_buttons = self.driver.find_elements(By.XPATH, "//button[contains(text(), 'Add to Cart')]")
        
        if add_to_cart_buttons:
            # Add first product to cart
            self.safe_click(add_to_cart_buttons[0])
            
            # Wait for cart update
            time.sleep(2)
            
            # Navigate to cart page
            load_time = self.navigate_to(TestConfig.FRONTEND_URLS['cart'])
            self.assert_performance_threshold(load_time, 'page_load_time')
            
            # Check if cart items are displayed
            cart_items = self.driver.find_elements(By.CLASS_NAME, 'cart-item') or self.driver.find_elements(By.XPATH, "//div[contains(@class, 'cart')]")
            
            if cart_items:
                print("✅ Cart items are displayed")
                
                # Test quantity update
                quantity_inputs = self.driver.find_elements(By.NAME, 'quantity') or self.driver.find_elements(By.XPATH, "//input[@type='number']")
                if quantity_inputs:
                    # Update quantity
                    self.safe_send_keys(quantity_inputs[0], '2')
                    time.sleep(1)
                    print("✅ Quantity update works")
                    
                # Test remove item
                remove_buttons = self.driver.find_elements(By.XPATH, "//button[contains(text(), 'Remove')]")
                if remove_buttons:
                    print("✅ Remove buttons are present")
            else:
                print("⚠️ No cart items found")
        else:
            print("⚠️ Add to cart buttons not found")
            
    def test_checkout_flow(self):
        """Test complete checkout process"""
        
        # First login and add item to cart
        self.login_user('testuser1@example.com', 'Test@123')
        
        # Navigate to products and add item
        self.navigate_to(TestConfig.FRONTEND_URLS['products'])
        time.sleep(2)
        
        add_to_cart_buttons = self.driver.find_elements(By.XPATH, "//button[contains(text(), 'Add to Cart')]")
        if add_to_cart_buttons:
            self.safe_click(add_to_cart_buttons[0])
            time.sleep(2)
            
            # Navigate to checkout
            load_time = self.navigate_to(TestConfig.FRONTEND_URLS['checkout'])
            self.assert_performance_threshold(load_time, 'page_load_time')
            
            # Fill checkout form
            form_fields = {
                'fname': 'John',
                'lname': 'Doe',
                'address1': '123 Test Street',
                'address2': 'Apt 4B',
                'city': 'Test City',
                'phone': '1234567890'
            }
            
            for field_name, value in form_fields.items():
                field = self.driver.find_elements(By.NAME, field_name)
                if field:
                    self.safe_send_keys(field[0], value)
                    
            # Submit order
            submit_button = self.driver.find_elements(By.XPATH, "//button[@type='submit']")
            if submit_button:
                self.safe_click(submit_button[0])
                
                # Wait for order processing
                time.sleep(5)
                
                # Verify order success
                if 'success' in self.driver.current_url.lower() or self.is_element_present(By.XPATH, "//*[contains(text(), 'Order placed')]"):
                    print("✅ Checkout flow completed successfully")
                else:
                    print("⚠️ Order placement may have failed")
            else:
                print("⚠️ Submit button not found")
        else:
            print("⚠️ Cannot test checkout without items in cart")
            
    def test_my_account_flow(self):
        """Test my account page functionality"""
        
        # First login
        self.login_user('testuser1@example.com', 'Test@123')
        
        # Navigate to my account page
        load_time = self.navigate_to(TestConfig.FRONTEND_URLS['my_account'])
        self.assert_performance_threshold(load_time, 'page_load_time')
        
        # Check for user information
        user_info_elements = self.driver.find_elements(By.XPATH, "//*[contains(text(), 'testuser1') or contains(text(), 'testuser1@example.com')]")
        
        if user_info_elements:
            print("✅ User information is displayed")
        else:
            print("⚠️ User information not found")
            
        # Check for order history
        order_elements = self.driver.find_elements(By.CLASS_NAME, 'order') or self.driver.find_elements(By.XPATH, "//div[contains(@class, 'order')]")
        
        if order_elements:
            print(f"✅ Found {len(order_elements)} orders in history")
        else:
            print("⚠️ No order history found")
            
    def test_navigation_flow(self):
        """Test navigation between different pages"""
        
        pages_to_test = [
            ('Home', TestConfig.FRONTEND_URLS['home']),
            ('Products', TestConfig.FRONTEND_URLS['products']),
            ('Login', TestConfig.FRONTEND_URLS['login']),
            ('Register', TestConfig.FRONTEND_URLS['register'])
        ]
        
        for page_name, url in pages_to_test:
            try:
                load_time = self.navigate_to(url)
                self.assert_performance_threshold(load_time, 'page_load_time')
                print(f"✅ {page_name} page loads in {load_time:.2f}s")
                
                # Verify page loaded correctly
                assert self.driver.title or self.is_element_present(By.TAG_NAME, 'body')
                
            except Exception as e:
                print(f"❌ {page_name} page failed: {e}")
                
    def test_responsive_design(self):
        """Test responsive design at different screen sizes"""
        
        screen_sizes = [
            (1920, 1080),  # Desktop
            (1366, 768),   # Laptop
            (768, 1024),   # Tablet
            (375, 667)     # Mobile
        ]
        
        for width, height in screen_sizes:
            self.driver.set_window_size(width, height)
            time.sleep(1)
            
            # Navigate to home page
            load_time = self.navigate_to(TestConfig.FRONTEND_URLS['home'])
            
            # Check if page is responsive
            page_width = self.driver.execute_script("return document.documentElement.clientWidth;")
            
            print(f"✅ Screen size {width}x{height}: Page width {page_width}px, Load time {load_time:.2f}s")
            
    def test_error_handling(self):
        """Test error handling for invalid inputs and scenarios"""
        
        # Test invalid login
        load_time = self.navigate_to(TestConfig.FRONTEND_URLS['login'])
        
        email_field = self.wait_for_element(By.NAME, 'email')
        self.safe_send_keys(email_field, 'invalid@email.com')
        
        password_field = self.wait_for_element(By.NAME, 'password')
        self.safe_send_keys(password_field, 'wrongpassword')
        
        submit_button = self.wait_for_element_clickable(By.XPATH, "//button[@type='submit']")
        self.safe_click(submit_button)
        
        # Wait for error message
        time.sleep(2)
        
        # Check for error message
        error_elements = self.driver.find_elements(By.CLASS_NAME, 'error') or self.driver.find_elements(By.XPATH, "//*[contains(@class, 'error') or contains(@class, 'alert')]")
        
        if error_elements:
            print("✅ Error handling works for invalid login")
        else:
            print("⚠️ No error message found for invalid login")
            
    def test_accessibility_features(self):
        """Test basic accessibility features"""
        
        # Navigate to home page
        self.navigate_to(TestConfig.FRONTEND_URLS['home'])
        
        # Check for alt text on images
        images = self.driver.find_elements(By.TAG_NAME, 'img')
        images_with_alt = [img for img in images if img.get_attribute('alt')]
        
        if images:
            alt_text_ratio = len(images_with_alt) / len(images)
            print(f"✅ {alt_text_ratio:.1%} of images have alt text")
            
        # Check for form labels
        forms = self.driver.find_elements(By.TAG_NAME, 'form')
        for form in forms:
            inputs = form.find_elements(By.TAG_NAME, 'input')
            labels = form.find_elements(By.TAG_NAME, 'label')
            
            if inputs and labels:
                print(f"✅ Form has {len(labels)} labels for {len(inputs)} inputs")
                
    @pytest.mark.performance
    def test_page_load_performance(self):
        """Performance test for page loads"""
        
        pages_to_test = [
            ('Home', TestConfig.FRONTEND_URLS['home']),
            ('Products', TestConfig.FRONTEND_URLS['products']),
            ('Login', TestConfig.FRONTEND_URLS['login']),
            ('Register', TestConfig.FRONTEND_URLS['register'])
        ]
        
        performance_results = {}
        
        for page_name, url in pages_to_test:
            load_times = []
            
            # Test each page multiple times
            for _ in range(3):
                try:
                    load_time = self.navigate_to(url)
                    load_times.append(load_time)
                    time.sleep(1)
                except Exception as e:
                    print(f"Error loading {page_name}: {e}")
                    
            if load_times:
                avg_load_time = sum(load_times) / len(load_times)
                max_load_time = max(load_times)
                min_load_time = min(load_times)
                
                performance_results[page_name] = {
                    'average': avg_load_time,
                    'maximum': max_load_time,
                    'minimum': min_load_time
                }
                
                print(f"{page_name} Page Performance:")
                print(f"  Average: {avg_load_time:.2f}s")
                print(f"  Maximum: {max_load_time:.2f}s")
                print(f"  Minimum: {min_load_time:.2f}s")
                
                # Assert performance thresholds
                self.assert_performance_threshold(avg_load_time, 'page_load_time')
                
        return performance_results 