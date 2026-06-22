#!/usr/bin/env python3
"""Présentation PFA AmanaBank — design professionnel (structure encadrante)."""

from __future__ import annotations

import shutil
import sys
from pathlib import Path

from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE
from pptx.enum.text import MSO_ANCHOR, PP_ALIGN
from pptx.util import Inches, Pt

ROOT = Path(__file__).resolve().parent
IMG = ROOT / "overleaf" / "images"
UML = ROOT / "uml"
UML_UC = UML / "cas-utilisation"
UML_DATA = ROOT / "modele-donnees"
PRESENTATION_DIR = ROOT / "presentation"
OUT_PROJECT = PRESENTATION_DIR / "presentation-PFA-AmanaBank.pptx"
OUT_DOWNLOADS = Path(r"C:\Users\HP\Downloads\PFA.pptx")

# Recherche diagrammes : overleaf/images puis uml/ et modele-donnees/
DIAGRAM_SOURCES = [IMG, UML_UC, UML, UML_DATA]

# ── Palette ────────────────────────────────────────────────────────
EMSI_GREEN = RGBColor(0x00, 0x66, 0x33)
EMSI_GREEN_LIGHT = RGBColor(0x4C, 0xAF, 0x7A)
BROWN = RGBColor(0x4B, 0x36, 0x21)
BROWN_MID = RGBColor(0x6B, 0x53, 0x44)
GOLD = RGBColor(0xA6, 0x89, 0x3A)
GOLD_LIGHT = RGBColor(0xC9, 0xA8, 0x4C)
BEIGE = RGBColor(0xF9, 0xF6, 0xF1)
BEIGE_DARK = RGBColor(0xED, 0xE6, 0xDA)
TEXT = RGBColor(0x3A, 0x2E, 0x22)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)
GRAY = RGBColor(0x88, 0x88, 0x88)

FOOTER = "AmanaBank — BENSOUDA Amina & AFILAL Soraya — EMSI Rabat — 2025/2026"
SLIDE_COUNTER = {"n": 0}

PLAN_SECTIONS = [
    ("01", "Contexte du projet"),
    ("02", "Problématique"),
    ("03", "Objectifs"),
    ("04", "Méthodologie"),
    ("05", "Conception"),
    ("06", "Réalisation / démonstration"),
    ("07", "Résultats obtenus"),
    ("08", "Difficultés rencontrées"),
    ("09", "Conclusion et perspectives"),
]


def pdf_to_png(pdf_path: Path, dpi: int = 300) -> Path | None:
    try:
        import fitz
    except ImportError:
        return None
    if not pdf_path.exists():
        return None
    out = PRESENTATION_DIR / "_tmp" / (pdf_path.stem + ".png")
    out.parent.mkdir(parents=True, exist_ok=True)
    doc = fitz.open(str(pdf_path))
    page = doc[0]
    pix = page.get_pixmap(matrix=fitz.Matrix(dpi / 72, dpi / 72), alpha=False)
    pix.save(str(out))
    doc.close()
    return _crop_whitespace(out)


def svg_to_png(svg_path: Path, target_width_px: int = 2800) -> Path | None:
    """Convertit un SVG PlantUML en PNG haute résolution pour le paysage PPT."""
    try:
        import fitz
    except ImportError:
        return None
    if not svg_path.exists():
        return None
    out = PRESENTATION_DIR / "_tmp" / (svg_path.stem + ".png")
    out.parent.mkdir(parents=True, exist_ok=True)
    try:
        doc = fitz.open(str(svg_path))
        page = doc[0]
        scale = target_width_px / max(page.rect.width, 1)
        pix = page.get_pixmap(matrix=fitz.Matrix(scale, scale), alpha=False)
        pix.save(str(out))
        doc.close()
    except Exception:
        return None
    return _crop_whitespace(out)


def find_diagram_file(name: str) -> Path | None:
    for folder in DIAGRAM_SOURCES:
        for ext in (".svg", ".pdf", ".png"):
            p = folder / (name + ext) if not name.endswith(ext) else folder / name
            if p.exists():
                return p
        if (folder / name).exists():
            return folder / name
    return None


def resolve_image(*candidates: str) -> Path | None:
    raster = {".png", ".jpg", ".jpeg"}
    for name in candidates:
        base = name.replace(".pdf", "").replace(".svg", "").replace(".png", "")
        for folder in DIAGRAM_SOURCES:
            for ext in (".svg", ".pdf", ".png", ".jpg"):
                p = folder / f"{base}{ext}"
                if not p.exists():
                    continue
                if p.suffix.lower() in raster:
                    return p
                if p.suffix.lower() == ".svg":
                    png = svg_to_png(p)
                    if png:
                        return png
                if p.suffix.lower() == ".pdf":
                    png = pdf_to_png(p)
                    if png:
                        return png
    return None


