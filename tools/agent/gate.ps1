<#
.SYNOPSIS  Evaluator (M_e) wrapper: runs a Gradle gate, stores the full log, prints a compact verdict.
.EXAMPLE   powershell -NoProfile -File tools/agent/gate.ps1 -Gate G2 -Ticket T3.2 -Tasks ":core:domain:test"
#>
param(
    [Parameter(Mandatory = $true)][ValidateSet('G1', 'G2', 'G5')][string]$Gate,
    [string]$Ticket = 'adhoc',
    [string]$Tasks = ''
)
$ErrorActionPreference = 'Continue'
$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Set-Location -LiteralPath $root
$logDir = Join-Path $root 'build/agent-logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$log = Join-Path $logDir ("{0}-{1}-{2}.log" -f $Ticket, $Gate, (Get-Date -Format 'yyyyMMdd-HHmmss'))
$gradlew = if ($env:OS -eq 'Windows_NT') { Join-Path $root 'gradlew.bat' } else { Join-Path $root 'gradlew' }
if (-not (Test-Path -LiteralPath $gradlew)) { Write-Output "GATE $Gate FAIL ($Ticket) gradle wrapper missing"; exit 1 }
[string[]]$taskList = if ($Tasks) { @($Tasks -split '\s+') } else {
    switch ($Gate) {
        'G1' { @('assembleDebug') }
        'G2' { @('testDebugUnitTest', ':core:domain:test') }
        'G5' { @('lintDebug') }
    }
}
$start = Get-Date
& $gradlew $taskList --console=plain *> $log
$code = $LASTEXITCODE
if ($code -eq 0) { Write-Output "GATE $Gate PASS ($Ticket) log=$log"; exit 0 }

Write-Output "GATE $Gate FAIL ($Ticket) exit=$code log=$log"
Write-Output '--- build signals (first 40) ---'
Select-String -LiteralPath $log -Pattern '^e: ', 'error:', 'What went wrong', 'Caused by:', 'Unresolved reference', 'FAILED' |
    Select-Object -First 40 | ForEach-Object { $_.Line }
Write-Output '--- failing tests (first 20) ---'
$dirs = @('app', 'core') | ForEach-Object { Join-Path $root $_ } | Where-Object { Test-Path -LiteralPath $_ }
Get-ChildItem -LiteralPath $dirs -Recurse -Filter 'TEST-*.xml' -ErrorAction SilentlyContinue |
    Where-Object { $_.LastWriteTime -gt $start } |
    ForEach-Object {
        [xml]$x = Get-Content -LiteralPath $_.FullName -Raw
        foreach ($tc in $x.testsuite.testcase) {
            if ($tc.failure) { '{0}.{1}: {2}' -f $tc.classname, $tc.name, (($tc.failure.message -split "`n")[0]) }
        }
    } | Select-Object -First 20
exit 1
