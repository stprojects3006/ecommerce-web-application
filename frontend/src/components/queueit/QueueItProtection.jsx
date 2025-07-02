import React, { useEffect, useState } from 'react';
import useQueueIt from '../../hooks/useQueueIt';

/**
 * QueueItProtection Component
 * Wraps protected content and handles Queue-it validation
 * 
 * @param {Object} props
 * @param {string} props.eventId - Queue-it event ID
 * @param {React.ReactNode} props.children - Content to protect
 * @param {string} props.fallbackUrl - URL to redirect to if queue is needed
 * @param {boolean} props.autoValidate - Whether to auto-validate on mount
 * @param {Function} props.onAccessGranted - Callback when access is granted
 * @param {Function} props.onAccessDenied - Callback when access is denied
 */
const QueueItProtection = ({
  eventId,
  children,
  fallbackUrl = '/queue',
  autoValidate = true,
  onAccessGranted,
  onAccessDenied,
  ...props
}) => {
  const [isProtected, setIsProtected] = useState(true);
  const [validationAttempted, setValidationAttempted] = useState(false);
  
  const {
    isLoading,
    error,
    validateQueueToken,
    getQueueToken,
    setupCookieExtension,
    clearError
  } = useQueueIt(eventId);

  // Auto-validate on mount if enabled
  useEffect(() => {
    if (autoValidate && eventId && !validationAttempted) {
      validateAccess();
    }
  }, [autoValidate, eventId, validationAttempted]);

  const validateAccess = async () => {
    if (!eventId) {
      console.error('Event ID is required for Queue-it protection');
      return;
    }

    setValidationAttempted(true);
    clearError();

    try {
      const queueitToken = getQueueToken();
      const currentUrl = window.location.href;

      if (!queueitToken) {
        // No token, redirect to queue
        console.log('No queue token found, redirecting to queue');
        if (onAccessDenied) {
          onAccessDenied('No queue token found');
        }
        window.location.href = fallbackUrl;
        return;
      }

      const result = await validateQueueToken(queueitToken, currentUrl);

      if (result && !result.redirect) {
        // Access granted
        console.log('Queue access granted, queueId:', result.queueId);
        setIsProtected(false);
        
        // Setup cookie extension for the session
        if (result.queueId) {
          setupCookieExtension(result.queueId);
        }

        if (onAccessGranted) {
          onAccessGranted(result);
        }
      } else {
        // Access denied, redirect will happen automatically
        console.log('Queue access denied, redirecting...');
        if (onAccessDenied) {
          onAccessDenied('Access denied');
        }
      }
    } catch (err) {
      console.error('Error validating queue access:', err);
      if (onAccessDenied) {
        onAccessDenied(err.message);
      }
    }
  };

  const handleRetry = () => {
    setValidationAttempted(false);
    validateAccess();
  };

  // Show loading state
  if (isLoading) {
    return (
      <div className="queueit-loading" {...props}>
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p>Validating queue access...</p>
        </div>
      </div>
    );
  }

  // Show error state
  if (error) {
    return (
      <div className="queueit-error" {...props}>
        <div className="error-message">
          <h3>Queue Access Error</h3>
          <p>{error}</p>
          <button onClick={handleRetry} className="retry-button">
            Retry
          </button>
        </div>
      </div>
    );
  }

  // Show protected content if access is granted
  if (!isProtected) {
    return <>{children}</>;
  }

  // Show validation in progress
  return (
    <div className="queueit-validating" {...props}>
      <div className="validating-message">
        <p>Checking queue access...</p>
      </div>
    </div>
  );
};

export default QueueItProtection; 