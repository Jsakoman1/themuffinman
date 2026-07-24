CREATE TABLE business_booking_resource_assignment (
    id BIGSERIAL PRIMARY KEY,
    business_booking_id BIGINT NOT NULL REFERENCES business_booking(id) ON DELETE CASCADE,
    business_resource_id BIGINT NOT NULL REFERENCES business_resource(id) ON DELETE RESTRICT,
    assigned_units INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_business_booking_resource_assignment_units CHECK (assigned_units >= 1),
    CONSTRAINT ux_business_booking_resource_assignment UNIQUE (business_booking_id, business_resource_id)
);

CREATE INDEX ix_business_booking_resource_assignment_resource
    ON business_booking_resource_assignment(business_resource_id, business_booking_id);
