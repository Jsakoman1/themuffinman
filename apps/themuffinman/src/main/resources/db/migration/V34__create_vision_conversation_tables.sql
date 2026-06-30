CREATE TABLE vision_conversation (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    intent VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    requested_slot VARCHAR(64),
    slot_data TEXT NOT NULL DEFAULT '{}',
    last_user_prompt TEXT,
    last_normalized_prompt TEXT,
    last_assistant_message TEXT,
    last_translation_reliable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_turn_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_vision_conversation_owner_last_turn
    ON vision_conversation(owner_id, last_turn_at DESC);

CREATE TABLE vision_turn (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES vision_conversation(id) ON DELETE CASCADE,
    turn_index INTEGER NOT NULL,
    source VARCHAR(16) NOT NULL,
    prompt TEXT NOT NULL,
    normalized_prompt TEXT NOT NULL,
    detected_intent VARCHAR(32) NOT NULL,
    agent_state VARCHAR(32) NOT NULL,
    next_action VARCHAR(32) NOT NULL,
    requested_slot VARCHAR(64),
    translation_applied BOOLEAN NOT NULL DEFAULT FALSE,
    translation_reliable BOOLEAN NOT NULL DEFAULT TRUE,
    assistant_message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_vision_turn_conversation_turn UNIQUE (conversation_id, turn_index)
);

CREATE INDEX ix_vision_turn_conversation_turn_index
    ON vision_turn(conversation_id, turn_index);
