import { useState, useEffect, useCallback } from 'react';
import QueueItService from '../api-service/queueit.service';

/**
 * React Hook for Queue-it Integration
 * Provides easy-to-use functions for Queue-it operations in React components
 */
const useQueueIt = (eventId) => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [queueStatus, setQueueStatus] = useState(null);
  const [cookieExtensionInterval, setCookieExtensionInterval] = useState(null);

  // Cleanup cookie extension on unmount
  useEffect(() => {
    return () => {
      if (cookieExtensionInterval) {
        QueueItService.clearCookieExtension(cookieExtensionInterval);
      }
    };
  }, [cookieExtensionInterval]);

  /**
   * Validate queue token and handle redirects
   */
  const validateQueueToken = useCallback(async (queueitToken, originalUrl) => {
    if (!eventId) {
      setError('Event ID is required');
      return null;
    }

    setIsLoading(true);
    setError(null);

    try {
      const result = await QueueItService.validateQueueToken(eventId, queueitToken, originalUrl);
      return result;
    } catch (err) {
      setError(err.message);
      QueueItService.handleError(err, 'Queue token validation');
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [eventId]);

  /**
   * Cancel queue session
   */
  const cancelQueueSession = useCallback(async (queueitToken) => {
    if (!eventId) {
      setError('Event ID is required');
      return null;
    }

    setIsLoading(true);
    setError(null);

    try {
      const result = await QueueItService.cancelQueueSession(eventId, queueitToken);
      return result;
    } catch (err) {
      setError(err.message);
      QueueItService.handleError(err, 'Queue session cancellation');
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [eventId]);

  /**
   * Extend queue cookie
   */
  const extendQueueCookie = useCallback(async (queueId, options = {}) => {
    if (!eventId) {
      setError('Event ID is required');
      return null;
    }

    setIsLoading(true);
    setError(null);

    try {
      const result = await QueueItService.extendQueueCookie(eventId, queueId, options);
      return result;
    } catch (err) {
      setError(err.message);
      QueueItService.handleError(err, 'Cookie extension');
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [eventId]);

  /**
   * Get queue status
   */
  const getQueueStatus = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const status = await QueueItService.getQueueStatus();
      setQueueStatus(status);
      return status;
    } catch (err) {
      setError(err.message);
      QueueItService.handleError(err, 'Queue status check');
      return null;
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Check health status
   */
  const checkHealth = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const health = await QueueItService.checkHealth();
      return health;
    } catch (err) {
      setError(err.message);
      QueueItService.handleError(err, 'Health check');
      return null;
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Check if user can access protected resource
   */
  const checkQueueAccess = useCallback(async (protectedUrl) => {
    if (!eventId) {
      setError('Event ID is required');
      return false;
    }

    setIsLoading(true);
    setError(null);

    try {
      const canAccess = await QueueItService.checkQueueAccess(eventId, protectedUrl);
      return canAccess;
    } catch (err) {
      setError(err.message);
      QueueItService.handleError(err, 'Queue access check');
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [eventId]);

  /**
   * Get queue token from current context
   */
  const getQueueToken = useCallback(() => {
    return QueueItService.getQueueToken();
  }, []);

  /**
   * Setup automatic cookie extension
   */
  const setupCookieExtension = useCallback((queueId, intervalMinutes = 25) => {
    if (!eventId) {
      setError('Event ID is required');
      return null;
    }

    // Clear existing interval if any
    if (cookieExtensionInterval) {
      QueueItService.clearCookieExtension(cookieExtensionInterval);
    }

    const intervalId = QueueItService.setupCookieExtension(eventId, queueId, intervalMinutes);
    setCookieExtensionInterval(intervalId);
    return intervalId;
  }, [eventId, cookieExtensionInterval]);

  /**
   * Clear cookie extension
   */
  const clearCookieExtension = useCallback(() => {
    if (cookieExtensionInterval) {
      QueueItService.clearCookieExtension(cookieExtensionInterval);
      setCookieExtensionInterval(null);
    }
  }, [cookieExtensionInterval]);

  /**
   * Clear error state
   */
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return {
    // State
    isLoading,
    error,
    queueStatus,
    
    // Actions
    validateQueueToken,
    cancelQueueSession,
    extendQueueCookie,
    getQueueStatus,
    checkHealth,
    checkQueueAccess,
    getQueueToken,
    setupCookieExtension,
    clearCookieExtension,
    clearError,
  };
};

export default useQueueIt; 