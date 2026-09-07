import { state } from "./shared.js";

/** One same-origin client for all modules; a 401 invalidates the local session. */
export async function api(path, { method = "GET", body, publicRequest = false } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (!publicRequest && state.session?.token) headers.Authorization = `Bearer ${state.session.token}`;
  let response;
  try {
    response = await fetch(path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  } catch {
    throw new Error("Serveur injoignable. Vérifie la connexion puis réessaie.");
  }
  if (response.status === 204) return null;
  const text = await response.text();
  let data;
  try { data = text ? JSON.parse(text) : null; } catch { data = null; }
  if (response.status === 401 && !publicRequest) {
    document.dispatchEvent(new Event("session-expired"));
    throw new Error("Ta session a expiré. Reconnecte-toi.");
  }
  if (!response.ok) throw new Error(data?.details?.join(" · ") || data?.message || `Erreur du serveur (${response.status}).`);
  return data;
}
