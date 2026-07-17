CREATE TABLE native_handoff_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    target_device VARCHAR(32) NOT NULL,
    intent VARCHAR(120) NOT NULL,
    resource_reference VARCHAR(160),
    redacted_context TEXT,
    nonce VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_native_handoff_token_user ON native_handoff_token(user_id);
