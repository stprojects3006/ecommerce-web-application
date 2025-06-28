#!/usr/bin/env python3
"""
Test Runner for PURELY E-commerce Load Testing
"""

import os
import sys
import argparse
import subprocess
import time
import json
from datetime import datetime

def run_command(command, description):
    """Run a command and handle errors"""
    print(f"\n🔄 {description}")
    print(f"Command: {command}")
    
    try:
        result = subprocess.run(command, shell=True, check=True, capture_output=True, text=True)
        print(f"✅ {description} completed successfully")
        return result.stdout
    except subprocess.CalledProcessError as e:
        print(f"❌ {description} failed:")
        print(f"Error: {e.stderr}")
        return None

def create_directories():
    """Create necessary directories for test outputs"""
    directories = [
        'screenshots',
        'reports',
        'logs',
        'test_data'
    ]
    
    for directory in directories:
        os.makedirs(directory, exist_ok=True)
        print(f"📁 Created directory: {directory}")

def run_selenium_tests(test_type, headless=False, parallel=False):
    """Run Selenium tests"""
    print(f"\n🧪 Running Selenium Tests: {test_type}")
    
    # Set environment variables
    env_vars = f"HEADLESS={str(headless).lower()}"
    
    # Build pytest command
    pytest_cmd = f"{env_vars} pytest"
    
    if test_type == 'smoke':
        pytest_cmd += " -m smoke"
    elif test_type == 'regression':
        pytest_cmd += " -m 'not performance'"
    elif test_type == 'performance':
        pytest_cmd += " -m performance"
    elif test_type == 'api':
        pytest_cmd += " test_api_endpoints.py"
    elif test_type == 'ui':
        pytest_cmd += " test_ui_flows.py"
    elif test_type == 'e2e':
        pytest_cmd += " test_ui_flows.py::TestUIFlows::test_checkout_flow"
    
    if parallel:
        pytest_cmd += " -n auto"
    
    pytest_cmd += " --html=reports/selenium_report.html --self-contained-html"
    pytest_cmd += " --junitxml=reports/selenium_junit.xml"
    
    return run_command(pytest_cmd, f"Selenium {test_type} tests")

def run_locust_load_test(users=10, spawn_rate=2, run_time="60s", host="http://localhost"):
    """Run Locust load test"""
    print(f"\n🐛 Running Locust Load Test")
    print(f"Users: {users}, Spawn Rate: {spawn_rate}, Duration: {run_time}")
    
    locust_cmd = f"locust -f locustfile.py --host={host} --users={users} --spawn-rate={spawn_rate} --run-time={run_time} --headless --html=reports/locust_report.html"
    
    return run_command(locust_cmd, "Locust load test")

def run_api_performance_test():
    """Run API performance tests with detailed latency monitoring"""
    print(f"\n⚡ Running API Performance Tests with Latency Monitoring")
    
    # Test different API endpoints with different loads
    endpoints = [
        ("categories", "GET", "/category-service/category/get/all"),
        ("products", "GET", "/product-service/product/get/all"),
        ("auth_health", "GET", "/actuator/health"),
        ("search", "GET", "/product-service/product/search?query=test")
    ]
    
    results = {}
    
    for name, method, endpoint in endpoints:
        print(f"\nTesting {name} endpoint...")
        
        # Use curl for simple API testing with detailed timing
        curl_cmd = f"curl -w '@curl-format.txt' -o /dev/null -s '{TestConfig.API_BASE_URL}{endpoint}'"
        
        # Run multiple times and collect detailed metrics
        times = []
        response_sizes = []
        status_codes = []
        
        for i in range(10):  # Increased iterations for better statistics
            result = run_command(curl_cmd, f"API test {name} (iteration {i+1})")
            if result:
                # Parse curl output for detailed timing
                lines = result.strip().split('\n')
                for line in lines:
                    if 'time_total' in line:
                        time_value = float(line.split(':')[1].strip())
                        times.append(time_value)
                    elif 'size_download' in line:
                        size_value = int(line.split(':')[1].strip())
                        response_sizes.append(size_value)
                    elif 'http_code' in line:
                        status_code = int(line.split(':')[1].strip())
                        status_codes.append(status_code)
        
        if times:
            avg_time = sum(times) / len(times)
            min_time = min(times)
            max_time = max(times)
            p95_time = sorted(times)[int(len(times) * 0.95)] if len(times) > 1 else times[0]
            p99_time = sorted(times)[int(len(times) * 0.99)] if len(times) > 1 else times[0]
            
            results[name] = {
                'average_time': avg_time,
                'min_time': min_time,
                'max_time': max_time,
                'p95_time': p95_time,
                'p99_time': p99_time,
                'response_size': sum(response_sizes) / len(response_sizes) if response_sizes else 0,
                'success_rate': sum(1 for code in status_codes if code == 200) / len(status_codes) * 100 if status_codes else 0,
                'total_requests': len(times)
            }
            print(f"✅ {name}: Avg={avg_time:.3f}s, P95={p95_time:.3f}s, P99={p99_time:.3f}s, Success={results[name]['success_rate']:.1f}%")
    
    return results

