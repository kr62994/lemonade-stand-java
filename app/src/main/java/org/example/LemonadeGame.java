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
        while (true) {
            String input = readLine(prompt);
            try {
                int quantity = Integer.parseInt(input);
                if (quantity >= minimum) {
                    return quantity;
                }
            } catch (NumberFormatException exception) {
                // keep the loop
            }
            System.out.printf("Enter a whole number of at least %d.%n", minimum);
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
                if (dollars.scale() <= 2) {
                    long cents = dollars.movePointRight(2).longValueExact();
                    if (cents >= 1) {
                        return cents;
                    }
                }
            } catch (NumberFormatException | ArithmeticException exception) {
                // keep the loop
            }
            System.out.println("Enter a positive dollar amount with at most two decimal places (for example, 1.50).");
        }
    }

    public static String money(long cents) {
        return "$" + BigDecimal.valueOf(cents, 2).toPlainString();
    }

    public void run() {
        System.out.println("Welcome to My Lemonade Stand!");
        try {
            for (int day = 1; ; day++) {
                System.out.printf("%nDay %d: Preparation Phase%n", day);
                boolean preparing = true;
                while (preparing) {
                    showStatus();
                    int choice = readQuantity("What would you like to do? ", 0);
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
                                int count = readQuantity("How many pitchers do you want to make? ", 1);
                                stand.makePitchers(count);
                                System.out.printf(
                                        "Made %d pitchers using %d lemons, %d cups of sugar, and %d cups of water.%n",
                                        count, count * stand.recipe().lemons(),
                                        count * stand.recipe().sugar(), count * stand.recipe().water());
                            }
                            case 5 -> { sell(day); preparing = false; }
                            default -> System.out.println("Choose an option from 0 to 5.");
                        }
                    } catch (IllegalArgumentException exception) {
                        System.out.println(exception.getMessage());
                    }
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
        System.out.printf("Recipe per pitcher: %d lemons, %d sugar, %d water%n",
                stand.recipe().lemons(), stand.recipe().sugar(), stand.recipe().water());
        System.out.printf("Price per cup: %s%n", money(stand.priceCents()));
        System.out.printf("%n1. Purchase Ingredients%n2. Tweak the Recipe%n3. Set the Price%n"
                + "4. Make Pitchers of Lemonade%n5. Move Onto the Selling Phase%n0. Quit%n");
    }

    private void purchase() {
        System.out.println("Lemons cost $1.00 each. Sugar costs $0.50 per cup.");
        int lemons = readQuantity("How many lemons do you want to buy? ", 0);
        int sugar = readQuantity("How many cups of sugar do you want to buy? ", 0);
        stand.purchase(lemons, sugar);
        System.out.println("Transaction complete!");
    }

    private void tweakRecipe() {
        int lemons = readQuantity("Lemons per pitcher? ", 1);
        int sugar = readQuantity("Cups of sugar per pitcher? ", 1);
        int water = readQuantity("Cups of water per pitcher? ", 1);
        stand.setRecipe(new Recipe(lemons, sugar, water));
        System.out.println("Recipe set! This recipe applies to newly made pitchers.");
    }

    private void sell(int day) {
        SalesResult result = stand.sell(customers.customersToday());
        System.out.printf("%nDay %d: Selling Phase%n", day);
        System.out.printf("%d customers visited. You sold %d cups for %s.%n",
                result.customers(), result.cupsSold(), money(result.revenueCents()));
        System.out.printf("Customers without lemonade: %d.%n", result.customers() - result.cupsSold());
        System.out.printf("Discarded lemonade: %d pitchers + %d cups.%n",
                result.wastedCups() / LemonadeStand.CUPS_PER_PITCHER,
                result.wastedCups() % LemonadeStand.CUPS_PER_PITCHER);
        System.out.printf("Cash: %s%n", money(stand.cashCents()));
        System.out.println("Let's try again tomorrow!");
    }
}

