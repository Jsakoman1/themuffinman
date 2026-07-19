CREATE TABLE app_user_profile_description_circle (
    owner_user_id BIGINT NOT NULL,
    circle_id BIGINT NOT NULL,
    CONSTRAINT pk_app_user_profile_description_circle PRIMARY KEY (owner_user_id, circle_id),
    CONSTRAINT fk_profile_description_circle_owner FOREIGN KEY (owner_user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_profile_description_circle_circle FOREIGN KEY (circle_id) REFERENCES circle_group(id) ON DELETE CASCADE
);

CREATE TABLE app_user_profile_avatar_circle (
    owner_user_id BIGINT NOT NULL,
    circle_id BIGINT NOT NULL,
    CONSTRAINT pk_app_user_profile_avatar_circle PRIMARY KEY (owner_user_id, circle_id),
    CONSTRAINT fk_profile_avatar_circle_owner FOREIGN KEY (owner_user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_profile_avatar_circle_circle FOREIGN KEY (circle_id) REFERENCES circle_group(id) ON DELETE CASCADE
);
