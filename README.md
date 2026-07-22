# BankEngine — Concurrent Banking Simulation

> **Academic stage:** **4th year** systems · **Created:** January 2026  
> **Learning focus:** Java concurrency · `ReentrantLock` · deadlock prevention · race conditions · JUnit concurrent tests · throughput benchmarking

## About

In-memory banking engine with fair locks, ordered transfer locking, audit log, and tests.

## What I learned

| Topic | How this project taught it |
|-------|----------------------------|
| Concurrency | Threads, shared state, mutual exclusion |
| Deadlocks | Ordered lock acquisition on transfers |
| Testing races | Multi-thread deposit / transfer tests |
| Performance | Ops/sec micro-benchmark |
| Systems hygiene | Audit logging, graceful dump |

## Quickstart

```bash
mvn -q test
mvn -q exec:java -Dexec.mainClass=com.bank.Main
mvn -q exec:java -Dexec.mainClass=com.bank.Benchmark -Dexec.args="16 20000"
```

## Author

Sankalp Sahu
