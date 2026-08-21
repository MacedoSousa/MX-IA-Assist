ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP;

ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_conversations_user_active_last_message
    ON conversations (user_id, deleted_at, last_message_at DESC);

CREATE INDEX IF NOT EXISTS idx_conversations_user_deleted_at
    ON conversations (user_id, deleted_at);
