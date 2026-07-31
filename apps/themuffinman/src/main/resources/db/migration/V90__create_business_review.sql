CREATE TABLE business_review (
    id BIGSERIAL PRIMARY KEY,
    business_booking_id BIGINT NOT NULL REFERENCES business_booking(id),
    business_profile_id BIGINT NOT NULL REFERENCES business_profile(id),
    reviewer_user_id BIGINT NOT NULL REFERENCES app_user(id),
    stars SMALLINT NOT NULL CHECK (stars BETWEEN 1 AND 5),
    comment VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_business_review_booking_reviewer UNIQUE (business_booking_id, reviewer_user_id)
);

CREATE INDEX idx_business_review_profile_created_at ON business_review(business_profile_id, created_at DESC);
