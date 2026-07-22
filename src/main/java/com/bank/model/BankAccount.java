package com.bank.model;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe bank account with a fair ReentrantLock for mutual exclusion.
 */
public class BankAccount {
    private final String id;
    private double balance;
    private final ReentrantLock lock = new ReentrantLock(true); // fair lock

    public BankAccount(String id, double initialBalance) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("account id required");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("initial balance cannot be negative");
        }
        this.id = id;
        this.balance = initialBalance;
    }

    public String getId() {
        return id;
    }

    public double getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }

    /** Caller must already hold {@link #getLock()}. */
    double balanceUnlocked() {
        return balance;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    /** Caller must already hold {@link #getLock()}. */
    void credit(double amount) {
        balance += amount;
    }

    /** Caller must already hold {@link #getLock()}. */
    void debit(double amount) {
        balance -= amount;
    }
}
