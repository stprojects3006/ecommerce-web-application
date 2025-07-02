import { Link } from "react-router-dom";
import Logo from "../../components/logo/logo";
import { AuthContext } from "../../contexts/auth.context";
import { useContext } from "react";
import './checkout.css'

function OrderSuccess() {

    const { user, toggleUser } = useContext(AuthContext)

    return (
        <main className='order-success'>
            <div className='order-success-box'>
                <Logo />
                <svg width="100" height="100" viewBox="0 0 100 100" fill="none" style={{margin: '20px 0'}}><circle cx="50" cy="50" r="48" stroke="#bfa14a" strokeWidth="4" fill="#fff8dc"/><path d="M30 52L45 67L70 37" stroke="#bfa14a" strokeWidth="8" strokeLinecap="round" strokeLinejoin="round"/></svg>
                <h4 style={{ textAlign: "center", color: "#bfa14a" }}>
                    Thank you for your order!<br />
                    Your order has been successfully placed.
                </h4>
                <h4 style={{ textAlign: "center", color: "#bfa14a" }}>Order confirmation email has been sent to {user?.email}.</h4>
                <Link to='/'><button>Go home</button></Link>
            </div>
        </main>
    )
}

export default OrderSuccess;