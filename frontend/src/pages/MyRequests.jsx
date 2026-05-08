import { useEffect, useState } from "react";
import Alert from "../components/Alert.jsx";
import StatusBadge from "../components/StatusBadge.jsx";
import Table from "../components/Table.jsx";
import { getMyRequests } from "../services/requestService.js";
import { REQUEST_TYPES } from "../utils/constants.js";
import { getErrorMessage } from "../utils/errorUtils.js";
import { formatDate, formatRequestType } from "../utils/formatters.js";

export default function MyRequests() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState("ALL");
  const [type, setType] = useState("ALL");
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false;

    async function loadRequests() {
      setLoading(true);
      setError("");
      try {
        const data = await getMyRequests();
        if (!ignore) setRequests(data);
      } catch (err) {
        if (!ignore) setError(getErrorMessage(err, "Impossible de charger vos demandes."));
      } finally {
        if (!ignore) setLoading(false);
      }
    }

    loadRequests();
    return () => {
      ignore = true;
    };
  }, []);

  const filtered = requests.filter((request) => {
    const statusMatch = status === "ALL" || request.status === status;
    const typeMatch = type === "ALL" || request.type === type;
    return statusMatch && typeMatch;
  });

  const columns = [
    { key: "type", header: "Type", render: (row) => formatRequestType(row.type) },
    { key: "description", header: "Description", render: (row) => row.description || "-" },
    { key: "submissionDate", header: "Date", render: (row) => formatDate(row.submissionDate) },
    { key: "status", header: "Etat", render: (row) => <StatusBadge status={row.status} /> },
    { key: "refusalReason", header: "Motif refus", render: (row) => row.refusalReason || "-" },
  ];

  return (
    <div className="page-stack">
      <section className="page-heading">
        <p className="eyebrow">Administration</p>
        <h2>Mes demandes</h2>
        <p>Suivez l'etat de vos demandes administratives.</p>
      </section>

      <Alert type="error">{error}</Alert>

      <div className="toolbar">
        <select value={status} onChange={(event) => setStatus(event.target.value)} aria-label="Filtrer par statut">
          <option value="ALL">Tous les statuts</option>
          <option value="PENDING">En attente</option>
          <option value="APPROVED">Validee</option>
          <option value="REJECTED">Refusee</option>
        </select>
        <select value={type} onChange={(event) => setType(event.target.value)} aria-label="Filtrer par type">
          <option value="ALL">Tous les types</option>
          {REQUEST_TYPES.map((item) => (
            <option key={item.value} value={item.value}>
              {item.label}
            </option>
          ))}
        </select>
      </div>

      <Table columns={columns} data={filtered} rowKey="requestId" loading={loading} emptyMessage="Aucune demande trouvee." />
    </div>
  );
}
