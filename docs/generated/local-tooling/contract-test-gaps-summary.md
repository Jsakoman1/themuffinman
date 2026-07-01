# Contract Test Gaps

- Generated at: `2026-07-01T14:49:51Z`
- Endpoints scanned: `99`
- Review needed: `10`
- High priority: `0`

- `medium` `GET /app_users/{id}/admin-detail` AppUserController gaps=missing_frontend_or_contract_usage_signal
- `medium` `GET /rides/offers` RideOfferController gaps=missing_frontend_or_contract_usage_signal
- `medium` `GET /things/listings` ThingSharingController gaps=missing_frontend_or_contract_usage_signal
- `medium` `POST /admin/agent/playground` AdminAgentController gaps=missing_frontend_or_contract_usage_signal
- `medium` `POST /admin/agent/simulate` AdminAgentController gaps=missing_frontend_or_contract_usage_signal
- `medium` `POST /rides/offers` RideOfferController gaps=missing_frontend_or_contract_usage_signal
- `medium` `POST /things/listings` ThingSharingController gaps=missing_frontend_or_contract_usage_signal
- `medium` `POST /things/listings/{listingId}/borrow-requests` ThingSharingController gaps=missing_frontend_or_contract_usage_signal
- `low` `GET /business/profiles` BusinessProfileController gaps=missing_frontend_or_contract_usage_signal
- `low` `GET /business/profiles/{slug}` BusinessProfileController gaps=missing_frontend_or_contract_usage_signal
