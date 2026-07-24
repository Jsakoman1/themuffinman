# Vision prompt problem analysis — 2026-07-23

## Evidence boundary

This analysis combines the Vision source tree, semantic audit tests, local fallback tests, the existing UI-linking plan, and the fresh Chromium trace in `docs/runtime-evidence/ui-linking-vision-inline-runtime.json`. Existing verified implementation plans are treated as baseline evidence, not as proof that the exact natural-language prompts work in a real browser.

## Additional prompt findings

1. `send message to <person>` is still represented as `OPEN_CHAT` in `VisionSemanticAuditMatrixTest`, while the product now has a `SEND_MESSAGE` intent and route. This is a contract drift that can silently reopen the old chat-only behavior.
2. `SEND_MESSAGE` is not included in the explicit semantic-intent allowlist in `VisionIntentRouter.routeSemanticIntent`, and it has no deterministic signal helper in `VisionIntentSignalSupport`. Runtime routing therefore depends on the semantic provider/catalog path.
3. `create new work` has no equivalent deterministic fallback alias. The fallback recognizes quest-oriented phrases such as `create quest`, `post a quest`, or `need someone`, but not the product-facing “new work” wording.
4. `go to circles` has no explicit navigation alias. `my circles`, `show circles`, and `open circles` are covered, but navigation-only wording is not separated from viewer-scoped content loading.
5. `open my circles` is semantically close to the existing circles read aliases, but its runtime result is not visible. The route, intent, content projection, and renderer need a single acceptance contract.
6. `open quest Suitcases` depends on provider extraction or entity resolution and has no runtime fixture proving title lookup, no-match messaging, ambiguous-match clarification, and detail handoff as separate outcomes.
7. The exact prompt tests inject semantic plans directly through test helpers. They do not prove the production OpenAI payload, provider response, contract sanitizer, router, conversation handler, renderer, and browser readback as one chain.
8. Local emergency fallback intentionally fail-closes many valid mutation prompts (`create/update/delete circle`, circle requests, application updates, application approval/decline, profile location). That is safe, but the user-facing contract does not consistently explain “provider unavailable” versus “unsupported capability”.
9. Read-only fallback coverage is uneven across the route catalog. Some read prompts have deterministic aliases while detail, business, things, borrow, rides, activity, and chat variants rely on broader heuristics or provider output.
10. Chat wording is overloaded: `message`, `send a message`, `direct message`, and `chat with` currently overlap. Without an explicit precedence table, a send request can degrade into opening a chat, while an open-chat request can be interpreted as a mutation.
11. Target extraction and message-body extraction are not tested as independent prompt phases for natural variants such as “tell Nikolina…”, “write to Nikolina…”, “send her…”, or a second-turn body after the recipient is resolved.
12. Follow-up slot answers are not covered for all Vision families. A short answer such as a name, title, price, location, or “yes” can inherit the wrong active slot when the semantic provider returns no focus slot.
13. Multi-signal precedence is only tested for a small number of cases. Prompts combining work, circles, chat, business, or notification words can select a broad family before the explicit action is recognized.
14. Unsupported responses can render as generic placeholder text (`Hello, test.`) instead of a typed unsupported, no-match, clarification, provider-failure, or recovery state.
15. Provider failure and local fallback status are persisted, but the browser contract does not yet prove that the correct recovery action and retry preserve the conversation without duplicate work.
16. Semantic route catalog examples, fallback aliases, audit-matrix cases, and browser scenarios are maintained in separate places without a generated coverage check that every critical prompt family has all four.
17. Natural-language localization is incomplete: the emergency fallback is English-only and fail-closes non-English prompts, while the product and user context can be multilingual.
18. Entity aliases are not uniformly normalized for punctuation, possessives, diacritics, casing, and conversational references (“my circles”, “the Suitcases quest”, “Nikolina’s chat”).
19. Route-only prompts and content-opening prompts can share the same final Web destination but require different backend semantics; that distinction is not uniformly represented in the response/readback contract.
20. Runtime auth/seed setup exposed a duplicate `vision_user_preference` unique-key failure and a 401 during an earlier disposable-account run. The seeded account run reached the shell, but this separate setup path remains an unresolved Vision runtime reliability finding.

## Priority interpretation

- P0: exact requested prompts, semantic contract drift, generic/unsupported runtime outcomes, direct-message safety, and provider/fallback parity.
- P1: complete prompt-family coverage, follow-up continuity, entity normalization, typed recovery states, and runtime failure/retry evidence.
- P2: multilingual fallback, broader aliases, and long-term model-quality tuning.
