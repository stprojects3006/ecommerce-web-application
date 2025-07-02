import React, { useState } from 'react';
import useQueueIt from '../../hooks/useQueueIt';
import QueueItProtection from './QueueItProtection';

/**
 * Example component demonstrating Queue-it integration
 * This shows how to use Queue-it in a real application
 */
const QueueItExample = () => {
  const [eventId] = useState('flash-sale-2024'); // Your Queue-it event ID
  const [showProtectedContent, setShowProtectedContent] = useState(false);
  
  const {
    isLoading,
    error,
    validateQueueToken,
    cancelQueueSession,
    extendQueueCookie,
    getQueueStatus,
    checkHealth,
    getQueueToken,
    clearError
  } = useQueueIt(eventId);

  const handleAccessGranted = (result) => {
    console.log('Access granted:', result);
    setShowProtectedContent(true);
  };

  const handleAccessDenied = (reason) => {
    console.log('Access denied:', reason);
    setShowProtectedContent(false);
  };

  const handleManualValidation = async () => {
    const token = getQueueToken();
    if (token) {
      const result = await validateQueueToken(token, window.location.href);
      console.log('Manual validation result:', result);
    } else {
      console.log('No queue token found');
    }
  };

  const handleCancelQueue = async () => {
    const token = getQueueToken();
    if (token) {
      const result = await cancelQueueSession(token);
      console.log('Queue cancellation result:', result);
    } else {
      console.log('No queue token found');
    }
  };

  const handleExtendCookie = async () => {
    const token = getQueueToken();
    if (token) {
      const result = await extendQueueCookie('queue-id-123', {
        cookieValidityMinutes: 30,
        isCookieHttpOnly: true,
        isCookieSecure: true
      });
      console.log('Cookie extension result:', result);
    } else {
      console.log('No queue token found');
    }
  };

  const handleCheckStatus = async () => {
    const status = await getQueueStatus();
    console.log('Queue status:', status);
  };

  const handleCheckHealth = async () => {
    const health = await checkHealth();
    console.log('Queue-it health:', health);
  };

  return (
    <div className="queueit-example">
      <h2>Queue-it Integration Example</h2>
      
      {/* Queue-it Protection Wrapper */}
      <QueueItProtection
        eventId={eventId}
        fallbackUrl="/queue"
        onAccessGranted={handleAccessGranted}
        onAccessDenied={handleAccessDenied}
      >
        {/* Protected Content */}
        <div className="protected-content">
          <h3>🎉 Welcome to the Flash Sale!</h3>
          <p>This content is protected by Queue-it. Only users who have passed through the queue can see this.</p>
          
          <div className="flash-sale-items">
            <div className="sale-item">
              <h4>Premium Product 1</h4>
              <p>Original: $100 | Sale: $50</p>
              <button className="buy-button">Buy Now</button>
            </div>
            <div className="sale-item">
              <h4>Premium Product 2</h4>
              <p>Original: $200 | Sale: $100</p>
              <button className="buy-button">Buy Now</button>
            </div>
          </div>
        </div>
      </QueueItProtection>

      {/* Queue-it Controls */}
      <div className="queueit-controls">
        <h3>Queue-it Controls</h3>
        
        <div className="control-buttons">
          <button 
            onClick={handleManualValidation}
            disabled={isLoading}
            className="control-button"
          >
            {isLoading ? 'Validating...' : 'Validate Token'}
          </button>
          
          <button 
            onClick={handleCancelQueue}
            disabled={isLoading}
            className="control-button"
          >
            Cancel Queue
          </button>
          
          <button 
            onClick={handleExtendCookie}
            disabled={isLoading}
            className="control-button"
          >
            Extend Cookie
          </button>
          
          <button 
            onClick={handleCheckStatus}
            disabled={isLoading}
            className="control-button"
          >
            Check Status
          </button>
          
          <button 
            onClick={handleCheckHealth}
            disabled={isLoading}
            className="control-button"
          >
            Check Health
          </button>
          
          <button 
            onClick={clearError}
            className="control-button"
          >
            Clear Error
          </button>
        </div>

        {/* Status Display */}
        <div className="status-display">
          <h4>Current Status</h4>
          <p><strong>Event ID:</strong> {eventId}</p>
          <p><strong>Queue Token:</strong> {getQueueToken() || 'None'}</p>
          <p><strong>Loading:</strong> {isLoading ? 'Yes' : 'No'}</p>
          {error && (
            <p><strong>Error:</strong> <span className="error-text">{error}</span></p>
          )}
        </div>
      </div>

      {/* CSS Styles */}
      <style jsx>{`
        .queueit-example {
          max-width: 800px;
          margin: 0 auto;
          padding: 20px;
          font-family: Arial, sans-serif;
        }

        .protected-content {
          background: #f0f8ff;
          border: 2px solid #4CAF50;
          border-radius: 8px;
          padding: 20px;
          margin: 20px 0;
        }

        .flash-sale-items {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
          gap: 20px;
          margin-top: 20px;
        }

        .sale-item {
          background: white;
          border: 1px solid #ddd;
          border-radius: 8px;
          padding: 15px;
          text-align: center;
        }

        .buy-button {
          background: #4CAF50;
          color: white;
          border: none;
          padding: 10px 20px;
          border-radius: 4px;
          cursor: pointer;
          font-size: 16px;
        }

        .buy-button:hover {
          background: #45a049;
        }

        .queueit-controls {
          background: #f5f5f5;
          border: 1px solid #ddd;
          border-radius: 8px;
          padding: 20px;
          margin-top: 20px;
        }

        .control-buttons {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
          margin-bottom: 20px;
        }

        .control-button {
          background: #2196F3;
          color: white;
          border: none;
          padding: 8px 16px;
          border-radius: 4px;
          cursor: pointer;
          font-size: 14px;
        }

        .control-button:hover:not(:disabled) {
          background: #1976D2;
        }

        .control-button:disabled {
          background: #ccc;
          cursor: not-allowed;
        }

        .status-display {
          background: white;
          border: 1px solid #ddd;
          border-radius: 4px;
          padding: 15px;
        }

        .error-text {
          color: #f44336;
          font-weight: bold;
        }

        .queueit-loading,
        .queueit-error,
        .queueit-validating {
          text-align: center;
          padding: 40px;
          background: #f9f9f9;
          border-radius: 8px;
          margin: 20px 0;
        }

        .loading-spinner {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 10px;
        }

        .spinner {
          width: 40px;
          height: 40px;
          border: 4px solid #f3f3f3;
          border-top: 4px solid #3498db;
          border-radius: 50%;
          animation: spin 1s linear infinite;
        }

        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }

        .error-message {
          color: #f44336;
        }

        .retry-button {
          background: #f44336;
          color: white;
          border: none;
          padding: 10px 20px;
          border-radius: 4px;
          cursor: pointer;
          margin-top: 10px;
        }

        .retry-button:hover {
          background: #d32f2f;
        }
      `}</style>
    </div>
  );
};

export default QueueItExample; 