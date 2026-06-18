# Rapport AmanaBank — Overleaf (prêt à compiler)

## Upload en 3 clics

1. Ouvrir [overleaf.com](https://www.overleaf.com) → **New Project** → **Upload Project**
2. Choisir **`amana-report-overleaf.zip`** (dans ce dossier)
3. Laisser le compilateur sur **pdfLaTeX** → **Recompile**

C'est tout. Un seul fichier `main.tex` + un dossier `images/`.

## Contenu du zip

| Élément | Description |
|---|---|
| `main.tex` | Rapport complet (tout le texte en un fichier) |
| `images/*.png` | 14 captures d'écran de l'application |
| `images/*.svg` | 11 diagrammes UML |

Tous les chemins dans `main.tex` pointent vers `images/nom-fichier` — rien à modifier.

## Si les diagrammes SVG ne s'affichent pas

Dans Overleaf : **Menu** (en haut à gauche) → **Settings** → **Compiler** → choisir **LaTeX** au lieu de pdfLaTeX, puis Recompile.

Alternative : les captures PNG fonctionnent toujours avec pdfLaTeX.

## Régénérer chez toi (optionnel)

```powershell
cd documentation\overleaf
python capture-screenshots.py   # app sur http://localhost:8081
python prepare.py               # reconstruit main.tex + zip
```

Puis re-uploader le nouveau `amana-report-overleaf.zip` sur Overleaf.

## Comptes utilisés pour les captures

| Rôle | Identifiant | Mot de passe |
|---|---|---|
| Agent | `agent` | `agent123` |
| Chef | `chef` | `chef123` |
| Admin | `admin` | `admin123` |
| Client (Moncef Bensouda) | `MB654321` | `client123` |
