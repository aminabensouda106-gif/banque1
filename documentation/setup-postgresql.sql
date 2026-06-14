-- Exécuter une seule fois sur PostgreSQL 18 (port 5433)
-- Exemple : psql -U postgres -h localhost -p 5433 -f documentation/setup-postgresql.sql

CREATE DATABASE banque_agence;

CREATE USER banque WITH PASSWORD 'banque';

GRANT ALL PRIVILEGES ON DATABASE banque_agence TO banque;

\c banque_agence

GRANT ALL ON SCHEMA public TO banque;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO banque;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO banque;
