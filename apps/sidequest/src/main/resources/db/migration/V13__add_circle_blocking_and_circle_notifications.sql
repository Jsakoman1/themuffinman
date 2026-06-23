alter table circle_request
    add column if not exists blocked_at timestamp,
    add column if not exists blocked_by_user_id bigint;

alter table circle_request
    add constraint fk_circle_request_blocked_by
        foreign key (blocked_by_user_id) references app_user (id);

alter table quest_news_item
    alter column quest_id drop not null;

alter table quest_news_item
    alter column quest_title drop not null;
