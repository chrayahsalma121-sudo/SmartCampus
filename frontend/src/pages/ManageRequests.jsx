import { useEffect, useState } from "react";
import Alert from "../components/Alert.jsx";
import Button from "../components/Button.jsx";
import Modal from "../components/Modal.jsx";
import StatusBadge from "../components/StatusBadge.jsx";
import Table from "../components/Table.jsx";
import { approveRequest, getAllRequests, rejectRequest } from "../services/requestService.js";
import { REQUEST_STATUS } from "../utils/constants.js";
import { getErrorMessage } from "../utils/errorUtils.js";
import { formatDate, formatRequestType } from "../utils/formatters.js";

export default function ManageRequests() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [approveTarget, setApproveTarget] = useState(null);
  const [rejectTarget, setRejectTarget] = useState(null);
  const [refusalReason, setRefusalReason] = useState("");
  const [status, setStatus] = useState("ALL");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadRequests() {
    setLoading(true);
    setError("");
    try {
      setRequests(await getAllRequests());
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de charger les demandes."));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadRequests();
  }, []);

  async function confirmApprove() {
    if (!approveTarget) return;
    setActionLoading(true);
    setError("");
    setSuccess("");
    try {
      const response = await approveRequest(approveTarget.requestId);
      setSuccess(response.message || "Demande validee.");
      setApproveTarget(null);
      await loadRequests();
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de valider la demande."));
    } finally {
      setActionLoading(false);
    }
  }

  async function confirmReject() {
    if (!rejectTarget) return;
    if (!refusalReason.trim()) {
      setError("Le motif de refus est obligatoire.");
      return;
    }

    setActionLoading(true);
    setError("");
    setSuccess("");
    try {
      const response = await rejectRequest(rejectTarget.requestId, refusalReason.trim());
      setSuccess(response.message || "Demande refusee.");
      setRejectTarget(null);
      setRefusalReason("");
      await loadRequests();
    } catch (err) {
      setError(getErrorMessage(err, "Impossible de refuser la demande."));
    } finally {
      setActionLoading(false);
    }
  }

  const filtered = requests.filter((request) => status === "ALL" || request.status === status);

  const columns = [
    { key: "requestId", header: "ID" },
    { key: "student", header: "Etudiant", render: (row) => row.studentName || `Etudiant #${row.studentId}` },
    { key: "type", header: "Type", render: (row) => formatRequestType(row.type) },
    { key: "submissionDate", header: "Date", render: (row) => formatDate(row.submissionDate) },
    { key: "status", header: "Statut", render: (row) => <StatusBadge status={row.status} /> },
    { key: "refusalReason", header: "Motif", render: (row) => row.refusalReason || "-" },
    {
      key: "actions",
      header: "Actions",
      render: (row) => (
        <div className="row-actions">
          <Button size="sm" disabled={row.status !== REQUEST_STATUS.pending} onClick={() => setApproveTarget(row)}>
            Valider
          </Button>
          <Button size="sm" variant="danger" disabled={row.status !== REQUEST_STATUS.pending} onClick={() => setRejectTarget(row)}>
            Refuser
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="page-stack">
      <section className="page-heading">
        <p className="eyebrow">Administration</p>
        <h2>Gestion des demandes</h2>
        <p>Validez ou refusez les demandes administratives en attente.</p>
      </section>

      <Alert type="error">{error}</Alert>
      <Alert type="success">{success}</Alert>

      <div className="toolbar">
        <select value={status} onChange={(event) => setStatus(event.target.value)} aria-label="Filtrer les demandes">
          <option value="ALL">Tous les statuts</option>
          <option value="PENDING">En attente</option>
          <option value="APPROVED">Validees</option>
          <option value="REJECTED">Refusees</option>
        </select>
      </div>

      <Table columns={columns} data={filtered} rowKey="requestId" loading={loading} emptyMessage="Aucune demande trouvee." />

      <Modal
        open={Boolean(approveTarget)}
        title="Valider la demande"
        confirmLabel="Valider"
        onClose={() => setApproveTarget(null)}
        onConfirm={confirmApprove}
        loading={actionLoading}
      >
        <p>Confirmer la validation de la demande #{approveTarget?.requestId} ?</p>
      </Modal>

      <Modal
        open={Boolean(rejectTarget)}
        title="Refuser la demande"
        confirmLabel="Refuser"
        danger
        onClose={() => {
          setRejectTarget(null);
          setRefusalReason("");
        }}
        onConfirm={confirmReject}
        loading={actionLoading}
      >
        <label>
          Motif du refus
          <textarea
            value={refusalReason}
            onChange={(event) => setRefusalReason(event.target.value)}
            rows="4"
            placeholder="Expliquez le motif du refus"
          />
        </label>
      </Modal>
    </div>
  );
}
