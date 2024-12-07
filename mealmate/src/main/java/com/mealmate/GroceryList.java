package com.mealmate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

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

    public void saveToPDF(String filePath){
        try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);
    
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
    
                float yPosition = 750;
                contentStream.newLineAtOffset(25, yPosition);
    
                String[] lines = toString().split("\n");
    
                for (String line : lines) {
                    if (yPosition < 50) { 
                        contentStream.endText();
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                        contentStream.newLineAtOffset(25, 750);
                        yPosition = 750;
                    }
    
                    contentStream.showText(line);
                    yPosition -= 15; 
                    contentStream.newLineAtOffset(0, -15);
                }
    
                contentStream.endText();
                contentStream.close();
    
                document.save(filePath);
    
                JOptionPane.showMessageDialog(null, "Grocery List saved as PDF.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error saving PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
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