def _crop_whitespace(png_path: Path, padding: int = 14) -> Path | None:
    """Rogne les marges blanches pour maximiser la zone utile du diagramme."""
    try:
        from PIL import Image, ImageChops
    except ImportError:
        return png_path
    img = Image.open(png_path).convert("RGB")
    bg_color = img.getpixel((0, 0))
    bg = Image.new("RGB", img.size, bg_color)
    diff = ImageChops.difference(img, bg)
    diff = diff.point(lambda p: 255 if p > 12 else 0)
    bbox = diff.getbbox()
    if not bbox:
        return png_path
    left, top, right, bottom = bbox
    left = max(0, left - padding)
    top = max(0, top - padding)
    right = min(img.width, right + padding)
    bottom = min(img.height, bottom + padding)
    img.crop((left, top, right, bottom)).save(png_path)
    return png_path


def image_aspect(path: Path) -> float:
    from PIL import Image
    with Image.open(path) as im:
        w, h = im.size
    return w / h if h else 1.0


def fit_in_box(aspect: float, max_w: float, max_h: float) -> tuple[float, float]:
    """Retourne (largeur, hauteur) en pouces pour tenir dans la boîte."""
    w, h = max_w, max_w / aspect
    if h > max_h:
        h = max_h
        w = h * aspect
    return w, h


def add_picture_fitted(slide, path: Path, box_left: float, box_top: float, box_w: float, box_h: float):
    """Insère une image centrée, entièrement visible dans le cadre."""
    aspect = image_aspect(path)
    w, h = fit_in_box(aspect, box_w, box_h)
    left = box_left + (box_w - w) / 2
    top = box_top + (box_h - h) / 2
    slide.shapes.add_picture(str(path), Inches(left), Inches(top), width=Inches(w), height=Inches(h))
    return w, h


def add_image_frame(slide, left: float, top: float, width: float, height: float):
    frame = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(left), Inches(top), Inches(width), Inches(height))
    frame.fill.solid()
    frame.fill.fore_color.rgb = WHITE
    frame.line.color.rgb = BEIGE_DARK
    frame.line.width = Pt(1.25)
    return frame


def new_prs() -> Presentation:
    prs = Presentation()
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    return prs


def blank(prs: Presentation):
    SLIDE_COUNTER["n"] += 1
    return prs.slides.add_slide(prs.slide_layouts[6])


def fill_bg(slide, color: RGBColor):
    slide.background.fill.solid()
    slide.background.fill.fore_color.rgb = color


def add_footer(slide, dark: bool = False):
    color = BEIGE if dark else GRAY
    box = slide.shapes.add_textbox(Inches(0.5), Inches(7.05), Inches(12.3), Inches(0.35))
    tf = box.text_frame
    p = tf.paragraphs[0]
    p.text = FOOTER
    p.font.size = Pt(9)
    p.font.color.rgb = color
    p.alignment = PP_ALIGN.RIGHT


def add_slide_number(slide, num: str, dark: bool = False):
    box = slide.shapes.add_textbox(Inches(12.2), Inches(7.05), Inches(0.8), Inches(0.35))
    tf = box.text_frame
    p = tf.paragraphs[0]
    p.text = num
    p.font.size = Pt(10)
    p.font.color.rgb = GOLD if dark else GOLD
    p.font.bold = True
    p.alignment = PP_ALIGN.RIGHT


def accent_bar(slide, left=0, top=0, height=7.5, width=0.18, color=EMSI_GREEN):
    s = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(left), Inches(top), Inches(width), Inches(height))
    s.fill.solid()
    s.fill.fore_color.rgb = color
    s.line.fill.background()


def gold_rule(slide, left, top, width):
    s = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(left), Inches(top), Inches(width), Inches(0.04))
    s.fill.solid()
    s.fill.fore_color.rgb = GOLD
    s.line.fill.background()


def set_para(p, text, size=18, color=TEXT, bold=False, align=PP_ALIGN.LEFT, space_after=6):
    p.text = text
    p.font.size = Pt(size)
    p.font.color.rgb = color
    p.font.bold = bold
    p.alignment = align
    p.space_after = Pt(space_after)


def add_bullets(slide, items, left, top, width, height, size=17, color=TEXT, level0_bold=False):
    box = slide.shapes.add_textbox(Inches(left), Inches(top), Inches(width), Inches(height))
    tf = box.text_frame
    tf.word_wrap = True
    for i, item in enumerate(items):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        level = 1 if item.startswith("  ") else 0
        p.text = item.strip()
        p.level = level
        p.font.size = Pt(size - (2 if level else 0))
        p.font.color.rgb = color
        p.font.bold = level0_bold and level == 0 and item.endswith(":")
        p.space_after = Pt(5)
    return box


