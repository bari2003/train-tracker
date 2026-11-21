$ErrorActionPreference = "Stop"
$projectDir = $PSScriptRoot

if (Test-Path "C:\jdk-27.0.1.1\bin\javac.exe") {
    $env:JAVA_HOME = "C:\jdk-27.0.1.1"
} elseif (Test-Path (Join-Path $projectDir ".tools\jdk-17\bin\javac.exe")) {
    $env:JAVA_HOME = Join-Path $projectDir ".tools\jdk-17"
}

$env:M2_HOME = Join-Path $projectDir ".tools\apache-maven"
$env:PATH = "$env:JAVA_HOME\bin;$env:M2_HOME\bin;$env:PATH"

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host " Real-Time Train Movement Tracker - Startup" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "Java Home : $env:JAVA_HOME"
Write-Host "Maven Home: $env:M2_HOME`n"

& java -version
if ($LASTEXITCODE -ne 0) {
    Write-Error "Java not found in $env:JAVA_HOME"
    exit 1
}

Write-Host "`nStarting application with embedded H2 + Live RailRadar API..." -ForegroundColor Green
Write-Host "Dashboard URL: http://localhost:8080`n" -ForegroundColor Yellow

$mvnCmd = Join-Path $env:M2_HOME "bin\mvn.cmd"
& $mvnCmd spring-boot:run -Dspring-boot.run.profiles=h2
