"""
Grafana Client for Queue-it Testing
Integration with Grafana for monitoring and dashboard management
"""

import requests
import json
import time
from typing import Dict, Any, List, Optional
from datetime import datetime, timedelta
from loguru import logger
from .test_helpers import config


class GrafanaClient:
    """Client for Grafana API integration"""
    
    def __init__(self, base_url: str, api_key: str):
        self.base_url = base_url.rstrip('/')
        self.api_key = api_key
        self.session = requests.Session()
        self.session.headers.update({
            'Authorization': f'Bearer {api_key}',
            'Content-Type': 'application/json'
        })
    
    def health_check(self) -> Dict[str, Any]:
        """Check Grafana health"""
        try:
            response = self.session.get(f"{self.base_url}/api/health")
            return {
                'status': 'healthy' if response.ok else 'unhealthy',
                'status_code': response.status_code,
                'data': response.json() if response.ok else None
            }
        except Exception as e:
            logger.error(f"Grafana health check failed: {e}")
            return {'status': 'error', 'error': str(e)}
    
    def get_dashboards(self) -> List[Dict[str, Any]]:
        """Get all dashboards"""
        try:
            response = self.session.get(f"{self.base_url}/api/search")
            if response.ok:
                return response.json()
            else:
                logger.error(f"Failed to get dashboards: {response.status_code}")
                return []
        except Exception as e:
            logger.error(f"Error getting dashboards: {e}")
            return []
    
    def get_dashboard(self, uid: str) -> Optional[Dict[str, Any]]:
        """Get specific dashboard by UID"""
        try:
            response = self.session.get(f"{self.base_url}/api/dashboards/uid/{uid}")
            if response.ok:
                return response.json()
            else:
                logger.error(f"Failed to get dashboard {uid}: {response.status_code}")
                return None
        except Exception as e:
            logger.error(f"Error getting dashboard {uid}: {e}")
            return None
    
    def create_dashboard(self, dashboard_data: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        """Create a new dashboard"""
        try:
            payload = {
                'dashboard': dashboard_data,
                'overwrite': True
            }
            response = self.session.post(f"{self.base_url}/api/dashboards/db", json=payload)
            if response.ok:
                return response.json()
            else:
                logger.error(f"Failed to create dashboard: {response.status_code}")
                return None
        except Exception as e:
            logger.error(f"Error creating dashboard: {e}")
            return None
    
    def update_dashboard(self, uid: str, dashboard_data: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        """Update existing dashboard"""
        try:
            # Get current dashboard
            current = self.get_dashboard(uid)
            if not current:
                return None
            
            # Update with new data
            dashboard_data['id'] = current['dashboard']['id']
            dashboard_data['version'] = current['dashboard']['version'] + 1
            
            payload = {
                'dashboard': dashboard_data,
                'overwrite': True
            }
            response = self.session.post(f"{self.base_url}/api/dashboards/db", json=payload)
            if response.ok:
                return response.json()
            else:
                logger.error(f"Failed to update dashboard: {response.status_code}")
                return None
        except Exception as e:
            logger.error(f"Error updating dashboard: {e}")
            return None
    
    def delete_dashboard(self, uid: str) -> bool:
        """Delete dashboard by UID"""
        try:
            response = self.session.delete(f"{self.base_url}/api/dashboards/uid/{uid}")
            return response.ok
        except Exception as e:
            logger.error(f"Error deleting dashboard {uid}: {e}")
            return False
    
    def get_metrics(self, query: str, start_time: str, end_time: str) -> List[Dict[str, Any]]:
        """Query metrics from Prometheus data source"""
        try:
            # First, get the Prometheus data source ID
            datasources = self.session.get(f"{self.base_url}/api/datasources")
            if not datasources.ok:
                logger.error("Failed to get data sources")
                return []
            
            prometheus_ds = None
            for ds in datasources.json():
                if ds.get('type') == 'prometheus':
                    prometheus_ds = ds
                    break
            
            if not prometheus_ds:
                logger.error("Prometheus data source not found")
                return []
            
            # Query metrics
            query_url = f"{self.base_url}/api/datasources/proxy/{prometheus_ds['id']}/api/v1/query_range"
            params = {
                'query': query,
                'start': start_time,
                'end': end_time,
                'step': '15s'
            }
            
            response = self.session.get(query_url, params=params)
            if response.ok:
                return response.json().get('data', {}).get('result', [])
            else:
                logger.error(f"Failed to query metrics: {response.status_code}")
                return []
        except Exception as e:
            logger.error(f"Error querying metrics: {e}")
            return []
    
    def create_annotation(self, text: str, tags: List[str] = None, time_start: int = None, time_end: int = None) -> Optional[Dict[str, Any]]:
        """Create annotation for test events"""
        try:
            payload = {
                'text': text,
                'tags': tags or ['queueit-test'],
                'time': time_start or int(time.time() * 1000),
                'timeEnd': time_end or int(time.time() * 1000)
            }
            
            response = self.session.post(f"{self.base_url}/api/annotations", json=payload)
            if response.ok:
                return response.json()
            else:
                logger.error(f"Failed to create annotation: {response.status_code}")
                return None
        except Exception as e:
            logger.error(f"Error creating annotation: {e}")
            return None


class QueueItDashboardManager:
    """Manager for Queue-it specific dashboards"""
    
    def __init__(self, grafana_client: GrafanaClient):
        self.client = grafana_client
    
    def create_api_performance_dashboard(self) -> Optional[Dict[str, Any]]:
        """Create Queue-it API performance dashboard"""
        dashboard = {
            'title': 'Queue-it API Performance',
            'uid': 'queueit-api-performance',
            'tags': ['queueit', 'api', 'performance'],
            'timezone': 'browser',
            'panels': [
                {
                    'title': 'API Response Time',
                    'type': 'graph',
                    'targets': [
                        {
                            'expr': 'rate(queueit_api_response_time_seconds_sum[5m]) / rate(queueit_api_response_time_seconds_count[5m])',
                            'legendFormat': '{{method}} {{endpoint}}'
                        }
                    ],
                    'yAxes': [
                        {'label': 'Response Time (seconds)', 'unit': 's'},
                        {'show': False}
                    ]
                },
                {
                    'title': 'API Throughput',
                    'type': 'graph',
                    'targets': [
                        {
                            'expr': 'rate(queueit_api_requests_total[5m])',
                            'legendFormat': '{{method}} {{endpoint}}'
                        }
                    ],
                    'yAxes': [
                        {'label': 'Requests per Second', 'unit': 'reqps'},
                        {'show': False}
                    ]
                },
                {
                    'title': 'Error Rate',
                    'type': 'graph',
                    'targets': [
                        {
                            'expr': 'rate(queueit_api_errors_total[5m])',
                            'legendFormat': '{{method}} {{endpoint}}'
                        }
                    ],
                    'yAxes': [
                        {'label': 'Errors per Second', 'unit': 'reqps'},
                        {'show': False}
                    ]
                }
            ]
        }
        
        return self.client.create_dashboard(dashboard)
    
    def create_frontend_metrics_dashboard(self) -> Optional[Dict[str, Any]]:
        """Create Queue-it frontend metrics dashboard"""
        dashboard = {
            'title': 'Queue-it Frontend Metrics',
            'uid': 'queueit-frontend-metrics',
            'tags': ['queueit', 'frontend', 'metrics'],
            'timezone': 'browser',
            'panels': [
                {
                    'title': 'Queue Trigger Rate',
                    'type': 'graph',
                    'targets': [
                        {
                            'expr': 'rate(queueit_queue_triggers_total[5m])',
                            'legendFormat': '{{event_id}}'
                        }
                    ],
                    'yAxes': [
                        {'label': 'Triggers per Second', 'unit': 'reqps'},
                        {'show': False}
                    ]
                },
                {
                    'title': 'Queue Size',
                    'type': 'graph',
                    'targets': [
                        {
                            'expr': 'queueit_queue_size',
                            'legendFormat': '{{event_id}}'
                        }
                    ],
                    'yAxes': [
                        {'label': 'Users in Queue', 'unit': 'short'},
                        {'show': False}
                    ]
                },
                {
                    'title': 'Wait Time',
                    'type': 'graph',
                    'targets': [
                        {
                            'expr': 'queueit_wait_time_seconds',
                            'legendFormat': '{{event_id}}'
                        }
                    ],
                    'yAxes': [
                        {'label': 'Wait Time (seconds)', 'unit': 's'},
                        {'show': False}
                    ]
                }
            ]
        }
        
        return self.client.create_dashboard(dashboard)
    
    def create_error_tracking_dashboard(self) -> Optional[Dict[str, Any]]:
        """Create Queue-it error tracking dashboard"""
        dashboard = {
            'title': 'Queue-it Error Tracking',
            'uid': 'queueit-error-tracking',
            'tags': ['queueit', 'errors', 'monitoring'],
            'timezone': 'browser',
            'panels': [
                {
                    'title': 'Error Rate by Type',
                    'type': 'graph',
                    'targets': [
                        {
                            'expr': 'rate(queueit_errors_total[5m])',
                            'legendFormat': '{{error_type}}'
                        }
                    ],
                    'yAxes': [
                        {'label': 'Errors per Second', 'unit': 'reqps'},
                        {'show': False}
                    ]
                },
                {
                    'title': 'Error Distribution',
                    'type': 'piechart',
                    'targets': [
                        {
                            'expr': 'sum(queueit_errors_total) by (error_type)',
                            'legendFormat': '{{error_type}}'
                        }
                    ]
                },
                {
                    'title': 'Recent Errors',
                    'type': 'table',
                    'targets': [
                        {
                            'expr': 'queueit_errors_total',
                            'format': 'table',
                            'instant': True
                        }
                    ]
                }
            ]
        }
        
        return self.client.create_dashboard(dashboard)
    
    def create_load_testing_dashboard(self) -> Optional[Dict[str, Any]]:
        """Create Queue-it load testing dashboard"""
        dashboard = {
            'title': 'Queue-it Load Testing',
            'uid': 'queueit-load-testing',
            'tags': ['queueit', 'load-testing', 'performance'],
            'timezone': 'browser',
            'panels': [
                {
                    'title': 'Concurrent Users',
                    'type': 'graph',
                    'targets': [
                        {
                            'expr': 'queueit_concurrent_users',
                            'legendFormat': 'Active Users'
                        }
                    ],
                    'yAxes': [
                        {'label': 'Users', 'unit': 'short'},
                        {'show': False}
                    ]
                },
                {
                    'title': 'Test Response Times',
                    'type': 'graph',
                    'targets': [
                        {
                            'expr': 'histogram_quantile(0.95, rate(queueit_test_duration_seconds_bucket[5m]))',
                            'legendFormat': '95th Percentile'
                        },
                        {
                            'expr': 'histogram_quantile(0.50, rate(queueit_test_duration_seconds_bucket[5m]))',
                            'legendFormat': 'Median'
                        }
                    ],
                    'yAxes': [
                        {'label': 'Response Time (seconds)', 'unit': 's'},
                        {'show': False}
                    ]
                },
                {
                    'title': 'Test Success Rate',
                    'type': 'singlestat',
                    'targets': [
                        {
                            'expr': 'rate(queueit_test_success_total[5m]) / rate(queueit_test_total[5m]) * 100',
                            'legendFormat': 'Success Rate'
                        }
                    ],
                    'fieldConfig': {
                        'defaults': {
                            'unit': 'percent',
                            'thresholds': {
                                'steps': [
                                    {'color': 'red', 'value': 90},
                                    {'color': 'green', 'value': 95}
                                ]
                            }
                        }
                    }
                }
            ]
        }
        
        return self.client.create_dashboard(dashboard)


class MetricsCollector:
    """Collect and send metrics to Grafana"""
    
    def __init__(self, grafana_client: GrafanaClient):
        self.client = grafana_client
        self.test_start_time = None
        self.test_end_time = None
    
    def start_test_session(self, test_name: str):
        """Start a test session and create annotation"""
        self.test_start_time = int(time.time() * 1000)
        self.client.create_annotation(
            text=f"Started: {test_name}",
            tags=['queueit-test', 'start', test_name],
            time_start=self.test_start_time
        )
        logger.info(f"Started test session: {test_name}")
    
    def end_test_session(self, test_name: str, success: bool = True):
        """End a test session and create annotation"""
        self.test_end_time = int(time.time() * 1000)
        status = "PASSED" if success else "FAILED"
        self.client.create_annotation(
            text=f"Ended: {test_name} - {status}",
            tags=['queueit-test', 'end', test_name, status.lower()],
            time_start=self.test_end_time
        )
        logger.info(f"Ended test session: {test_name} - {status}")
    
    def record_test_metric(self, metric_name: str, value: float, tags: Dict[str, str] = None):
        """Record a test metric (this would typically send to Prometheus)"""
        # In a real implementation, this would send metrics to Prometheus
        # For now, we'll just log them
        tag_str = ", ".join([f"{k}={v}" for k, v in (tags or {}).items()])
        logger.info(f"Metric: {metric_name}={value} {tag_str}")
    
    def get_test_metrics(self, test_name: str, start_time: str = None, end_time: str = None) -> List[Dict[str, Any]]:
        """Get metrics for a specific test"""
        if not start_time:
            start_time = datetime.now() - timedelta(hours=1)
            start_time = start_time.strftime('%Y-%m-%dT%H:%M:%SZ')
        
        if not end_time:
            end_time = datetime.now().strftime('%Y-%m-%dT%H:%M:%SZ')
        
        # Query relevant metrics
        metrics = []
        
        # API response time
        response_time_data = self.client.get_metrics(
            'queueit_api_response_time_seconds',
            start_time, end_time
        )
        metrics.extend(response_time_data)
        
        # Queue size
        queue_size_data = self.client.get_metrics(
            'queueit_queue_size',
            start_time, end_time
        )
        metrics.extend(queue_size_data)
        
        # Error rate
        error_rate_data = self.client.get_metrics(
            'rate(queueit_api_errors_total[5m])',
            start_time, end_time
        )
        metrics.extend(error_rate_data)
        
        return metrics


# Global instances
grafana_client = GrafanaClient(
    config.get('urls.grafana_url'),
    config.get('credentials.grafana_api_key')
)
dashboard_manager = QueueItDashboardManager(grafana_client)
metrics_collector = MetricsCollector(grafana_client) 