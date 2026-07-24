ALTER TABLE business_booking
    ADD COLUMN quantity_snapshot NUMERIC(12, 3) NOT NULL DEFAULT 1;

ALTER TABLE business_booking
    ADD CONSTRAINT ck_business_booking_quantity_snapshot CHECK (quantity_snapshot > 0);

CREATE INDEX ix_business_booking_quantity_overlap
    ON business_booking(business_offering_id, starts_at, ends_at, status, quantity_snapshot);
