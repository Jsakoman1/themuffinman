CREATE TABLE thing_wishlist_item (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES app_user(id),
    listing_id BIGINT NOT NULL REFERENCES thing_listing(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_thing_wishlist_owner_listing UNIQUE (owner_id, listing_id)
);

CREATE TABLE thing_wishlist_item_circle (
    wishlist_item_id BIGINT NOT NULL REFERENCES thing_wishlist_item(id) ON DELETE CASCADE,
    circle_id BIGINT NOT NULL REFERENCES circle_group(id) ON DELETE CASCADE,
    PRIMARY KEY (wishlist_item_id, circle_id)
);

CREATE INDEX idx_thing_wishlist_owner ON thing_wishlist_item(owner_id);
CREATE INDEX idx_thing_wishlist_circle ON thing_wishlist_item_circle(circle_id);
