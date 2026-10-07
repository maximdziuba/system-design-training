# ADR-001: Monolith Architecture for the Application

- **Status:** Accepted
- **Date:** 06.10.2026
- **Owners:** Backend Team

## Context

The architecture of the application for getting orders should be decided. The application is currently on MVP stage, developed by team of 6 developers. The application integrates with Payment Provider via HTTP and has Notification gateway. The application has normal load, with higher loads on selected periods (e.g. sales). The target on MVP stage is fast release of new features.

## Decision

Using the modular-monolith architecture:
- 4 Modules: Orders, Payments, Users, Notifications
- Each module has `*.api` (public API, accessible for another modules) and `*.infra.*` (internal business logic, access from another modules forbidden)
- Automatic check of intra-modular dependencies (no infra dependencies, no circular dependencies)

## Alternatives Considered

1. **Monolith app** - easier to implement, no time for setting up CI-checks, smaller amount of code because of direct imports of needed logic, harder to maintain on later stages, a lot harder to split to different micro-services later.
2. **Microservices** - overkill for current state, too much work for setting up communication between services, harder maintenance for 6 people, but easier scaling on later stages.
