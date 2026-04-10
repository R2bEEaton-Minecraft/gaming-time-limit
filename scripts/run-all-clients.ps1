<#
.SYNOPSIS
Runs `runClient` across the repo's supported loader/version targets.

.DESCRIPTION
Processes targets one Minecraft version at a time. For each version, all matched
loaders are launched in parallel. The script waits for every loader in that
version batch to exit before moving to the next version.

Each target gets an archived results folder containing:
- `console.log` with Gradle stdout/stderr
- copied Minecraft `logs/` files that changed during the run
- copied `crash-reports/` files created or updated during the run

The archive root is written under `build/client-run-logs/<timestamp>/`.

.EXAMPLE
.\scripts\run-all-clients.ps1

.EXAMPLE
.\scripts\run-all-clients.ps1 -ListOnly

.EXAMPLE
.\scripts\run-all-clients.ps1 -Loaders forge,fabric -Versions 1.20.1,1.21.11

.EXAMPLE
.\scripts\run-all-clients.ps1 -StartAt forge:1.21.11
#>
param(
    [string[]]$Versions,
    [ValidateSet("fabric", "forge", "neoforge")]
    [string[]]$Loaders,
    [string]$StartAt,
    [switch]$ListOnly
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = Split-Path -Parent $PSScriptRoot
$versionPropertiesDir = Join-Path $repoRoot "versionProperties"
$gradleWrapper = Join-Path $repoRoot "gradlew.bat"
$archiveRoot = Join-Path $repoRoot ("build\client-run-logs\" + (Get-Date -Format "yyyy-MM-dd_HH-mm-ss"))

if (-not (Test-Path $gradleWrapper)) {
    throw "Could not find gradlew.bat at $gradleWrapper"
}

function Get-VersionSortKey {
    param([Parameter(Mandatory = $true)][string]$Version)

    return (($Version.Split(".") | ForEach-Object { "{0:D4}" -f [int]$_ }) -join ".")
}

function Get-VersionMetadata {
    param([Parameter(Mandatory = $true)][string]$Path)

    $properties = @{}
    foreach ($line in Get-Content -Path $Path) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith("#")) {
            continue
        }

        $parts = $trimmed.Split("=", 2)
        if ($parts.Length -ne 2) {
            continue
        }

        $properties[$parts[0].Trim()] = $parts[1].Trim()
    }

    if (-not $properties.ContainsKey("minecraft_version")) {
        throw "Missing minecraft_version in $Path"
    }

    if (-not $properties.ContainsKey("builds_for")) {
        throw "Missing builds_for in $Path"
    }

    [pscustomobject]@{
        Version = $properties["minecraft_version"]
        Loaders = @($properties["builds_for"].Split(",") | ForEach-Object { $_.Trim() } | Where-Object { $_ })
    }
}

function Get-FileSnapshot {
    param([Parameter(Mandatory = $true)][string]$Path)

    $snapshot = @{}
    if (-not (Test-Path $Path)) {
        return $snapshot
    }

    Get-ChildItem -Path $Path -File -Recurse | ForEach-Object {
        $snapshot[$_.FullName] = $_.LastWriteTimeUtc.Ticks
    }

    return $snapshot
}

