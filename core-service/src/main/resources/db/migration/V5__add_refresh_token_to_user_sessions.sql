ALTER TABLE user_sessions
    ADD COLUMN refresh_token_hash VARCHAR(64),
    ADD COLUMN refresh_expires_at TIMESTAMP;

CREATE UNIQUE INDEX idx_user_sessions_refresh_token_hash
    ON user_sessions (refresh_token_hash)
    WHERE refresh_token_hash IS NOT NULL;
