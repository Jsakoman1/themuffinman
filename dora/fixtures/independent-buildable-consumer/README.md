# Independent buildable Dora consumer

This fixture is created entirely at test time from one reviewed local Dora source
with an immutable 40-character source reference. It bootstraps the
`spring-vue-buildable` starter, runs its declared setup, test, and build commands,
checks Dora health, generates CI, and executes a locally declared portable plugin.

It must not use MuffinMan paths, configuration, or domain code.
