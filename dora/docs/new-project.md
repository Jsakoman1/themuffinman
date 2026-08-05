# Create a new Dora project

This guide starts from an empty working directory. It does not require MuffinMan,
a global Ruby installation path, or a network download during bootstrap.

## 1. Choose a reviewed local Dora source

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

## 3. Confirm the local project is healthy

The bootstrap copies the reviewed Dora package into `my-app/dora`, records the
source ref in `.dora/bootstrap-source.yaml`, and creates `my-app/bin/dora`.

```text
cd my-app
./bin/dora doctor .dora/project.yaml
./bin/dora help
```

Project commands are declared in `.dora/project-commands.yaml`. Replace the starter
placeholders with your project's real setup, test, and build commands before using
them in CI. Dora controls and plugin reports help guide delivery work; they do not
prove product completion, runtime acceptance, or a release.
