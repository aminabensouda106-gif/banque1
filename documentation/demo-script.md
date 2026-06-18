# Script de démonstration — soutenance (5 à 7 minutes)

**URL :** http://localhost:8081  
**Prérequis :** PostgreSQL démarré, application lancée, données de démo chargées (voir [demo-data.md](demo-data.md)).

---

## 1. Connexion agent (30 s)

1. Ouvrir `/login`
2. Se connecter : `agent` / `agent123`
3. Mentionner : authentification Spring Security, rôles (agent, chef, admin)

## 2. Clients et comptes (1 min)

1. **Clients** → rechercher `CD789012` → fiche **Ahmed Benali**
2. Montrer les 2 comptes (courant + épargne) et soldes
3. *(Optionnel)* Créer un nouveau client pour montrer la saisie, ou passer si le temps presse

## 3. Opérations financières (2 min)

1. **Opérations** → **Dépôt** : 500 MAD sur compte courant Ahmed → succès
2. **Retrait** : saisir 50 000 MAD → **refus** (solde insuffisant) — montrer le message métier
3. **Retrait** : 200 MAD → succès
4. **Virement** : 300 MAD du courant vers l'épargne d'Ahmed

## 4. Historique et documents (1 min)

1. **Historique** → filtrer par compte `ACC-00001`
2. Ouvrir un **reçu** imprimable (bouton sur une transaction) → **Télécharger PDF** (ex. `reçu-DEPOT-2026-06-14.pdf`)
3. **Comptes** → relevé du compte courant Ahmed

## 5. Paiement de facture (45 s)

1. **Opérations** → **Paiement facture**
2. Compte Ahmed courant, facturier **LYDEC**, référence `DEMO-2026-001`, montant **180 MAD**
3. Valider → redirection vers le **reçu** avec facturier et référence

## 6. Commande de chéquier et notifications (1 min 15)

1. Menu **Chéquiers** → liste (montrer `CHQ-00002` en attente)
2. Depuis fiche compte **Ahmed** (`ACC-00001`) → **Commander un chéquier**
3. Choisir **40 feuillets**, quantité 1 → confirmer
4. Déconnexion → login **chef** / `chef123`
5. Montrer la **cloche** avec badge (nouvelle commande) → **Notifications** → marquer comme lu
6. **Tableau de bord** : bandeau **Alertes métier** (commandes en attente / en cours) — lien vers liste filtrée
7. Sur une commande → passer en **En cours de traitement** puis **Livrée**
8. Insister : **aucun impact sur le solde**

## 7. Supervision (45 s)

1. **Journal d'audit** : montrer les actions `BILL_PAYMENT_CREATED`, `CHECKBOOK_ORDER_CREATED`

## 8. Administration (30 s)

1. Déconnexion → login **admin** / `admin123`
2. **Utilisateurs** : liste des comptes personnel, rôles

## 9. Espace client (1 min)

1. Déconnexion → login **client** : CIN `CD789012` ou n° `CL-00001` / `client123`
2. **Tableau de bord** : soldes des comptes Ahmed
3. **Notifications** : montrer une alerte (ex. après un dépôt agent en direct)
4. **Historique** → ouvrir un **reçu** → télécharger le PDF
5. **Chéquiers** : suivi de la commande en cours

---

## Phrases clés pour le jury

- « Les règles métier sont dans la couche **service**, pas dans les contrôleurs. »
- « Chaque opération sensible est **journalisée** dans `audit_logs`. »
- « Le paiement facture crée une **transaction** de type `PAIEMENT_FACTURE` ; le chéquier est un **workflow administratif** sans mouvement financier. »
- « La base évolue par **Flyway** (migrations V1 à **V11**). »
- « Les **notifications** in-app informent le chef (chéquier) et le **client** (opérations sur ses comptes). »
- « L'**espace client** est en lecture seule : l'agent opère, le client suit et est notifié. »
- « Le **seed de démo** est modulaire : données métier si base vide, portail resynchronisé à chaque démarrage (`DemoPortalSync`). »

## En cas de problème

| Problème | Action |
|---|---|
| Base vide | Vérifier les logs : « Données de démonstration chargées » |
| Connexion client refusée | Redémarrer l'app (`DemoPortalSync`) ou `demo-reset.sql` puis redémarrage |
| Port 8081 occupé | Arrêter l'autre processus ou changer `server.port` |
| Erreur PostgreSQL | Vérifier port **5433** — voir [postgresql-local.md](postgresql-local.md) |
