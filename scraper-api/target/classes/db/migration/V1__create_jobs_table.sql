CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE job_status AS ENUM (
    'PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'RETRYING', 'DEAD'
);

CREATE TYPE job_type AS ENUM (
    'HTTP', 'JS', 'API'
);

CREATE TABLE jobs (
                      id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      url             TEXT NOT NULL,
                      job_type        VARCHAR(20) NOT NULL,
                      status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                      attempt_number  INT NOT NULL DEFAULT 0,
                      max_attempts    INT NOT NULL DEFAULT 3,
                      headers         JSONB,
                      extractor_config_id TEXT,
                      created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                      updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                      scheduled_at    TIMESTAMPTZ
);

CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_created_at ON jobs(created_at);