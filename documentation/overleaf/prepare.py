#!/usr/bin/env python3
"""Validate image refs in main.tex and build Overleaf zip (PDF/PNG only)."""
from __future__ import annotations

import re
import shutil
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
REPO = ROOT.parent.parent
OUT_IMAGES = ROOT / "images"
OUT_TEX = ROOT / "main.tex"
OUT_ZIP = ROOT / "amana-report-overleaf.zip"
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

UML_SRC_DIRS = [
    REPO / "documentation" / "uml" / "cas-utilisation",
    REPO / "documentation" / "uml",
    REPO / "documentation" / "uml" / "sequence",
    REPO / "documentation" / "uml" / "database",
    REPO / "documentation" / "uml" / "class",
    REPO / "documentation" / "modele-donnees",
]

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


def find_svg(name: str) -> Path | None:
    for base in UML_SRC_DIRS:
        p = base / name
        if p.exists():
            return p
    return None


def svg_to_pdf(svg: Path, pdf: Path) -> bool:
    for cmd in (
        ["inkscape", str(svg), "--export-type=pdf", f"--export-filename={pdf}"],
        ["rsvg-convert", "-f", "pdf", "-o", str(pdf), str(svg)],
    ):
        try:
            subprocess.run(cmd, check=True, capture_output=True)
            if pdf.exists() and pdf.stat().st_size > 0:
                return True
        except (FileNotFoundError, subprocess.CalledProcessError):
            continue
    try:
        import cairosvg

        cairosvg.svg2pdf(url=str(svg), write_to=str(pdf))
        if pdf.exists() and pdf.stat().st_size > 0:
            return True
    except Exception:
        pass
    try:
        from svglib.svglib import svg2rlg
        from reportlab.graphics import renderPDF

        drawing = svg2rlg(str(svg))
        if drawing is not None:
            renderPDF.drawToFile(drawing, str(pdf))
            if pdf.exists() and pdf.stat().st_size > 0:
                return True
    except Exception:
        pass
    if pdf.exists() and pdf.stat().st_size == 0:
        pdf.unlink(missing_ok=True)
    return False


def render_puml_to_svg(puml: Path, out_dir: Path) -> Path | None:
    svg = out_dir / puml.with_suffix(".svg").name
    jar = REPO / "tools" / "plantuml.jar"
    plantuml_cmds: list[list[str]] = []
    if shutil.which("plantuml"):
        plantuml_cmds.append(["plantuml", "-tsvg", "-o", str(out_dir), str(puml)])
    if jar.exists():
        plantuml_cmds.append(["java", "-jar", str(jar), "-tsvg", "-o", str(out_dir), str(puml)])
    for cmd in plantuml_cmds:
        try:
            subprocess.run(cmd, check=True, capture_output=True, cwd=puml.parent)
            if svg.exists():
                return svg
        except (FileNotFoundError, subprocess.CalledProcessError):
            continue
    return None


def render_puml_to_pdf(puml: Path, pdf: Path) -> bool:
    jar = REPO / "tools" / "plantuml.jar"
    out_dir = pdf.parent
    plantuml_cmds: list[list[str]] = []
    if shutil.which("plantuml"):
        plantuml_cmds.append(
            ["plantuml", "-tpdf", "-o", str(out_dir), str(puml)]
        )
    if jar.exists():
        plantuml_cmds.append(
            ["java", "-jar", str(jar), "-tpdf", "-o", str(out_dir), str(puml)]
        )
    for cmd in plantuml_cmds:
        try:
            subprocess.run(cmd, check=True, capture_output=True, cwd=puml.parent)
            generated = out_dir / puml.with_suffix(".pdf").name
            if generated.exists() and generated != pdf:
                generated.replace(pdf)
            if pdf.exists() and pdf.stat().st_size > 0:
                return True
            pdf.unlink(missing_ok=True)
        except (FileNotFoundError, subprocess.CalledProcessError):
            continue
    return False


