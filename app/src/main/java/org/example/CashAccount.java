package org.example;

public class CashAccount {
    private long balanceCents;

    public CashAccount(long startingCents) {
        if (startingCents < 0) {
            throw new IllegalArgumentException("Starting cash cannot be negative.");
        }
        balanceCents = startingCents;
    }

    public long balanceCents() {
        return balanceCents;
    }

    public void pay(long cents) {
        if (cents < 0) {
            throw new IllegalArgumentException("Payment cannot be negative.");
        }
        if (cents > balanceCents) {
            throw new IllegalArgumentException("You do not have enough money for that payment.");
        }
        balanceCents -= cents;
    }

    public void deposit(long cents) {
        if (cents < 0) {
            throw new IllegalArgumentException("Deposit cannot be negative.");
        }
        if (balanceCents > Long.MAX_VALUE - cents) {
            throw new IllegalArgumentException("That deposit would exceed the maximum account balance.");
        }
        balanceCents += cents;
    }

    public void chargeExpense(long cents) {
        if (cents < 0) {
            throw new IllegalArgumentException("Expense cannot be negative.");
        }
        if (balanceCents < Long.MIN_VALUE + cents) {
            throw new IllegalArgumentException("That expense would exceed the maximum account debt.");
        }
        balanceCents -= cents;
    }
}
