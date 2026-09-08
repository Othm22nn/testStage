import { $, value, state, roles, escapeHtml, notify } from "./shared.js";
import { api } from "./api.js";

export function restoreSession() {
  try {
    const saved = JSON.parse(sessionStorage.getItem("skytrace.session"));
    state.session = saved?.token && roles[saved.role] ? saved : null;
  } catch { state.session = null; }
}
export function renderNav() {
  const role = roles[state.session?.role];
  $("nav-container").innerHTML = (role
    ? `<button class="tab" data-panel="${role.panel}">${role.label}</button>`
    : '<button class="tab" data-panel="connexion">Connexion</button>')
    + '<button class="tab" data-panel="passager">Passager</button>';
  $("session-info").innerHTML = role
    ? `<div class="who"><strong>${escapeHtml(state.session.nom)}</strong><span>${role.label}</span></div>
       <button class="btn-logout" data-action="seDeconnecter">Déconnexion</button>` : "";
}
export function showPanel(id) {
  if (!["connexion", "passager", roles[state.session?.role]?.panel].includes(id)) return;
  document.querySelectorAll(".panel").forEach(panel => panel.classList.toggle("active", panel.id === id));
  document.querySelectorAll(".tab").forEach(tab => tab.classList.toggle("active", tab.dataset.panel === id));
}
export async function seConnecter() {
  try {
    state.session = await api("/api/auth/login", { method: "POST", publicRequest: true,
      body: { login: value("login-user"), motDePasse: $("login-pass").value } });
    sessionStorage.setItem("skytrace.session", JSON.stringify(state.session));
    $("login-pass").value = "";
    $("login-error").classList.remove("show");
    renderNav();
    showPanel(roles[state.session.role].panel);
    document.dispatchEvent(new Event("session-started"));
  } catch (error) {
    $("login-error").textContent = error.message;
    $("login-error").classList.add("show");
  }
}
export function seDeconnecter() {
  state.session = null;
  state.bagages = [];
  state.anomalies = [];
  sessionStorage.removeItem("skytrace.session");
  for (const id of ["dernier-bagage", "table-body", "utilisateurs-body", "scan-preview", "suivi-passager", "select-scan"])
    $(id).replaceChildren();
  $("passager-code-input").value = "";
  renderNav();
  showPanel("connexion");
  notify("");
}
