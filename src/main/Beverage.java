package main;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable class for different beverages. Assumed that 'name' is a unique identifier for a beverage
 */
final public class Beverage {
    private final String name;
    private final Map<Ingredient, Integer> recipe;
    // Can have other fields like Time to make a beverage in future. Currently assuming it is the same so not needed.

    public Beverage(String name, Map<Ingredient, Integer> recipe) {
        this.name = name;
        this.recipe = Collections.unmodifiableMap(recipe);
    }

    public String getName() {
        return name;
    }

    public Map<Ingredient, Integer> getRecipe() {
        return recipe;
    }
}
