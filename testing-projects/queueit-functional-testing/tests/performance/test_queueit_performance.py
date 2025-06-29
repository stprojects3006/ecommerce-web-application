"""
Performance Tests for Queue-it Integration
Load testing and performance analysis for Queue-it functionality
"""

import pytest
import time
import json
import concurrent.futures
import threading
from typing import Dict, Any, List, Optional
from datetime import datetime, timedelta
import statistics
import matplotlib.pyplot as plt
import pandas as pd

from utils.test_helpers import (
    config, performance_monitor, retry_helper,
    AssertionHelper, TestDataGenerator, api_client
)
from utils.grafana_client import metrics_collector


class QueueItLoadTest:
    """Base class for Queue-it load testing"""
    
    def __init__(self):
        self.test_start_time = None
        self.test_end_time = None
        self.results = []
        self.errors = []
    
    def start_test(self, test_name: str):
        """Start a load test"""
        self.test_start_time = time.time()
        metrics_collector.start_test_session(test_name)
        logger.info(f"Started load test: {test_name}")
    
    def end_test(self, test_name: str, success: bool = True):
        """End a load test"""
        self.test_end_time = time.time()
        metrics_collector.end_test_session(test_name, success)
        logger.info(f"Ended load test: {test_name}")
    
    def record_result(self, operation: str, duration: float, success: bool, metadata: Dict[str, Any] = None):
        """Record a test result"""
        result = {
            'operation': operation,
            'duration': duration,
            'success': success,
            'timestamp': datetime.now().isoformat(),
            'metadata': metadata or {}
        }
        self.results.append(result)
        
        if not success:
            self.errors.append(result)
    
    def calculate_statistics(self) -> Dict[str, float]:
        """Calculate performance statistics"""
        if not self.results:
            return {}
        
        durations = [r['duration'] for r in self.results if r['success']]
        if not durations:
            return {}
        
        return {
            'count': len(durations),
            'min': min(durations),
            'max': max(durations),
            'mean': statistics.mean(durations),
            'median': statistics.median(durations),
            'std_dev': statistics.stdev(durations) if len(durations) > 1 else 0,
            'p95': sorted(durations)[int(len(durations) * 0.95)],
            'p99': sorted(durations)[int(len(durations) * 0.99)],
            'success_rate': len(durations) / len(self.results) * 100
        }
    
    def generate_performance_report(self, test_name: str) -> str:
        """Generate performance test report"""
        stats = self.calculate_statistics()
        
        if not stats:
            return "No results to report"
        
        report = f"""
        Performance Test Report: {test_name}
        ======================================
        
        Test Duration: {self.test_end_time - self.test_start_time:.2f} seconds
        Total Operations: {len(self.results)}
        Successful Operations: {stats['count']}
        Failed Operations: {len(self.errors)}
        Success Rate: {stats['success_rate']:.2f}%
        
        Response Time Statistics (seconds):
        - Minimum: {stats['min']:.3f}
        - Maximum: {stats['max']:.3f}
        - Mean: {stats['mean']:.3f}
        - Median: {stats['median']:.3f}
        - Standard Deviation: {stats['std_dev']:.3f}
        - 95th Percentile: {stats['p95']:.3f}
        - 99th Percentile: {stats['p99']:.3f}
        
        Throughput: {stats['count'] / (self.test_end_time - self.test_start_time):.2f} ops/sec
        """
        
        return report


