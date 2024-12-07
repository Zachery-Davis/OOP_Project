package com.mealmate;

import java.util.List;

public class Recipe {
    private String name;
    private int cookingTime;
    private String steps;
    private List<Ingredient> ingredients;
    private Nutrition nutrition;

    // Default constructor (needed for JSON deserialization)
    public Recipe() {}

    public Recipe(String name, int cookingTime, String steps, List<Ingredient> ingredients, Nutrition nutrition) {
        this.name = name;
        this.cookingTime = cookingTime;
        this.steps = steps;
        this.ingredients = ingredients;
        this.nutrition = nutrition;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCookingTime() { return cookingTime; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }

    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }

    public Nutrition getNutrition() { return nutrition; }
    public void setNutrition(Nutrition nutrition) { this.nutrition = nutrition; }

    @Override
    public String toString() {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(name).append("\n")
               .append("Cooking Time: ").append(cookingTime).append(" minutes\n")
               .append("\nSteps:\n").append(steps).append("\n\n")
               .append("Ingredients:\n");
    
        for (Ingredient ingredient : ingredients) {
            details.append("- ").append(ingredient.getName()).append(": ")
                   .append(ingredient.getQuantity()).append(" ")
                   .append(ingredient.getUnit()).append("\n");
        }

        Nutrition nutrition = getNutrition();
        details.append("\nNutrition: \n").append(nutrition.getCalories()).append(" kcal\n")
                    .append(nutrition.getProtein()).append(" g protein\n")
                    .append(nutrition.getFat()).append(" g fat\n")
                    .append(nutrition.getCarbs()).append(" g carbs\n");

        return details.toString();
    } 
}
