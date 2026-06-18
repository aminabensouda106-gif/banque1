# Rapport AmanaBank — Overleaf

**Un seul fichier** `main.tex` + dossier `images/`. Plus de sources séparées.

## Compiler sur Overleaf

1. [overleaf.com](https://www.overleaf.com) → **New Project** → **Upload Project**
2. Uploader **`amana-report-overleaf.zip`**
3. Compilateur : **pdfLaTeX** → **Recompile**

## Design

- Police **Palatino** (`newpxtext`) + micro-typographie
- Charte **AmanaBank** : marron, or, beige
- Page de garde avec logo, bandeaux et encadré auteurs
- Tableaux à en-têtes marron et lignes alternées beige

## Régénérer le zip

```powershell
cd documentation\overleaf
python capture-screenshots.py   # optionnel — app sur :8081
python prepare.py               # met à jour le zip
```

## Captures (comptes démo)

| Rôle | Identifiant | Mot de passe |
|---|---|---|
| Agent | `agent` | `agent123` |
| Chef | `chef` | `chef123` |
| Admin | `admin` | `admin123` |
| Client | `MB654321` (Moncef Bensouda) | `client123` |

## SVG sur Overleaf

Si les diagrammes UML ne s'affichent pas : **Menu → Settings → Compiler → LaTeX**.
