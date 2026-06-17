# Données de démonstration

Jeu de données chargé **automatiquement** au premier démarrage en profil `dev` si la table `clients` est vide.

Désactiver : `banque.demo.seed-enabled=false` dans `application-dev.yml`.

## Réinitialiser les données

```sql
-- Connexion : psql -U banque -h localhost -p 5433 -d banque_agence
TRUNCATE audit_logs, checkbook_orders, bill_payments, transactions,
         accounts, clients RESTART IDENTITY CASCADE;
```

Puis redémarrer l'application (`mvn spring-boot:run` ou `run-dev.bat`).

> Les utilisateurs (`admin`, `agent`, `chef`) ne sont pas supprimés.

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
