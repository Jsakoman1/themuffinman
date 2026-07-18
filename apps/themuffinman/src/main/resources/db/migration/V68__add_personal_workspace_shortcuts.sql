CREATE TABLE personal_workspace_shortcut (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    target_type VARCHAR(40) NOT NULL,
    target_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_personal_workspace_shortcut UNIQUE (owner_id, target_type, target_id)
);
CREATE INDEX idx_personal_workspace_shortcut_owner_created ON personal_workspace_shortcut (owner_id, created_at DESC);
