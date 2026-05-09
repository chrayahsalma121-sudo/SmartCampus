import { useEffect, useState } from "react";
import Alert from "../components/Alert.jsx";
import Button from "../components/Button.jsx";
import Modal from "../components/Modal.jsx";
import Table from "../components/Table.jsx";
import { useAuth } from "../hooks/useAuth.js";
import { createUser, deleteUser, getUsers } from "../services/userService.js";
import { ROLES } from "../utils/constants.js";
import { getErrorMessage } from "../utils/errorUtils.js";

const emptyForm = {
  fullName: "",
  email: "",
  password: "1234",
  role: ROLES.student,
  filiere: "",
};

const roleLabels = {
  STUDENT: "Etudiant",
  ADMIN: "Admin",
  LIBRARIAN: "Bibliothecaire",
};

export default function ManageUsers() {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadUsers() {
    setLoading(true);
    setError("");
    try {
      setUsers(await getUsers());
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de charger les utilisateurs."));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadUsers();
  }, []);

  function openCreateModal() {
    setForm(emptyForm);
    setError("");
    setSuccess("");
    setModalOpen(true);
  }

  function updateForm(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submitUser() {
    setError("");
    setSuccess("");

    if (!form.fullName.trim() || !form.email.trim() || !form.password.trim()) {
      setError("Nom complet, email et mot de passe sont obligatoires.");
      return;
    }

    if (form.role === ROLES.student && !form.filiere.trim()) {
      setError("La filiere est obligatoire pour un etudiant.");
      return;
    }

    setActionLoading(true);
    try {
      const payload = {
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        password: form.password,
        role: form.role,
      };

      if (form.role === ROLES.student) {
        payload.filiere = form.filiere.trim();
      }

      const response = await createUser(payload);
      setSuccess(response.message || "Utilisateur cree.");
      setModalOpen(false);
      setForm(emptyForm);
      await loadUsers();
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de creer l'utilisateur."));
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
      const response = await deleteUser(deleteTarget.userId);
      setSuccess(response.message || "Utilisateur supprime.");
      setDeleteTarget(null);
      await loadUsers();
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de supprimer l'utilisateur."));
    } finally {
      setActionLoading(false);
    }
  }

  const columns = [
    { key: "fullName", header: "Nom complet" },
    { key: "email", header: "Email" },
    { key: "role", header: "Role", render: (row) => roleLabels[row.role] || row.role },
    {
      key: "actions",
      header: "Actions",
      render: (row) => {
        const isCurrentUser = row.userId === user?.userId;
        return (
          <Button size="sm" variant="danger" disabled={isCurrentUser} onClick={() => setDeleteTarget(row)}>
            {isCurrentUser ? "Compte actuel" : "Supprimer"}
          </Button>
        );
      },
    },
  ];

  return (
    <div className="page-stack">
      <section className="page-heading with-action">
        <div>
          <p className="eyebrow">Administration</p>
          <h2>Gestion des utilisateurs</h2>
          <p>Creer des comptes et gerer les acces etudiants, admins et bibliothecaires.</p>
        </div>
        <Button onClick={openCreateModal}>Ajouter un utilisateur</Button>
      </section>

      <Alert type="info">
        La creation d'un compte etudiant demande une filiere. Le compte admin connecte ne peut pas etre supprime depuis cette interface.
      </Alert>
      <Alert type="error">{error}</Alert>
      <Alert type="success">{success}</Alert>

      <Table columns={columns} data={users} rowKey="userId" loading={loading} emptyMessage="Aucun utilisateur trouve." />

      <Modal
        open={modalOpen}
        title="Ajouter un utilisateur"
        confirmLabel="Creer le compte"
        onClose={() => setModalOpen(false)}
        onConfirm={submitUser}
        loading={actionLoading}
      >
        <div className="form-grid one-column">
          <label>
            Nom complet
            <input
              id="user-full-name"
              name="fullName"
              value={form.fullName}
              onChange={(event) => updateForm("fullName", event.target.value)}
              placeholder="Nom et prenom"
            />
          </label>
          <label>
            Email
            <input
              id="user-email"
              name="email"
              type="email"
              value={form.email}
              onChange={(event) => updateForm("email", event.target.value)}
              placeholder="user@example.com"
            />
          </label>
          <label>
            Mot de passe
            <input
              id="user-password"
              name="password"
              type="password"
              value={form.password}
              onChange={(event) => updateForm("password", event.target.value)}
              placeholder="1234"
            />
          </label>
          <label>
            Role
            <select
              id="user-role"
              name="role"
              value={form.role}
              onChange={(event) => updateForm("role", event.target.value)}
            >
              <option value={ROLES.student}>Etudiant</option>
              <option value={ROLES.admin}>Admin</option>
              <option value={ROLES.librarian}>Bibliothecaire</option>
            </select>
          </label>
          {form.role === ROLES.student ? (
            <label>
              Filiere
              <input
                id="user-filiere"
                name="filiere"
                value={form.filiere}
                onChange={(event) => updateForm("filiere", event.target.value)}
                placeholder="Big Data & Cloud Computing"
              />
            </label>
          ) : null}
        </div>
      </Modal>

      <Modal
        open={Boolean(deleteTarget)}
        title="Supprimer l'utilisateur"
        confirmLabel="Supprimer"
        danger
        onClose={() => setDeleteTarget(null)}
        onConfirm={confirmDelete}
        loading={actionLoading}
      >
        <p>Confirmez la suppression de "{deleteTarget?.fullName}".</p>
      </Modal>
    </div>
  );
}
