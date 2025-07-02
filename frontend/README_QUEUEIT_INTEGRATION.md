# Queue-it Frontend Integration Guide

This guide explains how to integrate Queue-it functionality into your React frontend application.

## Overview

The Queue-it frontend integration provides:
- **QueueItService**: Service class for API calls
- **useQueueIt Hook**: React hook for Queue-it operations
- **QueueItProtection Component**: Wrapper for protecting content
- **QueueItExample Component**: Example implementation

## Installation

1. Ensure the backend Queue-it API is running and accessible
2. Set the `REACT_APP_API_BASE_URL` environment variable to point to your backend
3. Import the Queue-it components and hooks as needed

## Quick Start

### 1. Basic Usage with QueueItProtection

```jsx
import React from 'react';
import QueueItProtection from './components/queueit/QueueItProtection';

const FlashSalePage = () => {
  return (
    <QueueItProtection
      eventId="flash-sale-2024"
      fallbackUrl="/queue"
      onAccessGranted={(result) => console.log('Access granted:', result)}
      onAccessDenied={(reason) => console.log('Access denied:', reason)}
    >
      <div>
        <h1>🎉 Flash Sale!</h1>
        <p>This content is only visible to users who passed through the queue.</p>
        {/* Your protected content here */}
      </div>
    </QueueItProtection>
  );
};
```

### 2. Using the useQueueIt Hook

```jsx
import React from 'react';
import useQueueIt from './hooks/useQueueIt';

const QueueControlPanel = () => {
  const {
    isLoading,
    error,
    validateQueueToken,
    cancelQueueSession,
    getQueueToken,
    clearError
  } = useQueueIt('flash-sale-2024');

  const handleValidate = async () => {
    const token = getQueueToken();
    if (token) {
      const result = await validateQueueToken(token, window.location.href);
      console.log('Validation result:', result);
    }
  };

  return (
    <div>
      <button onClick={handleValidate} disabled={isLoading}>
        {isLoading ? 'Validating...' : 'Validate Token'}
      </button>
      {error && <p>Error: {error}</p>}
    </div>
  );
};
```

### 3. Direct API Calls with QueueItService

```jsx
import React from 'react';
import QueueItService from './api-service/queueit.service';

const QueueActions = () => {
  const handleHealthCheck = async () => {
    try {
      const health = await QueueItService.checkHealth();
      console.log('Queue-it health:', health);
    } catch (error) {
      console.error('Health check failed:', error);
    }
  };

  return (
    <button onClick={handleHealthCheck}>
      Check Queue-it Health
    </button>
  );
};
```

## API Reference

### QueueItService

#### Methods

- `validateQueueToken(eventId, queueitToken, originalUrl)` - Validate queue token
- `cancelQueueSession(eventId, queueitToken)` - Cancel queue session
- `extendQueueCookie(eventId, queueId, options)` - Extend cookie validity
- `getQueueStatus()` - Get queue status
- `checkHealth()` - Check Queue-it health
- `checkQueueAccess(eventId, protectedUrl)` - Check if user can access resource
- `getQueueToken()` - Get queue token from cookies/URL
- `setupCookieExtension(eventId, queueId, intervalMinutes)` - Setup auto cookie extension
- `clearCookieExtension(intervalId)` - Clear cookie extension
- `handleError(error, context)` - Handle Queue-it errors

### useQueueIt Hook

#### Parameters
- `eventId` (string) - Queue-it event ID

#### Returns
- `isLoading` (boolean) - Loading state
- `error` (string|null) - Error message
- `queueStatus` (object|null) - Queue status
- `validateQueueToken(queueitToken, originalUrl)` - Validate token
- `cancelQueueSession(queueitToken)` - Cancel session
- `extendQueueCookie(queueId, options)` - Extend cookie
- `getQueueStatus()` - Get status
- `checkHealth()` - Check health
- `checkQueueAccess(protectedUrl)` - Check access
- `getQueueToken()` - Get token
- `setupCookieExtension(queueId, intervalMinutes)` - Setup auto extension
- `clearCookieExtension()` - Clear extension
- `clearError()` - Clear error

### QueueItProtection Component

#### Props

- `eventId` (string, required) - Queue-it event ID
- `children` (ReactNode, required) - Content to protect
- `fallbackUrl` (string, default: '/queue') - URL to redirect to if queue needed
- `autoValidate` (boolean, default: true) - Auto-validate on mount
- `onAccessGranted` (function) - Callback when access is granted
- `onAccessDenied` (function) - Callback when access is denied

## Configuration

### Environment Variables

```bash
# Backend API URL
REACT_APP_API_BASE_URL=http://localhost:8080
```

### Queue-it Event Configuration

Configure your Queue-it events in the Queue-it portal and use the event IDs in your frontend:

```jsx
// Example event IDs for different scenarios
const EVENTS = {
  FLASH_SALE: 'flash-sale-2024',
  BLACK_FRIDAY: 'black-friday-2024',
  PRODUCT_LAUNCH: 'product-launch-2024',
  MAINTENANCE: 'maintenance-queue'
};
```

