import { NavLink } from "react-router-dom";
import logo from "../assets/logo.png";
import { ADMIN_NAV, LIBRARIAN_NAV, ROLES, STUDENT_NAV } from "../utils/constants.js";
import { useAuth } from "../hooks/useAuth.js";

function getNav(role) {
  if (role === ROLES.admin) return ADMIN_NAV;
  if (role === ROLES.librarian) return LIBRARIAN_NAV;
  return STUDENT_NAV;
}

export default function Sidebar() {
  const { user } = useAuth();
  const navItems = getNav(user?.role);

  return (
    <aside className="sidebar">
      <NavLink to="/" className="brand" aria-label="CampusServices accueil">
        <img src={logo} alt="CampusServices" />
        <span>
          <strong>Campus</strong>
          <small>Services</small>
        </span>
      </NavLink>

      <nav className="sidebar-nav" aria-label="Navigation principale">
        {navItems.map((item) => (
          <NavLink key={item.to} to={item.to} className={({ isActive }) => (isActive ? "active" : "") }>
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
