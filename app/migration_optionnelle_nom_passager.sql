-- =====================================================================
-- Script A EXECUTER pour que le prototype frontend puisse enregistrer
-- et afficher le nom du passager avec chaque bagage.
--
-- Le backend mappe maintenant la colonne nom_passager. Avec
-- spring.jpa.hibernate.ddl-auto=validate, l'application refusera de demarrer
-- tant que cette colonne n'existe pas dans SQL Server.
-- =====================================================================

USE SkyTraceDB;
GO

IF COL_LENGTH('Bagage', 'nom_passager') IS NULL
BEGIN
    ALTER TABLE Bagage
    ADD nom_passager NVARCHAR(100) NULL;
END
GO
