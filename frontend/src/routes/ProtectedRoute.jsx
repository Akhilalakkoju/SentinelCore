import { Navigate } from "react-router-dom";
import { getCurrentRole } from "../services/auth";

function ProtectedRoute({ children, allowedRoles }) {
    const token = localStorage.getItem("token");
    const role = getCurrentRole();

    if (!token) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles && allowedRoles.length > 0 && (!role || !allowedRoles.includes(role))) {
        return <Navigate to="/login" replace />;
    }

    return children;
}

export default ProtectedRoute;