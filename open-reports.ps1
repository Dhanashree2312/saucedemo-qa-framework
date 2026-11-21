
$reportsPath = Join-Path $PSScriptRoot "target\reports"
$port = 8000

$url = "http://localhost:$port/index.html"

Write-Host "Starting QA Reports Server..." -ForegroundColor Cyan
Write-Host "Consolidated Dashboard: $url"
Write-Host "Web Allure: http://localhost:$port/web/allure-report/"
Write-Host "API Allure: http://localhost:$port/api/allure-report/"
Write-Host "Mobile Allure: http://localhost:$port/mobile/allure-report/"
Write-Host ""
Write-Host "Keep this window open while viewing reports." -ForegroundColor Yellow

Start-Process $url

python -m http.server $port --directory $reportsPath
