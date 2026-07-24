CREATE TABLE business_offering_option (
    id BIGSERIAL PRIMARY KEY,
    business_offering_id BIGINT NOT NULL REFERENCES business_offering(id) ON DELETE CASCADE,
    option_key VARCHAR(80) NOT NULL,
    label VARCHAR(160) NOT NULL,
    description VARCHAR(500),
    value_type VARCHAR(32) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_business_offering_option_sort_order CHECK (sort_order >= 0)
);

CREATE UNIQUE INDEX ux_business_offering_option_key
    ON business_offering_option(business_offering_id, option_key);
CREATE INDEX ix_business_offering_option_active_sort
    ON business_offering_option(business_offering_id, active, sort_order, id);

CREATE TABLE business_demand_field (
    id BIGSERIAL PRIMARY KEY,
    business_offering_id BIGINT NOT NULL REFERENCES business_offering(id) ON DELETE CASCADE,
    field_key VARCHAR(80) NOT NULL,
    label VARCHAR(160) NOT NULL,
    description VARCHAR(500),
    value_type VARCHAR(32) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    customer_visible BOOLEAN NOT NULL DEFAULT TRUE,
    owner_visible BOOLEAN NOT NULL DEFAULT TRUE,
    retention_days INTEGER,
    validation_schema JSONB NOT NULL DEFAULT '{}'::jsonb,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_business_demand_field_retention CHECK (retention_days IS NULL OR retention_days >= 1),
    CONSTRAINT ck_business_demand_field_sort_order CHECK (sort_order >= 0)
);

CREATE UNIQUE INDEX ux_business_demand_field_key
    ON business_demand_field(business_offering_id, field_key);
CREATE INDEX ix_business_demand_field_active_sort
    ON business_demand_field(business_offering_id, active, sort_order, id);
