package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LemonadeGameTest {
    @Test
    void formatsMoneyWithTwoDecimalPlaces() {
        assertEquals("$0.00", LemonadeGame.money(0));
        assertEquals("$0.01", LemonadeGame.money(1));
        assertEquals("$5.00", LemonadeGame.money(500));
        assertEquals("$12.34", LemonadeGame.money(1234));
    }

    @Test
    void formatsDebtAndLargeBalances() {
        assertEquals("$-0.01", LemonadeGame.money(-1));
        assertEquals("$92233720368547758.07", LemonadeGame.money(Long.MAX_VALUE));
        assertEquals("$-92233720368547758.08", LemonadeGame.money(Long.MIN_VALUE));
    }
}
