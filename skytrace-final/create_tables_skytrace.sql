USE SkyTraceDB;
GO

-- Table Utilisateur
CREATE TABLE Utilisateur (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nom NVARCHAR(100) NOT NULL,
    login NVARCHAR(50) NOT NULL UNIQUE,
    mot_de_passe NVARCHAR(255) NOT NULL,
    role NVARCHAR(30) NOT NULL
);
GO

-- Table Vol
CREATE TABLE Vol (
    id INT IDENTITY(1,1) PRIMARY KEY,
    numero_vol NVARCHAR(20) NOT NULL,
    origine NVARCHAR(100) NOT NULL,
    destination NVARCHAR(100) NOT NULL,
    date_vol DATETIME2 NOT NULL
);
GO

-- Table Bagage
CREATE TABLE Bagage (
    id INT IDENTITY(1,1) PRIMARY KEY,
    code_qr NVARCHAR(50) NOT NULL UNIQUE,
    statut NVARCHAR(50) NOT NULL,
    poids DECIMAL(5,2) NULL,
    date_creation DATETIME2 NULL,
    nom_passager NVARCHAR(100) NULL,
    vol_id INT NOT NULL,
    CONSTRAINT FK_Bagage_Vol FOREIGN KEY (vol_id) REFERENCES Vol(id)
);
GO

-- Table Scan
CREATE TABLE Scan (
    id INT IDENTITY(1,1) PRIMARY KEY,
    bagage_id INT NOT NULL,
    utilisateur_id INT NOT NULL,
    point_scan NVARCHAR(100) NOT NULL,
    heure DATETIME2 NULL,
    CONSTRAINT FK_Scan_Bagage FOREIGN KEY (bagage_id) REFERENCES Bagage(id),
    CONSTRAINT FK_Scan_Utilisateur FOREIGN KEY (utilisateur_id) REFERENCES Utilisateur(id)
);
GO

-- Table Anomalie
CREATE TABLE Anomalie (
    id INT IDENTITY(1,1) PRIMARY KEY,
    bagage_id INT NOT NULL,
    type_anomalie NVARCHAR(100) NOT NULL,
    date_anomalie DATETIME2 NULL,
    resolu BIT NULL,
    CONSTRAINT FK_Anomalie_Bagage FOREIGN KEY (bagage_id) REFERENCES Bagage(id)
);
GO
