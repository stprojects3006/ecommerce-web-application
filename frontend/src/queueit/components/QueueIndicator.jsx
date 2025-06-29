import React from 'react';
import { useQueueStatus } from '../queueit-context';
import './QueueIndicator.css';

const QueueIndicator = () => {
  const { isQueuing, isQueued, currentEvent, position } = useQueueStatus();

  // Don't render if not in queue
  if (!isQueuing && !isQueued) {
    return null;
  }

  return (
    <div className="queue-indicator">
      <div className="queue-indicator-content">
        {isQueuing && (
          <div className="queue-status-badge queuing">
            <div className="status-dot"></div>
            <span>Joining Queue...</span>
          </div>
        )}
        
        {isQueued && (
          <div className="queue-status-badge queued">
            <div className="status-dot"></div>
            <span>Queue Position: {position || 'Calculating...'}</span>
          </div>
        )}
      </div>
    </div>
  );
};

export default QueueIndicator; 