# Dora v1.4 goal readiness

Status: prepared for atomic hardening, not yet implementation-active.

The master is based on MuffinMan commit `d52ab8dc077c78790488803cc0e18bcdf5726337`,
which pins the published Dora v1.3.0 release. It has six child plans, fifteen globally ordered implementation items, direct
predecessor dependencies, exact required paths, leaf validation commands, and evidence
boundaries. It preserves v1.3 as baseline evidence and adds no release authority.

Open implementation decisions retained for the first atomic task:

- Guided-session writes must reject non-empty destinations and duplicate answer IDs.
- Proposal commands must return data only and never apply paths or code.
- Decision records must append only with citations and reject duplicate IDs.
- The Playwright profile must require explicit consumer installation and must not claim
  a browser run from static or consumer declaration proof.
- App readiness must distinguish declared requirements from observed local tools and
  must never change the host to repair a gap.
- Authoring trace must preserve citations and omissions without turning a proposal or
  decision into a product fact.
- The actual Playwright execution task is the only v1.4 item requiring browser binary
  installation or a temporary local server, and it requires explicit approval.
