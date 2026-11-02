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
    void depositCanReachMaximumBalance() {
        account.deposit(Long.MAX_VALUE - account.balanceCents());
        assertEquals(Long.MAX_VALUE, account.balanceCents());
        account.deposit(0);
        assertEquals(Long.MAX_VALUE, account.balanceCents());
    }

    @Test
    void rejectsDepositOverflowWithoutChangingBalance() {
        assertThrows(IllegalArgumentException.class, () -> account.deposit(Long.MAX_VALUE));
        assertEquals(10000, account.balanceCents());
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

    @Test
    void mandatoryExpensesCanReachZeroAndThenCreateDebt() {
        account.chargeExpense(10000);
        assertEquals(0, account.balanceCents());
        account.chargeExpense(500);
        assertEquals(-500, account.balanceCents());
        account.chargeExpense(0);
        account.deposit(200);
        assertEquals(-300, account.balanceCents());
        account.deposit(300);
        assertEquals(0, account.balanceCents());
    }

    @Test
    void rejectsNegativeExpenseWithoutChangingBalance() {
        assertThrows(IllegalArgumentException.class, () -> account.chargeExpense(-1));
        assertEquals(10000, account.balanceCents());
    }

    @Test
    void expenseCannotWrapDebtIntoPositiveBalance() {
        CashAccount debt = new CashAccount(0);
        debt.chargeExpense(Long.MAX_VALUE);
        debt.chargeExpense(1);
        assertEquals(Long.MIN_VALUE, debt.balanceCents());
        assertThrows(IllegalArgumentException.class, () -> debt.chargeExpense(1));
        assertEquals(Long.MIN_VALUE, debt.balanceCents());
        debt.deposit(Long.MAX_VALUE);
        assertEquals(-1, debt.balanceCents());
    }

    @Test
    void purchasesCannotIncreaseDebt() {
        account.chargeExpense(10500);
        assertThrows(IllegalArgumentException.class, () -> account.pay(1));
        assertEquals(-500, account.balanceCents());
    }
}
