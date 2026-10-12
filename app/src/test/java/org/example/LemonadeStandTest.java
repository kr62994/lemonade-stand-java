package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LemonadeStandTest {
    private LemonadeStand stand;

    @BeforeEach
    void setUp() {
        stand = new LemonadeStand();
    }

    @Test
    void startsWithDefaultState() {
        assertEquals(10000, stand.cashCents());
        assertEquals(0, stand.lemons());
        assertEquals(0, stand.sugar());
        assertEquals(0, stand.pitchers());
        assertEquals(100, stand.priceCents());
        assertEquals(new Recipe(3, 1, 6), stand.recipe());
    }

    @Test
    void rejectsMissingAccount() {
        assertThrows(IllegalArgumentException.class, () -> new LemonadeStand(null));
    }

    @Test
    void purchasesAndSalesUseSuppliedAccount() {
        CashAccount account = new CashAccount(10000);
        LemonadeStand otherStand = new LemonadeStand(account);
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
        stand.setRecipe(new Recipe(5, 2, 6));
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
        stand.setRecipe(new Recipe(5, 2, 6));
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
}
