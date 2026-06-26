alter table app_user
    add column location_mode varchar(32) not null default 'OFF',
    add column location_radius_km integer not null default 10,
    add column location_label varchar(255),
    add column location_country_code varchar(16),
    add column location_country varchar(120),
    add column location_locality varchar(120),
    add column location_postal_code varchar(32),
    add column location_street varchar(160),
    add column location_house_number varchar(32),
    add column location_latitude numeric(9, 6),
    add column location_longitude numeric(9, 6),
    add column location_updated_at timestamp with time zone;

alter table quest
    add column location_visibility varchar(32) not null default 'INHERIT',
    add column location_label varchar(255),
    add column location_country_code varchar(16),
    add column location_country varchar(120),
    add column location_locality varchar(120),
    add column location_postal_code varchar(32),
    add column location_street varchar(160),
    add column location_house_number varchar(32),
    add column location_latitude numeric(9, 6),
    add column location_longitude numeric(9, 6);

create index idx_quest_location_coordinates
    on quest(location_latitude, location_longitude);
