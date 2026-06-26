alter table quest
    add column if not exists show_approved_applicants boolean not null default false;
