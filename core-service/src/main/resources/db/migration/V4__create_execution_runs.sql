CREATE TABLE IF NOT EXISTS execution_runs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    correlation_id UUID NOT NULL UNIQUE,
    input TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    skill_name VARCHAR(120),
    pending_approval VARCHAR(160),
    output TEXT,
    error_code VARCHAR(120),
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    finished_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_execution_runs_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_execution_runs_user_id ON execution_runs(user_id);
CREATE INDEX IF NOT EXISTS idx_execution_runs_status ON execution_runs(status);
CREATE INDEX IF NOT EXISTS idx_execution_runs_correlation_id ON execution_runs(correlation_id);
