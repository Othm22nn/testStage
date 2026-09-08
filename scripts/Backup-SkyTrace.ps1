param(
    [string]$Destination = 'D:\SkyTrace-Backups'
)
$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$suffix = [Guid]::NewGuid().ToString('N').Substring(0, 8)
$fileName = "SkyTraceDB-$stamp-$suffix.bak"
$containerBackup = "/var/opt/mssql/data/$fileName"
$containerCheck = "/var/opt/mssql/data/check-$fileName"
$checkDb = "SkyTraceRestoreCheck_$suffix"
$checkData = "/var/opt/mssql/data/$checkDb.mdf"
$checkLog = "/var/opt/mssql/data/$checkDb.ldf"
$restoreAttempted = $false

function Invoke-SkyTraceSql([string]$Sql) {
    $Sql | & docker compose exec -T db bash -c 'SQLCMDPASSWORD="$MSSQL_SA_PASSWORD" /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -C -b -r 1'
    if ($LASTEXITCODE -ne 0) { throw 'La commande SQL a echoue.' }
}
function Assert-DockerSuccess {
    if ($LASTEXITCODE -ne 0) { throw 'La commande Docker a echoue.' }
}

Push-Location $repo
try {
    New-Item -ItemType Directory -Path $Destination -Force | Out-Null
    $destinationPath = (Resolve-Path -LiteralPath $Destination).Path
    $backupPath = Join-Path $destinationPath $fileName
    if (Test-Path -LiteralPath $backupPath) { throw 'Le fichier de sauvegarde existe deja.' }

    Invoke-SkyTraceSql @"
BACKUP DATABASE [SkyTraceDB]
TO DISK = N'$containerBackup'
WITH COPY_ONLY, CHECKSUM, STATS = 10;
RESTORE VERIFYONLY FROM DISK = N'$containerBackup' WITH CHECKSUM;
"@
    & docker compose cp "db:$containerBackup" $backupPath
    Assert-DockerSuccess
    $backup = Get-Item -LiteralPath $backupPath
    if ($backup.Length -eq 0) { throw 'La sauvegarde copiee est vide.' }
    $hash = (Get-FileHash -LiteralPath $backupPath -Algorithm SHA256).Hash
    "$hash  $fileName" | Set-Content -LiteralPath "$backupPath.sha256" -Encoding ASCII

    # Restaurer la copie relue depuis Windows, et non le fichier source.
    & docker compose cp $backupPath "db:$containerCheck"
    Assert-DockerSuccess
    & docker compose exec -T -u root db chown mssql:mssql $containerCheck
    Assert-DockerSuccess
    Invoke-SkyTraceSql "IF DB_ID(N'$checkDb') IS NOT NULL THROW 50000, 'La base de controle existe deja.', 1;"
    $restoreAttempted = $true
    Invoke-SkyTraceSql @"
RESTORE DATABASE [$checkDb]
FROM DISK = N'$containerCheck'
WITH MOVE N'SkyTraceDB' TO N'$checkData',
     MOVE N'SkyTraceDB_log' TO N'$checkLog',
     CHECKSUM, RECOVERY;
GO
DBCC CHECKDB (N'$checkDb') WITH NO_INFOMSGS, ALL_ERRORMSGS;
USE [$checkDb];
SET NOCOUNT ON;
SELECT N'utilisateurs' AS categorie, COUNT_BIG(*) AS nombre FROM dbo.utilisateur
UNION ALL SELECT N'bagages', COUNT_BIG(*) FROM dbo.bagage
UNION ALL SELECT N'scans', COUNT_BIG(*) FROM dbo.scan
UNION ALL SELECT N'anomalies', COUNT_BIG(*) FROM dbo.anomalie;
"@
    $report = [ordered]@{
        database = 'SkyTraceDB'
        completedAt = (Get-Date).ToString('o')
        backupFile = $backupPath
        bytes = $backup.Length
        sha256 = $hash
        copyOnly = $true
        backupChecksum = $true
        verifyOnly = 'passed'
        restoredFromWindowsCopy = $true
        checkDb = 'passed'
    }
    $report | ConvertTo-Json | Set-Content -LiteralPath "$backupPath.json" -Encoding UTF8
    Write-Output "Sauvegarde verifiee : $backupPath"
}
finally {
    try {
        if ($restoreAttempted) {
            # Seul le nom unique cree par cette execution peut etre supprime.
            if ($checkDb -notmatch '^SkyTraceRestoreCheck_[0-9a-f]{8}$') { throw 'Nom de controle invalide.' }
            Invoke-SkyTraceSql @"
USE [master];
IF DB_ID(N'$checkDb') IS NOT NULL
BEGIN
    ALTER DATABASE [$checkDb] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [$checkDb];
END;
"@
        }
    }
    finally { Pop-Location }
}
