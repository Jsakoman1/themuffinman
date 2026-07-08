alter table chat_message
    add column if not exists attachment_data_url text,
    add column if not exists attachment_size_bytes integer;
