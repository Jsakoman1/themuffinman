# Dora

Dora is a portable delivery-control package for implementation planning,
verification, documentation contracts, and controlled project extensions.

It owns reusable protocols and command mechanics. A consuming project owns its
domain behavior, source code, tests, runtime evidence, documentation, and
extension commands.

## Project adapter

A project declares its paths, control commands, retained evidence locations,
and local extensions in a `dora_project_adapter` document. Validate one with:

```text
dora/bin/dora validate-adapter path/to/project-adapter.yaml
```

The adapter schema is `dora/project-adapter.schema.yaml`. Dora does not infer
product paths or completion status from an adapter.

## Package boundary

`bin/`, `lib/`, and `templates/` contain portable Dora behavior. `fixtures/`
contains consumer examples and is deliberately outside the portable package
boundary audit.

