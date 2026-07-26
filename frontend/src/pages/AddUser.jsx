import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { FaUser, FaEnvelope, FaLock, FaUserTag, FaSave, FaArrowLeft } from "react-icons/fa";
import { toast } from "react-toastify";

import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";
import AnimatedBackground from "../components/AnimatedBackground";
import GlassCard from "../components/ui/GlassCard";
import PageHeader from "../components/ui/PageHeader";
import api from "../services/api";

function AddUser() {
    const navigate = useNavigate();
    const [roles, setRoles] = useState([]);
    const [isSaving, setIsSaving] = useState(false);
    const [user, setUser] = useState({
        name: "",
        email: "",
        password: "",
        role: {
            id: ""
        }
    });

    useEffect(() => {
        fetchRoles();
    }, []);

    const fetchRoles = async () => {
        try {
            const response = await api.get("/roles");
            setRoles(response.data);
        } catch (error) {
            console.error("Failed to load roles:", error);
            toast.error("Failed to load roles");
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        if (name === "role") {
            setUser({
                ...user,
                role: {
                    id: value ? Number(value) : ""
                }
            });
        } else {
            setUser({
                ...user,
                [name]: value
            });
        }
    };

    const saveUser = async (e) => {
        e.preventDefault();
        
        // Basic Validation
        if (!user.name.trim()) {
            toast.warning("Please enter user name");
            return;
        }
        if (!user.email.trim()) {
            toast.warning("Please enter email address");
            return;
        }
        if (!user.password) {
            toast.warning("Please enter password");
            return;
        }
        if (!user.role.id) {
            toast.warning("Please select a security role");
            return;
        }

        setIsSaving(true);
        try {
            console.log("Sending User:", user);
            const response = await api.post("/users", user);
            console.log("Response:", response.data);
            toast.success("User added successfully!");
            navigate("/users");
        } catch (error) {
            console.error("FULL ERROR:", error);
            if (error.response) {
                console.log("Status:", error.response.status);
                console.log("Response:", error.response.data);
                toast.error(error.response.data.message || `Failed to add user (Status: ${error.response.status})`);
            } else if (error.request) {
                console.log(error.request);
                toast.error("No response received from backend server.");
            } else {
                console.log(error.message);
                toast.error(error.message);
            }
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <>
            <Navbar />
            <Sidebar />

            <main className="ml-64 mt-16 min-h-screen bg-slate-950 relative overflow-hidden">
                <AnimatedBackground />

                <div className="relative z-10 p-8 max-w-xl mx-auto">
                    {/* Back Button */}
                    <motion.button
                        whileHover={{ scale: 1.05, x: -4 }}
                        whileTap={{ scale: 0.95 }}
                        onClick={() => navigate("/users")}
                        className="flex items-center gap-2 text-slate-400 hover:text-white transition-colors duration-300 mb-6 bg-slate-900/60 border border-slate-800 rounded-xl px-4 py-2 text-sm font-semibold"
                    >
                        <FaArrowLeft className="text-xs" /> Back to Users
                    </motion.button>

                    <PageHeader
                        title="Add User"
                        subtitle="Create a new user account and assign system access permissions."
                    />

                    <GlassCard className="p-8">
                        <form onSubmit={saveUser} className="space-y-6">
                            {/* Full Name */}
                            <div>
                                <label className="block text-sm font-semibold text-slate-400 mb-2">Full Name *</label>
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500">
                                        <FaUser className="text-sm" />
                                    </div>
                                    <input
                                        type="text"
                                        name="name"
                                        required
                                        placeholder="Enter full name"
                                        value={user.name}
                                        onChange={handleChange}
                                        className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-11 pr-4 py-3 text-white placeholder:text-slate-600 focus:border-sky-400 focus:ring-2 focus:ring-sky-500/20 outline-none transition-all duration-300"
                                    />
                                </div>
                            </div>

                            {/* Email Address */}
                            <div>
                                <label className="block text-sm font-semibold text-slate-400 mb-2">Email Address *</label>
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500">
                                        <FaEnvelope className="text-sm" />
                                    </div>
                                    <input
                                        type="email"
                                        name="email"
                                        required
                                        placeholder="e.g., user@enterprise.com"
                                        value={user.email}
                                        onChange={handleChange}
                                        className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-11 pr-4 py-3 text-white placeholder:text-slate-600 focus:border-sky-400 focus:ring-2 focus:ring-sky-500/20 outline-none transition-all duration-300"
                                    />
                                </div>
                            </div>

                            {/* Password */}
                            <div>
                                <label className="block text-sm font-semibold text-slate-400 mb-2">Password *</label>
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500">
                                        <FaLock className="text-sm" />
                                    </div>
                                    <input
                                        type="password"
                                        name="password"
                                        required
                                        placeholder="••••••••"
                                        value={user.password}
                                        onChange={handleChange}
                                        className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-11 pr-4 py-3 text-white placeholder:text-slate-600 focus:border-sky-400 focus:ring-2 focus:ring-sky-500/20 outline-none transition-all duration-300"
                                    />
                                </div>
                            </div>

                            {/* Role Selection */}
                            <div>
                                <label className="block text-sm font-semibold text-slate-400 mb-2">Access Role *</label>
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500">
                                        <FaUserTag className="text-sm" />
                                    </div>
                                    <select
                                        name="role"
                                        required
                                        value={user.role.id}
                                        onChange={handleChange}
                                        className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-11 pr-4 py-3 text-white focus:border-sky-400 focus:ring-2 focus:ring-sky-500/20 outline-none transition-all duration-300 text-sm"
                                    >
                                        <option value="">Select Role</option>
                                        {roles.map((role) => (
                                            <option key={role.id} value={role.id}>
                                                {role.name}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                            </div>

                            {/* Save Button */}
                            <div className="pt-4 border-t border-slate-800/80">
                                <motion.button
                                    whileHover={{ scale: 1.02 }}
                                    whileTap={{ scale: 0.98 }}
                                    type="submit"
                                    disabled={isSaving}
                                    className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-sky-500 to-blue-600 hover:from-sky-400 hover:to-blue-500 text-white py-3.5 rounded-xl font-semibold shadow-lg shadow-sky-500/20 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300"
                                >
                                    <FaSave className="text-sm" /> {isSaving ? "Saving User..." : "Save User"}
                                </motion.button>
                            </div>
                        </form>
                    </GlassCard>
                </div>
            </main>
        </>
    );
}

export default AddUser;