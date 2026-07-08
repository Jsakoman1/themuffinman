# Chat Conversation State Plan

Status: complete

## Scope

- introduce per-user conversation state persistence
- add mute and archive operations
- shape workspace and conversation summary DTOs with member-state fields

## Tasks

- [x] add schema for conversation member state
- [x] wire entity, repository, and service helpers
- [x] expose mute/archive API endpoints
- [x] filter or order workspace results with archive state rules
- [x] add backend tests
