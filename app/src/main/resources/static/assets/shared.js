export const state = { session: null, bagages: [], anomalies: [] };
export const roles = {
  AGENT_ENREGISTREMENT: { label: "Agent d'enregistrement", panel: "enregistrement" },
  AGENT_MANUTENTION: { label: "Agent de manutention", panel: "manutention" },
  SUPERVISEUR: { label: "Superviseur", panel: "superviseur" },
  ADMINISTRATEUR: { label: "Administrateur", panel: "administrateur" },
};
export const steps = {
  ENREGISTREMENT: "Enregistrement", DEPOT_TAPIS: "Dépôt sur tapis",
  TRI_TRANSFERT: "Tri & transfert", CHARGEMENT: "Chargement",
  DECHARGEMENT: "Déchargement", LIVRAISON: "Livraison",
};
export const $ = id => document.getElementById(id);
export const value = id => $(id).value.trim();
export const escapeHtml = value => String(value ?? "").replace(/[&<>"']/g,
  char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[char]);
export const date = value => value ? new Date(value).toLocaleString("fr-FR") : "—";
export function notify(message) {
  $("message").textContent = message;
  $("message").hidden = !message;
}
export function stepper(status) {
  const current = Object.keys(steps).indexOf(status);
  return '<div class="stepper">' + Object.values(steps).map((label, index) =>
    `<div class="step ${index < current ? "done" : index === current ? "current" : ""}">
      <div class="line"></div><div class="dot">${index + 1}</div><div class="label">${label}</div>
    </div>`).join("") + "</div>";
}
