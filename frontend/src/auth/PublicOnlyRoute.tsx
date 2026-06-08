import { Navigate } from "react-router-dom";
import { useAuth } from "./AuthContext";

export function PublicOnlyRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <main className="centered-page">Loading...</main>;
  }

  if (isAuthenticated) {
    return <Navigate to="/applications" replace />;
  }

  return children;
}
