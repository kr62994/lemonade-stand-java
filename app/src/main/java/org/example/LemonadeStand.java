package org.example;

public class LemonadeStand {
    public static final int CUPS_PER_PITCHER = 8;
    private final CashAccount account;
    private int lemons;
    private int sugar;
    private int water; // for updates
    private int pitchers;
    private long priceCents = 100;
    private Recipe recipe = new Recipe(3, 1, 6); // initial recipe

    public LemonadeStand() {
        this(new CashAccount(10_000));
    }

    public LemonadeStand(CashAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("An account is required.");
        }
        this.account = account;
    }

    public long cashCents() { return account.balanceCents(); }
    public int lemons() { return lemons; }
    public int sugar() { return sugar; }
    public int pitchers() { return pitchers; }
    public long priceCents() { return priceCents; }
    public Recipe recipe() { return recipe; }

    public void purchase(int lemonCount, int sugarCount) {
        if (lemonCount < 0 || sugarCount < 0)
            throw new IllegalArgumentException("Purchase quantities cannot be negative.");
        long cost = lemonCount * 100L + sugarCount * 50L;
        account.pay(cost);
        lemons += lemonCount;
        sugar += sugarCount;
    }

    public void setRecipe(Recipe recipe) {
        if (recipe == null) throw new IllegalArgumentException("A recipe is required.");
        this.recipe = recipe;
    }

    public void setPrice(long cents) {
        if (cents < 1)
            throw new IllegalArgumentException("Price must be positive.");
        priceCents = cents;
    }

    public void makePitchers(int count) {
        if (count < 1) throw new IllegalArgumentException("Make at least one pitcher.");
        int neededLemons = recipe.lemons() * count;
        int neededSugar = recipe.sugar() * count;
        if (neededLemons > lemons || neededSugar > sugar)
            throw new IllegalArgumentException("You do not have enough lemons or sugar for those pitchers.");
        lemons -= neededLemons;
        sugar -= neededSugar;
        pitchers += count;
    }

    public SalesResult sell(int customers) {
        if (customers < 1 || customers > 500)
            throw new IllegalArgumentException("Customer count must be between 1 and 500.");
        int cups = pitchers * CUPS_PER_PITCHER;
        int sold = Math.min(cups, customers);
        long revenue = sold * priceCents;
        account.deposit(revenue);
        pitchers = 0;
        return new SalesResult(customers, sold, revenue, cups - sold);
    }
}
