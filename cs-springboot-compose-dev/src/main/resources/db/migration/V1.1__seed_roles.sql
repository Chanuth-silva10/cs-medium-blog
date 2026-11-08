-- The roles are fixed reference data, so they live in a migration: every environment gets
-- exactly the same rows, and adding a role later is just another migration.
INSERT INTO roles (name, description, created_at, updated_at)
VALUES ('MEMBER', 'Can read and manage their own account', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
       ('ADMIN', 'Can do everything a member can, and list all users', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
       ('SUPER_ADMIN', 'Can do everything, including creating administrators', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
