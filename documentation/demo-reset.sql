-- Réinitialise les données métier pour recharger le jeu de démo au prochain démarrage.
-- Les utilisateurs (admin, agent, chef) et les facturiers (bill_providers) sont conservés.
--
-- Usage :
--   psql -U banque -h localhost -p 5433 -d banque_agence -f documentation/demo-reset.sql
-- Puis redémarrer l'application (profil dev, banque.demo.seed-enabled=true).

TRUNCATE audit_logs RESTART IDENTITY CASCADE;
TRUNCATE checkbook_orders RESTART IDENTITY CASCADE;
TRUNCATE bill_payments RESTART IDENTITY CASCADE;
TRUNCATE transactions RESTART IDENTITY CASCADE;
TRUNCATE accounts RESTART IDENTITY CASCADE;
TRUNCATE clients RESTART IDENTITY CASCADE;
