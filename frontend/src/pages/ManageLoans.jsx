import { useEffect, useState } from "react";
import Alert from "../components/Alert.jsx";
import Button from "../components/Button.jsx";
import Modal from "../components/Modal.jsx";
import StatusBadge from "../components/StatusBadge.jsx";
import Table from "../components/Table.jsx";
import { getAllBorrowings, returnBorrowingAsLibrarian } from "../services/bookService.js";
import { getErrorMessage } from "../utils/errorUtils.js";
import { formatDate } from "../utils/formatters.js";

export default function ManageLoans() {
  const [borrowings, setBorrowings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [selectedBorrowing, setSelectedBorrowing] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadBorrowings() {
    setError("");
    try {
      setBorrowings(await getAllBorrowings());
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de charger les emprunts."));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    let ignore = false;

    async function run() {
      setError("");
      try {
        const data = await getAllBorrowings();
        if (!ignore) setBorrowings(data);
      } catch (err) {
        if (!ignore) setError(getErrorMessage(err, "Impossible de charger les emprunts."));
      } finally {
        if (!ignore) setLoading(false);
      }
    }

    run();
    return () => {
      ignore = true;
    };
  }, []);

  async function confirmReturn() {
    if (!selectedBorrowing) return;
    setActionLoading(true);
    setError("");
    setSuccess("");

    try {
      const response = await returnBorrowingAsLibrarian(selectedBorrowing.borrowingId);
      setSuccess(response.message || "Retour enregistre.");
      setSelectedBorrowing(null);
      setLoading(true);
      await loadBorrowings();
    } catch (err) {
      setError(getErrorMessage(err, "Impossible d'enregistrer le retour."));
    } finally {
      setActionLoading(false);
    }
  }

  const columns = [
    { key: "borrowingId", header: "ID" },
    { key: "studentName", header: "Etudiant" },
    { key: "title", header: "Livre" },
    { key: "author", header: "Auteur" },
    { key: "borrowDate", header: "Emprunt", render: (row) => formatDate(row.borrowDate) },
    { key: "returnDate", header: "Retour prevu", render: (row) => formatDate(row.returnDate) },
    { key: "status", header: "Statut", render: (row) => <StatusBadge status={row.returned ? "CANCELLED" : "CONFIRMED"} /> },
    {
      key: "actions",
      header: "Action",
      render: (row) => (
        <Button size="sm" disabled={row.returned} onClick={() => setSelectedBorrowing(row)}>
          Enregistrer retour
        </Button>
      ),
    },
  ];

  return (
    <div className="page-stack">
      <section className="page-heading">
        <p className="eyebrow">Bibliotheque</p>
        <h2>Gestion des emprunts</h2>
        <p>Consultez les emprunts et enregistrez les retours.</p>
      </section>

      <Alert type="error">{error}</Alert>
      <Alert type="success">{success}</Alert>

      <Table columns={columns} data={borrowings} rowKey="borrowingId" loading={loading} emptyMessage="Aucun emprunt trouve." />

      <Modal
        open={Boolean(selectedBorrowing)}
        title="Enregistrer le retour"
        confirmLabel="Enregistrer"
        onClose={() => setSelectedBorrowing(null)}
        onConfirm={confirmReturn}
        loading={actionLoading}
      >
        <p>Confirmez le retour de "{selectedBorrowing?.title}".</p>
      </Modal>
    </div>
  );
}