def slide_cover(prs):
    slide = blank(prs)
    fill_bg(slide, BEIGE)
    accent_bar(slide, 0, 0, 7.5, 0.22, EMSI_GREEN)
    accent_bar(slide, 0.22, 0, 7.5, 0.08, EMSI_GREEN_LIGHT)

    # Cercles décoratifs bas droite
    for x, y, r, alpha in [(11.8, 6.2, 1.2, 0.25), (10.5, 5.5, 0.7, 0.15)]:
        c = slide.shapes.add_shape(MSO_SHAPE.OVAL, Inches(x), Inches(y), Inches(r), Inches(r))
        c.fill.solid()
        c.fill.fore_color.rgb = EMSI_GREEN_LIGHT
        c.fill.transparency = alpha
        c.line.fill.background()

    logo = resolve_image("amana-logo.png")
    if logo:
        slide.shapes.add_picture(str(logo), Inches(4.9), Inches(0.55), width=Inches(3.5))
    emsi = resolve_image("emsi-logo.png")
    if emsi:
        slide.shapes.add_picture(str(emsi), Inches(0.55), Inches(0.35), width=Inches(2.4))
    honoris = resolve_image("honoris-logo.png")
    if honoris:
        slide.shapes.add_picture(str(honoris), Inches(10.8), Inches(0.35), height=Inches(0.9))

    lines = [
        ("Projet de Fin d'Année — Ingénierie Informatique et Réseaux", 14, BROWN_MID, False),
        ("Système de gestion d'agence bancaire", 34, BROWN, True),
        ("Application web AmanaBank", 22, GOLD, True),
        ("", 10, TEXT, False),
        ("Réalisé par : BENSOUDA Amina  ·  AFILAL Soraya", 16, TEXT, False),
        ("Encadrée par : Dr. ORCHI HOUDA", 16, TEXT, False),
        ("", 8, TEXT, False),
        ("École Marocaine des Sciences de l'Ingénieur — Rabat", 13, BROWN_MID, False),
        ("Année universitaire 2025/2026", 13, BROWN_MID, True),
    ]
    box = slide.shapes.add_textbox(Inches(0.7), Inches(2.15), Inches(12), Inches(4.2))
    tf = box.text_frame
    tf.vertical_anchor = MSO_ANCHOR.MIDDLE
    for i, (txt, sz, col, b) in enumerate(lines):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        set_para(p, txt, sz, col, b, PP_ALIGN.CENTER, 4)


def slide_plan(prs):
    slide = blank(prs)
    fill_bg(slide, WHITE)
    accent_bar(slide)

    tb = slide.shapes.add_textbox(Inches(0.65), Inches(0.45), Inches(5), Inches(0.8))
    set_para(tb.text_frame.paragraphs[0], "Plan de la présentation", 32, BROWN, True)
    gold_rule(slide, 0.65, 1.15, 2.5)

    tb2 = slide.shapes.add_textbox(Inches(0.65), Inches(1.35), Inches(8), Inches(0.5))
    set_para(tb2.text_frame.paragraphs[0], "15 minutes de présentation  ·  5 minutes de questions", 14, GRAY)

    # Grille 3×3
    cols, rows = 3, 3
    x0, y0 = 0.65, 2.0
    cw, rh, gap_x, gap_y = 3.85, 1.45, 0.35, 0.3
    for idx, (num, label) in enumerate(PLAN_SECTIONS):
        col = idx % cols
        row = idx // cols
        x = x0 + col * (cw + gap_x)
        y = y0 + row * (rh + gap_y)

        card = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(x), Inches(y), Inches(cw), Inches(rh))
        card.fill.solid()
        card.fill.fore_color.rgb = BEIGE if idx % 2 == 0 else WHITE
        card.line.color.rgb = BEIGE_DARK
        card.line.width = Pt(1)

        badge = slide.shapes.add_shape(MSO_SHAPE.OVAL, Inches(x + 0.2), Inches(y + 0.28), Inches(0.65), Inches(0.65))
        badge.fill.solid()
        badge.fill.fore_color.rgb = GOLD
        badge.line.fill.background()
        btf = badge.text_frame
        btf.vertical_anchor = MSO_ANCHOR.MIDDLE
        set_para(btf.paragraphs[0], num, 14, WHITE, True, PP_ALIGN.CENTER, 0)

        lb = slide.shapes.add_textbox(Inches(x + 1.0), Inches(y + 0.2), Inches(cw - 1.15), Inches(rh - 0.3))
        lb.text_frame.vertical_anchor = MSO_ANCHOR.MIDDLE
        set_para(lb.text_frame.paragraphs[0], label, 15, BROWN, True)

    add_footer(slide)


