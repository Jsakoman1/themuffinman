# Home dashboard child analysis

The Home API already exposes summary counts, but the frontend treats them as decorative metrics and adds a duplicate quick-action navigation layer. The implementation should add optional route targets to presentation data, render actionable metrics as links, and turn the remaining content into attention/recent-activity sections only when backend data exists. No frontend rule should infer whether a user may see or act on an item.

Risk: the current summary DTO may not contain enough item-level data for a useful attention feed. If so, keep the first slice count-based and record a backend read-model expansion instead of fabricating rows.
