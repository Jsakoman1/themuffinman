ALTER TABLE ride_offer ADD COLUMN status VARCHAR(24) NOT NULL DEFAULT 'OPEN';
ALTER TABLE ride_offer ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE ride_offer ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE ride_offer ADD COLUMN started_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE ride_offer ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE ride_offer ADD COLUMN cancelled_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE ride_offer ADD COLUMN cancel_reason VARCHAR(500);
UPDATE ride_offer SET status = CASE WHEN active THEN 'OPEN' ELSE 'CANCELLED' END;
CREATE INDEX ix_ride_offer_status_departure ON ride_offer(status, departure_at);

CREATE TABLE ride_participant (
    id BIGSERIAL PRIMARY KEY,
    ride_offer_id BIGINT NOT NULL REFERENCES ride_offer(id) ON DELETE CASCADE,
    passenger_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    status VARCHAR(24) NOT NULL DEFAULT 'JOINED',
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    left_at TIMESTAMP WITH TIME ZONE,
    UNIQUE (ride_offer_id, passenger_id)
);
CREATE INDEX ix_ride_participant_ride_status ON ride_participant(ride_offer_id, status);
CREATE INDEX ix_ride_participant_passenger ON ride_participant(passenger_id);

CREATE TABLE ride_audit_event (
    id BIGSERIAL PRIMARY KEY,
    ride_offer_id BIGINT NOT NULL REFERENCES ride_offer(id) ON DELETE CASCADE,
    actor_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    event_type VARCHAR(40) NOT NULL,
    from_status VARCHAR(24),
    to_status VARCHAR(24),
    participant_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL,
    details VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_ride_audit_ride_created ON ride_audit_event(ride_offer_id, created_at);
