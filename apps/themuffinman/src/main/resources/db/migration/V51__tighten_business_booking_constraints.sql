ALTER TABLE business_booking
    ADD CONSTRAINT ck_business_booking_status CHECK (
        status IN (
            'PENDING_CONFIRMATION',
            'CONFIRMED',
            'REJECTED',
            'CANCELLED_BY_CUSTOMER',
            'CANCELLED_BY_OWNER',
            'COMPLETED',
            'NO_SHOW'
        )
    ),
    ADD CONSTRAINT ck_business_booking_source CHECK (
        source IN ('CUSTOMER', 'OWNER_CREATED')
    );

CREATE INDEX ix_business_booking_status_starts_at
    ON business_booking(status, starts_at);
