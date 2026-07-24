ALTER TABLE business_offering
    ADD COLUMN fulfillment_mode VARCHAR(40) NOT NULL DEFAULT 'EXACT_APPOINTMENT',
    ADD COLUMN schema_version INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN duration_increment_minutes INTEGER,
    ADD COLUMN minimum_quantity INTEGER,
    ADD COLUMN maximum_quantity INTEGER;

ALTER TABLE business_offering
    ADD CONSTRAINT ck_business_offering_schema_version CHECK (schema_version >= 1),
    ADD CONSTRAINT ck_business_offering_duration_increment CHECK (
        duration_increment_minutes IS NULL OR duration_increment_minutes >= 1
    ),
    ADD CONSTRAINT ck_business_offering_minimum_quantity CHECK (
        minimum_quantity IS NULL OR minimum_quantity >= 1
    ),
    ADD CONSTRAINT ck_business_offering_maximum_quantity CHECK (
        maximum_quantity IS NULL OR maximum_quantity >= 1
    ),
    ADD CONSTRAINT ck_business_offering_quantity_range CHECK (
        maximum_quantity IS NULL OR minimum_quantity IS NULL OR maximum_quantity >= minimum_quantity
    );

CREATE INDEX ix_business_offering_fulfillment_mode
    ON business_offering(fulfillment_mode, active, id);
