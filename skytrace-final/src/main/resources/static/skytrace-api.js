const API_BASE = window.location.origin.startsWith("http")
  ? window.location.origin
  : "http://localhost:8080";

const STATUTS = ["ENREGISTREMENT", "DEPOT_TAPIS", "TRI_TRANSFERT", "CHARGEMENT", "DECHARGEMENT", "LIVRAISON"];
const STATUT_LABELS = {
  ENREGISTREMENT: "Enregistrement",
  DEPOT_TAPIS: "Depot sur tapis",
  TRI_TRANSFERT: "Tri & transfert",
  CHARGEMENT: "Chargement",
  DECHARGEMENT: "Dechargement",
  LIVRAISON: "Livraison",
};

let anomaliesApi = [];

function getToken() {
  return session && session.token ? session.token : null;
}

function headers(authenticated = true) {
  const h = { "Content-Type": "application/json" };
  const token = getToken();
  if (authenticated && token) h.Authorization = `Bearer ${token}`;
  return h;
}

async function api(path, options = {}, authenticated = true) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: { ...headers(authenticated), ...(options.headers || {}) },
  });

  if (response.status === 204) return null;

  const text = await response.text();
  const body = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const message = body?.details?.join("\n") || body?.message || `Erreur API ${response.status}`;
    throw new Error(message);
  }
  return body;
}

function formatDate(value) {
  if (!value) return "-";
  return new Date(value).toLocaleString("fr-FR", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    day: "2-digit",
    month: "2-digit",
  });
}

function normalizeBagage(b, scans = []) {
  const statut = b.statut || "ENREGISTREMENT";
  const etapeIndex = Math.max(0, STATUTS.indexOf(statut));
  const anomalie = anomaliesApi.some(a => a.bagageId === b.id && a.resolu === false);
  const historique = [
    { etape: STATUT_LABELS.ENREGISTREMENT, heure: formatDate(b.dateCreation) },
    ...scans.map(s => ({ etape: s.pointScan, heure: formatDate(s.heure) })),
  ];

  return {
    id: b.id,
    code: b.codeQr,
    codeQr: b.codeQr,
    nom: b.nomPassager || "Passager non renseigne",
    vol: b.vol?.numeroVol || "-",
    dest: b.vol?.destination || "-",
    poids: b.poids ?? "-",
    statut,
    etapeIndex,
    anomalie,
    anomalieId: anomaliesApi.find(a => a.bagageId === b.id && a.resolu === false)?.id,
    historique,
    qrCodeBase64: b.qrCodeBase64,
  };
}

async function chargerBagagesDepuisApi() {
  if (!getToken()) {
    bagages = [];
    renderSelects();
    renderTable();
    return;
  }

  try {
    anomaliesApi = [];
    if (session.role === "SUPERVISEUR" || session.role === "ADMINISTRATEUR") {
      anomaliesApi = await api("/api/anomalies?nonResoluesUniquement=true");
    }

    const rawBagages = await api("/api/bagages");
    const avecHistorique = await Promise.all(rawBagages.map(async b => {
      const scans = await api(`/api/scans/bagage/${b.id}`).catch(() => []);
      return normalizeBagage(b, scans);
    }));

    bagages = avecHistorique;
    renderSelects();
    renderTable();
  } catch (error) {
    alert(error.message);
    if (error.message.includes("JWT") || error.message.includes("Acces refuse")) seDeconnecter();
  }
}

chargerSession = function() {
  const donnees = sessionStorage.getItem("session");
  session = donnees ? JSON.parse(donnees) : null;
};

sauvegarderSession = function() {
  if (session) sessionStorage.setItem("session", JSON.stringify(session));
  else sessionStorage.removeItem("session");
};

seConnecter = async function() {
  const login = document.getElementById("login-user").value.trim();
  const motDePasse = document.getElementById("login-pass").value;
  const erreur = document.getElementById("login-error");

  try {
    const utilisateur = await api("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ login, motDePasse }),
    }, false);

    erreur.classList.remove("show");
    session = {
      nom: utilisateur.nom,
      login: utilisateur.login,
      role: utilisateur.role,
      token: utilisateur.token,
    };
    sauvegarderSession();
    document.getElementById("login-user").value = "";
    document.getElementById("login-pass").value = "";
    renderNav();
    await chargerBagagesDepuisApi();
    activerPanel(ROLES[session.role].panel);
  } catch (error) {
    erreur.textContent = error.message;
    erreur.classList.add("show");
  }
};

