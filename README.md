# TheMuffinMan

TheMuffinMan is a multi-module application workspace.

Planned modules:
- `apps/sidequest` - local work marketplace
- `apps/business-hub` - custom mini websites, business profiles, calendars, and appointment booking
- `apps/thing-sharing` - lending and sharing physical items
- `apps/car-sharing` - voluntary route-based ride sharing for selected circles
- `services/chat` - shared chat domain and service intended for cross-module use

## Current State

Today this repository contains the first migrated module:
- `apps/sidequest`

`SideQuest` remains a Spring Boot + Vue application inside the monorepo and can continue to be developed independently while the broader TheMuffinMan platform takes shape.

## Repository Structure

```text
themuffinman/
  apps/
    sidequest/
    business-hub/
    thing-sharing/
    car-sharing/
  services/
    chat/
  docs/
```

## IntelliJ Setup

1. Open the `themuffinman` folder in IntelliJ IDEA.
2. Import `apps/sidequest/pom.xml` as a Maven project.
3. Treat `apps/sidequest/frontend` as the Vue frontend workspace.
4. Add future modules incrementally instead of merging everything into one backend too early.

## Development Direction

- Keep core product logic in the backend.
- Keep frontend code thin and UI-focused.
- Design shared concepts carefully across modules: users, circles, scheduling, chat, visibility, and consent flows.
- Prefer incremental module extraction over large rewrites.

## SideQuest Module

See `apps/sidequest/README.md` for SideQuest-specific setup and commands.
