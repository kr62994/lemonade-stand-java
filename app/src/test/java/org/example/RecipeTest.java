package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecipeTest {
    private Recipe recipe;

    @BeforeEach
    void setUp() {
        recipe = new Recipe(3, 1, 6);
    }

    @Test
    void storesIngredientAmounts() {
        assertEquals(3, recipe.lemons());
        assertEquals(1, recipe.sugar());
        assertEquals(6, recipe.water());
    }

    @Test
    void allowsOneOfEachIngredient() {
        Recipe smallRecipe = new Recipe(1, 1, 1);
        assertEquals(1, smallRecipe.lemons());
        assertEquals(1, smallRecipe.sugar());
        assertEquals(1, smallRecipe.water());
    }

    @Test
    void rejectsZeroLemons() {
        assertThrows(IllegalArgumentException.class,
                () -> new Recipe(0, recipe.sugar(), recipe.water()));
    }

    @Test
    void rejectsNegativeLemons() {
        assertThrows(IllegalArgumentException.class,
                () -> new Recipe(-1, recipe.sugar(), recipe.water()));
    }

    @Test
    void rejectsZeroSugar() {
        assertThrows(IllegalArgumentException.class,
                () -> new Recipe(recipe.lemons(), 0, recipe.water()));
    }

    @Test
    void rejectsNegativeSugar() {
        assertThrows(IllegalArgumentException.class,
                () -> new Recipe(recipe.lemons(), -1, recipe.water()));
    }

    @Test
    void rejectsZeroWater() {
        assertThrows(IllegalArgumentException.class,
                () -> new Recipe(recipe.lemons(), recipe.sugar(), 0));
    }

    @Test
    void rejectsNegativeWater() {
        assertThrows(IllegalArgumentException.class,
                () -> new Recipe(recipe.lemons(), recipe.sugar(), -1));
    }
}
