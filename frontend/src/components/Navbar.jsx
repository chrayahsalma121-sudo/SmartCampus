import { useLocation, useNavigate } from "react-router-dom";
import Button from "./Button.jsx";
import { useAuth } from "../hooks/useAuth.js";

const pageTitles = {
  "/student/dashboard": "Dashboard etudiant",
  "/student/library": "Bibliotheque",
  "/student/my-loans": "Mes emprunts",
  "/student/rooms": "Reservation de salles",
  "/student/my-reservations": "Mes reservations",
  "/student/requests/new": "Nouvelle demande",
  "/student/requests": "Mes demandes",
  "/admin/dashboard": "Dashboard administrateur",
  "/admin/requests": "Gestion des demandes",
  "/admin/rooms": "Gestion des salles",
  "/admin/reservations": "Reservations",
  "/librarian/dashboard": "Dashboard bibliothecaire",
  "/librarian/books": "Catalogue",
  "/librarian/loans": "Emprunts",
};

export default function Navbar() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <header className="topbar">
      <div>
        <p className="topbar-kicker">Smart Campus</p>
        <h1>{pageTitles[location.pathname] || "CampusServices"}</h1>
      </div>
      <div className="topbar-user">
        <span className="user-pill">
          <strong>{user?.fullName || "Utilisateur"}</strong>
          <small>{user?.role}</small>
        </span>
        <Button variant="outline" size="sm" onClick={handleLogout}>
          Deconnexion
        </Button>
      </div>
    </header>
  );
}
