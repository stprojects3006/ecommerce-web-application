"""
Dedicated Latency Testing for PURELY E-commerce Application
"""

import time
import requests
import statistics
import json
from datetime import datetime
from typing import Dict, List, Tuple
from config import TestConfig
from latency_monitor import LatencyMonitor
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class LatencyTester:
    """Dedicated latency testing class"""
    
    def __init__(self):
        self.monitor = LatencyMonitor()
        self.session = requests.Session()
        self.results = {}
        
    def test_endpoint_latency(self, endpoint_name: str, url: str, method: str = "GET", 
                             data: dict = None, headers: dict = None, iterations: int = 20):
        """Test latency for a specific endpoint"""
        logger.info(f"Testing latency for {endpoint_name}: {method} {url}")
        
        latencies = []
        response_sizes = []
        status_codes = []
        
        for i in range(iterations):
            start_time = time.time()
            self.monitor.record_request(endpoint_name, method, start_time)
            
            try:
                if method.upper() == "GET":
                    response = self.session.get(url, headers=headers, timeout=30)
                elif method.upper() == "POST":
                    response = self.session.post(url, json=data, headers=headers, timeout=30)
                elif method.upper() == "PUT":
                    response = self.session.put(url, json=data, headers=headers, timeout=30)
                elif method.upper() == "DELETE":
                    response = self.session.delete(url, headers=headers, timeout=30)
                else:
                    raise ValueError(f"Unsupported method: {method}")
                    
                end_time = time.time()
                latency = (end_time - start_time) * 1000  # Convert to milliseconds
                
                # Record response
                response_size = len(response.content) if response.content else 0
                self.monitor.record_response(endpoint_name, method, end_time, response.status_code, response_size)
                
                latencies.append(latency)
                response_sizes.append(response_size)
                status_codes.append(response.status_code)
                
                logger.debug(f"Iteration {i+1}: {latency:.2f}ms, Status: {response.status_code}")
                
            except Exception as e:
                end_time = time.time()
                self.monitor.record_response(endpoint_name, method, end_time, 500, 0)
                logger.error(f"Request failed: {e}")
                latencies.append(5000)  # 5 second penalty for failed requests
                response_sizes.append(0)
                status_codes.append(500)
                
            # Small delay between requests
            time.sleep(0.1)
            
        # Calculate statistics
        if latencies:
            success_count = sum(1 for code in status_codes if code == 200)
            success_rate = (success_count / len(status_codes)) * 100
            
            stats = {
                'endpoint': endpoint_name,
                'url': url,
                'method': method,
                'iterations': iterations,
                'success_rate': success_rate,
                'latency_stats': {
                    'min': min(latencies),
                    'max': max(latencies),
                    'mean': statistics.mean(latencies),
                    'median': statistics.median(latencies),
                    'p50': statistics.quantiles(latencies, n=2)[0] if len(latencies) > 1 else latencies[0],
                    'p90': statistics.quantiles(latencies, n=10)[8] if len(latencies) > 9 else max(latencies),
                    'p95': statistics.quantiles(latencies, n=20)[18] if len(latencies) > 19 else max(latencies),
                    'p99': statistics.quantiles(latencies, n=100)[98] if len(latencies) > 99 else max(latencies),
                    'std_dev': statistics.stdev(latencies) if len(latencies) > 1 else 0
                },
                'response_stats': {
                    'avg_size': statistics.mean(response_sizes) if response_sizes else 0,
                    'min_size': min(response_sizes) if response_sizes else 0,
                    'max_size': max(response_sizes) if response_sizes else 0
                },
                'status_codes': dict(zip(*statistics.mode(status_codes))) if status_codes else {}
            }
            
            self.results[endpoint_name] = stats
            logger.info(f"✅ {endpoint_name}: Avg={stats['latency_stats']['mean']:.2f}ms, "
                       f"P95={stats['latency_stats']['p95']:.2f}ms, Success={success_rate:.1f}%")
            
            return stats
        else:
            logger.error(f"No valid latency data for {endpoint_name}")
            return None
            
    def test_all_endpoints(self):
        """Test latency for all configured endpoints"""
        logger.info("Starting comprehensive latency testing for all endpoints")
        
        # Test health endpoints
        health_endpoints = [
            ("API Gateway Health", TestConfig.API_ENDPOINTS['health_gateway']),
            ("Service Registry", TestConfig.API_ENDPOINTS['health_registry']),
            ("Auth Service Health", TestConfig.API_ENDPOINTS['health_auth']),
            ("Product Service Health", TestConfig.API_ENDPOINTS['health_product']),
            ("Cart Service Health", TestConfig.API_ENDPOINTS['health_cart']),
            ("Order Service Health", TestConfig.API_ENDPOINTS['health_order'])
        ]
        
        for name, url in health_endpoints:
            self.test_endpoint_latency(name, url, "GET")
            
        # Test API endpoints
        api_endpoints = [
            ("Categories API", TestConfig.API_ENDPOINTS['categories_get']),
            ("Products API", TestConfig.API_ENDPOINTS['products_get']),
            ("Product Search", TestConfig.API_ENDPOINTS['products_search'] + "?query=test"),
        ]
        
        for name, url in api_endpoints:
            self.test_endpoint_latency(name, url, "GET")
            
        # Test authenticated endpoints (if we have a test user)
        try:
            # Try to login and test authenticated endpoints
            login_data = {
                'email': 'testuser1@example.com',
                'password': 'Test@123'
            }
            
            login_response = self.session.post(TestConfig.API_ENDPOINTS['auth_signin'], json=login_data)
            if login_response.status_code == 200:
                token_data = login_response.json()
                token = token_data.get('response', {}).get('token')
                if token:
                    self.session.headers.update({'Authorization': f'Bearer {token}'})
                    
                    # Test authenticated endpoints
                    auth_endpoints = [
                        ("User Cart", TestConfig.API_ENDPOINTS['cart_get']),
                        ("User Orders", TestConfig.API_ENDPOINTS['order_get'])
                    ]
                    
                    for name, url in auth_endpoints:
                        self.test_endpoint_latency(name, url, "GET")
                        
        except Exception as e:
            logger.warning(f"Could not test authenticated endpoints: {e}")
            
    def test_concurrent_latency(self, endpoint_name: str, url: str, method: str = "GET", 
                               concurrent_users: int = 10, requests_per_user: int = 5):
        """Test latency under concurrent load"""
        logger.info(f"Testing concurrent latency for {endpoint_name}: {concurrent_users} users, {requests_per_user} requests each")
        
        import threading
        import queue
        
        results_queue = queue.Queue()
        
        def worker(user_id):
            """Worker function for concurrent requests"""
            user_latencies = []
            for i in range(requests_per_user):
                start_time = time.time()
                try:
                    if method.upper() == "GET":
                        response = self.session.get(url, timeout=30)
                    elif method.upper() == "POST":
                        response = self.session.post(url, timeout=30)
                    else:
                        response = self.session.get(url, timeout=30)
                        
                    end_time = time.time()
                    latency = (end_time - start_time) * 1000
                    user_latencies.append(latency)
                    
                except Exception as e:
                    end_time = time.time()
                    latency = 5000  # 5 second penalty
                    user_latencies.append(latency)
                    
            results_queue.put((user_id, user_latencies))
            
        # Start concurrent threads
        threads = []
        for user_id in range(concurrent_users):
            thread = threading.Thread(target=worker, args=(user_id,))
            threads.append(thread)
            thread.start()
            
        # Wait for all threads to complete
        for thread in threads:
            thread.join()
            
        # Collect results
        all_latencies = []
        while not results_queue.empty():
            user_id, latencies = results_queue.get()
            all_latencies.extend(latencies)
            
        # Calculate concurrent statistics
        if all_latencies:
            concurrent_stats = {
                'endpoint': endpoint_name,
                'concurrent_users': concurrent_users,
                'total_requests': len(all_latencies),
                'latency_stats': {
                    'min': min(all_latencies),
                    'max': max(all_latencies),
                    'mean': statistics.mean(all_latencies),
                    'median': statistics.median(all_latencies),
                    'p95': statistics.quantiles(all_latencies, n=20)[18] if len(all_latencies) > 19 else max(all_latencies),
                    'p99': statistics.quantiles(all_latencies, n=100)[98] if len(all_latencies) > 99 else max(all_latencies),
                    'std_dev': statistics.stdev(all_latencies) if len(all_latencies) > 1 else 0
                }
            }
            
            logger.info(f"✅ Concurrent {endpoint_name}: Avg={concurrent_stats['latency_stats']['mean']:.2f}ms, "
                       f"P95={concurrent_stats['latency_stats']['p95']:.2f}ms")
            
            return concurrent_stats
        else:
            logger.error(f"No concurrent latency data for {endpoint_name}")
            return None
            
    def generate_latency_report(self, output_file: str = "reports/detailed_latency_report.json"):
        """Generate detailed latency report"""
        timestamp = datetime.now().isoformat()
        
        # Calculate overall statistics
        all_latencies = []
        for endpoint_data in self.results.values():
            if 'latency_stats' in endpoint_data:
                all_latencies.extend([endpoint_data['latency_stats']['mean']] * endpoint_data['iterations'])
                
        overall_stats = {}
        if all_latencies:
            overall_stats = {
                'total_endpoints_tested': len(self.results),
                'total_requests': sum(data['iterations'] for data in self.results.values()),
                'overall_avg_latency': statistics.mean(all_latencies),
                'overall_p95_latency': statistics.quantiles(all_latencies, n=20)[18] if len(all_latencies) > 19 else max(all_latencies),
                'fastest_endpoint': min(self.results.items(), key=lambda x: x[1]['latency_stats']['mean'])[0] if self.results else None,
                'slowest_endpoint': max(self.results.items(), key=lambda x: x[1]['latency_stats']['mean'])[0] if self.results else None
            }
            
        report = {
            'timestamp': timestamp,
            'test_configuration': {
                'base_url': TestConfig.API_BASE_URL,
                'test_duration': time.time() - self.monitor.start_time
            },
            'overall_statistics': overall_stats,
            'endpoint_results': self.results,
            'recommendations': self._generate_recommendations()
        }
        
        # Save report
        with open(output_file, 'w') as f:
            json.dump(report, f, indent=2)
            
        logger.info(f"Detailed latency report saved to {output_file}")
        return report
        
    def _generate_recommendations(self) -> List[str]:
        """Generate recommendations based on latency data"""
        recommendations = []
        
        if not self.results:
            return recommendations
            
        # Check for slow endpoints
        slow_endpoints = []
        for endpoint, data in self.results.items():
            if data['latency_stats']['mean'] > 1000:  # 1 second
                slow_endpoints.append(f"{endpoint} ({data['latency_stats']['mean']:.2f}ms)")
                
        if slow_endpoints:
            recommendations.append(f"Optimize slow endpoints: {', '.join(slow_endpoints)}")
            
        # Check for high latency variance
        high_variance_endpoints = []
        for endpoint, data in self.results.items():
            mean_latency = data['latency_stats']['mean']
            std_dev = data['latency_stats']['std_dev']
            if std_dev > mean_latency * 0.5:  # High variance
                high_variance_endpoints.append(endpoint)
                
        if high_variance_endpoints:
            recommendations.append(f"High latency variance detected for: {', '.join(high_variance_endpoints)}")
            
        # Check for low success rates
        low_success_endpoints = []
        for endpoint, data in self.results.items():
            if data['success_rate'] < 95:
                low_success_endpoints.append(f"{endpoint} ({data['success_rate']:.1f}%)")
                
        if low_success_endpoints:
            recommendations.append(f"Low success rate endpoints: {', '.join(low_success_endpoints)}")
            
        return recommendations
        
    def print_summary(self):
        """Print latency testing summary"""
        print("\n" + "="*80)
        print("DETAILED LATENCY TESTING SUMMARY")
        print("="*80)
        
        if not self.results:
            print("No latency data available")
            return
            
        # Overall statistics
        all_latencies = []
        for data in self.results.values():
            all_latencies.extend([data['latency_stats']['mean']] * data['iterations'])
            
        print(f"Total Endpoints Tested: {len(self.results)}")
        print(f"Total Requests: {sum(data['iterations'] for data in self.results.values())}")
        print(f"Overall Average Latency: {statistics.mean(all_latencies):.2f}ms")
        print(f"Overall P95 Latency: {statistics.quantiles(all_latencies, n=20)[18]:.2f}ms" if len(all_latencies) > 19 else "Overall P95 Latency: N/A")
        
        print("\nEndpoint Details:")
        print("-" * 80)
        for endpoint, data in self.results.items():
            print(f"{endpoint}:")
            print(f"  Avg: {data['latency_stats']['mean']:.2f}ms")
            print(f"  P95: {data['latency_stats']['p95']:.2f}ms")
            print(f"  P99: {data['latency_stats']['p99']:.2f}ms")
            print(f"  Success Rate: {data['success_rate']:.1f}%")
            print(f"  Response Size: {data['response_stats']['avg_size']:.0f} bytes")
            print()


def main():
    """Main function for latency testing"""
    print("🔍 PURELY E-commerce - Detailed Latency Testing")
    print("=" * 60)
    
    # Create tester
    tester = LatencyTester()
    
    # Test all endpoints
    tester.test_all_endpoints()
    
    # Test concurrent load on key endpoints
    key_endpoints = [
        ("Products API (Concurrent)", TestConfig.API_ENDPOINTS['products_get']),
        ("Categories API (Concurrent)", TestConfig.API_ENDPOINTS['categories_get'])
    ]
    
    for name, url in key_endpoints:
        tester.test_concurrent_latency(name, url, "GET", concurrent_users=5, requests_per_user=3)
    
    # Generate report
    report = tester.generate_latency_report()
    
    # Print summary
    tester.print_summary()
    
    print("✅ Latency testing completed!")
    print("📊 Check reports/detailed_latency_report.json for full results")


if __name__ == "__main__":
    main() 