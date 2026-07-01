# Chat Frontend Capsule

## Responsibility

Owns chat API access, shared chat composable state, and the standalone chat workspace.

## Main Entry Points

- API: `api/chatApi.ts`
- Composable: `composables/useAppChat.ts`
- Vision route: `modules/vision/views/VisionChatWorkspaceView.vue`

## Validation

- `npm run type-check`
- `npm run build`

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`

## Forbidden Shortcuts

- Do not fork chat state between multiple frontend entry surfaces.
- Do not create frontend-only access rules for conversations.
- Do not assume chat belongs only to one product route family.
