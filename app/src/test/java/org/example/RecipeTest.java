package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecipeTest {
    @Test
    void fillsEmptyRecipeWithWater() {
        assertEquals(new Recipe(0, 0, 8), new Recipe(0, 0, 0));
    }

    @Test
    void topsUpUsingLemonAndDissolvedSugarVolumes() {
        Recipe recipe = new Recipe(3, 1, 6);
        assertEquals(3, recipe.lemons());
        assertEquals(1, recipe.sugar());
        assertEquals(6.75, recipe.water());
    }

    @Test
    void acceptsExactlyEightCupsWithoutAddingWater() {
        assertEquals(6.75, new Recipe(3, 1, 6.75).water());
        assertEquals(0.0, new Recipe(32, 0, 0).water());
        assertEquals(0.0, new Recipe(0, 16, 0).water());
        assertEquals(0.0, new Recipe(16, 8, 0).water());
    }

    @Test
    void permitsLeavingOutEitherIngredient() {
        assertEquals(7.75, new Recipe(1, 0, 0).water());
        assertEquals(7.5, new Recipe(0, 1, 0).water());
    }

    @Test
    void rejectsOverfilledRecipesIncludingQuarterCupOverflow() {
        assertThrows(IllegalArgumentException.class, () -> new Recipe(33, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Recipe(0, 17, 0));
        assertThrows(IllegalArgumentException.class, () -> new Recipe(3, 1, 7));
        assertThrows(IllegalArgumentException.class, () -> new Recipe(0, 0, 8.25));
    }

    @Test
    void rejectsNegativeIngredients() {
        assertThrows(IllegalArgumentException.class, () -> new Recipe(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Recipe(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Recipe(0, 0, -0.25));
    }

    @Test
    void rejectsHugeQuantitiesWithoutOverflow() {
        assertThrows(IllegalArgumentException.class,
                () -> new Recipe(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> new Recipe(Integer.MAX_VALUE, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Recipe(0, Integer.MAX_VALUE, 0));
    }

    @Test
    void everyCombinationThatFitsTotalsExactlyEightCups() {
        for (int lemons = 0; lemons <= 32; lemons++) {
            for (int sugar = 0; sugar <= 16; sugar++) {
                double volume = lemons / 4.0 + sugar / 2.0;
                if (volume <= 8) {
                    Recipe recipe = new Recipe(lemons, sugar, 0);
                    assertEquals(8.0, volume + recipe.water());
                }
            }
        }
    }
}
