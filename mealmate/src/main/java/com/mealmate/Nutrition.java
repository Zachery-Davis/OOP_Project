package com.mealmate;

public class Nutrition {
    private int calories;
    private int protein;
    private int fat;
    private int carbs;

    // Default constructor (needed for JSON deserialization)
    public Nutrition() {}

    public Nutrition (int calories, int protein, int fat, int carbs) {
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
    }

    // Getters and setters
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public int getProtein() { return protein; }
    public void setProtein(int protein) { this.protein = protein; }

    public int getFat() { return fat; }
    public void setFat(int fat) { this.fat = fat; }

    public int getCarbs() { return carbs; }
    public void setCarbs(int carbs) { this.carbs = carbs; }

    @Override
    public String toString() {
        return calories + "\n" + protein + "\n" + fat + "\n" + carbs;
    }
}