seDeconnecter = function() {
  session = null;
  bagages = [];
  anomaliesApi = [];
  sauvegarderSession();
  renderNav();
  renderSelects();
  renderTable();
  activerPanel("connexion");
};

ajouterUtilisateur = async function() {
  const nom = document.getElementById("admin-nom").value.trim();
  const login = document.getElementById("admin-login").value.trim();
  const motDePasse = document.getElementById("admin-pass").value;
  const role = document.getElementById("admin-role").value;

  if (!nom || !login || !motDePasse) {
    alert("Merci de remplir tous les champs.");
    return;
  }

  try {
    await api("/api/utilisateurs", {
      method: "POST",
      body: JSON.stringify({ nom, login, motDePasse, role }),
    });
    document.getElementById("admin-nom").value = "";
    document.getElementById("admin-login").value = "";
    document.getElementById("admin-pass").value = "";
    await renderUtilisateurs();
  } catch (error) {
    alert(error.message);
  }
};

supprimerUtilisateur = async function(id) {
  if (!confirm("Supprimer cet utilisateur ?")) return;
  try {
    await api(`/api/utilisateurs/${id}`, { method: "DELETE" });
    await renderUtilisateurs();
  } catch (error) {
    alert(error.message);
  }
};

renderUtilisateurs = async function() {
  const tbody = document.getElementById("utilisateurs-body");
  if (!tbody || !getToken()) return;
  try {
    const utilisateursApi = await api("/api/utilisateurs");
    tbody.innerHTML = utilisateursApi.map(u => `
      <tr>
        <td>${u.nom}</td>
        <td style="font-family:var(--mono);font-size:12.5px">${u.login}</td>
        <td><span class="role-tag">${ROLES[u.role]?.label || u.role}</span></td>
        <td><button class="ghost" onclick="supprimerUtilisateur(${u.id})">Supprimer</button></td>
      </tr>
    `).join("");
  } catch (error) {
    tbody.innerHTML = `<tr><td colspan="4">${error.message}</td></tr>`;
  }
};

activerPanel = function(panelId) {
  document.querySelectorAll(".tab").forEach(b => b.classList.toggle("active", b.dataset.panel === panelId));
  document.querySelectorAll(".panel").forEach(p => p.classList.toggle("active", p.id === panelId));
  if (panelId === "administrateur") renderUtilisateurs();
  if (panelId === "superviseur" || panelId === "manutention") chargerBagagesDepuisApi();
};

async function trouverOuCreerVol(numeroVol, destination) {
  const vols = await api("/api/vols");
  const existant = vols.find(v => v.numeroVol.toLowerCase() === numeroVol.toLowerCase());
  if (existant) return existant;

  const origine = document.getElementById("in-origine").value.trim();
  const dateVol = document.getElementById("in-date-vol").value;
  if (!origine || !dateVol) {
    throw new Error("L'origine et la date du vol sont obligatoires.");
  }

  return api("/api/vols", {
    method: "POST",
    body: JSON.stringify({
      numeroVol,
      origine,
      destination,
      dateVol,
    }),
  });
}

enregistrerBagage = async function() {
  const nomPassager = document.getElementById("in-nom").value.trim();
  const numeroVol = document.getElementById("in-vol").value.trim();
  const destination = document.getElementById("in-dest").value.trim();
  const poidsValue = document.getElementById("in-poids").value.trim();
  const poids = poidsValue ? Number(poidsValue) : null;

  if (!nomPassager || !numeroVol || !destination || !poids) {
    alert("Merci de renseigner le passager, le vol, la destination et le poids.");
    return;
  }

  try {
    const vol = await trouverOuCreerVol(numeroVol, destination);
    const bagageApi = await api("/api/bagages", {
      method: "POST",
      body: JSON.stringify({ volId: vol.id, poids, nomPassager }),
    });

    const bagage = normalizeBagage(bagageApi, []);
    document.getElementById("in-nom").value = "";
    document.getElementById("in-vol").value = "";
    document.getElementById("in-dest").value = "";
    document.getElementById("in-poids").value = "";

    renderDernierBagage(bagage);
    await chargerBagagesDepuisApi();
  } catch (error) {
    alert(error.message);
  }
};

