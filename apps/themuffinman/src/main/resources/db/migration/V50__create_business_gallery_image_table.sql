CREATE TABLE business_gallery_image (
    id BIGSERIAL PRIMARY KEY,
    business_profile_id BIGINT NOT NULL REFERENCES business_profile(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(240),
    sort_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_business_gallery_image_profile_active_sort
    ON business_gallery_image(business_profile_id, active, sort_order, id);
