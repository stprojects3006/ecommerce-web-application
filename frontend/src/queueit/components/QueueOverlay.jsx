import React, { useState, useEffect } from 'react';
import { useQueueStatus, useQueueActions } from '../queueit-context';
import { QUEUE_EVENTS } from '../queueit-config';
import './QueueOverlay.css';

const QueueOverlay = () => {
  const { isQueuing, isQueued, isError, currentEvent, position, estimatedWaitTime } = useQueueStatus();
  const { bypassQueue } = useQueueActions();
  const [timeInQueue, setTimeInQueue] = useState(0);

  // Timer for tracking time in queue
  useEffect(() => {
    let interval;
    if (isQueued) {
      interval = setInterval(() => {
        setTimeInQueue(prev => prev + 1);
      }, 1000);
    } else {
      setTimeInQueue(0);
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [isQueued]);

  // Format time display
  const formatTime = (seconds) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  };

  // Format estimated wait time
  const formatWaitTime = (minutes) => {
    if (minutes < 60) {
      return `${minutes} minutes`;
    }
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    return `${hours}h ${remainingMinutes}m`;
  };

  // Get event display name
  const getEventDisplayName = (eventKey) => {
    const eventNames = {
      [QUEUE_EVENTS.FLASH_SALE]: 'Flash Sale',
      [QUEUE_EVENTS.BLACK_FRIDAY]: 'Black Friday Sale',
      [QUEUE_EVENTS.HIGH_TRAFFIC]: 'High Traffic Protection',
      [QUEUE_EVENTS.CHECKOUT]: 'Checkout Queue',
    };
    return eventNames[eventKey] || 'Special Event';
  };

  // Don't render if not in queue
  if (!isQueuing && !isQueued && !isError) {
    return null;
  }

  return (
    <div className="queue-overlay">
      <div className="queue-container">
        <div className="queue-header">
          <div className="queue-logo">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
            <span>PURELY Queue</span>
          </div>
          {currentEvent && (
            <div className="queue-event">
              {getEventDisplayName(currentEvent)}
            </div>
          )}
        </div>

        <div className="queue-content">
          {isQueuing && (
            <div className="queue-status queuing">
              <div className="spinner"></div>
              <h2>Joining Queue...</h2>
              <p>Please wait while we add you to the queue</p>
            </div>
          )}

          {isQueued && (
            <div className="queue-status queued">
              <div className="queue-position">
                <h2>You're in the queue!</h2>
                <div className="position-info">
                  <div className="position-number">
                    <span className="label">Position:</span>
                    <span className="value">{position || 'Calculating...'}</span>
                  </div>
                  {estimatedWaitTime && (
                    <div className="wait-time">
                      <span className="label">Estimated wait:</span>
                      <span className="value">{formatWaitTime(estimatedWaitTime)}</span>
                    </div>
                  )}
                  <div className="time-in-queue">
                    <span className="label">Time in queue:</span>
                    <span className="value">{formatTime(timeInQueue)}</span>
                  </div>
                </div>
              </div>

              <div className="queue-progress">
                <div className="progress-bar">
                  <div 
                    className="progress-fill" 
                    style={{ 
                      width: position ? `${Math.max(0, 100 - (position / 100) * 100)}%` : '0%' 
                    }}
                  ></div>
                </div>
                <p className="progress-text">
                  {position ? `You are ${position} in line` : 'Calculating your position...'}
                </p>
              </div>

              <div className="queue-message">
                <p>Don't close this window or you'll lose your place in line.</p>
                <p>We'll automatically redirect you when it's your turn.</p>
              </div>
            </div>
          )}

          {isError && (
            <div className="queue-status error">
              <div className="error-icon">⚠️</div>
              <h2>Queue Error</h2>
              <p>There was an issue connecting to the queue. Please try refreshing the page.</p>
              <button 
                className="retry-button"
                onClick={() => window.location.reload()}
              >
                Refresh Page
              </button>
            </div>
          )}
        </div>

        <div className="queue-footer">
          <div className="queue-info">
            <p>This queue helps us provide the best experience during high traffic periods.</p>
          </div>
          
          {process.env.NODE_ENV === 'development' && (
            <button 
              className="bypass-button"
              onClick={bypassQueue}
            >
              Bypass Queue (Dev)
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default QueueOverlay; 