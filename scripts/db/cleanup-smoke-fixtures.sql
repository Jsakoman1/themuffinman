-- Removes generated local smoke data without touching real local accounts.
-- Scope: the audited local database keeps only ids 3, 4, 5 and 7 as real local
-- accounts; every other account is generated smoke/runtime data.
-- Run only against a disposable/local database.

BEGIN;
SELECT pg_advisory_xact_lock(91420260720);

CREATE TEMP TABLE smoke_users ON COMMIT DROP AS
SELECT id
FROM app_user
WHERE id NOT IN (3, 4, 5, 7);

CREATE TEMP TABLE smoke_quests ON COMMIT DROP AS
SELECT id FROM quest WHERE creator_id IN (SELECT id FROM smoke_users);

CREATE TEMP TABLE smoke_applications ON COMMIT DROP AS
SELECT id FROM quest_application
WHERE applicant_id IN (SELECT id FROM smoke_users)
   OR quest_id IN (SELECT id FROM smoke_quests);

CREATE TEMP TABLE smoke_circles ON COMMIT DROP AS
SELECT id FROM circle_group WHERE owner_id IN (SELECT id FROM smoke_users);

CREATE TEMP TABLE smoke_business_profiles ON COMMIT DROP AS
SELECT id FROM business_profile WHERE owner_id IN (SELECT id FROM smoke_users);

CREATE TEMP TABLE smoke_business_offerings ON COMMIT DROP AS
SELECT id FROM business_offering
WHERE business_profile_id IN (SELECT id FROM smoke_business_profiles);

CREATE TEMP TABLE smoke_bookings ON COMMIT DROP AS
SELECT id FROM business_booking
WHERE customer_user_id IN (SELECT id FROM smoke_users)
   OR business_profile_id IN (SELECT id FROM smoke_business_profiles)
   OR business_offering_id IN (SELECT id FROM smoke_business_offerings);

CREATE TEMP TABLE smoke_ride_offers ON COMMIT DROP AS
SELECT id FROM ride_offer WHERE driver_id IN (SELECT id FROM smoke_users);

CREATE TEMP TABLE smoke_listings ON COMMIT DROP AS
SELECT id FROM thing_listing WHERE owner_id IN (SELECT id FROM smoke_users);

CREATE TEMP TABLE smoke_vision_conversations ON COMMIT DROP AS
SELECT id FROM vision_conversation WHERE owner_id IN (SELECT id FROM smoke_users);

CREATE TEMP TABLE smoke_chat_conversations ON COMMIT DROP AS
SELECT id FROM chat_conversation
WHERE owner_user_id IN (SELECT id FROM smoke_users)
   OR created_by_user_id IN (SELECT id FROM smoke_users)
   OR last_message_sender_id IN (SELECT id FROM smoke_users)
   OR left_participant_id IN (SELECT id FROM smoke_users)
   OR right_participant_id IN (SELECT id FROM smoke_users)
   OR id IN (
       SELECT conversation_id FROM chat_conversation_participant
       WHERE user_id IN (SELECT id FROM smoke_users)
          OR added_by_user_id IN (SELECT id FROM smoke_users)
   )
   OR id IN (
       SELECT conversation_id FROM chat_conversation_member_state
       WHERE user_id IN (SELECT id FROM smoke_users)
   )
   OR id IN (
       SELECT conversation_id FROM chat_message
       WHERE sender_user_id IN (SELECT id FROM smoke_users)
   );

CREATE TEMP TABLE smoke_chat_messages ON COMMIT DROP AS
SELECT id FROM chat_message
WHERE conversation_id IN (SELECT id FROM smoke_chat_conversations)
   OR sender_user_id IN (SELECT id FROM smoke_users);

-- Business domain: remove children before profiles and offerings.
DELETE FROM business_booking_audit_event
WHERE business_booking_id IN (SELECT id FROM smoke_bookings)
   OR actor_user_id IN (SELECT id FROM smoke_users);
DELETE FROM business_booking WHERE id IN (SELECT id FROM smoke_bookings);
DELETE FROM business_availability_exception
WHERE business_profile_id IN (SELECT id FROM smoke_business_profiles)
   OR business_offering_id IN (SELECT id FROM smoke_business_offerings);
