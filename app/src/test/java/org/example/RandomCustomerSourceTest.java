package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RandomCustomerSourceTest {
    private RandomCustomerSource customerSource;

    @BeforeEach
    void setUp() {
        customerSource = new RandomCustomerSource();
    }

    @Test
    void customerCountsStayWithinCurrentRange() { // idk
        for (int day = 0; day < 1000; day++) {
            int customers = customerSource.customersToday();
            assertTrue(customers >= 1 && customers <= 99);
        }
    }
}
