param(
    [switch]$SkipBuild,
    [switch]$NoBrowser,
    [switch]$CheckOnly
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSCommandPath
$serverDir = Join-Path $projectRoot 'server'

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Assert-Command {
    param(
        [string]$CommandName,
        [string]$InstallHint
    )

    if (-not (Get-Command $CommandName -ErrorAction SilentlyContinue)) {
        throw "Missing required command: $CommandName. $InstallHint"
    }
}

function Test-PortListening {
    param([int]$Port)

    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $asyncResult = $client.BeginConnect('127.0.0.1', $Port, $null, $null)
        $connected = $asyncResult.AsyncWaitHandle.WaitOne(1000, $false)
        if (-not $connected) {
            return $false
        }

        $client.EndConnect($asyncResult)
        return $true
    }
    catch {
        return $false
    }
    finally {
        $client.Dispose()
    }
}

function Test-MySqlPort {
    return (Test-PortListening -Port 3306)
}

Write-Step 'Checking local environment'
Assert-Command -CommandName 'node' -InstallHint 'Please install Node.js 18+ and ensure it is added to PATH.'
Assert-Command -CommandName 'npm' -InstallHint 'Please install npm and ensure it is added to PATH.'
Assert-Command -CommandName 'mvn' -InstallHint 'Please install Maven 3.8+ and ensure it is added to PATH.'

$mysqlReady = Test-MySqlPort
if (-not $mysqlReady) {
    Write-Warning 'MySQL port 3306 is not detected. Please make sure MySQL is started and the campus_learning database is available.'
}

if ($CheckOnly) {
    Write-Host 'Environment check finished.' -ForegroundColor Green
    exit 0
}

if (-not (Test-Path (Join-Path $projectRoot 'node_modules'))) {
    Write-Step 'Installing frontend dependencies'
    Push-Location $projectRoot
    try {
        npm install
    }
    finally {
        Pop-Location
    }
}

if (-not $SkipBuild) {
    Write-Step 'Building frontend and syncing static files'
    Push-Location $projectRoot
    try {
        npm run build:cs
    }
    finally {
        Pop-Location
    }
}

if (Test-PortListening -Port 8080) {
    Write-Warning 'Port 8080 is already in use. If the campus service is already running, you can open it directly.'
    if (-not $NoBrowser) {
        Start-Process 'http://localhost:8080'
    }
    exit 0
}

Write-Step 'Starting backend service'
$escapedServerDir = $serverDir -replace "'", "''"
$startCommand = "Set-Location -LiteralPath '$escapedServerDir'; mvn spring-boot:run"
Start-Process -FilePath 'powershell.exe' `
    -ArgumentList @('-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', $startCommand) `
    -WorkingDirectory $serverDir | Out-Null

Start-Sleep -Seconds 5

if (-not $NoBrowser) {
    Start-Process 'http://localhost:8080'
}

Write-Host ''
Write-Host 'Campus learning service startup command has been launched.' -ForegroundColor Green
Write-Host 'Open http://localhost:8080 in your browser if it does not open automatically.'
