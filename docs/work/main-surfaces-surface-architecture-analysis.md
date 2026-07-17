# Surface architecture child analysis

`SurfaceContentView` currently combines dashboard, list, business operations, Chat inbox, and Calendar rendering in one component. The safe extraction boundary is presentation-only: a shared header, metric grid, section, and row/card primitives receive backend-prepared models, while Chat and Calendar keep specialized stateful renderers. Extraction must preserve route metadata, deep links, loading/error/empty states, and existing API ownership.
