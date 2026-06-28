## Goal

Fix the quest application submit/detail reload failure caused by lazy proxy access while building the quest detail response.

## Steps

1. Confirm the failing service path and the detached entity access point from the stack trace.
2. Keep quest detail DTO assembly inside a read transaction so fetched associations remain available during mapping.
3. Add a regression test that exercises the quest detail path with a current-user application.
4. Run targeted backend tests for the affected service.
