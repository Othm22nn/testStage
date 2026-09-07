import { $, value, state, steps, escapeHtml as e, date, stepper, notify } from "./shared.js";
import { api } from "./api.js";

export async function chargerBagagesDepuisApi() {
  if (!state.session) return;
  const [bags, anomalies] = await Promise.all([api("/api/bagages"),
    ["SUPERVISEUR", "ADMINISTRATEUR"].includes(state.session.role)
      ? api("/api/anomalies?nonResoluesUniquement=true") : []]);
  if (!state.session) return;
  state.bagages = bags;
  state.anomalies = anomalies;
  const previous = value("select-scan");
  $("select-scan").innerHTML = bags.map(b => `<option value="${e(b.codeQr)}">${e(b.codeQr)} — ${e(b.nomPassager)}</option>`).join("")
    || '<option value="">Aucun bagage disponible</option>';
  if (bags.some(b => b.codeQr === previous)) $("select-scan").value = previous;
  renderScanPreview();
  renderTable();
}
export function renderTable() {
  const filter = value("filter-bagages").toLocaleLowerCase();
  const rows = state.bagages.filter(b => [b.codeQr, b.nomPassager, b.vol.numeroVol].join(" ").toLocaleLowerCase().includes(filter));
  $("table-body").innerHTML = rows.map(b => {
    const anomaly = state.anomalies.find(a => a.bagageId === b.id && !a.resolu);
    return `<tr class="${anomaly ? "anomalie" : ""}">
      <td>${e(b.codeQr)}</td><td>${e(b.nomPassager)}</td><td>${e(b.vol.numeroVol)}</td>
      <td><span class="pill ${anomaly ? "warn" : "ok"}">${anomaly ? "Anomalie" : steps[b.statut]}</span></td>
      <td>${e(date(b.dateCreation))}</td>
      <td><button class="ghost" data-action="toggleAnomalie" data-id="${b.id}">${anomaly ? "Résoudre" : "Signaler"}</button></td>
    </tr>`;
  }).join("");
  $("table-empty").style.display = rows.length ? "none" : "block";
  $("table-empty").textContent = filter ? "Aucun résultat." : "Aucun bagage enregistré pour le moment.";
  $("stat-total").textContent = state.bagages.length;
  $("stat-livres").textContent = state.bagages.filter(b => b.statut === "LIVRAISON").length;
  $("stat-anomalies").textContent = new Set(state.anomalies.filter(a => !a.resolu).map(a => a.bagageId)).size;
}
export async function enregistrerBagage() {
  const flight = { numeroVol: value("in-vol").toUpperCase(), origine: value("in-origine"),
    destination: value("in-dest"), dateVol: value("in-date-vol") };
  const nomPassager = value("in-nom"), poids = Number(value("in-poids"));
  if (!nomPassager || Object.values(flight).some(v => !v) || !Number.isFinite(poids) || poids < .1 || poids > 99.99)
    throw new Error("Renseigne tous les champs et un poids entre 0,10 et 99,99 kg.");
  const flights = await api("/api/vols");
  let vol = flights.find(v => v.numeroVol.toUpperCase() === flight.numeroVol);
  if (vol && (vol.origine !== flight.origine || vol.destination !== flight.destination
      || vol.dateVol.slice(0, 16) !== flight.dateVol.slice(0, 16)))
    throw new Error("Ce numéro de vol existe avec une autre origine, destination ou date. Vérifie les informations.");
  vol ||= await api("/api/vols", { method: "POST", body: flight });
  const b = await api("/api/bagages", { method: "POST", body: { volId: vol.id, nomPassager, poids } });
  $("dernier-bagage").innerHTML = `<h2>Bagage enregistré</h2><div class="tag"><div class="tag-main">
    <div class="tag-code">${e(b.codeQr)}</div><div class="tag-name">${e(b.nomPassager)}</div>
    <div class="tag-meta">Vol ${e(b.vol.numeroVol)} — ${e(b.vol.destination)}<br>Poids : ${b.poids} kg</div>
    </div><div class="tag-stub"><img alt="QR du bagage" width="110" height="110" src="data:image/png;base64,${e(b.qrCodeBase64)}"></div></div>`;
  $("in-nom").value = "";
  $("in-poids").value = "";
  await chargerBagagesDepuisApi();
  notify("Bagage enregistré. Le code permet au passager de consulter son suivi.");
}
export function renderScanPreview() {
  const b = state.bagages.find(b => b.codeQr === value("select-scan"));
  $("scan-preview").innerHTML = b ? `<p><strong>${e(b.nomPassager)}</strong> — Vol ${e(b.vol.numeroVol)}</p>
    <p>Statut actuel : <strong>${steps[b.statut]}</strong></p>${stepper(b.statut)}` : '<p class="empty">Aucun bagage disponible.</p>';
}
export async function scannerBagage() {
  const b = state.bagages.find(b => b.codeQr === value("select-scan"));
  if (!b) throw new Error("Sélectionne un bagage.");
  await api("/api/scans", { method: "POST", body: { codeQr: b.codeQr, statutAttendu: b.statut } });
  await chargerBagagesDepuisApi();
  notify("Scan enregistré.");
}
export async function toggleAnomalie(id) {
  const anomaly = state.anomalies.find(a => a.bagageId === Number(id) && !a.resolu);
  if (anomaly) await api(`/api/anomalies/${anomaly.id}/resoudre`, { method: "PUT" });
  else await api("/api/anomalies", { method: "POST", body: { bagageId: Number(id), typeAnomalie: "Contrôle demandé par le superviseur" } });
  await chargerBagagesDepuisApi();
  notify(anomaly ? "Anomalie résolue." : "Anomalie signalée.");
}
