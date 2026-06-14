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
| type | ENUM | NOT NULL | DEPOT, RETRAIT ou VIREMENT |
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
