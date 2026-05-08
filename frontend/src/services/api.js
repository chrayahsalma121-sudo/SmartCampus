import { API_BASE_URL, STORAGE_KEYS } from "../utils/constants.js";

export class ApiError extends Error {
  constructor(message, status, body) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

function getToken() {
  return localStorage.getItem(STORAGE_KEYS.token);
}

function buildUrl(path) {
  return `${API_BASE_URL}${path.startsWith("/") ? path : `/${path}`}`;
}

async function parseResponse(response) {
  const text = await response.text();
  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return { success: false, message: text };
  }
}

async function request(path, options = {}) {
  const token = getToken();
  const headers = new Headers(options.headers || {});

  if (!headers.has("Content-Type") && options.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  let response;
  try {
    response = await fetch(buildUrl(path), {
      ...options,
      headers,
    });
  } catch (error) {
    throw new ApiError(
      "Impossible de joindre le backend. Verifiez que le serveur Java est demarre.",
      0,
      error,
    );
  }

  const body = await parseResponse(response);
  const message = body?.message || body?.detail || response.statusText;

  if (!response.ok || body?.success === false) {
    throw new ApiError(message || "Erreur backend.", response.status, body);
  }

  return body || { success: true, message: "OK", data: null };
}

export const api = {
  get(path) {
    return request(path, { method: "GET" });
  },
  post(path, payload) {
    return request(path, {
      method: "POST",
      body: payload === undefined ? undefined : JSON.stringify(payload),
    });
  },
};
