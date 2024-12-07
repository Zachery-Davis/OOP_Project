package com.mealmate;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.fasterxml.jackson.core.type.TypeReference;

public class RecipeGUI {
    private JFrame frame;
    private HashMap<String, Recipe> recipeMap;
    private List<Recipe> defaultRecipes;
    private JList<String> recipeNameList;
    private DefaultListModel<String> recipeNamesListModel;
    private JTextArea recipeDetailsArea;
    private JPanel inputPanel;
    private ArrayList<Component> inputList;

    public RecipeGUI(RecipeSelectionListener listener) {
        frame = new JFrame("Meal Prepping System");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Load recipes when the program starts
        defaultRecipes = FileManagement.loadJsonFile("data", "defaultRecipes.json", new TypeReference<List<Recipe>>() {});
        recipeMap = FileManagement.loadJsonFile("data", "recipes.json", new TypeReference<HashMap<String, Recipe>>() {});

        // Window listener to save recipes on program exit
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveRecipes();
            }
        });

        // Setup Panels
        setupRecipeListPanel();
        setupSearchAndFilter();

        // Setup Recipe Details Panel
        recipeDetailsArea = new JTextArea();
        recipeDetailsArea.setEditable(false);
        frame.add(new JScrollPane(recipeDetailsArea), BorderLayout.CENTER);

        // Control Panel
        JPanel controlPanel = new JPanel();
        JButton selectRecipeButton = new JButton("Select Recipe");
        JButton addRecipeButton = new JButton("Add Recipe");
        JButton editRecipeButton = new JButton("Edit Recipe");
        JButton deleteRecipeButton = new JButton("Delete Recipe");

        selectRecipeButton.addActionListener(e -> selectRecipe(listener));
        addRecipeButton.addActionListener(e -> addRecipe());
        editRecipeButton.addActionListener(e -> editRecipe());
        deleteRecipeButton.addActionListener(e -> deleteRecipe());

        controlPanel.add(selectRecipeButton);
        controlPanel.add(addRecipeButton);
        controlPanel.add(editRecipeButton);
        controlPanel.add(deleteRecipeButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        constructInputPanel();
    }

    private void selectRecipe(RecipeSelectionListener listener){
        String selectedName = recipeNameList.getSelectedValue();
        if (selectedName != null) {
            Recipe selectedRecipe = recipeMap.get(selectedName);
            saveRecipes();
            frame.dispose();
            listener.onRecipeSelected(selectedRecipe);
        } else {
            JOptionPane.showMessageDialog(frame, "Select a meal first.");
        }
    }

    private void saveRecipes(){
        FileManagement.saveToJson(recipeMap, "data", "recipes.json");
    }

    private void addRecipe() {
        Recipe recipe = getRecipeFromUser();
        if (recipe != null) {
            String recipeName = recipe.getName();
            recipeMap.put(recipeName, recipe);
            recipeNamesListModel.addElement(recipeName);
        }
    }

    private void constructInputPanel(){
        inputPanel = new JPanel(new GridLayout(6, 2));
        inputList = new ArrayList<Component>();

        JComboBox<String> defaultRecipeDropdown = new JComboBox<>(defaultRecipes.stream().map(Recipe::getName).toArray(String[]::new));
        JTextField nameField = new JTextField();
        JTextField timeField = new JTextField();
        JTextArea stepsField = new JTextArea(5, 30);
        JTextArea ingredientArea = new JTextArea(5, 20);
        JTextArea nutritionArea = new JTextArea(4, 20);

        inputPanel.add(new JLabel("Choose Default Recipe:"));
        inputPanel.add(defaultRecipeDropdown);
        inputList.add(defaultRecipeDropdown);
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputList.add(nameField);
        inputPanel.add(new JLabel("Cooking Time (min):"));
        inputPanel.add(timeField);
        inputList.add(timeField);
        inputPanel.add(new JLabel("Steps:"));
        inputPanel.add(new JScrollPane(stepsField));
        inputList.add(stepsField);
        inputPanel.add(new JLabel("<html>Ingredients (Per Serving):<br>(\"name,quantity,unit\" per line)</html>"));
        inputPanel.add(new JScrollPane(ingredientArea));
        inputList.add(ingredientArea);
        inputPanel.add(new JLabel("<html>Calories (kCal):<br>Protein (g):<br>Fat (g):<br>Carbs (g):</html>"));
        inputPanel.add(new JScrollPane(nutritionArea));
        inputList.add(nutritionArea);
    }

    private void updateInputPanel(String[] str){
        for(int i = 1; i < inputList.size(); i++){
            Component component = inputList.get(i);
            if (component instanceof JTextField) {
                ((JTextField) component).setText(str[i - 1]);
            } else if (component instanceof JTextArea) {
                ((JTextArea) component).setText(str[i - 1]);
            }
        }
    }

    private void editRecipe() {
        String selectedName = recipeNameList.getSelectedValue();
        if (selectedName == null) {
            JOptionPane.showMessageDialog(frame, "Please select a recipe to edit.");
            return;
        }

        Recipe selectedRecipe = recipeMap.get(selectedName);

        String nameInput = selectedRecipe.getName();
        String timeInput = String.valueOf(selectedRecipe.getCookingTime());
        String stepsInput = selectedRecipe.getSteps();
        String ingredientInput = "";
        String nutritionInput = selectedRecipe.getNutrition().toString().trim();

        for (Ingredient ingredient : selectedRecipe.getIngredients()) {
            ingredientInput += ingredient.getName() + "," 
            + ingredient.getQuantity() + "," 
            + ingredient.getUnit() + "\n";
        }
        
        updateInputPanel(new String[]{nameInput, timeInput, stepsInput, ingredientInput, nutritionInput});
    
        int result = JOptionPane.showConfirmDialog(frame, inputPanel, "Edit Recipe", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                // Retrieve values from the input fields
                JTextField nameField = (JTextField)inputList.get(1);
                JTextField timeField = (JTextField)inputList.get(2);
                JTextArea stepsField = (JTextArea)inputList.get(3);
                JTextArea ingredientArea = (JTextArea)inputList.get(4);
                JTextArea nutritionArea = (JTextArea)inputList.get(5);
                nameInput = nameField.getText().trim();
                timeInput = timeField.getText().trim();
                stepsInput = stepsField.getText().trim();
                ingredientInput = ingredientArea.getText().trim();
                nutritionInput = nutritionArea.getText().trim();
                String[] nutritionValues = nutritionInput.split("\n");

                // Use the new validation method
                if (!validateRecipeInput(nameInput, timeInput, stepsInput, ingredientInput, nutritionValues)) {
                    // Validation failed, method will show error message
                    return;
                }

                Nutrition nutrition = null;
                try {
                    if (nutritionValues.length != 4) {
                        throw new IllegalArgumentException("Please enter exactly four integers: Calories, Protein, Fat, and Carbs, each on a new line.");
                    }
                
                    int calories = Integer.parseInt(nutritionValues[0].trim());
                    int protein = Integer.parseInt(nutritionValues[1].trim());
                    int fat = Integer.parseInt(nutritionValues[2].trim());
                    int carbs = Integer.parseInt(nutritionValues[3].trim());
                
                    nutrition = new Nutrition(calories, protein, fat, carbs);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Invalid input format. Please enter numeric values.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }

                List<Ingredient> ingredients = parseIngredients(ingredientInput);

                // Update the selected recipe with the new values
                selectedRecipe.setName(nameInput);
                selectedRecipe.setCookingTime(Integer.parseInt(timeInput));
                selectedRecipe.setSteps(stepsInput);
                selectedRecipe.setIngredients(ingredients);
                selectedRecipe.setNutrition(nutrition);

                if (selectedRecipe != null) {
                    // Logic for editing remains the same
                    recipeMap.put(selectedRecipe.getName(), selectedRecipe);
                    displayRecipeDetails(selectedRecipe);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, 
                    "Error processing recipe: " + e.getMessage(), 
                    "Input Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private List<Ingredient> parseIngredients(String ingredientInput) {
        return Arrays.stream(ingredientInput.split("\n"))
            .filter(line -> !line.trim().isEmpty())
            .map(line -> {
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Invalid ingredient format: " + line);
                }
                return new Ingredient(
                    parts[0].trim(), 
                    Double.parseDouble(parts[1].trim()), 
                    parts[2].trim()
                );
            })
            .collect(Collectors.toList());
    }
    
    private void deleteRecipe() {
        String selectedName = recipeNameList.getSelectedValue();
        if (selectedName != null) {
            recipeMap.remove(selectedName);
            recipeNamesListModel.removeElement(selectedName);
            recipeDetailsArea.setText("");
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a recipe to delete.");
        }
    }

    private Recipe getRecipeFromUser() {
        // Initialize variables to store user input across form reopenings
        String nameInput = "";
        String timeInput = "";
        String stepsInput = "";
        String ingredientInput = "";
        String nutritionInput = "";
    
        while (true) {
            // Pre-fill input fields with the stored values
            @SuppressWarnings("unchecked")
            JComboBox<String> defaultRecipeDropdown = (JComboBox<String>)inputList.get(0);
            JTextField nameField = (JTextField)inputList.get(1);
            JTextField timeField = (JTextField)inputList.get(2);
            JTextArea stepsField = (JTextArea)inputList.get(3);
            JTextArea ingredientArea = (JTextArea)inputList.get(4);
            JTextArea nutritionArea = (JTextArea)inputList.get(5);

            updateInputPanel(new String[]{nameInput, timeInput, stepsInput, ingredientInput, nutritionInput});

            // Auto-fill when a default recipe is selected
            defaultRecipeDropdown.addActionListener(e -> {
                int selectedIndex = defaultRecipeDropdown.getSelectedIndex();
                if (selectedIndex >= 0) {
                    Recipe selectedRecipe = defaultRecipes.get(selectedIndex);
                    nameField.setText(selectedRecipe.getName());
                    timeField.setText(String.valueOf(selectedRecipe.getCookingTime()));
                    stepsField.setText(selectedRecipe.getSteps());
                    StringBuilder ingredientText = new StringBuilder();
                    for (Ingredient ingredient : selectedRecipe.getIngredients()) {
                        ingredientText.append(ingredient.getName()).append(",").append(ingredient.getQuantity()).append(",").append(ingredient.getUnit()).append("\n");
                    }
                    ingredientArea.setText(ingredientText.toString().trim());
                    nutritionArea.setText(selectedRecipe.getNutrition().toString());
                }
            });
    
            int result = JOptionPane.showConfirmDialog(frame, inputPanel, "Recipe Details", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    // Retrieve values from the input fields
                    nameInput = nameField.getText().trim();
                    timeInput = timeField.getText().trim();
                    stepsInput = stepsField.getText().trim();
                    ingredientInput = ingredientArea.getText().trim();
                    nutritionInput = nutritionArea.getText().trim();
                    String[] nutritionValues = nutritionArea.getText().trim().split("\\n");

    
                    // Validate and parse the inputs
                    if (!validateRecipeInput(nameInput, timeInput, stepsInput, ingredientInput, nutritionValues)) {
                        // Validation failed, return null to keep the dialog open
                        continue;
                    }
    
                    Nutrition nutrition = null;
                    try {
                        if (nutritionValues.length != 4) {
                            throw new IllegalArgumentException("Please enter exactly four integers: Calories, Protein, Fat, and Carbs, each on a new line.");
                        }
                    
                        int calories = Integer.parseInt(nutritionValues[0].trim());
                        int protein = Integer.parseInt(nutritionValues[1].trim());
                        int fat = Integer.parseInt(nutritionValues[2].trim());
                        int carbs = Integer.parseInt(nutritionValues[3].trim());
                    
                        nutrition = new Nutrition(calories, protein, fat, carbs);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(frame, "Invalid input format. Please enter numeric values.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
    
                    List<Ingredient> ingredients = parseIngredients(ingredientInput);

                    // Check if a recipe with the same name already exists
                    if (recipeMap.containsKey(nameInput)) {
                        throw new IllegalArgumentException(nameInput + " already exists.");
                    }
    
                    return new Recipe(nameInput, Integer.parseInt(timeInput), stepsInput, ingredients, nutrition);
    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(frame, 
                        "Error processing recipe: " + e.getMessage(), 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // User pressed cancel, stop the loop and return null
                return null;
            }
        }
    }

    private void updateRecipeNameList() {
        recipeNamesListModel.clear();
        recipeMap.keySet().forEach(recipeNamesListModel::addElement);
    }
    

    private void setupRecipeListPanel() {
        // Create a JList with the recipe names from the HashMap
        recipeNamesListModel = new DefaultListModel<>();
        updateRecipeNameList(); // Populate the list with recipe names
        
        recipeNameList = new JList<>(recipeNamesListModel);
        recipeNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add a listener to synchronize selection and display details
        recipeNameList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedName = recipeNameList.getSelectedValue();
                if (selectedName != null) {
                    Recipe selectedRecipe = recipeMap.get(selectedName);
                    if (selectedRecipe != null) {
                        displayRecipeDetails(selectedRecipe);
                    }
                }
            }
        });
        
        // Add the JList to a JScrollPane and place it on the left side of the frame
        JScrollPane recipeNameListScrollPane = new JScrollPane(recipeNameList);
        frame.add(recipeNameListScrollPane, BorderLayout.WEST);
    }
    
    private void displayRecipeDetails(Recipe recipe) {
        if (recipe == null) {
            return;
        }
    
        StringBuilder recipeDetails = new StringBuilder();
        recipeDetails.append("Name: ").append(recipe.getName())
            .append("\nCooking Time: ").append(recipe.getCookingTime()).append(" minutes\n")
            .append("\nSteps: \n").append(recipe.getSteps())
            .append("\n\nIngredients: \n");
    
        for (Ingredient ingredient : recipe.getIngredients()) {
            recipeDetails.append(ingredient.getName())
                .append(", ").append(ingredient.getQuantity())
                .append(" ").append(ingredient.getUnit()).append("\n");
        }

        Nutrition nutrition = recipe.getNutrition();
        recipeDetails.append("\nNutrition: \n").append(nutrition.getCalories()).append(" kcal\n")
                    .append(nutrition.getProtein()).append(" g protein\n")
                    .append(nutrition.getFat()).append(" g fat\n")
                    .append(nutrition.getCarbs()).append(" g carbs\n");

    
        recipeDetailsArea.setText(recipeDetails.toString());
    }    

    private boolean validateRecipeInput(String name, String cookingTime, String steps, String ingredientInput, String[] nutritionValues) {
        try {
            // Validate required fields
            validateField(name, "Recipe name");
            validateNumericField(cookingTime, "Cooking time");
            
            // Parse ingredients to validate their format
            parseIngredients(ingredientInput);
            
            for (String value : nutritionValues) {
                if (!value.matches("\\d+")) {
                    throw new IllegalArgumentException("Each line must contain a valid numeric value.");
                }
            }
            
            
            return true;
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(frame, 
                e.getMessage(), 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void validateField(String value, String fieldName) {
        if ((value == null || value.trim().isEmpty())) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
    }
    
    private int validateNumericField(String value, String fieldName) {
        validateField(value, fieldName);
        int numericValue = Integer.parseInt(value.trim());
        if (numericValue <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive number.");
        }
        return numericValue;
    }

    private void setupSearchAndFilter() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField(20);
        JComboBox<String> filterCombo = new JComboBox<>(new String[]{
            "All Recipes", 
            "Quick Meals (under 30 min)", 
            "Large Serving Meals (6+ servings)"
        });
    
        // Create a document listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterRecipes(searchField, filterCombo); }
            public void removeUpdate(DocumentEvent e) { filterRecipes(searchField, filterCombo); }
            public void insertUpdate(DocumentEvent e) { filterRecipes(searchField, filterCombo); }
        });
    
        filterCombo.addActionListener(e -> filterRecipes(searchField, filterCombo));
    
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(filterCombo, BorderLayout.EAST);
    
        frame.add(searchPanel, BorderLayout.NORTH);
    }
    
    private void filterRecipes(JTextField searchField, JComboBox<String> filterCombo) {
        String searchText = searchField.getText().toLowerCase();
        String selectedFilter = (String) filterCombo.getSelectedItem();
        
        recipeNamesListModel.clear();
        
        // Iterate through the HashMap and filter recipes
        for (String recipeName : recipeMap.keySet()) {
            Recipe recipe = recipeMap.get(recipeName);
            if (recipe != null && isRecipeMatchingCriteria(recipe, searchText, selectedFilter)) {
                recipeNamesListModel.addElement(recipeName);
            }
        }
        
        // Handle case when no recipes match
        if (recipeNamesListModel.isEmpty()) {
            recipeDetailsArea.setText("No matching recipes found.");
        } else {
            recipeNameList.setSelectedIndex(0);
        }
    }
    
    
    private boolean isRecipeMatchingCriteria(Recipe recipe, String searchText, String filter) {
        boolean matchesSearch = recipe.getName().toLowerCase().contains(searchText);
        boolean matchesFilter;
        switch (filter) {
            case "Quick Meals (under 30 min)":
                matchesFilter = recipe.getCookingTime() <= 30;
                break;
            default:
                matchesFilter = true;
        }
        return matchesSearch && matchesFilter;
    }
}