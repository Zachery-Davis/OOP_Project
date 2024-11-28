package com.mealmate;
import java.util.ArrayList;

public class GroceryList {
    private ArrayList<Ingredient> ingredients;

    public GroceryList() {
        this.ingredients = new ArrayList<>();
    }

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    public ArrayList<Ingredient> getIngredients() { return ingredients; }
}
