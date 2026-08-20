-- Reconcile legacy session schemas with the current UserSession entity.
ALTER TABLE user_sessions
    ADD COLUMN IF NOT EXISTS device_name VARCHAR(255) NOT NULL DEFAULT 'Dispositivo legado';

ALTER TABLE user_sessions
    ALTER COLUMN device_name DROP DEFAULT;

-- Older databases required token_hash, while the current model persists refresh
-- credentials separately. Keep legacy data but do not require the unused column.
ALTER TABLE user_sessions
    ADD COLUMN IF NOT EXISTS token_hash VARCHAR(255);

ALTER TABLE user_sessions
    ALTER COLUMN token_hash DROP NOT NULL;
