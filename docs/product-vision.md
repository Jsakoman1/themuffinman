# Product Vision

This document captures the intended long-term product feel, interaction principles, and direction for TheMuffinMan.

It is not an implementation backlog. It is a stable product-direction reference for UX, interaction design, and future module decisions.

## North Star

TheMuffinMan should feel like a social useful network that adapts to the user instead of forcing the user to adapt to rigid form layouts.

The product should help people:

- offer and find useful side work
- discover services, things, rides, and nearby opportunities
- communicate naturally
- move through the product with minimal friction

## What The App Should Feel Like

The app should feel like one intelligent surface, not like a stack of separate windows.

Instead of making users hunt through menus, modal dialogs, and long forms, the product should reshape itself around the current task:

- if the user is exploring, the surface should be open, visual, and lightweight
- if the user is deciding, the surface should show comparisons, summaries, and trust cues
- if the user is acting, the surface should become focused and direct
- if the user is talking, the surface should react with visible voice feedback
- if the user is reviewing results, the surface should compress the outcome into a clear state

The goal is not novelty for its own sake. The goal is to make the app feel like it understands what kind of moment the user is in.

The default visual idea is an abstract white canvas with motion, strong contrast, and clear state changes. It should feel playful, inviting, and alive rather than like an Excel sheet, enterprise dashboard, or dense text-heavy admin tool.

## UX Principles By Screen

- Discovery screens should stay open, sparse, and visually calm.
- Search and browse screens should prioritize ranked results, trust signals, and quick comparisons.
- Creation screens should ask only for the minimum structure needed to complete the job.
- Voice-driven screens should always show listening, processing, and completion states.
- Voice interactions should run with parallel visual confirmation rather than audio alone.
- Relationship or negotiation screens should make the social context visible without overwhelming the task.
- Completion screens should compress the outcome into a clear next step instead of leaving the user at a dead end.
- High-volume result screens should summarize and filter aggressively by default, then expand only when the user asks for more detail.
- Review surfaces should compress the decision into a compact confirmation strip with inline field-revision chips instead of opening another window.
- Suggestion chips should react to the current slot and the last heard transcript so the user gets useful next-step transforms instead of generic command buttons.
- When a slot already has a value, the composer should surface that value inline and offer keep/replace choices instead of forcing the user to rediscover state.

The rule is that each screen should optimize for the user's current intent, not for a generic component library habit.

## Screen Archetypes

The product should reuse a small set of screen archetypes that can stretch across modules:

- blank canvas: no clutter, one clear prompt, one clear next action
- guided intake: structured input appears only where precision matters
- ranked results: cards, rows, or tiles that can be compared quickly
- comparison surface: table or split-panel view for choosing between options
- conversation surface: chat or voice with visible system feedback
- action review surface: compact confirmation plus follow-up options
- social context surface: relationship, trust, visibility, and consent signals when needed

These archetypes should feel like different shapes of the same product, not unrelated screens.

## Product Feel

- Calm instead of crowded.
- Adaptive instead of static.
- Context-aware instead of form-heavy.
- Human and useful instead of generic and transactional.
- Simple on the surface, capable underneath.
- Playful and inviting instead of cold and bureaucratic.
- High-contrast and visually clear instead of small-text dense.

## Interaction Principles

- The default surface should behave like a blank canvas until the task needs more structure.
- The UI should reveal only the controls, text, visuals, or actions that are relevant to the current intent.
- Voice, gesture, animation, text, tables, and images are presentation modes, not separate products.
- Visual feedback should always confirm that the system heard, understood, or is working on the user's request.
- When a task is simple, the interface should stay minimal.
- When a task is complex, the interface should expand into a richer guided surface.
- A screen should be able to hold many kinds of content, but never all at once by default.
- The same shell should be able to become a text view, a table, a command surface, a result card, or a narrative surface depending on context.
- The user should feel guided by the product, not trapped inside a fixed form metaphor.

## Adaptive Surface Model

The surface can shift between a few practical modes:

