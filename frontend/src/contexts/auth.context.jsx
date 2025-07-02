import { createContext, useEffect, useState } from "react";

export const AuthContext = createContext();

export const useAuth = () => {
    const [user, setUser] = useState();

    const toggleUser = () => {
        const user = JSON.parse(localStorage.getItem("user"));
        setUser(user);
    };

    useEffect(() => {
        // Always clear user on app startup
        localStorage.removeItem("user");
        setUser(undefined);
        // console.log("toggling user")
    }, []);

    return { user, toggleUser };
};