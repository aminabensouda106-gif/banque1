#!/usr/bin/env python3
"""Regenerate .svg files next to each .puml under documentation/."""
from __future__ import annotations

import sys
import zlib
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parents[1]
DOC = ROOT / "documentation"
SERVER_SVG = "https://www.plantuml.com/plantuml/svg/"
SERVER_SVG_POST = "https://www.plantuml.com/plantuml/svg"


def encode6bit(b: int) -> str:
    if b < 10:
        return chr(48 + b)
    b -= 10
    if b < 26:
        return chr(65 + b)
    b -= 26
    if b < 26:
        return chr(97 + b)
    b -= 26
    if b == 0:
        return "-"
    if b == 1:
        return "_"
    return "?"


def encode3bytes(b1: int, b2: int, b3: int) -> str:
    c1 = b1 >> 2
    c2 = ((b1 & 0x3) << 4) | (b2 >> 4)
    c3 = ((b2 & 0xF) << 2) | (b3 >> 6)
    c4 = b3 & 0x3F
    return encode6bit(c1) + encode6bit(c2) + encode6bit(c3) + encode6bit(c4)


def plantuml_encode(text: str) -> str:
    data = zlib.compress(text.encode("utf-8"))[2:-4]
    result = []
    for i in range(0, len(data), 3):
        if i + 2 == len(data):
            result.append(encode3bytes(data[i], data[i + 1], 0))
        elif i + 1 == len(data):
            result.append(encode3bytes(data[i], 0, 0))
        else:
            result.append(encode3bytes(data[i], data[i + 1], data[i + 2]))
    return "".join(result)


def fetch_svg(puml_path: Path) -> bytes:
    source = puml_path.read_text(encoding="utf-8")
    encoded = plantuml_encode(source)
    url = SERVER_SVG + encoded
    request = Request(url, headers={"User-Agent": "banque-agence-docgen/1.0"})
    try:
        with urlopen(request, timeout=120) as response:
            body = response.read()
    except HTTPError as err:
        if err.code != 400 and err.code != 414:
            raise
        post = Request(
            SERVER_SVG_POST,
            data=source.encode("utf-8"),
            headers={
                "User-Agent": "banque-agence-docgen/1.0",
                "Content-Type": "text/plain; charset=utf-8",
            },
            method="POST",
        )
        with urlopen(post, timeout=120) as response:
            body = response.read()
    if b"<svg" not in body[:500] or b"Syntax Error" in body:
        raise RuntimeError(f"Unexpected response for {puml_path.name}")
    return body


def main() -> int:
    puml_files = sorted(DOC.rglob("*.puml"))
    if not puml_files:
        print("No .puml files found under documentation/")
        return 1

    ok = 0
    for puml in puml_files:
        svg = puml.with_suffix(".svg")
        try:
            svg.write_bytes(fetch_svg(puml))
            print(f"OK  {puml.relative_to(ROOT)} -> {svg.relative_to(ROOT)}")
            ok += 1
        except (HTTPError, URLError, RuntimeError, OSError) as exc:
            print(f"ERR {puml.relative_to(ROOT)}: {exc}", file=sys.stderr)
    print(f"\nGenerated {ok}/{len(puml_files)} SVG file(s).")
    return 0 if ok == len(puml_files) else 2


if __name__ == "__main__":
    raise SystemExit(main())
