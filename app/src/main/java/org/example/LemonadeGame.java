package org.example;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class LemonadeGame {
    private final LemonadeStand stand;
    private final Scanner scanner = new Scanner(System.in);
    private final CustomerSource customers;

    public LemonadeGame(LemonadeStand stand, CustomerSource customers) {
        this.stand = stand;
        this.customers = customers;
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new NoSuchElementException("Input closed.");
        }
        return scanner.nextLine().trim();
    }

    private int readQuantity(String prompt, int minimum) {
        return readQuantity(prompt, minimum, Integer.MAX_VALUE);
    }

    private int readQuantity(String prompt, int minimum, int maximum) {
        while (true) {
            String input = readLine(prompt);
            try {
                int quantity = Integer.parseInt(input);
                if (quantity >= minimum && quantity <= maximum) {
                    return quantity;
                }
            } catch (NumberFormatException exception) {
                // keep the loop
            }
            System.out.printf("Enter a whole number from %d to %d.%n", minimum, maximum);
        }
    }

    private long readPrice(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (input.startsWith("$")) {
                input = input.substring(1);
            }
            try {
                BigDecimal dollars = new BigDecimal(input);
                BigDecimal maximum = BigDecimal.valueOf(LemonadeStand.MAX_PRICE_CENTS, 2);
                if (dollars.scale() <= 2 && dollars.compareTo(BigDecimal.ZERO) > 0
                        && dollars.compareTo(maximum) <= 0) {
                    long cents = dollars.movePointRight(2).longValueExact();
                    if (cents >= 1) {
                        return cents;
                    }
                }
            } catch (NumberFormatException | ArithmeticException exception) {
                // keep the loop
            }
            System.out.printf("Enter a dollar amount from $0.01 to %s with at most two decimal places.%n",
                    money(LemonadeStand.MAX_PRICE_CENTS));
        }
    }

    public static String money(long cents) {
        return "$" + BigDecimal.valueOf(cents, 2).toPlainString();
    }

    public void run() {
        System.out.println("Welcome to My Lemonade Stand!");
        try {
            for (int day = 1; ; day++) {
                stand.payRent();
                System.out.printf("%nDay %d: Paid %s in rent. Cash: %s.%n", day, money(LemonadeStand.DAILY_RENT_CENTS),
                        money(stand.cashCents()));
                if (stand.isBankrupt()) {
                    System.out.printf("Game over! You made it to day %d before going bankrupt!%n", day);
                    return;
                }
                if (day > 1) {
                    stand.lemonMarket().advanceDay();
                    stand.sugarMarket().advanceDay();
                }
                showMarketPrice(Ingredient.LEMON, stand.lemonMarket(), day);
                showMarketPrice(Ingredient.SUGAR, stand.sugarMarket(), day);
                System.out.printf("%nDay %d: Preparation Phase%n", day);
                boolean preparing = true;
                while (preparing) {
                    showStatus();
                    int choice = readQuantity("What would you like to do? ", 0, 5);
                    try {
                        switch (choice) {
                            case 0 -> { System.out.println("Thanks for playing!"); return; }
                            case 1 -> purchase();
                            case 2 -> tweakRecipe();
                            case 3 -> {
                                stand.setPrice(readPrice("Price per cup (8 cups per pitcher): $"));
                                System.out.printf("Each cup costs %s.%n", money(stand.priceCents()));
                            }
                            case 4 -> {
                                makePitchers();
                            }
                            case 5 -> { sell(day); preparing = false; }
                            default -> System.out.println("Choose an option from 0 to 5.");
                        }
                    } catch (IllegalArgumentException exception) {
                        System.out.println(exception.getMessage());
                    }
                }
                if (day == Integer.MAX_VALUE) {
                    System.out.println("You reached the maximum number of days. Thanks for playing!");
                    return;
                }
            }
        } catch (NoSuchElementException exception) {
            System.out.println("\nInput closed. Thanks for playing!");
        }
    }

    private void showStatus() {
        System.out.printf("%n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~%n");
        System.out.printf("Lemons: %d%n", stand.lemons());
        System.out.printf("Sugar (Cups): %d%n", stand.sugar());
        System.out.println("Water (Cups): Infinity");
        System.out.printf("Current Pitchers: %d%n", stand.pitchers());
        System.out.printf("Cash: %s%n", money(stand.cashCents()));
        System.out.printf("Recipe per pitcher: %d lemons, %d sugar, %s cups of water%n",
                stand.recipe().lemons(), stand.recipe().sugar(), stand.recipe().water());
        System.out.printf("Price per cup: %s%n", money(stand.priceCents()));
        System.out.printf("%n1. Purchase Ingredients%n2. Tweak the Recipe%n3. Set the Price%n"
                + "4. Make Pitchers of Lemonade%n5. Move Onto the Selling Phase%n0. Quit%n");
    }

    private void purchase() {
        System.out.printf("Lemons cost %s each. Sugar costs %s per cup.%n",
                money(stand.lemonMarket().priceCents()), money(stand.sugarMarket().priceCents()));
        while (true) {
            int lemons = readQuantity("How many lemons do you want to buy? ", 0);
            int sugar = readQuantity("How many cups of sugar do you want to buy? ", 0);
            try {
                stand.purchase(lemons, sugar);
                System.out.println("Transaction complete!");
                return;
            } catch (IllegalArgumentException exception) {
                System.out.println(exception.getMessage());
                System.out.println("Try another order, or enter 0 for both quantities to cancel.");
            }
        }
    }

    private void makePitchers() {
        while (true) {
            int count = readQuantity("How many pitchers do you want to make? (0 to cancel) ", 0);
            if (count == 0) {
                return;
            }
            try {
                stand.makePitchers(count);
                double water = count * stand.recipe().water();
                System.out.printf(
                        "Made %d pitchers using %d lemons, %d cups of sugar, and %s cups of water.%n",
                        count, count * stand.recipe().lemons(), count * stand.recipe().sugar(), water);
                return;
            } catch (IllegalArgumentException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    private void tweakRecipe() {
        System.out.println("A lemon adds 0.25 cups; sugar adds 0.5 cups per cup. Water tops up to 8 cups.");
        int lemons = readQuantity("Lemons per pitcher? ", 0);
        int sugar = readQuantity("Cups of sugar per pitcher? ", 0);
        int water = readQuantity("Cups of water per pitcher? ", 0);
        stand.setRecipe(new Recipe(lemons, sugar, water));
        System.out.println("Recipe set! This recipe applies to newly made pitchers.");
    }

    private void sell(int day) {
        SalesResult result = stand.sell(customers.customersToday());
        System.out.printf("%nDay %d: Selling Phase%n", day);
        System.out.printf("%d customers visited. You sold %d cups for %s.%n",
                result.customers(), result.cupsSold(), money(result.revenueCents()));
        showRecipeFeedback();
        System.out.printf("Customers without lemonade: %d.%n", result.customers() - result.cupsSold());
        System.out.printf("Discarded lemonade: %d pitchers + %d cups.%n",
                result.wastedCups() / LemonadeStand.CUPS_PER_PITCHER,
                result.wastedCups() % LemonadeStand.CUPS_PER_PITCHER);
        System.out.printf("Cash: %s%n", money(stand.cashCents()));
        System.out.println("Let's try again tomorrow!");
    }

    private void showRecipeFeedback() {
        double multiplier = stand.recipe().customerMultiplier();
        if (multiplier == 0.0) {
            System.out.println("No water! Nobody wants pure lemon juice. Customers: 0.0x.");
        } else if (multiplier == 0.1) {
            System.out.println("It's just water! Refreshing, but not lemonade. Customers: 0.1x.");
        } else if (multiplier == 0.5) {
            System.out.println("No sugar! Some customers enjoy lemon water. Customers: 0.5x.");
        } else if (multiplier == 1.5) {
            System.out.println("Perfect lemonade! Customers: 1.5x.");
        } else if (multiplier == 1.25) {
            System.out.println("Tasty lemonade! Your recipe is close to the ideal. Customers: 1.25x.");
        } else {
            System.out.println("Your recipe is ordinary. Try balancing sugar, lemon juice, and water. Customers: 1.0x.");
        }
    }

    private void showMarketPrice(Ingredient ingredient, IngredientMarket market, int day) {
        System.out.printf("Today's price of %s is %s per %s.",
                ingredient.displayName(), money(market.priceCents()), ingredient.unit());
        if (day == 1) {
            System.out.println(" This is the starting market price.");
        } else if (market.priceCents() == market.previousPriceCents()) {
            System.out.printf(" This is unchanged from yesterday's price of %s.%n",
                    money(market.previousPriceCents()));
        } else {
            String direction = market.priceCents() > market.previousPriceCents() ? "up" : "down";
            System.out.printf(" This is %s from yesterday's price of %s.%n",
                    direction, money(market.previousPriceCents()));
        }
    }
}

