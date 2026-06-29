CREATE TABLE business_profile (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    business_name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL UNIQUE,
    headline VARCHAR(160),
    description TEXT,
    contact_email VARCHAR(160),
    contact_phone VARCHAR(80),
    website_url VARCHAR(300),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX ux_business_profile_owner ON business_profile(owner_id);
CREATE INDEX ix_business_profile_active_name ON business_profile(active, business_name);
