#!/usr/bin/env python3
"""Build single-file Overleaf report + images folder + upload zip."""
from __future__ import annotations

import re
import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
REPO = ROOT.parent.parent
RAPPORT = REPO / "documentation" / "rapport"
OUT_IMAGES = ROOT / "images"
OUT_TEX = ROOT / "main.tex"
OUT_ZIP = ROOT / "amana-report-overleaf.zip"
CAPTURES_SRC = OUT_IMAGES
CAPTURES_DST = RAPPORT / "figures" / "captures"

PNG_CAPTURES = [
    "landing.png",
    "landing-services.png",
    "login.png",
    "dashboard-agent.png",
    "clients-liste.png",
    "compte-detail.png",
    "operations-depot.png",
    "paiement-facture.png",
    "recu-pdf.png",
    "chequier-liste.png",
    "notifications.png",
    "admin-users.png",
    "portal-dashboard.png",
    "portal-notifications.png",
]

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

INCLUDES_ORDER = [
    "includes/page-garde.tex",
    "includes/dedicaces.tex",
    "includes/remerciements.tex",
    "includes/resume.tex",
    "includes/abstract.tex",
]

CHAPTERS_ORDER = [
    "chapitres/introduction.tex",
    "chapitres/chapitre1.tex",
    "chapitres/chapitre2.tex",
    "chapitres/chapitre3.tex",
    "chapitres/chapitre4.tex",
    "chapitres/chapitre5.tex",
    "chapitres/conclusion.tex",
]


def read(rel: str) -> str:
    return (RAPPORT / rel).read_text(encoding="utf-8")


def patch_chapter2(text: str) -> str:
    for svg in UML_SVGS:
        base = svg.replace(".svg", "")
        text = text.replace(f"figures/uml/{base}.pdf", f"images/{svg}")
        text = text.replace(
            f"{{width=\\textwidth}}{{{base}}}",
            f"{{width=\\textwidth}}{{{svg}}}",
        )
        text = text.replace(
            f"{{width=0.95\\textwidth}}{{{base}}}",
            f"{{width=0.95\\textwidth}}{{{svg}}}",
        )
        text = text.replace(
            f"{{width=0.85\\textwidth}}{{{base}}}",
            f"{{width=0.85\\textwidth}}{{{svg}}}",
        )
    return text


def patch_chapter5(text: str) -> str:
    text = text.replace("figures/captures/", "images/")
    text = re.sub(
        r"Les figures correspondent aux captures à déposer dans \\texttt\{figures/captures/\} \(voir README du dossier\)\.",
        "Les figures correspondent aux captures d'écran de l'application (dossier \\texttt{images/}).",
        text,
    )
    return text


def build_preamble() -> str:
    preamble = read("includes/preamble.tex")
    preamble = preamble.replace(
        "\\graphicspath{{figures/}{figures/uml/}{figures/captures/}{figures/architecture/}}",
        "\\graphicspath{{images/}}",
    )
    return preamble


def build_main_tex() -> str:
    parts: list[str] = [
        "% Rapport AmanaBank — fichier unique pour Overleaf\n",
        "% Compiler avec pdfLaTeX. Toutes les images sont dans images/\n",
        "\\documentclass[12pt,a4paper,twoside,openright]{report}\n",
        build_preamble(),
        "\n\\begin{document}\n\n",
        "\\pagenumbering{roman}\n",
    ]
    for inc in INCLUDES_ORDER:
        parts.append(read(inc))
        parts.append("\n")
    parts.extend([
        "\\tableofcontents\n\\clearpage\n",
        "\\listoffigures\n\\clearpage\n",
        "\\listoftables\n\\clearpage\n",
        read("includes/abreviations.tex") + "\n\\clearpage\n\n",
        "\\pagenumbering{arabic}\n",
        "\\setcounter{page}{1}\n\n",
    ])
    for ch in CHAPTERS_ORDER:
        content = read(ch)
        if "chapitre2" in ch:
            content = patch_chapter2(content)
        if "chapitre5" in ch:
            content = patch_chapter5(content)
        parts.append(content)
        parts.append("\n")
    parts.append(read("bibliographie.tex"))
    parts.append("\n\\end{document}\n")
    return "".join(parts)


def copy_uml_svgs() -> None:
    OUT_IMAGES.mkdir(parents=True, exist_ok=True)
    uml_src = RAPPORT / "figures" / "uml"
    for svg in UML_SVGS:
        src = uml_src / svg
        if src.exists():
            shutil.copy2(src, OUT_IMAGES / svg)
        else:
            print(f"WARN: missing UML {svg}")


def sync_captures_to_rapport() -> int:
    CAPTURES_DST.mkdir(parents=True, exist_ok=True)
    copied = 0
    for name in PNG_CAPTURES:
        src = CAPTURES_SRC / name
        if src.exists():
            shutil.copy2(src, CAPTURES_DST / name)
            copied += 1
        else:
            print(f"WARN: missing capture {name}")
    return copied


def build_zip() -> None:
    if OUT_ZIP.exists():
        OUT_ZIP.unlink()
    with zipfile.ZipFile(OUT_ZIP, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.write(OUT_TEX, "main.tex")
        zf.write(ROOT / "README.md", "README.md")
        for path in sorted(OUT_IMAGES.iterdir()):
            if path.is_file():
                zf.write(path, f"images/{path.name}")


def main() -> None:
    copy_uml_svgs()
    OUT_TEX.write_text(build_main_tex(), encoding="utf-8")
    captures = sync_captures_to_rapport()
    build_zip()
    png_count = len(list(OUT_IMAGES.glob("*.png")))
    svg_count = len(list(OUT_IMAGES.glob("*.svg")))
    print(f"OK: {OUT_TEX} ({OUT_TEX.stat().st_size // 1024} KB)")
    print(f"OK: images/ — {svg_count} SVG + {png_count} PNG")
    print(f"OK: synced {captures} captures -> rapport/figures/captures/")
    print(f"OK: {OUT_ZIP} ({OUT_ZIP.stat().st_size // 1024} KB)")


if __name__ == "__main__":
    main()
