-- MX Migration Control

CREATE TABLE IF NOT EXISTS schema_migrations (

    id SERIAL PRIMARY KEY,

    filename VARCHAR(255) UNIQUE NOT NULL,

    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);