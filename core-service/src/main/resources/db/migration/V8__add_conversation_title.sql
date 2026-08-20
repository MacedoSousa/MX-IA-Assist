-- Reconcile databases created before conversations.title was added to the JPA entity.
ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS title VARCHAR(200) NOT NULL DEFAULT 'Conversa';

ALTER TABLE conversations
    ALTER COLUMN title DROP DEFAULT;
