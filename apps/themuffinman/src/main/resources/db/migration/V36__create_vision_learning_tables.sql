CREATE TABLE vision_user_preference (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    preference_key VARCHAR(80) NOT NULL,
    preference_value VARCHAR(255) NOT NULL,
    observation_count INTEGER NOT NULL DEFAULT 1,
    source_type VARCHAR(32) NOT NULL DEFAULT 'turn',
    last_observed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_vision_user_preference_user_key UNIQUE (user_id, preference_key)
);

CREATE INDEX ix_vision_user_preference_user_last_observed
    ON vision_user_preference(user_id, last_observed_at DESC);

CREATE TABLE vision_memory_feedback_event (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    conversation_id BIGINT REFERENCES vision_conversation(id) ON DELETE CASCADE,
    turn_id BIGINT REFERENCES vision_turn(id) ON DELETE SET NULL,
    feedback_type VARCHAR(32) NOT NULL,
    intent VARCHAR(32),
    requested_slot VARCHAR(64),
    prompt TEXT,
    normalized_prompt TEXT,
    assistant_message TEXT,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_vision_memory_feedback_event_user_created
    ON vision_memory_feedback_event(user_id, created_at DESC);

CREATE TABLE vision_memory_summary (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    summary_kind VARCHAR(32) NOT NULL,
    summary_text TEXT NOT NULL,
    source_count INTEGER NOT NULL DEFAULT 0,
    source_window_started_at TIMESTAMP WITH TIME ZONE,
    source_window_ended_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_vision_memory_summary_user_created
    ON vision_memory_summary(user_id, created_at DESC);
