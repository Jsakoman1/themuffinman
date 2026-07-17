ALTER TABLE chat_attachment_upload ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE;
UPDATE chat_attachment_upload SET expires_at = created_at + INTERVAL '10 minutes' WHERE expires_at IS NULL;
ALTER TABLE chat_attachment_upload ALTER COLUMN expires_at SET NOT NULL;
