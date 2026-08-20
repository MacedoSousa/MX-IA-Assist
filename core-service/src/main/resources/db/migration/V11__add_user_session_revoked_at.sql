-- Reconcile legacy session schemas with the current revocation model.
ALTER TABLE user_sessions
    ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP NULL;
