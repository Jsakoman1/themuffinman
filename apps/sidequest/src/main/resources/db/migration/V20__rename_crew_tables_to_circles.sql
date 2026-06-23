alter table crew rename to circle_group;

alter table crew_member rename to circle_membership;
alter table circle_membership rename column crew_id to circle_id;
alter table circle_membership rename constraint uk_crew_member_pair to uk_circle_membership_pair;

alter table quest_crew rename to quest_circle_group;
alter table quest_circle_group rename column crew_id to circle_id;

alter table circle_group rename constraint uk_crew_owner_name to uk_circle_owner_name;
