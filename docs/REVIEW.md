# Review en 10 minutes

Branche : `refactor/modular-compose`. Lire les commits dans l'ordre :

```bash
git log --reverse --oneline c7ed41e..HEAD
```

| Étape | Ce qu'il faut regarder |
|---|---|
| État local préservé | Les ajustements Maven et SQL préexistants sont conservés dans un commit distinct. |
| Nettoyage | `app/`, wrapper Maven standard, fichiers générés et IDE exclus. |
| Démarrage fiable | Migration Flyway, IDs BIGINT, bootstrap admin et erreurs HTTP. |
| Domaines métier | Dépendances à sens unique ; scan transactionnel refusant une requête périmée. |
| Interface modulaire | `assets/app.js` relie les événements, `api.js` centralise les appels. |
| Tests | Parcours complet, rôles, concurrence, architecture et navigateur. |
| Déploiement | Compose : Nginx → application → SQL Server ; initialisation ponctuelle. |
| Documentation | Une seule procédure de lancement et des résultats de validation explicites. |

Décisions à retenir : un seul service applicatif, pas de framework frontend,
pas de données métier simulées, pas de migration automatique de l'ancienne base.
Les nouveaux codes QR sont plus longs pour protéger le suivi public ; les anciens restent acceptés.
L'API de scan exige désormais `statutAttendu` pour empêcher les doubles transitions.

Le déploiement est HTTP sur localhost par défaut. HTTPS et la reprise des données
historiques sont des étapes propres à l'environnement cible, documentées dans le README.
Les évolutions facultatives (caméra QR, exports, pagination serveur) restent hors de cette remise en état.
