CREATE TABLE IF NOT EXISTS attachments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(160) NOT NULL UNIQUE,
    content_type VARCHAR(120) NOT NULL,
    size BIGINT NOT NULL CHECK (size > 0),
    checksum_sha256 VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_attachments_user_created
    ON attachments (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_attachments_user_checksum
    ON attachments (user_id, checksum_sha256);
