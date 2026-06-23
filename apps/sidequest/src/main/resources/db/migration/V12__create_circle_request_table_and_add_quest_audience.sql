ALTER TABLE quest
    ADD COLUMN audience VARCHAR(50) NOT NULL DEFAULT 'CIRCLES';

CREATE TABLE circle_request (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    accepted_at TIMESTAMP NULL,

    CONSTRAINT fk_circle_request_requester
        FOREIGN KEY (requester_id)
            REFERENCES app_user (id),

    CONSTRAINT fk_circle_request_recipient
        FOREIGN KEY (recipient_id)
            REFERENCES app_user (id),

    CONSTRAINT uk_circle_request_pair
        UNIQUE (requester_id, recipient_id)
);
