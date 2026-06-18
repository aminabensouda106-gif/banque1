# Rapport AmanaBank — Overleaf

## Erreur « No PDF » / README.tex ?

Si Overleaf compile **README** au lieu du rapport :

1. Menu **☰** (en haut à gauche) → **Main document**
2. Choisir **`main.tex`** (pas README.md)
3. **Recompile**

Le fichier **README.md** est seulement de l'aide — ne pas le compiler.

## Upload des fichiers (drag & drop)

Structure obligatoire :

```
main.tex                    ← document principal
images/
  emsi-logo.png             ← logo EMSI (page de garde)
  honoris-logo.png          ← logo Honoris (page de garde)
  amana-logo.png
  landing.png, login.png, … (14 captures)
  *.pdf                     (11 diagrammes UML)
```

**Les 2 logos EMSI/Honoris** doivent être dans **`images/`** avec exactement ces noms :
- `emsi-logo.png`
- `honoris-logo.png`

## Méthode la plus simple

1. Télécharger **`amana-report-overleaf.zip`** (contient tout)
2. Overleaf → **New Project** → **Upload Project**
3. Vérifier **Main document = main.tex**
4. Compiler : **pdfLaTeX** → **Recompile**

## Régénérer le zip (local)

```powershell
cd documentation\overleaf
python prepare.py
```
