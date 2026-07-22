# BankEngine — Concurrent Banking Simulation (Java)

Interview-grade **concurrency / systems** project to balance your AI portfolio.

```text
ConcurrentHashMap accounts
  + fair ReentrantLock per account
  + ordered locking for deadlock-free transfers
  + async audit log
  + JUnit race tests
  + throughput benchmark
```

## Why top candidates ship this too

AI roles still need engineers who understand:

- shared-memory races  
- deadlock prevention  
- testable concurrency  

## Quickstart

```bash
# requires JDK 17+ and Maven
mvn -q test
mvn -q exec:java -Dexec.mainClass=com.bank.Main
mvn -q exec:java -Dexec.mainClass=com.bank.Benchmark -Dexec.args="16 20000"
```

## Tests

| Test | Proves |
|------|--------|
| `depositIsRaceFreeUnderContention` | 100×10 deposits → exact balance |
| `transferPreservesTotalBalance` | Concurrent A↔B keeps Σ balances |

## Design

| Problem | Solution |
|---------|----------|
| Lost updates | Per-account mutex |
| Deadlock on transfer | Lock lower account id first |
| Registry concurrency | `ConcurrentHashMap` |
| Audit I/O | Single-thread executor |

## Layout

```text
src/main/java/com/bank/
  Main.java
  Benchmark.java
  model/BankAccount.java
  service/BankEngine.java
  util/AuditLogger.java
src/test/java/.../BankEngineTest.java
```

## Author

Sankalp Sahu
