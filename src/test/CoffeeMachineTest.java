package test;

import main.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CoffeeMachineTest {

    private CoffeeMachine machine;
    private Map<String, Ingredient> ingredientMap;
    private Map<String, Beverage> beverageMap;
    private static int numOfOutlets = 3;
    private static int minimumThresholdIngredient = 15;
    private static final String HOT_TEA = "hot_tea";
    private static final String HOT_COFFEE = "hot_coffee";
    private static final String BLACK_TEA = "black_tea";
    private static final String GREEN_TEA = "green_tea";

    private static final String HOT_WATER = "hot_water";
    private static final String HOT_MILK = "hot_milk";
    private static final String GINGER_SYRUP = "ginger_syrup";
    private static final String SUGAR_SYRUP = "sugar_syrup";
    private static final String TEA_LEAVES_SYRUP = "tea_leaves_syrup";
    private static final String GREEN_MIXTURE = "green_mixture";

    @BeforeEach
    void instantiateMachine() {
        Ingredient hotWater = new Ingredient(HOT_WATER, 1000);
        Ingredient hotMilk = new Ingredient(HOT_MILK, 1000);
        Ingredient gingerSyrup = new Ingredient(GINGER_SYRUP, 200);
        Ingredient sugarSyrup = new Ingredient(SUGAR_SYRUP, 200);
        Ingredient teaLeavesSyrup = new Ingredient(TEA_LEAVES_SYRUP, 200);
        Ingredient greenMixture = new Ingredient(GREEN_MIXTURE, 200);
        ingredientMap = new HashMap<>();
        ingredientMap.put(HOT_WATER, hotWater);
        ingredientMap.put(HOT_MILK, hotMilk);
        ingredientMap.put(GINGER_SYRUP, gingerSyrup);
        ingredientMap.put(SUGAR_SYRUP, sugarSyrup);
        ingredientMap.put(TEA_LEAVES_SYRUP, teaLeavesSyrup);
        ingredientMap.put(GREEN_MIXTURE, greenMixture);

        Beverage hotTea = new Beverage(HOT_TEA, Map.of(hotWater,200, hotMilk,100,
                gingerSyrup,10, sugarSyrup, 10, teaLeavesSyrup, 30));
        Beverage hotCoffee = new Beverage(HOT_COFFEE, Map.of(hotWater,100, hotMilk,400,
                gingerSyrup,30, sugarSyrup, 50, teaLeavesSyrup, 30));
        Beverage blackTea = new Beverage(BLACK_TEA, Map.of(hotWater,300, gingerSyrup,30,
                sugarSyrup, 50, teaLeavesSyrup, 30));
        Beverage greenTea = new Beverage(GREEN_TEA, Map.of(hotWater,100, gingerSyrup,30,
                sugarSyrup, 50, greenMixture, 30));
        beverageMap = Map.of(HOT_TEA, hotTea, HOT_COFFEE, hotCoffee, BLACK_TEA, blackTea, GREEN_TEA, greenTea);

        HashMap<Ingredient, Integer> inventory = new HashMap<>(Map.of(
                hotWater, 500,
                hotMilk, 500,
                gingerSyrup, 100,
                sugarSyrup, 100,
                teaLeavesSyrup, 100));

        this.machine = new CoffeeMachine(numOfOutlets, inventory, beverageMap, minimumThresholdIngredient);
    }

    @Test
    public void prepareSingleBeverage() throws Throwable {
        machine.prepareBeverage(1, HOT_COFFEE);
        machine.shutdown();
        assertEquals(machine.getInventory().get(ingredientMap.get(HOT_WATER)), 400);
        assertEquals(machine.getInventory().get(ingredientMap.get(HOT_MILK)), 100);
        assertEquals(machine.getInventory().get(ingredientMap.get(GINGER_SYRUP)), 70);
        assertEquals(machine.getInventory().get(ingredientMap.get(SUGAR_SYRUP)), 50);
        assertEquals(machine.getInventory().get(ingredientMap.get(TEA_LEAVES_SYRUP)), 70);
    }

    @Test
    public void prepareMultipleBeverages() throws Throwable {
        machine.addIngredientToInventory(ingredientMap.get(HOT_WATER), 600);
        machine.addIngredientToInventory(ingredientMap.get(HOT_MILK), 100);
        machine.addIngredientToInventory(ingredientMap.get(GINGER_SYRUP), 10);
        machine.addIngredientToInventory(ingredientMap.get(SUGAR_SYRUP), 70);
        machine.addIngredientToInventory(ingredientMap.get(TEA_LEAVES_SYRUP), 50);
        machine.prepareBeverage(1, HOT_TEA);
        machine.prepareBeverage(2, BLACK_TEA);
        machine.prepareBeverage(3, HOT_COFFEE);
        machine.prepareBeverage(3, HOT_TEA);
        machine.prepareBeverage(3, BLACK_TEA);
        machine.shutdown();
        assertEquals(machine.getInventory().get(ingredientMap.get(HOT_WATER)), 0);
        assertEquals(machine.getInventory().get(ingredientMap.get(HOT_MILK)), 0);
        assertEquals(machine.getInventory().get(ingredientMap.get(GINGER_SYRUP)), 0);
        assertEquals(machine.getInventory().get(ingredientMap.get(SUGAR_SYRUP)), 0);
        assertEquals(machine.getInventory().get(ingredientMap.get(TEA_LEAVES_SYRUP)), 0);
    }

    @Test
    public void handleBeveragePreparationError() {
        Assertions.assertThrows(BeveragePreparationError.class, () -> {
            machine.prepareBeverage(2, "random_beverage");
        });
        machine.shutdown();
    }

    @Test
    public void prepareBeverageWithoutIngredients() {
        machine.prepareBeverage(2, GREEN_TEA);
        machine.prepareBeverage(1, HOT_COFFEE);
        machine.prepareBeverage(3, BLACK_TEA);
        machine.prepareBeverage(3, HOT_COFFEE);
        machine.shutdown();
        assertEquals(machine.getInventory().get(ingredientMap.get(HOT_WATER)), 100);
        assertEquals(machine.getInventory().get(ingredientMap.get(HOT_MILK)), 100);
        assertEquals(machine.getInventory().get(ingredientMap.get(GINGER_SYRUP)), 40);
        assertEquals(machine.getInventory().get(ingredientMap.get(SUGAR_SYRUP)), 0);
        assertEquals(machine.getInventory().get(ingredientMap.get(TEA_LEAVES_SYRUP)), 40);
        machine.setIngredientThresholdPercentage(12);
        assertEquals(machine.getBelowThresholdIngredients().size(), 3);
    }
}