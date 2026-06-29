CREATE TABLE ride_offer (
    id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    origin VARCHAR(140) NOT NULL,
    destination VARCHAR(140) NOT NULL,
    departure_at TIMESTAMP WITH TIME ZONE NOT NULL,
    seats INTEGER NOT NULL DEFAULT 1,
    note TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_ride_offer_active_departure ON ride_offer(active, departure_at);
CREATE INDEX ix_ride_offer_driver ON ride_offer(driver_id);

CREATE TABLE ride_offer_visible_circle (
    ride_offer_id BIGINT NOT NULL REFERENCES ride_offer(id) ON DELETE CASCADE,
    circle_id BIGINT NOT NULL REFERENCES circle_group(id) ON DELETE CASCADE,
    PRIMARY KEY (ride_offer_id, circle_id)
);
