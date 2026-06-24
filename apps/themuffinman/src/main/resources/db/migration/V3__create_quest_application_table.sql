CREATE TABLE quest_application
(
    id             BIGSERIAL PRIMARY KEY,
    quest_id       BIGINT      NOT NULL,
    applicant_id   BIGINT      NOT NULL,
    message        VARCHAR(2000),
    proposed_price NUMERIC(10, 2),
    status         VARCHAR(50) NOT NULL,
    created_at     TIMESTAMP   NOT NULL,

    CONSTRAINT fk_quest_application_quest
        FOREIGN KEY (quest_id)
            REFERENCES quest (id),

    CONSTRAINT fk_quest_application_applicant
        FOREIGN KEY (applicant_id)
            REFERENCES app_user (id),

    CONSTRAINT uk_quest_application_quest_applicant
        UNIQUE (quest_id, applicant_id)
);