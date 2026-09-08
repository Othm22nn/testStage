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
- La construction des images, le démarrage Compose et le parcours Edge contre les
  conteneurs ont été validés après redémarrage (voir ci-dessous).
- La CI inclut la construction et l'E2E via Compose ; elle n'a pas été exécutée sur GitHub.
- SQL Server local est en version 2025. Flyway signale cette version comme plus récente
  que celles couvertes officiellement par sa version embarquée ; les résultats locaux sont consignés ci-dessous.

Références utilisées pour le déploiement et les tests :
[ordre de démarrage Compose](https://docs.docker.com/compose/how-tos/startup-order/),
[navigateurs Playwright Java](https://playwright.dev/java/docs/browsers).

## Préparation Docker du poste

- Docker Desktop 4.90.0 installé dans `D:\DockerDesktop` ; données WSL Docker
  configurées dans `D:\DockerData`. Installateur officiel signé et SHA-256 vérifié.
- CLI Docker 29.7.2 et Compose 5.5.1 vérifiées ; `compose config --quiet` réussi.
- WSL 2.7.13.0 installé depuis le MSI Microsoft signé, SHA-256 vérifié.
- Fonctionnalités Windows WSL et VirtualMachinePlatform activées sans redémarrage.
- Réglage Lenovo `VirtualizationTechnology=Enable` enregistré avec succès.
  Il ne prendra effet qu'après redémarrage ; aucun autre réglage BIOS modifié.
- C: reste très contraint (environ 0,5 Go libre après installation des composants
  Windows). Ne pas y télécharger les images ni y déplacer les données Docker.

Reprise après redémarrage : démarrer Docker Desktop, vérifier `docker info`,
puis exécuter `docker compose up -d --build --wait --wait-timeout 240` à la racine.
Conserver le `.env` existant. Valider ensuite le parcours navigateur contre
`http://localhost:8080` avec `E2E_BASE_URL` et `E2E_ADMIN_PASSWORD` issu du `.env`,
puis consigner le résultat réel ici. Aucun conteneur SkyTrace n'a encore été créé.

Références : [installation Docker Windows](https://docs.docker.com/desktop/setup/install/windows-install/),
[installation WSL](https://learn.microsoft.com/en-us/windows/wsl/install),
[configuration BIOS Lenovo](https://docs.lenovocdrt.com/ref/bios/wmi/wmi_guide/).

## Validation Docker après redémarrage — 7 septembre 2026

- Moteur Docker 29.7.2 opérationnel avec WSL 2 ; disques virtuels dans `D:\DockerData`.
- `docker compose up -d --build --wait --wait-timeout 240` terminé avec le code 0.
- Image SkyTrace construite avec Java 21 ; SQL Server 2022 Express, application et
  Nginx déclarés `healthy`. Initialisation `db-init` terminée avec le code 0.
- `http://127.0.0.1:8080/actuator/health` répond `{"status":"UP"}`.
- Parcours Microsoft Edge contre `E2E_BASE_URL=http://127.0.0.1:8080`, avec
  Java 21.0.11 et le mot de passe administrateur du `.env` existant :
  `mvnw.cmd -B -ntp -Pe2e -Dtest=BrowserE2ETest test` réussi.
  Résultat : 1 test, 0 échec, 0 erreur, 0 ignoré ; durée du test 56,21 secondes.
- Le navigateur cible Nginx, Spring Boot et SQL Server dans Docker. Le contexte
  Spring/H2 auxiliaire lancé par le harnais de test ne sert pas le parcours navigateur.
- Les réponses 409 (scan après livraison) et 404 (code inconnu) sont attendues
  par le scénario ; aucune erreur JavaScript ni réponse 5xx détectée.
- Preuve : [rapport E2E Docker](evidence/browser-docker.txt).
  Captures de ce passage : `app/target/e2e/01-administration.png` à `05-mobile.png`.
- Stack laissée démarrée sur `http://localhost:8080`. Le `.env` est conservé.
  Les trois comptes de démonstration (`enregistrement`, `manutention`, `superviseur`)
  et le bagage de test livré étaient présents à la fin de ce test dans le volume Docker `skytrace_sql-data`.
  Le scénario crée des identifiants fixes : une nouvelle exécution nécessite
  une base de test vide ou une adaptation des données du scénario.
- La validation GitHub CI reste à effectuer.

## Nettoyage des données de démonstration — 8 septembre 2026

Une nouvelle sauvegarde a été restaurée et vérifiée avant le nettoyage.
Le compte Test manutention, le bagage de démonstration, ses cinq scans et son
anomalie ont été supprimés dans une transaction ciblée. Les quatre comptes,
les deux autres bagages, leurs sept scans et leur anomalie ont été conservés.
Les trois services Docker sont restés sains après cette opération.

Procédure de sauvegarde : [guide](BACKUP.md).
