package main;

public class UnavailableIngredientException extends Exception {
    public UnavailableIngredientException(Beverage beverage, Ingredient i) {
        super(beverage.getName() + " cannot be prepared because " + i.getName() + " is not available");
    }
}
