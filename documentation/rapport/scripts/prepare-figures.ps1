# Convertit les diagrammes SVG du projet en PDF pour LaTeX
# Usage: .\prepare-figures.ps1

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RapportDir = Split-Path -Parent $ScriptDir
$DocDir = Split-Path -Parent $RapportDir
$OutDir = Join-Path $RapportDir "figures\uml"

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

$Mappings = @(
    @{ Src = "uml\cas-utilisation\01-clients-comptes.svg"; Dst = "01-clients-comptes.pdf" },
    @{ Src = "uml\cas-utilisation\02-operations-supervision.svg"; Dst = "02-operations-supervision.pdf" },
    @{ Src = "uml\cas-utilisation\03-portail-client.svg"; Dst = "03-portail-client.pdf" },
    @{ Src = "uml\diagramme-classes.svg"; Dst = "diagramme-classes.pdf" },
    @{ Src = "uml\diagramme-activite-virement.svg"; Dst = "diagramme-activite-virement.pdf" },
    @{ Src = "uml\sequence\01-authentification.svg"; Dst = "01-authentification.pdf" },
    @{ Src = "uml\sequence\03-operations-financieres.svg"; Dst = "03-operations-financieres.pdf" },
    @{ Src = "uml\sequence\06-paiement-facture.svg"; Dst = "06-paiement-facture.pdf" },
    @{ Src = "uml\sequence\07-commande-chequier.svg"; Dst = "07-commande-chequier.pdf" },
    @{ Src = "modele-donnees\MCD.svg"; Dst = "MCD.pdf" },
    @{ Src = "modele-donnees\MLD.svg"; Dst = "MLD.pdf" }
)

$Inkscape = Get-Command inkscape -ErrorAction SilentlyContinue

foreach ($m in $Mappings) {
    $srcPath = Join-Path $DocDir $m.Src
    $dstPath = Join-Path $OutDir $m.Dst

    if (-not (Test-Path $srcPath)) {
        Write-Warning "Source introuvable: $srcPath"
        continue
    }

    if ($Inkscape) {
        Write-Host "Conversion: $($m.Dst)"
        & inkscape $srcPath --export-type=pdf --export-filename=$dstPath 2>$null
        if ($LASTEXITCODE -ne 0) {
            & inkscape $srcPath --export-pdf=$dstPath
        }
    } else {
        Write-Warning "Inkscape absent - copie SVG: $($m.Dst -replace '\.pdf$','.svg')"
        Copy-Item $srcPath (Join-Path $OutDir ($m.Dst -replace '\.pdf$','.svg')) -Force
    }
}

if (-not $Inkscape) {
    Write-Host ""
    Write-Host "Installez Inkscape puis relancez, OU exportez manuellement en PDF vers:"
    Write-Host "  $OutDir"
}

Write-Host "Termine."
