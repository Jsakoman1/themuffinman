alter table app_user
    add column exact_location_visibility_scope varchar(32) not null default 'NOBODY';

create table if not exists app_user_exact_location_circle (
    owner_user_id bigint not null references app_user(id) on delete cascade,
    circle_id bigint not null references circle_group(id) on delete cascade,
    primary key (owner_user_id, circle_id)
);

create table if not exists app_user_exact_location_user (
    owner_user_id bigint not null references app_user(id) on delete cascade,
    viewer_user_id bigint not null references app_user(id) on delete cascade,
    primary key (owner_user_id, viewer_user_id)
);

create index if not exists idx_app_user_exact_location_circle_owner
    on app_user_exact_location_circle(owner_user_id);

create index if not exists idx_app_user_exact_location_user_owner
    on app_user_exact_location_user(owner_user_id);
