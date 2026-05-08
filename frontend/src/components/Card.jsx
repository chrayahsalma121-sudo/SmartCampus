import { Link } from "react-router-dom";

export default function Card({
  title,
  value,
  eyebrow,
  children,
  actionLabel,
  to,
  tone = "default",
  className = "",
}) {
  const content = (
    <section className={`card card-${tone} ${className}`.trim()}>
      {eyebrow ? <p className="card-eyebrow">{eyebrow}</p> : null}
      {title ? <h3>{title}</h3> : null}
      {value !== undefined ? <p className="card-value">{value}</p> : null}
      {children ? <div className="card-body">{children}</div> : null}
      {actionLabel ? <span className="card-action">{actionLabel}</span> : null}
    </section>
  );

  return to ? (
    <Link to={to} className="card-link">
      {content}
    </Link>
  ) : (
    content
  );
}
