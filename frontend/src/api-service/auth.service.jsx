import { useState } from "react"
import API_BASE_URL from "./apiConfig";
import axios from 'axios';
import { useNavigate } from "react-router-dom";

function AuthService() {
    const [isLoading, setLoading] = useState(false);
    const [response, setResponse] = useState(null);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const login = async (email, password) => {
        console.log(email, password)
        setLoading(true)
        await axios.post(`${API_BASE_URL}/auth-service/auth/signin`, { email: email, password: password })
            .then((response) => {
                if (response.data.response.token) {
                    setResponse(true);
                    setError(null)
                    localStorage.setItem("user", JSON.stringify(response.data.response));
                }
            })
            .catch((error) => {
                console.log(error)
                setResponse(null)
                const resMessage = (error.response && error.response.data && error.response.data.message) || error.message || error.toString();
                if (resMessage == "Bad credentials") {
                    setError("Invalid email or password!");
                } else {
                    setError("Unable to perform login now. Try again later!");
                }
            })
        setLoading(false)
    }

    const save = async (userName, email, password) => {
        setLoading(true)
        await axios.post(`${API_BASE_URL}/auth-service/auth/signup`, { userName, email, password })
            .then((response) => {
                console.log(response.data)
                if (response.data.success) {
                    setResponse(true);
                    setError(null)
                    const verificationCode = response.data.response?.verificationCode;
                    
                    // Store user info temporarily for later use during verification
                    const tempUserInfo = {
                        username: userName,
                        email: email,
                    };
                    localStorage.setItem("tempUserInfo", JSON.stringify(tempUserInfo));
                    
                    navigate(`/auth/userRegistrationVerfication/${email}`, { state: { verificationCode } });
                }
            })
            .catch((error) => {
                console.error(error)
                setResponse(null)
                if (error.response) {
                    setError(error.response.data.message);
                } else {
                    setError("Unable to perform register now. Try again later!");
                }
            })
        setLoading(false)
    }

    const verifyRegistration = async (verificationCode) => {
        setLoading(true)
        await axios.get(`${API_BASE_URL}/auth-service/auth/signup/verify`, {
                params: {
                    code: verificationCode
                }
            })
            .then(async (response) => {
                console.log(response.data)
                if (response.data.success) {
                    setResponse(true);
                    setError(null)
                    
                    // Automatically create user in user-service after successful verification
                    try {
                        // Get the temporary user info stored during signup
                        const tempUserInfo = JSON.parse(localStorage.getItem("tempUserInfo"));
                        if (tempUserInfo) {
                            // First, log in to get the user ID
                            const tempPassword = localStorage.getItem("tempPassword");
                            if (tempPassword) {
                                const loginResponse = await axios.post(`${API_BASE_URL}/auth-service/auth/signin`, { 
                                    email: tempUserInfo.email, 
                                    password: tempPassword
                                });
                                
                                if (loginResponse.data.response) {
                                    const userInfo = loginResponse.data.response;
                                    
                                    // Create user in user-service
                                    const createUserResult = await createUserInUserService(userInfo);
                                    console.log("User creation result:", createUserResult);
                                    
                                    // Store the user info for the session
                                    localStorage.setItem("user", JSON.stringify(userInfo));
                                    
                                    // Clean up temporary data
                                    localStorage.removeItem("tempUserInfo");
                                    localStorage.removeItem("tempPassword");
                                }
                            }
                        }
                    } catch (err) {
                        console.error("Failed to create user in user-service:", err);
                        // Don't block the verification success, just log the error
                    }
                    
                    navigate(`/auth/success-registration`);
                }
            })
            .catch((error) => {
                console.error(error)
                setResponse(null)
                if (error.response) {
                    setError(error.response.data.message);
                } else {
                    setError("Unable to perform verify now. Try again later!");
                }
            })
        setLoading(false)
    }

    const resendVerificationCode = async (email) => {
        await axios.get(`${API_BASE_URL}/auth-service/auth/signup/resend`, {
                params: {
                    email: email
                }
            })
            .then((response) => {
                console.log(response.data)
                if (response.data.success) {
                    setResponse(true);
                    setError(null)
                }
            })
            .catch((error) => {
                console.error(error)
                setResponse(null)
                if (error.response) {
                    setError(error.response.data.message);
                } else {
                    setError("Unable to resend code now. Try again later!");
                }
            })
    }

    const createUserInUserService = async (user) => {
        try {
            const res = await axios.post(
                `${API_BASE_URL}/user-service/user/create`,
                {
                    userId: user.id,
                    username: user.username,
                    email: user.email
                }
            );
            return { success: true, message: res.data.message };
        } catch (err) {
            return { success: false, message: err.response?.data?.message || err.message };
        }
    };

    return {login, save, verifyRegistration, resendVerificationCode, isLoading, response, error, createUserInUserService};
}

export default AuthService;