CREATE TABLE business_booking_snapshot (
    id BIGSERIAL PRIMARY KEY,
    business_booking_id BIGINT NOT NULL REFERENCES business_booking(id) ON DELETE CASCADE,
    offering_schema_version INTEGER NOT NULL,
    fulfillment_mode VARCHAR(40) NOT NULL,
    demand JSONB NOT NULL DEFAULT '{}'::jsonb,
    selected_options JSONB NOT NULL DEFAULT '{}'::jsonb,
    price_lines JSONB NOT NULL DEFAULT '[]'::jsonb,
    resource_assignments JSONB NOT NULL DEFAULT '[]'::jsonb,
    capacity_consumption JSONB NOT NULL DEFAULT '{}'::jsonb,
    conditions_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_business_booking_snapshot_schema_version CHECK (offering_schema_version >= 1)
);

CREATE UNIQUE INDEX ux_business_booking_snapshot_booking
    ON business_booking_snapshot(business_booking_id);
CREATE INDEX ix_business_booking_snapshot_fulfillment
    ON business_booking_snapshot(fulfillment_mode, id);
