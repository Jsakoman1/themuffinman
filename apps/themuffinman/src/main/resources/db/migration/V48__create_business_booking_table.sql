CREATE TABLE business_booking (
    id BIGSERIAL PRIMARY KEY,
    business_profile_id BIGINT NOT NULL REFERENCES business_profile(id) ON DELETE CASCADE,
    business_offering_id BIGINT NOT NULL REFERENCES business_offering(id) ON DELETE RESTRICT,
    customer_user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    status VARCHAR(40) NOT NULL,
    source VARCHAR(40) NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    timezone VARCHAR(80) NOT NULL,
    customer_note VARCHAR(1000),
    owner_note VARCHAR(1000),
    idempotency_key VARCHAR(120),
    offering_title_snapshot VARCHAR(120) NOT NULL,
    price_snapshot_amount NUMERIC(12, 2),
    price_snapshot_currency VARCHAR(3),
    duration_snapshot_minutes INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

ALTER TABLE business_booking
    ADD CONSTRAINT ck_business_booking_end_after_start CHECK (ends_at > starts_at),
    ADD CONSTRAINT ck_business_booking_duration_positive CHECK (duration_snapshot_minutes >= 1);

CREATE UNIQUE INDEX ux_business_booking_customer_idempotency
    ON business_booking(customer_user_id, idempotency_key);
CREATE INDEX ix_business_booking_offering_time
    ON business_booking(business_offering_id, starts_at, ends_at);
CREATE INDEX ix_business_booking_owner_time
    ON business_booking(business_profile_id, starts_at, ends_at);
CREATE INDEX ix_business_booking_customer_time
    ON business_booking(customer_user_id, starts_at, ends_at);
