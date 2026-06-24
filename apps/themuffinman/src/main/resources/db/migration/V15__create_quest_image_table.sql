create table quest_image
(
    quest_id    bigint       not null,
    sort_order  integer      not null,
    image_data_url text      not null,

    constraint fk_quest_image_quest
        foreign key (quest_id)
            references quest (id)
            on delete cascade
);
