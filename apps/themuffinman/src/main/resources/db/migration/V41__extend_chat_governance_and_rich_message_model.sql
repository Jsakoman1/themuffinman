alter table chat_message
    add column if not exists reply_to_message_id bigint,
    add column if not exists attachment_name varchar(255),
    add column if not exists attachment_mime_type varchar(120);

alter table chat_message
    add constraint fk_chat_message_reply_to_message
        foreign key (reply_to_message_id) references chat_message (id) on delete set null;

create index if not exists idx_chat_message_reply_to_message_id on chat_message (reply_to_message_id);

create table if not exists chat_message_reaction (
    id bigserial primary key,
    message_id bigint not null references chat_message (id) on delete cascade,
    user_id bigint not null references app_user (id) on delete cascade,
    emoji varchar(32) not null,
    created_at timestamptz not null default now(),
    constraint uk_chat_message_reaction_message_user_emoji unique (message_id, user_id, emoji)
);

create index if not exists idx_chat_message_reaction_message_created
    on chat_message_reaction (message_id, created_at desc);

create index if not exists idx_chat_message_reaction_user_created
    on chat_message_reaction (user_id, created_at desc);
