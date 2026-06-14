# PostgreSQL local — guide de connexion

## Deux serveurs sur ce PC

| Service Windows | Version | Port |
|---|---|---|
| `PostgreSQL_For_Odoo` | 9.5 | **5432** |
| `postgresql-x64-18` | **18** | **5433** |

Le projet **Banque Agence** utilise **PostgreSQL 18 sur le port 5433**.

Si pgAdmin ou `psql` se connectent au port **5432**, c'est l'instance **Odoo** — le mot de passe `postgres` de PostgreSQL 18 ne fonctionnera pas.

## pgAdmin

1. Clic droit **Servers** → Register → Server
2. **General** : nom = `PostgreSQL 18`
3. **Connection** :
   - Host : `localhost`
   - Port : **5433**
   - Username : `postgres`
   - Password : mot de passe défini à l'installation de PostgreSQL 18

## Créer la base du projet

```bash
psql -U postgres -h localhost -p 5433 -f documentation/setup-postgresql.sql
```

## Mot de passe postgres oublié (PostgreSQL 18)

1. Ouvrir en **administrateur** : `C:\Program Files\PostgreSQL\18\data\pg_hba.conf`
2. Remplacer `scram-sha-256` par `trust` pour `127.0.0.1/32` et `::1/128`
3. Redémarrer le service **postgresql-x64-18**
4. Terminal :
   ```bash
   psql -U postgres -h localhost -p 5433
   ALTER USER postgres PASSWORD 'VotreNouveauMotDePasse';
   \q
   ```
5. Remettre `scram-sha-256` dans `pg_hba.conf`
6. Redémarrer **postgresql-x64-18**

## Vérifier

```bash
psql -U postgres -h localhost -p 5433 -c "SELECT version();"
```

Doit afficher PostgreSQL 18.x.
