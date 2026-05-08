import { api } from "./api.js";
import { STORAGE_KEYS } from "../utils/constants.js";

export function saveSession(accessToken, user) {
  localStorage.setItem(STORAGE_KEYS.token, accessToken);
  localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(user));
}

export function clearSession() {
  localStorage.removeItem(STORAGE_KEYS.token);
  localStorage.removeItem(STORAGE_KEYS.user);
}

export function getStoredToken() {
  return localStorage.getItem(STORAGE_KEYS.token);
}

export function getStoredUser() {
  const raw = localStorage.getItem(STORAGE_KEYS.user);
  if (!raw) return null;

  try {
    return JSON.parse(raw);
  } catch {
    clearSession();
    return null;
  }
}

export async function login(email, password) {
  const response = await api.post("/auth/login", { email, password });
  const { accessToken, user } = response.data || {};

  if (!accessToken || !user) {
    throw new Error("La reponse de connexion est incomplete.");
  }

  saveSession(accessToken, user);
  return { accessToken, user, message: response.message };
}

export async function getCurrentUser() {
  const response = await api.get("/auth/me");
  return response.data;
}

export function logout() {
  clearSession();
}
