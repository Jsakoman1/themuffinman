# Location Services Setup

## What is already prepared

- User profile now stores:
  - privacy mode: `OFF`, `APPROXIMATE`, `EXACT`
  - default search radius in km
  - location label, address parts, latitude, longitude
- Quest create/edit now stores:
  - quest location visibility: `INHERIT`, `OFF`, `APPROXIMATE`, `EXACT`
- Find jobs now supports:
  - radius filtering based on the current user's saved location
- Backend now exposes provider-ready lookup endpoint:
  - `POST /location/lookup`
  - `POST /location/reverse-lookup`

## Recommended provider

- Provider: `Geoapify`
- Why:
  - simple geocoding API
  - free daily tier for development
  - easy upgrade path

## Geoapify account steps

1. Create an account at `https://www.geoapify.com/`
2. Open the Geoapify dashboard
3. Create a new project for `TheMuffinMan`
4. Create an API key
5. Enable geocoding/search usage for that key
6. Copy the API key

## Local backend setup

Add these environment variables before starting the backend:

```bash
export SIDEQUEST_LOCATION_PROVIDER=geoapify
export SIDEQUEST_GEOAPIFY_API_KEY=your_api_key_here
```

Optional:

```bash
export SIDEQUEST_GEOAPIFY_BASE_URL=https://api.geoapify.com/v1
```

## PostgreSQL spatial support

Nearby quest search now uses `PostGIS`.

What that means:
- `PostGIS` is free and open-source
- the database must have the `postgis` extension available
- the app enables the extension automatically through Flyway migration `V24`

If your local PostgreSQL does not have `PostGIS` installed yet, install it first, then start the backend again.

## Start the backend

```bash
cd apps/themuffinman
make dev
```

Backend only:

```bash
cd apps/themuffinman
make backend-dev
```

## Quick verification

Call the lookup endpoint:

```bash
curl -X POST http://localhost:8080/location/lookup \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -d '{"query":"Ilica 1, Zagreb"}'
```

Expected result:

- `configured: true`
- `provider: "geoapify"`
- one or more lookup candidates with `label`, `latitude`, `longitude`

## Current UX flow

1. Open `Profile -> Settings`
2. Set `Location mode`
3. Enter search radius
4. Search for an address or use the current location button
5. Save profile
6. Create or edit a quest
7. Choose `Quest location`
8. Open `Find jobs`
9. Turn on `Only nearby` to use your saved profile radius

## Important note

The profile form now supports search suggestions and reverse lookup from the browser's current location.
Manual address fields remain available as a fallback.

The authenticated profile settings surface now performs address search and current-device reverse lookup directly. A resolved candidate must be selected before an enabled location mode can be saved; when browser permission is denied, manual search remains available.
