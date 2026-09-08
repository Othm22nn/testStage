#!/bin/bash
set -euo pipefail
# Escape a SQL string literal; the application never receives the sa credential.
escaped_password=$(printf '%s' "$DB_PASSWORD" | sed "s/'/''/g")
/opt/mssql-tools18/bin/sqlcmd -S db -U sa -C -b <<SQL
IF DB_ID(N'SkyTraceDB') IS NULL CREATE DATABASE SkyTraceDB;
GO
IF SUSER_ID(N'skytrace') IS NULL
    CREATE LOGIN skytrace WITH PASSWORD=N'$escaped_password', CHECK_POLICY=ON;
GO
USE SkyTraceDB;
GO
IF USER_ID(N'skytrace') IS NULL
    CREATE USER skytrace FOR LOGIN skytrace WITH DEFAULT_SCHEMA=dbo;
ALTER ROLE db_datareader ADD MEMBER skytrace;
ALTER ROLE db_datawriter ADD MEMBER skytrace;
ALTER ROLE db_ddladmin ADD MEMBER skytrace;
GO
SQL
