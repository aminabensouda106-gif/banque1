# Guide du projet Banque Agence — PFA

> **À qui s'adresse ce document ?**  
> Ce mémo est écrit pour des personnes qui **ne connaissent pas le projet** : camarades de classe, encadrants, membres du jury, ou toute personne curieuse.  
> Aucune connaissance en programmation n'est requise pour lire les premières sections. Les sections techniques en fin de document sont là pour approfondir.

---

## En bref — 2 minutes pour comprendre le projet

**Banque Agence** est une **application web** (un site accessible depuis un navigateur, comme Gmail ou une page d'administration) qui simule le fonctionnement d'une **agence bancaire**.

Concrètement, elle permet de :

1. **Enregistrer des clients** (nom, CIN, téléphone…)
2. **Ouvrir des comptes bancaires** pour ces clients (courant, épargne…)
3. **Effectuer des opérations** : dépôt d'argent, retrait, virement d'un compte à un autre
4. **Payer des factures** (eau, électricité, téléphone…) depuis le compte d'un client
5. **Commander un chéquier** et suivre sa livraison
6. **Superviser** l'activité (tableau de bord, journal d'audit)
7. **Permettre au client** de consulter ses comptes en ligne (portail client)

**Qui utilise l'application ?**

| Personne | Ce qu'elle fait |
|----------|-----------------|
| **Agent bancaire** | S'occupe des clients au quotidien : création, opérations, recherche |
| **Chef d'agence** | Supervise : statistiques, alertes, journal des actions |
| **Administrateur** | Gère les comptes du personnel (agents, chefs) |
| **Client** | Consulte ses comptes et reçoit des notifications (sans faire d'opérations lui-même) |

**Pourquoi ce projet existe ?**  
C'est un **Projet de Fin d'Année (PFA)** : un travail universitaire qui montre qu'on sait analyser un besoin, concevoir une solution, la programmer et la présenter.

---

## Glossaire — mots techniques expliqués simplement

| Mot | Explication simple |
|-----|-------------------|
| **Application web** | Un programme qu'on utilise via un navigateur (Chrome, Firefox…) sans l'installer comme un logiciel classique |
| **Backend** | La partie « invisible » qui traite les données, vérifie les règles, parle à la base de données |
| **Frontend / Interface** | Ce que l'utilisateur voit à l'écran : boutons, formulaires, tableaux |
| **Base de données** | Un fichier structuré (comme un Excel géant) qui stocke clients, comptes, transactions de façon permanente |
| **Java** | Langage de programmation utilisé pour écrire toute la logique de l'application |
| **Spring Boot** | Un « kit » Java qui facilite la création d'applications web (connexion BDD, sécurité, etc.) |
| **PostgreSQL** | Logiciel qui gère la base de données (comme MySQL ou SQL Server) |
| **Thymeleaf** | Outil qui génère les pages HTML à partir de modèles (l'agent voit une page « Clients », Thymeleaf la construit) |
| **Flyway** | Outil qui met à jour la structure de la base de données étape par étape (V1, V2, V3…) sans tout casser |
| **UML** | Langage de dessin standard pour représenter un système avant de coder (diagrammes) |
| **MCD / MLD** | Schémas de la base de données : MCD = vue conceptuelle, MLD = vue technique avec tables |
| **API / REST** | *(Non utilisé ici)* Façon pour deux programmes de communiquer ; notre projet n'en a pas besoin |
| **Monolithe** | Toute l'application est dans **un seul projet** (pas découpée en 10 micro-services) |
| **Authentification** | Vérifier identifiant + mot de passe pour savoir qui se connecte |
| **Autorisation** | Une fois connecté, définir **ce que** la personne a le droit de faire (agent ≠ admin) |
| **BCrypt** | Méthode pour **chiffrer** les mots de passe — on ne stocke jamais le mot de passe en clair |
| **Transaction (informatique)** | Opération groupée : soit tout réussit, soit tout est annulé (évite un solde incohérent) |
| **Audit** | Journal qui enregistre « qui a fait quoi et quand » pour la traçabilité |

---

## 1. Le problème que le projet résout

### Situation actuelle (sans logiciel)

Dans une petite agence, le personnel peut utiliser des cahiers, des fichiers Excel ou des outils non connectés. Cela pose des problèmes :

- **Erreurs** : mauvaise saisie du solde, double débit
- **Lenteur** : retrouver un client ou son historique prend du temps
- **Pas de traçabilité** : difficile de savoir quel agent a fait quelle opération
- **Pas de vue d'ensemble** : le chef d'agence ne voit pas facilement l'activité du jour

### Solution proposée

Une **application centralisée** où :

- Toutes les informations clients et comptes sont au même endroit
- Chaque opération vérifie automatiquement les règles (solde suffisant, compte actif…)
- Chaque action importante est **enregistrée** dans un journal
- Le client peut **consulter** ses comptes en ligne après une opération faite en agence

---

## 2. Comment fonctionne l'application — vue utilisateur

### Scénario exemple : un dépôt de 500 MAD

Voici ce qui se passe quand un **agent** dépose 500 MAD sur le compte d'Ahmed :

```
1. L'agent ouvre le navigateur → http://localhost:8081
2. Il se connecte (login: agent / mot de passe: agent123)
3. Il va dans « Opérations » → « Dépôt »
4. Il choisit le compte d'Ahmed et saisit 500 MAD
5. Il clique « Valider »

   ──── En coulisses (invisible pour l'agent) ────
6. L'application vérifie : le compte existe ? Il est actif (pas bloqué) ?
7. Le solde du compte augmente de 500
8. Une ligne « transaction » est créée (type DÉPÔT, montant, date, agent)
9. Une entrée est ajoutée au journal d'audit
10. Ahmed reçoit une notification sur son portail client (si activé)

11. L'agent voit un message « Opération réussie » avec le nouveau solde
```

### Les deux « espaces » de l'application

| Espace | Adresse web | Qui y accède | Que peut-on y faire |
|--------|-------------|--------------|---------------------|
| **Agence** | `/dashboard`, `/clients`, `/operations`… | Personnel (agent, chef, admin) | Tout gérer : clients, comptes, opérations |
| **Portail client** | `/portal/dashboard`, `/portal/accounts`… | Clients | **Uniquement consulter** : soldes, historique, reçus |

**Point important :** le client **ne peut pas** faire de virement ou retrait en ligne. L'agent opère en agence ; le client **suit** et est **notifié**.

### Page de connexion unique

Tout le monde passe par **`/login`**. Après connexion :

- Un **agent** → redirigé vers le tableau de bord agence
- Un **client** → redirigé vers son espace client

L'application reconnaît automatiquement le type d'utilisateur.

---

## 3. Les technologies — qu'est-ce que c'est et pourquoi ?

### Vue d'ensemble

L'application est construite comme une **maison à étages** :

| Étage | Technologie | Rôle en langage simple |
|-------|-------------|------------------------|
| **Ce que vous voyez** | Thymeleaf + Bootstrap | Les pages web : menus, formulaires, couleurs |
| **Le cerveau** | Java + Spring Boot | Réfléchit : « Est-ce que le solde est suffisant ? » |
| **La mémoire** | PostgreSQL | Stocke tout : clients, comptes, transactions |
| **La sécurité** | Spring Security | Vérifie login, mots de passe, droits d'accès |
| **Les mises à jour BDD** | Flyway | Fichiers SQL numérotés qui créent/modifient les tables |
| **Les PDF** | OpenPDF | Génère les reçus téléchargeables |
| **La compilation** | Maven | Assemble le projet et lance les tests |

### Détail de chaque technologie

#### Java 21
- **C'est quoi ?** Un langage de programmation très utilisé en entreprise.
- **Pourquoi ici ?** Exigence du cahier des charges ; robuste pour les applications métier (banque, assurance…).

#### Spring Boot 3
- **C'est quoi ?** Un framework (boîte à outils) qui évite de tout recoder from scratch.
- **Pourquoi ici ?** Gère la connexion à la base, la sécurité, les pages web en quelques configurations. Standard dans l'industrie Java.

#### PostgreSQL
- **C'est quoi ?** Un système de gestion de base de données relationnelle (SQL).
- **Pourquoi ici ?** Les données bancaires doivent être **fiables** : relations entre client ↔ compte ↔ transaction, montants précis (`NUMERIC`, pas de virgule flottante imprécise).

#### Thymeleaf
- **C'est quoi ?** Moteur de templates HTML côté serveur.
- **Pourquoi ici ?** Pas besoin d'une application React séparée : une seule codebase, plus simple pour un projet solo.

#### Bootstrap 5
- **C'est quoi ?** Bibliothèque CSS pour un design moderne (menus, tableaux, boutons).
- **Pourquoi ici ?** Interface professionnelle rapidement, responsive (s'adapte à la taille d'écran).

#### Flyway
- **C'est quoi ?** Outil qui exécute des scripts SQL dans l'ordre (V1, V2, V3…).
- **Pourquoi ici ?** Quand on ajoute une fonctionnalité (ex. portail client), on ajoute V11 sans recréer toute la base. Reproductible sur n'importe quelle machine.

#### Spring Security + BCrypt
- **C'est quoi ?** Module de sécurité + algorithme de hachage des mots de passe.
- **Pourquoi ici ?** Règle métier R7 : les mots de passe ne sont **jamais** stockés en clair. Seuls les rôles autorisés accèdent à chaque page.

### Ce qu'on a volontairement **non** utilisé

| Technologie | Pourquoi pas |
|-------------|--------------|
| React / Angular | Trop complexe pour ce PFA ; Thymeleaf suffit |
| Docker | Pas nécessaire : PostgreSQL installé localement |
| Microservices | Un seul développeur → un seul projet monolithique |
| Python | Choix Java uniquement pour ce projet |

---

## 4. L'architecture — comment le code est organisé

### Analogie : un restaurant

| Restaurant | Application |
|------------|-------------|
| **Serveur** (prend la commande) | **Controller** — reçoit la requête HTTP (clic sur « Valider dépôt ») |
| **Cuisinier** (applique la recette) | **Service** — vérifie les règles métier, calcule le nouveau solde |
| **Garde-manger** (stocke les ingrédients) | **Repository** — lit/écrit dans la base de données |
| **Réserve** | **PostgreSQL** — stockage permanent |

**Règle d'or :** la « recette » (règles métier) est dans la **cuisine (Service)**, pas chez le serveur (Controller).  
Exemple : « Refuser un retrait si solde insuffisant » est codé dans `TransactionService`, pas dans la page HTML.

### Schéma des couches

```
   UTILISATEUR (navigateur Chrome, Firefox…)
              │
              ▼
   ┌──────────────────────────────┐
   │  COUCHE WEB                  │  Pages, formulaires, boutons
   │  (Controllers + Thymeleaf)   │  Ex: ClientController, login.html
   └──────────────┬───────────────┘
                  │
                  ▼
   ┌──────────────────────────────┐
   │  COUCHE SERVICE              │  Règles métier, calculs
   │  (Services Java)             │  Ex: TransactionService.deposit()
   └──────────────┬───────────────┘
                  │
                  ▼
   ┌──────────────────────────────┐
   │  COUCHE REPOSITORY           │  Accès base de données
   │  (Interfaces JPA)            │  Ex: AccountRepository
   └──────────────┬───────────────┘
                  │
                  ▼
   ┌──────────────────────────────┐
   │  POSTGRESQL                  │  Tables: clients, accounts…
   └──────────────────────────────┘

   En transversal : Sécurité, Audit, Validation des formulaires
```

### Dossiers principaux du projet — à quoi servent-ils ?

```
banque1/                          ← Racine du projet
│
├── pom.xml                       ← Liste des bibliothèques Java (comme un package.json)
├── mvnw                          ← Commande pour lancer Maven sans l'installer
├── run-dev.bat                   ← Double-clic pour démarrer l'app en dev (Windows)
│
├── src/main/java/                ← TOUT LE CODE JAVA
│   └── com/banque/agence/
│       ├── config/               ← Configuration (sécurité, données de démo)
│       ├── domain/entity/        ← « Modèles » : Client, Account, Transaction…
│       ├── domain/enums/         ← Listes fixes : ACTIVE, BLOCKED, DEPOT, RETRAIT…
│       ├── repository/           ← Accès BDD (findById, search…)
│       ├── service/              ← LOGIQUE MÉTIER (le cœur du projet)
│       ├── web/controller/       ← Pages web côté agence
│       ├── web/controller/portal/← Pages web côté client
│       ├── security/             ← Gestion login personnel + client
│       └── audit/                ← Journal des actions sensibles
│
├── src/main/resources/
│   ├── application.yml           ← Paramètres (port 8081, nom de la BDD)
│   ├── application-dev.yml       ← Paramètres développement (données de test)
│   ├── db/migration/             ← Scripts SQL Flyway (V1 à V13)
│   ├── templates/                ← Fichiers HTML (pages de l'interface)
│   └── static/                   ← CSS, images, JavaScript
│
├── src/test/java/                ← Tests automatiques (~80 tests)
│
└── documentation/                ← TOUTE LA DOC DU PFA
    ├── uml/                      ← Diagrammes de conception
    ├── modele-donnees/           ← MCD, MLD, dictionnaire
    ├── overleaf/                 ← Rapport écrit (LaTeX)
    ├── demo-script.md            ← Script de démonstration soutenance
    └── MEMO-SOUTENANCE-PFA.md    ← Ce document
```

### Fichiers Java les plus importants — expliqués

| Fichier | Rôle concret |
|---------|--------------|
| `BanqueAgenceApplication.java` | Bouton « ON » : démarre toute l'application |
| `SecurityConfig.java` | Définit qui peut accéder à quelle URL ; configure le login |
| `ClientService.java` | Créer/modifier/chercher un client ; génère le n° CLI-00001 |
| `AccountService.java` | Ouvrir, bloquer, clôturer un compte |
| `TransactionService.java` | Dépôt, retrait, virement — **vérifie le solde** |
| `BillPaymentService.java` | Payer une facture LYDEC/ONEE depuis un compte |
| `CheckbookOrderService.java` | Demander un chéquier ; change le statut (en attente → livré) |
| `NotificationService.java` | Envoie les alertes (cloche en haut de l'écran) |
| `AuditService.java` | Écrit dans le journal « agent X a créé client Y » |
| `DevDemoDataInitializer.java` | Au premier lancement, remplit la BDD avec des clients fictifs pour tester |

---

## 5. La conception — ce qu'on a dessiné avant de coder

En ingénierie logicielle, on **conçoit d'abord**, on **code ensuite**. Le cahier des charges exige des diagrammes UML **avant** le développement.

### Pourquoi dessiner avant de coder ?

- Voir les **oublis** avant d'écrire des milliers de lignes
- **Expliquer** le projet au jury avec des schémas clairs
- **Valider** avec l'encadrant que la solution répond au besoin

### Les types de diagrammes (dossier `documentation/uml/`)

#### 1. Diagrammes de cas d'utilisation
- **C'est quoi ?** Dessine **qui** (acteur) fait **quoi** (fonctionnalité).
- **Exemple :** « L'agent peut effectuer un dépôt » ; « L'admin peut créer un utilisateur ».
- **Fichiers :** `01-clients-comptes`, `02-operations-supervision`, `03-portail-client`

#### 2. Diagramme de classes
- **C'est quoi ?** Liste les **objets** du système (Client, Compte, Transaction…) et leurs **liens**.
- **Exemple :** Un Client possède plusieurs Comptes ; une Transaction est exécutée par un User.
- **Fichier :** `diagramme-classes.puml`

#### 3. Diagrammes de séquence
- **C'est quoi ?** Montre **l'ordre des échanges** entre l'utilisateur, l'application et la base, étape par étape.
- **Exemple pour un virement :** Agent → Controller → Service → vérifie solde → Repository → met à jour BDD → retour succès.
- **Fichiers :** `01-authentification` à `09-portail-client-notifications` (9 scénarios)

#### 4. Diagramme d'activité
- **C'est quoi ?** Flux de décisions sous forme de flowchart.
- **Exemple :** Virement → compte actif ? → solde OK ? → oui → exécuter / non → erreur.
- **Fichier :** `diagramme-activite-virement.puml`

#### 5. MCD et MLD (dossier `modele-donnees/`)
- **MCD (Modèle Conceptuel de Données)** : vue « métier » — Client, Compte, sans détail technique.
- **MLD (Modèle Logique de Données)** : vue « informaticien » — tables SQL, clés primaires, clés étrangères.
- **Dictionnaire de données** : description de chaque champ (ex. `balance` = solde du compte en MAD).

### Ordre de développement du projet (phases)

| Phase | Contenu | En clair |
|-------|---------|----------|
| 1 | Conception UML | Dessiner tous les diagrammes |
| 2–8 | Noyau | Login, clients, comptes, opérations, users, dashboard |
| 10–11 | Extensions | Paiement facture + chéquier |
| 13–14 | Canal client | Notifications + portail client |
| 12 | Clôture | Documentation, démo, soutenance |

---

## 6. La base de données — où sont stockées les informations

### Tables principales (comme des feuilles Excel liées entre elles)

| Table | Contient quoi | Exemple |
|-------|---------------|---------|
| `users` | Personnel de l'agence | admin, agent, chef |
| `clients` | Fiches clients | Ahmed Benali, CIN CD789012 |
| `accounts` | Comptes bancaires | ACC-00001, solde 5000 MAD, type COURANT |
| `transactions` | Historique des mouvements | Dépôt 500 MAD le 14/06/2026 |
| `bill_providers` | Liste des facturiers | LYDEC, ONEE, Orange |
| `bill_payments` | Détail d'un paiement de facture | Référence DEMO-2026-001 |
| `checkbook_orders` | Commandes de chéquier | CHQ-00001, statut EN ATTENTE |
| `notifications` | Alertes in-app | « Nouvelle commande chéquier » |
| `audit_logs` | Journal de traçabilité | « agent a créé client CLI-00005 » |

### Relations importantes (à comprendre)

```
Un CLIENT peut avoir PLUSIEURS COMPTES
    Ahmed → compte courant + compte épargne

Un COMPTE appartient à UN SEUL CLIENT
    ACC-00001 → Ahmed uniquement

Une TRANSACTION est faite par UN AGENT (user)
    Dépôt 500 MAD → exécuté par « agent »

Un PAIEMENT DE FACTURE crée une TRANSACTION
    Payer LYDEC → ligne dans transactions + bill_payments

Une COMMANDE CHÉQUIER ne touche PAS au solde
    C'est administratif, pas financier
```

### Flyway — évolution de la base

Chaque fichier `V*.sql` ajoute ou modifie des tables :

| Version | Ce qu'elle ajoute |
|---------|-------------------|
| V2 | Table des utilisateurs (admin, agent…) |
| V3 | Table des clients |
| V5 | Table des comptes |
| V6 | Table des transactions |
| V7 | Paiement de facture |
| V8 | Commande chéquier |
| V10 | Notifications |
| V11 | Portail client (mot de passe client en ligne) |

**Pourquoi c'est utile ?** On peut recréer la même base sur un autre PC en lançant simplement l'application.

---

## 7. Fonctionnalités détaillées — module par module

### Module Clients
**Qui ?** Agent, chef, admin.  
**Actions :** Créer un client, modifier ses infos, le rechercher (nom, CIN, téléphone), le suspendre ou le désactiver.  
**Numéro auto :** CLI-00001, CLI-00002…

### Module Comptes
**Qui ?** Agent, chef, admin.  
**Actions :** Ouvrir un compte (courant, épargne, professionnel), bloquer, débloquer, clôturer.  
**Règle :** On ne clôture un compte que si le solde est à zéro.

### Module Opérations (Transactions)
**Qui ?** Agent, chef, admin.  
**Actions :**
- **Dépôt** : ajoute de l'argent au compte
- **Retrait** : retire de l'argent — **refusé si solde insuffisant**
- **Virement** : transfert entre deux comptes — **les deux doivent être actifs**

### Module Paiement de facture (extension)
**Qui ?** Agent.  
**Actions :** Choisir un compte client, un facturier (LYDEC…), saisir référence et montant → le compte est débité → reçu généré.

### Module Chéquier (extension)
**Qui ?** Agent demande ; chef suit.  
**Actions :** Commander un chéquier (20 ou 40 feuillets) sur un compte courant/professionnel.  
**Workflow :** En attente → En cours de traitement → Livré (ou Annulé).  
**Important :** Aucun argent n'est débité — ce n'est pas une opération financière.

### Module Notifications (extension)
**Qui ?** Chef (alertes chéquier) ; client (opérations sur ses comptes).  
**Actions :** Cloche avec badge rouge ; liste des notifications ; marquer comme lu.

### Module Portail client (extension)
**Qui ?** Client uniquement.  
**Actions :** Voir ses comptes, soldes, historique, télécharger reçus PDF, suivre commande chéquier.  
**Limitation :** Lecture seule — pas de virement en ligne.

### Module Administration
**Qui ?** Admin uniquement.  
**Actions :** Créer des comptes personnel (nouveaux agents), changer les rôles, activer/désactiver.

### Module Supervision
**Qui ?** Chef et admin.  
**Actions :** Tableau de bord (statistiques), journal d'audit (qui a fait quoi).

---

## 8. Règles métier — les « lois » de l'application

Ces règles sont **automatiquement appliquées** par le code. L'agent ne peut pas les contourner via l'interface.

| # | Règle | Explication concrète |
|---|-------|----------------------|
| R1 | Un client → plusieurs comptes | Ahmed peut avoir courant + épargne |
| R2 | Un compte → un seul client | ACC-00001 n'appartient qu'à Ahmed |
| R3 | Solde insuffisant → retrait refusé | Solde 100 MAD, retrait 500 → message d'erreur |
| R4 | Virement : comptes actifs | Impossible de virer depuis un compte bloqué |
| R5 | Traçabilité | Chaque opération enregistre date, heure et agent |
| R6 | Compte bloqué/clôturé | Pas d'opération possible |
| R7 | Mots de passe chiffrés | En base on voit `$2a$10$...`, pas « admin123 » |
| R8 | Audit | Création client, opération financière → journal |
| R9–R10 | Paiement facture | Compte actif, solde OK, référence obligatoire |
| R11–R13 | Chéquier | Compte courant/pro ; max 1 en attente ; pas d'impact solde |

---

## 9. Sécurité — comment l'application se protège

1. **Login obligatoire** — sans identifiant/mot de passe, aucune page métier n'est accessible
2. **Rôles** — un agent ne peut pas accéder à « Gestion utilisateurs » (réservé admin)
3. **BCrypt** — les mots de passe sont hachés (irréversible)
4. **Isolation portail** — un client ne voit que **ses** comptes, pas ceux des autres
5. **CSRF** — protection contre les attaques par formulaire forgé (standard Spring Security)

### Comptes de test (pour essayer l'application)

| Rôle | Identifiant | Mot de passe | URL après login |
|------|-------------|--------------|-----------------|
| Agent | `agent` | `agent123` | Tableau de bord agence |
| Chef | `chef` | `chef123` | Tableau de bord agence |
| Admin | `admin` | `admin123` | Tableau de bord agence |
| Client (Ahmed) | `CD789012` ou `CLI-00001` | `client123` | Portail client |

**Adresse :** http://localhost:8081 (application doit être lancée sur la machine)

---

## 10. Comment lancer le projet (guide rapide)

### Prérequis sur la machine

1. **Java 21** installé
2. **PostgreSQL** installé et démarré
3. Base de données `banque_agence` créée (script `documentation/setup-postgresql.sql`)

### Démarrer l'application

```bash
mvn spring-boot:run
```

Ou double-clic sur `run-dev.bat` (Windows).

Puis ouvrir **http://localhost:8081** dans le navigateur.

### Lancer les tests automatiques

```bash
mvn test
```

Environ **80 tests** vérifient que les règles métier fonctionnent (solde, compte bloqué, portail…).

---

## 11. Documents du projet — où trouver quoi ?

| Document | Contenu | Pour qui |
|----------|---------|----------|
| `MEMO-SOUTENANCE-PFA.md` | **Ce guide** — vue d'ensemble | Tout le monde |
| `cahier-charge-PFA.pdf` | Exigences officielles du professeur | Encadrant, jury |
| `TECHNICAL.md` | Détails techniques complets | Développeurs |
| `ROADMAP-Amina.md` | Plan de développement phase par phase | Équipe projet |
| `demo-script.md` | Scénario de démo 5–7 min pour la soutenance | Présentateur |
| `manuel-utilisateur.md` | Mode d'emploi écran par écran | Utilisateurs test |
| `presentation-outline-Amina.md` | Structure du PowerPoint | Présentateur |
| `overleaf/main-Amina.tex` | Rapport écrit complet | Jury |
| `uml/*.svg` | Diagrammes visuels | Rapport, slides |
| `modele-donnees/MCD.svg`, `MLD.svg` | Schémas base de données | Rapport |

---

## 12. Préparation de la soutenance — pour le présentateur

### Déroulement type (15 minutes)

| Temps | Contenu |
|-------|---------|
| 2 min | Contexte : pourquoi une agence a besoin d'un logiciel |
| 3 min | Technologies et architecture (schéma des couches) |
| 3 min | Conception : montrer 2–3 diagrammes (cas d'utilisation, MCD, séquence) |
| 5–7 min | **Démo live** sur l'application |
| 2 min | Tests, difficultés, conclusion |
| 3 min | Questions du jury |

### Démo recommandée (ordre logique)

1. Login **agent** → chercher client Ahmed (CIN `CD789012`)
2. **Dépôt** 500 MAD → **retrait** refusé (montant trop élevé) → retrait 200 OK
3. **Virement** 300 MAD (courant → épargne)
4. **Historique** + télécharger **reçu PDF**
5. **Paiement facture** LYDEC
6. **Commander chéquier**
7. Login **chef** → **notifications** + changer statut → Livré
8. **Journal d'audit**
9. Login **admin** → gestion utilisateurs
10. Login **client** → portail + notification

### Phrases utiles face au jury

- « Nous avons d'abord **modélisé** le système en UML, puis **implémenté** en Java Spring Boot. »
- « Les règles bancaires (solde, compte actif…) sont dans la couche **Service**, pas dans l'interface. »
- « Chaque opération sensible est **tracée** dans le journal d'audit. »
- « Le portail client est en **lecture seule** : l'agent opère, le client consulte. »

---

## 13. Questions fréquentes — avec réponses expliquées

**Pourquoi Java et pas Python ?**  
Le cahier des charges oriente vers Java / Spring Boot. C'est aussi le standard des applications bancaires en entreprise.

**Pourquoi pas une application mobile ?**  
Le PFA demande une application **web**. Une version mobile serait une évolution future.

**Comment l'application évite les erreurs de solde ?**  
Trois mécanismes : calcul avec `BigDecimal` (précision décimale), opérations **atomiques** (`@Transactional` = tout ou rien), et vérification systématique avant chaque retrait/virement.

**Quelle différence entre paiement facture et chéquier ?**  
Le paiement facture **retire de l'argent** du compte (comme un retrait). Le chéquier est une **demande administrative** : on change un statut (en attente → livré) sans toucher au solde.

**Le client peut-il faire un virement en ligne ?**  
Non. Par choix de conception : le client **consulte** et reçoit des **notifications**. Les opérations financières passent par l'agent en agence.

**C'est quoi Flyway en une phrase ?**  
Des fichiers SQL numérotés qui construisent la base de données étape par étape, comme des mises à jour versionnées.

**Combien de tests ?**  
Environ 80 tests automatiques qui simulent des actions (dépôt, login client, notification…) et vérifient que le résultat est correct.

---

## 14. Référence technique rapide (pour aller plus loin)

### Stack complète

Java 21 · Spring Boot 3.4 · Spring Security · Spring Data JPA · PostgreSQL 18 · Flyway · Thymeleaf · Bootstrap 5 · OpenPDF · Maven · JUnit 5

### Acteurs et rôles

| Acteur | Code technique |
|--------|----------------|
| Administrateur | `ADMIN` |
| Agent bancaire | `AGENT` |
| Chef d'agence | `CHEF_AGENCE` |
| Client | `CLIENT` |

### Migrations Flyway

V1 baseline → V2 users → V3 clients → V4 audit → V5 accounts → V6 transactions → V7 bills → V8 checkbook → V9 sheet count → V10 notifications → V11 portal → V12–V13 demo fixes

---

*Document rédigé pour faciliter la compréhension du PFA Banque Agence — Juin 2026*  
*Pour les détails techniques exhaustifs, voir `documentation/TECHNICAL.md`*
