package com.bank.service;

import com.bank.util.AuditLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankEngineTest {

    @TempDir
    Path tempDir;

    @Test
    void depositIsRaceFreeUnderContention() throws Exception {
        Path log = tempDir.resolve("audit.log");
        try (AuditLogger audit = new AuditLogger(log)) {
            BankEngine engine = new BankEngine(audit);
            engine.openAccount("X", 0.0);

            int threads = 100;
            int depositsEach = 10;
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    for (int j = 0; j < depositsEach; j++) {
                        engine.deposit("X", 1.0);
                    }
                }));
            }
            for (Future<?> f : futures) {
                f.get(30, TimeUnit.SECONDS);
            }
            pool.shutdown();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
            assertEquals(threads * depositsEach, engine.getAccount("X").getBalance(), 1e-9);
        }
    }

    @Test
    void transferPreservesTotalBalance() throws Exception {
        Path log = tempDir.resolve("audit2.log");
        try (AuditLogger audit = new AuditLogger(log)) {
            BankEngine engine = new BankEngine(audit);
            engine.openAccount("A", 500.0);
            engine.openAccount("B", 500.0);

            int threads = 50;
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                final int idx = i;
                futures.add(pool.submit(() -> {
                    if (idx % 2 == 0) {
                        engine.transfer("A", "B", 1.0);
                    } else {
                        engine.transfer("B", "A", 1.0);
                    }
                }));
            }
            for (Future<?> f : futures) {
                f.get(30, TimeUnit.SECONDS);
            }
            pool.shutdown();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

            double total = engine.getAccount("A").getBalance() + engine.getAccount("B").getBalance();
            assertEquals(1000.0, total, 1e-9);
        }
    }
}
