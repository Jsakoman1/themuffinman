create table quest_news_item (
    id bigserial primary key,
    recipient_user_id bigint not null,
    actor_user_id bigint not null,
    actor_username varchar(255) not null,
    quest_id bigint not null,
    quest_title varchar(255) not null,
    application_id bigint null,
    type varchar(50) not null,
    title varchar(255) not null,
    message varchar(2000) not null,
    created_at timestamptz not null default now()
);
