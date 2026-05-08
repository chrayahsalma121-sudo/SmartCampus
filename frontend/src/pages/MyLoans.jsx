import { useEffect, useState } from "react";
import Alert from "../components/Alert.jsx";
import Button from "../components/Button.jsx";
import Modal from "../components/Modal.jsx";
import StatusBadge from "../components/StatusBadge.jsx";
import Table from "../components/Table.jsx";
import { getBooks } from "../services/bookService.js";
import { getMyLoans, returnLoan } from "../services/loanService.js";
import { getErrorMessage } from "../utils/errorUtils.js";
import { formatDate } from "../utils/formatters.js";

export default function MyLoans() {
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [selectedLoan, setSelectedLoan] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadLoans() {
    setLoading(true);
    setError("");
    try {
      const [loansResult, booksResult] = await Promise.allSettled([getMyLoans(), getBooks()]);
      if (loansResult.status === "rejected") throw loansResult.reason;

      const books = booksResult.status === "fulfilled" ? booksResult.value : [];
      const bookById = new Map(books.map((book) => [book.bookId, book]));
      setLoans(
        loansResult.value.map((loan) => ({
          ...loan,
          author: bookById.get(loan.bookId)?.author || "Auteur non fourni",
        })),
      );
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de charger vos emprunts."));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadLoans();
  }, []);

  async function confirmReturn() {
    if (!selectedLoan) return;
    setActionLoading(true);
    setError("");
    setSuccess("");
    try {
      const response = await returnLoan(selectedLoan.borrowingId);
      setSuccess(response.message || "Livre retourne avec succes.");
      setSelectedLoan(null);
      await loadLoans();
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de retourner ce livre."));
    } finally {
      setActionLoading(false);
    }
  }

  const columns = [
    { key: "title", header: "Livre" },
    { key: "author", header: "Auteur" },
    { key: "borrowDate", header: "Date emprunt", render: (loan) => formatDate(loan.borrowDate) },
    { key: "returnDate", header: "Retour prevu", render: (loan) => formatDate(loan.returnDate) },
    { key: "status", header: "Statut", render: (loan) => <StatusBadge status={loan.returned ? "CANCELLED" : "CONFIRMED"} /> },
    {
      key: "actions",
      header: "Action",
      render: (loan) => (
        <Button size="sm" disabled={loan.returned} onClick={() => setSelectedLoan(loan)}>
          Retourner
        </Button>
      ),
    },
  ];

  return (
    <div className="page-stack">
      <section className="page-heading">
        <p className="eyebrow">Bibliotheque</p>
        <h2>Mes emprunts</h2>
        <p>Consultez les livres empruntes et enregistrez les retours.</p>
      </section>

      <Alert type="error">{error}</Alert>
      <Alert type="success">{success}</Alert>

      <Table columns={columns} data={loans} rowKey="borrowingId" loading={loading} emptyMessage="Aucun emprunt trouve." />

      <Modal
        open={Boolean(selectedLoan)}
        title="Retourner le livre"
        confirmLabel="Retourner"
        onClose={() => setSelectedLoan(null)}
        onConfirm={confirmReturn}
        loading={actionLoading}
      >
        <p>Confirmez le retour de "{selectedLoan?.title}".</p>
      </Modal>
    </div>
  );
}
