# Guide de lancement et de test de SkyTrace

## 1. Vérifier les prérequis

Installez Java 21 et vérifiez :

```powershell
java -version
```

La première ligne doit indiquer la version 21. SQL Server doit être démarré et la base `SkyTraceDB` accessible depuis SSMS.

## 2. Vérifier la base

Dans SQL Server Management Studio :

1. ouvrez `verification_schema.sql` et exécutez-le ;
2. si le résultat mentionne seulement `nom_passager`, exécutez `migration_optionnelle_nom_passager.sql` ;
3. recommencez la vérification : le résultat doit être vide ;
4. exécutez `create_admin.sql` pour créer le premier administrateur.

Le compte de développement est `admin` / `admin123`. Changez ce mot de passe avant toute utilisation réelle.

## 3. Configurer PowerShell

Ouvrez PowerShell dans le dossier du projet :

```powershell
$env:DB_USERNAME="votre_login_sql"
$env:DB_PASSWORD="votre_mot_de_passe_sql"
$env:JWT_SECRET="une-cle-secrete-aleatoire-de-32-caracteres-minimum"
```

Pour une instance SQL Server différente :

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=SkyTraceDB;encrypt=false;trustServerCertificate=true"
```

## 4. Exécuter les tests

```powershell
.\mvnw.cmd test
```

Le résultat attendu est `BUILD SUCCESS`.

## 5. Démarrer l’application

```powershell
.\mvnw.cmd spring-boot:run
```

Attendez le message indiquant que Tomcat a démarré sur le port 8080, puis ouvrez :

```text
http://localhost:8080/
```

N’ouvrez pas le fichier HTML directement depuis l’Explorateur Windows.

## 6. Tester les profils

### Administrateur

1. Connectez-vous avec `admin` / `admin123`.
2. Créez trois comptes avec des mots de passe d’au moins six caractères :
   - `agent_enr` : agent d’enregistrement ;
   - `agent_man` : agent de manutention ;
   - `superviseur` : superviseur.
3. Vérifiez que les comptes apparaissent dans le tableau.

### Agent d’enregistrement

1. Déconnectez-vous puis connectez-vous avec `agent_enr`.
2. Saisissez le passager, le numéro, l’origine, la destination, la date du vol et le poids.
3. Enregistrez le bagage.
4. Notez le code `BAG-xxxx-xxxx` et vérifiez l’affichage du QR code.

### Agent de manutention

1. Connectez-vous avec `agent_man`.
2. Sélectionnez le bagage.
3. Cliquez successivement sur le bouton de scan.
4. Vérifiez la progression : dépôt, tri, chargement, déchargement et livraison.
5. Un scan supplémentaire après la livraison doit être refusé.

### Passager

1. Ouvrez l’onglet Passager, même sans connexion.
2. Saisissez le code du bagage.
3. Vérifiez le vol, la destination, le statut et l’historique.

### Superviseur

1. Connectez-vous avec le compte superviseur.
2. Vérifiez les statistiques et le tableau des bagages.
3. Signalez une anomalie.
4. Vérifiez l’alerte dans le suivi passager.
5. Résolvez l’anomalie et vérifiez que l’alerte disparaît.

## 7. Problèmes fréquents

- `Java version incorrecte` : installez Java 21 et corrigez `JAVA_HOME`.
- `Login failed for user` : vérifiez `DB_USERNAME`, `DB_PASSWORD` et le mode d’authentification SQL Server.
- `Connection refused` : démarrez SQL Server et vérifiez le port dans `DB_URL`.
- `Schema-validation missing column` : exécutez la migration indiquée ou corrigez la structure de la base.
- `JWT_SECRET doit contenir au moins 32 octets` : utilisez une clé plus longue.
- `Port 8080 already in use` : arrêtez l’application utilisant ce port ou définissez `SERVER_PORT` après adaptation de la configuration.
- Erreur 401/403 après une ancienne connexion : déconnectez-vous ou videz `sessionStorage`, puis reconnectez-vous.
