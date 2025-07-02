/**
 * Queue-it Service for Frontend Integration
 * Provides methods to interact with Queue-it backend APIs
 */

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

class QueueItService {
  /**
   * Validate a user's queue token before accessing protected resources
   * @param {string} eventId - The Queue-it event ID
   * @param {string} queueitToken - The queue token to validate
   * @param {string} originalUrl - The original URL the user was trying to access
   * @returns {Promise<Object>} Validation result
   */
  static async validateQueueToken(eventId, queueitToken, originalUrl) {
    try {
      const response = await fetch(`${API_BASE_URL}/api/queueit/validate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          eventId,
          queueitToken,
          originalUrl
        })
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result = await response.json();
      
      // Handle redirect if needed
      if (result.redirect && result.redirectUrl) {
        console.log('Redirecting to queue:', result.redirectUrl);
        window.location.href = result.redirectUrl;
        return null; // Don't return result as we're redirecting
      }

      return result;
    } catch (error) {
      console.error('Error validating queue token:', error);
      throw error;
    }
  }

  /**
   * Cancel a user's queue session
   * @param {string} eventId - The Queue-it event ID
   * @param {string} queueitToken - The queue token to cancel
   * @returns {Promise<Object>} Cancellation result
   */
  static async cancelQueueSession(eventId, queueitToken) {
    try {
      const response = await fetch(`${API_BASE_URL}/api/queueit/cancel`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          eventId,
          queueitToken
        })
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result = await response.json();
      
      // Handle redirect if needed
      if (result.redirect && result.redirectUrl) {
        console.log('Redirecting after cancellation:', result.redirectUrl);
        window.location.href = result.redirectUrl;
        return null;
      }

      return result;
    } catch (error) {
      console.error('Error canceling queue session:', error);
      throw error;
    }
  }

  /**
   * Extend the validity of queue cookies
   * @param {string} eventId - The Queue-it event ID
   * @param {string} queueId - The queue ID
   * @param {Object} options - Cookie extension options
   * @returns {Promise<Object>} Extension result
   */
  static async extendQueueCookie(eventId, queueId, options = {}) {
    try {
      const {
        cookieValidityMinutes = 30,
        cookieDomain = null,
        isCookieHttpOnly = true,
        isCookieSecure = true
      } = options;

      const response = await fetch(`${API_BASE_URL}/api/queueit/extend-cookie`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          eventId,
          queueId,
          cookieValidityMinutes,
          cookieDomain,
          isCookieHttpOnly,
          isCookieSecure
        })
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error extending queue cookie:', error);
      throw error;
    }
  }

  /**
   * Get queue/event status information
   * @returns {Promise<Object>} Status information
   */
  static async getQueueStatus() {
    try {
      const response = await fetch(`${API_BASE_URL}/api/queueit/status`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error getting queue status:', error);
      throw error;
    }
  }

  /**
   * Health check for Queue-it integration
   * @returns {Promise<Object>} Health information
   */
  static async checkHealth() {
    try {
      const response = await fetch(`${API_BASE_URL}/api/queueit/health`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error checking Queue-it health:', error);
      throw error;
    }
  }

  /**
   * Check if user should be queued before accessing a protected resource
   * This is a convenience method that combines validation and queue logic
   * @param {string} eventId - The Queue-it event ID
   * @param {string} protectedUrl - The URL being accessed
   * @returns {Promise<boolean>} True if user can proceed, false if queued
   */
  static async checkQueueAccess(eventId, protectedUrl) {
    try {
      // Get queue token from cookies or URL parameters
      const queueitToken = this.getQueueToken();
      
      if (!queueitToken) {
        // No token, user needs to be queued
        console.log('No queue token found, user needs to be queued');
        return false;
      }

      const result = await this.validateQueueToken(eventId, queueitToken, protectedUrl);
      
      if (result && !result.redirect) {
        // User can proceed
        console.log('User can proceed, queueId:', result.queueId);
        return true;
      }

      // User needs to be queued (redirect will happen automatically)
      return false;
    } catch (error) {
      console.error('Error checking queue access:', error);
      // On error, assume user needs to be queued
      return false;
    }
  }

  /**
   * Get queue token from cookies or URL parameters
   * @returns {string|null} Queue token or null if not found
   */
  static getQueueToken() {
    // Try to get from URL parameters first
    const urlParams = new URLSearchParams(window.location.search);
    const tokenFromUrl = urlParams.get('queueit');
    
    if (tokenFromUrl) {
      return tokenFromUrl;
    }

    // Try to get from cookies
    const cookies = document.cookie.split(';');
    for (const cookie of cookies) {
      const [name, value] = cookie.trim().split('=');
      if (name === 'queueit') {
        return value;
      }
    }

    return null;
  }

  /**
   * Set up periodic cookie extension for active queue sessions
   * @param {string} eventId - The Queue-it event ID
   * @param {string} queueId - The queue ID
   * @param {number} intervalMinutes - Extension interval in minutes (default: 25)
   */
  static setupCookieExtension(eventId, queueId, intervalMinutes = 25) {
    const intervalMs = intervalMinutes * 60 * 1000;
    
    const extensionInterval = setInterval(async () => {
      try {
        await this.extendQueueCookie(eventId, queueId);
        console.log('Queue cookie extended successfully');
      } catch (error) {
        console.error('Failed to extend queue cookie:', error);
        // Stop trying to extend if it fails
        clearInterval(extensionInterval);
      }
    }, intervalMs);

    // Return the interval ID so it can be cleared later
    return extensionInterval;
  }

  /**
   * Clear cookie extension interval
   * @param {number} intervalId - The interval ID returned by setupCookieExtension
   */
  static clearCookieExtension(intervalId) {
    if (intervalId) {
      clearInterval(intervalId);
    }
  }

  /**
   * Handle queue-related errors
   * @param {Error} error - The error to handle
   * @param {string} context - Context where the error occurred
   */
  static handleError(error, context = 'Queue-it operation') {
    console.error(`${context} failed:`, error);
    
    // You can add custom error handling here
    // For example, show user-friendly error messages
    if (error.message.includes('401')) {
      console.log('Authentication error - user may need to re-enter queue');
    } else if (error.message.includes('404')) {
      console.log('Queue event not found');
    } else if (error.message.includes('500')) {
      console.log('Server error - please try again later');
    }
  }

  /**
   * Simulate placing a user in the queue (integration test only)
   * @param {Object} data - eventId, queueitToken, etc.
   * @returns {Promise<Object>} Response
   */
  static async queueUser(data) {
    try {
      const response = await fetch(`${API_BASE_URL}/api/queueit/queue`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('Error queueing user:', error);
      throw error;
    }
  }

  /**
   * Simulate a queue event (integration test only, backend must implement)
   */
  static async simulateEvent(eventId) {
    // Placeholder for future backend endpoint
    return Promise.reject('Not implemented');
  }

  /**
   * Inspect queue/session state (integration test only, backend must implement)
   */
  static async getSessionInfo(queueId) {
    // Placeholder for future backend endpoint
    return Promise.reject('Not implemented');
  }

  /**
   * Reset test state (integration test only, backend must implement)
   */
  static async resetTestState() {
    // Placeholder for future backend endpoint
    return Promise.reject('Not implemented');
  }
}

export default QueueItService; 