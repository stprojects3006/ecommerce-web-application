import { QUEUE_IT_CONFIG, QUEUE_STATUS, QUEUE_ERRORS } from './queueit-config';

class QueueItService {
  constructor() {
    this.status = QUEUE_STATUS.IDLE;
    this.currentEvent = null;
    this.queueToken = null;
    this.error = null;
    this.listeners = [];
  }

  // Initialize Queue-it service
  async initialize() {
    try {
      if (!QUEUE_IT_CONFIG.development.enabled) {
        console.log('Queue-it is disabled');
        return;
      }

      // Check if user is already in queue
      const existingToken = this.getQueueToken();
      if (existingToken) {
        this.queueToken = existingToken;
        this.status = QUEUE_STATUS.QUEUED;
        this.notifyListeners();
        return;
      }

      // Check current URL for queue triggers
      await this.checkQueueTriggers();
    } catch (error) {
      console.error('Queue-it initialization error:', error);
      this.handleError(QUEUE_ERRORS.CONFIGURATION_ERROR, error);
    }
  }

  // Check if current URL triggers a queue
  async checkQueueTriggers() {
    const currentPath = window.location.pathname;
    
    for (const [eventKey, eventConfig] of Object.entries(QUEUE_IT_CONFIG.events)) {
      const shouldTrigger = eventConfig.triggers.some(trigger => {
        if (trigger.validatorType === 'UrlValidator') {
          switch (trigger.operator) {
            case 'Contains':
              return currentPath.toLowerCase().includes(trigger.valueToCompare.toLowerCase());
            case 'Equals':
              return currentPath === trigger.valueToCompare;
            case 'StartsWith':
              return currentPath.startsWith(trigger.valueToCompare);
            default:
              return false;
          }
        }
        return false;
      });

      if (shouldTrigger) {
        await this.triggerQueue(eventKey, eventConfig);
        break;
      }
    }
  }

  // Trigger queue for specific event
  async triggerQueue(eventKey, eventConfig) {
    try {
      this.status = QUEUE_STATUS.QUEUING;
      this.currentEvent = eventKey;
      this.notifyListeners();

      // Check if queue is active
      const queueStatus = await this.checkQueueStatus(eventConfig.eventId);
      
      if (queueStatus.isActive) {
        await this.joinQueue(eventConfig);
      } else {
        // Queue is not active, allow access
        this.status = QUEUE_STATUS.ENTERED;
        this.notifyListeners();
      }
    } catch (error) {
      console.error('Queue trigger error:', error);
      this.handleError(QUEUE_ERRORS.NETWORK_ERROR, error);
    }
  }

