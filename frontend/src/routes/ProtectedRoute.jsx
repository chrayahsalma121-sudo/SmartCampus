import { Navigate, useLocation } from "react-router-dom";
import LoadingState from "../components/LoadingState.jsx";
import { useAuth } from "../hooks/useAuth.js";

export default function ProtectedRoute({ roles, children }) {
  const { isAuthenticated, loading, user } = useAuth();
  const location = useLocation();

  if (loading) {
    return <LoadingState label="Verification de la session" fullPage />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (roles?.length && !roles.includes(user?.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
}
