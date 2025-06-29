// Queue-it Configuration for PURELY E-commerce Application
export const QUEUE_IT_CONFIG = {
  // Queue-it Account Configuration
  customerId: process.env.REACT_APP_QUEUE_IT_CUSTOMER_ID || 'futuraforge',
  secretKey: process.env.REACT_APP_QUEUE_IT_SECRET_KEY || 'your-secret-key',
  apiKey: process.env.REACT_APP_QUEUE_IT_API_KEY || 'your-api-key',
  
  // Queue Settings
  isEnqueueTokenEnabled: true,
  enqueueTokenKeyEnabled: true,
  enqueueTokenValidityTime: 20, // minutes
  
  // Event Configuration
  events: {
    // Flash Sale Event
    flashSale: {
      eventId: 'flash-sale-2024',
      queueDomain: 'futuraforge.queue-it.net',
      cookieValidityMinute: 20,
      extendCookieValidity: true,
      triggers: [
        {
          operator: 'Contains',
          valueToCompare: '/flash-sale',
          urlPart: 'PageUrl',
          validatorType: 'UrlValidator',
          isIgnoreCase: true
        }
      ]
    },
    
    // Black Friday Event
    blackFriday: {
      eventId: 'black-friday-2024',
      queueDomain: 'futuraforge.queue-it.net',
      cookieValidityMinute: 30,
      extendCookieValidity: true,
      triggers: [
        {
          operator: 'Contains',
          valueToCompare: '/black-friday',
          urlPart: 'PageUrl',
          validatorType: 'UrlValidator',
          isIgnoreCase: true
        }
      ]
    },
    
    // High Traffic Protection
    highTraffic: {
      eventId: 'high-traffic-protection',
      queueDomain: 'futuraforge.queue-it.net',
      cookieValidityMinute: 15,
      extendCookieValidity: true,
      triggers: [
        {
          operator: 'Contains',
          valueToCompare: '/products',
          urlPart: 'PageUrl',
          validatorType: 'UrlValidator',
          isIgnoreCase: true
        }
      ]
    },
    
    // Checkout Protection
    checkout: {
      eventId: 'checkout-protection',
      queueDomain: 'futuraforge.queue-it.net',
      cookieValidityMinute: 10,
      extendCookieValidity: true,
      triggers: [
        {
          operator: 'Contains',
          valueToCompare: '/order/checkout',
          urlPart: 'PageUrl',
          validatorType: 'UrlValidator',
          isIgnoreCase: true
        }
      ]
    }
  },
  
  // Queue-it Integration Settings
  integration: {
    isCookieHttpOnly: false,
    isCookieSecure: process.env.NODE_ENV === 'production',
    cookieDomain: '',
    redirectLogic: 'AllowTParameter',
    forcedTargetUrl: null
  },
  
  // Development Settings
  development: {
    enabled: process.env.REACT_APP_QUEUE_IT_ENABLED === 'true',
    debug: process.env.NODE_ENV === 'development',
    bypassQueue: process.env.REACT_APP_QUEUE_IT_BYPASS === 'true'
  }
};

// Queue-it Event Types
export const QUEUE_EVENTS = {
  FLASH_SALE: 'flashSale',
  BLACK_FRIDAY: 'blackFriday',
  HIGH_TRAFFIC: 'highTraffic',
  CHECKOUT: 'checkout'
};

// Queue-it Status Types
export const QUEUE_STATUS = {
  IDLE: 'idle',
  QUEUING: 'queuing',
  QUEUED: 'queued',
  ENTERED: 'entered',
  ERROR: 'error'
};

// Queue-it Error Types
export const QUEUE_ERRORS = {
  NETWORK_ERROR: 'network_error',
  CONFIGURATION_ERROR: 'configuration_error',
  AUTHENTICATION_ERROR: 'authentication_error',
  QUEUE_FULL: 'queue_full',
  EVENT_NOT_FOUND: 'event_not_found'
};

export default QUEUE_IT_CONFIG; 