  // Check if queue is active for an event
  async checkQueueStatus(eventId) {
    try {
      const response = await fetch(`/api/queueit/status/${eventId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Queue status check error:', error);
      // If we can't check status, assume queue is not active
      return { isActive: false };
    }
  }

  // Join the queue
  async joinQueue(eventConfig) {
    try {
      const response = await fetch('/api/queueit/enqueue', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          eventId: eventConfig.eventId,
          customerId: QUEUE_IT_CONFIG.customerId,
          targetUrl: window.location.href,
          userAgent: navigator.userAgent,
          ipAddress: await this.getClientIP(),
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result = await response.json();
      
      if (result.redirectUrl) {
        // Redirect to Queue-it
        window.location.href = result.redirectUrl;
      } else if (result.queueToken) {
        // Store queue token and wait
        this.queueToken = result.queueToken;
        this.status = QUEUE_STATUS.QUEUED;
        this.storeQueueToken(result.queueToken);
        this.notifyListeners();
        
        // Start polling for queue position
        this.startQueuePolling(eventConfig.eventId);
      }
    } catch (error) {
      console.error('Join queue error:', error);
      this.handleError(QUEUE_ERRORS.NETWORK_ERROR, error);
    }
  }

  // Poll queue position
  startQueuePolling(eventId) {
    this.pollInterval = setInterval(async () => {
      try {
        const response = await fetch(`/api/queueit/position/${eventId}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.queueToken}`,
          },
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        
        if (result.redirectUrl) {
          // User can enter the site
          clearInterval(this.pollInterval);
          this.status = QUEUE_STATUS.ENTERED;
          this.clearQueueToken();
          this.notifyListeners();
          window.location.href = result.redirectUrl;
        } else {
          // Update queue position
          this.notifyListeners({
            type: 'position_update',
            position: result.position,
            estimatedWaitTime: result.estimatedWaitTime,
          });
        }
      } catch (error) {
        console.error('Queue polling error:', error);
        this.handleError(QUEUE_ERRORS.NETWORK_ERROR, error);
      }
    }, 5000); // Poll every 5 seconds
  }

  // Get client IP address
  async getClientIP() {
    try {
      const response = await fetch('https://api.ipify.org?format=json');
      const data = await response.json();
      return data.ip;
    } catch (error) {
      console.warn('Could not get client IP:', error);
      return 'unknown';
    }
  }

  // Store queue token in localStorage
  storeQueueToken(token) {
    try {
      localStorage.setItem('queueit_token', token);
      localStorage.setItem('queueit_timestamp', Date.now().toString());
    } catch (error) {
      console.warn('Could not store queue token:', error);
    }
  }

  // Get queue token from localStorage
  getQueueToken() {
    try {
      const token = localStorage.getItem('queueit_token');
      const timestamp = localStorage.getItem('queueit_timestamp');
      
      if (token && timestamp) {
        const age = Date.now() - parseInt(timestamp);
        const maxAge = QUEUE_IT_CONFIG.enqueueTokenValidityTime * 60 * 1000; // Convert to milliseconds
        
        if (age < maxAge) {
          return token;
        } else {
          // Token expired, clear it
          this.clearQueueToken();
        }
      }
    } catch (error) {
      console.warn('Could not get queue token:', error);
    }
    
    return null;
  }

  // Clear queue token
  clearQueueToken() {
    try {
      localStorage.removeItem('queueit_token');
      localStorage.removeItem('queueit_timestamp');
    } catch (error) {
      console.warn('Could not clear queue token:', error);
    }
  }

  // Handle errors
  handleError(errorType, error) {
    this.error = {
      type: errorType,
      message: error.message,
      timestamp: Date.now(),
    };
    this.status = QUEUE_STATUS.ERROR;
    this.notifyListeners();
  }

  // Add event listener
  addListener(callback) {
    this.listeners.push(callback);
    return () => {
      this.listeners = this.listeners.filter(listener => listener !== callback);
    };
  }

  // Notify all listeners
  notifyListeners(data = null) {
    const eventData = {
      status: this.status,
      currentEvent: this.currentEvent,
      queueToken: this.queueToken,
      error: this.error,
      ...data,
    };

    this.listeners.forEach(listener => {
      try {
        listener(eventData);
      } catch (error) {
        console.error('Queue-it listener error:', error);
      }
    });
  }

  // Get current status
  getStatus() {
    return {
      status: this.status,
      currentEvent: this.currentEvent,
      queueToken: this.queueToken,
      error: this.error,
    };
  }

  // Manually trigger queue for testing
  async triggerQueueManually(eventKey) {
    const eventConfig = QUEUE_IT_CONFIG.events[eventKey];
    if (eventConfig) {
      await this.triggerQueue(eventKey, eventConfig);
    } else {
      console.error(`Event ${eventKey} not found in configuration`);
    }
  }

  // Bypass queue (for development)
  bypassQueue() {
    if (QUEUE_IT_CONFIG.development.bypassQueue) {
      this.status = QUEUE_STATUS.ENTERED;
      this.notifyListeners();
      return true;
    }
    return false;
  }

  // Cleanup
  cleanup() {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
    }
    this.listeners = [];
  }
}

// Create singleton instance
const queueItService = new QueueItService();

export default queueItService; 