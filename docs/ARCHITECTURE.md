# Architecture

Un seul exécutable Spring Boot sert l'API et les fichiers statiques. Nginx est
le point d'entrée du déploiement. SQL Server conserve les données.

| Module Java | Responsabilité | Dépendances métier |
|---|---|---|
| `utilisateurs` | Comptes, rôles, premier administrateur | aucune |
| `auth` | Connexion, JWT, autorisations | utilisateurs |
| `vols` | Vols | aucune |
| `bagages` | Enregistrement, QR, état du bagage | vols |
| `scans` | Transitions et historique | bagages, utilisateurs |
| `anomalies` | Signalement et résolution | bagages |
| `suivi` | Lecture publique combinant les domaines | bagages, scans, anomalies |
| `shared` | Réponses d'erreur communes | aucune |

Chaque domaine regroupe ses contrôleurs, services, DTO, entités et repositories.
Les références JPA sont unidirectionnelles pour éviter les cycles entre modules.
Les contrôleurs valident l'HTTP ; les services portent les transactions et les règles.
Le module `suivi` est une composition en lecture seule, sans logique de mutation.

Le frontend utilise les modules ES natifs de `static/assets/` : un client HTTP,
les utilitaires communs, l'authentification, les utilisateurs, les bagages et le suivi.
`app.js` relie les événements aux actions. Aucun bundler ni framework supplémentaire.

Points de cohérence :

- Flyway versionne le schéma ; Hibernate le valide sans le modifier.
- Les clés étrangères protègent les données liées, y compris contre les suppressions.
- Un scan verrouille le bagage et vérifie le statut attendu : une requête périmée échoue en 409.
- Le code public utilise un UUID aléatoire ; les anciens codes restent consultables.
- Les valeurs affichées sont échappées et le suivi public ne divulgue pas le nom des agents.
- Le JWT en sessionStorage reste un compromis de simplicité : éviter toute injection HTML.
