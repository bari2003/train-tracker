$toolsDir = Join-Path (Get-Location) ".tools"
if (-not (Test-Path $toolsDir)) {
    New-Item -ItemType Directory -Path $toolsDir -Force | Out-Null
}

$mvnZip = Join-Path $toolsDir "maven.zip"
$mvnTarget = Join-Path $toolsDir "apache-maven"

if (-not (Test-Path (Join-Path $mvnTarget "bin\mvn.cmd"))) {
    Write-Host "Downloading Apache Maven 3.9.9 (approx 9 MB)..."
    Invoke-WebRequest -Uri "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip" -OutFile $mvnZip
    Write-Host "Extracting Maven..."
    Expand-Archive -Path $mvnZip -DestinationPath $toolsDir -Force
    Remove-Item $mvnZip -Force

    $dir = Get-ChildItem -Path $toolsDir -Directory | Where-Object { $_.Name -like "apache-maven-3*" } | Select-Object -First 1
    if ($dir -and ($dir.FullName -ne $mvnTarget)) {
        Rename-Item -Path $dir.FullName -NewName "apache-maven"
    }
}
Write-Host "Maven is ready in $mvnTarget"
