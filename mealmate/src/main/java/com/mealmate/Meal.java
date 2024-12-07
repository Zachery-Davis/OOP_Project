package com.mealmate;

public class Meal extends Recipe {
    private int servingSize;

    public Meal(){
        servingSize = 1;
    }

    public Meal(Recipe recipe){
        super(recipe.getName(), recipe.getCookingTime(), recipe.getSteps(), recipe.getIngredients(), recipe.getNutrition());
        servingSize = 1;
    }

    public int getServingSize() {
        return servingSize;
    }

    public void setServingSize(int servingSize) {
        this.servingSize = servingSize;
    }

    public void adjustForServing(int servingSize) {
        this.setCookingTime((int)(this.getCookingTime() * (1 + 0.5 * (servingSize - 1))));

        for(Ingredient ingredient : this.getIngredients()){
            ingredient.setQuantity(ingredient.getQuantity() * servingSize);
        }

        Nutrition nutrition = this.getNutrition();
        nutrition.setCalories(nutrition.getCalories() * servingSize);
        nutrition.setCarbs(nutrition.getCarbs() * servingSize);
        nutrition.setFat(nutrition.getFat() * servingSize);
        nutrition.setProtein(nutrition.getProtein() * servingSize);

    }
    
    @Override
    public String toString() {
        StringBuilder details = new StringBuilder();
        details.append(super.toString());

        details.insert((details.indexOf("Steps") - 1), "Serving Size: " + servingSize + "\n");

        return details.toString();
    } 
}
