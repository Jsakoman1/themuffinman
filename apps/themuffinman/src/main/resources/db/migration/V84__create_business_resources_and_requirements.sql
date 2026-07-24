CREATE TABLE business_resource_pool (
    id BIGSERIAL PRIMARY KEY,
    business_profile_id BIGINT NOT NULL REFERENCES business_profile(id) ON DELETE CASCADE,
    pool_key VARCHAR(80) NOT NULL,
    label VARCHAR(160) NOT NULL,
    resource_type VARCHAR(40) NOT NULL,
    capacity INTEGER NOT NULL DEFAULT 1,
    public_label VARCHAR(160),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_business_resource_pool_capacity CHECK (capacity >= 1)
);

CREATE UNIQUE INDEX ux_business_resource_pool_key
    ON business_resource_pool(business_profile_id, pool_key);
CREATE INDEX ix_business_resource_pool_active
    ON business_resource_pool(business_profile_id, active, id);

CREATE TABLE business_resource (
    id BIGSERIAL PRIMARY KEY,
    business_profile_id BIGINT NOT NULL REFERENCES business_profile(id) ON DELETE CASCADE,
    resource_pool_id BIGINT REFERENCES business_resource_pool(id) ON DELETE SET NULL,
    owner_user_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL,
    resource_key VARCHAR(80) NOT NULL,
    label VARCHAR(160) NOT NULL,
    resource_type VARCHAR(40) NOT NULL,
    public_label VARCHAR(160),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX ux_business_resource_key
    ON business_resource(business_profile_id, resource_key);
CREATE INDEX ix_business_resource_active_pool
    ON business_resource(resource_pool_id, active, id);
CREATE INDEX ix_business_resource_owner
    ON business_resource(owner_user_id, active, id);

CREATE TABLE business_offering_resource_requirement (
    id BIGSERIAL PRIMARY KEY,
    business_offering_id BIGINT NOT NULL REFERENCES business_offering(id) ON DELETE CASCADE,
    resource_pool_id BIGINT REFERENCES business_resource_pool(id) ON DELETE CASCADE,
    resource_type VARCHAR(40) NOT NULL,
    required_count INTEGER NOT NULL DEFAULT 1,
    assignment_mode VARCHAR(32) NOT NULL DEFAULT 'ANY_AVAILABLE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_business_offering_resource_requirement_count CHECK (required_count >= 1),
    CONSTRAINT ck_business_offering_resource_requirement_target CHECK (
        resource_pool_id IS NOT NULL OR resource_type <> ''
    )
);

CREATE INDEX ix_business_offering_resource_requirement_offering
    ON business_offering_resource_requirement(business_offering_id, id);
