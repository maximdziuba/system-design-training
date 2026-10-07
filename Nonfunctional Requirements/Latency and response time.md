# Latency and Response Time

## Latency

Time between "begin of operation" and "result is ready". Almost always has to be specified:

- Network latency (RTT, TLS handshake)
- Queueing latency (how long waited in queue / threadpool / DB)
- Service latency / processing time (processing time inside of service: code, DB, cache, external requests)
- E2E latency (latency from user perspective)

## Response Time

Time between the sent request and getting the response.

## Processing Time vs. Waiting Time

- **Service time** - real work inside of service (code, DB, cache, external API call)
- **Waiting / queueing time** - waiting for resources:
  - Waiting for new thread
  - Waiting for connecting to new thread
  - Waiting for locking / semaphore
  - Waiting the queue in DB / message broker

> **Often waiting time is the problem under high load.**

## Percentiles

- **p50 (50%)** - usually
- **p95** - how bad is regularly
- **p99** - often some problem with queueing

---

Higher latency means more requests that are waiting at the same time, higher load on resources, and higher possibility of queues and cascade errors.  
Higher latency $\rightarrow$ higher infra costs.