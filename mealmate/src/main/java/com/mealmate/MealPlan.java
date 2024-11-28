package com.mealmate;
import java.util.ArrayList;

public class MealPlan {
    private ArrayList<Recipe> recipes;

    public MealPlan() {
        this.recipes = new ArrayList<>();
    }

    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
    }

    public ArrayList<Recipe> getRecipes() { return recipes; }
}
