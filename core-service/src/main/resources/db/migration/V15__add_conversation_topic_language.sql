ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS topic VARCHAR(120) NOT NULL DEFAULT 'geral';

ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS language VARCHAR(10) NOT NULL DEFAULT 'pt-BR';

ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS last_message_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_conversations_user_last_message
    ON conversations (user_id, last_message_at DESC);

CREATE INDEX IF NOT EXISTS idx_conversations_user_topic_last_message
    ON conversations (user_id, topic, last_message_at DESC);

CREATE INDEX IF NOT EXISTS idx_conversations_user_language
    ON conversations (user_id, language);
