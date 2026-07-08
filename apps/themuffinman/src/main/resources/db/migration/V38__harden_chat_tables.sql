alter table chat_message
    add column client_message_id varchar(80),
    add column updated_at timestamp with time zone not null default current_timestamp,
    add column edited_at timestamp with time zone,
    add column deleted_at timestamp with time zone;

alter table chat_conversation
    add column last_message_id bigint,
    add column last_message_deleted boolean not null default false;

alter table chat_conversation
    add constraint fk_chat_conversation_last_message
        foreign key (last_message_id) references chat_message(id) on delete set null;

create index idx_chat_message_conversation_id_desc
    on chat_message(conversation_id, id desc);

create unique index uk_chat_message_sender_client_message
    on chat_message(conversation_id, sender_user_id, client_message_id)
    where client_message_id is not null;
