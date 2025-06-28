import { useEffect, useState } from "react"
import API_BASE_URL from "./apiConfig";
import axios from 'axios';
import { useNavigate } from "react-router-dom";

function OrderService() {
    const [orderError, setError] = useState(null);
    const [isLoading, setLoading] = useState(false);
    const [userOrders, setUserOrders] = useState([])
    const navigate = useNavigate()

    const authHeader = () => {
        const user = JSON.parse(localStorage.getItem("user"));
        return { Authorization: `${user?.type}${user?.token}` };
    }

    const placeOrder = async ({ fname, lname, address1, address2, city, phone }, cart, clearCart) => {
        console.log(fname, lname, address1, address2, city, phone, cart)
        setLoading(true)
        try {
            const response = await axios.post(`${API_BASE_URL}/order-service/order/create`,
                { firstName: fname, lastName: lname, addressLine1: address1, addressLine2: address2, city: city, phoneNo: phone, cartId: cart },
                { headers: authHeader() }
            );
            
            setError(null)
            console.log("Order placed successfully:", response.data)
            
            // Clear the cart after successful order placement
            if (clearCart) {
                try {
                    console.log("Clearing cart...")
                    await clearCart();
                    console.log("Cart cleared successfully");
                } catch (error) {
                    console.error("Failed to clear cart:", error);
                }
            }
            
            navigate("/order/success")
        } catch (error) {
            console.log("Order placement failed:", error)
            setError(error.response?.data?.message || "Failed to place order")
        }
        setLoading(false)
    };

    const getOrdersByUser = async () => {
        setLoading(true)
        await axios.get(
            `${API_BASE_URL}/order-service/order/get/byUser`,
            { headers: authHeader() }
        ).then((response) => {
            setError(null)
            setUserOrders(response.data.response)
        }).catch((error) => {
            console.log(error)
            setUserOrders([])
        });
        setLoading(false)
    };

    useEffect(() => {
        getOrdersByUser()
    }, [])

    return { isLoading, orderError, userOrders, getOrdersByUser, placeOrder };

}

export default OrderService;