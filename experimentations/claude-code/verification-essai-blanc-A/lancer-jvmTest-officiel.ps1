# Verification independante de l'essai a blanc Claude Code (strategie A)
# Mesure officielle : clone propre de exp/claude-code-A + 36 tests de l'auteur dans reference/, puis gradlew clean jvmTest.
# A lancer depuis PowerShell :  powershell -ExecutionPolicy Bypass -File .\lancer-jvmTest-officiel.ps1
$ErrorActionPreference = "Stop"
$ici     = $PSScriptRoot
$projet  = Split-Path -Parent $ici
$depot   = Join-Path $projet "depot-github"
$auteur  = "C:\Users\abdul\Desktop\Projet\Candy-Crush-Clone"
$copie   = Join-Path $env:TEMP "verif-claude-code-A"
$sortie  = Join-Path $ici "resultats-jvmTest.txt"

$env:JAVA_HOME = (Get-ChildItem "$HOME\.jdks" -Directory | Where-Object Name -like "*17*" | Select-Object -First 1).FullName
Write-Host "JAVA_HOME = $env:JAVA_HOME"

# 1. Clone propre de la branche (sans deps/, build/, .gradle/)
if (Test-Path $copie) { Remove-Item $copie -Recurse -Force }
git clone --quiet --branch exp/claude-code-A $depot $copie
$commit = git -C $copie rev-parse --short HEAD

# 2. Tests de l'auteur (branche master, 36 tests) copies tels quels dans reference/
$brancheAuteur = git -C $auteur rev-parse --abbrev-ref HEAD
$commitAuteur  = git -C $auteur rev-parse --short HEAD
$ref = Join-Path $copie "src\commonTest\kotlin\reference"
New-Item -ItemType Directory -Force $ref | Out-Null
Copy-Item "$auteur\src\commonTest\kotlin\*.kt" $ref

# 3. Compilation et tests
Push-Location $copie
$debut = Get-Date
.\gradlew.bat clean jvmTest --console=plain *> (Join-Path $ici "log-jvmTest.txt")
$code = $LASTEXITCODE
$duree = (Get-Date) - $debut
Pop-Location

# 4. Comptage a partir des rapports XML
$lignes = @("Essai : exp/claude-code-A, commit $commit", "Tests auteur : branche $brancheAuteur, commit $commitAuteur", "gradlew clean jvmTest : code retour $code, duree $([int]$duree.TotalSeconds) s", "")
$xml = Get-ChildItem (Join-Path $copie "build\test-results\jvmTest") -Filter *.xml -ErrorAction SilentlyContinue
$tot = @{ ia = @(0,0,0); ref = @(0,0,0) }
foreach ($f in $xml) {
  [xml]$x = Get-Content $f.FullName
  $s = $x.testsuite
  $cle = if ($s.name -like "j4k.*") { "ia" } else { "ref" }
  $tot[$cle][0] += [int]$s.tests; $tot[$cle][1] += [int]$s.failures; $tot[$cle][2] += [int]$s.errors
  foreach ($t in $s.testcase) { if ($t.failure -or $t.error) { $lignes += "ECHEC $($s.name).$($t.name)" } }
}
$lignes += "Tests de l'auteur (reference) : $($tot.ref[0]) lances, $($tot.ref[1]) echecs, $($tot.ref[2]) erreurs (attendu : 36)"
$lignes += "Tests de l'outil              : $($tot.ia[0]) lances, $($tot.ia[1]) echecs, $($tot.ia[2]) erreurs (annonce : 67)"
if ($code -ne 0 -and $xml.Count -eq 0) { $lignes += "La compilation a echoue : voir log-jvmTest.txt (CA-04 non atteint si l'erreur vient de reference/)" }
$lignes | Set-Content $sortie -Encoding UTF8
$lignes | ForEach-Object { Write-Host $_ }
Write-Host "Resultat ecrit dans $sortie"