- minimal text mode for quick commands and confirmations
- guided form mode for structured data entry
- visual feedback mode for voice and system responses
- table mode for comparisons, lists, and summaries
- image or media mode for product discovery, identity, or context
- gesture-ready mode for future mobile or spatial interactions

The key rule is that the surface should match the user's current need rather than always exposing the same window, textbox, and button pattern.

### Blank Canvas Behavior

- The default state should be visually quiet, white, and spacious.
- The first prompt composer should live inside the blank canvas itself so the user can act immediately without opening a separate dock or modal.
- The prompt surface should autofocus when the task becomes active and should grow more visibly as content or structure accumulates.
- Actions should appear contextually, so the surface does not show voice or review controls before the task needs them.
- Voice input should echo the recognized transcript inline so the user can verify the last heard phrase inside the same surface.
- Lightweight suggestion chips should appear only when they help the current slot or blank state, not as a permanent command palette.
- Primary content should appear only when the user has an intent or when the system has something meaningful to show.
- Empty states should feel intentional, not broken.
- The canvas should be able to hold a single dominant task, then expand into supporting panels only when needed.
- The user should never have to fight the interface just to get to the relevant action.
- The system should use the full screen as a working surface instead of confining key content to a small central box.
- Content should appear and disappear dynamically as the state changes.

### Example Surface Transformations

- A side job search can start as a simple prompt, then expand into a curated list, then into a comparison table, then into a contact or booking action.
- A request to offer help can start as voice or plain text, then become structured fields only where precision matters.
- A social interaction can start as a lightweight intent, then reveal trust, relationship, and context signals only when useful.
- A completed action can collapse into a compact success state with next-step suggestions instead of leaving the user on a generic success page.
- A large result set such as `find job` should first show the most relevant subset with intuitive filters, and only reveal very long lists when the user explicitly wants them.

## Voice And Feedback

- Voice interaction should be accompanied by visible confirmation.
- When the system listens, the UI should show that state clearly.
- When the system is processing, the UI should show progress or transition feedback.
- When the system completes an action, the UI should show a compact result state, not only a silent backend success.
- Voice should feel like a first-class interaction mode, not a bolt-on microphone icon.
- The visual layer should make speech understandable even when the user is in a noisy or attention-split environment.
- Pure audio should not be the primary model because background noise, low concentration, and recognition ambiguity still make voice-only interaction too fragile.
- Parallel audio and visual feedback is the preferred mode because the user can hear the response, verify what was recognized, and understand complex structures that are easier to show than to narrate.
- The runtime contract should tell the client whether the current turn is better treated as focused, coordinating, reviewing, passive, or blocked, so the UI can stay calm instead of guessing the interaction density.
- The runtime contract should also expose device role, session anchor, action hints, and lightweight audio or haptic cues so mobile and voice clients can present the same turn at different densities without inventing local rules.
- Retry and replay on the same turn should stay idempotent from the user perspective, so a repeated submit or transport retry does not duplicate the task.
- The system should explicitly show what it heard from the user so recognition errors can be caught immediately.
- The system should also show which active slot the transcript is being mapped into, so the user can see the semantic target instead of only the raw text.
- The semantic mapping layer should expose one focus slot for the turn, so a spoken or typed utterance does not collapse into the wrong field by default.
- When the user is already inside a requested slot, the backend should keep that slot as the fallback semantic focus if the model does not choose one explicitly.
- Shared semantic mapping should cover prompt intake and review-edit follow-ups, so the same focus rule applies across the whole conversation turn.
- The backend should expose which slots were actually applied on the current turn, so the UI can confirm real state changes instead of inferring them.
- Review confirmations should be present but visually restrained, so the summary reads as a decision surface rather than a modal approval prompt.
- Review surfaces should show the slots applied in the current turn as a compact inline strip, so users can verify the immediate change before confirming the larger summary.
- Recent conversation entries should surface the latest applied slot badges inline, so resume feels like re-entering a live surface instead of a dead history list.
- Complex information should be drawn, listed, summarized, or highlighted visually instead of being explained only through speech.
- While the speech runtime may start browser-native, the product should still prefer backend-governed capability and default-setting contracts instead of letting each client invent its own voice behavior.