DELETE FROM business_availability_rule
WHERE business_profile_id IN (SELECT id FROM smoke_business_profiles)
   OR business_offering_id IN (SELECT id FROM smoke_business_offerings);
DELETE FROM business_booking_policy
WHERE business_profile_id IN (SELECT id FROM smoke_business_profiles);
DELETE FROM business_favorite
WHERE owner_id IN (SELECT id FROM smoke_users)
   OR business_profile_id IN (SELECT id FROM smoke_business_profiles);
DELETE FROM business_gallery_image
WHERE business_profile_id IN (SELECT id FROM smoke_business_profiles);
DELETE FROM business_offering
WHERE id IN (SELECT id FROM smoke_business_offerings);
DELETE FROM business_profile
WHERE id IN (SELECT id FROM smoke_business_profiles);

-- Chat domain: conversations containing generated participants/messages are generated data.
DELETE FROM chat_attachment_upload
WHERE uploaded_by_user_id IN (SELECT id FROM smoke_users)
   OR consumed_message_id IN (SELECT id FROM smoke_chat_messages);
DELETE FROM chat_message_reaction
WHERE user_id IN (SELECT id FROM smoke_users)
   OR message_id IN (SELECT id FROM smoke_chat_messages);
DELETE FROM chat_audit_event
WHERE user_id IN (SELECT id FROM smoke_users)
   OR conversation_id IN (SELECT id FROM smoke_chat_conversations);
DELETE FROM chat_message WHERE id IN (SELECT id FROM smoke_chat_messages);
DELETE FROM chat_conversation_member_state
WHERE user_id IN (SELECT id FROM smoke_users)
   OR conversation_id IN (SELECT id FROM smoke_chat_conversations);
DELETE FROM chat_conversation_participant
WHERE user_id IN (SELECT id FROM smoke_users)
   OR added_by_user_id IN (SELECT id FROM smoke_users)
   OR conversation_id IN (SELECT id FROM smoke_chat_conversations);
DELETE FROM chat_presence WHERE user_id IN (SELECT id FROM smoke_users);
DELETE FROM chat_conversation WHERE id IN (SELECT id FROM smoke_chat_conversations);

-- Circles and circle visibility links.
DELETE FROM app_user_exact_location_circle
WHERE owner_user_id IN (SELECT id FROM smoke_users)
   OR circle_id IN (SELECT id FROM smoke_circles);
DELETE FROM app_user_profile_avatar_circle
WHERE owner_user_id IN (SELECT id FROM smoke_users)
   OR circle_id IN (SELECT id FROM smoke_circles);
DELETE FROM app_user_profile_description_circle
WHERE owner_user_id IN (SELECT id FROM smoke_users)
   OR circle_id IN (SELECT id FROM smoke_circles);
DELETE FROM circle_request
WHERE requester_id IN (SELECT id FROM smoke_users)
   OR recipient_id IN (SELECT id FROM smoke_users)
   OR blocked_by_user_id IN (SELECT id FROM smoke_users);
DELETE FROM circle_membership
WHERE member_user_id IN (SELECT id FROM smoke_users)
   OR circle_id IN (SELECT id FROM smoke_circles);
DELETE FROM quest_circle_group
WHERE quest_id IN (SELECT id FROM smoke_quests)
   OR circle_id IN (SELECT id FROM smoke_circles);
DELETE FROM ride_offer_visible_circle
WHERE circle_id IN (SELECT id FROM smoke_circles)
   OR ride_offer_id IN (SELECT id FROM smoke_ride_offers);
DELETE FROM circle_group WHERE id IN (SELECT id FROM smoke_circles);

-- Work domain and its generated notifications/media/reviews.
DELETE FROM quest_news_item
WHERE recipient_user_id IN (SELECT id FROM smoke_users)
   OR actor_user_id IN (SELECT id FROM smoke_users)
   OR quest_id IN (SELECT id FROM smoke_quests)
   OR application_id IN (SELECT id FROM smoke_applications)
   OR circle_request_id IN (
       SELECT id FROM circle_request
       WHERE requester_id IN (SELECT id FROM smoke_users)
          OR recipient_id IN (SELECT id FROM smoke_users)
   );
DELETE FROM user_review
WHERE quest_id IN (SELECT id FROM smoke_quests)
   OR reviewer_user_id IN (SELECT id FROM smoke_users)
   OR reviewed_user_id IN (SELECT id FROM smoke_users);
