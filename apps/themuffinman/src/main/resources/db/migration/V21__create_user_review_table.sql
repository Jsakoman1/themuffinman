CREATE TABLE user_review (
    id BIGSERIAL PRIMARY KEY,
    quest_id BIGINT NOT NULL REFERENCES quest(id) ON DELETE CASCADE,
    reviewer_user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    reviewed_user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    reviewed_role VARCHAR(20) NOT NULL,
    stars SMALLINT NOT NULL CHECK (stars BETWEEN 1 AND 5),
    comment VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_user_review_quest_reviewer_reviewed UNIQUE (quest_id, reviewer_user_id, reviewed_user_id),
    CONSTRAINT chk_user_review_not_self CHECK (reviewer_user_id <> reviewed_user_id)
);

CREATE INDEX idx_user_review_reviewed_user_role_created_at
    ON user_review (reviewed_user_id, reviewed_role, created_at DESC);
