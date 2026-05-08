export function getErrorMessage(error, fallback = "Une erreur est survenue.") {
  if (!error) return fallback;
  if (typeof error === "string") return error;
  if (error.message) return error.message;
  return fallback;
}

export function isUnauthorizedError(error) {
  return error?.status === 401;
}
