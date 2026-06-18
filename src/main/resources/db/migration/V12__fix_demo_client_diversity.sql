-- Diversifie les clients affichés sur la liste des comptes (un titulaire distinct
-- parmi les 5 comptes les plus récents) et remplace Abdellah Raisouni par Abdellah Idrissi.
-- Les réaffectations ne s'appliquent qu'à l'ancien jeu « 2 clients / 5 comptes » (sans ACC-00007).

UPDATE clients
SET last_name         = 'Idrissi',
    email             = 'abdellah.idrissi@email.ma',
    cin               = 'AI876543',
    professional_info = 'Ingénieur informatique',
    updated_at        = NOW()
WHERE cin = 'AR234567'
   OR (first_name = 'Abdellah' AND last_name = 'Raisouni');

INSERT INTO clients (client_number, cin, first_name, last_name, email, phone, address, professional_info, status)
SELECT v.client_number, v.cin, v.first_name, v.last_name, v.email, v.phone, v.address, v.professional_info,
       v.status::client_status
FROM (VALUES
    ('CL-00003', 'KA234567', 'Karim', 'El Amrani', 'karim.elamrani@email.ma', '0633445566',
     '22 Av Mohammed VI, Marrakech', 'Gérant restaurant', 'ACTIVE'),
    ('CL-00004', 'NB987654', 'Nadia', 'Bennani', 'nadia.bennani@email.ma', '0644556677',
     '5 Rue Sebou, Fès', NULL, 'ACTIVE'),
    ('CL-00005', 'MC123789', 'Mehdi', 'Chraibi', 'mehdi.chraibi@email.ma', '0655667788',
     '14 Bd Pasteur, Tanger', NULL, 'ACTIVE')
) AS v(client_number, cin, first_name, last_name, email, phone, address, professional_info, status)
WHERE NOT EXISTS (
    SELECT 1 FROM clients c WHERE c.cin = v.cin OR c.client_number = v.client_number
);

UPDATE accounts
SET client_id = (SELECT id FROM clients WHERE cin = 'KA234567' LIMIT 1)
WHERE account_number = 'ACC-00002'
  AND NOT EXISTS (SELECT 1 FROM accounts WHERE account_number = 'ACC-00007')
  AND client_id = (SELECT id FROM clients WHERE cin = 'MB654321' LIMIT 1);

UPDATE accounts
SET client_id = (SELECT id FROM clients WHERE cin = 'NB987654' LIMIT 1)
WHERE account_number = 'ACC-00003'
  AND NOT EXISTS (SELECT 1 FROM accounts WHERE account_number = 'ACC-00007')
  AND client_id = (SELECT id FROM clients WHERE cin = 'MB654321' LIMIT 1);

UPDATE accounts
SET client_id = (SELECT id FROM clients WHERE cin = 'MC123789' LIMIT 1)
WHERE account_number = 'ACC-00004'
  AND NOT EXISTS (SELECT 1 FROM accounts WHERE account_number = 'ACC-00007')
  AND client_id IN (SELECT id FROM clients WHERE cin IN ('AR234567', 'AI876543'));

UPDATE accounts
SET client_id = (SELECT id FROM clients WHERE cin = 'AI876543' LIMIT 1)
WHERE account_number = 'ACC-00005'
  AND NOT EXISTS (SELECT 1 FROM accounts WHERE account_number = 'ACC-00007')
  AND client_id IN (SELECT id FROM clients WHERE cin IN ('AR234567', 'AI876543'));

UPDATE accounts SET opened_at = NOW() - INTERVAL '20 days' WHERE account_number = 'ACC-00001';
UPDATE accounts SET opened_at = NOW() - INTERVAL '4 days'  WHERE account_number = 'ACC-00002'
  AND NOT EXISTS (SELECT 1 FROM accounts WHERE account_number = 'ACC-00007');
UPDATE accounts SET opened_at = NOW() - INTERVAL '3 days'  WHERE account_number = 'ACC-00003'
  AND NOT EXISTS (SELECT 1 FROM accounts WHERE account_number = 'ACC-00007');
UPDATE accounts SET opened_at = NOW() - INTERVAL '2 days'  WHERE account_number = 'ACC-00004'
  AND NOT EXISTS (SELECT 1 FROM accounts WHERE account_number = 'ACC-00007');
UPDATE accounts SET opened_at = NOW() - INTERVAL '1 day'   WHERE account_number = 'ACC-00005'
  AND NOT EXISTS (SELECT 1 FROM accounts WHERE account_number = 'ACC-00007');
