import { useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthLayout from "../components/auth/AuthLayout";
import RegisterForm from "../components/auth/RegisterForm";
import api from "../services/api";
import { toast } from "react-hot-toast";

function Register() {

    const navigate = useNavigate();

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [loading, setLoading] = useState(false);

    const handleRegister = async (e) => {

        e.preventDefault();

        if (!name || !email || !password || !confirmPassword) {
            toast.error("Please fill in all required fields.");
            return;
        }

        if (password !== confirmPassword) {
            toast.error("Passwords do not match.");
            return;
        }

        if (password.length < 8) {
            toast.error("Password must be at least 8 characters long.");
            return;
        }

        try {

            setLoading(true);

            const response = await api.post("/auth/register", {
                name,
                email,
                password
            });

            const successMessage = typeof response.data === "string" 
                ? response.data 
                : (response.data?.message || "Registration Successful!");

            toast.success(successMessage);
            navigate("/login");

        } catch (error) {

            let errorMessage = "Registration failed. Please try again.";

            if (error.response?.data) {
                if (typeof error.response.data === "string") {
                    errorMessage = error.response.data;
                } else if (error.response.data.message) {
                    errorMessage = error.response.data.message;
                }
            } else if (error.message) {
                errorMessage = error.message;
            }

            toast.error(errorMessage);

        } finally {

            setLoading(false);

        }
    };

    return (
        <AuthLayout>
            <RegisterForm
                name={name}
                email={email}
                password={password}
                confirmPassword={confirmPassword}
                setName={setName}
                setEmail={setEmail}
                setPassword={setPassword}
                setConfirmPassword={setConfirmPassword}
                handleRegister={handleRegister}
                loading={loading}
            />
        </AuthLayout>
    );
}

export default Register;