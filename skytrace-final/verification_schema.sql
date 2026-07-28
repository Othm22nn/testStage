USE SkyTraceDB;
GO

WITH ColonnesAttendues (table_name, column_name) AS (
    SELECT 'Utilisateur', 'id' UNION ALL
    SELECT 'Utilisateur', 'nom' UNION ALL
    SELECT 'Utilisateur', 'login' UNION ALL
    SELECT 'Utilisateur', 'mot_de_passe' UNION ALL
    SELECT 'Utilisateur', 'role' UNION ALL
    SELECT 'Vol', 'id' UNION ALL
    SELECT 'Vol', 'numero_vol' UNION ALL
    SELECT 'Vol', 'origine' UNION ALL
    SELECT 'Vol', 'destination' UNION ALL
    SELECT 'Vol', 'date_vol' UNION ALL
    SELECT 'Bagage', 'id' UNION ALL
    SELECT 'Bagage', 'code_qr' UNION ALL
    SELECT 'Bagage', 'statut' UNION ALL
    SELECT 'Bagage', 'poids' UNION ALL
    SELECT 'Bagage', 'date_creation' UNION ALL
    SELECT 'Bagage', 'nom_passager' UNION ALL
    SELECT 'Bagage', 'vol_id' UNION ALL
    SELECT 'Scan', 'id' UNION ALL
    SELECT 'Scan', 'bagage_id' UNION ALL
    SELECT 'Scan', 'utilisateur_id' UNION ALL
    SELECT 'Scan', 'point_scan' UNION ALL
    SELECT 'Scan', 'heure' UNION ALL
    SELECT 'Anomalie', 'id' UNION ALL
    SELECT 'Anomalie', 'bagage_id' UNION ALL
    SELECT 'Anomalie', 'type_anomalie' UNION ALL
    SELECT 'Anomalie', 'date_anomalie' UNION ALL
    SELECT 'Anomalie', 'resolu'
)
SELECT table_name AS table_manquante, NULL AS colonne_manquante
FROM (SELECT DISTINCT table_name FROM ColonnesAttendues) t
WHERE OBJECT_ID(t.table_name, 'U') IS NULL
UNION ALL
SELECT table_name, column_name
FROM ColonnesAttendues
WHERE OBJECT_ID(table_name, 'U') IS NOT NULL
  AND COL_LENGTH(table_name, column_name) IS NULL;
GO

-- Un resultat vide signifie que toutes les tables et colonnes attendues existent.
