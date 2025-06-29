import React, { useState, useEffect } from 'react';
import { useQueueStatus, useQueueActions } from '../../queueit/queueit-context';
import { QUEUE_EVENTS } from '../../queueit/queueit-config';
import './flash-sale.css';

const FlashSale = () => {
  const { isQueuing, isQueued, isEntered, position, estimatedWaitTime } = useQueueStatus();
  const { triggerQueue, bypassQueue } = useQueueActions();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Simulate loading flash sale products
    const loadProducts = async () => {
      setLoading(true);
      // Simulate API call delay
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      const flashSaleProducts = [
        {
          id: 1,
          name: "Premium Wireless Headphones",
          originalPrice: 299.99,
          salePrice: 149.99,
          discount: 50,
          image: "https://via.placeholder.com/300x200?text=Headphones",
          stock: 15
        },
        {
          id: 2,
          name: "Smart Fitness Watch",
          originalPrice: 199.99,
          salePrice: 99.99,
          discount: 50,
          image: "https://via.placeholder.com/300x200?text=Smart+Watch",
          stock: 8
        },
        {
          id: 3,
          name: "4K Ultra HD TV",
          originalPrice: 899.99,
          salePrice: 449.99,
          discount: 50,
          image: "https://via.placeholder.com/300x200?text=4K+TV",
          stock: 5
        },
        {
          id: 4,
          name: "Gaming Laptop",
          originalPrice: 1299.99,
          salePrice: 649.99,
          discount: 50,
          image: "https://via.placeholder.com/300x200?text=Gaming+Laptop",
          stock: 3
        }
      ];
      
      setProducts(flashSaleProducts);
      setLoading(false);
    };

    loadProducts();
  }, []);

  const handleTriggerQueue = () => {
    triggerQueue(QUEUE_EVENTS.FLASH_SALE);
  };

  const handleBypassQueue = () => {
    bypassQueue();
  };

  if (loading) {
    return (
      <div className="flash-sale-container">
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p>Loading Flash Sale...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flash-sale-container">
      <div className="flash-sale-header">
        <h1>🔥 FLASH SALE 🔥</h1>
        <p className="sale-description">
          Limited time offers! Up to 50% off on premium products.
          <br />
          <strong>Sale ends in: 2 hours 15 minutes</strong>
        </p>
        
        {/* Queue Status Display */}
        {isQueuing && (
          <div className="queue-notice queuing">
            <span>🔄 Joining queue...</span>
          </div>
        )}
        
        {isQueued && (
          <div className="queue-notice queued">
            <span>⏳ You're in queue! Position: {position}</span>
            {estimatedWaitTime && (
              <span> | Est. wait: {estimatedWaitTime} minutes</span>
            )}
          </div>
        )}
        
        {isEntered && (
          <div className="queue-notice entered">
            <span>✅ You're in! Shop now before items sell out!</span>
          </div>
        )}
      </div>

      {/* Development Controls */}
      {process.env.NODE_ENV === 'development' && (
        <div className="dev-controls">
          <h3>Development Controls</h3>
          <div className="control-buttons">
            <button 
              className="trigger-queue-btn"
              onClick={handleTriggerQueue}
              disabled={isQueuing || isQueued}
            >
              Trigger Queue
            </button>
            <button 
              className="bypass-queue-btn"
              onClick={handleBypassQueue}
            >
              Bypass Queue
            </button>
          </div>
        </div>
      )}

      {/* Products Grid */}
      <div className="products-grid">
        {products.map(product => (
          <div key={product.id} className="product-card">
            <div className="product-image">
              <img src={product.image} alt={product.name} />
              <div className="discount-badge">
                -{product.discount}%
              </div>
            </div>
            <div className="product-info">
              <h3>{product.name}</h3>
              <div className="price-container">
                <span className="original-price">${product.originalPrice}</span>
                <span className="sale-price">${product.salePrice}</span>
              </div>
              <div className="stock-info">
                <span className="stock-badge">
                  {product.stock > 0 ? `${product.stock} left` : 'Sold Out'}
                </span>
              </div>
              <button 
                className="add-to-cart-btn"
                disabled={product.stock === 0}
              >
                {product.stock > 0 ? 'Add to Cart' : 'Sold Out'}
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Sale Timer */}
      <div className="sale-timer">
        <h3>⏰ Sale Ends In:</h3>
        <div className="timer-display">
          <div className="time-unit">
            <span className="time-value">02</span>
            <span className="time-label">Hours</span>
          </div>
          <div className="time-separator">:</div>
          <div className="time-unit">
            <span className="time-value">15</span>
            <span className="time-label">Minutes</span>
          </div>
          <div className="time-separator">:</div>
          <div className="time-unit">
            <span className="time-value">30</span>
            <span className="time-label">Seconds</span>
          </div>
        </div>
      </div>

      {/* Queue Information */}
      <div className="queue-info">
        <h3>ℹ️ About This Queue</h3>
        <p>
          This flash sale is experiencing high traffic. The queue ensures everyone gets a fair chance to shop.
          Don't close this page or you'll lose your place in line!
        </p>
        <ul>
          <li>✅ Fair access for all customers</li>
          <li>✅ Prevents website crashes</li>
          <li>✅ Maintains your place in line</li>
          <li>✅ Automatic redirect when ready</li>
        </ul>
      </div>
    </div>
  );
};

export default FlashSale; 