function Copy-ChangedFiles {
    param(
        [Parameter(Mandatory = $true)][string]$SourceRoot,
        [Parameter(Mandatory = $true)][string]$DestinationRoot,
        [Parameter(Mandatory = $true)][hashtable]$BeforeSnapshot,
        [Parameter(Mandatory = $true)][datetime]$StartedAtUtc
    )

    if (-not (Test-Path $SourceRoot)) {
        return 0
    }

    $copiedCount = 0
    $thresholdTicks = $StartedAtUtc.AddSeconds(-1).Ticks

    Get-ChildItem -Path $SourceRoot -File -Recurse | ForEach-Object {
        $sourceFile = $_.FullName
        $isNew = -not $BeforeSnapshot.ContainsKey($sourceFile)
        $lastWriteTicks = $_.LastWriteTimeUtc.Ticks
        $wasUpdated = $BeforeSnapshot.ContainsKey($sourceFile) -and $lastWriteTicks -gt $BeforeSnapshot[$sourceFile]
        $startedDuringRun = $lastWriteTicks -ge $thresholdTicks

        if ($isNew -or $wasUpdated -or $startedDuringRun) {
            $relativePath = $sourceFile.Substring($SourceRoot.Length).TrimStart('\', '/')
            $destinationFile = Join-Path $DestinationRoot $relativePath
            $destinationDir = Split-Path -Parent $destinationFile

            if (-not (Test-Path $destinationDir)) {
                New-Item -ItemType Directory -Path $destinationDir -Force | Out-Null
            }

            Copy-Item -Path $sourceFile -Destination $destinationFile -Force
            $copiedCount++
        }
    }

    return $copiedCount
}

$preferredLoaderOrder = @{
    fabric = 0
    forge = 1
    neoforge = 2
}

$allVersions = Get-ChildItem -Path $versionPropertiesDir -Filter "*.properties" |
    ForEach-Object { Get-VersionMetadata -Path $_.FullName } |
    Sort-Object -Property @{ Expression = { Get-VersionSortKey $_.Version } }

if ($Versions -and $Versions.Count -gt 0) {
    $wantedVersions = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
    foreach ($version in $Versions) {
        [void]$wantedVersions.Add($version)
    }

    $allVersions = @($allVersions | Where-Object { $wantedVersions.Contains($_.Version) })
}

$requestedLoaders = $null
if ($Loaders -and $Loaders.Count -gt 0) {
    $requestedLoaders = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
    foreach ($loader in $Loaders) {
        [void]$requestedLoaders.Add($loader)
    }
}

$queue = New-Object System.Collections.Generic.List[object]
foreach ($version in $allVersions) {
    $versionLoaders = $version.Loaders | Sort-Object { $preferredLoaderOrder[$_] }
    if ($requestedLoaders) {
        $versionLoaders = @($versionLoaders | Where-Object { $requestedLoaders.Contains($_) })
    }

    foreach ($loader in $versionLoaders) {
        $queue.Add([pscustomobject]@{
            Version = $version.Version
            Loader = $loader
            Label = ("{0}:{1}" -f $loader, $version.Version)
        })
    }
}

if ($StartAt) {
    $startIndex = -1
    for ($i = 0; $i -lt $queue.Count; $i++) {
        if ($queue[$i].Label.Equals($StartAt, [System.StringComparison]::OrdinalIgnoreCase)) {
            $startIndex = $i
            break
        }
    }

    if ($startIndex -lt 0) {
        throw "StartAt '$StartAt' was not found. Use values like fabric:1.21.11 or forge:1.20.1."
    }

    $queue = @($queue[$startIndex..($queue.Count - 1)])
}

if ($queue.Count -eq 0) {
    throw "No loader/version targets matched the current filters."
}

if ($ListOnly) {
    Write-Host "Queued targets:"
    $listGroups = $queue | Group-Object -Property Version | Sort-Object { Get-VersionSortKey $_.Name }
    foreach ($group in $listGroups) {
        Write-Host ("- {0}: {1}" -f $group.Name, (($group.Group | ForEach-Object { $_.Loader }) -join ", "))
    }
    exit 0
}

New-Item -ItemType Directory -Path $archiveRoot -Force | Out-Null

$summaryPath = Join-Path $archiveRoot "summary.txt"
$csvPath = Join-Path $archiveRoot "summary.csv"
$results = New-Object System.Collections.Generic.List[object]
$versionGroups = $queue | Group-Object -Property Version | Sort-Object { Get-VersionSortKey $_.Name }
$totalTargets = $queue.Count
$completedTargets = 0

Write-Host ""
Write-Host "Running $totalTargets client target(s) from $repoRoot" -ForegroundColor Cyan
Write-Host "Archive folder: $archiveRoot" -ForegroundColor Cyan
Write-Host "Execution mode: one Minecraft version at a time, all loaders for that version in parallel."
Write-Host ""

foreach ($group in $versionGroups) {
    $version = $group.Name
    $versionDir = Join-Path $archiveRoot $version
    New-Item -ItemType Directory -Path $versionDir -Force | Out-Null

    Write-Host ("=== Minecraft {0} ===" -f $version) -ForegroundColor Yellow
    Write-Host ("Preparing loaders: {0}" -f (($group.Group | ForEach-Object { $_.Loader }) -join ", "))
    Write-Host ""

    $preparedTargets = New-Object System.Collections.Generic.List[object]
    foreach ($target in $group.Group) {
        $completedTargets++
        $targetDir = Join-Path $versionDir $target.Loader
        New-Item -ItemType Directory -Path $targetDir -Force | Out-Null

        $metadataPath = Join-Path $targetDir "run-metadata.txt"
        $prebuildLog = Join-Path $targetDir "prebuild.log"
        $loaderRunRoot = Join-Path $repoRoot $target.Loader
        $minecraftClientRoot = Join-Path $loaderRunRoot "run\client"
        $logsSource = Join-Path $minecraftClientRoot "logs"
        $crashSource = Join-Path $minecraftClientRoot "crash-reports"
        $prebuildStartedAt = Get-Date
        $prebuildArgs = @("--console=plain", ":$($target.Loader):classes", "-Pmc_ver=$($target.Version)")
        $prebuildCommandLine = ('call "{0}" {1} {2} {3} > "{4}" 2>&1' -f $gradleWrapper, $prebuildArgs[0], $prebuildArgs[1], $prebuildArgs[2], $prebuildLog)

        Set-Content -Path $metadataPath -Value @(
            ("Label={0}" -f $target.Label)
            ("PrebuildStartedAt={0}" -f $prebuildStartedAt.ToString("o"))
            ("PrebuildLog={0}" -f $prebuildLog)
            ("MinecraftClientRoot={0}" -f $minecraftClientRoot)
        )

        Write-Host ("[{0}/{1}] Prebuilding {2}" -f $completedTargets, $totalTargets, $target.Label) -ForegroundColor Cyan
        Write-Host (".\gradlew.bat {0} {1} {2}" -f $prebuildArgs[0], $prebuildArgs[1], $prebuildArgs[2])

        $prebuildProcess = Start-Process -FilePath "cmd.exe" `
            -ArgumentList "/c", $prebuildCommandLine `
            -WorkingDirectory $repoRoot `
            -PassThru `
            -Wait

        Add-Content -Path $metadataPath -Value @(
            ("PrebuildEndedAt={0}" -f (Get-Date).ToString("o"))
            ("PrebuildExitCode={0}" -f $prebuildProcess.ExitCode)
        )

        if ($prebuildProcess.ExitCode -ne 0) {
            $results.Add([pscustomobject]@{
                Label = $target.Label
                Version = $target.Version
                Loader = $target.Loader
                ExitCode = $prebuildProcess.ExitCode
                Folder = $targetDir
            })
            continue
        }

        $preparedTargets.Add([pscustomobject]@{
            Label = $target.Label
            Loader = $target.Loader
            Version = $target.Version
            TargetDir = $targetDir
            MetadataPath = $metadataPath
            LogsSource = $logsSource
            CrashSource = $crashSource
        })
    }

    if ($preparedTargets.Count -eq 0) {
        Write-Host ""
        continue
    }

    Write-Host ""
    Write-Host ("Launching loaders: {0}" -f (($preparedTargets | ForEach-Object { $_.Loader }) -join ", "))
    Write-Host ""

    $running = New-Object System.Collections.Generic.List[object]
    foreach ($target in $preparedTargets) {
        $consoleLog = Join-Path $target.TargetDir "console.log"
        $startedAt = Get-Date
        $startedAtUtc = $startedAt.ToUniversalTime()

        $logsSnapshot = Get-FileSnapshot -Path $target.LogsSource
        $crashSnapshot = Get-FileSnapshot -Path $target.CrashSource

        $commandArgs = @(
            "--console=plain",
            ":$($target.Loader):runClient",
            "-Pmc_ver=$($target.Version)",
            "-x", "compileJava",
            "-x", "processResources",
            "-x", "classes"
        )
        $commandLine = ('call "{0}" {1} {2} {3} {4} {5} {6} {7} {8} > "{9}" 2>&1' -f $gradleWrapper, $commandArgs[0], $commandArgs[1], $commandArgs[2], $commandArgs[3], $commandArgs[4], $commandArgs[5], $commandArgs[6], $commandArgs[7], $consoleLog)

        Add-Content -Path $target.MetadataPath -Value @(
            ("StartedAt={0}" -f $startedAt.ToString("o"))
            ("ConsoleLog={0}" -f $consoleLog)
        )

        Write-Host ("Launching {0}" -f $target.Label) -ForegroundColor Cyan
        Write-Host (".\gradlew.bat {0} {1} {2} {3} {4} {5} {6} {7}" -f $commandArgs[0], $commandArgs[1], $commandArgs[2], $commandArgs[3], $commandArgs[4], $commandArgs[5], $commandArgs[6], $commandArgs[7])

        $process = Start-Process -FilePath "cmd.exe" `
            -ArgumentList "/c", $commandLine `
            -WorkingDirectory $repoRoot `
            -PassThru

        $running.Add([pscustomobject]@{
            Label = $target.Label
            Loader = $target.Loader
            Version = $target.Version
            Process = $process
            StartedAt = $startedAt
            StartedAtUtc = $startedAtUtc
            TargetDir = $target.TargetDir
            MetadataPath = $target.MetadataPath
            LogsSource = $target.LogsSource
            CrashSource = $target.CrashSource
            LogsSnapshot = $logsSnapshot
            CrashSnapshot = $crashSnapshot
        })
    }

    Write-Host ""
    Write-Host ("Waiting for Minecraft {0} loaders to exit before continuing..." -f $version) -ForegroundColor Yellow
    Write-Host ""

    foreach ($entry in $running) {
        $null = $entry.Process.WaitForExit()

        $archivedLogs = Copy-ChangedFiles `
            -SourceRoot $entry.LogsSource `
            -DestinationRoot (Join-Path $entry.TargetDir "logs") `
            -BeforeSnapshot $entry.LogsSnapshot `
            -StartedAtUtc $entry.StartedAtUtc

        $archivedCrashReports = Copy-ChangedFiles `
            -SourceRoot $entry.CrashSource `
            -DestinationRoot (Join-Path $entry.TargetDir "crash-reports") `
            -BeforeSnapshot $entry.CrashSnapshot `
            -StartedAtUtc $entry.StartedAtUtc

        Add-Content -Path $entry.MetadataPath -Value @(
            ("EndedAt={0}" -f (Get-Date).ToString("o"))
            ("ExitCode={0}" -f $entry.Process.ExitCode)
            ("ArchivedLogFiles={0}" -f $archivedLogs)
            ("ArchivedCrashReports={0}" -f $archivedCrashReports)
        )

        $results.Add([pscustomobject]@{
            Label = $entry.Label
            Version = $entry.Version
            Loader = $entry.Loader
            ExitCode = $entry.Process.ExitCode
            Folder = $entry.TargetDir
        })
    }

    Write-Host ""
}

$results |
    Sort-Object @{ Expression = { Get-VersionSortKey $_.Version } }, @{ Expression = { $preferredLoaderOrder[$_.Loader] } } |
    Select-Object Version, Loader, Label, ExitCode, Folder |
    Export-Csv -Path $csvPath -NoTypeInformation

$summaryLines = New-Object System.Collections.Generic.List[string]
$summaryLines.Add(("ArchiveRoot={0}" -f $archiveRoot))
$summaryLines.Add("")

foreach ($result in ($results | Sort-Object @{ Expression = { Get-VersionSortKey $_.Version } }, @{ Expression = { $preferredLoaderOrder[$_.Loader] } })) {
    $summaryLines.Add(("{0} -> exit code {1} -> {2}" -f $result.Label, $result.ExitCode, $result.Folder))
}

Set-Content -Path $summaryPath -Value $summaryLines

$failed = @($results | Where-Object { $_.ExitCode -ne 0 })
$passed = @($results | Where-Object { $_.ExitCode -eq 0 })

Write-Host "Run summary"
Write-Host ("Passed: {0}" -f $passed.Count) -ForegroundColor Green
Write-Host ("Failed: {0}" -f $failed.Count) -ForegroundColor ($(if ($failed.Count -gt 0) { "Red" } else { "Green" }))
Write-Host ("Summary: {0}" -f $summaryPath)
Write-Host ("CSV: {0}" -f $csvPath)

if ($failed.Count -gt 0) {
    Write-Host ""
    Write-Host "Failures:" -ForegroundColor Red
    foreach ($failure in $failed) {
        Write-Host ("- {0} (exit code {1})" -f $failure.Label, $failure.ExitCode)
        Write-Host ("  {0}" -f $failure.Folder)
    }

    exit 1
}
