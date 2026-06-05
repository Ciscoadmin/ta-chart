$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$buildDir = Join-Path $scriptDir "build"
$jarPath = Join-Path $buildDir "libs\chart-renderer.jar"
$testClassesDir = Join-Path $buildDir "test-classes"
$testOutputDir = Join-Path $buildDir "test-output"
$testSourcesFile = Join-Path $buildDir "test-sources.txt"

powershell -ExecutionPolicy Bypass -File (Join-Path $scriptDir "build.ps1")

if (Test-Path $testClassesDir) {
    Remove-Item -LiteralPath $testClassesDir -Recurse -Force
}
if (Test-Path $testOutputDir) {
    Remove-Item -LiteralPath $testOutputDir -Recurse -Force
}

New-Item -ItemType Directory -Path $testClassesDir -Force | Out-Null
New-Item -ItemType Directory -Path $testOutputDir -Force | Out-Null

Get-ChildItem -Path (Join-Path $scriptDir "src\test\java") -Recurse -Filter *.java |
    Sort-Object FullName |
    ForEach-Object { $_.FullName } |
    Set-Content -Path $testSourcesFile -Encoding ASCII

$testSourcesArg = "@$testSourcesFile"
javac -encoding UTF-8 -d $testClassesDir $testSourcesArg

function Invoke-RenderCase {
    param(
        [string] $Name,
        [string[]] $ChartArgs
    )

    $output = Join-Path $testOutputDir "$Name.png"
    $renderArgs = @(
        "-Xms16m",
        "-Xmx64m",
        "-Djava.awt.headless=true",
        "-jar",
        $jarPath
    ) + $ChartArgs + @(
        "--width",
        "400",
        "--height",
        "300",
        "--scale",
        "2",
        "--output",
        $output
    )

    & java @renderArgs
    & java -cp $testClassesDir io.github.ciscoadmin.tachart.PngAssertions $output 800 600 1000
}

Invoke-RenderCase "small-failed-label" @("--passed", "31", "--failed", "1", "--title", "Regress feature")
Invoke-RenderCase "all-failed-no-percent" @("--passed", "0", "--failed", "767")
Invoke-RenderCase "empty-placeholder" @("--passed", "0", "--failed", "0")
Invoke-RenderCase "large-failed" @("--passed", "936", "--failed", "7672")

Write-Host "Chart renderer smoke tests passed."
