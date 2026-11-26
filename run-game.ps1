$jdkHome = Join-Path $env:LOCALAPPDATA 'Programs\Temurin-17\jdk-17.0.21.1+2'
if (-not (Test-Path (Join-Path $jdkHome 'bin\java.exe'))) {
    throw 'JDK 17 is missing. Install Temurin 17 or set JAVA_HOME before running the game.'
}
$env:JAVA_HOME = $jdkHome
$env:PATH = "$jdkHome\bin;$env:PATH"
$env:GRADLE_USER_HOME = Join-Path $env:LOCALAPPDATA 'Gradle'
$nativeHome = Join-Path $env:LOCALAPPDATA 'Programs\KotlinNative-1.7.20\compiler\kotlin-native-prebuilt-windows-x86_64-1.7.20'
& (Join-Path $PSScriptRoot 'gradlew.bat') runJvm "-Pkotlin.native.home=$nativeHome"
exit $LASTEXITCODE
