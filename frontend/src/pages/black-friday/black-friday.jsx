import React, { useState, useEffect, useContext } from 'react';
import { useQueueStatus, useQueueActions } from '../../queueit/queueit-context';
import { QUEUE_EVENTS } from '../../queueit/queueit-config';
import '../flash-sale/flash-sale.css';
import EventHeader from '../../components/header/EventHeader';
import CartContext from '../../contexts/cart.contect';
import { AuthContext } from '../../contexts/auth.context';
import { useNavigate } from 'react-router-dom';
import Info from '../../components/info/info';

const BlackFriday = () => {
  const { isQueuing, isQueued, isEntered, position, estimatedWaitTime } = useQueueStatus();
  const { triggerQueue, bypassQueue } = useQueueActions();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const { addItemToCart } = useContext(CartContext);
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [cartMessage, setCartMessage] = useState("");

  useEffect(() => {
    // Simulate loading Black Friday products
    const loadProducts = async () => {
      setLoading(true);
      await new Promise(resolve => setTimeout(resolve, 1000));
      const blackFridayProducts = [
        {
          id: 1,
          name: "Ultra HD Smart TV",
          originalPrice: 1199.99,
          salePrice: 599.99,
          discount: 50,
          image: "https://via.placeholder.com/300x200?text=Smart+TV",
          stock: 10
        },
        {
          id: 2,
          name: "Bluetooth Speaker",
          originalPrice: 149.99,
          salePrice: 74.99,
          discount: 50,
          image: "https://via.placeholder.com/300x200?text=Speaker",
          stock: 20
        },
        {
          id: 3,
          name: "Wireless Earbuds",
          originalPrice: 99.99,
          salePrice: 49.99,
          discount: 50,
          image: "https://via.placeholder.com/300x200?text=Earbuds",
          stock: 30
        },
        {
          id: 4,
          name: "Gaming Console",
          originalPrice: 499.99,
          salePrice: 299.99,
          discount: 40,
          image: "https://via.placeholder.com/300x200?text=Console",
          stock: 5
        }
      ];
      setProducts(blackFridayProducts);
      setLoading(false);
    };
    loadProducts();
  }, []);

  const handleTriggerQueue = () => {
    triggerQueue(QUEUE_EVENTS.BLACK_FRIDAY);
  };

  const handleBypassQueue = () => {
    bypassQueue();
  };

  const handleAddToCart = (product) => {
    if (!user) {
      navigate('/auth/login');
      return;
    }
    addItemToCart(product.id, 1);
    setCartMessage(`${product.name} added to cart!`);
    setTimeout(() => setCartMessage(""), 2000);
  };

  if (loading) {
    return (
      <div className="flash-sale-container">
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p>Loading Black Friday Deals...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flash-sale-container">
      <EventHeader />
      {cartMessage && <Info message={cartMessage} />}
      <div className="flash-sale-header">
        <h1>🖤 BLACK FRIDAY 🖤</h1>
        <p className="sale-description">
          Massive Black Friday discounts! Up to 50% off on top electronics and more.<br />
          <strong>Sale ends in: 1 day 5 hours</strong>
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
                onClick={() => handleAddToCart(product)}
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
            <span className="time-value">01</span>
            <span className="time-label">Days</span>
          </div>
          <div className="time-separator">:</div>
          <div className="time-unit">
            <span className="time-value">05</span>
            <span className="time-label">Hours</span>
          </div>
          <div className="time-separator">:</div>
          <div className="time-unit">
            <span className="time-value">00</span>
            <span className="time-label">Minutes</span>
          </div>
        </div>
      </div>
      {/* Queue Information */}
      <div className="queue-info">
        <h3>ℹ️ About This Queue</h3>
        <p>
          Black Friday is experiencing high traffic. The queue ensures everyone gets a fair chance to shop.<br />
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

export default BlackFriday; 