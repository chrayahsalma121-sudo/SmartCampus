import { Link } from "react-router-dom";
export default function NotFound() {
  return (
    <main className="center-page">
      <section className="state-card">
        <p className="eyebrow">404</p>
        <h1>Page introuvable</h1>
        <p>La page demandee n'existe pas dans CampusServices.</p>
        <Link className="btn btn-primary btn-md" to="/">
          Retour
        </Link>
      </section>
    </main>
  );
}
