@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET "MVN_CMD=mvn") ELSE (SET MVN_CMD=%__MVNW_ARG0_NAME__%)
@SET MAVEN_PROJECTBASEDIR=%~dp0
@SET MAVEN_WRAPPER_DIR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper
@SET DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip
@SET MVN_HOME=%MAVEN_WRAPPER_DIR%\apache-maven-3.9.9

IF EXIST "%MVN_HOME%\bin\mvn.cmd" GOTO RunMaven

echo Downloading Apache Maven 3.9.9...
powershell -Command "Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%MAVEN_WRAPPER_DIR%\maven.zip' -UseBasicParsing"
powershell -Command "Expand-Archive -Path '%MAVEN_WRAPPER_DIR%\maven.zip' -DestinationPath '%MAVEN_WRAPPER_DIR%' -Force"
del "%MAVEN_WRAPPER_DIR%\maven.zip"

:RunMaven
IF NOT "%JAVA_HOME%"=="" IF EXIST "%JAVA_HOME%\bin\javac.exe" GOTO JavaOk
IF EXIST "%MAVEN_PROJECTBASEDIR%\.tools\jdk-17\bin\javac.exe" SET "JAVA_HOME=%MAVEN_PROJECTBASEDIR%\.tools\jdk-17"
FOR /D %%D IN ("C:\Program Files\Eclipse Adoptium\jdk-*" "C:\Program Files\Java\jdk-*") DO (
    IF EXIST "%%D\bin\javac.exe" SET "JAVA_HOME=%%D"
)
:JavaOk
IF NOT "%JAVA_HOME%"=="" SET "PATH=%JAVA_HOME%\bin;%PATH%"
SET "PATH=%MVN_HOME%\bin;%PATH%"
CALL "%MVN_HOME%\bin\mvn.cmd" %*