## Core Product Ideas

- Social utility should be the main product filter.
- Features should help people do something valuable with other people, not just browse content.
- Reusable interaction patterns matter more than one-off visual novelty.
- The design should support a future where the product can feel more like an assistant or adaptive environment than a classic form app.
- The product should help people discover, request, offer, compare, and coordinate useful things around them.
- The network should feel socially useful, not just socially present.
- The UI should make the useful thing obvious before making the mechanism obvious.
- Everything shown on screen should be useful in that exact moment.
- Information density should expand and contract based on user intent, not based on a fixed page template.
- The system should learn durable user preferences and correction patterns over time, but only through explicit backend memory and feedback capture rather than silent policy drift.
- Durable preferences should also carry confidence and decay semantics, so repeated behavior strengthens the signal while stale habits naturally cool down.

## Frontend Epics

The vision can be translated into implementation batches that are easier to deliver and reason about:

1. Adaptive shell and blank-canvas framework
   - one shared page shell
   - task-aware surface switching
   - consistent loading, empty, and completion states

2. Voice and visual feedback layer
   - visible listening state
   - visible speaking state
   - visible processing state
   - visible confirmation state
   - recognition transcript or confirmation surface
   - speech-to-result transitions that remain understandable without sound

3. Adaptive task surfaces
   - text-first quick entry
   - table or comparison views for choosing between options
   - cards and summaries for browsing
   - guided panels for structured workflows
   - dynamic filtering and summarization for large result sets

4. Social utility flow surfaces
   - work marketplace
   - business appointment and profile surfaces
   - thing sharing and borrowing surfaces
   - ride sharing and circle-aware surfaces
   - shared chat surfaces across modules

5. Motion and polish layer
   - subtle transitions
   - state-change feedback
   - expressive but restrained animation
   - optional future gesture support

These epics are implementation buckets, not separate product visions.

## How I Would Frame The Product

Think of TheMuffinMan as an adaptive social utility layer:

- a blank canvas that becomes a work marketplace when you need work
- a service surface when you need a business profile or appointment
- a sharing surface when you need to lend or borrow
- a movement surface when you need a ride
- a conversation surface when human negotiation is the right next step

The interesting part is not that these are separate modules. The interesting part is that the user should feel one continuous system that can take on different shapes.

That is the Her-like direction: not a folder of features, but a responsive environment.

It should also evolve in the softer Her sense: the system should remember, learn, and become more personally useful to the user over time without losing clarity or control.

## What Should Stay Core

- one adaptive shell
- one clear visual language
- one strong voice-feedback model
- one social utility network identity
- one set of reusable interaction patterns that scale across modules

## What Should Stay Experimental

- highly expressive motion systems
- gesture-heavy interactions
- spatial or ambient interfaces
- advanced voice-first shortcuts
- animated canvases that replace clarity

## What Belongs In This Vision

- interaction tone
- presentation modes
- UX principles
- product feel
- future-facing experience direction
- guidance for making the product feel modern and intuitive

## What Does Not Belong In This Vision

- open implementation tasks
- code-level details
- temporary design experiments
- operational backlog items
- backend invariants

## Relationship To Other Docs

- `docs/business-logic.md` defines what the product does in user-facing terms.
- `docs/domain-technical.md` defines the technical source of truth.
- `docs/product-memory.md` records durable lessons learned from implementation and audits.
- `docs/implementation-backlog.md` tracks open product work.

## Stable Direction

The long-term goal is a product that feels modern, useful, and adaptive without becoming chaotic or gimmicky.

If a future design choice makes the product more expressive but less clear, clarity wins.

The app should always feel like it is adapting around a real task, not like the user is filling out a generic software form.

Future product-direction sessions should start from `docs/product-memory.md` for durable lessons and `docs/product-vision.md` for long-term direction, then expand outward into the living business and technical docs only when the task needs implementation detail.

Future `/vision` implementation sessions should also read `docs/vision-architecture-patterns.md` before making backend orchestration, API, frontend canvas, prompt-handling, or executor decisions.
