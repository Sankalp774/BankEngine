package com.bank;

import com.bank.service.BankEngine;
import com.bank.util.AuditLogger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Concurrent simulation entrypoint.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Path auditPath = Path.of("system_audit.log");
        Path inventoryPath = Path.of("inventory.csv");

        try (AuditLogger audit = new AuditLogger(auditPath)) {
            BankEngine engine = new BankEngine(audit);

            engine.openAccount("A001", 1000.0);
            engine.openAccount("A002", 1000.0);
            engine.openAccount("A003", 1000.0);

            int workers = 16;
            int opsPerWorker = 200;
            ExecutorService pool = Executors.newFixedThreadPool(workers);
            List<Future<?>> futures = new ArrayList<>();

            for (int w = 0; w < workers; w++) {
                futures.add(pool.submit(() -> {
                    ThreadLocalRandom rnd = ThreadLocalRandom.current();
                    String[] ids = {"A001", "A002", "A003"};
                    for (int i = 0; i < opsPerWorker; i++) {
                        int op = rnd.nextInt(3);
                        String from = ids[rnd.nextInt(ids.length)];
                        String to = ids[rnd.nextInt(ids.length)];
                        double amount = 1.0 + rnd.nextInt(10);
                        try {
                            switch (op) {
                                case 0 -> engine.deposit(from, amount);
                                case 1 -> engine.withdraw(from, amount);
                                default -> engine.transfer(from, to, amount);
                            }
                        } catch (IllegalArgumentException ignored) {
                            // same-account transfer etc.
                        }
                    }
                }));
            }

            for (Future<?> f : futures) {
                f.get();
            }
            pool.shutdown();
            pool.awaitTermination(30, TimeUnit.SECONDS);

            engine.dumpInventory(inventoryPath);
            System.out.println("Simulation complete.");
            System.out.println("Audit log: " + auditPath.toAbsolutePath());
            System.out.println("Inventory: " + inventoryPath.toAbsolutePath());
            engine.allAccounts().forEach(a ->
                    System.out.println(a.getId() + " -> " + a.getBalance()));
        }
    }
}
