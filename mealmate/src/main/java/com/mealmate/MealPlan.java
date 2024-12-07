package com.mealmate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MealPlan {
    private HashMap<LocalDate, ArrayList<Meal>> meals;
    private ArrayList<LocalDate> cookingDates;
    private GroceryList groceryList;
    private LocalDate startDate;
    private LocalDate endDate;

    public MealPlan(){
        this.meals = new HashMap<>();
    }

    public MealPlan(LocalDate startDate, LocalDate endDate) {
        this.meals = new HashMap<>();
        this.cookingDates = new ArrayList<>();
        this.groceryList = new GroceryList();
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void addMeal(LocalDate date, Meal meal) {
        if(meals.get(date) != null){
            meals.get(date).add(meal);
        }else{
            meals.put(date, new ArrayList<>(Arrays.asList(meal)));
        }
        groceryList.addMeal(meal);
    }

    public void removeMeal(LocalDate date, Meal meal) {
        meals.get(date).remove(meal);
        groceryList.removeMeal(meal);
    }

    public void addCookingDate(LocalDate date) {
        if (!cookingDates.contains(date)) {
            cookingDates.add(date);
        }
    }

    public void removeCookingDate(LocalDate date) {
        cookingDates.remove(date);
    }

    public HashMap<LocalDate, ArrayList<Meal>> getMeals() {
        return meals;
    }

    public ArrayList<LocalDate> getCookingDates() {
        return cookingDates;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public GroceryList getGroceryList() {
        return groceryList;
    }
}
