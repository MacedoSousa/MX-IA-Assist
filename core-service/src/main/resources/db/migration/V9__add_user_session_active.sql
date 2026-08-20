-- Reconcile databases created before the persisted session active flag was introduced.
ALTER TABLE user_sessions
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE user_sessions
    ALTER COLUMN active DROP DEFAULT;

CREATE INDEX IF NOT EXISTS idx_user_sessions_active ON user_sessions(active);
