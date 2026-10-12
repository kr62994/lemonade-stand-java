package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CashAccountTest {
    private CashAccount account;

    @BeforeEach
    void setUp() {
        account = new CashAccount(10000);
    }

    @Test
    void startsWithGivenBalance() {
        assertEquals(10000, account.balanceCents());
    }

    @Test
    void allowsZeroStartingBalance() { // < 0
        CashAccount emptyAccount = new CashAccount(0);
        assertEquals(0, emptyAccount.balanceCents());
    }

    @Test
    void rejectsNegativeStartingBalance() {
        assertThrows(IllegalArgumentException.class, () -> new CashAccount(-1));
    }

    @Test
    void paymentReducesBalance() {
        account.pay(2500);
        assertEquals(7500, account.balanceCents());
    }

    @Test
    void canPayEntireBalance() {
        account.pay(10000);
        assertEquals(0, account.balanceCents());
    }

    @Test
    void rejectsUnaffordablePayment() {
        assertThrows(IllegalArgumentException.class, () -> account.pay(10001));
        assertEquals(10000, account.balanceCents());
    }

    @Test
    void rejectsNegativePayment() {
        assertThrows(IllegalArgumentException.class, () -> account.pay(-1));
        assertEquals(10000, account.balanceCents());
    }

    @Test
    void depositIncreasesBalance() {
        account.deposit(1500);
        assertEquals(11500, account.balanceCents());
    }

    @Test
    void rejectsNegativeDeposit() {
        assertThrows(IllegalArgumentException.class, () -> account.deposit(-1));
        assertEquals(10000, account.balanceCents());
    }

    @Test
    void zeroTransactionsLeaveBalanceUnchanged() {
        account.pay(0);
        account.deposit(0);
        assertEquals(10000, account.balanceCents());
    }
}