def slide_section_divider(prs, num: str, title: str):
    slide = blank(prs)
    fill_bg(slide, BROWN)

    # Bande dorée diagonale visuelle
    band = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(8.5), Inches(-0.5), Inches(5), Inches(9))
    band.fill.solid()
    band.fill.fore_color.rgb = GOLD
    band.fill.transparency = 0.82
    band.line.fill.background()
    band.rotation = 12

    accent_bar(slide, 0, 0, 7.5, 0.12, GOLD)

    nb = slide.shapes.add_textbox(Inches(0.7), Inches(2.0), Inches(4), Inches(2))
    set_para(nb.text_frame.paragraphs[0], num, 96, GOLD_LIGHT, True, PP_ALIGN.LEFT, 0)

    tb = slide.shapes.add_textbox(Inches(0.7), Inches(4.2), Inches(10), Inches(1.2))
    set_para(tb.text_frame.paragraphs[0], title, 36, WHITE, True)

    add_footer(slide, dark=True)
    add_slide_number(slide, num, dark=True)


def slide_content(prs, section_num: str, title: str, paragraphs: list[str] | None = None, bullets: list[str] | None = None):
    slide = blank(prs)
    fill_bg(slide, WHITE)
    accent_bar(slide)

    header = slide.shapes.add_textbox(Inches(0.65), Inches(0.4), Inches(11), Inches(0.7))
    set_para(header.text_frame.paragraphs[0], f"{section_num}. {title}", 28, BROWN, True)
    gold_rule(slide, 0.65, 1.05, 3.0)

    if paragraphs:
        box = slide.shapes.add_textbox(Inches(0.65), Inches(1.35), Inches(11.8), Inches(5.5))
        tf = box.text_frame
        tf.word_wrap = True
        for i, para in enumerate(paragraphs):
            p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
            set_para(p, para, 17, TEXT, False, PP_ALIGN.JUSTIFY, 14)

    if bullets:
        add_bullets(slide, bullets, 0.65, 1.35, 11.8, 5.5, 17, TEXT)

    add_footer(slide)
    add_slide_number(slide, section_num)


def slide_two_col(prs, section_num: str, title: str, bullets: list[str], image_names: list[str]):
    slide = blank(prs)
    fill_bg(slide, WHITE)
    accent_bar(slide)

    header = slide.shapes.add_textbox(Inches(0.65), Inches(0.4), Inches(11), Inches(0.7))
    set_para(header.text_frame.paragraphs[0], f"{section_num}. {title}", 26, BROWN, True)
    gold_rule(slide, 0.65, 1.05, 2.5)

    add_bullets(slide, bullets, 0.65, 1.3, 5.8, 5.6, 16, TEXT)

    img = resolve_image(*image_names)
    if img:
        frame_left, frame_top, frame_w, frame_h = 6.55, 1.22, 6.35, 5.72
        add_image_frame(slide, frame_left, frame_top, frame_w, frame_h)
        add_picture_fitted(slide, img, frame_left + 0.08, frame_top + 0.08, frame_w - 0.16, frame_h - 0.16)

    add_footer(slide)
    add_slide_number(slide, section_num)


def slide_diagram(prs, section_num: str, title: str, image_names: list[str], caption: str = ""):
    """Slide diagramme UML — image PlantUML réelle, cadrée dans le paysage 16:9."""
    slide = blank(prs)
    fill_bg(slide, WHITE)
    accent_bar(slide, width=0.12)

    header = slide.shapes.add_textbox(Inches(0.5), Inches(0.18), Inches(12.3), Inches(0.5))
    set_para(header.text_frame.paragraphs[0], f"{section_num}. {title}", 20, BROWN, True)
    gold_rule(slide, 0.5, 0.62, 2.0)

    frame_left, frame_top = 0.35, 0.72
    frame_w, frame_h = 12.65, 6.18
    add_image_frame(slide, frame_left, frame_top, frame_w, frame_h)

    img = resolve_image(*image_names)
    if img:
        add_picture_fitted(slide, img, frame_left + 0.1, frame_top + 0.1, frame_w - 0.2, frame_h - 0.2)
    else:
        err = slide.shapes.add_textbox(Inches(2), Inches(3.5), Inches(9), Inches(1))
        set_para(err.text_frame.paragraphs[0], f"Diagramme introuvable : {', '.join(image_names)}", 16, GRAY, False, PP_ALIGN.CENTER)

    if caption:
        cap = slide.shapes.add_textbox(Inches(0.5), Inches(6.95), Inches(12.3), Inches(0.35))
        set_para(cap.text_frame.paragraphs[0], caption, 11, BROWN_MID, False, PP_ALIGN.CENTER)

    add_footer(slide)
    add_slide_number(slide, section_num)


