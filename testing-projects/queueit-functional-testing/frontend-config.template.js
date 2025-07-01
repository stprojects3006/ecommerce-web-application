// Queue-It Frontend Configuration Template
// Copy this to frontend/src/queueit/queueit-config.js

export const queueitConfig = {
    customerId: 'your-customer-id',
    secretKey: 'your-secret-key-here',
    apiKey: 'your-api-key-here',
    queueDomain: 'your-customer-id.queue-it.net',
    
    events: {
        flashSale: {
            eventId: 'flash-sale-2024',
            layoutId: 'your-layout-id',
            culture: 'en-US'
        },
        blackFriday: {
            eventId: 'black-friday-2024',
            layoutId: 'your-layout-id',
            culture: 'en-US'
        },
        checkout: {
            eventId: 'checkout-protection',
            layoutId: 'your-layout-id',
            culture: 'en-US'
        }
    }
};

// Example values (replace with your actual credentials):
// customerId: 'futuraforge',
// secretKey: '12345678-1234-1234-1234-123456789012',
// apiKey: '87654321-4321-4321-4321-210987654321',
// queueDomain: 'futuraforge.queue-it.net',
