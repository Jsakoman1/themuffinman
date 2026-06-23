alter table quest
    add column assignee_target integer null;

update quest
set assignee_target = 1
where assignee_target is null;
