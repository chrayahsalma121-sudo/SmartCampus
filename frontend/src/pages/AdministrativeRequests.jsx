import { useState } from "react";
import { Link } from "react-router-dom";
import Alert from "../components/Alert.jsx";
import Button from "../components/Button.jsx";
import { useAuth } from "../hooks/useAuth.js";
import { submitRequest } from "../services/requestService.js";
import { REQUEST_TYPES } from "../utils/constants.js";
import { getErrorMessage } from "../utils/errorUtils.js";

export default function AdministrativeRequests() {
  const { isStudentValid } = useAuth();
  const [type, setType] = useState("SCHOOL_CERTIFICATE");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (!isStudentValid) {
      setError("Votre compte etudiant n'est pas valide.");
      return;
    }

    if (!type) {
      setError("Le type de demande est obligatoire.");
      return;
    }

    setLoading(true);
    try {
      const response = await submitRequest({ type, description });
      setSuccess(response.message || "Demande administrative envoyee.");
      setDescription("");
    } catch (err) {
      setError(getErrorMessage(err, "Impossible d'envoyer la demande."));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page-stack narrow-page">
      <section className="page-heading">
        <p className="eyebrow">Administration</p>
        <h2>Nouvelle demande administrative</h2>
        <p>Soumettez une demande en moins de cinq etapes.</p>
      </section>

      {!isStudentValid ? <Alert type="warning">Votre compte non valide bloque les demandes.</Alert> : null}
      <Alert type="error">{error}</Alert>
      <Alert type="success">{success}</Alert>

      <form className="form-card" onSubmit={handleSubmit}>
        <label>
          Type de demande
          <select value={type} onChange={(event) => setType(event.target.value)} required>
            {REQUEST_TYPES.map((item) => (
              <option key={item.value} value={item.value}>
                {item.label}
              </option>
            ))}
          </select>
        </label>

        <label>
          Informations complementaires
          <textarea
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            rows="6"
            placeholder="Expliquez votre besoin..."
          />
        </label>

        <div className="form-actions">
          <Button type="submit" loading={loading} disabled={!isStudentValid}>
            Soumettre la demande
          </Button>
          <Link className="btn btn-ghost btn-md" to="/student/requests">
            Voir mes demandes
          </Link>
        </div>
      </form>
    </div>
  );
}
