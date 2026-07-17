# Frontend UI/UX modernization analysis

## Scope

This batch covers the authenticated web application shell and its module surfaces.
The frontend remains a thin client: permissions, action availability, validation,
workflow transitions, visibility, and persistence stay in backend services and API
DTOs. The frontend owns presentation state only (open, selected, editing, loading,
and form draft state).

## Findings

- Navigation is present but visually text-heavy and does not consistently communicate
  the current surface or the primary action.
- Most module views repeat local button, input, status, card, eyebrow, and form CSS.
- Several cards expose a link or an action button, but do not have a consistent
  preview-first interaction model. Nested actions must stop propagation.
- Inline edit is implemented ad hoc in some surfaces and absent in others.
- Chat has the right backend interactions (conversation loading, sync, pagination,
  attachments, reactions, reply, edit, delete), but the layout does not yet read as
  a conversation-first product surface.
- Tiptap packages are already available, but there is no shared rich-text editor
  component used by the module forms.
- Empty, loading, error, privacy, and helper copy is duplicated across views.
- Basic global interaction affordances exist (`cursor: pointer`, focus ring), but
  hover, tooltip, disabled, destructive, and keyboard states are not standardized.

## Design decisions

1. Create a small shared UI kit under `modules/app-shell/components` rather than
   introduce a large design system abstraction.
2. Standardize controls through reusable button/icon-button, status, dialog/popover,
   inline-edit, and rich-text components.
3. Keep API DTOs and backend-provided `allowedActions` authoritative. Do not infer
   permissions from route names or duplicate business rules in Vue.
4. Use click-to-preview for discoverable cards and inline pencil actions for short
   editable fields. Use a dialog only when the edit/preview context is too large for
   the page.
5. Keep explanatory copy close to the relevant action, use `title`/tooltip only for
   icon-only controls, and remove repeated module introductions where the shell
   already provides context.
6. Make the desktop two-pane chat layout collapse into a usable mobile conversation
   flow while preserving all existing API actions and runtime selectors.

## Acceptance themes

- Every primary module surface has one obvious primary action.
- Every interactive card has a visible hover/focus state and a keyboard-accessible
  target; secondary actions do not accidentally open the card preview.
- Read-only fields can enter edit mode through a compact pencil control where the
  API supports editing.
- Rich text supports paragraph, bold, italic, underline, lists, links, and clear
  placeholder behavior through one shared component.
- Chat presents participants, message ownership, timestamps, attachments, replies,
  reactions, edit/delete, and composer state as one coherent thread.
- `npm run type-check`, `npm run build`, surface contract audits, and runtime smoke
  remain passing.

## Current verification evidence

- `npm --prefix apps/themuffinman/frontend run type-check` passed after the latest
  Circles, Work Applications, and Business booking dialog changes.
- `npm --prefix apps/themuffinman/frontend run build` passed with fresh generated
  Vision contracts.
- `npm --prefix apps/themuffinman/frontend run validate:modern-surface` passed with
  36 checks, including shared icon, dialog, status, rich-text, and preview contracts.
- `make audit-frontend` passed: 39 routes, 38 concrete surfaces, 0 placeholders,
  0 likely-unused frontend files, and 0 detached routes/callsites.
- `npm --prefix apps/themuffinman/frontend run web-runtime-smoke` passed after the
  latest dialog batch, covering authenticated navigation and existing runtime
  regression routes.
- `make work-verify plan=docs/work/frontend-ux-modernization.yaml` passed, and
  `make audit-docs` passed with no documentation review findings.

## Shared primitive inventory

The authenticated UI now uses a small vocabulary for repeated interaction patterns:

- `AppCard` provides keyboard-accessible whole-card activation while ignoring nested links, buttons, and form controls.
- `AppActionMenu` groups low-frequency actions behind an accessible disclosure.
- `AppFormField` and `AppFormFooter` standardize labels, hints, errors, and save/cancel alignment.
- `AppDialog`, `AppStatus`, `AppIconButton`, rich-text editor/preview, and inline edit remain the shared overlay, feedback, action, and content primitives.
- `base.css` owns semantic state tokens, focus rings, coarse-pointer targets, disabled affordances, and reduced-motion behavior.

## Final runtime evidence

- `npm --prefix apps/themuffinman/frontend run web-runtime-smoke` passed with authenticated browser coverage for Work, Things, Chat, Circles, People, Business, Calendar, Rides, profile/location recovery, cross-user visibility, and Vision review paths.
- The smoke records the current backend configuration honestly: Vision execution may return an explicit disabled response, which is accepted as a visible recovery state while direct web module flows remain fully exercised.
