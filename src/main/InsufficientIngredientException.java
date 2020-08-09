package main;

public class InsufficientIngredientException extends Exception{

    public InsufficientIngredientException(Beverage beverage, Ingredient i) {
        super(beverage.getName() + " cannot be prepared because " + i.getName() + " is not sufficient");
    }
}
