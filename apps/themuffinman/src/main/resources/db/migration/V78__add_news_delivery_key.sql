ALTER TABLE quest_news_item ADD COLUMN delivery_key VARCHAR(255);
CREATE UNIQUE INDEX uq_quest_news_item_delivery_key ON quest_news_item (delivery_key);
