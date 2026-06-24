alter table app_user
    add column if not exists created_at timestamptz not null default now();
