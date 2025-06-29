import React, { createContext, useContext, useEffect, useState } from 'react';
import queueItService from './queueit-service';
import { QUEUE_STATUS } from './queueit-config';

// Create Queue-it Context
const QueueItContext = createContext();

// Queue-it Provider Component
export const QueueItProvider = ({ children }) => {
  const [queueState, setQueueState] = useState({
    status: QUEUE_STATUS.IDLE,
    currentEvent: null,
    queueToken: null,
    error: null,
    position: null,
    estimatedWaitTime: null,
  });

  const [isInitialized, setIsInitialized] = useState(false);

  // Initialize Queue-it service
  useEffect(() => {
    const initializeQueueIt = async () => {
      try {
        await queueItService.initialize();
        setIsInitialized(true);
      } catch (error) {
        console.error('Failed to initialize Queue-it:', error);
        setIsInitialized(true); // Mark as initialized even if failed
      }
    };

    initializeQueueIt();

    // Cleanup on unmount
    return () => {
      queueItService.cleanup();
    };
  }, []);

  // Listen to Queue-it events
  useEffect(() => {
    const unsubscribe = queueItService.addListener((eventData) => {
      setQueueState(prevState => ({
        ...prevState,
        status: eventData.status,
        currentEvent: eventData.currentEvent,
        queueToken: eventData.queueToken,
        error: eventData.error,
        position: eventData.position || prevState.position,
        estimatedWaitTime: eventData.estimatedWaitTime || prevState.estimatedWaitTime,
      }));
    });

    return unsubscribe;
  }, []);

  // Queue-it actions
  const queueActions = {
    // Trigger queue manually
    triggerQueue: async (eventKey) => {
      await queueItService.triggerQueueManually(eventKey);
    },

    // Bypass queue (development only)
    bypassQueue: () => {
      return queueItService.bypassQueue();
    },

    // Get current status
    getStatus: () => {
      return queueItService.getStatus();
    },

    // Clear queue token
    clearQueueToken: () => {
      queueItService.clearQueueToken();
    },

    // Refresh queue status
    refreshStatus: () => {
      const status = queueItService.getStatus();
      setQueueState(prevState => ({
        ...prevState,
        ...status,
      }));
    },
  };

  const contextValue = {
    ...queueState,
    isInitialized,
    actions: queueActions,
  };

  return (
    <QueueItContext.Provider value={contextValue}>
      {children}
    </QueueItContext.Provider>
  );
};

// Custom hook to use Queue-it context
export const useQueueIt = () => {
  const context = useContext(QueueItContext);
  if (!context) {
    throw new Error('useQueueIt must be used within a QueueItProvider');
  }
  return context;
};

// Hook for checking if user is in queue
export const useQueueStatus = () => {
  const { status, currentEvent, position, estimatedWaitTime } = useQueueIt();
  
  return {
    isQueuing: status === QUEUE_STATUS.QUEUING,
    isQueued: status === QUEUE_STATUS.QUEUED,
    isEntered: status === QUEUE_STATUS.ENTERED,
    isError: status === QUEUE_STATUS.ERROR,
    isIdle: status === QUEUE_STATUS.IDLE,
    currentEvent,
    position,
    estimatedWaitTime,
  };
};

// Hook for queue actions
export const useQueueActions = () => {
  const { actions } = useQueueIt();
  return actions;
};

export default QueueItContext; 