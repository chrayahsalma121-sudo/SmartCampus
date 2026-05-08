export default function LoadingState({ label = "Chargement", fullPage = false }) {
  return (
    <div className={fullPage ? "loading-state loading-full" : "loading-state"}>
      <span className="spinner" aria-hidden="true" />
      <p>{label}</p>
    </div>
  );
}
