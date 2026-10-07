# Non-Functional Requirements Task

## NFRs

- **Availability:** 99.9%, possible downtime 1m 26s a day $\rightarrow$ 10m 2s a week, 8 hours 45 minutes 36 seconds a year.
- **Latency:** Order must feel fast, so p95 = 0.5s, p99 = 0.8s processing time of order, until the response.
- **Reliability:** Because the user likely will not continue the order if it is not processed from the first try, the system needs to have a bigger MTBF, probably 2–3 months (2190 hours).
- **Maintainability:** Because of yearly downtime, the target MTTR is 2.2 hours.
- **Maintainability Practices:** For decreasing MTTR, observability should be introduced, where errors can be localized through logs/metrics of the service.
- **Throughput / Scalability:** By 2,000,000 DAU and 80% of actions in 20% of time, we have 320,000 operations in 4.8 hours $\rightarrow$ 66,666 operations per hour $\rightarrow$ 18.5 operations per second with 37 KB output every second.
- **Scaling Strategy:** The system currently can be scaled vertically by adding extra hardware on peak hours, because 18.5 RPS and 37 KB/s are possible to handle with one instance. Also, the extra complexity of the system is avoided (with horizontal scaling, race conditions, DB integrity, and sync become critical).
- **Downtime Definition:** Downtime is the state of the system when requests cannot be processed or are processed in an incorrect way.

## BOTEC (Back-of-the-Envelope Calculation)

- $2,000,000 \times 0.2 = 400,000$ orders in 24h
- $4.62$ RPS on average
- Because of 80/20 rule: $18.5$ RPS on peak
- Concurrency = $18.5 \times 0.2 = 3.7$ requests
- Ingress: cannot specify because no request size is given
- Egress: 37 KB/s