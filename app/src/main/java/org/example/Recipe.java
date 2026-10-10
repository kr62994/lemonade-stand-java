package org.example;

public record Recipe(int lemons, int sugar, int water) {
    public Recipe {
        if (lemons < 1 || sugar < 1 || water < 1)
            throw new IllegalArgumentException("Each recipe ingredient must be at least 1.");
    }
}
