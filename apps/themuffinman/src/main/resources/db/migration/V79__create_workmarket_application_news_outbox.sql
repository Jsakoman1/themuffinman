CREATE TABLE workmarket_application_news_outbox (
    event_id VARCHAR(36) NOT NULL PRIMARY KEY,
    event_type VARCHAR(32) NOT NULL,
    application_id BIGINT NOT NULL,
    actor_user_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    available_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    delivered_at TIMESTAMP,
    last_error VARCHAR(1000)
);

CREATE INDEX ix_workmarket_news_outbox_dispatch
    ON workmarket_application_news_outbox (status, available_at, created_at);
