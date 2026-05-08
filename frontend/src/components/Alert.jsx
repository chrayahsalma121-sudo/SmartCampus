export default function Alert({ type = "info", title, children, className = "" }) {
  if (!children && !title) return null;

  return (
    <div className={`alert alert-${type} ${className}`.trim()} role="alert">
      {title ? <strong>{title}</strong> : null}
      {children ? <span>{children}</span> : null}
    </div>
  );
}
