ALTER TABLE user_preferences
    ADD COLUMN IF NOT EXISTS preferred_language VARCHAR(10) NOT NULL DEFAULT 'pt-BR';

ALTER TABLE user_preferences
    ADD COLUMN IF NOT EXISTS communication_style VARCHAR(40) NOT NULL DEFAULT 'natural';

CREATE TABLE IF NOT EXISTS user_preference_vocabulary (
    preference_id UUID NOT NULL REFERENCES user_preferences(id) ON DELETE CASCADE,
    hint VARCHAR(40) NOT NULL,
    PRIMARY KEY (preference_id, hint)
);

CREATE INDEX IF NOT EXISTS idx_user_preference_vocabulary_hint
    ON user_preference_vocabulary (hint);
