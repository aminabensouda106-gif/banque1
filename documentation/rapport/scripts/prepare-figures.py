"""Copy UML SVG files into rapport/figures/uml for LaTeX report.
If Inkscape is available, also export PDF versions."""
from __future__ import annotations

import shutil
import subprocess
import sys
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
RAPPORT_DIR = SCRIPT_DIR.parent
DOC_DIR = RAPPORT_DIR.parent
OUT_DIR = RAPPORT_DIR / "figures" / "uml"

MAPPINGS = [
    ("uml/cas-utilisation/01-clients-comptes.svg", "01-clients-comptes"),
    ("uml/cas-utilisation/02-operations-supervision.svg", "02-operations-supervision"),
    ("uml/cas-utilisation/03-portail-client.svg", "03-portail-client"),
    ("uml/diagramme-classes.svg", "diagramme-classes"),
    ("uml/diagramme-activite-virement.svg", "diagramme-activite-virement"),
    ("uml/sequence/01-authentification.svg", "01-authentification"),
    ("uml/sequence/03-operations-financieres.svg", "03-operations-financieres"),
    ("uml/sequence/06-paiement-facture.svg", "06-paiement-facture"),
    ("uml/sequence/07-commande-chequier.svg", "07-commande-chequier"),
    ("modele-donnees/MCD.svg", "MCD"),
    ("modele-donnees/MLD.svg", "MLD"),
]


def inkscape_path() -> str | None:
    for name in ("inkscape", "inkscape.exe"):
        found = shutil.which(name)
        if found:
            return found
    return None


def to_pdf(inkscape: str, src: Path, dst: Path) -> bool:
    try:
        subprocess.run(
            [inkscape, str(src), "--export-type=pdf", f"--export-filename={dst}"],
            check=True,
            capture_output=True,
        )
        return True
    except (subprocess.CalledProcessError, FileNotFoundError):
        return False


def main() -> int:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    inkscape = inkscape_path()
    pdf_count = 0
    svg_count = 0

    for rel_src, base in MAPPINGS:
        src = DOC_DIR / rel_src.replace("/", "\\") if "\\" in str(DOC_DIR) else DOC_DIR / Path(rel_src)
        src = DOC_DIR / Path(rel_src)
        if not src.exists():
            print(f"WARN missing: {src}")
            continue

        pdf_dst = OUT_DIR / f"{base}.pdf"
        svg_dst = OUT_DIR / f"{base}.svg"

        shutil.copy2(src, svg_dst)
        svg_count += 1

        if inkscape and to_pdf(inkscape, src, pdf_dst):
            print(f"PDF OK: {base}.pdf")
            pdf_count += 1
        else:
            print(f"SVG copied: {base}.svg (no PDF - install Inkscape for pdflatex)")

    print(f"\nDone: {svg_count} SVG, {pdf_count} PDF in {OUT_DIR}")
    if pdf_count == 0:
        print("Note: LaTeX report will show placeholders until PDFs exist.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
