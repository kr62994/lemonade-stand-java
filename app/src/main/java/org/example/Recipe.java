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

    public double customerMultiplier() {
        if (water == 0) return 0.0;
        if (lemons == 0 && sugar == 0) return 0.1;
        if (sugar == 0) return 0.5;

        double juice = lemons / 4.0;
        double total = sugar + juice + water;
        double sugarShare = sugar / total;
        double juiceShare = juice / total;
        double waterShare = water / total;
        double idealSugar = 7.0 / 48;
        double idealJuice = 9.0 / 48;
        double idealWater = 32.0 / 48;

        if (Math.round(sugarShare * 10) == Math.round(idealSugar * 10)
                && Math.round(juiceShare * 10) == Math.round(idealJuice * 10)
                && Math.round(waterShare * 10) == Math.round(idealWater * 10)) {
            return 1.5;
        }
        if (Math.abs(sugarShare - idealSugar) <= 0.05
                && Math.abs(juiceShare - idealJuice) <= 0.05
                && Math.abs(waterShare - idealWater) <= 0.05) {
            return 1.25;
        }
        return 1.0;
    }
}
