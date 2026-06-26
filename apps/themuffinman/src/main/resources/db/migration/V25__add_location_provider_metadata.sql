alter table app_user
    add column location_provider varchar(64),
    add column location_provider_place_id varchar(255),
    add column location_resolved_at timestamp with time zone;

alter table quest
    add column location_provider varchar(64),
    add column location_provider_place_id varchar(255),
    add column location_resolved_at timestamp with time zone;

create index if not exists idx_app_user_location_provider_place_id
    on app_user(location_provider_place_id);

create index if not exists idx_quest_location_provider_place_id
    on quest(location_provider_place_id);
