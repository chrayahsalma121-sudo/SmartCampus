import { useEffect, useState } from "react";
import Alert from "../components/Alert.jsx";
import Button from "../components/Button.jsx";
import Modal from "../components/Modal.jsx";
import StatusBadge from "../components/StatusBadge.jsx";
import Table from "../components/Table.jsx";
import { addBook, deleteBook, getBooks, updateBook } from "../services/bookService.js";
import { getErrorMessage } from "../utils/errorUtils.js";
import { formatDate } from "../utils/formatters.js";

const emptyForm = { title: "", author: "" };

export default function ManageBooks() {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [modalMode, setModalMode] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [selectedBook, setSelectedBook] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadBooks() {
    setLoading(true);
    setError("");
    try {
      setBooks(await getBooks());
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de charger le catalogue."));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadBooks();
  }, []);

  function openAdd() {
    setModalMode("add");
    setSelectedBook(null);
    setForm(emptyForm);
  }

  function openEdit(book) {
    setModalMode("edit");
    setSelectedBook(book);
    setForm({ title: book.title || "", author: book.author || "" });
  }

  async function submitBook() {
    setError("");
    setSuccess("");

    if (!form.title.trim() || !form.author.trim()) {
      setError("Titre et auteur sont obligatoires.");
      return;
    }

    setActionLoading(true);
    try {
      const payload = { title: form.title.trim(), author: form.author.trim() };
      const response = modalMode === "edit"
        ? await updateBook({ bookId: selectedBook.bookId, ...payload })
        : await addBook(payload);
      setSuccess(response.message || "Catalogue mis a jour.");
      setModalMode(null);
      setSelectedBook(null);
      await loadBooks();
    } catch (err) {
      setError(getErrorMessage(err, "Impossible d'enregistrer le livre."));
    } finally {
      setActionLoading(false);
    }
  }

  async function confirmDelete() {
    if (!deleteTarget) return;
    setActionLoading(true);
    setError("");
    setSuccess("");
    try {
      const response = await deleteBook(deleteTarget.bookId);
      setSuccess(response.message || "Livre supprime.");
      setDeleteTarget(null);
      await loadBooks();
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de supprimer le livre."));
    } finally {
      setActionLoading(false);
    }
  }

  const columns = [
    { key: "title", header: "Titre" },
    { key: "author", header: "Auteur" },
    { key: "status", header: "Statut", render: (row) => <StatusBadge status={row.status} /> },
    { key: "returnDate", header: "Retour prevu", render: (row) => formatDate(row.returnDate) },
    {
      key: "actions",
      header: "Actions",
      render: (row) => (
        <div className="row-actions">
          <Button size="sm" variant="outline" onClick={() => openEdit(row)}>
            Modifier
          </Button>
          <Button size="sm" variant="danger" onClick={() => setDeleteTarget(row)}>
            Supprimer
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="page-stack">
      <section className="page-heading with-action">
        <div>
          <p className="eyebrow">Bibliotheque</p>
          <h2>Gestion du catalogue</h2>
          <p>Ajoutez, modifiez ou supprimez des livres.</p>
        </div>
        <Button onClick={openAdd}>Ajouter un livre</Button>
      </section>

      <Alert type="info">
        Le backend ne propose pas encore d'action pour marquer manuellement un livre disponible ou emprunte.
      </Alert>
      <Alert type="error">{error}</Alert>
      <Alert type="success">{success}</Alert>

      <Table columns={columns} data={books} rowKey="bookId" loading={loading} emptyMessage="Aucun livre trouve." />

      <Modal
        open={Boolean(modalMode)}
        title={modalMode === "edit" ? "Modifier le livre" : "Ajouter un livre"}
        confirmLabel={modalMode === "edit" ? "Modifier" : "Ajouter"}
        onClose={() => setModalMode(null)}
        onConfirm={submitBook}
        loading={actionLoading}
      >
        <div className="form-grid one-column">
          <label>
            Titre
            <input value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} />
          </label>
          <label>
            Auteur
            <input value={form.author} onChange={(event) => setForm({ ...form, author: event.target.value })} />
          </label>
        </div>
      </Modal>

      <Modal
        open={Boolean(deleteTarget)}
        title="Supprimer le livre"
        confirmLabel="Supprimer"
        danger
        onClose={() => setDeleteTarget(null)}
        onConfirm={confirmDelete}
        loading={actionLoading}
      >
        <p>Confirmez la suppression de "{deleteTarget?.title}".</p>
      </Modal>
    </div>
  );
}
