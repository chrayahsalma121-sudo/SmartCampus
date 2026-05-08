import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import logo from "../assets/logo.png";
import Alert from "../components/Alert.jsx";
import Button from "../components/Button.jsx";
import { useAuth } from "../hooks/useAuth.js";
import { getDashboardPath } from "../utils/constants.js";
import { getErrorMessage } from "../utils/errorUtils.js";

export default function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isAuthenticated, user, sessionError } = useAuth();
  const [email, setEmail] = useState("student@example.com");
  const [password, setPassword] = useState("1234");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isAuthenticated && user) {
      navigate(getDashboardPath(user.role), { replace: true });
    }
  }, [isAuthenticated, navigate, user]);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      const session = await login(email.trim(), password);
      const target = location.state?.from?.pathname || getDashboardPath(session.user.role);
      navigate(target, { replace: true });
    } catch (err) {
      setError(getErrorMessage(err, "Identifiants incorrects."));
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-page">
      <section className="login-hero" aria-label="Presentation CampusServices">
        <div className="login-brand-card">
          <div className="hero-brand-row">
            <img src={logo} alt="CampusServices" />
            <span>
              <strong>CampusServices</strong>
              <small>Smart Campus</small>
            </span>
          </div>

          <p className="hero-lead">Un seul acces pour gerer les services essentiels du campus.</p>

          <div className="service-stack" aria-label="Services disponibles">
            <article className="service-preview-card service-preview-card-primary">
              <span>01</span>
              <strong>Bibliotheque</strong>
              <small>Catalogue, emprunts et retours</small>
            </article>
            <article className="service-preview-card">
              <span>02</span>
              <strong>Salles</strong>
              <small>Disponibilites et reservations</small>
            </article>
            <article className="service-preview-card">
              <span>03</span>
              <strong>Demandes</strong>
              <small>Documents administratifs</small>
            </article>
          </div>

        </div>
        <div className="hero-orbit" aria-hidden="true" />
      </section>

      <section className="login-panel">
        <div className="login-copy">
          <p className="eyebrow">Smart Campus</p>
          <h1>Connectez-vous a CampusServices</h1>
          <p>Chaque role accede automatiquement a son espace dedie apres connexion.</p>
        </div>

        <Alert type="error">{error || sessionError}</Alert>

        <form className="form-card" onSubmit={handleSubmit}>
          <label>
            Email
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="student@example.com"
              required
            />
          </label>

          <label>
            Mot de passe
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="1234"
              required
            />
          </label>

          <Button type="submit" loading={loading}>
            Se connecter
          </Button>
        </form>

        <div className="seed-box">
          <strong>Comptes de test</strong>
          <span>student@example.com / 1234</span>
          <span>admin@example.com / 1234</span>
          <span>librarian@example.com / 1234</span>
        </div>

        <Link className="subtle-link" to="/">
          Retour a l'accueil
        </Link>
      </section>
    </main>
  );
}
