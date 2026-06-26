create table location_lookup_event (
    id bigserial primary key,
    provider varchar(64) not null,
    request_type varchar(32) not null,
    created_at timestamptz not null default now()
);

create index idx_location_lookup_event_created_at on location_lookup_event (created_at);
create index idx_location_lookup_event_request_type_created_at on location_lookup_event (request_type, created_at);