def ensure_uml_pdfs(force: bool = False) -> None:
    OUT_IMAGES.mkdir(parents=True, exist_ok=True)
    for svg_name in UML_SVGS:
        pdf_name = svg_name.replace(".svg", ".pdf")
        pdf_path = OUT_IMAGES / pdf_name
        src = find_svg(svg_name)
        puml = src.with_suffix(".puml") if src else None
        if puml and puml.exists():
            fresh = render_puml_to_svg(puml, puml.parent)
            if fresh:
                src = fresh
        if not force and pdf_path.exists() and src and pdf_path.stat().st_mtime >= src.stat().st_mtime:
            continue
        if puml and puml.exists() and render_puml_to_pdf(puml, pdf_path):
            print(f"  PDF (PlantUML): {pdf_name}")
            continue
        if src and svg_to_pdf(src, pdf_path):
            print(f"  PDF: {pdf_name}")
            continue
        if src:
            shutil.copy2(src, OUT_IMAGES / svg_name)
            print(f"  WARN: could not convert {svg_name} — copy SVG only")
        else:
            print(f"  WARN: missing {svg_name}")


def sync_logo() -> None:
    OUT_IMAGES.mkdir(parents=True, exist_ok=True)
    if LOGO_SRC.exists():
        shutil.copy2(LOGO_SRC, OUT_IMAGES / "amana-logo.png")
    normalize_pngs()


def normalize_pngs() -> None:
    """Convert palette/RGBA PNGs to RGB for pdfLaTeX compatibility."""
    try:
        from PIL import Image
    except ImportError:
        return

    for path in OUT_IMAGES.glob("*.png"):
        try:
            with Image.open(path) as im:
                if im.mode in ("RGB", "L"):
                    continue
                rgb = im.convert("RGB")
                rgb.save(path, format="PNG", optimize=True)
                print(f"  PNG normalized: {path.name} ({im.mode} -> RGB)")
        except Exception as exc:
            print(f"  WARN: could not normalize {path.name}: {exc}")


def extract_image_refs(tex: str) -> set[str]:
    refs = set(re.findall(r"\\includegraphics(?:\[[^\]]*\])?\{([^}#]+)\}", tex))
    refs.update(re.findall(r"\\includefigure(?:\[[^\]]*\])?\{([^}#]+\.pdf)\}", tex))
    refs.update(re.findall(r"\\includefiagram(?:\[[^\]]*\])?\{([^}#]+\.pdf)\}", tex))
    refs.update(re.findall(r"\\includescreenshot(?:\[[^\]]*\])?\{([^}#]+\.png)\}", tex))
    refs.update(re.findall(r"IfFileExists\{images/([^}#]+)\}", tex))
    return {r.strip() for r in refs if r.strip() and not r.startswith("#")}


def validate(tex: str) -> list[str]:
    missing = []
    for ref in sorted(extract_image_refs(tex)):
        if not (OUT_IMAGES / ref).exists():
            missing.append(ref)
    return missing


def build_zip() -> None:
    if OUT_ZIP.exists():
        OUT_ZIP.unlink()
    with zipfile.ZipFile(OUT_ZIP, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.write(OUT_TEX, "main.tex")
        readme = ROOT / "README.md"
        if readme.exists():
            zf.write(readme, "README.md")
        for path in sorted(OUT_IMAGES.iterdir()):
            if path.suffix.lower() in {".pdf", ".png", ".jpg", ".jpeg"}:
                zf.write(path, f"images/{path.name}")


def main() -> None:
    import sys

    force = "--force" in sys.argv
    if not OUT_TEX.exists():
        raise SystemExit("main.tex missing")
    tex = OUT_TEX.read_text(encoding="utf-8")
    sync_logo()
    print("UML diagrams:")
    ensure_uml_pdfs(force=force)
    missing = validate(tex)
    if missing:
        print("MISSING images:")
        for m in missing:
            print(f"  - {m}")
        raise SystemExit(1)
    build_zip()
    n_pdf = len(list(OUT_IMAGES.glob("*.pdf")))
    n_png = len(list(OUT_IMAGES.glob("*.png")))
    print(f"OK: all {len(extract_image_refs(tex))} image refs found")
    print(f"OK: images/ — {n_pdf} PDF + {n_png} PNG")
    print(f"OK: {OUT_ZIP.name} ({OUT_ZIP.stat().st_size // 1024} KB)")


if __name__ == "__main__":
    main()
