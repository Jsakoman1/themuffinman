# VisionForWeb remaining modules: current-state analysis

Baseline: `479332c`

## Executive finding

The Web application already has canonical routes and substantial backend domain behavior for the remaining modules. The missing work is the integration layer: backend-published semantic route descriptors, authorized target resolution, typed Vision actions, and browser runtime evidence from natural-language input to the final URL. The current implementation has contract-level mappings for several list surfaces, but only My Work has been proven end to end.

Production natural-language understanding remains OpenAI-owned. A local parser is allowed only for development and test fixtures; it must not become a production translator or fallback classifier.

## Module-by-module gap analysis

| Module family | Existing surface | Current integration state | Meaningful remaining work |
| --- | --- | --- | --- |
| Work | `/work`, `/work/quests`, `/work/applications`, quest and application details | My Work navigation has runtime evidence; other Work actions are incomplete or only contract-level | Add application/detail descriptors, authorized target resolution, and runtime proof for list, detail, and return-context flows |
| Circles, People, Profile | `/circles`, `/people`, `/people/:userId`, `/profile` | Domain pages exist; generic Vision navigation does not yet cover the full identity surface | Add route catalog entries, person/circle disambiguation, viewer-safe labels, authorization behavior, and runtime proof |
| Things and Rides | `/things`, `/things/mine`, `/things/:listingId`, `/rides`, `/rides/mine`, `/rides/:rideId` | Pages/routes exist, but Vision coverage and target resolution are not complete | Connect listing, borrow, thing-detail, ride-list, and ride-detail actions without frontend entity matching |
| Business | `/business`, `/business/find`, public business pages, bookings, availability, calendar | Business booking navigation exists in the current action set; discovery/profile/booking/calendar coverage is incomplete | Add public/private scope-aware descriptors, business/booking targets, and runtime proof for owner and customer contexts |
| Chat | `/chat`, `/chat/:conversationId` | Workspace action exists; conversation target navigation is not proven | Add authorized conversation resolution, `OPEN_CONVERSATION`, ambiguity handling, and runtime proof |
| Notifications and Activity | `/notifications`, `/activity` | Typed entry-point mappings exist; no complete browser runtime matrix | Verify navigation, recovery behavior, and contextual return path from every authenticated surface |
| Cross-module context | Personal context, people, entities, time, conversation | Resolution remains distributed and module-specific | Create one backend target-resolution contract reused by all modules and preserve portable action semantics for future iPhone/Watch clients |

## Required evidence standard

Each module family is complete only when a real browser scenario records the user input, OpenAI semantic result, backend action contract, resolved authorized target, final URL, and any clarification/recovery behavior. Unit tests and route catalog assertions are necessary but are not sufficient for module closeout.

## Plan boundary

This analysis does not propose rebuilding existing module domain features or native clients. It scopes the missing VisionForWeb integration and its runtime proof; the accompanying master and child YAML plans are the execution source of truth.
