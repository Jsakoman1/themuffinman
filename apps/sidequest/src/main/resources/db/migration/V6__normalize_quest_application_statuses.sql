UPDATE quest_application
SET status = 'APPROVED'
WHERE status = 'ACCEPTED';

UPDATE quest_application
SET status = 'DECLINED'
WHERE status = 'REJECTED';