DELETE FROM quest_image WHERE quest_id IN (SELECT id FROM smoke_quests);
DELETE FROM quest_application WHERE id IN (SELECT id FROM smoke_applications);
DELETE FROM quest WHERE id IN (SELECT id FROM smoke_quests);

-- Rides and things.
DELETE FROM ride_audit_event
WHERE ride_offer_id IN (SELECT id FROM smoke_ride_offers)
   OR actor_id IN (SELECT id FROM smoke_users)
   OR participant_id IN (
       SELECT id FROM ride_participant WHERE passenger_id IN (SELECT id FROM smoke_users)
   );
DELETE FROM ride_participant
WHERE passenger_id IN (SELECT id FROM smoke_users)
   OR ride_offer_id IN (SELECT id FROM smoke_ride_offers);
DELETE FROM ride_offer WHERE id IN (SELECT id FROM smoke_ride_offers);
DELETE FROM thing_borrow_request
WHERE borrower_id IN (SELECT id FROM smoke_users)
   OR listing_id IN (SELECT id FROM smoke_listings);
DELETE FROM thing_listing WHERE id IN (SELECT id FROM smoke_listings);

-- Vision and user-level generated state.
DELETE FROM vision_memory_feedback_event
WHERE user_id IN (SELECT id FROM smoke_users)
   OR conversation_id IN (SELECT id FROM smoke_vision_conversations)
   OR turn_id IN (SELECT id FROM vision_turn WHERE conversation_id IN (SELECT id FROM smoke_vision_conversations));
DELETE FROM vision_turn WHERE conversation_id IN (SELECT id FROM smoke_vision_conversations);
DELETE FROM vision_conversation WHERE id IN (SELECT id FROM smoke_vision_conversations);
DELETE FROM vision_memory_summary WHERE user_id IN (SELECT id FROM smoke_users);
DELETE FROM vision_user_preference WHERE user_id IN (SELECT id FROM smoke_users);

-- The retained local accounts only contain probe prompts in Vision history in
-- this audited database; clear that test history as well.
CREATE TEMP TABLE all_vision_conversations ON COMMIT DROP AS
SELECT id FROM vision_conversation;
DELETE FROM vision_memory_feedback_event
WHERE conversation_id IN (SELECT id FROM all_vision_conversations)
   OR turn_id IN (SELECT id FROM vision_turn);
DELETE FROM vision_turn;
DELETE FROM vision_conversation;
DELETE FROM vision_memory_summary;
DELETE FROM vision_user_preference;

DELETE FROM app_user_exact_location_user
WHERE owner_user_id IN (SELECT id FROM smoke_users)
   OR viewer_user_id IN (SELECT id FROM smoke_users);
DELETE FROM appearance_preference WHERE owner_id IN (SELECT id FROM smoke_users);
DELETE FROM activity_resume_dismissal WHERE user_id IN (SELECT id FROM smoke_users);
DELETE FROM commute_preference WHERE user_id IN (SELECT id FROM smoke_users);
DELETE FROM native_handoff_token WHERE user_id IN (SELECT id FROM smoke_users);
DELETE FROM notification_preference WHERE user_id IN (SELECT id FROM smoke_users);
DELETE FROM onboarding_progress WHERE user_id IN (SELECT id FROM smoke_users);
DELETE FROM password_recovery_token WHERE user_id IN (SELECT id FROM smoke_users);
DELETE FROM personal_workspace_shortcut WHERE owner_id IN (SELECT id FROM smoke_users);
DELETE FROM profile_gallery_image WHERE owner_id IN (SELECT id FROM smoke_users);
DELETE FROM safety_report
WHERE reporter_id IN (SELECT id FROM smoke_users)
   OR target_user_id IN (SELECT id FROM smoke_users);
DELETE FROM saved_search_intent WHERE owner_id IN (SELECT id FROM smoke_users);
DELETE FROM workspace_rail_preference WHERE owner_id IN (SELECT id FROM smoke_users);

-- These are standalone operational records produced by smoke runs, with no user FK.
DELETE FROM location_lookup_event;

DELETE FROM app_user WHERE id IN (SELECT id FROM smoke_users);

COMMIT;
