package org.example;

public class App {
    public static void main(String[] args) {
        LemonadeGame game = new LemonadeGame(new LemonadeStand(), new RandomCustomerSource());
        game.run();
    }
}
