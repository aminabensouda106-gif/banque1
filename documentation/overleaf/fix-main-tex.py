#!/usr/bin/env python3
"""Fix main.tex for pdfLaTeX + Overleaf compatibility."""
import re
from pathlib import Path

TEX = Path(__file__).resolve().parent / "main.tex"
text = TEX.read_text(encoding="utf-8")

# UML: PDF only (pdfLaTeX cannot embed SVG)
text = text.replace(".svg}", ".pdf}")

# Screenshot figures: strip IfFileExists wrappers
pattern = re.compile(
    r"\\begin\{figure\}\[H\]\s*\n"
    r"\s*\\centering\s*\n"
    r"\s*\\IfFileExists\{images/[^}]+\}\{%\s*\n"
    r"\s*\\includegraphics\[([^\]]+)\]\{([^}]+)\}%\s*\n"
    r"\s*\}\{%[^%]*%\s*\n"
    r"\s*\}\s*\n"
    r"\s*\\caption\{([^}]+)\}\s*\n"
    r"\s*\\label\{([^}]+)\}\s*\n"
    r"\\end\{figure\}",
    re.MULTILINE,
)

def repl(m: re.Match) -> str:
    opts, fname, caption, label = m.groups()
    return (
        f"\\begin{{figure}}[H]\n"
        f"    \\centering\n"
        f"    \\includegraphics[{opts}]{{{fname}}}\n"
        f"    \\caption{{{caption}}}\n"
        f"    \\label{{{label}}}\n"
        f"\\end{{figure}}"
    )

text, n = pattern.subn(repl, text)
print(f"Simplified {n} screenshot figures")

TEX.write_text(text, encoding="utf-8")
print("OK:", TEX)
