package main;

import java.util.Objects;

final public class Ingredient {

    /**
     * This could have been an enum as well depending on what the real world use case is. If we want to read Ingredients dynamically from a DB or file, then having it as an immutable class is more convenient.
     * It also has capacity field to check for a minimum threshold when it gets too low.
     **/
    private final String name;
    private final Integer capacity;

    public Ingredient(String name, Integer capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return name.equals(that.name) &&
                capacity.equals(that.capacity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, capacity);
    }
}
