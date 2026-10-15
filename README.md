# Inventory reservation lab (Java)

Small assistant-assisted learning project using fictional stock and orders. It is not a production service, professional work experience, or affiliated with any retailer. Intended for reading, running and explaining before interviews.

## What it demonstrates

- Java 11 classes, maps, exceptions and input validation.
- Atomic stock reservations using a synchronized critical section, preventing overselling inside one JVM.
- Idempotent successful-request retries; payload mismatches are rejected.
- Concurrent request simulation and text counters for attempts, conflicts, reservations and retries.
- Dependency-free regression checks, including 200 competing requests and 100 duplicate retries.

## Run

Install a JDK 11 or newer (not just a JRE), then:

```sh
sh run-tests.sh
```

The test program prints its check count. The simulation starts with 25 units and 100 requests: exactly 25 succeed, 75 conflict, and stock ends at zero. Output is synthetic, not a real retailer benchmark.

## Design and limitations

The same monitor protects the availability check, decrement and reservation record. Separate locks or a check outside the lock would permit overselling. A successful retry returns the original reservation and does not deduct stock twice. Failed requests are not saved; clients may retry them after conditions change.

Everything is in memory: a restart loses stock changes, counters and idempotency records. The single lock does not coordinate multiple servers and is not a claim of scalable infrastructure. There is no database, REST server, authentication, expiration, cancellation, deployment, Kotlin or AWS. A production design would require durable transactions and a unique idempotency key, bounded retention and business-specific recovery rules.

## Explain it yourself

See [STUDY_GUIDE.md](STUDY_GUIDE.md) for a short walkthrough and exercises. An optional GitHub Actions example is in `docs/ci-example.yml`. It is a reference example, not an enabled workflow. Tests were run locally.
