# BankEngine — High-Concurrency Banking Simulation

Thread-safe **in-memory banking engine** in Java demonstrating:

- Fair `ReentrantLock` per account  
- **Deadlock-free transfers** via ordered lock acquisition  
- Concurrent deposits / withdrawals / transfers  
- Async audit logging  
- CSV inventory dump on shutdown  
- JUnit tests for race freedom & balance conservation  

## Why this repo

Systems/concurrency depth for interviews — pairs well with Applied AI projects (shows you can write real concurrent code, not only notebooks).

## Layout

```text
src/main/java/com/bank/
  Main.java                 # concurrent simulation
  model/BankAccount.java
  service/BankEngine.java
  util/AuditLogger.java
src/test/java/com/bank/service/BankEngineTest.java
pom.xml
```

## Requirements

- JDK 17+  
- Maven 3.8+  

## Run simulation

```bash
mvn -q clean compile exec:java
```

Outputs:

- `system_audit.log` — per-operation audit lines  
- `inventory.csv` — final balances  

## Tests

```bash
mvn -q test
```

Key tests:

- **Race-free deposits** — 100 threads × 10 deposits of $1 → balance $1000  
- **Transfer conservation** — concurrent A↔B transfers keep total balance constant  

## Design notes

| Concern | Approach |
|---------|----------|
| Mutual exclusion | Per-account fair `ReentrantLock` |
| Deadlocks on transfer | Always lock lower account id first |
| Shared map | `ConcurrentHashMap` for account registry |
| Audit I/O | Single-thread executor append log |

## Limitations

- In-memory only (no durable DB transactions)  
- Not a real bank / no ACID disk durability  
- Demo-scale; not a production payments core  

## License

MIT / educational use.
