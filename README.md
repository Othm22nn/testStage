# SkyTrace

Traçabilité des bagages : Java 21, Spring Boot, SQL Server et interface web native.

- `app/` : application et tests Maven.
- `deploy/` : configuration Nginx et initialisation du déploiement.
- `docs/` : architecture, validation et ordre de review.

## Démarrer avec Docker

Prérequis : Docker Engine + Compose, conteneurs Linux x86-64, au moins 4 Go de RAM
disponibles. SQL Server Express est inclus pour une installation autonome.

Depuis la racine, sous PowerShell :

```powershell
.\scripts\New-Environment.ps1
docker compose up -d --build --wait --wait-timeout 240
```

Ouvrir http://localhost:8080. Identifiant `admin`, mot de passe `ADMIN_PASSWORD`
dans `.env`. Le script ne remplace jamais un `.env` existant. Sous Linux, copier
`.env.example` en `.env` et renseigner quatre secrets distincts : mots de passe
d'au moins 12 caractères, clé JWT d'au moins 32 octets.

Trois services persistants : Nginx, application, SQL Server. `db-init` ne fait
qu'initialiser la base et son utilisateur, puis s'arrête. Seul Nginx expose un port.
Les données restent dans le volume `sql-data` après `docker compose down`.
Les mots de passe SQL et administrateur ne sont pas réinitialisés au redémarrage.

Le port est lié à localhost par défaut. Pour un serveur public, configurer le
domaine et HTTPS dans Nginx avant d'exposer le service ; TLS n'est pas fourni ici.

## Développement sans Docker

Java 21 et une **base SQL Server vide dédiée** sont nécessaires. La base locale
historique n'est pas modifiée automatiquement. Depuis `app/` :

```powershell
$env:DB_URL='jdbc:sqlserver://localhost:1433;databaseName=SkyTraceDev;encrypt=false;trustServerCertificate=true'
$env:DB_USERNAME='votre_login'
$env:DB_PASSWORD='votre_mot_de_passe'
$env:JWT_SECRET='une-cle-aleatoire-d-au-moins-32-octets'
$env:ADMIN_PASSWORD='un-mot-de-passe-initial-d-au-moins-12-caracteres'
.\mvnw.cmd spring-boot:run
```

Le compte SQL doit pouvoir créer les tables dans cette base. Flyway applique les
migrations, Hibernate vérifie le schéma. Le premier administrateur est créé
uniquement si la table des utilisateurs est vide. Le frontend est servi par
Spring Boot à http://localhost:8080 ; ne pas ouvrir le HTML directement.

Pour conserver une base ancienne, faire d'abord une sauvegarde et préparer une
migration de ses données vers le nouveau schéma. Ne pas activer automatiquement
`baseline-on-migrate` : cela masquerait les différences de types et contraintes.

## Vérifier

Depuis `app/` :

```powershell
.\mvnw.cmd verify                 # Tests unitaires, API, concurrence et architecture, base H2 isolée
.\mvnw.cmd -Pe2e test            # Ajoute le parcours navigateur avec Edge installé
```

Depuis la racine, pour tester SQL Server Express local avec l'authentification
Windows permettant de créer une base et un login temporaires :

```powershell
.\scripts\Test-SqlServer.ps1 -Suite Api
.\scripts\Test-SqlServer.ps1 -Suite Browser
```

Les deux scripts créent et suppriment uniquement leurs propres données de test.
Les captures sont dans `app/target/e2e/`. Le profil E2E utilise Playwright Java,
sans installation de Node ni dépendance navigateur dans l'application livrée.
Pour tester Nginx localement, définir `NGINX_BINARY` avec le chemin de son exécutable.
La CI prépare Chromium et exécute le parcours E2E contre la stack Compose.

Pour la review : [ordre des commits](docs/REVIEW.md), [architecture](docs/ARCHITECTURE.md),
[preuves et limites](docs/VALIDATION.md).

## Sauvegarder la base Docker

Depuis la racine : `.\scripts\Backup-SkyTrace.ps1`.
Le script copie la sauvegarde sur D: et vérifie sa restauration dans une base temporaire.
Voir le [guide de sauvegarde](docs/BACKUP.md) pour les détails et la conservation.
