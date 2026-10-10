package org.example;

import java.util.Random;

public class RandomCustomerSource implements CustomerSource {
    private final Random random = new Random();

    @Override
    public int customersToday() {
        return random.nextInt(99) + 1; // simple implementation for now
    }
}
