[CmdletBinding()]
param(
    [string]$Version = 'latest',
    [string]$DestinationRoot = (Join-Path $PSScriptRoot '..\src\main\resources\static'),
    [switch]$Force
)

$ErrorActionPreference = 'Stop'

function Invoke-Npm {
    param([string[]]$Arguments)

    $output = & npm @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "npm failed: $($output -join [Environment]::NewLine)"
    }

    return ($output -join [Environment]::NewLine).Trim()
}

$resolvedVersion = Invoke-Npm @('view', "pdfjs-dist@$Version", 'version', '--json')
$resolvedVersion = $resolvedVersion.Trim('"', "`r", "`n", ' ')
if ($resolvedVersion -notmatch '^\d+\.\d+\.\d+([-.].+)?$') {
    throw "Unable to resolve a valid pdfjs-dist version from npm: $resolvedVersion"
}

$destinationRootPath = if ([IO.Path]::IsPathRooted($DestinationRoot)) {
    [IO.Path]::GetFullPath($DestinationRoot)
}
else {
    [IO.Path]::GetFullPath((Join-Path (Get-Location).Path $DestinationRoot))
}
$destinationPath = Join-Path $destinationRootPath "pdfjs-assets-$resolvedVersion"
if ((Test-Path $destinationPath) -and -not $Force) {
    throw "Destination already exists: $destinationPath. Use -Force to replace it."
}

$tempPath = Join-Path ([IO.Path]::GetTempPath()) "pdfjs-dist-$([Guid]::NewGuid())"
New-Item -ItemType Directory -Path $tempPath -Force | Out-Null

try {
    New-Item -ItemType Directory -Path $destinationRootPath -Force | Out-Null
    $archivePath = Invoke-Npm @('pack', "pdfjs-dist@$resolvedVersion", '--pack-destination', $tempPath, '--silent')
    $archivePath = $archivePath.Trim()
    if (-not [IO.Path]::IsPathRooted($archivePath)) {
        $archivePath = Join-Path $tempPath $archivePath
    }

    $extractPath = Join-Path $tempPath 'package'
    New-Item -ItemType Directory -Path $extractPath -Force | Out-Null
    tar -xf $archivePath -C $tempPath
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to extract archive: $archivePath"
    }

    if (Test-Path $destinationPath) {
        Remove-Item $destinationPath -Recurse -Force
    }
    New-Item -ItemType Directory -Path $destinationPath -Force | Out-Null

    $files = @(
        'build/pdf.mjs',
        'build/pdf.worker.mjs',
        'web/pdf_viewer.mjs',
        'web/pdf_viewer.css'
    )
    foreach ($relativePath in $files) {
        $sourcePath = Join-Path $extractPath $relativePath
        if (-not (Test-Path $sourcePath -PathType Leaf)) {
            throw "Required PDF.js asset is missing: $relativePath"
        }

        $targetPath = Join-Path $destinationPath ([IO.Path]::GetFileName($relativePath))
        Copy-Item $sourcePath $targetPath
    }

    $imagesPath = Join-Path $extractPath 'web/images'
    if (-not (Test-Path $imagesPath -PathType Container)) {
        throw 'Required PDF.js viewer images directory is missing: web/images'
    }
    Copy-Item $imagesPath (Join-Path $destinationPath 'images') -Recurse

    Write-Host "Downloaded PDF.js $resolvedVersion to $destinationPath"
}
finally {
    if (Test-Path $tempPath) {
        Remove-Item $tempPath -Recurse -Force
    }
}
