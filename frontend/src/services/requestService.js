import { api } from "./api.js";

export async function submitRequest(payload) {
  return api.post("/requests", payload);
}

export async function getMyRequests() {
  const response = await api.get("/requests/my-requests");
  return response.data || [];
}

export async function getAllRequests() {
  const response = await api.get("/admin/requests");
  return response.data || [];
}

export async function approveRequest(requestId) {
  return api.post("/admin/requests/approve", { requestId });
}

export async function rejectRequest(requestId, refusalReason) {
  return api.post("/admin/requests/reject", { requestId, refusalReason });
}
