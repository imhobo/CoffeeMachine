package main;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Safal Pandita on 09/08/2020.
 */

public class CoffeeMachine {

    private final int numOfOutlets;
    private final Map<Ingredient, Integer> inventory;
    private final Map<String, Beverage> beverages;
    private final Map<Integer, Outlet> outlets;
    private final ExecutorService pool;

    /**
     * If an ingredient goes below this percentage, then we display a message.
     * e.g capacity = 1000, ingredientThresholdPercentage = 20%, so the actual threshold : 0.2 * 1000 = 200
     * Could also be part of ingredients in future if we want separate thresholds for every ingredient
     *
     * belowThresholdIngredients is Set of ingredients below the minimum threshold
     */
    private Integer ingredientThresholdPercentage;
    private Set<Ingredient> belowThresholdIngredients;

    public CoffeeMachine(int numOfOutlets, Map<Ingredient, Integer> inventory, Map<String, Beverage> beverages, Integer ingredientThresholdPercentage) {
        this.numOfOutlets = numOfOutlets;
        this.inventory = inventory;
        this.beverages = beverages;
        this.ingredientThresholdPercentage = ingredientThresholdPercentage;
        this.belowThresholdIngredients = new HashSet<>();
        this.outlets = new HashMap<>();
        for (int i = 1; i <= numOfOutlets; i++)
            outlets.put(i, new Outlet(i));
        this.pool = Executors.newFixedThreadPool(numOfOutlets);
    }

    public Map<Ingredient, Integer> getInventory() {
        return inventory;
    }

    /**
     * Takes input an OutletId and the beverage to be made. OutletId is needed to know the destination where the
     * beverage will be dispensed.
     *
     * Giving an order on an outlet that is busy throws an Error. It can also be changed to wait for the
     * existing order to finish in the future.
     *
     * We pick a thread from the pool and execute the task to dispense beverage.
     * @param outletId
     * @param beverageName
     */
    public void prepareBeverage(int outletId, String beverageName) {
        try {
            if (!beverages.containsKey(beverageName))
                throw new BeveragePreparationError(beverageName + " is not a valid beverage");
            Beverage beverage = beverages.get(beverageName);
            // TODO :: Catch child thread exceptions
            pool.execute(() -> dispenseBeverage(beverage, outletId));

        } catch (BeveragePreparationError e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Synchronized on Outlet and CoffeeMachine instances. First checks whether a beverage can be prepared using the
     * current inventory and then dispenses it.
     * Synchronized the Outlet instance because the status should not be modifiable by any other thread/method.
     * @param beverage
     * @param outletId
     */
    private synchronized void dispenseBeverage(Beverage beverage, int outletId) throws BeveragePreparationError {
        Outlet outlet = outlets.get(outletId);
        if (outlet == null) throw new BeveragePreparationError("Invalid outlet : " + outletId);

        synchronized (outlet) {

            if (outlet.getStatus() == Outlet.Status.BUSY) throw new BeveragePreparationError("Outlet " + outlet.getId()
                    + " is busy. Can't prepare " + beverage.getName());
            outlet.setStatus(Outlet.Status.BUSY);
            try {
                Map<Ingredient, Integer> recipe = beverage.getRecipe();

                //Check if quantity is sufficient for dispensing beverage
                for (Map.Entry<Ingredient, Integer> e : recipe.entrySet()) {
                    Ingredient i = e.getKey();
                    Integer quantity = e.getValue();
                    Integer totalQuantity = getInventory().get(i);
                    if (totalQuantity == null)
                        throw new UnavailableIngredientException(beverage, i);
                    if (quantity > totalQuantity)
                        throw new InsufficientIngredientException(beverage, i);
                }

                //Everything is valid about the order, just dispense
                for (Map.Entry<Ingredient, Integer> e : recipe.entrySet()) {
                    final Ingredient i = e.getKey();
                    Integer quantity = e.getValue();
                    Integer totalQuantity = getInventory().get(i);
                    Integer newQuantity = totalQuantity - quantity;
                    getInventory().put(i, newQuantity);
                    if(isBelowThreshold(i, newQuantity)) {
                        //Running low message will be displayed after the beverage is prepared.
                        belowThresholdIngredients.add(i);
                    }

                }
                outlet.setStatus(Outlet.Status.AVAILABLE);
                System.out.println(beverage.getName() + " is prepared on outlet : " + outlet.getId());
                for(Ingredient i : belowThresholdIngredients) {
                    System.out.println("Ingredient " + i.getName() + " is running low");
                }
            } catch (UnavailableIngredientException | InsufficientIngredientException e) {
                //Making the status AVAILABLE again since we set it as BUSY in the beginning
                outlet.setStatus(Outlet.Status.AVAILABLE);
                e.printStackTrace();
            } catch (Exception e) {
                //Making the status AVAILABLE again since we set it as BUSY in the beginning
                outlet.setStatus(Outlet.Status.AVAILABLE);
                e.printStackTrace();
            }
         }
    }

    /**
     * Method for restocking inventory.
     * If ingredient is present in inventory, then just adds more to it. Else adds it as a new ingredient
     * Also checks if the restocked ingredient is now not part of belowThresholdIngredients
     * @param ingredient
     * @param quantity
     */
    public void addIngredientToInventory(Ingredient ingredient, int quantity) {
        Integer currentStock = getInventory().get(ingredient);
        if(currentStock == null) getInventory().put(ingredient, quantity);
        else {
            Integer newQuantity = currentStock + quantity;
            getInventory().put(ingredient, newQuantity);
            //If this ingredient was part of below threshold ingredients but now it is not, then remove it from the set
            if(belowThresholdIngredients.contains(ingredient) && !isBelowThreshold(ingredient, newQuantity)) {
                belowThresholdIngredients.remove(ingredient);
            }
        }
    }

    public Boolean isBelowThreshold(Ingredient ingredient, Integer quantity) {
        Integer capacity = ingredient.getCapacity();
        Double threshold = (capacity * getIngredientThresholdPercentage()) / Double.valueOf(100);
        return quantity < threshold;
    }

    public Set<Ingredient> getBelowThresholdIngredients() {
        return belowThresholdIngredients;
    }


    public Integer getIngredientThresholdPercentage() {
        return ingredientThresholdPercentage;
    }

    public void setIngredientThresholdPercentage(Integer ingredientThresholdPercentage) {
        this.ingredientThresholdPercentage = ingredientThresholdPercentage;
    }

    /**
     * Gracefully waits for all threads to complete before shutting down
     */
    public void shutdown() {
        if(pool!=null) {
            pool.shutdown();
            try {
                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
