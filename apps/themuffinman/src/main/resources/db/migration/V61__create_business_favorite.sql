CREATE TABLE business_favorite (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    business_profile_id BIGINT NOT NULL REFERENCES business_profile(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_business_favorite_owner_business UNIQUE (owner_id, business_profile_id)
);

CREATE INDEX idx_business_favorite_owner ON business_favorite(owner_id, created_at DESC);
