# Vision Voice Frontend Integration

Purpose: connect `/vision` to real speech capabilities through the backend contract and browser-native APIs.

## Goal

Add browser-native speech recognition and text-to-speech to `/vision`, gated by backend config and runtime browser support.

## Scope

- `apps/themuffinman/frontend/src/modules/workmarket/api/clients/dashboardApi.ts`
- `apps/themuffinman/frontend/src/modules/workmarket/api/contracts.ts`
- `apps/themuffinman/frontend/src/modules/vision/composables/useVisionSurface.ts`
- `apps/themuffinman/frontend/src/modules/vision/views/VisionSurfaceView.vue`
- `apps/themuffinman/frontend/src/styles/vision-surface.css`

## Checklist

- [x] Read the backend voice config from the dashboard API surface.
- [x] Add browser speech recognition support with transcript syncing and fail-soft guards.
- [x] Add browser speech synthesis support with explicit speak/stop controls.
- [x] Reflect support, active state, and capability errors in the `/vision` UI.

## Validation

- `npm --prefix apps/themuffinman/frontend run type-check`
- `npm --prefix apps/themuffinman/frontend run build`

## Completion Evidence

- Status: complete
- Execution status: complete
- Validation:
  - `npm --prefix apps/themuffinman/frontend run generate:contracts`
  - `npm --prefix apps/themuffinman/frontend run type-check`
  - `npm --prefix apps/themuffinman/frontend run build`
- Notes:
  - `/vision` now fetches backend voice config in parallel with dashboard data.
  - Browser-native STT/TTS runs only when both backend capability flags and browser runtime support allow it.
  - The UI now shows listening/speaking state, support pills, runtime errors, and a generated spoken summary surface.
- Persistent backlog item: none
