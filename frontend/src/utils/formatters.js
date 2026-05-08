import { REQUEST_TYPES } from "./constants.js";

const statusLabels = {
  AVAILABLE: "Disponible",
  BORROWED: "Emprunte",
  CONFIRMED: "Confirmee",
  CANCELLED: "Annulee",
  PENDING: "En attente",
  APPROVED: "Validee",
  REJECTED: "Refusee",
};

export function formatStatus(status) {
  return statusLabels[status] || status || "Non defini";
}

export function formatRequestType(type) {
  return REQUEST_TYPES.find((item) => item.value === type)?.label || type || "-";
}

export function formatBoolean(value, truthy = "Oui", falsy = "Non") {
  return value ? truthy : falsy;
}

export function formatDate(value) {
  if (!value) return "-";
  return String(value).slice(0, 10);
}

export function formatTime(value) {
  if (!value) return "-";
  return String(value).slice(0, 5);
}

export function countBy(list, predicate) {
  return list.reduce((total, item) => (predicate(item) ? total + 1 : total), 0);
}
