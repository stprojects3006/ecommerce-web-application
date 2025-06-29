import { BrowserRouter } from "react-router-dom"
import AppRoutes from "./routes/routes"
import './assets/styles/index.css'
import { AuthContext, useAuth } from "./contexts/auth.context"
import CartService from "./api-service/cart.service";
import CartContext from "./contexts/cart.contect";
import { QueueItProvider } from "./queueit/queueit-context";
import QueueOverlay from "./queueit/components/QueueOverlay";
import QueueIndicator from "./queueit/components/QueueIndicator";

function App() {

  const {user, toggleUser} = useAuth();
  const { cart, cartError, isProcessingCart, addItemToCart, removeItemFromCart, clearCart, getCartInformation } = CartService();

  return (
    <QueueItProvider>
      <BrowserRouter>
        <AuthContext.Provider value={{user, toggleUser}}>
          <CartContext.Provider value={{ cart, cartError, isProcessingCart, addItemToCart, removeItemFromCart, clearCart, getCartInformation }}>
            <AppRoutes/>
            <QueueOverlay />
            <QueueIndicator />
          </CartContext.Provider>
        </AuthContext.Provider>
      </BrowserRouter>
    </QueueItProvider>
  )
}

export default App
