---
machine_kind: plan
machine_status: complete
machine_title: Chat Telemetry Audit Plan
---

# Chat Telemetry Audit Plan

Status: complete

## Scope

- persist websocket session and abuse-relevant audit events
- capture socket lifecycle, parse failures, auth failures when possible, and rate-limit violations
- keep telemetry append-only and operational

## Tasks

- [x] add durable chat audit event storage
- [x] record websocket connect, disconnect, ping, and invalid payload events
- [x] record chat rate-limit violations and handshake failures
- [x] add focused tests where practical
