import { createContext, useEffect, useState } from "react";
import {
  getCurrentUser,
  getStoredToken,
  getStoredUser,
  login as loginRequest,
  logout as logoutRequest,
  saveSession,
} from "../services/authService.js";
import { getDashboardPath } from "../utils/constants.js";
import { getErrorMessage } from "../utils/errorUtils.js";

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(() => getStoredToken());
  const [user, setUser] = useState(() => getStoredUser());
  const [loading, setLoading] = useState(true);
  const [sessionError, setSessionError] = useState("");

  useEffect(() => {
    let ignore = false;

    async function refreshSession() {
      if (!getStoredToken()) {
        setLoading(false);
        return;
      }

      try {
        const currentUser = await getCurrentUser();
        if (ignore) return;
        const token = getStoredToken();
        setUser(currentUser);
        setAccessToken(token);
        saveSession(token, currentUser);
      } catch (error) {
        if (ignore) return;
        logoutRequest();
        setUser(null);
        setAccessToken(null);
        setSessionError(getErrorMessage(error, "Session expiree."));
      } finally {
        if (!ignore) setLoading(false);
      }
    }

    refreshSession();

    return () => {
      ignore = true;
    };
  }, []);

  async function login(email, password) {
    const session = await loginRequest(email, password);
    setAccessToken(session.accessToken);
    setUser(session.user);
    setSessionError("");
    return session;
  }

  function logout() {
    logoutRequest();
    setAccessToken(null);
    setUser(null);
  }

  async function refreshCurrentUser() {
    const currentUser = await getCurrentUser();
    const token = getStoredToken();
    setUser(currentUser);
    setAccessToken(token);
    saveSession(token, currentUser);
    return currentUser;
  }

  const value = {
    accessToken,
    user,
    loading,
    sessionError,
    isAuthenticated: Boolean(accessToken && user),
    isStudentValid: user?.role !== "STUDENT" || user?.valid === true,
    dashboardPath: getDashboardPath(user?.role),
    login,
    logout,
    refreshCurrentUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