def slide_image(prs, section_num: str, title: str, image_names: list[str], caption: str = ""):
    slide_diagram(prs, section_num, title, image_names, caption)


def slide_stack_cards(prs, section_num: str, title: str, cards: list[tuple[str, list[str]]]):
    slide = blank(prs)
    fill_bg(slide, WHITE)
    accent_bar(slide)

    header = slide.shapes.add_textbox(Inches(0.65), Inches(0.4), Inches(11), Inches(0.7))
    set_para(header.text_frame.paragraphs[0], f"{section_num}. {title}", 26, BROWN, True)
    gold_rule(slide, 0.65, 1.05, 2.5)

    n = len(cards)
    cw = (12.0 - 0.2 * (n - 1)) / n
    for i, (card_title, items) in enumerate(cards):
        x = 0.65 + i * (cw + 0.2)
        card = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(x), Inches(1.35), Inches(cw), Inches(5.5))
        card.fill.solid()
        card.fill.fore_color.rgb = BEIGE if i % 2 == 0 else WHITE
        card.line.color.rgb = GOLD
        card.line.width = Pt(1.5)

        th = slide.shapes.add_textbox(Inches(x + 0.15), Inches(1.5), Inches(cw - 0.3), Inches(0.55))
        set_para(th.text_frame.paragraphs[0], card_title, 15, BROWN, True, PP_ALIGN.CENTER)

        add_bullets(slide, items, x + 0.15, 2.05, cw - 0.3, 4.6, 13, TEXT)

    add_footer(slide)
    add_slide_number(slide, section_num)


def slide_thanks(prs):
    slide = blank(prs)
    fill_bg(slide, BROWN)
    accent_bar(slide, 0, 0, 7.5, 0.12, GOLD)

    logo = resolve_image("amana-logo.png")
    if logo:
        slide.shapes.add_picture(str(logo), Inches(5.0), Inches(1.0), width=Inches(3.3))

    box = slide.shapes.add_textbox(Inches(1), Inches(3.2), Inches(11.3), Inches(3.5))
    tf = box.text_frame
    tf.vertical_anchor = MSO_ANCHOR.MIDDLE
    for i, (txt, sz, b) in enumerate([
        ("Merci pour votre attention", 40, True),
        ("", 12, False),
        ("Questions ?", 28, True),
        ("", 16, False),
        ("BENSOUDA Amina  ·  AFILAL Soraya", 16, False),
        ("Dr. ORCHI HOUDA  —  EMSI Rabat  —  2025/2026", 14, False),
    ]):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        set_para(p, txt, sz, WHITE if sz >= 28 else BEIGE, b, PP_ALIGN.CENTER, 6)


