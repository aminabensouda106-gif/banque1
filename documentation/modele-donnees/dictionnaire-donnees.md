# Dictionnaire de données

## Table `users`

| Champ | Type | Contrainte | Description |
|---|---|---|---|
| id | BIGINT | PK, auto | Identifiant unique |
| username | VARCHAR(50) | NOT NULL, UNIQUE | Login de l'utilisateur interne |
| password_hash | VARCHAR(255) | NOT NULL | Mot de passe chiffré (BCrypt) |
| full_name | VARCHAR(100) | NOT NULL | Nom affiché |
| email | VARCHAR(100) | | Email professionnel |
| role | ENUM | NOT NULL | ADMIN, AGENT ou CHEF_AGENCE |
| enabled | BOOLEAN | NOT NULL, défaut TRUE | FALSE = compte désactivé |
| created_at | TIMESTAMPTZ | NOT NULL | Date de création |
| last_login_at | TIMESTAMPTZ | | Dernière connexion |

## Table `clients`

| Champ | Type | Contrainte | Description |
|---|---|---|---|
| id | BIGINT | PK, auto | Identifiant unique |
| client_number | VARCHAR(20) | NOT NULL, UNIQUE | Numéro client généré (ex. CLI-00001) |
| cin | VARCHAR(20) | NOT NULL, UNIQUE | Carte d'identité nationale |
| first_name | VARCHAR(50) | NOT NULL | Prénom |
| last_name | VARCHAR(50) | NOT NULL | Nom |
| email | VARCHAR(100) | | Email du client |
| phone | VARCHAR(20) | | Téléphone |
| address | VARCHAR(255) | | Adresse |
| professional_info | TEXT | | Informations professionnelles |
| status | ENUM | NOT NULL | ACTIVE, SUSPENDED ou INACTIVE |
| created_at | TIMESTAMPTZ | NOT NULL | Date de création |
| updated_at | TIMESTAMPTZ | NOT NULL | Dernière modification |
| password_hash | VARCHAR(255) | | Mot de passe portail (BCrypt) |
| portal_enabled | BOOLEAN | NOT NULL, défaut false | Accès espace client activé |
| last_login_at | TIMESTAMPTZ | | Dernière connexion portail |

## Table `accounts`

| Champ | Type | Contrainte | Description |
|---|---|---|---|
| id | BIGINT | PK, auto | Identifiant unique |
| account_number | VARCHAR(20) | NOT NULL, UNIQUE | Numéro de compte (ex. ACC-00001) |
| client_id | BIGINT | FK → clients, NOT NULL | Client propriétaire |
| type | ENUM | NOT NULL | COURANT, EPARGNE ou PROFESSIONNEL |
| status | ENUM | NOT NULL | ACTIVE, BLOCKED ou CLOSED |
| balance | NUMERIC(19,4) | NOT NULL, ≥ 0 | Solde courant |
| opened_at | TIMESTAMPTZ | NOT NULL | Date d'ouverture |
| closed_at | TIMESTAMPTZ | | Date de clôture (si CLOSED) |
| version | BIGINT | NOT NULL | Verrouillage optimiste des mises à jour |

## Table `transactions`

| Champ | Type | Contrainte | Description |
|---|---|---|---|
| id | BIGINT | PK, auto | Identifiant unique |
| type | ENUM | NOT NULL | DEPOT, RETRAIT, VIREMENT ou **PAIEMENT_FACTURE** |
| amount | NUMERIC(19,4) | NOT NULL, > 0 | Montant de l'opération |
| source_account_id | BIGINT | FK → accounts | Compte débité (retrait, virement) |
| destination_account_id | BIGINT | FK → accounts | Compte crédité (dépôt, virement) |
| executed_by | BIGINT | FK → users, NOT NULL | Agent ayant exécuté l'opération |
| executed_at | TIMESTAMPTZ | NOT NULL | Date et heure de l'opération |
| description | VARCHAR(255) | | Libellé optionnel |

## Table `audit_logs`

| Champ | Type | Contrainte | Description |
|---|---|---|---|
| id | BIGINT | PK, auto | Identifiant unique |
| action | VARCHAR(50) | NOT NULL | Code action (ex. CLIENT_CREATED) |
| entity_type | VARCHAR(50) | | Type d'entité concernée |
| entity_id | BIGINT | | Identifiant de l'entité |
| performed_by | BIGINT | FK → users, NOT NULL | Utilisateur ayant effectué l'action |
| performed_at | TIMESTAMPTZ | NOT NULL | Date et heure |
| details | TEXT | | Détail textuel ou snapshot JSON |

