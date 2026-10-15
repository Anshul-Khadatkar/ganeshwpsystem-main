-- ============================================================
-- V2: Seed the initial set of building/donation categories.
-- These are ordinary data rows, not special-cased anywhere in
-- application code - admins can add/edit/deactivate more via
-- the Buildings page without any code change.
-- ============================================================

INSERT INTO buildings (name, description, active, requires_floor, requires_flat, sort_order, created_at, updated_at, deleted)
VALUES
    ('Sunflower-A', 'Tower located near the main gate.', TRUE, TRUE,  TRUE,  1, NOW(), NOW(), FALSE),
    ('Sunflower-B', NULL,                                  TRUE, TRUE,  TRUE,  2, NOW(), NOW(), FALSE),
    ('Lotus',       NULL,                                  TRUE, TRUE,  TRUE,  3, NOW(), NOW(), FALSE),
    ('Aster',       NULL,                                  TRUE, TRUE,  TRUE,  4, NOW(), NOW(), FALSE),
    ('Cosmos',      NULL,                                  TRUE, TRUE,  TRUE,  5, NOW(), NOW(), FALSE),
    ('Tulip',       NULL,                                  TRUE, TRUE,  TRUE,  6, NOW(), NOW(), FALSE),
    ('Orchid',      NULL,                                  TRUE, TRUE,  TRUE,  7, NOW(), NOW(), FALSE),
    ('Row Houses',  NULL,                                  TRUE, FALSE, FALSE, 8, NOW(), NOW(), FALSE),
    ('Shops',       NULL,                                  TRUE, FALSE, FALSE, 9, NOW(), NOW(), FALSE);