def build():
    SLIDE_COUNTER["n"] = 0
    # Regénérer les PNG depuis les PDF (crop + dpi)
    tmp = PRESENTATION_DIR / "_tmp"
    tmp.mkdir(parents=True, exist_ok=True)
    for old in tmp.glob("*.png"):
        try:
            old.unlink()
        except OSError:
            pass
    prs = new_prs()

    # ── Couverture & plan ──
    slide_cover(prs)
    slide_plan(prs)

    # ── 01 Contexte ──
    slide_section_divider(prs, "01", "Contexte du projet")
    slide_content(
        prs, "01", "Contexte du projet",
        paragraphs=[
            "La digitalisation des services bancaires est aujourd'hui un enjeu majeur. "
            "Au sein d'une agence, les agents traitent quotidiennement l'ouverture de comptes, "
            "les dépôts, retraits, virements et la supervision des opérations.",
            "Dans le cadre de notre formation en Ingénierie Informatique et Réseaux à l'EMSI Rabat, "
            "nous avons développé AmanaBank : un système web simulant la gestion d'une agence bancaire "
            "marocaine fictive, encadré par Dr. ORCHI HOUDA dans le cadre du PFA 2025/2026.",
            "Le projet reproduit les processus quotidiens d'une agence : accueil client, opérations "
            "financières, reporting, administration du personnel et portail client en ligne.",
        ],
    )

    # ── 02 Problématique ──
    slide_section_divider(prs, "02", "Problématique")
    slide_content(
        prs, "02", "Problématique",
        bullets=[
            "Dans de nombreuses agences, la gestion repose encore sur :",
            "  • Fiches clients papier ou fichiers non centralisés",
            "  • Suivi des soldes via tableur sans contrôle automatique",
            "  • Opérations saisies manuellement, sujettes aux erreurs",
            "  • Absence de journal centralisé des actions sensibles",
            "  • Pas d'accès en ligne pour le client",
            "",
            "Conséquences :",
            "  • Incohérences de solde et saisies répétitives",
            "  • Faible traçabilité (agent responsable difficile à identifier)",
            "  • Supervision limitée pour le chef d'agence",
            "",
            "Comment concevoir une application web fiable qui centralise "
            "la gestion de l'agence tout en offrant un espace client sécurisé ?",
        ],
    )

    # ── 03 Objectifs ──
    slide_section_divider(prs, "03", "Objectifs")
    slide_content(
        prs, "03", "Objectifs",
        bullets=[
            "Analyser le besoin à partir du cahier des charges PFA",
            "Modéliser le système (UML, MCD/MLD)",
            "Concevoir une architecture Spring Boot en couches",
            "Réaliser le noyau métier :",
            "  • Clients, comptes, transactions, utilisateurs",
            "  • Audit, reporting, tableaux de bord KPI",
            "Enrichir le périmètre avec des extensions cohérentes :",
            "  • Paiement de factures (LYDEC, ONEE, IAM…)",
            "  • Commande de chéquier (workflow administratif)",
            "  • Centre de notifications et portail client",
            "Valider par 80 tests d'intégration et une démo reproductible",
        ],
    )

    # ── 04 Méthodologie ──
    slide_section_divider(prs, "04", "Méthodologie")
    slide_content(
        prs, "04", "Méthodologie de travail",
        bullets=[
            "Approche itérative et incrémentale inspirée du processus 2TUP :",
            "  • Piste conception : analyse, UML, modèle de données",
            "  • Piste réalisation : modules testables phase par phase",
            "",
            "Phases principales du projet :",
            "  1. Conception UML (cas d'utilisation, classes, séquences)",
            "  2–3. Bootstrap Spring Boot + authentification par rôles",
            "  4–6. Métier cœur (clients, comptes, transactions)",
            "  7–8. Administration + reporting (dashboard, audit)",
            "  10–11. Extensions (paiement facture, chéquier)",
            "  13–14. Canal client (notifications, portail en ligne)",
            "  12. Clôture (manuel, script démo, rapport)",
            "",
            "Chaque phase validée par mvn test avant de passer à la suivante.",
        ],
    )

    # ── 05 Conception (style exemple PFA : texte puis diagramme PlantUML) ──
    slide_section_divider(prs, "05", "Conception")
    slide_stack_cards(
        prs, "05", "Acteurs du système",
        [
            ("Agent bancaire", ["Clients et comptes", "Opérations courantes", "Historique et chéquiers"]),
            ("Chef d'agence", ["Supervision et KPI", "Notifications", "Journal d'audit"]),
            ("Administrateur", ["Gestion du personnel", "Rôles et activation", "Supervision globale"]),
            ("Client bancaire", ["Portail en ligne", "Consultation comptes", "Notifications et chéquiers"]),
        ],
    )
    slide_content(
        prs, "05", "A. Diagramme de cas d'utilisation — clients et comptes",
        bullets=[
            "Acteur principal : Personnel de l'agence (administrateur, agent, chef d'agence)",
            "",
            "Accès : s'authentifier, se déconnecter",
            "Clients : ajouter, modifier, consulter, rechercher, gérer le statut",
            "Comptes : ouvrir, consulter, lister, gérer le statut, commander un chéquier",
        ],
    )
    slide_diagram(
        prs, "05", "Diagramme — clients et comptes",
        ["01-clients-comptes"],
        "Socle métier du cahier des charges (§7.1 – §7.2)",
    )
    slide_content(
        prs, "05", "B. Diagramme de cas d'utilisation — opérations et supervision",
        bullets=[
            "Acteurs : Personnel de l'agence, administrateur, agent, chef d'agence",
            "",
            "Transactions : dépôt, retrait, virement, paiement facture, historique, relevé/reçu",
            "Supervision : tableau de bord, opérations récentes, alertes, notifications",
            "Administration : gestion utilisateurs, journal d'audit, validation opérations sensibles",
            "Règle transversale : contrôle d'éligibilité (solde, statut compte)",
        ],
    )
    slide_diagram(
        prs, "05", "Diagramme — opérations et supervision",
        ["02-operations-supervision"],
        "Transactions, factures, reporting et supervision (§7.3 – §7.5)",
    )
    slide_content(
        prs, "05", "C. Diagramme de cas d'utilisation — portail client",
        bullets=[
            "Acteur : Client bancaire (authentification CIN ou n° client)",
            "",
            "Accès commun /login : s'authentifier, se déconnecter",
            "Espace /portal : tableau de bord, comptes, historique, reçus PDF",
            "Suivi commandes chéquier et notifications",
            "Consultation en lecture seule — aucune opération d'écriture en ligne",
        ],
    )
    slide_diagram(
        prs, "05", "Diagramme — portail client",
        ["03-portail-client"],
        "Espace client en ligne — rôle CLIENT",
    )
    slide_content(
        prs, "05", "D. Diagramme de classes",
        bullets=[
            "Entités centrales : Client, Account, Transaction, User",
            "Extensions : BillProvider, BillPayment, CheckbookOrder, Notification",
            "Traçabilité : AuditLog",
            "Énumérations métier : UserRole, AccountStatus, TransactionType, etc.",
            "Cardinalités R1–R2 : un client possède un ou plusieurs comptes",
        ],
    )
    slide_diagram(
        prs, "05", "Diagramme de classes",
        ["diagramme-classes"],
        "Structure statique JPA — 9 entités persistées",
    )
    slide_content(
        prs, "05", "E. Modèle logique de données (MLD)",
        bullets=[
            "Tables relationnelles PostgreSQL : clients, accounts, transactions, users…",
            "Extensions : bill_providers, bill_payments, checkbook_orders, notifications, audit_logs",
            "Clés étrangères et contraintes d'intégrité référentielle",
            "11 migrations Flyway versionnées (V1 à V11)",
        ],
    )
    slide_diagram(
        prs, "05", "Modèle logique de données (MLD)",
        ["MLD"],
        "Schéma PostgreSQL — base banque_agence",
    )
    slide_content(
        prs, "05", "Règles de gestion clés",
        bullets=[
            "R1–R2 : Un client possède plusieurs comptes ; un compte → un seul client",
            "R3 : Retrait / paiement refusé si solde insuffisant",
            "R4 : Virement uniquement entre comptes actifs",
            "R5–R8 : Traçabilité, comptes bloqués/clôturés, BCrypt, audit",
            "R9–R14 : Facture (référence obligatoire), chéquier (1 seul PENDING, sans impact solde)",
            "",
            "11 diagrammes UML : cas d'utilisation, classes, MCD/MLD, séquences, activité",
        ],
    )

    # ── 06 Réalisation ──
    slide_section_divider(prs, "06", "Réalisation / démonstration")
    slide_stack_cards(
        prs, "06", "Stack technique",
        [
            ("Back-end", ["Java 21", "Spring Boot 3.4", "Spring Security + JPA", "PostgreSQL + Flyway", "OpenPDF"]),
            ("Front-end", ["Thymeleaf MVC", "Bootstrap 5.3", "Charte AmanaBank", "DM Sans, CSS custom"]),
            ("Qualité", ["JUnit 5 — 80 tests", "PlantUML", "Git + seed dev", "Maven"]),
        ],
    )
    slide_content(
        prs, "06", "Architecture en couches",
        bullets=[
            "Monolithe Spring Boot — layered architecture :",
            "",
            "  Navigateur  →  Contrôleurs web  →  Services (@Transactional)",
            "       →  Repositories JPA  →  PostgreSQL",
            "",
            "Transversal : Spring Security, AuditService, NotificationModelAdvice",
            "",
            "9 entités JPA · 7 énumérations · ~40 classes Java · 20+ templates Thymeleaf",
            "Espaces distincts : /dashboard (agence) et /portal (client)",
            "Connexion unifiée /login avec redirection selon le rôle",
        ],
    )
    slide_two_col(
        prs, "06", "Interface — vitrine et connexion",
        [
            "Landing page institutionnelle (/)",
            "Charte marron · beige · or",
            "Connexion unique personnel + client",
            "Redirection automatique :",
            "  /dashboard ou /portal/dashboard",
            "Identifiants démo sur l'écran",
        ],
        ["landing.png"],
    )
    slide_two_col(
        prs, "06", "Interface — espace agence",
        [
            "Dashboard KPI (clients, comptes, volume)",
            "Alertes chéquier cliquables",
            "CRUD clients — recherche multicritère",
            "Opérations : dépôt, retrait, virement",
            "Paiement facture + reçu PDF",
            "Workflow chéquier et notifications",
        ],
        ["dashboard-agent.png"],
    )
    slide_two_col(
        prs, "06", "Interface — portail client",
        [
            "Consultation en lecture seule",
            "Synthèse patrimoine et comptes",
            "Historique + téléchargement PDF",
            "Notifications opérations agence",
            "Suivi commandes de chéquier",
            "Badge « Espace client »",
        ],
        ["portal-dashboard.png"],
    )
    slide_content(
        prs, "06", "Parcours de démonstration live",
        bullets=[
            "Durée suggérée : 5 à 7 minutes",
            "",
            "1. Landing → Connexion agent",
            "2. Dashboard KPI et alertes chéquier",
            "3. Fiche client → compte → dépôt ou virement",
            "4. Paiement facture LYDEC / ONEE → reçu PDF",
            "5. Commande chéquier → notification chef d'agence",
            "6. Connexion client (CIN MB654321 — Moncef Bensouda)",
            "7. Portail : comptes, historique, notifications",
            "",
            "Application : run-dev.bat → http://localhost:8081",
        ],
    )

    # ── 07 Résultats ──
    slide_section_divider(prs, "07", "Résultats obtenus")
    slide_content(
        prs, "07", "Résultats obtenus",
        bullets=[
            "Cahier des charges §7–§12 : entièrement couvert",
            "Extensions livrées : factures, chéquier, notifications, portail client",
            "",
            "Indicateurs qualité :",
            "  • 80 tests d'intégration automatisés (mvn test)",
            "  • 11 migrations Flyway versionnées",
            "  • Jeu de démo reproductible (seed modulaire au démarrage)",
            "  • 11 diagrammes UML + rapport complet",
            "",
            "Objectifs atteints :",
            "  ✓ Centralisation des données clients et transactions",
            "  ✓ Règles métier R1–R14 appliquées et testées",
            "  ✓ Traçabilité (historique + AuditLog)",
            "  ✓ Sécurité BCrypt, rôles, portail lecture seule",
            "  ✓ Interfaces professionnelles par profil utilisateur",
        ],
    )

    # ── 08 Difficultés ──
    slide_section_divider(prs, "08", "Difficultés rencontrées")
    slide_content(
        prs, "08", "Difficultés rencontrées et solutions",
        bullets=[
            "PostgreSQL port 5432 occupé",
            "  → Basculement port 5433, documentation setup-postgresql.sql",
            "",
            "Cohérence transactionnelle des soldes",
            "  → @Transactional + @Version sur Account + tests dédiés",
            "",
            "Authentification dual (personnel + client) sur un seul formulaire",
            "  → UserDetailsServiceImpl composite (SecurityUser / SecurityClient)",
            "",
            "Portail client sur base existante",
            "  → DemoPortalSync au démarrage (profil dev)",
            "",
            "Chéquier vs opération financière",
            "  → Entité CheckbookOrder dédiée, sans impact sur le solde",
            "",
            "Refonte graphique pour une image bancaire professionnelle",
            "  → Charte AmanaBank complète (landing, KPI, sidebar par rôle)",
        ],
    )

    # ── 09 Conclusion ──
    slide_section_divider(prs, "09", "Conclusion et perspectives")
    slide_content(
        prs, "09", "Conclusion et perspectives",
        paragraphs=[
            "AmanaBank répond de manière concrète à la problématique posée : centraliser "
            "la gestion d'une agence bancaire, sécuriser les opérations financières, "
            "garantir la traçabilité et offrir au client un espace de consultation en ligne.",
            "Ce PFA démontre notre maîtrise du cycle complet de développement Java/Spring Boot : "
            "analyse, UML, architecture en couches, sécurisation, tests et interfaces professionnelles.",
            "Perspectives : API REST (Swagger), export PDF des relevés complets, portail enrichi "
            "(alertes SMS/e-mail), authentification 2FA, CI/CD Docker, conformité KYC.",
        ],
    )

    slide_thanks(prs)

    PRESENTATION_DIR.mkdir(parents=True, exist_ok=True)
    staging = OUT_PROJECT.with_suffix(".build.pptx")
    prs.save(str(staging))
    saved_project = OUT_PROJECT
    try:
        if OUT_PROJECT.exists():
            OUT_PROJECT.unlink()
        staging.replace(OUT_PROJECT)
    except OSError:
        saved_project = staging
        print(
            f"WARN: could not overwrite {OUT_PROJECT} (file may be open); "
            f"kept {staging}",
            file=sys.stderr,
        )
    try:
        shutil.copy2(saved_project, OUT_DOWNLOADS)
        print(f"OK downloads: {OUT_DOWNLOADS}")
    except OSError as exc:
        print(f"WARN: copy to downloads failed: {exc}", file=sys.stderr)
    print(f"OK project : {saved_project}")
    print(f"Slides: {len(prs.slides)}")


if __name__ == "__main__":
    try:
        import fitz  # noqa: F401
    except ImportError:
        import subprocess
        subprocess.check_call([sys.executable, "-m", "pip", "install", "pymupdf", "-q"])
    build()
