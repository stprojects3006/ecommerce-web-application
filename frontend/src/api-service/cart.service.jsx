import { useEffect, useState } from "react"
import API_BASE_URL from "./apiConfig";
import axios from 'axios';

function CartService() {
    const [cart, setCart] = useState({})
    const [cartError, setError] = useState(false);
    const [isProcessingCart, setProcessing] = useState(false);

    const authHeader = () => {
        const user = JSON.parse(localStorage.getItem("user"));
        return { Authorization: `${user?.type}${user?.token}` };
    }

    const addItemToCart = async (productId, quantity) => {
        setProcessing(true)
        await axios.post(
            `${API_BASE_URL}/cart-service/cart/add`,
            { productId, quantity },
            { headers: authHeader() }
        )
            .then((response) => {
                setError(false)
            })
            .catch((error) => {
                setError(true)
            })
        setProcessing(false)
        getCartInformation()
    }

    const updateItemQuantity = async (productId, quantity) => {
        setProcessing(true)
        await axios.post(
            `${API_BASE_URL}/cart-service/cart/add`,
            { productId, quantity },
            { headers: authHeader() }
        )
            .then((response) => {
                setError(false)
            })
            .catch((error) => {
                setError(true)
            })
        setProcessing(false)
        getCartInformation()
    }

    const removeItemFromCart = async (productId) => {
        setProcessing(true)
        await axios.delete(`${API_BASE_URL}/cart-service/cart/remove`, {
            headers: authHeader(),
            params: {
                productId: productId
            }
        })
            .then((response) => {
                setError(false)
            })
            .catch((error) => {
                setError(true)
            })
        getCartInformation()
    }

    const clearCart = async () => {
        console.log("Starting cart clear process...")
        setProcessing(true)
        try {
            const response = await axios.delete(`${API_BASE_URL}/cart-service/cart/clear/byId`, {
                headers: authHeader(),
                params: {
                    id: cart.cartId
                }
            });
            
            console.log("Cart clear response:", response.data)
            setError(false)
            setCart({cartItems: [], noOfCartItems: 0, subtotal: 0})
            console.log("Cart state updated locally")
            
            // Refresh cart information to ensure state is updated
            await getCartInformation()
            console.log("Cart information refreshed")
        } catch (error) {
            console.error("Failed to clear cart:", error)
            setError(true)
        }
        setProcessing(false)
        console.log("Cart clear process completed")
    }

    const getCartInformation = async () => {
        const user = JSON.parse(localStorage.getItem("user"));
        if (!user?.token) {
            setCart({})
            setError(false)
            return
        }
        setProcessing(true)
        await axios.get(`${API_BASE_URL}/cart-service/cart/get/byUser`, {
            headers: authHeader()
        })
            .then((response) => {
                setError(false)
                setCart(response.data.response)
            })
            .catch((error) => {
                setCart({cartItems:[]})
                setError(true)
            })
        setProcessing(false)
    }

    useEffect(() => {
        getCartInformation()
    }, [])

    return { cart, cartError, isProcessingCart, addItemToCart, updateItemQuantity, removeItemFromCart, clearCart, getCartInformation };

}

export default CartService;