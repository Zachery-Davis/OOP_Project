package com.mealmate;
public class Ingredient {
    private String name;
    private double quantity;
    private String unit;

    public Ingredient() {}

    public Ingredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void decreaseQuantity(double quantity) { this.quantity -= quantity; }
    
    public void combineIngredients(Ingredient ingredient) { 
        if(name.equalsIgnoreCase(ingredient.getName())){
            if(unit.equalsIgnoreCase(ingredient.getUnit())){
                this.quantity += ingredient.getQuantity();
            }
        }
    }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    @Override
    public String toString() {
        return name + " (" + quantity + " " + unit + ")";
    }
}
