# Manuel utilisateur — Banque Agence

Application web de gestion d'agence bancaire : **personnel interne** (agence) et **espace client** (portail en ligne). Interface en **français**.

---

## 1. Accès à l'application

1. Ouvrir le navigateur à l'adresse fournie par l'administrateur (ex. http://localhost:8081)
2. Saisir **identifiant** et **mot de passe**
3. Cliquer sur **Se connecter**

En cas d'échec : vérifier les identifiants ou contacter l'administrateur si le compte est désactivé.

**Déconnexion :** bouton en bas du menu latéral gauche.

### Rôles (personnel)

| Rôle | Accès principal |
|---|---|
| Agent bancaire | Clients, comptes, opérations, historique, chéquiers |
| Chef d'agence | Idem + tableau de bord, journal d'audit, notifications |
| Administrateur | Idem + gestion des utilisateurs |
| **Client bancaire** | Portail `/portal/**` — consultation et notifications uniquement |

---

## 2. Tableau de bord

Réservé au **chef d'agence** et à l'**administrateur**.

Affiche :
- nombre de clients et de comptes actifs ;
- transactions du jour ;
- **alertes métier** : commandes de chéquier en attente ou en cours (liens vers la liste filtrée) ;
- liste des dernières opérations.

---

## 2 bis. Notifications

Accessible à **tous les utilisateurs** via l'icône **cloche** en haut à droite.

- Le **badge** indique le nombre de notifications non lues.
- Page **Notifications** : liste chronologique, lien vers la fiche concernée.
- Actions : **Marquer comme lu** (une notification) ou **Tout marquer comme lu**.

Règles métier :
- Nouvelle commande chéquier → notification pour chaque **chef d'agence** actif.
- Changement de statut d'une commande → notification pour l'**agent demandeur** (si différent de celui qui modifie le statut).

---

## 3. Gestion des clients

### Liste et recherche

Menu **Clients** : tableau paginé. La barre de recherche accepte nom, CIN, numéro client ou téléphone.

### Créer un client

1. **Nouveau client**
2. Renseigner nom, prénom, CIN (unique), coordonnées
3. **Enregistrer**

### Modifier / statut

Depuis la fiche client : **Modifier** ou changer le statut (actif, suspendu, inactif).

---

## 4. Comptes bancaires

### Ouvrir un compte

1. Depuis la fiche client → **Ouvrir un compte**
2. Choisir le type : **Courant**, **Épargne** ou **Professionnel**
3. Valider — un numéro de compte est attribué automatiquement

### Fiche compte

- Solde et statut (actif, bloqué, clôturé)
- Historique des transactions liées
- Commandes de chéquier associées
- Actions : bloquer, débloquer, clôturer (selon règles métier)
- **Relevé** : version imprimable des mouvements

---

## 5. Opérations financières

Menu **Opérations** :

| Opération | Description |
|---|---|
| Dépôt | Crédite un compte actif |
| Retrait | Débite si le solde est suffisant |
| Virement | Transfert entre deux comptes actifs |
| Paiement facture | Débit pour un facturier (LYDEC, IAM, etc.) |

Chaque opération réussie peut générer un **reçu** depuis l'historique.

### Messages d'erreur courants

- *Solde insuffisant* — montant de retrait ou paiement trop élevé
- *Compte bloqué ou clôturé* — opération refusée
- *Compte épargne* — non éligible à la commande de chéquier

---

## 6. Historique des transactions

Menu **Historique** :

- Filtrer par compte, type, dates
- Consulter le détail et imprimer le **reçu**

Types affichés : Dépôt, Retrait, Virement, Paiement facture.

---

## 7. Commandes de chéquier

Menu **Chéquiers** :

1. **Liste** — toutes les demandes, filtre par statut
2. **Nouvelle demande** — choisir le compte (courant ou professionnel actif), la quantité (1 à 10) et le format **20 ou 40 feuillets**
3. **Suivi** — depuis la fiche commande, faire évoluer le statut :
   - En attente → En cours de traitement → Livrée
   - Annulation possible tant que non livrée

> La commande de chéquier **ne modifie pas le solde** du compte.

---

## 8. Gestion des utilisateurs (admin)

Menu **Utilisateurs** :

- Créer un compte personnel (identifiant, mot de passe, rôle)
- Modifier nom, e-mail, rôle, activer/désactiver
- Réinitialiser le mot de passe

---

## 9. Journal d'audit

Menu **Journal d'audit** (admin et chef d'agence) :

- Liste chronologique des actions importantes (création client, opération, changement de statut chéquier, etc.)
- Lecture seule — traçabilité pour la supervision

---

## 10. Espace client (portail en ligne)

Connexion avec **n° client** ou **CIN** + mot de passe (même page `/login` que le personnel).

### Navigation (alignée sur l'interface agence)

- **Sidebar** : icônes Bootstrap, menu sticky, pied de page avec nom du client et déconnexion.
- **En-tête** : titre de page + **cloche** (badge notifications non lues).
- **Couleur** : thème teal pour distinguer l'espace client du thème bleu agence.

### Fonctionnalités (`/portal/**`)

- **Tableau de bord** : soldes et dernières opérations
- **Mes comptes** : liste des comptes du client
- **Historique** : opérations sur ses comptes uniquement, filtres par type et date
- **Reçus** : impression depuis l'historique
- **Chéquiers** : suivi des commandes (statut, dates)
- **Notifications** : alertes sur dépôts, retraits, virements, factures et chéquiers

> Le client **ne saisit pas** d'opérations financières : elles sont effectuées par l'agent en agence.

---

## 11. Raccourcis démo (environnement de test)

Voir [demo-data.md](demo-data.md) pour le détail du **seed modulaire** (`DevUserInitializer`, `DevDemoDataInitializer`, `DemoPortalSync`).

| Utilisateur | Mot de passe |
|---|---|
| agent | agent123 |
| chef | chef123 |
| admin | admin123 |

**Portail client :**

| Identifiant | Mot de passe | Client |
|---|---|---|
| `CD789012` ou `CL-00001` | `client123` | Ahmed Benali |
| `BE123456` ou `CL-00003` | `client123` | Youssef Idrissi |

> Si la connexion client échoue sur une base déjà existante : **redémarrer l'application** (`DemoPortalSync` réactive le portail) ou exécuter `demo-reset.sql` puis redémarrer.

---

*Banque Agence — PFA EMSI. Dernière mise à jour : juin 2026 (portail client + seed modulaire).*
