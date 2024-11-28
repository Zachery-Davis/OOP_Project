package com.mealmate;
import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private String name;
    private int cookingTime;
    private String steps;
    private int servingSize;
    private List<Ingredient> ingredients;

    // Default constructor (needed for JSON deserialization)
    public Recipe() {}

    public Recipe(String name, int cookingTime, String steps, int servingSize, List<Ingredient> ingredients) {
        this.name = name;
        this.cookingTime = cookingTime;
        this.steps = steps;
        this.servingSize = servingSize;
        this.ingredients = ingredients;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCookingTime() { return cookingTime; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }

    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }

    public int getServingSize() { return servingSize; }
    public void setServingSize(int servingSize) { this.servingSize = servingSize; }

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }

    @Override
    public String toString() {
        return "Recipe: " + name + "\nCooking Time: " + cookingTime + " mins\nServing Size: " + servingSize + "\nIngredients: " + ingredients + "\nSteps: " + steps;
    }
}