def generate_curl_format_file():
    """Generate curl format file for detailed timing measurements"""
    curl_format = """     time_namelookup:  %{time_namelookup}\\n
        time_connect:  %{time_connect}\\n
     time_appconnect:  %{time_appconnect}\\n
    time_pretransfer:  %{time_pretransfer}\\n
       time_redirect:  %{time_redirect}\\n
  time_starttransfer:  %{time_starttransfer}\\n
                     ----------\\n
          time_total:  %{time_total}\\n
       size_download:  %{size_download}\\n
          http_code:  %{http_code}\\n"""
    
    with open('curl-format.txt', 'w') as f:
        f.write(curl_format)

def run_health_checks():
    """Run health checks for all services"""
    print(f"\n🏥 Running Health Checks")
    
    health_endpoints = [
        ("API Gateway", "http://localhost:8081/actuator/health"),
        ("Service Registry", "http://localhost:8761"),
        ("Auth Service", "http://localhost:8082/actuator/health"),
        ("Product Service", "http://localhost:8084/actuator/health"),
        ("Cart Service", "http://localhost:8085/actuator/health"),
        ("Order Service", "http://localhost:8086/actuator/health")
    ]
    
    results = {}
    
    for service_name, endpoint in health_endpoints:
        curl_cmd = f"curl -s -o /dev/null -w '%{{http_code}}' {endpoint}"
        result = run_command(curl_cmd, f"Health check for {service_name}")
        
        if result:
            status_code = result.strip()
            if status_code == '200':
                print(f"✅ {service_name}: Healthy")
                results[service_name] = "Healthy"
            else:
                print(f"❌ {service_name}: Unhealthy (Status: {status_code})")
                results[service_name] = f"Unhealthy (Status: {status_code})"
        else:
            print(f"❌ {service_name}: Connection failed")
            results[service_name] = "Connection failed"
    
    return results

def generate_latency_report(test_results, health_results, api_results):
    """Generate comprehensive latency report"""
    print(f"\n📊 Generating Comprehensive Latency Report")
    
    # Calculate overall latency statistics
    overall_latency_stats = {}
    if api_results:
        all_times = []
        all_success_rates = []
        for endpoint_data in api_results.values():
            all_times.extend([endpoint_data['average_time']] * endpoint_data['total_requests'])
            all_success_rates.append(endpoint_data['success_rate'])
        
        overall_latency_stats = {
            'total_api_requests': sum(data['total_requests'] for data in api_results.values()),
            'average_response_time': sum(all_times) / len(all_times) if all_times else 0,
            'overall_success_rate': sum(all_success_rates) / len(all_success_rates) if all_success_rates else 0,
            'slowest_endpoint': max(api_results.items(), key=lambda x: x[1]['average_time'])[0] if api_results else None,
            'fastest_endpoint': min(api_results.items(), key=lambda x: x[1]['average_time'])[0] if api_results else None
        }
    
    report = {
        'timestamp': datetime.now().isoformat(),
        'test_summary': {
            'selenium_tests': test_results,
            'health_checks': health_results,
            'api_performance': api_results
        },
        'latency_analysis': {
            'overall_statistics': overall_latency_stats,
            'endpoint_performance': api_results,
            'performance_recommendations': generate_performance_recommendations(api_results, health_results)
        },
        'recommendations': []
    }
    
    # Add recommendations based on results
    if health_results:
        unhealthy_services = [service for service, status in health_results.items() if status != "Healthy"]
        if unhealthy_services:
            report['recommendations'].append(f"Fix unhealthy services: {', '.join(unhealthy_services)}")
    
    if api_results:
        slow_endpoints = [name for name, data in api_results.items() if data['average_time'] > 1.0]
        if slow_endpoints:
            report['recommendations'].append(f"Optimize slow endpoints: {', '.join(slow_endpoints)}")
        
        low_success_endpoints = [name for name, data in api_results.items() if data['success_rate'] < 95]
        if low_success_endpoints:
            report['recommendations'].append(f"Investigate low success rate endpoints: {', '.join(low_success_endpoints)}")
    
    # Save comprehensive report
    with open('reports/comprehensive_latency_report.json', 'w') as f:
        json.dump(report, f, indent=2)
    
    # Generate latency summary
    generate_latency_summary(api_results, health_results)
    
    print(f"✅ Comprehensive latency report saved to reports/comprehensive_latency_report.json")
    return report

