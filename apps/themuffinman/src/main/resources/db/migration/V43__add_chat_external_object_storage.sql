create table chat_attachment_upload (
    id bigserial primary key,
    uploaded_by_user_id bigint not null references app_user (id),
    storage_provider varchar(40) not null,
    storage_key varchar(500) not null unique,
    attachment_name varchar(255) not null,
    attachment_mime_type varchar(120) not null,
    attachment_size_bytes integer not null,
    consumed_message_id bigint references chat_message (id),
    created_at timestamp with time zone not null default now(),
    consumed_at timestamp with time zone
);

create index idx_chat_attachment_upload_uploaded_by_user_id
    on chat_attachment_upload (uploaded_by_user_id);

create index idx_chat_attachment_upload_consumed_at
    on chat_attachment_upload (consumed_at);

alter table chat_message
    add column attachment_storage_provider varchar(40),
    add column attachment_storage_key varchar(500);
