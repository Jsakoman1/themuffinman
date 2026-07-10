ALTER TABLE vision_conversation
    ADD COLUMN last_client_request_id VARCHAR(120);

CREATE INDEX ix_vision_conversation_owner_client_request
    ON vision_conversation(owner_id, last_client_request_id);
