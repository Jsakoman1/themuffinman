create extension if not exists postgis;

create index if not exists idx_quest_location_geography
    on quest
    using gist ((ST_SetSRID(ST_MakePoint(location_longitude, location_latitude), 4326)::geography))
    where location_latitude is not null
      and location_longitude is not null;
