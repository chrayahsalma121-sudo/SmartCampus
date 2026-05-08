import { api } from "./api.js";

export async function getRooms() {
  const response = await api.get("/rooms");
  return response.data || [];
}

export async function reserveRoom(payload) {
  return api.post("/rooms/reserve", payload);
}

export async function cancelReservation(reservationId) {
  return api.post("/rooms/cancel-reservation", { reservationId });
}

export async function getMyReservations() {
  const response = await api.get("/rooms/my-reservations");
  return response.data || [];
}

export async function getAllReservations() {
  const response = await api.get("/admin/rooms/reservations");
  return response.data || [];
}

export async function addRoom(payload) {
  return api.post("/admin/rooms", payload);
}

export async function updateRoomAvailability(roomId, available) {
  return api.post("/admin/rooms/update-availability", { roomId, available });
}
