package com.bank;

import com.bank.service.BankEngine;
import com.bank.util.AuditLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Lightweight throughput benchmark (ops/sec) for concurrent transfers.
 */
public class Benchmark {
    public static void main(String[] args) throws Exception {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 16;
        int ops = args.length > 1 ? Integer.parseInt(args[1]) : 20_000;

        Path tmp = Files.createTempDirectory("bank-bench");
        try (AuditLogger audit = new AuditLogger(tmp.resolve("audit.log"))) {
            BankEngine engine = new BankEngine(audit);
            for (int i = 0; i < 10; i++) {
                engine.openAccount("A" + i, 10_000.0);
            }

            ExecutorService pool = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();
            long t0 = System.nanoTime();
            for (int t = 0; t < threads; t++) {
                futures.add(pool.submit(() -> {
                    ThreadLocalRandom rnd = ThreadLocalRandom.current();
                    int per = ops / threads;
                    for (int i = 0; i < per; i++) {
                        String a = "A" + rnd.nextInt(10);
                        String b = "A" + rnd.nextInt(10);
                        engine.transfer(a, b, 1.0);
                    }
                }));
            }
            for (Future<?> f : futures) {
                f.get();
            }
            pool.shutdown();
            pool.awaitTermination(60, TimeUnit.SECONDS);
            long nanos = System.nanoTime() - t0;
            double sec = nanos / 1_000_000_000.0;
            double opsPerSec = ops / sec;
            System.out.printf(
                    "threads=%d ops=%d seconds=%.3f throughput=%.1f ops/sec%n",
                    threads, ops, sec, opsPerSec
            );
        }
    }
}
