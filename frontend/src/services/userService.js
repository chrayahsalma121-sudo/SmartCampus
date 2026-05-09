import { api } from "./api.js";

export async function getUsers() {
  const response = await api.get("/admin/users");
  return response.data || [];
}

export function createUser(payload) {
  return api.post("/admin/users", payload);
}

export function deleteUser(userId) {
  return api.post("/admin/users/delete", { userId });
}
