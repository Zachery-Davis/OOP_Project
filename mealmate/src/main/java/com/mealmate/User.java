package com.mealmate;
import java.util.ArrayList;

public class User {
    private String name;
    private ArrayList<Recipe> recipes;
    private ArrayList<MealPlan> mealPlans;

    public User(String name) {
        this.name = name;
        this.recipes = new ArrayList<>();
        this.mealPlans = new ArrayList<>();
    }

    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
    }

    public void addMealPlan(MealPlan mealPlan) {
        mealPlans.add(mealPlan);
    }

    public String getName() { return name; }
    public ArrayList<Recipe> getRecipes() { return recipes; }
    public ArrayList<MealPlan> getMealPlans() { return mealPlans; }
}
