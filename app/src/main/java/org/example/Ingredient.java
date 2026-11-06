package org.example;

public enum Ingredient {
    LEMON("lemons", "lemon"),
    SUGAR("sugar", "cup");

    private final String displayName;
    private final String unit;

    Ingredient(String displayName, String unit) {
        this.displayName = displayName;
        this.unit = unit;
    }

    public String displayName() { return displayName; }
    public String unit() { return unit; }
}
