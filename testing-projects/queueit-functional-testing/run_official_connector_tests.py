#!/usr/bin/env python3
"""
Queue-It Official Connector Test Runner
Runs comprehensive tests for both backend and frontend Queue-It integration
"""

import subprocess
import sys
import os
import time
import json
from typing import Dict, Any, List

class QueueItOfficialTestRunner:
    """Test runner for Queue-It official connector tests"""
    
    def __init__(self):
        self.test_results = {}
        self.start_time = time.time()
        
    def run_backend_tests(self) -> Dict[str, Any]:
        """Run backend Queue-It tests"""
        print("🔧 Running Queue-It Backend Official Connector Tests...")
        
        try:
            # Run the backend test script
            result = subprocess.run([
                sys.executable, 
                'tests/backend/test_queueit_official_connector.py'
            ], capture_output=True, text=True, cwd=os.getcwd())
            
            if result.returncode == 0:
                print("✅ Backend tests completed successfully")
                return {'status': 'PASS', 'output': result.stdout}
            else:
                print(f"❌ Backend tests failed: {result.stderr}")
                return {'status': 'FAIL', 'error': result.stderr}
                
        except Exception as e:
            print(f"❌ Backend test execution failed: {str(e)}")
            return {'status': 'FAIL', 'error': str(e)}
    
    def run_frontend_tests(self) -> Dict[str, Any]:
        """Run frontend Queue-It tests"""
        print("🎨 Running Queue-It Frontend Official Connector Tests...")
        
        try:
            # Check if Node.js and npm are available
            node_check = subprocess.run(['node', '--version'], capture_output=True, text=True)
            if node_check.returncode != 0:
                return {'status': 'SKIP', 'reason': 'Node.js not available'}
            
            # Install dependencies if needed
            if not os.path.exists('node_modules'):
                print("📦 Installing Node.js dependencies...")
                subprocess.run(['npm', 'install'], check=True)
            
            # Run the frontend test script
            result = subprocess.run([
                'node', 
                'tests/frontend/test_queueit_frontend_official.js'
            ], capture_output=True, text=True, cwd=os.getcwd())
            
            if result.returncode == 0:
                print("✅ Frontend tests completed successfully")
                return {'status': 'PASS', 'output': result.stdout}
            else:
                print(f"❌ Frontend tests failed: {result.stderr}")
                return {'status': 'FAIL', 'error': result.stderr}
                
        except Exception as e:
            print(f"❌ Frontend test execution failed: {str(e)}")
            return {'status': 'FAIL', 'error': str(e)}
    
    def run_integration_tests(self) -> Dict[str, Any]:
        """Run integration tests"""
        print("🔗 Running Queue-It Integration Tests...")
        
        try:
            # Test API Gateway connectivity
            import requests
            
            api_url = "http://18.217.148.69:8081/api/queueit/health"
            response = requests.get(api_url, timeout=10)
            
            if response.status_code == 200:
                data = response.json()
                if data.get('connector') == 'official-java-connector':
                    print("✅ API Gateway Queue-It integration verified")
                    return {'status': 'PASS', 'data': data}
                else:
                    return {'status': 'FAIL', 'error': 'Official connector not detected'}
            else:
                return {'status': 'FAIL', 'error': f'API Gateway not responding: {response.status_code}'}
                
        except Exception as e:
            print(f"❌ Integration test failed: {str(e)}")
            return {'status': 'FAIL', 'error': str(e)}
    
    def run_performance_tests(self) -> Dict[str, Any]:
        """Run performance tests"""
        print("⚡ Running Queue-It Performance Tests...")
        
        try:
            import requests
            import time
            
            api_base = "http://18.217.148.69:8081/api/queueit"
            test_events = ['flash-sale-2024', 'black-friday-2024', 'checkout-protection']
            
            performance_results = {}
            
            for event_id in test_events:
                start_time = time.time()
                
                # Test status endpoint
                response = requests.get(f"{api_base}/status/{event_id}", timeout=5)
                status_time = time.time() - start_time
                
                # Test stats endpoint
                start_time = time.time()
                response = requests.get(f"{api_base}/stats/{event_id}", timeout=5)
                stats_time = time.time() - start_time
                
                performance_results[event_id] = {
                    'status_response_time': round(status_time * 1000, 2),  # ms
                    'stats_response_time': round(stats_time * 1000, 2),    # ms
                    'status_code': response.status_code
                }
            
            # Check performance thresholds
            all_fast = all(
                result['status_response_time'] < 500 and result['stats_response_time'] < 500
                for result in performance_results.values()
            )
            
            if all_fast:
                print("✅ Performance tests passed - all endpoints responding quickly")
                return {'status': 'PASS', 'results': performance_results}
            else:
                print("⚠️ Performance tests - some endpoints are slow")
                return {'status': 'WARN', 'results': performance_results}
                
        except Exception as e:
            print(f"❌ Performance test failed: {str(e)}")
            return {'status': 'FAIL', 'error': str(e)}
    
    def generate_test_report(self) -> Dict[str, Any]:
        """Generate comprehensive test report"""
        end_time = time.time()
        duration = end_time - self.start_time
        
        # Calculate overall results
        test_categories = ['backend', 'frontend', 'integration', 'performance']
        passed_tests = sum(1 for category in test_categories 
                          if self.test_results.get(category, {}).get('status') == 'PASS')
        total_tests = len(test_categories)
        
        overall_status = 'PASS' if passed_tests == total_tests else 'FAIL'
        
        report = {
            'overall_status': overall_status,
            'passed_tests': passed_tests,
            'total_tests': total_tests,
            'success_rate': f"{(passed_tests/total_tests)*100:.1f}%",
            'duration_seconds': round(duration, 2),
            'timestamp': time.strftime('%Y-%m-%d %H:%M:%S'),
            'test_results': self.test_results,
            'summary': {
                'backend': self.test_results.get('backend', {}).get('status', 'NOT_RUN'),
                'frontend': self.test_results.get('frontend', {}).get('status', 'NOT_RUN'),
                'integration': self.test_results.get('integration', {}).get('status', 'NOT_RUN'),
                'performance': self.test_results.get('performance', {}).get('status', 'NOT_RUN')
            }
        }
        
        return report
    
    def print_report(self, report: Dict[str, Any]):
        """Print formatted test report"""
        print("\n" + "="*80)
        print("QUEUE-IT OFFICIAL CONNECTOR COMPREHENSIVE TEST REPORT")
        print("="*80)
        print(f"Overall Status: {report['overall_status']}")
        print(f"Tests Passed: {report['passed_tests']}/{report['total_tests']}")
        print(f"Success Rate: {report['success_rate']}")
        print(f"Duration: {report['duration_seconds']} seconds")
        print(f"Timestamp: {report['timestamp']}")
        print("="*80)
        
        # Print category results
        for category, status in report['summary'].items():
            icon = "✅" if status == 'PASS' else "❌" if status == 'FAIL' else "⚠️" if status == 'WARN' else "⏭️"
            print(f"{icon} {category.upper()}: {status}")
        
        print("="*80)
        
        # Print detailed results if any failed
        if report['overall_status'] != 'PASS':
            print("\n📋 DETAILED RESULTS:")
            for category, result in report['test_results'].items():
                if result.get('status') != 'PASS':
                    print(f"\n{category.upper()}:")
                    if 'error' in result:
                        print(f"  Error: {result['error']}")
                    if 'output' in result:
                        print(f"  Output: {result['output'][:200]}...")
        
        print("="*80)
    
    def save_report(self, report: Dict[str, Any], filename: str = "queueit_official_test_report.json"):
        """Save test report to file"""
        try:
            with open(filename, 'w') as f:
                json.dump(report, f, indent=2)
            print(f"📄 Test report saved to: {filename}")
        except Exception as e:
            print(f"❌ Failed to save report: {str(e)}")
    
    def run_all_tests(self) -> Dict[str, Any]:
        """Run all Queue-It official connector tests"""
        print("🚀 Starting Queue-It Official Connector Comprehensive Test Suite")
        print("="*80)
        
        # Run all test categories
        self.test_results['backend'] = self.run_backend_tests()
        self.test_results['frontend'] = self.run_frontend_tests()
        self.test_results['integration'] = self.run_integration_tests()
        self.test_results['performance'] = self.run_performance_tests()
        
        # Generate and print report
        report = self.generate_test_report()
        self.print_report(report)
        
        # Save report
        self.save_report(report)
        
        return report

def main():
    """Main test runner"""
    runner = QueueItOfficialTestRunner()
    report = runner.run_all_tests()
    
    # Return appropriate exit code
    exit_code = 0 if report['overall_status'] == 'PASS' else 1
    sys.exit(exit_code)

if __name__ == "__main__":
    main() 