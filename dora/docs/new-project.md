# Create a new Dora project

This guide documents Dora's reviewed local-source bootstrap path for Dora development,
testing, and pre-release work. It starts from an empty working directory and does not
require MuffinMan, a global Ruby installation path, or a network download during
bootstrap.

## Stable consumer runtime policy

A stable consumer project uses its own reproducibly vendored Dora package pinned to an
immutable official release/tag and commit. Its launcher executes that local package and
never resolves a developer workstation path. This guide's local `bootstrap-source.yaml`
format is deliberately not the normal stable-consumer runtime model: the current
bootstrap command accepts reviewed local sources only and does not fetch a release URL
or mutable tag. Do not represent a local bootstrap record as a stable consumer pin.

## 1. Choose a reviewed local Dora development/pre-release source

Obtain Dora locally through your team's approved process and record its reviewed
forty-character commit id. Create `bootstrap-source.yaml` next to the destination:

```yaml
kind: dora_bootstrap_source
version: 1
source:
  path: /absolute/path/to/reviewed/dora
  ref: 0123456789abcdef0123456789abcdef01234567
```

`path` must be a local directory containing Dora's `bin/dora` and `lib/dora`.
URLs, branch names, and mutable tags are rejected.

## 2. Bootstrap a project-local Dora command

From the empty parent directory, choose a technical starter:

```text
/absolute/path/to/reviewed/dora/bin/dora bootstrap my-app --project my-app --source bootstrap-source.yaml --starter blank
```

Use `--starter spring-vue` when you want empty `backend/` and `frontend/` roots.
Neither starter creates a business domain, authentication, database schema, or
user-facing feature.

## 3. Confirm the local development/pre-release project is healthy

The bootstrap copies the reviewed Dora package into `my-app/dora`, records the
source ref in `.dora/bootstrap-source.yaml`, and creates `my-app/bin/dora`. Before
normal stable-consumer use, apply the approved vendored release/tag pin process and
remove or replace the development bootstrap record rather than treating it as runtime
authority.

```text
cd my-app
./bin/dora doctor .dora/project.yaml
./bin/dora help
```

Project commands are declared in `.dora/project-commands.yaml`. Replace the starter
placeholders with your project's real setup, test, and build commands before using
them in CI. Dora controls and plugin reports help guide delivery work; they do not
prove product completion, runtime acceptance, or a release.