class TestQueueItAPIPerformance:
    """Test Queue-it API performance under load"""
    
    def test_api_health_performance(self):
        """Test API health endpoint performance"""
        load_test = QueueItLoadTest()
        load_test.start_test("API Health Performance Test")
        
        try:
            num_requests = 100
            concurrent_users = 10
            
            def make_health_request():
                start_time = time.time()
                try:
                    response = api_client.health_check()
                    duration = time.time() - start_time
                    
                    success = response['status_code'] == 200
                    load_test.record_result(
                        'health_check',
                        duration,
                        success,
                        {'status_code': response['status_code']}
                    )
                    
                    return success
                except Exception as e:
                    duration = time.time() - start_time
                    load_test.record_result(
                        'health_check',
                        duration,
                        False,
                        {'error': str(e)}
                    )
                    return False
            
            # Execute concurrent requests
            with concurrent.futures.ThreadPoolExecutor(max_workers=concurrent_users) as executor:
                futures = [executor.submit(make_health_request) for _ in range(num_requests)]
                results = [future.result() for future in concurrent.futures.as_completed(futures)]
            
            # Calculate and assert performance
            stats = load_test.calculate_statistics()
            
            # Performance assertions
            assert stats['success_rate'] >= 95, f"Success rate should be >= 95%, got {stats['success_rate']}%"
            assert stats['p95'] <= 2.0, f"95th percentile should be <= 2s, got {stats['p95']}s"
            assert stats['mean'] <= 1.0, f"Mean response time should be <= 1s, got {stats['mean']}s"
            
            # Record metrics
            performance_monitor.record_metric('api_health_p95', stats['p95'])
            performance_monitor.record_metric('api_health_mean', stats['mean'])
            performance_monitor.record_metric('api_health_throughput', stats['count'] / (load_test.test_end_time - load_test.test_start_time))
            
            metrics_collector.record_test_metric(
                'api_health_performance',
                stats['p95'],
                {
                    'test_type': 'load_test',
                    'num_requests': num_requests,
                    'concurrent_users': concurrent_users,
                    'success_rate': stats['success_rate']
                }
            )
            
            # Generate report
            report = load_test.generate_performance_report("API Health Performance")
            logger.info(report)
            
        finally:
            load_test.end_test("API Health Performance Test", success=True)
    
    def test_enqueue_performance(self):
        """Test user enqueueing performance"""
        load_test = QueueItLoadTest()
        load_test.start_test("Enqueue Performance Test")
        
        try:
            num_requests = 50  # Lower number due to queue state changes
            concurrent_users = 5
            event_id = "flash-sale-2024"
            
            def make_enqueue_request():
                start_time = time.time()
                try:
                    user_data = TestDataGenerator.generate_user_data()
                    response = api_client.enqueue_user(event_id, user_data)
                    duration = time.time() - start_time
                    
                    success = response['status_code'] == 200
                    load_test.record_result(
                        'enqueue_user',
                        duration,
                        success,
                        {
                            'status_code': response['status_code'],
                            'position': response['data']['position'] if success else None
                        }
                    )
                    
                    return success
                except Exception as e:
                    duration = time.time() - start_time
                    load_test.record_result(
                        'enqueue_user',
                        duration,
                        False,
                        {'error': str(e)}
                    )
                    return False
            
            # Execute concurrent requests
            with concurrent.futures.ThreadPoolExecutor(max_workers=concurrent_users) as executor:
                futures = [executor.submit(make_enqueue_request) for _ in range(num_requests)]
                results = [future.result() for future in concurrent.futures.as_completed(futures)]
            
            # Calculate and assert performance
            stats = load_test.calculate_statistics()
            
            # Performance assertions
            assert stats['success_rate'] >= 90, f"Success rate should be >= 90%, got {stats['success_rate']}%"
            assert stats['p95'] <= 5.0, f"95th percentile should be <= 5s, got {stats['p95']}s"
            assert stats['mean'] <= 3.0, f"Mean response time should be <= 3s, got {stats['mean']}s"
            
            # Record metrics
            performance_monitor.record_metric('enqueue_p95', stats['p95'])
            performance_monitor.record_metric('enqueue_mean', stats['mean'])
            performance_monitor.record_metric('enqueue_throughput', stats['count'] / (load_test.test_end_time - load_test.test_start_time))
            
            metrics_collector.record_test_metric(
                'enqueue_performance',
                stats['p95'],
                {
                    'test_type': 'load_test',
                    'num_requests': num_requests,
                    'concurrent_users': concurrent_users,
                    'success_rate': stats['success_rate']
                }
            )
            
            # Generate report
            report = load_test.generate_performance_report("Enqueue Performance")
            logger.info(report)
            
        finally:
            load_test.end_test("Enqueue Performance Test", success=True)
    
    def test_position_check_performance(self):
        """Test position checking performance"""
        load_test = QueueItLoadTest()
        load_test.start_test("Position Check Performance Test")
        
        try:
            # First, create some tokens for testing
            event_id = "flash-sale-2024"
            tokens = []
            
            for i in range(10):
                user_data = TestDataGenerator.generate_user_data()
                response = api_client.enqueue_user(event_id, user_data)
                if response['status_code'] == 200:
                    tokens.append(response['data']['queueToken'])
            
            if not tokens:
                pytest.skip("No tokens available for position check testing")
            
            num_requests = 100
            concurrent_users = 10
            
            def make_position_request():
                start_time = time.time()
                try:
                    token = tokens[hash(threading.current_thread().ident) % len(tokens)]
                    response = api_client.check_position(event_id, token)
                    duration = time.time() - start_time
                    
                    success = response['status_code'] == 200
                    load_test.record_result(
                        'check_position',
                        duration,
                        success,
                        {
                            'status_code': response['status_code'],
                            'position': response['data']['position'] if success else None
                        }
                    )
                    
                    return success
                except Exception as e:
                    duration = time.time() - start_time
                    load_test.record_result(
                        'check_position',
                        duration,
                        False,
                        {'error': str(e)}
                    )
                    return False
            
            # Execute concurrent requests
            with concurrent.futures.ThreadPoolExecutor(max_workers=concurrent_users) as executor:
                futures = [executor.submit(make_position_request) for _ in range(num_requests)]
                results = [future.result() for future in concurrent.futures.as_completed(futures)]
            
            # Calculate and assert performance
            stats = load_test.calculate_statistics()
            
            # Performance assertions
            assert stats['success_rate'] >= 95, f"Success rate should be >= 95%, got {stats['success_rate']}%"
            assert stats['p95'] <= 3.0, f"95th percentile should be <= 3s, got {stats['p95']}s"
            assert stats['mean'] <= 2.0, f"Mean response time should be <= 2s, got {stats['mean']}s"
            
            # Record metrics
            performance_monitor.record_metric('position_check_p95', stats['p95'])
            performance_monitor.record_metric('position_check_mean', stats['mean'])
            performance_monitor.record_metric('position_check_throughput', stats['count'] / (load_test.test_end_time - load_test.test_start_time))
            
            metrics_collector.record_test_metric(
                'position_check_performance',
                stats['p95'],
                {
                    'test_type': 'load_test',
                    'num_requests': num_requests,
                    'concurrent_users': concurrent_users,
                    'success_rate': stats['success_rate']
                }
            )
            
            # Generate report
            report = load_test.generate_performance_report("Position Check Performance")
            logger.info(report)
            
        finally:
            load_test.end_test("Position Check Performance Test", success=True)


