# Availability and Reliability

**Availability** - percent of time when service can get and process requests.

$$
\text{Availability} = \frac{\text{whole time} - \text{downtime}}{\text{whole time}} = \frac{MTBF}{MTBF + MTTR}
$$

- **MTBF** - mean time between failures
- **MTTR** - mean time to repair

$$
MTBF = \frac{\text{whole time} - \text{whole downtime}}{\text{amount of failures}}
$$

$$
MTTR = \frac{\text{whole repair time}}{\text{amount of repairs}}
$$

- High reliability + long MTBF = low availability
- Low reliability + almost no MTBF (instant fix) = high availability

## Reliability

How rare the system has failures.

Measured through:
- MTBF / MTTF
- Failure rate
- Amount of incidents in period

## Availability

Percent of time when service can get and process requests.

Measured through:
- $\text{Availability} = \frac{\text{whole time} - \text{downtime}}{\text{whole time}} = \frac{MTBF}{MTBF + MTTR}$
- "Nines" (99.9%, 99.99%)

## Maintainability

How fast the system can be recovered.

Measured through:
- MTTR