def generate_performance_recommendations(api_results, health_results):
    """Generate performance recommendations based on latency data"""
    recommendations = []
    
    if not api_results:
        return recommendations
    
    # Check for high latency endpoints
    high_latency_threshold = 1000  # 1 second
    high_latency_endpoints = []
    for endpoint, data in api_results.items():
        if data['average_time'] > high_latency_threshold:
            high_latency_endpoints.append(f"{endpoint} ({data['average_time']:.2f}ms)")
    
    if high_latency_endpoints:
        recommendations.append({
            'type': 'high_latency',
            'severity': 'high',
            'message': f"High latency detected: {', '.join(high_latency_endpoints)}",
            'suggestion': "Consider database optimization, caching, or service scaling"
        })
    
    # Check for low success rates
    low_success_endpoints = []
    for endpoint, data in api_results.items():
        if data['success_rate'] < 95:
            low_success_endpoints.append(f"{endpoint} ({data['success_rate']:.1f}%)")
    
    if low_success_endpoints:
        recommendations.append({
            'type': 'low_success_rate',
            'severity': 'high',
            'message': f"Low success rate detected: {', '.join(low_success_endpoints)}",
            'suggestion': "Investigate error logs and service health"
        })
    
    # Check for high P95 latency
    high_p95_endpoints = []
    for endpoint, data in api_results.items():
        if data['p95_time'] > 2000:  # 2 seconds
            high_p95_endpoints.append(f"{endpoint} (P95: {data['p95_time']:.2f}ms)")
    
    if high_p95_endpoints:
        recommendations.append({
            'type': 'high_p95_latency',
            'severity': 'medium',
            'message': f"High P95 latency detected: {', '.join(high_p95_endpoints)}",
            'suggestion': "Consider load balancing or resource scaling"
        })
    
    return recommendations

