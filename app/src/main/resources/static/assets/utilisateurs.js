import { $, value, roles, escapeHtml, notify } from "./shared.js";
import { api } from "./api.js";

export async function renderUtilisateurs() {
  const users = await api("/api/utilisateurs");
  $("utilisateurs-body").innerHTML = users.map(user => `<tr>
    <td>${escapeHtml(user.nom)}</td><td>${escapeHtml(user.login)}</td>
    <td><span class="role-tag">${escapeHtml(roles[user.role]?.label)}</span></td>
    <td><button class="ghost" data-action="supprimerUtilisateur" data-id="${user.id}">Supprimer</button></td>
  </tr>`).join("");
}
export async function ajouterUtilisateur() {
  const body = { nom: value("admin-nom"), login: value("admin-login"),
    motDePasse: $("admin-pass").value, role: value("admin-role") };
  if (!body.nom || !body.login || body.motDePasse.length < 6)
    throw new Error("Renseigne le nom, l'identifiant et un mot de passe d'au moins 6 caractères.");
  await api("/api/utilisateurs", { method: "POST", body });
  for (const id of ["admin-nom", "admin-login", "admin-pass"]) $(id).value = "";
  await renderUtilisateurs();
  notify("Utilisateur créé.");
}
export async function supprimerUtilisateur(id) {
  if (!confirm("Supprimer cet utilisateur ?")) return;
  await api(`/api/utilisateurs/${id}`, { method: "DELETE" });
  await renderUtilisateurs();
  notify("Utilisateur supprimé.");
}