## Table `bill_providers` (extension v1.1)

| Champ | Type | Contrainte | Description |
|---|---|---|---|
| id | BIGINT | PK, auto | Identifiant unique |
| code | VARCHAR(20) | NOT NULL, UNIQUE | Code court (ex. LYDEC) |
| name | VARCHAR(100) | NOT NULL | Libellé affiché |
| category | VARCHAR(50) | | Catégorie (eau, électricité, télécom…) |
| active | BOOLEAN | NOT NULL, défaut TRUE | FALSE = masqué dans les formulaires |

## Table `bill_payments` (extension v1.1)

| Champ | Type | Contrainte | Description |
|---|---|---|---|
| id | BIGINT | PK, auto | Identifiant unique |
| account_id | BIGINT | FK → accounts, NOT NULL | Compte débité |
| bill_provider_id | BIGINT | FK → bill_providers, NOT NULL | Facturier payé |
| client_reference | VARCHAR(50) | NOT NULL | Référence contrat / facture |
| amount | NUMERIC(19,4) | NOT NULL, > 0 | Montant payé |
| transaction_id | BIGINT | FK → transactions, NOT NULL, UNIQUE | Transaction liée (PAIEMENT_FACTURE) |
| created_at | TIMESTAMPTZ | NOT NULL | Date d'enregistrement |

## Table `checkbook_orders` (extension v1.1)

| Champ | Type | Contrainte | Description |
|---|---|---|---|
| id | BIGINT | PK, auto | Identifiant unique |
| order_number | VARCHAR(20) | NOT NULL, UNIQUE | Numéro commande (ex. CHQ-00001) |
| account_id | BIGINT | FK → accounts, NOT NULL | Compte concerné |
| client_id | BIGINT | FK → clients, NOT NULL | Client demandeur |
| quantity | INTEGER | NOT NULL, > 0, défaut 1 | Nombre de chéquiers |
| sheet_count | VARCHAR(20) | NOT NULL, défaut FEUILLES_20 | Format : FEUILLES_20 (20 feuillets) ou FEUILLES_40 (40 feuillets) |
| status | ENUM | NOT NULL | PENDING, PROCESSING, DELIVERED ou CANCELLED |
| requested_at | TIMESTAMPTZ | NOT NULL | Date de la demande |
| processed_at | TIMESTAMPTZ | | Date passage en traitement |
| delivered_at | TIMESTAMPTZ | | Date de livraison |
| requested_by | BIGINT | FK → users, NOT NULL | Agent ayant saisi la demande |
| notes | VARCHAR(255) | | Commentaire optionnel |

## Table `notifications` (Phase 13 + 14)

| Champ | Type | Contrainte | Description |
|---|---|---|---|
| id | BIGINT | PK, auto | Identifiant unique |
| user_id | BIGINT | FK → users | Destinataire personnel (chef, agent) |
| client_id | BIGINT | FK → clients | Destinataire client portail |
| type | VARCHAR(50) | NOT NULL | Type métier (staff ou client) |
| title | VARCHAR(150) | NOT NULL | Titre affiché dans le centre de notifications |
| message | VARCHAR(500) | NOT NULL | Texte détaillé |
| link | VARCHAR(255) | | URL relative |
| read | BOOLEAN | NOT NULL, défaut false | Lu / non lu |
| created_at | TIMESTAMPTZ | NOT NULL | Date de création |

> Contrainte : exactement un des champs `user_id` ou `client_id` est renseigné.

---

## Annexe — Seed de démonstration (profil dev, hors schéma BDD)

Composants Java documentés dans `ROADMAP.md` et `demo-data.md` :

| Composant | Rôle |
|---|---|
| `DevUserInitializer` | Crée admin, agent, chef si table `users` vide |
| `DevDemoDataInitializer` | Jeu métier complet si table `clients` vide |
| `DemoPortalSync` | Active `portal_enabled` + `password_hash` pour Ahmed (`CD789012`) et Youssef (`BE123456`) |

Configuration : `application-dev.yml` → `banque.demo.seed-enabled`, `banque.demo.portal-sync-enabled`.
