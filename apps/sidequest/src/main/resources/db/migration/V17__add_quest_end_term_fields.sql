alter table quest
    add column ends_at timestamp with time zone null,
    add column pending_ends_at timestamp with time zone null;

update quest
set ends_at = null,
    pending_ends_at = null
where true;
