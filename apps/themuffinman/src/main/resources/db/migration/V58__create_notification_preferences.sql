CREATE TABLE notification_preference (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    category VARCHAR(32) NOT NULL,
    level VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_notification_preference_user_category_level UNIQUE (user_id, category, level)
);

CREATE INDEX ix_notification_preference_user ON notification_preference(user_id);
