# Plan de remise en état

Chaque commit traite une étape indépendante ; les raisons sont dans son message.

1. Préserver les changements locaux existants.
2. Nettoyer le dépôt et rendre Maven reproductible.
3. Fiabiliser le démarrage, le schéma SQL et les erreurs HTTP.
4. Organiser le backend par domaine et séparer les ressources du frontend.
5. Vérifier les parcours métier, les permissions et le navigateur.
6. Livrer Docker Compose + Nginx, la CI et un guide de review court.

Architecture : un monolithe Spring Boot, SQL Server, HTML/CSS/JavaScript natifs.
Pas de microservices, broker, cache distribué ou framework frontend ajouté.

La base existante n'est pas utilisée pour les tests. Les vérifications exécutées
et les limites de l'environnement sont consignées dans `VALIDATION.md`.
