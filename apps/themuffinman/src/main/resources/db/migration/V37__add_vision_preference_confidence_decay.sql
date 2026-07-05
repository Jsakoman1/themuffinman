ALTER TABLE vision_user_preference
    ADD COLUMN confidence_score DOUBLE PRECISION NOT NULL DEFAULT 0.60;

ALTER TABLE vision_user_preference
    ADD COLUMN confidence_updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
