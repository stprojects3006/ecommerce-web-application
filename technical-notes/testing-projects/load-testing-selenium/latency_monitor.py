"""
Latency Monitoring and Reporting for PURELY E-commerce Load Testing
"""

import time
import json
import statistics
from datetime import datetime
from collections import defaultdict, deque
import matplotlib.pyplot as plt
import pandas as pd
from typing import Dict, List, Tuple, Optional
import logging

logger = logging.getLogger(__name__)

class LatencyMonitor:
    """Monitors and reports request/response latency"""
    
    def __init__(self, max_samples=1000):
        self.max_samples = max_samples
        self.latency_data = defaultdict(lambda: deque(maxlen=max_samples))
        self.request_times = defaultdict(list)
        self.response_times = defaultdict(list)
        self.error_counts = defaultdict(int)
        self.start_time = time.time()
        
    def record_request(self, endpoint: str, method: str, start_time: float):
        """Record the start of a request"""
        key = f"{method} {endpoint}"
        self.request_times[key].append(start_time)
        
    def record_response(self, endpoint: str, method: str, end_time: float, 
                       status_code: int, response_size: int = 0):
        """Record the end of a request and calculate latency"""
        key = f"{method} {endpoint}"
        
        if key in self.request_times and self.request_times[key]:
            start_time = self.request_times[key].pop(0)
            latency = (end_time - start_time) * 1000  # Convert to milliseconds
            
            self.latency_data[key].append({
                'latency': latency,
                'status_code': status_code,
                'response_size': response_size,
                'timestamp': end_time
            })
            
            if status_code >= 400:
                self.error_counts[key] += 1
                
    def get_latency_stats(self, endpoint: str, method: str) -> Dict:
        """Get latency statistics for a specific endpoint"""
        key = f"{method} {endpoint}"
        latencies = [item['latency'] for item in self.latency_data[key]]
        
        if not latencies:
            return {}
            
        return {
            'count': len(latencies),
            'min': min(latencies),
            'max': max(latencies),
            'mean': statistics.mean(latencies),
            'median': statistics.median(latencies),
            'p50': statistics.quantiles(latencies, n=2)[0] if len(latencies) > 1 else latencies[0],
            'p95': statistics.quantiles(latencies, n=20)[18] if len(latencies) > 19 else max(latencies),
            'p99': statistics.quantiles(latencies, n=100)[98] if len(latencies) > 99 else max(latencies),
            'std_dev': statistics.stdev(latencies) if len(latencies) > 1 else 0,
            'error_count': self.error_counts[key],
            'error_rate': (self.error_counts[key] / len(latencies)) * 100 if latencies else 0
        }
        
    def get_all_stats(self) -> Dict:
        """Get latency statistics for all endpoints"""
        all_stats = {}
        for key in self.latency_data.keys():
            method, endpoint = key.split(' ', 1)
            all_stats[key] = self.get_latency_stats(endpoint, method)
        return all_stats
        
    def generate_latency_report(self, output_file: str = "reports/latency_report.json"):
        """Generate comprehensive latency report"""
        stats = self.get_all_stats()
        
        # Calculate overall statistics
        all_latencies = []
        for key, data in self.latency_data.items():
            all_latencies.extend([item['latency'] for item in data])
            
        overall_stats = {
            'total_requests': len(all_latencies),
            'total_errors': sum(self.error_counts.values()),
            'overall_error_rate': (sum(self.error_counts.values()) / len(all_latencies)) * 100 if all_latencies else 0,
            'overall_avg_latency': statistics.mean(all_latencies) if all_latencies else 0,
            'overall_p95_latency': statistics.quantiles(all_latencies, n=20)[18] if len(all_latencies) > 19 else 0,
            'overall_p99_latency': statistics.quantiles(all_latencies, n=100)[98] if len(all_latencies) > 99 else 0,
        }
        
        report = {
            'timestamp': datetime.now().isoformat(),
            'test_duration': time.time() - self.start_time,
            'overall_statistics': overall_stats,
            'endpoint_statistics': stats,
            'recommendations': self._generate_recommendations(stats, overall_stats)
        }
        
        # Save report
        with open(output_file, 'w') as f:
            json.dump(report, f, indent=2)
            
        logger.info(f"Latency report saved to {output_file}")
        return report
        
    def _generate_recommendations(self, stats: Dict, overall_stats: Dict) -> List[str]:
        """Generate recommendations based on latency data"""
        recommendations = []
        
        # Check for high error rates
        if overall_stats['overall_error_rate'] > 5:
            recommendations.append(f"High error rate detected: {overall_stats['overall_error_rate']:.2f}%. Investigate service health and error logs.")
            
        # Check for slow endpoints
        slow_endpoints = []
        for endpoint, data in stats.items():
            if data.get('p95', 0) > 2000:  # 2 seconds
                slow_endpoints.append(f"{endpoint} (P95: {data['p95']:.2f}ms)")
                
        if slow_endpoints:
            recommendations.append(f"Slow endpoints detected: {', '.join(slow_endpoints)}. Consider optimization.")
            
        # Check for high latency variance
        high_variance_endpoints = []
        for endpoint, data in stats.items():
            if data.get('std_dev', 0) > data.get('mean', 0) * 0.5:  # High variance
                high_variance_endpoints.append(endpoint)
                
        if high_variance_endpoints:
            recommendations.append(f"High latency variance detected for: {', '.join(high_variance_endpoints)}. Check for resource contention.")
            
        # Performance recommendations
        if overall_stats['overall_avg_latency'] > 1000:
            recommendations.append("Average latency is high (>1s). Consider database optimization, caching, or scaling.")
            
        if overall_stats['overall_p99_latency'] > 5000:
            recommendations.append("P99 latency is very high (>5s). Investigate timeout issues and service bottlenecks.")
            
        return recommendations
        
    def generate_latency_charts(self, output_dir: str = "reports"):
        """Generate latency visualization charts"""
        try:
            import matplotlib.pyplot as plt
            import pandas as pd
        except ImportError:
            logger.warning("matplotlib/pandas not available. Skipping chart generation.")
            return
            
        # Create latency distribution chart
        plt.figure(figsize=(15, 10))
        
        # Subplot 1: Latency distribution by endpoint
        plt.subplot(2, 2, 1)
        endpoint_data = []
        for key, data in self.latency_data.items():
            latencies = [item['latency'] for item in data]
            endpoint_data.extend([(key, latency) for latency in latencies])
            
        if endpoint_data:
            df = pd.DataFrame(endpoint_data, columns=['endpoint', 'latency'])
            df.boxplot(column='latency', by='endpoint', ax=plt.gca())
            plt.title('Latency Distribution by Endpoint')
            plt.xticks(rotation=45)
            plt.ylabel('Latency (ms)')
            
        # Subplot 2: Response time trends
        plt.subplot(2, 2, 2)
        for key, data in self.latency_data.items():
            if data:
                timestamps = [item['timestamp'] - self.start_time for item in data]
                latencies = [item['latency'] for item in data]
                plt.plot(timestamps, latencies, label=key, alpha=0.7)
                
        plt.title('Response Time Trends')
        plt.xlabel('Time (seconds)')
        plt.ylabel('Latency (ms)')
        plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left')
        
        # Subplot 3: Error rates
        plt.subplot(2, 2, 3)
        endpoints = list(self.error_counts.keys())
        error_rates = []
        for endpoint in endpoints:
            total_requests = len(self.latency_data[endpoint])
            error_rate = (self.error_counts[endpoint] / total_requests * 100) if total_requests > 0 else 0
            error_rates.append(error_rate)
            
        if endpoints:
            plt.bar(endpoints, error_rates)
            plt.title('Error Rates by Endpoint')
            plt.xticks(rotation=45)
            plt.ylabel('Error Rate (%)')
            
        # Subplot 4: P95 vs P99 latency
        plt.subplot(2, 2, 4)
        stats = self.get_all_stats()
        endpoints = list(stats.keys())
        p95_values = [stats[endpoint].get('p95', 0) for endpoint in endpoints]
        p99_values = [stats[endpoint].get('p99', 0) for endpoint in endpoints]
        
        if endpoints:
            x = range(len(endpoints))
            width = 0.35
            plt.bar([i - width/2 for i in x], p95_values, width, label='P95', alpha=0.8)
            plt.bar([i + width/2 for i in x], p99_values, width, label='P99', alpha=0.8)
            plt.title('P95 vs P99 Latency')
            plt.xlabel('Endpoints')
            plt.ylabel('Latency (ms)')
            plt.xticks(x, endpoints, rotation=45)
            plt.legend()
            
        plt.tight_layout()
        plt.savefig(f'{output_dir}/latency_charts.png', dpi=300, bbox_inches='tight')
        plt.close()
        
        logger.info(f"Latency charts saved to {output_dir}/latency_charts.png")
        
    def export_latency_data(self, output_file: str = "reports/latency_data.csv"):
        """Export raw latency data to CSV for further analysis"""
        all_data = []
        for key, data in self.latency_data.items():
            method, endpoint = key.split(' ', 1)
            for item in data:
                all_data.append({
                    'timestamp': datetime.fromtimestamp(item['timestamp']).isoformat(),
                    'endpoint': endpoint,
                    'method': method,
                    'latency_ms': item['latency'],
                    'status_code': item['status_code'],
                    'response_size': item['response_size']
                })
                
        if all_data:
            import csv
            with open(output_file, 'w', newline='') as f:
                writer = csv.DictWriter(f, fieldnames=['timestamp', 'endpoint', 'method', 'latency_ms', 'status_code', 'response_size'])
                writer.writeheader()
                writer.writerows(all_data)
            logger.info(f"Latency data exported to {output_file}")
            
    def print_summary(self):
        """Print a summary of latency statistics"""
        stats = self.get_all_stats()
        
        print("\n" + "="*60)
        print("LATENCY MONITORING SUMMARY")
        print("="*60)
        
        # Overall statistics
        all_latencies = []
        for key, data in self.latency_data.items():
            all_latencies.extend([item['latency'] for item in data])
            
        if all_latencies:
            print(f"Total Requests: {len(all_latencies)}")
            print(f"Average Latency: {statistics.mean(all_latencies):.2f}ms")
            print(f"P95 Latency: {statistics.quantiles(all_latencies, n=20)[18]:.2f}ms" if len(all_latencies) > 19 else "P95 Latency: N/A")
            print(f"P99 Latency: {statistics.quantiles(all_latencies, n=100)[98]:.2f}ms" if len(all_latencies) > 99 else "P99 Latency: N/A")
            print(f"Total Errors: {sum(self.error_counts.values())}")
            print(f"Error Rate: {(sum(self.error_counts.values()) / len(all_latencies)) * 100:.2f}%")
            
        print("\nEndpoint Statistics:")
        print("-" * 60)
        for endpoint, data in stats.items():
            if data:
                print(f"{endpoint}:")
                print(f"  Count: {data['count']}, Avg: {data['mean']:.2f}ms, P95: {data['p95']:.2f}ms, Errors: {data['error_count']}")
                
        print("="*60)


class LatencyDecorator:
    """Decorator to automatically monitor latency for functions"""
    
    def __init__(self, monitor: LatencyMonitor, endpoint: str, method: str = "GET"):
        self.monitor = monitor
        self.endpoint = endpoint
        self.method = method
        
    def __call__(self, func):
        def wrapper(*args, **kwargs):
            start_time = time.time()
            self.monitor.record_request(self.endpoint, self.method, start_time)
            
            try:
                result = func(*args, **kwargs)
                end_time = time.time()
                
                # Extract status code and response size if available
                status_code = 200
                response_size = 0
                
                if hasattr(result, 'status_code'):
                    status_code = result.status_code
                if hasattr(result, 'content'):
                    response_size = len(result.content)
                    
                self.monitor.record_response(self.endpoint, self.method, end_time, status_code, response_size)
                return result
                
            except Exception as e:
                end_time = time.time()
                self.monitor.record_response(self.endpoint, self.method, end_time, 500, 0)
                raise
                
        return wrapper 