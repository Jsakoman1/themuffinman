-- V36 already creates these columns in the initial vision learning schema.
-- Keep this migration idempotent so local databases that reached V37 do not fail on restart.
ALTER TABLE vision_user_preference
    ADD COLUMN IF NOT EXISTS confidence_score DOUBLE PRECISION NOT NULL DEFAULT 0.60;

ALTER TABLE vision_user_preference
    ADD COLUMN IF NOT EXISTS confidence_updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
