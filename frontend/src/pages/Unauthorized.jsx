import { Link } from "react-router-dom";
import { useAuth } from "../hooks/useAuth.js";

export default function Unauthorized() {
  const { dashboardPath } = useAuth();

  return (
    <main className="center-page">
      <section className="state-card">
        <p className="eyebrow">Acces refuse</p>
        <h1>Vous n'avez pas acces a cette page.</h1>
        <p>Votre role ne permet pas de consulter cette ressource.</p>
        <Link className="btn btn-primary btn-md" to={dashboardPath}>
          Retour au dashboard
        </Link>
      </section>
    </main>
  );
}
