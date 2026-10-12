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
        balanceCents += cents;
    }
}
