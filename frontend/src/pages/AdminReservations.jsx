import { useEffect, useState } from "react";
import Alert from "../components/Alert.jsx";
import StatusBadge from "../components/StatusBadge.jsx";
import Table from "../components/Table.jsx";
import { getAllReservations } from "../services/roomService.js";
import { getErrorMessage } from "../utils/errorUtils.js";
import { formatDate, formatTime } from "../utils/formatters.js";

export default function AdminReservations() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false;

    async function loadReservations() {
      try {
        const data = await getAllReservations();
        if (!ignore) setReservations(data);
      } catch (err) {
        if (!ignore) setError(getErrorMessage(err, "Impossible de charger les reservations."));
      } finally {
        if (!ignore) setLoading(false);
      }
    }

    loadReservations();
    return () => {
      ignore = true;
    };
  }, []);

  const columns = [
    { key: "reservationId", header: "ID" },
    { key: "student", header: "Etudiant", render: (row) => row.studentName || `Etudiant #${row.studentId}` },
    { key: "room", header: "Salle", render: (row) => row.roomName || `Salle #${row.roomId}` },
    { key: "reservationDate", header: "Date", render: (row) => formatDate(row.reservationDate) },
    { key: "startTime", header: "Debut", render: (row) => formatTime(row.startTime) },
    { key: "endTime", header: "Fin", render: (row) => formatTime(row.endTime) },
    { key: "status", header: "Statut", render: (row) => <StatusBadge status={row.status} /> },
  ];

  return (
    <div className="page-stack">
      <section className="page-heading">
        <p className="eyebrow">Administration</p>
        <h2>Reservations</h2>
        <p>Consultez toutes les reservations exposees par le backend.</p>
      </section>

      <Alert type="error">{error}</Alert>
      <Table columns={columns} data={reservations} rowKey="reservationId" loading={loading} emptyMessage="Aucune reservation trouvee." />
    </div>
  );
}
