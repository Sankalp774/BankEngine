package com.bank.service;

import com.bank.model.BankAccount;
import com.bank.util.AuditLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory banking engine with ordered locking for deadlock-free transfers.
 */
public class BankEngine {
    private final Map<String, BankAccount> accounts = new ConcurrentHashMap<>();
    private final AuditLogger auditLogger;

    public BankEngine(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    public BankAccount openAccount(String id, double initialBalance) {
        BankAccount account = new BankAccount(id, initialBalance);
        BankAccount existing = accounts.putIfAbsent(id, account);
        if (existing != null) {
            throw new IllegalStateException("Account already exists: " + id);
        }
        auditLogger.log("OPEN account=" + id + " balance=" + initialBalance);
        return account;
    }

    public BankAccount getAccount(String id) {
        BankAccount account = accounts.get(id);
        if (account == null) {
            throw new IllegalArgumentException("Unknown account: " + id);
        }
        return account;
    }

    public Collection<BankAccount> allAccounts() {
        return accounts.values();
    }

    public void deposit(String id, double amount) {
        requirePositive(amount);
        BankAccount account = getAccount(id);
        account.getLock().lock();
        try {
            account.credit(amount);
            auditLogger.log("DEPOSIT account=" + id + " amount=" + amount + " ok");
        } finally {
            account.getLock().unlock();
        }
    }

    public boolean withdraw(String id, double amount) {
        requirePositive(amount);
        BankAccount account = getAccount(id);
        account.getLock().lock();
        try {
            if (!tryDebit(account, amount)) {
                auditLogger.log("WITHDRAW account=" + id + " amount=" + amount + " FAIL insufficient");
                return false;
            }
            auditLogger.log("WITHDRAW account=" + id + " amount=" + amount + " ok");
            return true;
        } finally {
            account.getLock().unlock();
        }
    }

    /**
     * Deadlock-free transfer: always lock accounts in sorted id order.
     */
    public boolean transfer(String fromId, String toId, double amount) {
        requirePositive(amount);
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        BankAccount a = getAccount(fromId);
        BankAccount b = getAccount(toId);

        BankAccount first = a.getId().compareTo(b.getId()) < 0 ? a : b;
        BankAccount second = first == a ? b : a;

        first.getLock().lock();
        second.getLock().lock();
        try {
            if (!tryDebit(a, amount)) {
                auditLogger.log("TRANSFER " + fromId + "->" + toId + " amount=" + amount + " FAIL");
                return false;
            }
            b.credit(amount);
            auditLogger.log("TRANSFER " + fromId + "->" + toId + " amount=" + amount + " ok");
            return true;
        } finally {
            second.getLock().unlock();
            first.getLock().unlock();
        }
    }

    public void dumpInventory(Path csvPath) throws IOException {
        StringBuilder sb = new StringBuilder("account_id,balance\n");
        accounts.values().stream()
                .sorted(Comparator.comparing(BankAccount::getId))
                .forEach(acc -> sb.append(acc.getId()).append(',').append(acc.getBalance()).append('\n'));
        Files.writeString(csvPath, sb.toString(), StandardCharsets.UTF_8);
        auditLogger.log("DUMP inventory -> " + csvPath.toAbsolutePath());
    }

    private static void requirePositive(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    /** Caller must hold account lock. */
    private boolean tryDebit(BankAccount account, double amount) {
        if (account.balanceUnlocked() < amount) {
            return false;
        }
        account.debit(amount);
        return true;
    }
}
