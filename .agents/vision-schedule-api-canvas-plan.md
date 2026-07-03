---
machine_kind: plan
machine_status: complete
machine_title: Vision Schedule API Canvas Plan
---

# Vision Schedule API Canvas Plan

## Status

Complete.

## Result

Updated the `/vision` canvas contract and frontend rendering so partial fixed schedules are represented honestly:

- field requests now ask for `scheduled_date` and `scheduled_time` separately
- slot summaries and schedule review text now show partial schedule progress
- the prompt composer remains conversational instead of forcing a browser datetime picker

## Validation

- covered by frontend type-check/build and backend conversation tests
