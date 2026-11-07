package org.example;

public class LemonadeStand {
    public static final int CUPS_PER_PITCHER = 8;
    public static final long DAILY_RENT_CENTS = 500;
    public static final long MAX_PRICE_CENTS = Long.MAX_VALUE / 750;
    public static final long DEMAND_CUTOFF_CENTS = 1000;
    private final CashAccount account;
    private final IngredientMarket lemonMarket;
    private final IngredientMarket sugarMarket;
    private int lemons;
    private int sugar;
    private int water; // for updates
    private int pitchers;
    private long priceCents = 100;
    private Recipe recipe = new Recipe(0, 0, 8);

    public LemonadeStand() {
        this(new CashAccount(10_000));
    }

    public LemonadeStand(CashAccount account) {
        this(account, new IngredientMarket(100), new IngredientMarket(50));
    }

    public LemonadeStand(CashAccount account, IngredientMarket lemonMarket, IngredientMarket sugarMarket) {
        if (account == null) {
            throw new IllegalArgumentException("An account is required.");
        }
        if (lemonMarket == null || sugarMarket == null) {
            throw new IllegalArgumentException("Lemon and sugar markets are required.");
        }
        this.account = account;
        this.lemonMarket = lemonMarket;
        this.sugarMarket = sugarMarket;
    }

    public long cashCents() { return account.balanceCents(); }
    public int lemons() { return lemons; }
    public int sugar() { return sugar; }
    public int pitchers() { return pitchers; }
    public long priceCents() { return priceCents; }
    public Recipe recipe() { return recipe; }
    public IngredientMarket lemonMarket() { return lemonMarket; }
    public IngredientMarket sugarMarket() { return sugarMarket; }

    public void payRent() {
        account.chargeExpense(DAILY_RENT_CENTS);
    }

    public boolean isBankrupt() {
        return cashCents() < 0;
    }

    public void purchase(int lemonCount, int sugarCount) {
        if (lemonCount < 0 || sugarCount < 0)
            throw new IllegalArgumentException("Purchase quantities cannot be negative.");
        long cost = (long) lemonCount * lemonMarket.priceCents()
                + (long) sugarCount * sugarMarket.priceCents();
        if (lemonCount > Integer.MAX_VALUE - lemons || sugarCount > Integer.MAX_VALUE - sugar) {
            throw new IllegalArgumentException("That purchase would exceed the maximum ingredient inventory.");
        }
        account.pay(cost);
        lemons += lemonCount;
        sugar += sugarCount;
    }

    public void setRecipe(Recipe recipe) {
        if (recipe == null) throw new IllegalArgumentException("A recipe is required.");
        this.recipe = recipe;
    }

    public void setPrice(long cents) {
        if (cents < 0 || cents > MAX_PRICE_CENTS)
            throw new IllegalArgumentException("Price must be from 0 to " + MAX_PRICE_CENTS + " cents.");
        priceCents = cents;
    }

    public void makePitchers(int count) {
        if (count < 1) throw new IllegalArgumentException("Make at least one pitcher.");
        if ((long) count * recipe.lemons() > lemons || (long) count * recipe.sugar() > sugar)
            throw new IllegalArgumentException("You do not have enough lemons or sugar for those pitchers.");
        if (count > Integer.MAX_VALUE / CUPS_PER_PITCHER - pitchers)
            throw new IllegalArgumentException("That many pitchers would exceed the maximum cup inventory.");
        int neededLemons = recipe.lemons() * count;
        int neededSugar = recipe.sugar() * count;
        lemons -= neededLemons;
        sugar -= neededSugar;
        pitchers += count;
    }

    public SalesResult sell(int customers) {
        if (customers < 1 || customers > 500)
            throw new IllegalArgumentException("Customer count must be between 1 and 500.");
        int cups = pitchers * CUPS_PER_PITCHER;
        if (priceCents == 0) {
            customers = cups;
        } else if (priceCents >= DEMAND_CUTOFF_CENTS) {
            customers = 0;
        } else {
            double demand = (DEMAND_CUTOFF_CENTS - priceCents) / (double) DEMAND_CUTOFF_CENTS;
            customers = (int) Math.round(customers * recipe.customerMultiplier() * demand);
        }
        int sold = Math.min(cups, customers);
        long revenue = sold * priceCents;
        account.deposit(revenue);
        pitchers = 0;
        return new SalesResult(customers, sold, revenue, cups - sold);
    }
}
