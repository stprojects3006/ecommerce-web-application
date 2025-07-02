import { Link } from 'react-router-dom';
import './EventHeader.css';
import Cart from '../cart/cart';
import { useContext, useState } from 'react';
import CartContext from '../../contexts/cart.contect';

function EventHeader() {
  const [isCartOpen, setCart] = useState(false);
  const { cart } = useContext(CartContext);

  const toggleCart = () => {
    setCart(prev => !prev);
  };

  return (
    <nav className="event-header">
      <Link to="/" className="event-home-link">🏠 Home</Link>
      <div className="event-cart-icon" onClick={toggleCart}>
        <i className="fa fa-shopping-cart" aria-hidden="true"></i>
        <span>({cart.noOfCartItems || 0})</span>
      </div>
      <Cart isCartOpen={isCartOpen} setIsCartOpen={setCart} onClose={() => setCart(false)} />
    </nav>
  );
}

export default EventHeader; 