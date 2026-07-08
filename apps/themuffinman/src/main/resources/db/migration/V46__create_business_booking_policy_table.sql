CREATE TABLE business_booking_policy (
    id BIGSERIAL PRIMARY KEY,
    business_profile_id BIGINT NOT NULL UNIQUE REFERENCES business_profile(id) ON DELETE CASCADE,
    lead_time_minutes INTEGER NOT NULL,
    max_advance_days INTEGER NOT NULL,
    customer_cancellation_window_minutes INTEGER NOT NULL,
    owner_reschedule_window_minutes INTEGER NOT NULL,
    requires_owner_confirmation_default BOOLEAN NOT NULL DEFAULT TRUE,
    allow_customer_cancellation BOOLEAN NOT NULL DEFAULT TRUE,
    allow_owner_manual_approval BOOLEAN NOT NULL DEFAULT TRUE,
    allow_owner_manual_rejection BOOLEAN NOT NULL DEFAULT TRUE,
    allow_waitlist BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

ALTER TABLE business_booking_policy
    ADD CONSTRAINT ck_business_booking_policy_lead_time CHECK (lead_time_minutes >= 0),
    ADD CONSTRAINT ck_business_booking_policy_max_advance CHECK (max_advance_days >= 1),
    ADD CONSTRAINT ck_business_booking_policy_customer_cancel CHECK (customer_cancellation_window_minutes >= 0),
    ADD CONSTRAINT ck_business_booking_policy_owner_reschedule CHECK (owner_reschedule_window_minutes >= 0);
