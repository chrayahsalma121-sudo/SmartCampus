import { Link } from "react-router-dom";
import logo from "../assets/logo.png";
import { useAuth } from "../hooks/useAuth.js";

export default function Home() {
  const { isAuthenticated, dashboardPath } = useAuth();
  const actionPath = isAuthenticated ? dashboardPath : "/login";
  const actionLabel = isAuthenticated ? "Entrer dans mon espace" : "Se connecter";

  return (
    <main className="home-page">
      <nav className="home-nav" aria-label="Navigation principale">
        <Link className="home-brand" to="/">
          <img src={logo} alt="CampusServices" />
          <span>
            <strong>CampusServices</strong>
            <small>Smart Campus</small>
          </span>
        </Link>

        <div className="home-actions">
          <a href="#services">Services</a>
          <Link className="btn btn-primary btn-md" to={actionPath}>
            {actionLabel}
          </Link>
        </div>
      </nav>

      <section className="home-hero" aria-labelledby="home-title">
        <div className="home-hero-copy">
          <p className="eyebrow">Plateforme universitaire</p>
          <h1 id="home-title">Tous les services du campus dans un espace simple.</h1>
          <p>
            Consultez la bibliotheque, reservez une salle et suivez vos demandes administratives depuis une interface unique.
          </p>

          <div className="home-hero-actions">
            <Link className="btn btn-primary btn-md" to={actionPath}>
              {actionLabel}
            </Link>
            <a className="btn btn-outline btn-md" href="#services">
              Voir les services
            </a>
          </div>
        </div>

        <div className="home-orb-card" aria-label="Apercu CampusServices">
          <div className="orb-logo">
            <img src={logo} alt="" />
          </div>
          <strong>CampusServices</strong>
          <span>Bibliotheque</span>
          <span>Salles</span>
          <span>Demandes</span>
        </div>
      </section>

      <section id="services" className="home-feature-grid" aria-label="Services CampusServices">
        <article className="home-feature-card">
          <span>01</span>
          <h2>Bibliotheque</h2>
          <p>Parcourez le catalogue, empruntez un livre et consultez vos emprunts actifs.</p>
        </article>

        <article className="home-feature-card">
          <span>02</span>
          <h2>Reservations</h2>
          <p>Trouvez une salle disponible et reservez-la pour vos travaux ou reunions.</p>
        </article>

        <article className="home-feature-card">
          <span>03</span>
          <h2>Administration</h2>
          <p>Envoyez vos demandes de documents et suivez leur traitement en temps reel.</p>
        </article>
      </section>
    </main>
  );
}
