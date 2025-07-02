import { Link } from "react-router-dom";
import success from "../../../assets/images/icons/success.gif"
import Logo from "../../../components/logo/logo";

function RegistrationSuccessful() {
    return(

        <main className='order-success'>
            <div className='order-success-box'>
                <Logo />
                <svg width="100" height="100" viewBox="0 0 100 100" fill="none" style={{margin: '20px 0'}}><circle cx="50" cy="50" r="48" stroke="#bfa14a" strokeWidth="4" fill="#fff8dc"/><path d="M30 52L45 67L70 37" stroke="#bfa14a" strokeWidth="8" strokeLinecap="round" strokeLinejoin="round"/></svg>
                <h4 style={{ textAlign: "center", color: "#bfa14a" }}>
                    Congratulations, Your account has been successfully created!
                </h4>
                <Link to='/auth/login'><button>Login now</button></Link>
            </div>
        </main>
    )
}

export default RegistrationSuccessful;