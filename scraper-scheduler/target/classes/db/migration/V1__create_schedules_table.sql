CREATE TABLE schedules (
                           id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           url                 TEXT NOT NULL,
                           job_type            VARCHAR(20) NOT NULL,
                           cron_expression     VARCHAR(100) NOT NULL,
                           active              BOOLEAN NOT NULL DEFAULT TRUE,
                           max_attempts        INT NOT NULL DEFAULT 3,
                           extractor_config_id TEXT,
                           headers             JSONB,
                           created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
                           updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_schedules_active ON schedules(active);