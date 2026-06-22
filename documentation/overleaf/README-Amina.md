# Rapport AmanaBank — Overleaf

## Compiler (3 étapes)

1. Upload **`amana-report-overleaf.zip`** sur [overleaf.com](https://www.overleaf.com)
2. Vérifier que le compilateur est **pdfLaTeX** (Menu → Settings)
3. Cliquer **Recompile**

Structure du projet Overleaf après upload :

```
main.tex
images/
  amana-logo.png
  landing.png, login.png, …   (14 captures)
  01-clients-comptes.pdf, …   (11 diagrammes UML en PDF)
```

## Corrections appliquées

- Diagrammes UML en **PDF** (pdfLaTeX ne lit pas les SVG)
- Chemins images via `\graphicspath{{images/}}`
- Toutes les figures utilisent `\includegraphics` direct (pas de `\IfFileExists`)

## Régénérer le zip

```powershell
cd documentation\overleaf
python capture-screenshots.py   # optionnel
python prepare.py               # valide les images + crée le zip
```

`prepare.py` échoue si une image référencée dans `main.tex` est manquante.
