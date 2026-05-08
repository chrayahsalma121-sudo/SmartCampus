import { formatStatus } from "../utils/formatters.js";

export default function StatusBadge({ status }) {
  const normalized = status || "UNKNOWN";
  return <span className={`status-badge status-${normalized.toLowerCase()}`}>{formatStatus(status)}</span>;
}
