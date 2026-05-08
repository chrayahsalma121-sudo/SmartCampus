import { useEffect, useState } from "react";
import Alert from "../components/Alert.jsx";
import Button from "../components/Button.jsx";
import StatusBadge from "../components/StatusBadge.jsx";
import Table from "../components/Table.jsx";
import { addRoom, getRooms, updateRoomAvailability } from "../services/roomService.js";
import { getErrorMessage } from "../utils/errorUtils.js";

export default function ManageRooms() {
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [name, setName] = useState("");
  const [capacity, setCapacity] = useState(20);
  const [available, setAvailable] = useState(true);
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

  async function handleAddRoom(event) {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (!name.trim()) {
      setError("Le nom de la salle est obligatoire.");
      return;
    }

    setActionLoading(true);
    try {
      const response = await addRoom({ name: name.trim(), capacity: Number(capacity), available });
      setSuccess(response.message || "Salle ajoutee.");
      setName("");
      setCapacity(20);
      setAvailable(true);
      await loadRooms();
    } catch (err) {
      setError(getErrorMessage(err, "Impossible d'ajouter la salle."));
    } finally {
      setActionLoading(false);
    }
  }

  async function toggleAvailability(room) {
    setError("");
    setSuccess("");
    try {
      const response = await updateRoomAvailability(room.roomId, !room.available);
      setSuccess(response.message || "Disponibilite mise a jour.");
      await loadRooms();
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de modifier la disponibilite."));
    }
  }

  const columns = [
    { key: "name", header: "Salle" },
    { key: "capacity", header: "Capacite" },
    { key: "available", header: "Statut", render: (row) => <StatusBadge status={row.available ? "AVAILABLE" : "BORROWED"} /> },
    {
      key: "actions",
      header: "Action",
      render: (row) => (
        <Button size="sm" variant="outline" onClick={() => toggleAvailability(row)}>
          {row.available ? "Rendre indisponible" : "Rendre disponible"}
        </Button>
      ),
    },
  ];

  return (
    <div className="page-stack">
      <section className="page-heading">
        <p className="eyebrow">Administration</p>
        <h2>Gestion des salles</h2>
        <p>Ajoutez des salles et gerez leur disponibilite.</p>
      </section>

      <Alert type="info">
        Le backend expose l'ajout et la disponibilite. La modification complete, suppression et gestion des creneaux ne sont pas encore disponibles.
      </Alert>
      <Alert type="error">{error}</Alert>
      <Alert type="success">{success}</Alert>

      <form className="form-card inline-form" onSubmit={handleAddRoom}>
        <label>
          Nom de la salle
          <input value={name} onChange={(event) => setName(event.target.value)} placeholder="Salle C" required />
        </label>
        <label>
          Capacite
          <input type="number" min="1" value={capacity} onChange={(event) => setCapacity(event.target.value)} required />
        </label>
        <label className="checkbox-line">
          <input type="checkbox" checked={available} onChange={(event) => setAvailable(event.target.checked)} />
          Disponible
        </label>
        <Button type="submit" loading={actionLoading}>
          Ajouter
        </Button>
      </form>

      <Table columns={columns} data={rooms} rowKey="roomId" loading={loading} emptyMessage="Aucune salle trouvee." />
    </div>
  );
}
