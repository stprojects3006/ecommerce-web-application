import { useEffect, useState } from "react"
import API_BASE_URL from "./apiConfig";
import axios from 'axios';

function CartService() {
    // Initialize cart from localStorage if present
    const getInitialCart = () => {
        const storedCart = localStorage.getItem("cart");
        return storedCart ? JSON.parse(storedCart) : {};
    }
    const [cart, setCart] = useState(getInitialCart())
    const [cartError, setError] = useState(false);
    const [isProcessingCart, setProcessing] = useState(false);

    const saveCartToLocalStorage = (cartObj) => {
        localStorage.setItem("cart", JSON.stringify(cartObj));
    }

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
        await getCartInformation()
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
        await getCartInformation()
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
        await getCartInformation()
    }

    const clearCart = async () => {
        setProcessing(true)
        try {
            const response = await axios.delete(`${API_BASE_URL}/cart-service/cart/clear/byId`, {
                headers: authHeader(),
                params: {
                    id: cart.cartId
                }
            });
            setError(false)
            setCart({cartItems: [], noOfCartItems: 0, subtotal: 0})
            saveCartToLocalStorage({cartItems: [], noOfCartItems: 0, subtotal: 0})
            await getCartInformation()
        } catch (error) {
            setError(true)
        }
        setProcessing(false)
    }

    const getCartInformation = async () => {
        const user = JSON.parse(localStorage.getItem("user"));
        if (!user?.token) {
            setCart({})
            saveCartToLocalStorage({})
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
                saveCartToLocalStorage(response.data.response)
            })
            .catch((error) => {
                setCart({cartItems:[]})
                saveCartToLocalStorage({cartItems:[]})
                setError(true)
            })
        setProcessing(false)
    }

    useEffect(() => {
        getCartInformation()
    }, [])

    // Also update localStorage whenever cart changes (for manual updates)
    useEffect(() => {
        saveCartToLocalStorage(cart)
    }, [cart])

    return { cart, cartError, isProcessingCart, addItemToCart, updateItemQuantity, removeItemFromCart, clearCart, getCartInformation };
}

export default CartService;