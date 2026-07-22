ALTER TABLE workmarket_application_news_outbox
    ADD COLUMN lease_owner VARCHAR(100),
    ADD COLUMN lease_until TIMESTAMP,
    ADD COLUMN replay_reference VARCHAR(160),
    ADD COLUMN failure_code VARCHAR(100),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX ix_workmarket_news_outbox_lease
    ON workmarket_application_news_outbox (status, lease_until);
