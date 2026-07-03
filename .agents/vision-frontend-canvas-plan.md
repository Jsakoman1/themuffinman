---
machine_kind: plan
machine_status: planning only. do not implement until explicitly requested
machine_title: Vision Frontend Canvas Plan
machine_goal: 'Evolve `/vision` into the future primary frontend: a white blank canvas
  that adapts to the user''s current task and reveals only the UI needed for the next
  step.'
---

# Vision Frontend Canvas Plan

## Status

Planning only. Do not implement until explicitly requested.

## Purpose

Evolve `/vision` into the future primary frontend: a white blank canvas that adapts to the user's current task and reveals only the UI needed for the next step.

## Current Frontend State

Current `/vision` already has:
- central animated agent
- bottom prompt composer
- text and voice input
- backend prompt processing

It still needs:
- backend-driven canvas blocks
- true stepwise field reveal
- white/minimal visual language
- removal of dashboard-card assumptions
- stronger separation from legacy screens

## Frontend Architecture

Recommended structure:
- `modules/vision/views/VisionSurfaceModernView.vue`
- `modules/vision/composables/useVisionConversation.ts`
- `modules/vision/components/VisionAgent.vue`
- `modules/vision/components/VisionPromptDock.vue`
- `modules/vision/components/VisionCanvasRenderer.vue`
- `modules/vision/components/blocks/*`
- `modules/vision/api/visionApi.ts`
- `modules/vision/types/canvas.ts`

Purpose:
- Keep the view thin.
- Keep API calls in one client.
- Keep adaptive block rendering reusable.
- Keep motion and display separate from orchestration logic.

## Rendering Model

The frontend receives `canvasMode`, `agentState`, `nextAction`, and `blocks`.

Rendering rules:
- `blank`: show only agent and optional prompt affordance.
- `prompt`: show prompt dock.
- `clarification`: show agent, one question, one focused input or option set.
- `results`: show compressed result summary first.
- `review`: show compact action review and confirm/cancel.
- `complete`: show compact success state and next step.
- `blocked`: show clear stop reason and safe recovery option.

The frontend should not infer:
- which fields are required
- whether an action is safe
- which backend use case to call
- whether target resolution is exact

## Visual Direction

Target:
- white or near-white background
- sparse canvas
- strong typography, but not oversized where it hurts task clarity
- subtle motion linked to state
- minimal controls
- no dashboard chrome
- no generic cards unless they represent repeated result items or a review object
- no modal/dialog-first workflow

State motion:
- idle: soft living motion
- listening: focused audio-reactive pulse
- thinking: restrained processing movement
- asking: prompt field appears
- reviewing: content sharpens into a compact review
- executing: short progress state
- complete: collapse to success and next step
- blocked: stop state with one recovery action

## Prompt And Field Behavior

Prompt dock:
- hidden in blank state
- appears on focus, voice start, or backend request
- collapses after completion unless follow-up is expected

Field reveal:
- show one field or one small choice set at a time
- keep labels short
- avoid explanatory help text in the UI
- let validation messages be specific and brief

Examples:
- "Title"
- "When?"
- "Reward"
- "Who can see it?"
- "Review"

## Avoided Patterns

- Long static forms.
- Wizard pages with many visible steps.
- Dashboard cards as the default layout.
- Permanent sidebars.
- Nested cards.
- Marketing-style hero page.
- Frontend-only intent mapping.
- Window/dialog metaphors for the main task.

## Accessibility And Practical Constraints

- Text input must remain available when voice is unavailable.
- Audio feedback must not be required to understand state.
- Keyboard entry must work.
- Focus management should follow visible field reveal.
- Mobile layout should keep prompt dock reachable.

## Implementation Phases

1. Extract current `/vision` into agent, prompt dock, and canvas renderer components.
2. Add a local renderer for backend canvas blocks while preserving current endpoint.
3. Switch to new `/vision/conversations/turns` contract.
4. Remove dashboard-card assumptions from the composable.
5. Move from dark experimental look to white blank-canvas design.
6. Add state transitions and progressive disclosure polish.

## Validation

- `npm run type-check`
- `npm run build`
- Visual smoke through local dev server once implementation begins.
- Optional Playwright screenshot checks once the canvas renderer stabilizes.
