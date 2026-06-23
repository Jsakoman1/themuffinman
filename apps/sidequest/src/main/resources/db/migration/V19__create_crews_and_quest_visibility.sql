create table crew
(
    id         bigserial primary key,
    owner_id   bigint      not null references app_user (id) on delete cascade,
    name       varchar(80) not null,
    created_at timestamp   not null default now(),
    constraint uk_crew_owner_name unique (owner_id, name)
);

create table crew_member
(
    id             bigserial primary key,
    crew_id        bigint    not null references crew (id) on delete cascade,
    member_user_id bigint    not null references app_user (id) on delete cascade,
    created_at     timestamp not null default now(),
    constraint uk_crew_member_pair unique (crew_id, member_user_id)
);

create table quest_crew
(
    quest_id bigint not null references quest (id) on delete cascade,
    crew_id  bigint not null references crew (id) on delete cascade,
    primary key (quest_id, crew_id)
);