def generate_latency_summary(api_results, health_results):
    """Generate a human-readable latency summary"""
    summary_file = "reports/latency_summary.txt"
    
    with open(summary_file, 'w') as f:
        f.write("=" * 60 + "\n")
        f.write("PURELY E-commerce - Latency Test Summary\n")
        f.write("=" * 60 + "\n")
        f.write(f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
        
        # Health Check Summary
        f.write("HEALTH CHECKS:\n")
        f.write("-" * 20 + "\n")
        if health_results:
            healthy_count = sum(1 for status in health_results.values() if status == "Healthy")
            total_count = len(health_results)
            f.write(f"Services Healthy: {healthy_count}/{total_count}\n")
            for service, status in health_results.items():
                f.write(f"  {service}: {status}\n")
        f.write("\n")
        
        # API Performance Summary
        f.write("API PERFORMANCE:\n")
        f.write("-" * 20 + "\n")
        if api_results:
            total_requests = sum(data['total_requests'] for data in api_results.values())
            avg_response_time = sum(data['average_time'] for data in api_results.values()) / len(api_results)
            overall_success_rate = sum(data['success_rate'] for data in api_results.values()) / len(api_results)
            
            f.write(f"Total API Requests: {total_requests}\n")
            f.write(f"Average Response Time: {avg_response_time:.2f}ms\n")
            f.write(f"Overall Success Rate: {overall_success_rate:.1f}%\n\n")
            
            f.write("Endpoint Details:\n")
            for endpoint, data in api_results.items():
                f.write(f"  {endpoint}:\n")
                f.write(f"    Avg Time: {data['average_time']:.2f}ms\n")
                f.write(f"    P95 Time: {data['p95_time']:.2f}ms\n")
                f.write(f"    P99 Time: {data['p99_time']:.2f}ms\n")
                f.write(f"    Success Rate: {data['success_rate']:.1f}%\n")
                f.write(f"    Response Size: {data['response_size']:.0f} bytes\n\n")
        
        # Recommendations
        f.write("RECOMMENDATIONS:\n")
        f.write("-" * 20 + "\n")
        if api_results:
            slow_endpoints = [name for name, data in api_results.items() if data['average_time'] > 1000]
            if slow_endpoints:
                f.write(f"• Optimize slow endpoints: {', '.join(slow_endpoints)}\n")
            
            low_success_endpoints = [name for name, data in api_results.items() if data['success_rate'] < 95]
            if low_success_endpoints:
                f.write(f"• Investigate low success rate endpoints: {', '.join(low_success_endpoints)}\n")
        
        if health_results:
            unhealthy_services = [service for service, status in health_results.items() if status != "Healthy"]
            if unhealthy_services:
                f.write(f"• Fix unhealthy services: {', '.join(unhealthy_services)}\n")
    
    print(f"✅ Latency summary saved to {summary_file}")

def main():
    """Main function"""
    parser = argparse.ArgumentParser(description='PURELY E-commerce Load Testing Runner with Latency Monitoring')
    parser.add_argument('--test-type', choices=['smoke', 'regression', 'performance', 'api', 'ui', 'e2e', 'all'], 
                       default='smoke', help='Type of tests to run')
    parser.add_argument('--headless', action='store_true', help='Run browser in headless mode')
    parser.add_argument('--parallel', action='store_true', help='Run tests in parallel')
    parser.add_argument('--load-test', action='store_true', help='Run load tests')
    parser.add_argument('--users', type=int, default=10, help='Number of users for load test')
    parser.add_argument('--spawn-rate', type=int, default=2, help='User spawn rate for load test')
    parser.add_argument('--run-time', default='60s', help='Load test duration')
    parser.add_argument('--host', default='http://localhost', help='Target host for load test')
    parser.add_argument('--health-check', action='store_true', help='Run health checks')
    parser.add_argument('--api-performance', action='store_true', help='Run API performance tests')
    parser.add_argument('--latency-only', action='store_true', help='Run only latency monitoring tests')
    
    args = parser.parse_args()
    
    print("🚀 PURELY E-commerce Load Testing Suite with Latency Monitoring")
    print("=" * 70)
    
    # Create directories
    create_directories()
    
    # Generate curl format file
    generate_curl_format_file()
    
    test_results = {}
    health_results = {}
    api_results = {}
    
    # Run health checks if requested
    if args.health_check or args.test_type == 'all' or args.latency_only:
        health_results = run_health_checks()
    
    # Run Selenium tests
    if args.test_type in ['smoke', 'regression', 'performance', 'api', 'ui', 'e2e'] and not args.latency_only:
        test_results = run_selenium_tests(args.test_type, args.headless, args.parallel)
    elif args.test_type == 'all' and not args.latency_only:
        test_types = ['smoke', 'api', 'ui']
        for test_type in test_types:
            result = run_selenium_tests(test_type, args.headless, args.parallel)
            test_results[test_type] = result
    
    # Run load tests
    if args.load_test or args.test_type == 'all':
        load_result = run_locust_load_test(args.users, args.spawn_rate, args.run_time, args.host)
        test_results['load_test'] = load_result
    
    # Run API performance tests
    if args.api_performance or args.test_type == 'all' or args.latency_only:
        api_results = run_api_performance_test()
    
    # Generate comprehensive latency report
    report = generate_latency_report(test_results, health_results, api_results)
    
    # Print summary
    print(f"\n📋 Test Summary")
    print("=" * 70)
    print(f"Test Type: {args.test_type}")
    print(f"Headless Mode: {args.headless}")
    print(f"Parallel Execution: {args.parallel}")
    
    if health_results:
        healthy_count = sum(1 for status in health_results.values() if status == "Healthy")
        total_count = len(health_results)
        print(f"Health Checks: {healthy_count}/{total_count} services healthy")
    
    if api_results:
        print(f"API Performance Tests: {len(api_results)} endpoints tested")
        avg_latency = sum(data['average_time'] for data in api_results.values()) / len(api_results)
        print(f"Average API Response Time: {avg_latency:.2f}ms")
    
    if report['recommendations']:
        print(f"\n💡 Recommendations:")
        for rec in report['recommendations']:
            print(f"  - {rec}")
    
    print(f"\n✅ Testing completed! Check reports/ directory for detailed results.")
    print(f"📊 Latency reports available:")
    print(f"  - reports/comprehensive_latency_report.json")
    print(f"  - reports/latency_summary.txt")

if __name__ == "__main__":
    main() 