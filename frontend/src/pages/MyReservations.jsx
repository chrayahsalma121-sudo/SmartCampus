import { useEffect, useState } from "react";
import Alert from "../components/Alert.jsx";
import Button from "../components/Button.jsx";
import Modal from "../components/Modal.jsx";
import StatusBadge from "../components/StatusBadge.jsx";
import Table from "../components/Table.jsx";
import { cancelReservation, getMyReservations, getRooms } from "../services/roomService.js";
import { RESERVATION_STATUS } from "../utils/constants.js";
import { getErrorMessage } from "../utils/errorUtils.js";
import { formatDate, formatTime } from "../utils/formatters.js";

export default function MyReservations() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [selectedReservation, setSelectedReservation] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadReservations() {
    setLoading(true);
    setError("");
    try {
      const [reservationResult, roomsResult] = await Promise.allSettled([getMyReservations(), getRooms()]);
      if (reservationResult.status === "rejected") throw reservationResult.reason;

      const rooms = roomsResult.status === "fulfilled" ? roomsResult.value : [];
      const roomById = new Map(rooms.map((room) => [room.roomId, room]));
      setReservations(
        reservationResult.value.map((reservation) => ({
          ...reservation,
          roomName: reservation.roomName || roomById.get(reservation.roomId)?.name || `Salle #${reservation.roomId}`,
        })),
      );
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de charger vos reservations."));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadReservations();
  }, []);

  async function confirmCancel() {
    if (!selectedReservation) return;
    setActionLoading(true);
    setError("");
    setSuccess("");
    try {
      const response = await cancelReservation(selectedReservation.reservationId);
      setSuccess(response.message || "Reservation annulee avec succes.");
      setSelectedReservation(null);
      await loadReservations();
    } catch (err) {
      setError(getErrorMessage(err, "Impossible d'annuler cette reservation."));
    } finally {
      setActionLoading(false);
    }
  }

  const columns = [
    { key: "roomName", header: "Salle" },
    { key: "reservationDate", header: "Date", render: (row) => formatDate(row.reservationDate) },
    { key: "startTime", header: "Debut", render: (row) => formatTime(row.startTime) },
    { key: "endTime", header: "Fin", render: (row) => formatTime(row.endTime) },
    { key: "status", header: "Statut", render: (row) => <StatusBadge status={row.status} /> },
    {
      key: "actions",
      header: "Action",
      render: (row) => (
        <Button size="sm" variant="danger" disabled={row.status !== RESERVATION_STATUS.confirmed} onClick={() => setSelectedReservation(row)}>
          Annuler
        </Button>
      ),
    },
  ];

  return (
    <div className="page-stack">
      <section className="page-heading">
        <p className="eyebrow">Salles</p>
        <h2>Mes reservations</h2>
        <p>Suivez vos reservations confirmees ou annulees.</p>
      </section>

      <Alert type="error">{error}</Alert>
      <Alert type="success">{success}</Alert>

      <Table columns={columns} data={reservations} rowKey="reservationId" loading={loading} emptyMessage="Aucune reservation trouvee." />

      <Modal
        open={Boolean(selectedReservation)}
        title="Annuler la reservation"
        confirmLabel="Annuler la reservation"
        danger
        onClose={() => setSelectedReservation(null)}
        onConfirm={confirmCancel}
        loading={actionLoading}
      >
        <p>Confirmez l'annulation de {selectedReservation?.roomName}.</p>
      </Modal>
    </div>
  );
}
