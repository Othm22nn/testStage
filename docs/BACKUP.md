# Sauvegarde de SkyTrace avec Docker

Depuis la racine du projet, dans PowerShell :

```powershell
.\scripts\Backup-SkyTrace.ps1
```

Destination par défaut : `D:\SkyTrace-Backups`. Pour un autre disque :

```powershell
.\scripts\Backup-SkyTrace.ps1 -Destination 'E:\SkyTrace-Backups'
```

Docker et le service `db` doivent être démarrés. Le script effectue une sauvegarde
SQL Server complète `COPY_ONLY` avec checksums, vérifie le fichier, le copie sur
Windows et calcule son SHA-256. Il relit ensuite cette copie dans une base temporaire,
exécute `DBCC CHECKDB`, puis supprime uniquement cette base temporaire.
La base active n'est pas remplacée et la plateforme reste disponible.

Chaque exécution produit un fichier `.bak`, son empreinte `.bak.sha256` et,
si la restauration de contrôle réussit, un rapport `.bak.json`.
Une sauvegarde sans rapport JSON ne doit pas être considérée comme entièrement validée.
Les copies intermédiaires `.bak` dans le volume Docker sont conservées ; aucune
purge automatique des sauvegardes n'est configurée. Le script vise les fichiers
logiques actuels `SkyTraceDB` et `SkyTraceDB_log`.

## Première sauvegarde validée

Le 8 septembre 2026 :
`D:\SkyTrace-Backups\SkyTraceDB-20260908-030234-e2afb381.bak`.
Sauvegarde, VERIFYONLY, restauration depuis la copie Windows et CHECKDB réussis.
Données restaurées : 4 utilisateurs, 1 bagage, 5 scans, 1 anomalie.
Un premier fichier créé à 03:01:38 n'a pas passé la restauration de contrôle
à cause d'une séparation de commandes SQL corrigée depuis ; utiliser celui de 03:02:34.

## Conservation et reprise

Copier les trois fichiers de la sauvegarde validée sur un disque externe ou un
stockage distinct du PC : D: seul ne protège pas contre la perte du poste.
Ces fichiers contiennent les données de la plateforme ; limiter leur accès.
Conserver aussi le projet et son `.env` dans un emplacement privé pour reconstruire
le déploiement. Le `.bak` couvre la base, pas les secrets ni les logins du serveur.

Le script vérifie une restauration isolée. Une restauration de la base active
nécessite une procédure dédiée avec arrêt des écritures et choix explicite du
fichier à restaurer ; ne pas remplacer la base active pour simplement tester une copie.
Aucune sauvegarde planifiée n'a encore été configurée.
