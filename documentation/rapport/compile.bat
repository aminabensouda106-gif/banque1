@echo off
REM Compile le rapport PFA AmanaBank
subst X: "c:\Users\HP\OneDrive - Ecole Marocaine des Sciences de l'Ingénieur\Bureau\Banque" 2>nul
X:
cd documentation\rapport

echo [1/3] Preparation des figures UML...
python scripts\prepare-figures.py

echo.
echo [2/3] Recherche pdflatex...
set PDFLATEX=pdflatex
where pdflatex >nul 2>&1
if errorlevel 1 (
    if exist "%LOCALAPPDATA%\Programs\MiKTeX\miktex\bin\x64\pdflatex.exe" (
        set PDFLATEX=%LOCALAPPDATA%\Programs\MiKTeX\miktex\bin\x64\pdflatex.exe
    ) else if exist "C:\Program Files\MiKTeX\miktex\bin\x64\pdflatex.exe" (
        set PDFLATEX=C:\Program Files\MiKTeX\miktex\bin\x64\pdflatex.exe
    ) else (
        echo ERREUR: pdflatex introuvable. Installez MiKTeX: https://miktex.org/download
        goto end
    )
)

echo [3/3] Compilation main.tex (2 passes)...
"%PDFLATEX%" -interaction=nonstopmode main.tex
"%PDFLATEX%" -interaction=nonstopmode main.tex

if exist main.pdf (
    echo.
    echo OK: main.pdf genere
    dir main.pdf
) else (
    echo.
    echo ECHEC - consultez main.log
)

:end
pause
