$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$srcDir = Join-Path $scriptDir "src\main\java"
$buildDir = Join-Path $scriptDir "build"
$classesDir = Join-Path $buildDir "classes"
$jarPath = Join-Path $buildDir "libs\chart-renderer.jar"

if (Test-Path $classesDir) {
    Remove-Item -LiteralPath $classesDir -Recurse -Force
}

New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
New-Item -ItemType Directory -Path (Split-Path -Parent $jarPath) -Force | Out-Null

$sourcesFile = Join-Path $buildDir "sources.txt"
Get-ChildItem -Path $srcDir -Recurse -Filter *.java |
    Sort-Object FullName |
    ForEach-Object { $_.FullName } |
    Set-Content -Path $sourcesFile -Encoding ASCII

$sourcesArg = "@$sourcesFile"
javac -encoding UTF-8 -d $classesDir $sourcesArg
jar --create --file $jarPath --main-class io.github.ciscoadmin.tachart.ChartRenderer -C $classesDir .

Write-Host "Built $jarPath"
