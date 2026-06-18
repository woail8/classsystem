param(
    [int[]]$Ports = @(8080, 5173)
)

$ErrorActionPreference = 'Stop'

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Get-ListeningProcessIds {
    param([int]$Port)

    $netstatLines = netstat -ano -p tcp | Select-String -Pattern "LISTENING"
    $processIds = @()

    foreach ($line in $netstatLines) {
        $text = ($line.ToString() -replace '\s+', ' ').Trim()
        if (-not $text) {
            continue
        }

        $parts = $text.Split(' ')
        if ($parts.Length -lt 5) {
            continue
        }

        $localAddress = $parts[1]
        $pidText = $parts[$parts.Length - 1]

        if ($localAddress -match ":(\d+)$" -and [int]$Matches[1] -eq $Port) {
            $pidValue = 0
            if ([int]::TryParse($pidText, [ref]$pidValue)) {
                $processIds += $pidValue
            }
        }
    }

    return $processIds | Sort-Object -Unique
}

function Stop-ProcessByPort {
    param([int]$Port)

    $pids = Get-ListeningProcessIds -Port $Port
    if (-not $pids -or $pids.Count -eq 0) {
        Write-Host "Port $Port is not listening." -ForegroundColor DarkYellow
        return
    }

    foreach ($pid in $pids) {
        try {
            $process = Get-Process -Id $pid -ErrorAction Stop
            Write-Host "Stopping port $Port -> PID $pid ($($process.ProcessName))"
            Stop-Process -Id $pid -Force -ErrorAction Stop
        }
        catch {
            Write-Warning "Failed to stop PID $pid on port ${Port}: $($_.Exception.Message)"
        }
    }
}

Write-Step 'Stopping campus learning services'
foreach ($port in $Ports) {
    Stop-ProcessByPort -Port $port
}

Write-Host ''
Write-Host 'Stop command finished.' -ForegroundColor Green
