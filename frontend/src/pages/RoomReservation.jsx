import { useEffect, useState } from "react";
import Alert from "../components/Alert.jsx";
import Button from "../components/Button.jsx";
import Modal from "../components/Modal.jsx";
import StatusBadge from "../components/StatusBadge.jsx";
import Table from "../components/Table.jsx";
import { useAuth } from "../hooks/useAuth.js";
import { getRooms, reserveRoom } from "../services/roomService.js";
import { getErrorMessage } from "../utils/errorUtils.js";
import { formatBoolean } from "../utils/formatters.js";

function today() {
  return new Date().toISOString().slice(0, 10);
}

export default function RoomReservation() {
  const { isStudentValid } = useAuth();
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [reservationDate, setReservationDate] = useState(today());
  const [startTime, setStartTime] = useState("10:00");
  const [endTime, setEndTime] = useState("12:00");
  const [minCapacity, setMinCapacity] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadRooms() {
    setLoading(true);
    setError("");
    try {
      setRooms(await getRooms());
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de charger les salles."));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadRooms();
  }, []);

  function validateSlot() {
    if (!reservationDate || !startTime || !endTime) return "Date et heures obligatoires.";
    if (endTime <= startTime) return "L'heure de fin doit etre apres l'heure de debut.";
    if (!isStudentValid) return "Votre compte etudiant n'est pas valide.";
    return "";
  }

  async function confirmReservation() {
    const validation = validateSlot();
    if (validation) {
      setError(validation);
      return;
    }

    setActionLoading(true);
    setError("");
    setSuccess("");
    try {
      const response = await reserveRoom({
        roomId: selectedRoom.roomId,
        reservationDate,
        startTime,
        endTime,
      });
      setSuccess(response.message || "Salle reservee avec succes.");
      setSelectedRoom(null);
      await loadRooms();
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de reserver cette salle."));
    } finally {
      setActionLoading(false);
    }
  }

  const filteredRooms = rooms.filter((room) => {
    if (!minCapacity) return true;
    return room.capacity >= Number(minCapacity);
  });

  const columns = [
    { key: "name", header: "Salle" },
    { key: "capacity", header: "Capacite" },
    { key: "available", header: "Disponibilite", render: (room) => <StatusBadge status={room.available ? "AVAILABLE" : "BORROWED"} /> },
    { key: "label", header: "Ouverte", render: (room) => formatBoolean(room.available) },
    {
      key: "actions",
      header: "Action",
      render: (room) => (
        <Button size="sm" disabled={!isStudentValid || !room.available} onClick={() => setSelectedRoom(room)}>
          Reserver
        </Button>
      ),
    },
  ];

  return (
    <div className="page-stack">
      <section className="page-heading">
        <p className="eyebrow">Salles de travail</p>
        <h2>Reservation de salles</h2>
        <p>Choisissez une salle disponible et un creneau libre.</p>
      </section>

      {!isStudentValid ? <Alert type="warning">Votre compte non valide bloque les reservations.</Alert> : null}
      <Alert type="error">{error}</Alert>
      <Alert type="success">{success}</Alert>

      <div className="form-grid compact-grid">
        <label>
          Date
          <input type="date" value={reservationDate} onChange={(event) => setReservationDate(event.target.value)} />
        </label>
        <label>
          Debut
          <input type="time" value={startTime} onChange={(event) => setStartTime(event.target.value)} />
        </label>
        <label>
          Fin
          <input type="time" value={endTime} onChange={(event) => setEndTime(event.target.value)} />
        </label>
        <label>
          Capacite min.
          <input
            type="number"
            min="1"
            value={minCapacity}
            onChange={(event) => setMinCapacity(event.target.value)}
            placeholder="Ex: 20"
          />
        </label>
      </div>

      <Table columns={columns} data={filteredRooms} rowKey="roomId" loading={loading} emptyMessage="Aucune salle trouvee." />

      <Modal
        open={Boolean(selectedRoom)}
        title="Confirmer la reservation"
        confirmLabel="Reserver"
        onClose={() => setSelectedRoom(null)}
        onConfirm={confirmReservation}
        loading={actionLoading}
      >
        <p>
          Reserver {selectedRoom?.name} le {reservationDate} de {startTime} a {endTime} ?
        </p>
      </Modal>
    </div>
  );
}
