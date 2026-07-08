CREATE TABLE business_offering (
    id BIGSERIAL PRIMARY KEY,
    business_profile_id BIGINT NOT NULL REFERENCES business_profile(id) ON DELETE CASCADE,
    title VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL,
    summary VARCHAR(240),
    description TEXT,
    pricing_type VARCHAR(32) NOT NULL,
    base_price_amount NUMERIC(12, 2),
    base_price_currency VARCHAR(3),
    duration_mode VARCHAR(32) NOT NULL,
    default_duration_minutes INTEGER,
    min_duration_minutes INTEGER,
    max_duration_minutes INTEGER,
    capacity_mode VARCHAR(32) NOT NULL,
    slot_capacity INTEGER NOT NULL,
    booking_mode VARCHAR(32) NOT NULL,
    requires_owner_confirmation BOOLEAN NOT NULL DEFAULT TRUE,
    buffer_before_minutes INTEGER NOT NULL DEFAULT 0,
    buffer_after_minutes INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

ALTER TABLE business_offering
    ADD CONSTRAINT ck_business_offering_slot_capacity CHECK (slot_capacity >= 1),
    ADD CONSTRAINT ck_business_offering_buffer_before CHECK (buffer_before_minutes >= 0),
    ADD CONSTRAINT ck_business_offering_buffer_after CHECK (buffer_after_minutes >= 0),
    ADD CONSTRAINT ck_business_offering_default_duration CHECK (
        default_duration_minutes IS NULL OR default_duration_minutes >= 1
    ),
    ADD CONSTRAINT ck_business_offering_min_duration CHECK (
        min_duration_minutes IS NULL OR min_duration_minutes >= 1
    ),
    ADD CONSTRAINT ck_business_offering_max_duration CHECK (
        max_duration_minutes IS NULL OR max_duration_minutes >= 1
    );

CREATE UNIQUE INDEX ux_business_offering_profile_slug ON business_offering(business_profile_id, slug);
CREATE INDEX ix_business_offering_profile_active_sort ON business_offering(business_profile_id, active, sort_order, id);
