CREATE TABLE thing_listing (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    title VARCHAR(140) NOT NULL,
    description TEXT,
    condition_note VARCHAR(180),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_thing_listing_available_created ON thing_listing(available, created_at DESC);
CREATE INDEX ix_thing_listing_owner ON thing_listing(owner_id);

CREATE TABLE thing_borrow_request (
    id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL REFERENCES thing_listing(id) ON DELETE CASCADE,
    borrower_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    message TEXT,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_thing_borrow_request_listing ON thing_borrow_request(listing_id);
CREATE INDEX ix_thing_borrow_request_borrower_status ON thing_borrow_request(borrower_id, status);
CREATE UNIQUE INDEX ux_thing_borrow_request_pending ON thing_borrow_request(listing_id, borrower_id, status);
