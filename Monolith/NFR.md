# Non-Functional Requirements (NFR)

- **Availability**: 99.9% (8.76 hours of downtime in a year). Reasonable target for MVP. Allows not to spend too much time on expensive infra on MVP stage. Downtime - whole time when the endpoint is not reachable or fails because of external dependency (e.g. Payment provider).
- **Latency SLO**: p95 latency - 0.8s, p99 latency - 1.3s (including the roundtrip to payment provider, processing of order and returning response back to client).
- **Reliability**: Because of monolith design the application can not fail too often. The target MTBF = 3-4 months (2190-2920 hours).
- **Maintainability**: Expected MTTR = 1-2 hours, which allows to fit inside of expected downtime in a year. For better MTTR following must be done:
  - Observability must be setup and collect all logs about state of the application in one place (Loki+Grafana). Logs should be saved in DB with full text search (e.g. Clickhouse) for fast querying of logs.
  - Notifications on responsible it-admins by the load 20% bigger than expected, unexpected errors.
  - On-calls for developers for fast fixing of unexpected errors.
- **Scalability**: Because expected the application not to be real high load, it will be scaled vertically with buying more powerful infra on expected peaks (sales). On peaks recommended approach also would be to deploy 2 instances of the app to ensure that even if it fails because of overload, the users can continue accessing the second instance.

### Verification & Monitoring

- Latency must be measured on `OrderController` before the return-statement.
- Health-check service (e.g. Cloudflare) can be added for tracking the health and downtime of the service.
