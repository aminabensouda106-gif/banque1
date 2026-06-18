#!/usr/bin/env python3
"""Package Overleaf : zip main.tex + images/ (+ logo, captures, UML)."""
from __future__ import annotations

import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
REPO = ROOT.parent.parent
OUT_IMAGES = ROOT / "images"
OUT_TEX = ROOT / "main.tex"
OUT_ZIP = ROOT / "amana-report-overleaf.zip"

UML_SRC_DIRS = [
    REPO / "documentation" / "uml",
    REPO / "documentation" / "uml" / "sequence",
    REPO / "documentation" / "uml" / "use-case",
    REPO / "documentation" / "uml" / "class",
    REPO / "documentation" / "uml" / "activity",
    REPO / "documentation" / "uml" / "database",
]
LOGO_SRC = REPO / "src" / "main" / "resources" / "static" / "images" / "amana-logo.png"

UML_SVGS = [
    "01-clients-comptes.svg",
    "02-operations-supervision.svg",
    "03-portail-client.svg",
    "diagramme-classes.svg",
    "MCD.svg",
    "MLD.svg",
    "01-authentification.svg",
    "03-operations-financieres.svg",
    "06-paiement-facture.svg",
    "07-commande-chequier.svg",
    "diagramme-activite-virement.svg",
]


def sync_uml_svgs() -> None:
    OUT_IMAGES.mkdir(parents=True, exist_ok=True)
    for name in UML_SVGS:
        dst = OUT_IMAGES / name
        if dst.exists():
            continue
        for base in UML_SRC_DIRS:
            src = base / name
            if src.exists():
                shutil.copy2(src, dst)
                break


def sync_logo() -> None:
    OUT_IMAGES.mkdir(parents=True, exist_ok=True)
    if LOGO_SRC.exists():
        shutil.copy2(LOGO_SRC, OUT_IMAGES / "amana-logo.png")


def build_zip() -> None:
    if not OUT_TEX.exists():
        raise SystemExit(f"Missing {OUT_TEX}")
    if OUT_ZIP.exists():
        OUT_ZIP.unlink()
    with zipfile.ZipFile(OUT_ZIP, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.write(OUT_TEX, "main.tex")
        readme = ROOT / "README.md"
        if readme.exists():
            zf.write(readme, "README.md")
        for path in sorted(OUT_IMAGES.iterdir()):
            if path.is_file():
                zf.write(path, f"images/{path.name}")


def main() -> None:
    sync_logo()
    sync_uml_svgs()
    build_zip()
    n_img = len(list(OUT_IMAGES.iterdir()))
    print(f"OK: {OUT_TEX.name} ({OUT_TEX.stat().st_size // 1024} KB)")
    print(f"OK: images/ ({n_img} fichiers)")
    print(f"OK: {OUT_ZIP.name} ({OUT_ZIP.stat().st_size // 1024} KB)")


if __name__ == "__main__":
    main()
