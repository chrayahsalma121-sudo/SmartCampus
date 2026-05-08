import { useEffect, useState } from "react";
import Alert from "../components/Alert.jsx";
import Card from "../components/Card.jsx";
import LoadingState from "../components/LoadingState.jsx";
import { getBooks } from "../services/bookService.js";
import { BOOK_STATUS } from "../utils/constants.js";
import { getErrorMessage } from "../utils/errorUtils.js";
import { countBy } from "../utils/formatters.js";

export default function LibrarianDashboard() {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false;

    async function loadBooks() {
      try {
        const data = await getBooks();
        if (!ignore) setBooks(data);
      } catch (err) {
        if (!ignore) setError(getErrorMessage(err, "Impossible de charger le dashboard bibliothecaire."));
      } finally {
        if (!ignore) setLoading(false);
      }
    }

    loadBooks();
    return () => {
      ignore = true;
    };
  }, []);

  if (loading) return <LoadingState label="Chargement du dashboard bibliothecaire" />;

  const available = countBy(books, (book) => book.status === BOOK_STATUS.available);
  const borrowed = countBy(books, (book) => book.status === BOOK_STATUS.borrowed);

  return (
    <div className="page-stack">
      <section className="hero-card librarian-hero">
        <div>
          <p className="eyebrow">Bibliotheque</p>
          <h2>Gestion du catalogue</h2>
          <p>Suivez la disponibilite des livres et administrez le catalogue.</p>
        </div>
      </section>

      <Alert type="error">{error}</Alert>

      <div className="stats-grid">
        <Card title="Total livres" value={books.length} tone="blue" />
        <Card title="Disponibles" value={available} tone="mint" />
        <Card title="Empruntes" value={borrowed} tone="orange" />
        <Card title="Emprunts detailles" value="Limite backend" tone="default" />
      </div>

      <div className="quick-grid">
        <Card title="Catalogue" to="/librarian/books" actionLabel="Gerer les livres" />
        <Card title="Emprunts" to="/librarian/loans" actionLabel="Voir la vue simplifiee" />
      </div>
    </div>
  );
}
