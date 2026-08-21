CREATE TABLE IF NOT EXISTS user_preferences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    learning_style VARCHAR(40) NOT NULL DEFAULT 'balanced',
    knowledge_level VARCHAR(40) NOT NULL DEFAULT 'beginner',
    last_active_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_preference_topics (
    preference_id UUID NOT NULL REFERENCES user_preferences(id) ON DELETE CASCADE,
    topic VARCHAR(80) NOT NULL,
    PRIMARY KEY (preference_id, topic)
);

CREATE INDEX IF NOT EXISTS idx_user_preferences_last_active
    ON user_preferences(last_active_at);

CREATE INDEX IF NOT EXISTS idx_user_preference_topics_topic
    ON user_preference_topics(topic);
