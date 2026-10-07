## Modularity Rules

- Imports from external modules are allowed only from package `*.api.*`.
- Imports from external module from `*.infra.*` are strictly forbidden.
- No common logic between few modules without ADR and review can be implemented.
- Modules cannot have circular dependencies.

## CI Gate

- The Gradle task `checkInfraImports` should be implemented that checks if there are no imports from another module's `*.infra.*`.
- `checkInfraImports` is checked before test-stage on CI/CD.