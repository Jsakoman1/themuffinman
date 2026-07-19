# Interaction and action-system child analysis

The shared `AppActionMenu` primitive remains in use. The former unused `AppCard` primitive was removed during pre-shell cleanup. The main interaction contract remains data-driven: metrics and rows must expose an explicit destination when actionable, while nested controls must stop parent activation and retain keyboard semantics.
