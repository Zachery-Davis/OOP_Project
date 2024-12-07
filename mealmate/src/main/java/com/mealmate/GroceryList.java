package com.mealmate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroceryList {
    private ArrayList<Ingredient> ingredients;
    HashMap<String, Double> combinedMap;

    public GroceryList() {
        this.ingredients = new ArrayList<Ingredient>();
    }

    public ArrayList<Ingredient> getIngredients() { return ingredients; }

    public void addMeal(Meal meal) {
        List<Ingredient> ingredientList = meal.getIngredients();

        if(ingredientList == null){
            return;
        }
        
        for(Ingredient ingredient : ingredientList){
            ingredients.add(ingredient);
        }
    }

    public void removeMeal(Meal meal) {
        List<Ingredient> ingredientList = meal.getIngredients();
        for(Ingredient ingredient : ingredientList){
            int index = findIngredientIndex(ingredient);
            Ingredient ing = ingredients.get(index);
            if(ing.getQuantity() < ingredient.getQuantity()){
                ingredients.remove(index);
            }else{
                ing.decreaseQuantity(ingredient.getQuantity());
            }
        }
    }

    private int findIngredientIndex(Ingredient ingredient){
        ingredients.sort(Comparator.comparing(Ingredient::getName));
        int index = Collections.binarySearch(ingredients, ingredient, Comparator.comparing(Ingredient::getName));
        return (index >= 0) ? index : null;
    }

    private HashMap<String, Double> combine(){
        combinedMap = new HashMap<>();
        combinedMap.clear();
        
        for (Ingredient ingredient : ingredients) {
            String name = ingredient.getName();
            if(combinedMap.containsKey(name)){
                combinedMap.replace(name, (combinedMap.get(name) + ingredient.getQuantity()));
            }else{
                combinedMap.put(name, ingredient.getQuantity());
            }
        }

        return combinedMap;
    }

    @Override
    public String toString(){
        String str = "";

        for(Map.Entry<String, Double> entry : combine().entrySet()){
            str += entry.getKey() + ": " + entry.getValue() + "\n";
        }

        return str;
    }
}
