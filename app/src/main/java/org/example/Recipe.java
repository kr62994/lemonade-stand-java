package org.example;

public record Recipe(int lemons, int sugar, double water) {
    public Recipe {
        if (lemons < 0 || sugar < 0 || water < 0)
            throw new IllegalArgumentException("Recipe ingredients must be nonnegative amounts.");
        double ingredientVolume = lemons / 4.0 + sugar / 2.0;
        if (ingredientVolume + water > LemonadeStand.CUPS_PER_PITCHER)
            throw new IllegalArgumentException("A recipe cannot exceed 8 cups of liquid. Recipe unchanged.");
        water = LemonadeStand.CUPS_PER_PITCHER - ingredientVolume;
    }
}