renderDernierBagage = function(b) {
  const el = document.getElementById("dernier-bagage");
  const qrHtml = b.qrCodeBase64
    ? `<img alt="QR ${b.code}" width="96" height="96" src="data:image/png;base64,${b.qrCodeBase64}">`
    : `<div class="qr-box" id="qr-${b.code}"></div>`;

  el.innerHTML = `
    <h2 style="margin-top:24px">Bagage enregistre</h2>
    <div class="tag">
      <div class="tag-main">
        <div class="tag-code">${b.code}</div>
        <div class="tag-name">${b.nom}</div>
        <div class="tag-meta">
          Vol ${b.vol} - ${b.dest}<br>
          Poids : ${b.poids} kg<br>
          Statut : ${STATUT_LABELS[b.statut] || b.statut}
        </div>
      </div>
      <div class="tag-stub">
        ${qrHtml}
        <div style="font-size:10px;color:var(--ink-soft);font-family:var(--mono)">SCAN MOI</div>
      </div>
    </div>
  `;

  if (!b.qrCodeBase64 && window.QRCode) {
    new QRCode(document.getElementById(`qr-${b.code}`), {
      text: b.code, width: 96, height: 96, colorDark: "#B01E23", colorLight: "#ffffff",
    });
  }
};

renderSelects = function() {
  const opts = bagages.map(b => `<option value="${b.code}">${b.code} - ${b.nom}</option>`).join("");
  const selScan = document.getElementById("select-scan");
  const selPass = document.getElementById("select-passager");
  const prevScan = selScan.value;
  const prevPass = selPass.value;
  selScan.innerHTML = opts || '<option value="">Aucun bagage disponible</option>';
  selPass.innerHTML = opts || '<option value="">Aucun bagage disponible</option>';
  if (bagages.some(b => b.code === prevScan)) selScan.value = prevScan;
  if (bagages.some(b => b.code === prevPass)) selPass.value = prevPass;
  renderScanPreview();
};

renderScanPreview = function() {
  const code = document.getElementById("select-scan").value;
  const el = document.getElementById("scan-preview");
  const b = trouverBagage(code);
  if (!b) {
    el.innerHTML = '<p class="empty">Connecte-toi avec un compte manutention et enregistre d abord un bagage.</p>';
    return;
  }
  el.innerHTML = `
    <p style="font-size:14px;margin:16px 0 4px"><strong>${b.nom}</strong> - Vol ${b.vol}</p>
    ${renderStepper(b)}
  `;
};

scannerBagage = async function() {
  const codeQr = document.getElementById("select-scan").value;
  if (!codeQr) return;
  try {
    await api("/api/scans", {
      method: "POST",
      body: JSON.stringify({ codeQr }),
    });
    await chargerBagagesDepuisApi();
    afficherSuiviPassager();
  } catch (error) {
    alert(error.message);
  }
};

toggleAnomalie = async function(code) {
  const b = trouverBagage(code);
  if (!b) return;

  try {
    if (b.anomalie && b.anomalieId) {
      await api(`/api/anomalies/${b.anomalieId}/resoudre`, { method: "PUT" });
    } else {
      await api("/api/anomalies", {
        method: "POST",
        body: JSON.stringify({ bagageId: b.id, typeAnomalie: "Anomalie signalee depuis le tableau de bord" }),
      });
    }
    await chargerBagagesDepuisApi();
  } catch (error) {
    alert(error.message);
  }
};