class TestQueueItStressTesting:
    """Test Queue-it under stress conditions"""
    
    def test_high_concurrency_stress(self):
        """Test Queue-it under high concurrency stress"""
        load_test = QueueItLoadTest()
        load_test.start_test("High Concurrency Stress Test")
        
        try:
            num_requests = 200
            concurrent_users = 50  # High concurrency
            
            def make_stress_request():
                start_time = time.time()
                try:
                    # Mix of different operations
                    operation = hash(threading.current_thread().ident) % 3
                    
                    if operation == 0:
                        # Health check
                        response = api_client.health_check()
                        op_name = 'health_check'
                    elif operation == 1:
                        # Enqueue user
                        user_data = TestDataGenerator.generate_user_data()
                        response = api_client.enqueue_user("flash-sale-2024", user_data)
                        op_name = 'enqueue_user'
                    else:
                        # Get queue status
                        response = api_client.get_queue_status("flash-sale-2024")
                        op_name = 'get_status'
                    
                    duration = time.time() - start_time
                    success = response['status_code'] in [200, 201]
                    
                    load_test.record_result(
                        op_name,
                        duration,
                        success,
                        {'status_code': response['status_code']}
                    )
                    
                    return success
                except Exception as e:
                    duration = time.time() - start_time
                    load_test.record_result(
                        'stress_request',
                        duration,
                        False,
                        {'error': str(e)}
                    )
                    return False
            
            # Execute high concurrency requests
            with concurrent.futures.ThreadPoolExecutor(max_workers=concurrent_users) as executor:
                futures = [executor.submit(make_stress_request) for _ in range(num_requests)]
                results = [future.result() for future in concurrent.futures.as_completed(futures)]
            
            # Calculate and assert performance
            stats = load_test.calculate_statistics()
            
            # Stress test assertions (more lenient)
            assert stats['success_rate'] >= 80, f"Success rate should be >= 80%, got {stats['success_rate']}%"
            assert stats['p95'] <= 10.0, f"95th percentile should be <= 10s, got {stats['p95']}s"
            
            # Record metrics
            performance_monitor.record_metric('stress_test_p95', stats['p95'])
            performance_monitor.record_metric('stress_test_mean', stats['mean'])
            performance_monitor.record_metric('stress_test_throughput', stats['count'] / (load_test.test_end_time - load_test.test_start_time))
            
            metrics_collector.record_test_metric(
                'stress_test_performance',
                stats['p95'],
                {
                    'test_type': 'stress_test',
                    'num_requests': num_requests,
                    'concurrent_users': concurrent_users,
                    'success_rate': stats['success_rate']
                }
            )
            
            # Generate report
            report = load_test.generate_performance_report("High Concurrency Stress Test")
            logger.info(report)
            
        finally:
            load_test.end_test("High Concurrency Stress Test", success=True)
    
    def test_endurance_test(self):
        """Test Queue-it endurance over time"""
        load_test = QueueItLoadTest()
        load_test.start_test("Endurance Test")
        
        try:
            test_duration = 60  # 1 minute endurance test
            requests_per_second = 5
            total_requests = test_duration * requests_per_second
            
            def make_endurance_request():
                start_time = time.time()
                try:
                    # Mix of operations
                    operation = hash(threading.current_thread().ident) % 4
                    
                    if operation == 0:
                        response = api_client.health_check()
                        op_name = 'health_check'
                    elif operation == 1:
                        response = api_client.get_queue_status("flash-sale-2024")
                        op_name = 'get_status'
                    elif operation == 2:
                        response = api_client.get_queue_stats("flash-sale-2024")
                        op_name = 'get_stats'
                    else:
                        user_data = TestDataGenerator.generate_user_data()
                        response = api_client.enqueue_user("flash-sale-2024", user_data)
                        op_name = 'enqueue_user'
                    
                    duration = time.time() - start_time
                    success = response['status_code'] in [200, 201]
                    
                    load_test.record_result(
                        op_name,
                        duration,
                        success,
                        {'status_code': response['status_code']}
                    )
                    
                    return success
                except Exception as e:
                    duration = time.time() - start_time
                    load_test.record_result(
                        'endurance_request',
                        duration,
                        False,
                        {'error': str(e)}
                    )
                    return False
            
            # Execute endurance test
            start_time = time.time()
            with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
                futures = []
                
                while time.time() - start_time < test_duration:
                    future = executor.submit(make_endurance_request)
                    futures.append(future)
                    time.sleep(1.0 / requests_per_second)  # Rate limiting
                
                results = [future.result() for future in concurrent.futures.as_completed(futures)]
            
            # Calculate and assert performance
            stats = load_test.calculate_statistics()
            
            # Endurance test assertions
            assert stats['success_rate'] >= 90, f"Success rate should be >= 90%, got {stats['success_rate']}%"
            assert stats['p95'] <= 5.0, f"95th percentile should be <= 5s, got {stats['p95']}s"
            
            # Check for performance degradation over time
            if len(load_test.results) > 10:
                first_half = load_test.results[:len(load_test.results)//2]
                second_half = load_test.results[len(load_test.results)//2:]
                
                first_durations = [r['duration'] for r in first_half if r['success']]
                second_durations = [r['duration'] for r in second_half if r['success']]
                
                if first_durations and second_durations:
                    first_mean = statistics.mean(first_durations)
                    second_mean = statistics.mean(second_durations)
                    
                    # Performance should not degrade more than 50%
                    degradation = (second_mean - first_mean) / first_mean
                    assert degradation <= 0.5, f"Performance degradation should be <= 50%, got {degradation*100}%"
            
            # Record metrics
            performance_monitor.record_metric('endurance_test_p95', stats['p95'])
            performance_monitor.record_metric('endurance_test_mean', stats['mean'])
            performance_monitor.record_metric('endurance_test_throughput', stats['count'] / (load_test.test_end_time - load_test.test_start_time))
            
            metrics_collector.record_test_metric(
                'endurance_test_performance',
                stats['p95'],
                {
                    'test_type': 'endurance_test',
                    'test_duration': test_duration,
                    'requests_per_second': requests_per_second,
                    'success_rate': stats['success_rate']
                }
            )
            
            # Generate report
            report = load_test.generate_performance_report("Endurance Test")
            logger.info(report)
            
        finally:
            load_test.end_test("Endurance Test", success=True)


class TestQueueItMemoryLeakDetection:
    """Test for memory leaks in Queue-it integration"""
    
    def test_memory_usage_over_time(self):
        """Test memory usage over time to detect leaks"""
        load_test = QueueItLoadTest()
        load_test.start_test("Memory Leak Detection Test")
        
        try:
            # This test would typically monitor memory usage
            # For now, we'll test that repeated operations don't cause errors
            
            num_iterations = 50
            operations_per_iteration = 10
            
            for iteration in range(num_iterations):
                iteration_start = time.time()
                
                # Perform multiple operations
                for op in range(operations_per_iteration):
                    start_time = time.time()
                    try:
                        # Mix of operations
                        if op % 3 == 0:
                            response = api_client.health_check()
                            op_name = 'health_check'
                        elif op % 3 == 1:
                            response = api_client.get_queue_status("flash-sale-2024")
                            op_name = 'get_status'
                        else:
                            user_data = TestDataGenerator.generate_user_data()
                            response = api_client.enqueue_user("flash-sale-2024", user_data)
                            op_name = 'enqueue_user'
                        
                        duration = time.time() - start_time
                        success = response['status_code'] in [200, 201]
                        
                        load_test.record_result(
                            op_name,
                            duration,
                            success,
                            {'iteration': iteration, 'operation': op}
                        )
                        
                    except Exception as e:
                        duration = time.time() - start_time
                        load_test.record_result(
                            'memory_test_op',
                            duration,
                            False,
                            {'iteration': iteration, 'operation': op, 'error': str(e)}
                        )
                
                # Small delay between iterations
                time.sleep(0.1)
                
                # Record iteration metrics
                iteration_duration = time.time() - iteration_start
                performance_monitor.record_metric('memory_test_iteration_duration', iteration_duration)
            
            # Calculate and assert performance
            stats = load_test.calculate_statistics()
            
            # Memory leak test assertions
            assert stats['success_rate'] >= 95, f"Success rate should be >= 95%, got {stats['success_rate']}%"
            
            # Check for performance degradation over iterations
            if len(load_test.results) > 20:
                first_quarter = load_test.results[:len(load_test.results)//4]
                last_quarter = load_test.results[3*len(load_test.results)//4:]
                
                first_durations = [r['duration'] for r in first_quarter if r['success']]
                last_durations = [r['duration'] for r in last_quarter if r['success']]
                
                if first_durations and last_durations:
                    first_mean = statistics.mean(first_durations)
                    last_mean = statistics.mean(last_durations)
                    
                    # Performance should not degrade significantly
                    degradation = (last_mean - first_mean) / first_mean
                    assert degradation <= 0.3, f"Performance degradation should be <= 30%, got {degradation*100}%"
            
            # Record metrics
            performance_monitor.record_metric('memory_test_p95', stats['p95'])
            performance_monitor.record_metric('memory_test_mean', stats['mean'])
            
            metrics_collector.record_test_metric(
                'memory_leak_test_performance',
                stats['p95'],
                {
                    'test_type': 'memory_leak_test',
                    'num_iterations': num_iterations,
                    'operations_per_iteration': operations_per_iteration,
                    'success_rate': stats['success_rate']
                }
            )
            
            # Generate report
            report = load_test.generate_performance_report("Memory Leak Detection Test")
            logger.info(report)
            
        finally:
            load_test.end_test("Memory Leak Detection Test", success=True)


class TestQueueItPerformanceAnalysis:
    """Generate performance analysis and reports"""
    
    def test_performance_analysis_report(self):
        """Generate comprehensive performance analysis report"""
        # This test collects data from previous tests and generates analysis
        
        # Collect all performance metrics
        all_metrics = performance_monitor.get_metrics()
        
        if not all_metrics:
            pytest.skip("No performance metrics available for analysis")
        
        # Group metrics by type
        metric_groups = {}
        for metric in all_metrics:
            metric_type = metric['name'].split('_')[0] if '_' in metric['name'] else 'other'
            if metric_type not in metric_groups:
                metric_groups[metric_type] = []
            metric_groups[metric_type].append(metric)
        
        # Generate analysis
        analysis = {
            'total_metrics': len(all_metrics),
            'metric_types': len(metric_groups),
            'summary': {}
        }
        
        for metric_type, metrics in metric_groups.items():
            values = [m['value'] for m in metrics]
            analysis['summary'][metric_type] = {
                'count': len(values),
                'min': min(values),
                'max': max(values),
                'mean': statistics.mean(values),
                'median': statistics.median(values)
            }
        
        # Generate performance report
        report = f"""
        Queue-it Performance Analysis Report
        ===================================
        
        Total Metrics Collected: {analysis['total_metrics']}
        Metric Types: {analysis['metric_types']}
        
        Performance Summary:
        """
        
        for metric_type, stats in analysis['summary'].items():
            report += f"""
        {metric_type.upper()}:
        - Count: {stats['count']}
        - Min: {stats['min']:.3f}
        - Max: {stats['max']:.3f}
        - Mean: {stats['mean']:.3f}
        - Median: {stats['median']:.3f}
        """
        
        # Save analysis to file
        with open('reports/performance_analysis.json', 'w') as f:
            json.dump(analysis, f, indent=2)
        
        # Generate visualization (if matplotlib is available)
        try:
            self._generate_performance_charts(all_metrics)
        except Exception as e:
            logger.warning(f"Could not generate performance charts: {e}")
        
        logger.info(report)
        
        # Record analysis metrics
        metrics_collector.record_test_metric(
            'performance_analysis_complete',
            1,
            {
                'total_metrics': analysis['total_metrics'],
                'metric_types': analysis['metric_types']
            }
        )
    
    def _generate_performance_charts(self, metrics: List[Dict[str, Any]]):
        """Generate performance visualization charts"""
        # Create DataFrame for analysis
        df = pd.DataFrame(metrics)
        df['timestamp'] = pd.to_datetime(df['timestamp'])
        
        # Group by metric name
        metric_names = df['name'].unique()
        
        # Create subplots for each metric type
        fig, axes = plt.subplots(2, 2, figsize=(15, 10))
        fig.suptitle('Queue-it Performance Metrics', fontsize=16)
        
        for i, metric_name in enumerate(metric_names[:4]):  # Show first 4 metrics
            ax = axes[i // 2, i % 2]
            metric_data = df[df['name'] == metric_name]
            
            ax.plot(metric_data['timestamp'], metric_data['value'], marker='o')
            ax.set_title(f'{metric_name}')
            ax.set_xlabel('Time')
            ax.set_ylabel('Value')
            ax.tick_params(axis='x', rotation=45)
        
        plt.tight_layout()
        plt.savefig('reports/performance_charts.png', dpi=300, bbox_inches='tight')
        plt.close()
        
        logger.info("Performance charts generated: reports/performance_charts.png")


# Test fixtures
@pytest.fixture(scope="session")
def setup_performance_test_session():
    """Setup performance test session"""
    metrics_collector.start_test_session("Queue-it Performance Tests")
    yield
    metrics_collector.end_test_session("Queue-it Performance Tests", success=True)


@pytest.fixture(autouse=True)
def setup_performance_test():
    """Setup for each performance test"""
    performance_monitor.metrics.clear()
    yield


# Run tests
if __name__ == "__main__":
    pytest.main([__file__, "-v", "--html=reports/performance_report.html"]) 