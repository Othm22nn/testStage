# Validation — 7 septembre 2026

| Vérification | Résultat |
|---|---|
| `mvnw verify` avec Java 21 | 9 tests réussis et JAR exécutable construit |
| API sur SQL Server 2025, avec les rôles SQL du déploiement | 3 tests réussis, dont deux scans concurrents : une seule transition |
| Edge → Nginx 1.28.3 → Spring Boot → SQL Server | Parcours E2E réussi, aucun échec JavaScript ni réponse 5xx |
| Configuration Compose | `config --quiet` réussi avec Compose 5.5.1 |
| Script d'initialisation | Syntaxe Bash vérifiée |

Le test derrière Nginx a détecté et permis de corriger la transmission du port
public (`X-Forwarded-Host`) ainsi que la duplication des en-têtes de sécurité.

Preuves courtes : [API SQL](evidence/api-sql.txt),
[navigateur via Nginx et SQL](evidence/browser-sql-nginx.txt).
Captures : [bagage et QR](screenshots/bagage.png), [suivi](screenshots/suivi.png),
[mobile](screenshots/mobile.png). Le nom contenant une balise HTML est une donnée
de test volontaire : il est affiché comme du texte, sans créer d'élément HTML.

Scénario navigateur : administrateur → création des trois profils → enregistrement
du bagage et QR → cinq scans → refus après livraison → anomalie → résolution →
suivi sans connexion. Vérifications supplémentaires : recherche vide, code inconnu,
texte HTML affiché sans exécution, absence d'erreurs JavaScript et aperçu mobile.

Les tests SQL créent une base et un login temporaires, puis les suppriment même
en cas d'échec. La base historique `SkyTraceDB` n'a pas été modifiée.
Contrôle final : aucune base ni aucun login de test résiduel, aucun Nginx de test actif.

Limites connues :

- Le navigateur intégré n'a pas pu être initialisé dans cette session ; le parcours
  est exécuté avec Playwright Java et Microsoft Edge en arrière-plan.
- Le moteur Docker n'est pas installé. La configuration Compose peut être validée,
  mais la construction des images et le démarrage des conteneurs ne sont pas attestés localement.
- La CI inclut la construction et l'E2E via Compose ; elle n'a pas été exécutée sur GitHub.
- SQL Server local est en version 2025. Flyway signale cette version comme plus récente
  que celles couvertes officiellement par sa version embarquée ; les résultats locaux sont consignés ci-dessous.

Références utilisées pour le déploiement et les tests :
[ordre de démarrage Compose](https://docs.docker.com/compose/how-tos/startup-order/),
[navigateurs Playwright Java](https://playwright.dev/java/docs/browsers).