renderTable = function() {
  const tbody = document.getElementById("table-body");
  const empty = document.getElementById("table-empty");
  if (bagages.length === 0) {
    tbody.innerHTML = "";
    empty.style.display = "block";
  } else {
    empty.style.display = "none";
    tbody.innerHTML = bagages.map(b => `
      <tr class="${b.anomalie ? "anomalie" : ""}">
        <td style="font-family:var(--mono);font-size:12.5px">${b.code}</td>
        <td>${b.nom}</td>
        <td>${b.vol}</td>
        <td>
          <div class="status-cell">
            <span class="pill ${b.anomalie ? "warn" : "ok"}">${b.anomalie ? "Anomalie" : (STATUT_LABELS[b.statut] || b.statut)}</span>
          </div>
        </td>
        <td>${b.historique[b.historique.length - 1]?.heure || "-"}</td>
        <td><button class="ghost" onclick="toggleAnomalie('${b.code}')">${b.anomalie ? "Resoudre" : "Signaler"}</button></td>
      </tr>
    `).join("");
  }

  document.getElementById("stat-total").textContent = bagages.length;
  document.getElementById("stat-livres").textContent = bagages.filter(b => b.statut === "LIVRAISON").length;
  document.getElementById("stat-anomalies").textContent = bagages.filter(b => b.anomalie).length;
};

afficherSuiviPassager = async function() {
  const selectValue = document.getElementById("select-passager").value;
  const inputValue = document.getElementById("passager-code-input")?.value.trim();
  const codeQr = inputValue || selectValue;
  const el = document.getElementById("suivi-passager");

  if (!codeQr) {
    el.innerHTML = "";
    return;
  }

  try {
    const suivi = await api(`/api/bagages/suivi/${encodeURIComponent(codeQr)}`, {}, false);
    const statutIndex = Math.max(0, STATUTS.indexOf(suivi.statut));
    const b = {
      etapeIndex: statutIndex,
      anomalie: suivi.anomalieEnCours,
      historique: suivi.historique.map(h => ({ etape: h.pointScan, heure: formatDate(h.heure) })),
    };
    const historiqueHtml = b.historique.slice().reverse().map(h => `<div>${h.etape} - ${h.heure}</div>`).join("");

    el.innerHTML = `
      <div class="card">
        ${suivi.anomalieEnCours ? '<div class="notice">Une anomalie a ete signalee sur ce bagage. Un agent va vous contacter.</div>' : ""}
        <p style="font-size:14px;color:var(--ink-soft);margin:0 0 2px">Vol ${suivi.numeroVol} - ${suivi.destination}</p>
        <p style="font-size:18px;font-weight:700;color:var(--navy);margin:0 0 4px">${suivi.nomPassager || "Passager"}</p>
        <p style="font-size:14px;margin:0 0 4px">
          Statut actuel : <strong>${STATUT_LABELS[suivi.statut] || suivi.statut}</strong>
          ${suivi.livre ? '<span class="pill ok" style="margin-left:8px">Livre</span>' : '<span class="pill ok" style="margin-left:8px">En cours</span>'}
        </p>
        ${renderStepper(b)}
        <div class="history">
          <div style="font-weight:700;color:var(--ink);margin-bottom:4px">Historique des scans</div>
          ${historiqueHtml || "<div>Enregistrement - information disponible cote agent</div>"}
        </div>
      </div>
    `;
  } catch (error) {
    el.innerHTML = `<div class="notice">${error.message}</div>`;
  }
};

reinitialiser = function() {
  alert("Les donnees sont maintenant dans SQL Server. Supprime-les depuis la base si tu veux repartir de zero.");
};

function installerSuiviPublic() {
  const select = document.getElementById("select-passager");
  if (!select || document.getElementById("passager-code-input")) return;

  const label = document.createElement("label");
  label.textContent = "Ou saisir le code QR";
  const input = document.createElement("input");
  input.id = "passager-code-input";
  input.placeholder = "ex: BAG-0001-ABCD";
  input.addEventListener("change", afficherSuiviPassager);
  input.addEventListener("keydown", event => {
    if (event.key === "Enter") afficherSuiviPassager();
  });
  const button = document.createElement("button");
  button.className = "primary";
  button.type = "button";
  button.textContent = "Suivre";
  button.onclick = afficherSuiviPassager;
  select.parentElement.append(label, input, button);
}

function initialiserDateVol() {
  const input = document.getElementById("in-date-vol");
  if (!input || input.value) return;
  const date = new Date(Date.now() + 60 * 60 * 1000);
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  input.value = date.toISOString().slice(0, 16);
}

chargerSession();
renderNav();
installerSuiviPublic();
initialiserDateVol();
if (session) {
  chargerBagagesDepuisApi();
  activerPanel(ROLES[session.role].panel);
} else {
  renderSelects();
  renderTable();
  activerPanel("connexion");
}
