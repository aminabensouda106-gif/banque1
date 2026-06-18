# Captures d'écran — Chapitre 5 du rapport

Prendre ces captures depuis http://localhost:8081 (application démarrée, données de démo chargées).

Format recommandé : **PNG**, largeur ~1200 px.

## Espace public (nouveau design)

| Fichier | Écran | Conseil |
|---|---|---|
| `landing.png` | Page d'accueil `/` — **section hero** | Inclure header + titre + boutons CTA |
| `landing-services.png` | Même page — **grille services** (optionnel) | Scroll jusqu'à « Des solutions bancaires complètes » |
| `login.png` | Page `/login` | Montrer logo, formulaire et encadrés d'aide |

## Espace agence

| Fichier | Écran | Compte |
|---|---|---|
| `dashboard-agent.png` | Dashboard avec cartes KPI et alertes | `chef` / `chef123` |
| `clients-liste.png` | Liste clients + recherche | `agent` / `agent123` |
| `compte-detail.png` | Fiche compte Ahmed | agent |
| `operations-depot.png` | Formulaire dépôt | agent |
| `paiement-facture.png` | Paiement facture LYDEC | agent |
| `recu-pdf.png` | Reçu avec bouton PDF | agent |
| `chequier-liste.png` | Liste commandes chéquier | agent |
| `notifications.png` | Centre notifications | `chef` |
| `admin-users.png` | Gestion utilisateurs | `admin` / `admin123` |

## Espace client (portail)

| Fichier | Écran | Compte |
|---|---|---|
| `portal-dashboard.png` | Dashboard client (KPI + comptes) | `CD789012` / `client123` |
| `portal-notifications.png` | Notifications client | client |

## Commandes utiles

```powershell
# Démarrer l'application
mvn spring-boot:run

# Ouvrir dans le navigateur
start http://localhost:8081
```
