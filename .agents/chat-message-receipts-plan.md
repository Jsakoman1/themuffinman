# Chat Message Receipts Plan

Status: complete

## Scope

- replace read-only receipt handling with delivered and seen lifecycle fields
- keep read action semantics aligned with the richer receipt model
- extend socket payloads and read DTOs

## Tasks

- [x] add schema for delivered and seen timestamps
- [x] update repository queries and unread calculations
- [x] advance delivery state from safe backend touchpoints
- [x] advance seen state from explicit read actions
- [x] add regression tests for receipt transitions
