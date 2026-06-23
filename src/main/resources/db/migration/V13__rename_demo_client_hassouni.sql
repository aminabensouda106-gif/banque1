-- Renomme le client de démo CL-00002 en Mohamed Amine Hassouni.

UPDATE clients
SET first_name        = 'Mohamed Amine',
    last_name         = 'Hassouni',
    email             = 'mohamedamine.hassouni@email.ma',
    cin               = 'MH876543',
    professional_info = 'Consultant en finance',
    updated_at        = NOW()
WHERE client_number = 'CL-00002'
   OR cin IN ('AR234567', 'AI876543');
