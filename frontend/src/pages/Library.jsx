import { useDeferredValue, useEffect, useState } from "react";
import Alert from "../components/Alert.jsx";
import Button from "../components/Button.jsx";
import Modal from "../components/Modal.jsx";
import StatusBadge from "../components/StatusBadge.jsx";
import Table from "../components/Table.jsx";
import { useAuth } from "../hooks/useAuth.js";
import { borrowBook, getBooks } from "../services/bookService.js";
import { BOOK_STATUS } from "../utils/constants.js";
import { getErrorMessage } from "../utils/errorUtils.js";
import { formatDate } from "../utils/formatters.js";

export default function Library() {
  const { isStudentValid } = useAuth();
  const [books, setBooks] = useState([]);
  const [search, setSearch] = useState("");
  const deferredSearch = useDeferredValue(search);
  const [status, setStatus] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [selectedBook, setSelectedBook] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadBooks() {
    setLoading(true);
    setError("");
    try {
      setBooks(await getBooks());
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de charger les livres."));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadBooks();
  }, []);

  async function confirmBorrow() {
    if (!selectedBook) return;
    setActionLoading(true);
    setError("");
    setSuccess("");

    try {
      const response = await borrowBook(selectedBook.bookId);
      setSuccess(response.message || "Livre emprunte avec succes.");
      setSelectedBook(null);
      await loadBooks();
    } catch (err) {
      setError(getErrorMessage(err, "Impossible d'emprunter ce livre."));
    } finally {
      setActionLoading(false);
    }
  }

  const normalizedSearch = deferredSearch.trim().toLowerCase();
  const filteredBooks = books.filter((book) => {
    const matchesSearch =
      !normalizedSearch ||
      book.title?.toLowerCase().includes(normalizedSearch) ||
      book.author?.toLowerCase().includes(normalizedSearch);
    const matchesStatus = status === "ALL" || book.status === status;
    return matchesSearch && matchesStatus;
  });

  const columns = [
    { key: "title", header: "Titre" },
    { key: "author", header: "Auteur" },
    { key: "status", header: "Statut", render: (book) => <StatusBadge status={book.status} /> },
    { key: "returnDate", header: "Retour prevu", render: (book) => formatDate(book.returnDate) },
    {
      key: "actions",
      header: "Action",
      render: (book) => (
        <Button
          size="sm"
          disabled={!isStudentValid || book.status !== BOOK_STATUS.available}
          onClick={() => setSelectedBook(book)}
        >
          Emprunter
        </Button>
      ),
    },
  ];

  return (
    <div className="page-stack">
      <section className="page-heading">
        <p className="eyebrow">Bibliotheque</p>
        <h2>Catalogue des livres</h2>
        <p>Recherchez un livre disponible et envoyez une demande d'emprunt au backend.</p>
      </section>

      {!isStudentValid ? (
        <Alert type="warning">Votre compte non valide bloque les emprunts.</Alert>
      ) : null}
      <Alert type="error">{error}</Alert>
      <Alert type="success">{success}</Alert>

      <div className="toolbar">
        <input
          type="search"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Rechercher par titre ou auteur"
          aria-label="Rechercher un livre"
        />
        <select value={status} onChange={(event) => setStatus(event.target.value)} aria-label="Filtrer par statut">
          <option value="ALL">Tous les statuts</option>
          <option value="AVAILABLE">Disponible</option>
          <option value="BORROWED">Emprunte</option>
        </select>
      </div>

      <Table columns={columns} data={filteredBooks} rowKey="bookId" loading={loading} emptyMessage="Aucun livre trouve." />

      <Modal
        open={Boolean(selectedBook)}
        title="Confirmer l'emprunt"
        confirmLabel="Emprunter"
        onClose={() => setSelectedBook(null)}
        onConfirm={confirmBorrow}
        loading={actionLoading}
      >
        <p>Voulez-vous emprunter le livre "{selectedBook?.title}" ?</p>
      </Modal>
    </div>
  );
}
