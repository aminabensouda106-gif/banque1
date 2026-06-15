import sys
from pathlib import Path

pdf_path = Path(__file__).parent / "documentation" / "cahier-charge-PFA.pdf"

for mod in ("pypdf", "PyPDF2", "fitz"):
    try:
        if mod == "pypdf":
            import pypdf
            reader = pypdf.PdfReader(str(pdf_path))
            pages = reader.pages
        elif mod == "PyPDF2":
            import PyPDF2
            reader = PyPDF2.PdfReader(str(pdf_path))
            pages = reader.pages
        else:
            import fitz
            doc = fitz.open(str(pdf_path))
            for i in range(min(40, len(doc))):
                print(f"--- PAGE {i+1} ---")
                print(doc[i].get_text())
            sys.exit(0)

        for i, page in enumerate(pages[:40]):
            text = page.extract_text()
            if text:
                print(f"--- PAGE {i+1} ---")
                print(text)
        sys.exit(0)
    except ImportError:
        continue
    except Exception as e:
        print(f"{mod} error: {e}", file=sys.stderr)
        continue

print("No PDF library available", file=sys.stderr)
sys.exit(1)
