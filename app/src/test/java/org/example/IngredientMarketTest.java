package org.example;

import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IngredientMarketTest {
    private Random lowestPrice() {
        return new Random() {
            @Override
            public long nextLong(long origin, long bound) { return origin; }
        };
    }

    private Random highestPrice() {
        return new Random() {
            @Override
            public long nextLong(long origin, long bound) { return bound - 1; }
        };
    }

    @Test
    void startsAtTheGivenPriceWithoutFluctuating() {
        IngredientMarket market = new IngredientMarket(100);
        assertEquals(100, market.priceCents());
        assertEquals(100, market.previousPriceCents());
    }

    @Test
    void rejectsNonpositivePricesAndMissingRandomSource() {
        assertThrows(IllegalArgumentException.class, () -> new IngredientMarket(0));
        assertThrows(IllegalArgumentException.class, () -> new IngredientMarket(-1));
        assertThrows(IllegalArgumentException.class, () -> new IngredientMarket(100, null));
    }

    @Test
    void canReachBothFivePercentEndpoints() {
        IngredientMarket falling = new IngredientMarket(100, lowestPrice());
        IngredientMarket rising = new IngredientMarket(100, highestPrice());
        falling.advanceDay();
        rising.advanceDay();
        assertEquals(95, falling.priceCents());
        assertEquals(105, rising.priceCents());
        assertEquals(100, falling.previousPriceCents());
        assertEquals(100, rising.previousPriceCents());
    }

    @Test
    void fractionalCentLimitsRoundInward() {
        IngredientMarket falling = new IngredientMarket(50, lowestPrice());
        IngredientMarket rising = new IngredientMarket(50, highestPrice());
        falling.advanceDay();
        rising.advanceDay();
        assertEquals(48, falling.priceCents());
        assertEquals(52, rising.priceCents());
    }

    @Test
    void eachChangeUsesThePreviousDayRatherThanTheStartingPrice() {
        IngredientMarket market = new IngredientMarket(100, lowestPrice());
        market.advanceDay();
        market.advanceDay();
        assertEquals(95, market.previousPriceCents());
        assertEquals(91, market.priceCents());
        market.advanceDay();
        assertEquals(91, market.previousPriceCents());
        assertEquals(87, market.priceCents());
    }

    @Test
    void unchangedPriceIsAllowed() {
        IngredientMarket market = new IngredientMarket(100, new Random() {
            @Override
            public long nextLong(long origin, long bound) { return (origin + bound - 1) / 2; }
        });
        market.advanceDay();
        assertEquals(100, market.priceCents());
        assertEquals(100, market.previousPriceCents());
    }

    @Test
    void smallPricesRemainPositiveAndWithinFivePercent() {
        for (int price = 1; price < 20; price++) {
            IngredientMarket market = new IngredientMarket(price, lowestPrice());
            market.advanceDay();
            assertEquals(price, market.priceCents());
        }
    }

    @Test
    void largestPriceCannotOverflow() {
        IngredientMarket market = new IngredientMarket(Integer.MAX_VALUE, highestPrice());
        market.advanceDay();
        assertEquals(Integer.MAX_VALUE, market.priceCents());
        assertEquals(Integer.MAX_VALUE, market.previousPriceCents());
    }

    @Test
    void repeatedRandomPricesStayWithinDailyLimits() {
        IngredientMarket market = new IngredientMarket(10000, new Random(42));
        boolean increased = false;
        boolean decreased = false;
        for (int day = 0; day < 10000; day++) {
            int previous = market.priceCents();
            market.advanceDay();
            assertEquals(previous, market.previousPriceCents());
            long difference = (long) market.priceCents() - previous;
            assertTrue(Math.abs(difference) * 20 <= previous);
            assertTrue(market.priceCents() > 0);
            increased |= difference > 0;
            decreased |= difference < 0;
        }
        assertTrue(increased);
        assertTrue(decreased);
    }
}
