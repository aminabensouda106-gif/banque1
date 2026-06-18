# Rapport PFA LaTeX — AmanaBank

**Binôme :** BENSOUDA Amina & AFILAL Soraya  
**Encadrante :** Dr. ORCHI HOUDA  
**École :** EMSI Rabat — 3ème année IIR

---

## Structure du rapport (5 chapitres)

```
documentation/rapport/
├── main.tex                    ← Compiler ce fichier
├── includes/
│   ├── preamble.tex            # Noms, encadrante, packages
│   ├── page-garde.tex
│   ├── dedicaces.tex
│   ├── remerciements.tex
│   ├── resume.tex
│   ├── abstract.tex
│   └── abreviations.tex
├── chapitres/
│   ├── introduction.tex        # Contexte, problématique, objectifs, plan
│   ├── chapitre1.tex           # Contexte projet + méthodologie
│   ├── chapitre2.tex           # Analyse besoins + UML
│   ├── chapitre3.tex           # Environnement + technologies
│   ├── chapitre4.tex           # Architecture back-end + front-end
│   ├── chapitre5.tex           # Captures d'écran interfaces
│   └── conclusion.tex          # Bilan, difficultés, perspectives
├── bibliographie.tex
├── figures/
│   ├── uml/                    # Diagrammes PDF (script prepare-figures)
│   └── captures/               # Screenshots application
├── scripts/
│   └── prepare-figures.ps1
├── compile.bat
└── README.md
```

## Plan du rapport

| Section | Contenu |
|---|---|
| **Introduction générale** | Contexte, problématique, objectifs, plan |
| **Chapitre 1** | Organisme d'accueil (EMSI), existant, méthodologie 2TUP |
| **Chapitre 2** | Acteurs, besoins, UML (UC, classes, séquences, activité, MCD/MLD) |
| **Chapitre 3** | Outils : Java, Spring Boot, PostgreSQL, Thymeleaf, Bootstrap… |
| **Chapitre 4** | Arborescence `src/main/java`, services, contrôleurs, templates |
| **Chapitre 5** | Captures : landing, login, agence, portail client |
| **Conclusion** | Bilan, difficultés, perspectives |

## Compiler le PDF

### 1. Figures UML

```powershell
cd documentation\rapport\scripts
.\prepare-figures.ps1
```

### 2. Captures écran

Voir `figures/captures/README.md` — lancer l'app sur http://localhost:8081

### 3. Compilation

```powershell
cd documentation\rapport
.\compile.bat
```

Ou manuellement :
```powershell
pdflatex main.tex
pdflatex main.tex
```

### Overleaf

Uploader le dossier `documentation/rapport/` entier et compiler `main.tex`.

## Personnalisation

Les informations du binôme sont dans `includes/preamble.tex` :
- `\etudianteun`, `\etudiantedeux`, `\encadrante`

## Prérequis

- Distribution LaTeX : **MiKTeX** ou **TeX Live**
- Optionnel : **Inkscape** (conversion SVG → PDF)
