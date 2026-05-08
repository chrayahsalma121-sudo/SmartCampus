import { useEffect, useState } from "react";
import Alert from "../components/Alert.jsx";
import Card from "../components/Card.jsx";
import LoadingState from "../components/LoadingState.jsx";
import { useAuth } from "../hooks/useAuth.js";
import { getMyBorrowings } from "../services/bookService.js";
import { getMyRequests } from "../services/requestService.js";
import { getMyReservations } from "../services/roomService.js";
import { REQUEST_STATUS, RESERVATION_STATUS } from "../utils/constants.js";
import { getErrorMessage } from "../utils/errorUtils.js";
import { countBy } from "../utils/formatters.js";

export default function StudentDashboard() {
  const { user, isStudentValid } = useAuth();
  const [stats, setStats] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false;

    async function loadDashboard() {
      setError("");
      try {
        const [borrowings, reservations, requests] = await Promise.all([
          getMyBorrowings(),
          getMyReservations(),
          getMyRequests(),
        ]);

        if (!ignore) {
          setStats({ borrowings, reservations, requests });
        }
      } catch (err) {
        if (!ignore) setError(getErrorMessage(err, "Impossible de charger le dashboard."));
      }
    }

    loadDashboard();
    return () => {
      ignore = true;
    };
  }, []);

  if (!stats && !error) return <LoadingState label="Chargement du dashboard" />;

  const activeBorrowings = stats ? countBy(stats.borrowings, (item) => !item.returned) : 0;
  const activeReservations = stats
    ? countBy(stats.reservations, (item) => item.status === RESERVATION_STATUS.confirmed)
    : 0;
  const pendingRequests = stats
    ? countBy(stats.requests, (item) => item.status === REQUEST_STATUS.pending)
    : 0;

  return (
    <div className="page-stack">
      <section className="hero-card">
        <div>
          <p className="eyebrow">Espace etudiant</p>
          <h2>Bonjour {user?.fullName}</h2>
          <p>Accedez aux services du campus depuis une seule interface.</p>
        </div>
        <span className={isStudentValid ? "valid-pill" : "valid-pill invalid"}>
          {isStudentValid ? "Compte valide" : "Compte non valide"}
        </span>
      </section>

      {!isStudentValid ? (
        <Alert type="warning" title="Acces limite">
          Votre compte etudiant n'est pas valide pour utiliser les services actifs.
        </Alert>
      ) : null}
      <Alert type="error">{error}</Alert>

      <div className="stats-grid">
        <Card title="Livres actifs" value={activeBorrowings} eyebrow="Bibliotheque" tone="mint" />
        <Card title="Reservations" value={activeReservations} eyebrow="Salles" tone="blue" />
        <Card title="Demandes en attente" value={pendingRequests} eyebrow="Administration" tone="orange" />
        <Card title="Statut" value={isStudentValid ? "Valide" : "Non valide"} eyebrow={user?.filiere} />
      </div>

      <div className="quick-grid">
        <Card title="Bibliotheque" to="/student/library" actionLabel="Consulter" />
        <Card title="Mes emprunts" to="/student/my-loans" actionLabel="Voir mes livres" />
        <Card title="Reservation de salles" to="/student/rooms" actionLabel="Reserver" />
        <Card title="Demandes administratives" to="/student/requests/new" actionLabel="Creer une demande" />
      </div>
    </div>
  );
}
