ALTER TABLE execution_runs
    ADD COLUMN IF NOT EXISTS pending_approval_arguments TEXT,
    ADD COLUMN IF NOT EXISTS approval_nonce_hash VARCHAR(128),
    ADD COLUMN IF NOT EXISTS approval_expires_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

UPDATE execution_runs
SET updated_at = COALESCE(finished_at, received_at)
WHERE updated_at IS NULL;

ALTER TABLE execution_runs
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE execution_runs
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(120);

CREATE INDEX IF NOT EXISTS idx_execution_runs_user_updated_at
    ON execution_runs(user_id, updated_at, id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_execution_runs_user_idempotency_key
    ON execution_runs(user_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;
