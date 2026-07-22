package com.bank.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronous append-only audit log.
 */
public class AuditLogger implements AutoCloseable {
    private final Path logPath;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "audit-logger");
        t.setDaemon(true);
        return t;
    });

    public AuditLogger(Path logPath) throws IOException {
        this.logPath = logPath;
        if (logPath.getParent() != null) {
            Files.createDirectories(logPath.getParent());
        }
        if (!Files.exists(logPath)) {
            Files.createFile(logPath);
        }
    }

    public void log(String message) {
        String line = Instant.now() + " | " + Thread.currentThread().getName() + " | " + message;
        worker.execute(() -> {
            try (BufferedWriter w = Files.newBufferedWriter(
                    logPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                w.write(line);
                w.newLine();
            } catch (IOException e) {
                System.err.println("Audit log failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void close() {
        worker.shutdown();
        try {
            worker.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
