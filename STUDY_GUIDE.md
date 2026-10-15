# Study guide

1. Follow `reserve`: validate inputs, look up a previous success, check stock, change stock, record the result.
2. Explain why two threads cannot both reserve the last unit. Point to `synchronized` and describe what breaks if the check and decrement are separated.
3. Explain why an order retry must not reduce inventory twice. Why reject the same key with a different SKU or quantity?
4. Read the concurrency tests: 200 requests compete for 40 units. Why is request order nondeterministic but the final count deterministic?
5. Distinguish a counter (cumulative attempts) from a gauge (current available stock).
6. Try adding cancellation with tests. Decide whether cancellation itself needs an idempotency key.
7. Sketch a database version: one transaction, conditional stock update, unique request key. What would happen on a crash?

Be clear in applications: this is a new assistant-assisted learning lab, not past employment or production operations. Run it and modify a test before discussing it as hands-on practice.
