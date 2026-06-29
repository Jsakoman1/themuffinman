# Chat Frontend Capsule

## Responsibility

Owns chat API access, shared chat composable state, global chat tray integration, and the standalone chat workspace.

## Main Entry Points

- API: `api/chatApi.ts`
- Composable: `composables/useAppChat.ts`
- View: `views/ChatWorkspaceView.vue`
- Global tray component: `src/components/app/AppChatTray.vue`

## Validation

- `npm run type-check`
- `npm run build`

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`

## Forbidden Shortcuts

- Do not fork chat state between tray and workspace.
- Do not create frontend-only access rules for conversations.
- Do not assume chat belongs only to workmarket routes.
