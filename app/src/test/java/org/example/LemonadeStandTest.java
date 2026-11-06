package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LemonadeStandTest {
    private LemonadeStand stand;

    @BeforeEach
    void setUp() {
        stand = new LemonadeStand();
        stand.setRecipe(new Recipe(3, 1, 6));
    }

    @Test
    void rejectsIngredientInventoryOverflowBeforeCharging() {
        CashAccount account = new CashAccount(Long.MAX_VALUE);
        LemonadeStand largeStand = new LemonadeStand(account);
        largeStand.purchase(Integer.MAX_VALUE, Integer.MAX_VALUE);
        long balance = account.balanceCents();
        assertThrows(IllegalArgumentException.class, () -> largeStand.purchase(1, 0));
        assertThrows(IllegalArgumentException.class, () -> largeStand.purchase(0, 1));
        assertEquals(Integer.MAX_VALUE, largeStand.lemons());
        assertEquals(Integer.MAX_VALUE, largeStand.sugar());
        assertEquals(balance, account.balanceCents());
        assertEquals(Long.MAX_VALUE - Integer.MAX_VALUE * 150L, balance);
    }

    @Test
    void hugeRecipeAndBatchCannotWrapIngredientRequirements() {
        stand.purchase(10, 10);
        Recipe original = stand.recipe();
        assertThrows(IllegalArgumentException.class,
                () -> stand.setRecipe(new Recipe(Integer.MAX_VALUE, 1, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> stand.setRecipe(new Recipe(1, Integer.MAX_VALUE, 1)));
        assertEquals(original, stand.recipe());
        stand.setRecipe(new Recipe(3, 1, 6));
        assertThrows(IllegalArgumentException.class, () -> stand.makePitchers(Integer.MAX_VALUE));
        assertEquals(10, stand.lemons());
        assertEquals(10, stand.sugar());
        assertEquals(0, stand.pitchers());
    }

    @Test
    void rejectsCupOverflowBeforeConsumingIngredients() {
        LemonadeStand largeStand = new LemonadeStand(new CashAccount(Long.MAX_VALUE));
        largeStand.purchase(Integer.MAX_VALUE, Integer.MAX_VALUE);
        largeStand.setRecipe(new Recipe(1, 1, 0));
        int maximumPitchers = Integer.MAX_VALUE / LemonadeStand.CUPS_PER_PITCHER;
        assertThrows(IllegalArgumentException.class, () -> largeStand.makePitchers(maximumPitchers + 1));
        assertEquals(Integer.MAX_VALUE, largeStand.lemons());
        largeStand.makePitchers(maximumPitchers);
        int remainingLemons = largeStand.lemons();
        assertThrows(IllegalArgumentException.class, () -> largeStand.makePitchers(1));
        assertEquals(remainingLemons, largeStand.lemons());
        assertEquals(maximumPitchers, largeStand.pitchers());
        largeStand.setPrice(1);
        SalesResult result = largeStand.sell(1);
        assertEquals(maximumPitchers * 8 - 1, result.wastedCups());
    }

    @Test
    void rejectsPricesThatCouldOverflowDailyRevenue() {
        assertThrows(IllegalArgumentException.class, () -> stand.setPrice(LemonadeStand.MAX_PRICE_CENTS + 1));
        assertThrows(IllegalArgumentException.class, () -> stand.setPrice(Long.MAX_VALUE));
        assertEquals(100, stand.priceCents());
    }

    @Test
    void maximumPriceCanProduceFiveHundredSalesWithoutOverflow() {
        CashAccount emptyAccount = new CashAccount(9450);
        LemonadeStand salesStand = new LemonadeStand(emptyAccount);
        salesStand.setRecipe(new Recipe(1, 1, 1));
        salesStand.purchase(63, 63);
        salesStand.makePitchers(63);
        salesStand.setPrice(LemonadeStand.MAX_PRICE_CENTS);
        SalesResult result = salesStand.sell(500);
        assertEquals(500, result.cupsSold());
        assertEquals(500 * LemonadeStand.MAX_PRICE_CENTS, result.revenueCents());
        assertEquals(result.revenueCents(), emptyAccount.balanceCents());
        assertEquals(4, result.wastedCups());
    }

    @Test
    void rejectedRevenueDepositPreservesCashAndPreparedPitchers() {
        CashAccount account = new CashAccount(Long.MAX_VALUE);
        LemonadeStand richStand = new LemonadeStand(account);
        richStand.setRecipe(new Recipe(3, 1, 6));
        richStand.purchase(3, 1);
        richStand.makePitchers(1);
        long balance = account.balanceCents();
        assertThrows(IllegalArgumentException.class, () -> richStand.sell(8));
        assertEquals(balance, account.balanceCents());
        assertEquals(1, richStand.pitchers());
        richStand.setPrice(1);
        assertEquals(8, richStand.sell(8).cupsSold());
    }

    @Test
    void startsWithDefaultState() {
        assertEquals(10000, stand.cashCents());
        assertEquals(0, stand.lemons());
        assertEquals(0, stand.sugar());
        assertEquals(0, stand.pitchers());
        assertEquals(100, stand.priceCents());
        assertEquals(new Recipe(0, 0, 8), new LemonadeStand().recipe());
    }

    @Test
    void rentIsFixedAndChargedToTheSuppliedAccount() {
        CashAccount account = new CashAccount(10000);
        LemonadeStand rentedStand = new LemonadeStand(account);
        rentedStand.payRent();
        rentedStand.payRent();
        assertEquals(10000 - 2 * LemonadeStand.DAILY_RENT_CENTS, account.balanceCents());
        assertFalse(rentedStand.isBankrupt());
    }

    @Test
    void exactlyZeroAfterRentContinuesButOneCentBelowEndsTheGame() {
        LemonadeStand zeroStand = new LemonadeStand(new CashAccount(LemonadeStand.DAILY_RENT_CENTS));
        zeroStand.payRent();
        assertEquals(0, zeroStand.cashCents());
        assertFalse(zeroStand.isBankrupt());
        LemonadeStand poorStand = new LemonadeStand(new CashAccount(LemonadeStand.DAILY_RENT_CENTS - 1));
        poorStand.payRent();
        assertEquals(-1, poorStand.cashCents());
        assertTrue(poorStand.isBankrupt());
    }

    @Test
    void rejectsMissingAccount() {
        assertThrows(IllegalArgumentException.class, () -> new LemonadeStand(null));
    }

    @Test
    void purchasesAndSalesUseSuppliedAccount() {
        CashAccount account = new CashAccount(10000);
        LemonadeStand otherStand = new LemonadeStand(account);
        otherStand.setRecipe(new Recipe(3, 1, 6));
        otherStand.purchase(3, 1);
        assertEquals(9650, account.balanceCents());
        otherStand.makePitchers(1);
        otherStand.sell(8);
        assertEquals(10450, account.balanceCents());
        assertEquals(account.balanceCents(), otherStand.cashCents());
    }

    @Test
    void purchaseAddsIngredientsAndChargesCorrectCost() {
        stand.purchase(10, 4);
        assertEquals(10, stand.lemons());
        assertEquals(4, stand.sugar());
        assertEquals(8800, stand.cashCents());
    }

    @Test
    void canPurchaseJustOneIngredient() {
        stand.purchase(2, 0);
        stand.purchase(0, 3);
        assertEquals(2, stand.lemons());
        assertEquals(3, stand.sugar());
        assertEquals(9650, stand.cashCents());
    }

    @Test
    void zeroPurchaseLeavesStateUnchanged() {
        stand.purchase(0, 0);
        assertEquals(10000, stand.cashCents());
        assertEquals(0, stand.lemons());
        assertEquals(0, stand.sugar());
    }

    @Test
    void unaffordablePurchaseLeavesStateUnchanged() {
        assertThrows(IllegalArgumentException.class, () -> stand.purchase(100, 1));
        assertEquals(10000, stand.cashCents());
        assertEquals(0, stand.lemons());
        assertEquals(0, stand.sugar());
    }

    @Test
    void rejectsNegativePurchaseQuantities() {
        assertThrows(IllegalArgumentException.class, () -> stand.purchase(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> stand.purchase(0, -1));
        assertEquals(10000, stand.cashCents());
        assertEquals(0, stand.lemons());
        assertEquals(0, stand.sugar());
    }

    @Test
    void recipeCanBeChangedMultipleTimes() {
        stand.setRecipe(new Recipe(5, 2, 5));
        stand.setRecipe(new Recipe(1, 1, 4));
        assertEquals(new Recipe(1, 1, 4), stand.recipe());
    }

    @Test
    void rejectsMissingRecipe() {
        Recipe originalRecipe = stand.recipe();
        assertThrows(IllegalArgumentException.class, () -> stand.setRecipe(null));
        assertEquals(originalRecipe, stand.recipe());
    }

    @Test
    void priceCanBeChanged() {
        stand.setPrice(125);
        assertEquals(125, stand.priceCents());
    }

    @Test
    void rejectsZeroAndNegativePrices() {
        assertThrows(IllegalArgumentException.class, () -> stand.setPrice(0));
        assertThrows(IllegalArgumentException.class, () -> stand.setPrice(-1));
        assertEquals(100, stand.priceCents());
    }

    @Test
    void makingPitchersConsumesIngredientsAndAddsToExistingStock() {
        stand.purchase(9, 3);
        stand.makePitchers(1);
        stand.makePitchers(2);
        assertEquals(0, stand.lemons());
        assertEquals(0, stand.sugar());
        assertEquals(3, stand.pitchers());
        assertEquals(8950, stand.cashCents());
    }

    @Test
    void insufficientLemonsLeaveIngredientsUnchanged() {
        stand.purchase(2, 1);
        assertThrows(IllegalArgumentException.class, () -> stand.makePitchers(1));
        assertEquals(2, stand.lemons());
        assertEquals(1, stand.sugar());
        assertEquals(0, stand.pitchers());
    }

    @Test
    void insufficientSugarLeavesIngredientsUnchanged() {
        stand.purchase(3, 0);
        assertThrows(IllegalArgumentException.class, () -> stand.makePitchers(1));
        assertEquals(3, stand.lemons());
        assertEquals(0, stand.sugar());
        assertEquals(0, stand.pitchers());
    }

    @Test
    void rejectsZeroAndNegativePitcherCounts() {
        assertThrows(IllegalArgumentException.class, () -> stand.makePitchers(0));
        assertThrows(IllegalArgumentException.class, () -> stand.makePitchers(-1));
        assertEquals(0, stand.pitchers());
    }

    @Test
    void recipeChangesAffectOnlyFuturePitchers() {
        stand.purchase(10, 10);
        stand.makePitchers(1);
        stand.setRecipe(new Recipe(1, 2, 4));
        assertEquals(1, stand.pitchers());
        assertEquals(7, stand.lemons());
        assertEquals(9, stand.sugar());
        stand.makePitchers(1);
        assertEquals(2, stand.pitchers());
        assertEquals(6, stand.lemons());
        assertEquals(7, stand.sugar());
    }

    @Test
    void salesCreditRevenueDiscardLeftoversAndKeepUnusedIngredients() {
        stand.setRecipe(new Recipe(5, 2, 5));
        stand.purchase(50, 40);
        stand.makePitchers(8);
        SalesResult result = stand.sell(30);
        assertEquals(30, result.customers());
        assertEquals(30, result.cupsSold());
        assertEquals(3000, result.revenueCents());
        assertEquals(34, result.wastedCups());
        assertEquals(6000, stand.cashCents());
        assertEquals(10, stand.lemons());
        assertEquals(24, stand.sugar());
        assertEquals(0, stand.pitchers());
        SalesResult nextDay = stand.sell(1);
        assertEquals(0, nextDay.cupsSold());
        assertEquals(6000, stand.cashCents());
    }

    @Test
    void salesStopWhenLemonadeRunsOutAndUseCurrentPrice() {
        stand.purchase(3, 1);
        stand.makePitchers(1);
        stand.setPrice(125);
        SalesResult result = stand.sell(500);
        assertEquals(8, result.cupsSold());
        assertEquals(1000, result.revenueCents());
        assertEquals(0, result.wastedCups());
        assertEquals(10650, stand.cashCents());
    }

    @Test
    void noPreparedLemonadeMeansNoSales() {
        SalesResult result = stand.sell(1);
        assertEquals(0, result.cupsSold());
        assertEquals(0, result.revenueCents());
        assertEquals(0, result.wastedCups());
        assertEquals(10000, stand.cashCents());
    }

    @Test
    void invalidCustomerCountsLeavePreparedStockUnchanged() {
        stand.purchase(3, 1);
        stand.makePitchers(1);
        assertThrows(IllegalArgumentException.class, () -> stand.sell(0));
        assertThrows(IllegalArgumentException.class, () -> stand.sell(501));
        assertEquals(1, stand.pitchers());
        assertEquals(9650, stand.cashCents());
    }

    @Test
    void zeroCashStillAllowsUsingExistingIngredients() {
        stand.purchase(80, 40);
        assertEquals(0, stand.cashCents());
        assertThrows(IllegalArgumentException.class, () -> stand.purchase(1, 0));
        stand.makePitchers(1);
        stand.sell(1);
        assertEquals(100, stand.cashCents());
    }

    @Test
    void startingWaterRecipeNeedsNoPurchasedIngredients() {
        LemonadeStand waterStand = new LemonadeStand();
        waterStand.makePitchers(2);
        assertEquals(2, waterStand.pitchers());
        assertEquals(0, waterStand.lemons());
        assertEquals(0, waterStand.sugar());
        assertEquals(10000, waterStand.cashCents());
        assertEquals(2, waterStand.sell(20).cupsSold());
    }

    @Test
    void recipesCanUseOnlyLemonsOrOnlySugar() {
        stand.purchase(4, 4);
        stand.setRecipe(new Recipe(0, 2, 0));
        stand.makePitchers(2);
        assertEquals(4, stand.lemons());
        assertEquals(0, stand.sugar());
        stand.setRecipe(new Recipe(2, 0, 0));
        stand.makePitchers(2);
        assertEquals(0, stand.lemons());
        assertEquals(4, stand.pitchers());
    }

    @Test
    void overfilledRecipeLeavesExistingRecipeAndPreparedStockUnchanged() {
        stand.purchase(3, 1);
        stand.makePitchers(1);
        Recipe original = stand.recipe();
        long cash = stand.cashCents();
        assertThrows(IllegalArgumentException.class, () -> stand.setRecipe(new Recipe(3, 1, 7)));
        assertSame(original, stand.recipe());
        assertEquals(1, stand.pitchers());
        assertEquals(cash, stand.cashCents());
    }

    @Test
    void waterOnlyBatchesStillRespectCupLimit() {
        stand.setRecipe(new Recipe(0, 0, 0));
        int maximum = Integer.MAX_VALUE / LemonadeStand.CUPS_PER_PITCHER;
        assertThrows(IllegalArgumentException.class, () -> stand.makePitchers(maximum + 1));
        assertEquals(0, stand.pitchers());
        stand.makePitchers(maximum);
        assertThrows(IllegalArgumentException.class, () -> stand.makePitchers(1));
        assertEquals(maximum, stand.pitchers());
    }

    @Test
    void recipeMultipliersScaleCustomersSalesRevenueAndWaste() {
        Recipe[] recipes = {new Recipe(32, 0, 0), new Recipe(0, 0, 0),
                new Recipe(1, 0, 0), new Recipe(8, 1, 0),
                new Recipe(6, 1, 0), new Recipe(3, 1, 0)};
        int[] expectedCustomers = {0, 2, 10, 25, 30, 20};
        for (int i = 0; i < recipes.length; i++) {
            LemonadeStand salesStand = new LemonadeStand();
            salesStand.purchase(96, 8);
            salesStand.setRecipe(recipes[i]);
            salesStand.makePitchers(3);
            long cash = salesStand.cashCents();
            SalesResult result = salesStand.sell(20);
            int sold = Math.min(24, expectedCustomers[i]);
            assertEquals(expectedCustomers[i], result.customers());
            assertEquals(sold, result.cupsSold());
            assertEquals(sold * 100L, result.revenueCents());
            assertEquals(24 - sold, result.wastedCups());
            assertEquals(cash + result.revenueCents(), salesStand.cashCents());
            assertEquals(0, salesStand.pitchers());
        }
    }

    @Test
    void fractionalCustomerCountsRoundDownIncludingToZero() {
        stand.setRecipe(new Recipe(0, 0, 0));
        assertEquals(0, stand.sell(9).customers());
        stand.setRecipe(new Recipe(1, 0, 0));
        assertEquals(1, stand.sell(3).customers());
        stand.setRecipe(new Recipe(8, 1, 0));
        assertEquals(3, stand.sell(3).customers());
        stand.setRecipe(new Recipe(6, 1, 0));
        assertEquals(4, stand.sell(3).customers());
    }

    @Test
    void maximumBoostAndPriceCannotOverflowDailyRevenue() {
        CashAccount account = new CashAccount(100000);
        LemonadeStand salesStand = new LemonadeStand(account);
        salesStand.setRecipe(new Recipe(6, 1, 0));
        salesStand.purchase(564, 94);
        salesStand.makePitchers(94);
        account.pay(account.balanceCents());
        salesStand.setPrice(LemonadeStand.MAX_PRICE_CENTS);
        SalesResult result = salesStand.sell(500);
        assertEquals(750, result.customers());
        assertEquals(750, result.cupsSold());
        assertEquals(750 * LemonadeStand.MAX_PRICE_CENTS, result.revenueCents());
        assertEquals(result.revenueCents(), salesStand.cashCents());
        assertEquals(2, result.wastedCups());
    }

    @Test
    void adjustedAttendanceUsesTheCurrentRecipe() {
        stand.setRecipe(new Recipe(0, 0, 0));
        stand.makePitchers(1);
        stand.setRecipe(new Recipe(6, 1, 0));
        SalesResult result = stand.sell(20);
        assertEquals(30, result.customers());
        assertEquals(8, result.cupsSold());
    }

    @Test
    void purchasesUseBothCurrentMarketPrices() {
        IngredientMarket lemons = new IngredientMarket(100, new java.util.Random(1));
        IngredientMarket sugar = new IngredientMarket(50, new java.util.Random(2));
        CashAccount account = new CashAccount(10000);
        LemonadeStand marketStand = new LemonadeStand(account, lemons, sugar);
        lemons.advanceDay();
        sugar.advanceDay();
        long firstCost = 3L * lemons.priceCents() + 2L * sugar.priceCents();
        marketStand.purchase(3, 2);
        assertEquals(10000 - firstCost, account.balanceCents());
        assertEquals(3, marketStand.lemons());
        assertEquals(2, marketStand.sugar());
        lemons.advanceDay();
        sugar.advanceDay();
        long secondCost = 2L * lemons.priceCents() + 4L * sugar.priceCents();
        marketStand.purchase(2, 4);
        assertEquals(10000 - firstCost - secondCost, account.balanceCents());
        assertEquals(5, marketStand.lemons());
        assertEquals(6, marketStand.sugar());
    }

    @Test
    void purchasesDoNotAdvanceMarkets() {
        stand.purchase(1, 1);
        stand.purchase(1, 1);
        assertEquals(100, stand.lemonMarket().priceCents());
        assertEquals(50, stand.sugarMarket().priceCents());
        assertEquals(9700, stand.cashCents());
    }

    @Test
    void marketChangesDoNotAlterCashOrExistingStock() {
        stand.purchase(3, 1);
        stand.lemonMarket().advanceDay();
        stand.sugarMarket().advanceDay();
        assertEquals(9650, stand.cashCents());
        assertEquals(3, stand.lemons());
        assertEquals(1, stand.sugar());
    }

    @Test
    void rejectsMissingMarkets() {
        assertThrows(IllegalArgumentException.class,
                () -> new LemonadeStand(new CashAccount(10000), null, new IngredientMarket(50)));
        assertThrows(IllegalArgumentException.class,
                () -> new LemonadeStand(new CashAccount(10000), new IngredientMarket(100), null));
    }

    @Test
    void extremeMarketPricesAndQuantitiesCannotWrapPurchaseCost() {
        IngredientMarket lemons = new IngredientMarket(Integer.MAX_VALUE);
        IngredientMarket sugar = new IngredientMarket(Integer.MAX_VALUE);
        LemonadeStand expensiveStand = new LemonadeStand(new CashAccount(10000), lemons, sugar);
        assertThrows(IllegalArgumentException.class,
                () -> expensiveStand.purchase(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(10000, expensiveStand.cashCents());
        assertEquals(0, expensiveStand.lemons());
        assertEquals(0, expensiveStand.sugar());

        CashAccount account = new CashAccount(Long.MAX_VALUE);
        LemonadeStand richStand = new LemonadeStand(account, lemons, sugar);
        richStand.purchase(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(Long.MAX_VALUE - 2L * Integer.MAX_VALUE * Integer.MAX_VALUE, account.balanceCents());
        assertEquals(Integer.MAX_VALUE, richStand.lemons());
        assertEquals(Integer.MAX_VALUE, richStand.sugar());
    }
}
