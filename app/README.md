# SkyTrace

Plateforme de traçabilité des bagages développée avec Spring Boot, SQL Server et un frontend HTML/CSS/JavaScript.

## Prérequis

- Java 21
- SQL Server et SQL Server Management Studio
- Base existante `SkyTraceDB`
- Port 8080 disponible

## Configuration Windows PowerShell

```powershell
$env:DB_USERNAME="ton_login_sql"
$env:DB_PASSWORD="ton_mot_de_passe_sql"
$env:JWT_SECRET="une-cle-secrete-aleatoire-de-32-caracteres-minimum"
```

L’URL SQL Server peut être remplacée avec `DB_URL`. Les origines CORS supplémentaires peuvent être définies avec `CORS_ALLOWED_ORIGINS`.

## Préparation de la base

1. Vérifier que les cinq tables existent : `Utilisateur`, `Vol`, `Bagage`, `Scan`, `Anomalie`.
2. Exécuter `migration_optionnelle_nom_passager.sql`.
3. Exécuter `create_admin.sql` une seule fois. Le script est idempotent et ne recrée pas l’administrateur s’il existe déjà.
4. Remplacer le mot de passe administrateur de démonstration avant toute utilisation réelle.

Hibernate est configuré avec `ddl-auto=validate`. Il vérifie le schéma sans créer ni modifier automatiquement les tables.

## Tests et lancement

Sous Windows :

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Sous Linux ou macOS :

```bash
./mvnw test
./mvnw spring-boot:run
```

Ouvrir ensuite :

```text
http://localhost:8080/
```

Ne pas ouvrir directement le fichier HTML. Le frontend doit être servi par Spring Boot.

## Comptes et rôles

Le premier administrateur est créé avec le script SQL. Il peut ensuite créer :

- un agent d’enregistrement ;
- un agent de manutention ;
- un superviseur ;
- d’autres administrateurs.

## Parcours de test recommandé

1. Se connecter comme administrateur et créer les trois profils internes.
2. Se connecter comme agent d’enregistrement et enregistrer un bagage.
3. Vérifier le code unique et le QR code.
4. Se connecter comme agent de manutention et effectuer les scans successifs.
5. Consulter le suivi public avec le code du bagage.
6. Se connecter comme superviseur, signaler une anomalie puis la résoudre.

## Architecture

```text
src/main/java/com/skytrace/
├── config/       Configuration de sécurité et CORS
├── controller/   API REST
├── dto/          Objets de requête et de réponse
├── entity/       Entités JPA et énumérations
├── exception/    Gestion centralisée des erreurs
├── repository/   Accès SQL Server avec Spring Data JPA
├── security/     JWT et chargement des utilisateurs
└── service/      Logique métier et génération des QR codes
```

Le frontend connecté se trouve dans `src/main/resources/static`.

## Sécurité

- JWT valable 24 heures ;
- mots de passe hachés avec BCrypt ;
- autorisations contrôlées par rôle ;
- secrets fournis par variables d’environnement ;
- CORS limité aux origines configurées ;
- détails techniques des erreurs non exposés au navigateur.

Local excution using XAMPP / WAMP , 