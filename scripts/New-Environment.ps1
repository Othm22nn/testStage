$ErrorActionPreference = 'Stop'
$destination = Join-Path (Split-Path $PSScriptRoot -Parent) '.env'
if (Test-Path -LiteralPath $destination) { throw '.env already exists; it was not changed.' }
function New-Secret {
    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    return 'St!' + [Convert]::ToBase64String($bytes)
}
$content = @(
    'HTTP_BIND=127.0.0.1', 'HTTP_PORT=8080', 'ADMIN_LOGIN=admin',
    ('ADMIN_PASSWORD=' + (New-Secret)), ('DB_PASSWORD=' + (New-Secret)),
    ('MSSQL_SA_PASSWORD=' + (New-Secret)), ('JWT_SECRET=' + (New-Secret))
)
[System.IO.File]::WriteAllLines($destination, $content, (New-Object System.Text.UTF8Encoding $false))
Write-Host '.env created. Read ADMIN_PASSWORD locally for the first login. Do not commit this file.'