## Usage Patterns

### 1. Protecting Entire Pages

```jsx
const ProtectedPage = () => (
  <QueueItProtection eventId="my-event">
    <div>
      <h1>Protected Content</h1>
      <p>Only queued users can see this.</p>
    </div>
  </QueueItProtection>
);
```

### 2. Protecting Specific Components

```jsx
const ProductPage = () => (
  <div>
    <h1>Product Page</h1>
    <p>Public content here...</p>
    
    <QueueItProtection eventId="premium-features">
      <div>
        <h2>Premium Features</h2>
        <p>Protected content here...</p>
      </div>
    </QueueItProtection>
  </div>
);
```

### 3. Manual Queue Management

```jsx
const QueueManager = () => {
  const queueIt = useQueueIt('my-event');

  const handleManualQueue = async () => {
    const canAccess = await queueIt.checkQueueAccess('/protected-page');
    if (!canAccess) {
      // Redirect to queue or show queue UI
      window.location.href = '/queue';
    }
  };

  return (
    <button onClick={handleManualQueue}>
      Enter Queue
    </button>
  );
};
```

### 4. Cookie Management

```jsx
const SessionManager = () => {
  const queueIt = useQueueIt('my-event');

  useEffect(() => {
    // Setup automatic cookie extension when user has access
    const token = queueIt.getQueueToken();
    if (token) {
      const intervalId = queueIt.setupCookieExtension('queue-id', 25);
      
      // Cleanup on unmount
      return () => queueIt.clearCookieExtension();
    }
  }, []);

  return <div>Session managed automatically</div>;
};
```

## Error Handling

### Built-in Error Handling

The Queue-it integration includes built-in error handling:

```jsx
const ErrorHandlingExample = () => {
  const { error, clearError } = useQueueIt('my-event');

  if (error) {
    return (
      <div className="error-container">
        <h3>Queue Error</h3>
        <p>{error}</p>
        <button onClick={clearError}>Try Again</button>
      </div>
    );
  }

  return <div>Normal content</div>;
};
```

### Custom Error Handling

```jsx
const CustomErrorHandling = () => {
  const handleError = (error, context) => {
    // Custom error handling logic
    if (error.message.includes('401')) {
      // Handle authentication errors
      showLoginModal();
    } else if (error.message.includes('500')) {
      // Handle server errors
      showRetryDialog();
    }
  };

  return (
    <QueueItProtection
      eventId="my-event"
      onAccessDenied={handleError}
    >
      <div>Protected content</div>
    </QueueItProtection>
  );
};
```

## Best Practices

### 1. Event ID Management

```jsx
// Create a constants file for event IDs
// constants/queueit.js
export const QUEUEIT_EVENTS = {
  FLASH_SALE: 'flash-sale-2024',
  BLACK_FRIDAY: 'black-friday-2024',
  PRODUCT_LAUNCH: 'product-launch-2024'
};

// Use in components
import { QUEUEIT_EVENTS } from '../constants/queueit';

const FlashSalePage = () => (
  <QueueItProtection eventId={QUEUEIT_EVENTS.FLASH_SALE}>
    {/* Content */}
  </QueueItProtection>
);
```

### 2. Loading States

```jsx
const LoadingExample = () => {
  const { isLoading } = useQueueIt('my-event');

  if (isLoading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
        <p>Validating queue access...</p>
      </div>
    );
  }

  return <div>Content</div>;
};
```

### 3. Token Management

```jsx
const TokenExample = () => {
  const { getQueueToken, validateQueueToken } = useQueueIt('my-event');

  const handleTokenValidation = async () => {
    const token = getQueueToken();
    
    if (!token) {
      console.log('No token found, user needs to enter queue');
      return;
    }

    try {
      const result = await validateQueueToken(token, window.location.href);
      console.log('Token validation result:', result);
    } catch (error) {
      console.error('Token validation failed:', error);
    }
  };

  return <button onClick={handleTokenValidation}>Validate Token</button>;
};
```

## Troubleshooting

### Common Issues

1. **CORS Errors**: Ensure your backend allows requests from your frontend domain
2. **Token Not Found**: Check if Queue-it cookies are being set correctly
3. **Redirect Loops**: Verify your fallback URLs are correct
4. **API Errors**: Check backend logs for Queue-it integration errors

### Debug Mode

Enable debug logging by checking the browser console for Queue-it related messages.

## Security Considerations

1. **Never expose secret keys** in frontend code
2. **Always validate tokens server-side**
3. **Use HTTPS** in production
4. **Validate all inputs** before sending to Queue-it APIs
5. **Handle errors gracefully** to prevent information leakage

## Support

For issues with the Queue-it integration:
1. Check the browser console for error messages
2. Verify your Queue-it event configuration
3. Test the backend APIs directly
4. Review the Queue-it documentation for your specific use case 