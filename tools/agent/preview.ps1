<#
.SYNOPSIS  Evaluator G3: asks the Compose Preview sidecar to run `android preview render` and reports the job.
.EXAMPLE   powershell -NoProfile -File tools/agent/preview.ps1 -ConversationId <id> -Reference "gradle://.?:app@debug" -Fqn "a.b.FooKt.FooPreview,a.b.BarKt.BarPreview"
.EXAMPLE   powershell -NoProfile -File tools/agent/preview.ps1 -ConversationId <id> -Rerender
#>
param(
    [Parameter(Mandatory = $true)][string]$ConversationId,
    [string]$Fqn = '',
    [string]$Reference = 'gradle://.?:app@debug',
    [switch]$Rerender
)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$androidHome = if ($env:ANDROID_USER_HOME) { $env:ANDROID_USER_HOME } else { Join-Path $HOME '.android' }
$infoFile = Join-Path $androidHome 'cli/sidecars/compose-preview.json'
if (-not (Test-Path -LiteralPath $infoFile)) { Write-Output 'PREVIEW BLOCKED: sidecar not running. Ask the user to open the "Android Compose Preview" pane.'; exit 2 }
$base = (Get-Content -LiteralPath $infoFile -Raw | ConvertFrom-Json).url
try { Invoke-RestMethod -Uri "$base/api/status" -TimeoutSec 10 | Out-Null }
catch { Write-Output 'PREVIEW BLOCKED: sidecar not responding. Ask the user to open the pane.'; exit 2 }
$headers = @{ 'X-Sidecar-Conversation' = $ConversationId }
if ($Rerender) {
    $res = Invoke-RestMethod -Method Post -Uri "$base/api/render" -Headers $headers -ContentType 'application/json' -Body '{"wait":true}' -TimeoutSec 1800
} else {
    $fqns = @($Fqn -split ',' | ForEach-Object { $_.Trim() } | Where-Object { $_ })
    if ($fqns.Count -eq 0) { Write-Output 'PREVIEW ERROR: -Fqn required for a new session'; exit 1 }
    $body = @{ project_dir = $root; reference = $Reference; fqns = $fqns; render = $true; wait = $true } | ConvertTo-Json -Depth 5
    $res = Invoke-RestMethod -Method Post -Uri "$base/api/session" -Headers $headers -ContentType 'application/json' -Body $body -TimeoutSec 1800
}
$job = if ($res.job) { $res.job } else { $res }
Write-Output "PREVIEW STATUS: $($job.status)"
if ($job.status -ne 'completed') {
    if ($job.error) { Write-Output "ERROR: $($job.error)" }
    $job.output | Select-Object -Last 40
    exit 1
}
$job.images | ForEach-Object { "IMAGE: $_" }
exit 0
