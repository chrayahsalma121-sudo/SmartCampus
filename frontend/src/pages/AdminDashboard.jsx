import { useEffect, useState } from "react";
import Alert from "../components/Alert.jsx";
import Card from "../components/Card.jsx";
import LoadingState from "../components/LoadingState.jsx";
import { getAllRequests } from "../services/requestService.js";
import { getAllReservations, getRooms } from "../services/roomService.js";
import { REQUEST_STATUS, RESERVATION_STATUS } from "../utils/constants.js";
import { getErrorMessage } from "../utils/errorUtils.js";
import { countBy } from "../utils/formatters.js";

export default function AdminDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false;

    async function loadDashboard() {
      setError("");
      try {
        const [requests, rooms, reservations] = await Promise.all([
          getAllRequests(),
          getRooms(),
          getAllReservations(),
        ]);
        if (!ignore) setData({ requests, rooms, reservations });
      } catch (err) {
        if (!ignore) setError(getErrorMessage(err, "Impossible de charger le dashboard admin."));
      }
    }

    loadDashboard();
    return () => {
      ignore = true;
    };
  }, []);

  if (!data && !error) return <LoadingState label="Chargement du dashboard admin" />;

  const pending = data ? countBy(data.requests, (item) => item.status === REQUEST_STATUS.pending) : 0;
  const approved = data ? countBy(data.requests, (item) => item.status === REQUEST_STATUS.approved) : 0;
  const rejected = data ? countBy(data.requests, (item) => item.status === REQUEST_STATUS.rejected) : 0;
  const availableRooms = data ? countBy(data.rooms, (item) => item.available) : 0;
  const activeReservations = data
    ? countBy(data.reservations, (item) => item.status === RESERVATION_STATUS.confirmed)
    : 0;

  return (
    <div className="page-stack">
      <section className="hero-card admin-hero">
        <div>
          <p className="eyebrow">Administration</p>
          <h2>Pilotage des services campus</h2>
          <p>Suivez les demandes et la disponibilite des espaces de travail.</p>
        </div>
      </section>

      <Alert type="error">{error}</Alert>

      <div className="stats-grid">
        <Card title="Demandes en attente" value={pending} tone="orange" />
        <Card title="Demandes validees" value={approved} tone="mint" />
        <Card title="Demandes refusees" value={rejected} tone="danger" />
        <Card title="Salles disponibles" value={availableRooms} tone="blue" />
        <Card title="Reservations actives" value={activeReservations} tone="default" />
      </div>

      <div className="quick-grid">
        <Card title="Gestion des demandes" to="/admin/requests" actionLabel="Traiter" />
        <Card title="Gestion des salles" to="/admin/rooms" actionLabel="Administrer" />
        <Card title="Reservations" to="/admin/reservations" actionLabel="Voir la limite backend" />
      </div>
    </div>
  );
}
