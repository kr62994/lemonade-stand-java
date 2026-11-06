package org.example;

import java.util.Random;

public class IngredientMarket {
    private final Random random;
    private int priceCents;
    private int previousPriceCents;

    public IngredientMarket(int startingPriceCents) {
        this(startingPriceCents, new Random());
    }

    public IngredientMarket(int startingPriceCents, Random random) {
        if (startingPriceCents < 1)
            throw new IllegalArgumentException("Ingredient prices must be at least one cent.");
        if (random == null)
            throw new IllegalArgumentException("A random source is required.");
        priceCents = startingPriceCents;
        previousPriceCents = startingPriceCents;
        this.random = random;
    }

    public int priceCents() { return priceCents; }
    public int previousPriceCents() { return previousPriceCents; }

    public void advanceDay() {
        previousPriceCents = priceCents;
        int maximumChange = priceCents / 20;
        long minimum = (long) priceCents - maximumChange;
        long maximum = Math.min(Integer.MAX_VALUE, (long) priceCents + maximumChange);
        priceCents = (int) random.nextLong(minimum, maximum + 1);
    }
}
