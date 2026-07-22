-- Small, readable local fixture set for manual UI/Vision testing.
-- Requires the seeded local accounts: jsak (id 3), Nikol (id 5), test (id 7).

BEGIN;
SELECT pg_advisory_xact_lock(91420260720);

DO $$
DECLARE
    jsak_id bigint;
    nikol_id bigint;
    test_id bigint;
    lawn_id bigint;
    tap_id bigint;
    bookcase_id bigint;
    photos_id bigint;
    wardrobe_id bigint;
BEGIN
    SELECT id INTO jsak_id FROM app_user WHERE username = 'jsak';
    SELECT id INTO nikol_id FROM app_user WHERE username = 'Nikol';
    SELECT id INTO test_id FROM app_user WHERE username = 'test';

    IF jsak_id IS NULL OR nikol_id IS NULL OR test_id IS NULL THEN
        RAISE EXCEPTION 'Expected local fixture accounts jsak, Nikol and test were not found';
    END IF;

    INSERT INTO quest (creator_id, title, description, award_amount, status, term_fixed, audience,
                       location_visibility, location_label, location_country_code, location_country,
                       location_locality, location_source, show_approved_applicants)
    VALUES
        (jsak_id, 'Mow the lawn before the weekend barbecue',
         'Mow the front and back lawn, trim the edges, and leave the clippings in the green-waste bin.',
         45.00, 'OPEN', false, 'EVERYONE', 'APPROXIMATE', 'Wipkingen, Zurich', 'CH', 'Switzerland', 'Zurich', 'CUSTOM', true)
    RETURNING id INTO lawn_id;

    INSERT INTO quest (creator_id, title, description, award_amount, status, term_fixed, audience,
                       location_visibility, location_label, location_country_code, location_country,
                       location_locality, location_source, show_approved_applicants)
    VALUES
        (jsak_id, 'Repair a leaking kitchen tap',
         'Replace the worn cartridge or washer in a standard kitchen mixer tap and check that it no longer drips.',
         80.00, 'OPEN', false, 'EVERYONE', 'APPROXIMATE', 'Wipkingen, Zurich', 'CH', 'Switzerland', 'Zurich', 'CUSTOM', true)
    RETURNING id INTO tap_id;

    INSERT INTO quest (creator_id, title, description, award_amount, status, term_fixed, audience,
                       location_visibility, location_label, location_country_code, location_country,
                       location_locality, location_source, show_approved_applicants)
    VALUES
        (jsak_id, 'Move two bookcases upstairs',
         'Carry two empty bookcases from the ground floor to the second floor. Bring a second pair of hands and a blanket for the stairs.',
         60.00, 'OPEN', false, 'EVERYONE', 'APPROXIMATE', 'Wipkingen, Zurich', 'CH', 'Switzerland', 'Zurich', 'CUSTOM', true)
    RETURNING id INTO bookcase_id;

    INSERT INTO quest (creator_id, title, description, award_amount, status, term_fixed, audience,
                       location_visibility, location_label, location_country_code, location_country,
                       location_locality, location_source, show_approved_applicants)
    VALUES
        (jsak_id, 'Photograph a bright two-room apartment',
         'Take a short set of natural-light photos for a rental listing. Smartphone photos are fine; focus on rooms, balcony, and floor plan.',
         120.00, 'OPEN', false, 'EVERYONE', 'APPROXIMATE', 'Wipkingen, Zurich', 'CH', 'Switzerland', 'Zurich', 'CUSTOM', true)
    RETURNING id INTO photos_id;

    INSERT INTO quest (creator_id, title, description, award_amount, status, term_fixed, audience,
                       location_visibility, location_label, location_country_code, location_country,
                       location_locality, location_source, show_approved_applicants)
    VALUES
        (jsak_id, 'Assemble a flat-pack wardrobe',
         'Assemble one medium-size flat-pack wardrobe, level it, and anchor it safely to the wall. Tools are available on site.',
         75.00, 'OPEN', false, 'EVERYONE', 'APPROXIMATE', 'Wipkingen, Zurich', 'CH', 'Switzerland', 'Zurich', 'CUSTOM', true)
    RETURNING id INTO wardrobe_id;

    INSERT INTO quest_application (quest_id, applicant_id, message, proposed_price, status)
    VALUES
        (lawn_id, nikol_id, 'I have my own mower and can do the edges carefully.', 45.00, 'PENDING'),
        (tap_id, nikol_id, 'I have repaired similar mixer taps and can bring basic tools.', 80.00, 'PENDING'),
        (bookcase_id, test_id, 'I can bring a second person and protective blankets for the stairs.', 60.00, 'APPROVED'),
        (photos_id, nikol_id, 'I can take and select a clean set of apartment photos in daylight.', 120.00, 'PENDING'),
        (wardrobe_id, test_id, 'I am comfortable with flat-pack assembly and wall anchoring.', 75.00, 'PENDING');

    INSERT INTO thing_listing (owner_id, title, description, condition_note, available, archived)
    VALUES
        (jsak_id, 'Cordless drill and bit set',
         '18V cordless drill with charger and a complete wood, metal, and masonry bit set. Available for short home projects.',
         'Good working condition; battery holds charge.', true, false),
        (jsak_id, 'Two-person camping tent',
         'Lightweight two-person tent with footprint and compact carry bag for weekend camping trips.',
         'Clean and waterproof after the last use.', true, false);

    INSERT INTO thing_borrow_request (listing_id, borrower_id, message, status)
    SELECT id, nikol_id, 'Could I borrow this for assembling shelves next Saturday?', 'PENDING'
    FROM thing_listing
    WHERE owner_id = jsak_id AND title = 'Cordless drill and bit set';

    INSERT INTO ride_offer (driver_id, origin, destination, departure_at, seats, note, active, status)
    VALUES
        (jsak_id, 'Zurich HB', 'Winterthur station', now() + interval '3 days', 2,
         'Leaving after work; small bags are fine. Please be ready five minutes early.', true, 'OPEN');

    INSERT INTO business_profile (owner_id, business_name, slug, headline, description,
                                  contact_email, active, timezone, booking_enabled, public_address_label)
    VALUES
        (jsak_id, 'Jsak Home Repairs', 'jsak-home-repairs',
         'Small home repairs, done carefully',
         'Help with taps, shelves, flat-pack furniture, and other practical jobs around the home.',
         'jsakoman1@icloud.com', true, 'Europe/Zurich', true, 'Wipkingen, Zurich');

    INSERT INTO business_offering (business_profile_id, title, slug, summary, description,
                                   pricing_type, base_price_amount, base_price_currency,
                                   duration_mode, default_duration_minutes, min_duration_minutes,
                                   max_duration_minutes, capacity_mode, slot_capacity, booking_mode,
                                   requires_owner_confirmation, active, sort_order)
    SELECT id, 'Kitchen tap repair', 'kitchen-tap-repair',
           'Diagnosis and repair of a standard kitchen mixer tap.',
           'A one-hour visit to diagnose a dripping tap, replace a washer or cartridge where possible,
            and confirm the connection is leak-free.',
           'FIXED', 80.00, 'CHF', 'FIXED', 60, 60, 60, 'SINGLE', 1, 'REQUEST', true, true, 1
    FROM business_profile
    WHERE owner_id = jsak_id AND slug = 'jsak-home-repairs';
END $$;

COMMIT;
