# Données de démonstration

Seed **modulaire** en profil `dev` : chaque brique est indépendante pour pouvoir ajouter des phases sans tout réinitialiser.

## Composants (ordre de démarrage)

| Ordre | Classe | Condition | Contenu |
|---|---|---|---|
| 1 | `DevUserInitializer` | table `users` vide | admin, agent, chef |
| 2 | `DevDemoDataInitializer` | table `clients` vide | 5 clients, 8 comptes, 20 transactions, 2 factures, 2 chéquiers |
| 3 | `DemoPortalSync` | **à chaque démarrage** | active le portail pour Ahmed et Youssef si manquant |

## Configuration (`application-dev.yml`)

```yaml
banque:
  demo:
    seed-enabled: true          # utilisateurs + jeu métier (si tables vides)
    portal-sync-enabled: true   # resync portail client (recommandé)
```

Désactiver tout le seed : `seed-enabled: false`.  
Désactiver uniquement la resync portail : `portal-sync-enabled: false`.

## Réinitialiser les données

```bash
psql -U banque -h localhost -p 5433 -d banque_agence -f documentation/demo-reset.sql
```

Puis redémarrer l'application. Le seed métier se relance si `clients` est vide ; le portail est resynchronisé par `DemoPortalSync`.

> Les utilisateurs (`admin`, `agent`, `chef`) ne sont pas supprimés par `demo-reset.sql`.

## Comptes applicatifs (personnel)

| Utilisateur | Mot de passe | Rôle |
|---|---|---|
| `admin` | `admin123` | Administrateur |
| `agent` | `agent123` | Agent bancaire |
| `chef` | `chef123` | Chef d'agence |

## Clients de démo (5)

| N° client | CIN | Nom | Ville |
|---|---|---|---|
| CL-00001 | **CD789012** | Ahmed Benali | Casablanca |
| CL-00002 | CD456789 | Fatima Alaoui | Marrakech |
| CL-00003 | BE123456 | Youssef Idrissi | Rabat |
| CL-00004 | BE987654 | Khadija Tazi | Fès |
| CL-00005 | BE654321 | Omar Berrada | Tanger |

**Client vedette pour la soutenance :** Ahmed Benali — CIN `CD789012`.

## Accès portail client (démo)

| Identifiant | Mot de passe | Client |
|---|---|---|
| `CD789012` ou `CL-00001` | `client123` | Ahmed Benali |
| `BE123456` ou `CL-00003` | `client123` | Youssef Idrissi |

> Si la connexion échoue avec une base déjà existante : **redémarrez l'application** (`DemoPortalSync` corrige automatiquement). Sinon exécutez `demo-reset.sql` puis redémarrez.

## Comptes (8)

| N° compte | Client | Type | Solde indicatif (MAD) |
|---|---|---|---|
| ACC-00001 | Ahmed Benali | Courant | 8 550 |
| ACC-00002 | Ahmed Benali | Épargne | 950 |
| ACC-00003 | Fatima Alaoui | Professionnel | 2 000 |
| ACC-00004 | Youssef Idrissi | Courant | 4 250 |
| ACC-00005 | Youssef Idrissi | Épargne | 400 |
| ACC-00006 | Khadija Tazi | Courant | 2 050 |
| ACC-00007 | Omar Berrada | Courant | 600 |
| ACC-00008 | Omar Berrada | Épargne | 1 100 |

## Historique préchargé

- **20 transactions** : dépôts, retraits, virements sur 14 jours
- **2 paiements facture** : LYDEC (Ahmed, 250 MAD), IAM (Youssef, 150 MAD)
- **2 commandes chéquier** :
  - `CHQ-00001` — Ahmed, 20 feuillets, statut **LIVRÉE**
  - `CHQ-00002` — Youssef, 2 × 40 feuillets, statut **EN ATTENTE**

## Facturiers disponibles

LYDEC, ONEE, IAM, Orange Maroc, Inwi (migration Flyway V7).
