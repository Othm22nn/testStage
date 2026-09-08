import { $, value, steps, escapeHtml as e, date, stepper } from "./shared.js";
import { api } from "./api.js";

export async function afficherSuiviPassager() {
  const code = value("passager-code-input");
  if (!code) throw new Error("Saisis le code de ton bagage.");
  $("suivi-passager").replaceChildren();
  const suivi = await api(`/api/bagages/suivi/${encodeURIComponent(code)}`, { publicRequest: true });
  $("suivi-passager").innerHTML = `<div class="card">
    ${suivi.anomalieEnCours ? '<div class="notice">Une anomalie est en cours de traitement.</div>' : ""}
    <p>Vol ${e(suivi.numeroVol)} — ${e(suivi.destination)}</p>
    <p>${e(suivi.nomPassager)}</p><p>Statut actuel : <strong>${steps[suivi.statut]}</strong></p>
    ${stepper(suivi.statut)}<div class="history"><strong>Historique des scans</strong>
    ${suivi.historique.map(h => `<div>${e(h.pointScan)} — ${e(date(h.heure))}</div>`).join("") || "<p>En attente du premier scan.</p>"}
    </div></div>`;
}
