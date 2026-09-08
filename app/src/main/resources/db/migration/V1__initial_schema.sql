-- BIGINT corresponds to Java Long. Names follow Hibernate's snake_case convention.
CREATE TABLE utilisateur (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nom NVARCHAR(100) NOT NULL,
    login NVARCHAR(50) NOT NULL UNIQUE,
    mot_de_passe NVARCHAR(255) NOT NULL,
    role NVARCHAR(30) NOT NULL
);
CREATE TABLE vol (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    numero_vol NVARCHAR(20) NOT NULL UNIQUE,
    origine NVARCHAR(100) NOT NULL,
    destination NVARCHAR(100) NOT NULL,
    date_vol DATETIME2 NOT NULL
);
CREATE TABLE bagage (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code_qr NVARCHAR(50) NOT NULL UNIQUE,
    statut NVARCHAR(50) NOT NULL,
    poids DECIMAL(5,2),
    date_creation DATETIME2,
    nom_passager NVARCHAR(100),
    vol_id BIGINT NOT NULL REFERENCES vol(id)
);
CREATE TABLE scan (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    bagage_id BIGINT NOT NULL REFERENCES bagage(id),
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateur(id),
    point_scan NVARCHAR(100) NOT NULL,
    heure DATETIME2
);
CREATE TABLE anomalie (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    bagage_id BIGINT NOT NULL REFERENCES bagage(id),
    type_anomalie NVARCHAR(100) NOT NULL,
    date_anomalie DATETIME2,
    resolu BIT
);
CREATE INDEX ix_bagage_vol ON bagage(vol_id);
CREATE INDEX ix_scan_bagage_heure ON scan(bagage_id, heure);
CREATE INDEX ix_scan_utilisateur ON scan(utilisateur_id);
CREATE INDEX ix_anomalie_bagage ON anomalie(bagage_id);
