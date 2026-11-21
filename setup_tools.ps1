$toolsDir = Join-Path (Get-Location) ".tools"
$jdkZip = Join-Path $toolsDir "jdk17.zip"
$mvnZip = Join-Path $toolsDir "maven.zip"

# Download JDK 17 if not extracted
$jdkTarget = Join-Path $toolsDir "jdk-17"
if (-not (Test-Path $jdkTarget)) {
    Write-Host "Downloading Eclipse Temurin OpenJDK 17..."
    Invoke-WebRequest -Uri "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse" -OutFile $jdkZip
    Write-Host "Extracting JDK 17..."
    Expand-Archive -Path $jdkZip -DestinationPath $toolsDir -Force
    Remove-Item $jdkZip -Force
    $extractedJdk = Get-ChildItem -Path $toolsDir -Directory | Where-Object { $_.Name -like "jdk-17*" } | Select-Object -First 1
    if ($extractedJdk.FullName -ne $jdkTarget) {
        Rename-Item -Path $extractedJdk.FullName -NewName "jdk-17"
    }
}

# Download Maven if not extracted
$mvnTarget = Join-Path $toolsDir "apache-maven"
if (-not (Test-Path $mvnTarget)) {
    Write-Host "Downloading Apache Maven 3.9.9..."
    Invoke-WebRequest -Uri "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip" -OutFile $mvnZip
    Write-Host "Extracting Maven..."
    Expand-Archive -Path $mvnZip -DestinationPath $toolsDir -Force
    Remove-Item $mvnZip -Force
    $extractedMvn = Get-ChildItem -Path $toolsDir -Directory | Where-Object { $_.Name -like "apache-maven-3*" } | Select-Object -First 1
    if ($extractedMvn.FullName -ne $mvnTarget) {
        Rename-Item -Path $extractedMvn.FullName -NewName "apache-maven"
    }
}

Write-Host "Tools setup complete!"
