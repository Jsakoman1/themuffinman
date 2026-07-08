CREATE TABLE business_availability_rule (
    id BIGSERIAL PRIMARY KEY,
    business_profile_id BIGINT NOT NULL REFERENCES business_profile(id) ON DELETE CASCADE,
    business_offering_id BIGINT REFERENCES business_offering(id) ON DELETE CASCADE,
    day_of_week INTEGER NOT NULL,
    start_time_local TIME NOT NULL,
    end_time_local TIME NOT NULL,
    slot_granularity_minutes INTEGER NOT NULL,
    capacity_override INTEGER,
    valid_from DATE,
    valid_until DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

ALTER TABLE business_availability_rule
    ADD CONSTRAINT ck_business_availability_rule_day_of_week CHECK (day_of_week BETWEEN 1 AND 7),
    ADD CONSTRAINT ck_business_availability_rule_end_after_start CHECK (end_time_local > start_time_local),
    ADD CONSTRAINT ck_business_availability_rule_slot_granularity CHECK (slot_granularity_minutes >= 5),
    ADD CONSTRAINT ck_business_availability_rule_capacity_override CHECK (capacity_override IS NULL OR capacity_override >= 1),
    ADD CONSTRAINT ck_business_availability_rule_valid_range CHECK (valid_until IS NULL OR valid_from IS NULL OR valid_until >= valid_from);

CREATE INDEX ix_business_availability_rule_profile_active_day
    ON business_availability_rule(business_profile_id, active, day_of_week, start_time_local);
CREATE INDEX ix_business_availability_rule_offering_active_day
    ON business_availability_rule(business_offering_id, active, day_of_week, start_time_local);

CREATE TABLE business_availability_exception (
    id BIGSERIAL PRIMARY KEY,
    business_profile_id BIGINT NOT NULL REFERENCES business_profile(id) ON DELETE CASCADE,
    business_offering_id BIGINT REFERENCES business_offering(id) ON DELETE CASCADE,
    exception_type VARCHAR(32) NOT NULL,
    start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    replacement_capacity INTEGER,
    replacement_start_time_local TIME,
    replacement_end_time_local TIME,
    reason VARCHAR(240),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

ALTER TABLE business_availability_exception
    ADD CONSTRAINT ck_business_availability_exception_end_after_start CHECK (end_at > start_at),
    ADD CONSTRAINT ck_business_availability_exception_replacement_capacity CHECK (
        replacement_capacity IS NULL OR replacement_capacity >= 1
    ),
    ADD CONSTRAINT ck_business_availability_exception_replacement_window CHECK (
        replacement_end_time_local IS NULL
        OR replacement_start_time_local IS NULL
        OR replacement_end_time_local > replacement_start_time_local
    );

CREATE INDEX ix_business_availability_exception_profile_start
    ON business_availability_exception(business_profile_id, start_at, end_at);
CREATE INDEX ix_business_availability_exception_offering_start
    ON business_availability_exception(business_offering_id, start_at, end_at);
