-- Insert an initial administrator user.
-- The BCrypt hash below is for the development password admin123 (cost 12).
-- Never commit a real password or production hash. Generate a unique hash for production.
IF NOT EXISTS (SELECT 1 FROM Utilisateur WHERE login = 'admin')
BEGIN
    INSERT INTO Utilisateur (nom, login, mot_de_passe, role)
    VALUES ('Administrateur RAM', 'admin', '$2a$12$wLhV3eTdcMXsSk0pJpHcnuNxmZ7U9PIR6BmYb4Wrd1u5JdYqpdObK', 'ADMINISTRATEUR');
END
GO
