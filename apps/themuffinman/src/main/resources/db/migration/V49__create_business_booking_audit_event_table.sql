CREATE TABLE business_booking_audit_event (
    id BIGSERIAL PRIMARY KEY,
    business_booking_id BIGINT NOT NULL REFERENCES business_booking(id) ON DELETE CASCADE,
    event_type VARCHAR(40) NOT NULL,
    previous_status VARCHAR(40),
    new_status VARCHAR(40),
    actor_user_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL,
    actor_username VARCHAR(120),
    note VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_business_booking_audit_event_booking_created
    ON business_booking_audit_event(business_booking_id, created_at);
