alter table quest
    add column scheduled_at timestamp with time zone null,
    add column term_fixed boolean not null default false,
    add column pending_scheduled_at timestamp with time zone null,
    add column pending_term_fixed boolean null,
    add column term_change_previous_status varchar(50) null;
