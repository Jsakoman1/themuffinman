ALTER TABLE app_user
    ADD COLUMN profile_description_visibility VARCHAR(16) NOT NULL DEFAULT 'PUBLIC',
    ADD COLUMN profile_avatar_visibility VARCHAR(16) NOT NULL DEFAULT 'PUBLIC';
