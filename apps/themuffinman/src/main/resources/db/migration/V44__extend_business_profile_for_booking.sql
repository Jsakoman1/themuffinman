ALTER TABLE business_profile
    ADD COLUMN timezone VARCHAR(80),
    ADD COLUMN booking_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN public_address_label VARCHAR(240),
    ADD COLUMN latitude DOUBLE PRECISION,
    ADD COLUMN longitude DOUBLE PRECISION,
    ADD COLUMN contact_whatsapp VARCHAR(80),
    ADD COLUMN hero_image_url VARCHAR(500);
