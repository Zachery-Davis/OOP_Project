package com.mealmate;
import java.util.ArrayList;

public class Inventory {
    private ArrayList<Ingredient> storedIngredients;

    public Inventory() {
        this.storedIngredients = new ArrayList<>();
    }

    public void storeIngredient(Ingredient ingredient) {
        storedIngredients.add(ingredient);
    }

    public ArrayList<Ingredient> getStoredIngredients() { return storedIngredients; }
}
