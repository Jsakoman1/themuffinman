ALTER TABLE workmarket_application_news_outbox
    ALTER COLUMN event_id TYPE UUID USING event_id::uuid;
