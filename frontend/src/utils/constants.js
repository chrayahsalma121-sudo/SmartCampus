export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

export const STORAGE_KEYS = {
  token: "campusServices.accessToken",
  user: "campusServices.user",
};

export const ROLES = {
  student: "STUDENT",
  admin: "ADMIN",
  librarian: "LIBRARIAN",
};

export const BOOK_STATUS = {
  available: "AVAILABLE",
  borrowed: "BORROWED",
};

export const RESERVATION_STATUS = {
  confirmed: "CONFIRMED",
  cancelled: "CANCELLED",
};

export const REQUEST_STATUS = {
  pending: "PENDING",
  approved: "APPROVED",
  rejected: "REJECTED",
};

export const REQUEST_TYPES = [
  { value: "SCHOOL_CERTIFICATE", label: "Attestation de scolarite" },
  { value: "ATTENDANCE_CERTIFICATE", label: "Certificat de presence" },
  { value: "TRANSCRIPT", label: "Releve de notes" },
  { value: "OTHER", label: "Autre document" },
];

export const DASHBOARD_PATHS = {
  STUDENT: "/student/dashboard",
  ADMIN: "/admin/dashboard",
  LIBRARIAN: "/librarian/dashboard",
};

export function getDashboardPath(role) {
  return DASHBOARD_PATHS[role] || "/login";
}

export const STUDENT_NAV = [
  { label: "Dashboard", to: "/student/dashboard" },
  { label: "Bibliotheque", to: "/student/library" },
  { label: "Mes emprunts", to: "/student/my-loans" },
  { label: "Salles", to: "/student/rooms" },
  { label: "Mes reservations", to: "/student/my-reservations" },
  { label: "Nouvelle demande", to: "/student/requests/new" },
  { label: "Mes demandes", to: "/student/requests" },
];

export const ADMIN_NAV = [
  { label: "Dashboard", to: "/admin/dashboard" },
  { label: "Demandes", to: "/admin/requests" },
  { label: "Salles", to: "/admin/rooms" },
  { label: "Reservations", to: "/admin/reservations" },
];

export const LIBRARIAN_NAV = [
  { label: "Dashboard", to: "/librarian/dashboard" },
  { label: "Catalogue", to: "/librarian/books" },
  { label: "Emprunts", to: "/librarian/loans" },
];
