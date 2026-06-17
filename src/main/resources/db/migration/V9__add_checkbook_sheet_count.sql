ALTER TABLE checkbook_orders
    ADD COLUMN sheet_count VARCHAR(20) NOT NULL DEFAULT 'FEUILLES_20'
        CHECK (sheet_count IN ('FEUILLES_20', 'FEUILLES_40'));
