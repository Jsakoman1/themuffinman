CREATE TABLE quest
(
    id           BIGSERIAL PRIMARY KEY,
    creator_id   BIGINT       NOT NULL,
    title        VARCHAR(255) NOT NULL,
    description  VARCHAR(2000),
    award_amount NUMERIC(10, 2),
    status       VARCHAR(50)  NOT NULL,

    CONSTRAINT fk_quest_creator
        FOREIGN KEY (creator_id)
            REFERENCES app_user (id)
);