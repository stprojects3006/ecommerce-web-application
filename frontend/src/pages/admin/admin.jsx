import React, { useState, useEffect } from 'react';
import Header from '../../components/header/header';
import Footer from '../../components/footer/footer';
import Info from '../../components/info/info';
import axios from 'axios';

function AdminPanel() {
  // State for admin user management
  const [newAdminEmail, setNewAdminEmail] = useState("");
  const [newAdminPassword, setNewAdminPassword] = useState("");
  const [adminMessage, setAdminMessage] = useState("");

  // State for orders
  const [orders, setOrders] = useState([]);
  const [orderMessage, setOrderMessage] = useState("");

  // State for queue events
  const [queueEvents, setQueueEvents] = useState([
    { name: 'Flash Sale', ended: false },
    { name: 'Black Friday', ended: false },
    { name: 'Checkout Queue', ended: false },
  ]);

  // State for order search
  const [searchEmail, setSearchEmail] = useState("");

  // State for managing cart items
  const [cartUser, setCartUser] = useState(null);
  const [cartItems, setCartItems] = useState([]);
  const [cartMessage, setCartMessage] = useState("");

  const API_GATEWAY_URL = '/api'; // Adjust if needed

  // Fetch orders (simulate API call)
  useEffect(() => {
    // TODO: Replace with real API call
    setOrders([
      { id: 1, user: 'user1@example.com', status: 'Pending', amount: 100 },
      { id: 2, user: 'user2@example.com', status: 'Shipped', amount: 200 },
    ]);
  }, []);

  // Filtered orders by email
  const filteredOrders = searchEmail
    ? orders.filter(order => order.user.toLowerCase().includes(searchEmail.toLowerCase()))
    : orders;

  // Add new admin user (simulate API call)
  const handleAddAdmin = (e) => {
    e.preventDefault();
    // TODO: Replace with real API call
    setAdminMessage(`Admin user ${newAdminEmail} added!`);
    setNewAdminEmail("");
    setNewAdminPassword("");
    setTimeout(() => setAdminMessage(""), 2000);
  };

  // Modify or delete order (simulate API call)
  const handleDeleteOrder = (orderId) => {
    setOrders(orders.filter(o => o.id !== orderId));
    setOrderMessage(`Order ${orderId} deleted.`);
    setTimeout(() => setOrderMessage(""), 2000);
  };

  // Mark end of queue event
  const handleEndQueueEvent = (eventName) => {
    setQueueEvents(queueEvents.map(ev => ev.name === eventName ? { ...ev, ended: true } : ev));
  };

  // Simulate fetching cart items for a user
  const handleViewCart = async (userEmail) => {
    setCartUser(userEmail);
    setCartItems([]);
    setCartMessage('');
    try {
      // 1. Get userId by email
      const userRes = await axios.get(`${API_GATEWAY_URL}/user-service/user/get/byEmail`, { params: { email: userEmail } });
      const userId = userRes.data.response.userId;
      // 2. Get cart by userId
      const cartRes = await axios.get(`${API_GATEWAY_URL}/cart-service/cart/get/byUserId`, { params: { userId } });
      const cart = cartRes.data.response;
      setCartItems(cart.cartItems || []);
    } catch (err) {
      setCartMessage('Failed to fetch cart for user.');
    }
  };

  // Remove cart item
  const handleRemoveCartItem = async (productId) => {
    try {
      // 1. Get userId by email
      const userRes = await axios.get(`${API_GATEWAY_URL}/user-service/user/get/byEmail`, { params: { email: cartUser } });
      const userId = userRes.data.response.userId;
      // 2. Remove item from cart
      await axios.delete(`${API_GATEWAY_URL}/cart-service/cart/remove/byAdmin`, { params: { userId, productId } });
      setCartItems(cartItems.filter(item => item.productId !== productId));
      setCartMessage('Item removed from cart.');
      setTimeout(() => setCartMessage(""), 2000);
    } catch (err) {
      setCartMessage('Failed to remove item from cart.');
    }
  };

  // Simulate closing cart view
  const handleCloseCart = () => {
    setCartUser(null);
    setCartItems([]);
  };

  return (
    <>
      <Header />
      <div className="admin-panel" style={{ padding: '12vh 5% 40px' }}>
        <h1>Admin Panel</h1>
        <section style={{ marginBottom: 40 }}>
          <h2>Add New Admin User</h2>
          <form onSubmit={handleAddAdmin} style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
            <input type="email" placeholder="Admin Email" value={newAdminEmail} onChange={e => setNewAdminEmail(e.target.value)} required />
            <input type="password" placeholder="Password" value={newAdminPassword} onChange={e => setNewAdminPassword(e.target.value)} required />
            <button type="submit">Add Admin</button>
          </form>
          {adminMessage && <Info message={adminMessage} />}
        </section>
        <section style={{ marginBottom: 40 }}>
          <h2>Manage Orders</h2>
          <div style={{ marginBottom: 10 }}>
            <input
              type="email"
              placeholder="Search by user email"
              value={searchEmail}
              onChange={e => setSearchEmail(e.target.value)}
              style={{ minWidth: 250, marginRight: 10 }}
            />
            <button onClick={() => setSearchEmail("")}>Clear</button>
          </div>
          {orderMessage && <Info message={orderMessage} />}
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th>ID</th>
                <th>User</th>
                <th>Status</th>
                <th>Amount</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredOrders.map(order => (
                <tr key={order.id}>
                  <td>{order.id}</td>
                  <td>{order.user}</td>
                  <td>{order.status}</td>
                  <td>{order.amount}</td>
                  <td>
                    <button onClick={() => handleDeleteOrder(order.id)}>Delete</button>
                    <button style={{ marginLeft: 8 }} onClick={() => handleViewCart(order.user)}>View Cart</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
        <section>
          <h2>Queue Events</h2>
          <ul>
            {queueEvents.map(ev => (
              <li key={ev.name} style={{ marginBottom: 10 }}>
                {ev.name} - {ev.ended ? <span style={{ color: 'red' }}>Ended</span> : <button onClick={() => handleEndQueueEvent(ev.name)}>Mark as Ended</button>}
              </li>
            ))}
          </ul>
        </section>
      </div>
      {cartUser && (
        <div style={{ background: '#f8f9fa', padding: 20, borderRadius: 8, margin: '20px 0' }}>
          <h3>Cart for {cartUser}</h3>
          {cartMessage && <Info message={cartMessage} />}
          {cartItems.length === 0 ? (
            <p>No items in cart.</p>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: 10 }}>
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Quantity</th>
                  <th>Price</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {cartItems.map(item => (
                  <tr key={item.productId}>
                    <td>{item.name}</td>
                    <td>{item.quantity}</td>
                    <td>{item.price}</td>
                    <td><button onClick={() => handleRemoveCartItem(item.productId)}>Remove</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          <button onClick={handleCloseCart}>Close</button>
        </div>
      )}
      <Footer />
    </>
  );
}

export default AdminPanel; 