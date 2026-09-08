import { $, state, roles, notify } from "./shared.js";
import { restoreSession, renderNav, showPanel, seConnecter, seDeconnecter } from "./auth.js";
import { renderUtilisateurs, ajouterUtilisateur, supprimerUtilisateur } from "./utilisateurs.js";
import { chargerBagagesDepuisApi, enregistrerBagage, renderScanPreview, scannerBagage, renderTable, toggleAnomalie } from "./bagages.js";
import { afficherSuiviPassager } from "./suivi.js";

const actions = { seConnecter, seDeconnecter, ajouterUtilisateur, supprimerUtilisateur,
  chargerBagagesDepuisApi, enregistrerBagage, renderScanPreview, scannerBagage, toggleAnomalie, afficherSuiviPassager };
let busy = false;
async function run(action, button, id) {
  if (busy) return;
  busy = true;
  if (button) button.disabled = true;
  notify("");
  try { await action(id); } catch (error) { notify(error.message); }
  finally { busy = false; if (button) button.disabled = false; }
}
async function loadSession() {
  if (state.session?.role === "ADMINISTRATEUR") await renderUtilisateurs();
  else if (state.session) await chargerBagagesDepuisApi();
}
document.addEventListener("click", event => {
  const button = event.target.closest("button");
  if (!button) return;
  if (button.dataset.panel) {
    showPanel(button.dataset.panel);
    if (button.dataset.panel !== "passager") run(loadSession);
  } else if (actions[button.dataset.action]) run(actions[button.dataset.action], button, button.dataset.id);
});
document.addEventListener("change", event => {
  if (actions[event.target.dataset.change]) run(actions[event.target.dataset.change]);
});
$("filter-bagages").addEventListener("input", renderTable);
$("login-pass").addEventListener("keydown", event => { if (event.key === "Enter") run(seConnecter); });
$("passager-code-input").addEventListener("keydown", event => { if (event.key === "Enter") run(afficherSuiviPassager); });
document.addEventListener("session-started", () => { loadSession().catch(error => notify(error.message)); });
document.addEventListener("session-expired", seDeconnecter);

const departure = new Date(Date.now() + 3600000);
departure.setMinutes(departure.getMinutes() - departure.getTimezoneOffset());
$("in-date-vol").value = departure.toISOString().slice(0, 16);
restoreSession();
renderNav();
showPanel(roles[state.session?.role]?.panel || "connexion");
run(loadSession);
