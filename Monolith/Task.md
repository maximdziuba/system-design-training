# Workshop: "Design a Modular Monolith for Specified NFRs and Prove Its Manageability"

## Context

You are designing an "Orders" service for an e-commerce / online product.

**Conditions:**

- **Team:** 6 developers
- **Phase:** MVP → early growth (frequent requirement changes)
- **Integrations:** External Payment Provider (HTTP), Notification Gateway (email/SMS)
- **Goal:** Fast feature delivery without building a "spaghetti monolith"
- **Load:** Moderate (not hyperscale), but peaks are possible (sales/promotions)

## Requirements

### Non-Functional Requirements (NFRs)

1. **Define at least 8 measurable NFRs, strictly including:**
   - **Availability** (in "nines") + definition of what constitutes downtime
   - **Latency SLO** (p95 and p99) for a key endpoint (e.g., `POST /orders`)
   - **Reliability** (failure rate / target MTBF)
   - **Maintainability** (target MTTR + practices to reduce MTTR)
   - **Scalability** (how you will scale with load growth)

2. **For availability:** Specify the acceptable downtime per year/month (using the "nines" table) and explain why this specific value was chosen.

### Architecture First: C4 + Sequence (as Code)

Add to the repository (PlantUML):

1. **C4 Container**: UI → Modular Monolith App → Postgres (+ external providers)
2. **C4 Component**: Modules inside the monolith: `Orders / Payments / Users / Notifications`, strictly including:
   - Public facades `*.public` (or `*.api`)
   - Prohibition of internal `*.infra.*` imports between modules
3. **Sequence #1**: "Create Order" — Orders invokes Users / Payments / Notifications via facades
4. **Sequence #2**: "Modularity PR Gate" — CI runs linters + ArchUnit; code review requires justification for new dependencies and an ADR if necessary

### Modularity Rules

1. **Define 3–5 rules** (in the README or a separate document), for example:
   - Inter-module calls only via `..public..` / `..api..`
   - Prohibition of importing another module's `..infra..`
   - Prohibition of circular dependencies between modules
   - Prohibition of a "shared domain" without an ADR

2. **Add a CI gate** (description or actual configuration):
   - A step that fails the PR upon violation of the modularity rules (ArchUnit / linter)

### Architecture Decision Record (ADR)

Submit at least **1 ADR**:

- "Why a modular monolith instead of microservices at this stage"
- Alternatives considered (microservices, plain monolith without strict boundaries)
- Consequences (trade-offs), and what triggers splitting a module into an independent service

### Metrics and NFR Verifiability (Minimum)

Describe how you will verify the stated NFRs:

- **Latency**: p95 / p99 (where measured: edge + service)
- **Availability**: What constitutes "down" and how the metric is collected
- **MTTR**: Which engineering practices will reduce MTTR (runbooks, auto-rollback, alerts)

## Definition of Done

- 8+ measurable NFRs (Availability / Latency / Reliability / Maintainability / Scalability)
- C4 Container + C4 Component + 2 Sequence diagrams (PlantUML)
- Explicit modularity rules + CI gate (description / implementation)
- At least 1 ADR with alternatives and consequences
- Documented method for verifying NFRs (metrics / data sources / approach)
