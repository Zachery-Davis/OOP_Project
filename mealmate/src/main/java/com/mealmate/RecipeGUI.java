package com.mealmate;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import com.fasterxml.jackson.databind.ObjectMapper;

public class RecipeGUI {
    private JFrame frame;
    private HashMap<String, Recipe> recipeMap;
    private List<Recipe> defaultRecipes;
    private JList<String> recipeNameList;
    private DefaultListModel<String> recipeNamesListModel;
    private JTextArea recipeDetailsArea;

    public RecipeGUI(RecipeSelectionListener listener) {
        frame = new JFrame("Meal Prepping System");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Load recipes when the program starts
        recipeMap = new HashMap<>();
        loadRecipesFromFile();

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

        // Window listener to save recipes on program exit
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveRecipesToFile();
            }
        });

        frame.setVisible(true);
    }

    private void selectRecipe(RecipeSelectionListener listener){
        String selectedName = recipeNameList.getSelectedValue();
        if (selectedName != null) {
            Recipe selectedRecipe = recipeMap.get(selectedName);
            listener.onRecipeSelected(selectedRecipe);
            frame.dispose();
        } else {
            JOptionPane.showMessageDialog(frame, "Select a meal first.");
        }
    }

    private void addRecipe() {
        Recipe recipe = getRecipeFromUser();
        if (recipe != null) {
            String recipeName = recipe.getName();
            recipeMap.put(recipeName, recipe);
            recipeNamesListModel.addElement(recipeName);
        }
    }

    private void editRecipe() {
        String selectedName = recipeNameList.getSelectedValue();
        if (selectedName == null) {
            JOptionPane.showMessageDialog(frame, "Please select a recipe to edit.");
            return;
        }

        Recipe selectedRecipe = recipeMap.get(selectedName);
    
        // Pre-fill input fields with the selected recipe's details
        JTextField nameField = new JTextField(selectedRecipe.getName());
        JTextField timeField = new JTextField(String.valueOf(selectedRecipe.getCookingTime()));
        JTextArea stepsField = new JTextArea(selectedRecipe.getSteps(), 5, 20);
        JTextField servingsField = new JTextField(String.valueOf(selectedRecipe.getServingSize()));
        JTextArea ingredientArea = new JTextArea(5, 20);
        StringBuilder ingredientText = new StringBuilder();
        for (Ingredient ingredient : selectedRecipe.getIngredients()) {
            ingredientText.append(ingredient.getName()).append(",").append(ingredient.getQuantity()).append(",").append(ingredient.getUnit()).append("\n");
        }
        ingredientArea.setText(ingredientText.toString().trim());
    
        JPanel inputPanel = new JPanel(new GridLayout(0, 2));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Cooking Time (min):"));
        inputPanel.add(timeField);
        inputPanel.add(new JLabel("Steps:"));
        inputPanel.add(new JScrollPane(stepsField));
        inputPanel.add(new JLabel("Serving Size:"));
        inputPanel.add(servingsField);
        inputPanel.add(new JLabel("Ingredients (name,quantity,unit per line):"));
        inputPanel.add(new JScrollPane(ingredientArea));
    
        int result = JOptionPane.showConfirmDialog(frame, inputPanel, "Edit Recipe", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                // Retrieve values from the input fields
                String name = nameField.getText().trim();
                String timeInput = timeField.getText().trim();
                String stepsInput = stepsField.getText().trim();
                String servingsInput = servingsField.getText().trim();
                String ingredientInput = ingredientArea.getText().trim();

                // Use the new validation method
                if (!validateRecipeInput(name, timeInput, stepsInput, servingsInput, ingredientInput)) {
                    // Validation failed, method will show error message
                    return;
                }

                int cookingTime = Integer.parseInt(timeInput);
                int servingSize = Integer.parseInt(servingsInput);

                List<Ingredient> ingredients = parseIngredients(ingredientInput);

                // Update the selected recipe with the new values
                selectedRecipe.setName(name);
                selectedRecipe.setCookingTime(cookingTime);
                selectedRecipe.setSteps(stepsInput);
                selectedRecipe.setServingSize(servingSize);
                selectedRecipe.setIngredients(ingredients);

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
        return Arrays.stream(ingredientInput.split("\\n"))
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
        String servingsInput = "";
        String ingredientInput = "";

        loadDefaultRecipes();
    
        while (true) {
            // Pre-fill input fields with the stored values
            JComboBox<String> defaultRecipeDropdown = new JComboBox<>(defaultRecipes.stream().map(Recipe::getName).toArray(String[]::new));
            JTextField nameField = new JTextField(nameInput);
            JTextField timeField = new JTextField(timeInput);
            JTextArea stepsField = new JTextArea(stepsInput, 5, 20);
            JTextField servingsField = new JTextField(servingsInput);
            JTextArea ingredientArea = new JTextArea(ingredientInput, 5, 20);

            // Auto-fill when a default recipe is selected
            defaultRecipeDropdown.addActionListener(e -> {
                int selectedIndex = defaultRecipeDropdown.getSelectedIndex();
                if (selectedIndex >= 0) {
                    Recipe selectedRecipe = defaultRecipes.get(selectedIndex);
                    nameField.setText(selectedRecipe.getName());
                    timeField.setText(String.valueOf(selectedRecipe.getCookingTime()));
                    stepsField.setText(selectedRecipe.getSteps());
                    servingsField.setText(String.valueOf(selectedRecipe.getServingSize()));
                    StringBuilder ingredientText = new StringBuilder();
                    for (Ingredient ingredient : selectedRecipe.getIngredients()) {
                        ingredientText.append(ingredient.getName()).append(",").append(ingredient.getQuantity()).append(",").append(ingredient.getUnit()).append("\n");
                    }
                    ingredientArea.setText(ingredientText.toString().trim());
                }
            });
    
            JPanel inputPanel = new JPanel(new GridLayout(0, 2));
            inputPanel.add(new JLabel("Choose Default Recipe:"));
            inputPanel.add(defaultRecipeDropdown);
            inputPanel.add(new JLabel("Name:"));
            inputPanel.add(nameField);
            inputPanel.add(new JLabel("Cooking Time (min):"));
            inputPanel.add(timeField);
            inputPanel.add(new JLabel("Steps:"));
            inputPanel.add(new JScrollPane(stepsField));
            inputPanel.add(new JLabel("Serving Size:"));
            inputPanel.add(servingsField);
            inputPanel.add(new JLabel("Ingredients (name,quantity,unit per line):"));
            inputPanel.add(new JScrollPane(ingredientArea));
    
            int result = JOptionPane.showConfirmDialog(frame, inputPanel, "Recipe Details", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    // Retrieve values from the input fields
                    nameInput = nameField.getText().trim();
                    timeInput = timeField.getText().trim();
                    stepsInput = stepsField.getText().trim();
                    servingsInput = servingsField.getText().trim();
                    ingredientInput = ingredientArea.getText().trim();
    
                    // Validate and parse the inputs
                    if (!validateRecipeInput(nameInput, timeInput, stepsInput, servingsInput, ingredientInput)) {
                        // Validation failed, return null to keep the dialog open
                        continue;
                    }

                    // Check if a recipe with the same name already exists
                    if (recipeMap.containsKey(nameInput)) {
                        throw new IllegalArgumentException(nameInput + " already exists.");
                    }
    
                    int cookingTime = Integer.parseInt(timeInput);
                    int servingSize = Integer.parseInt(servingsInput);
    
                    List<Ingredient> ingredients = parseIngredients(ingredientInput);
    
                    return new Recipe(nameInput, cookingTime, stepsInput, servingSize, ingredients);
    
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

    private void saveRecipesToFile() {
        List<Recipe> data = new ArrayList<>(recipeMap.values());
        String filename = "data/recipes.json";
        try {
            ensureDataDirectoryExists();
            File file = new File(filename);

            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, data);

            // Create backup
            File backupFile = new File(filename.replace(".json", "_backup.json"));
            Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, 
                "Error saving to " + filename + ": " + e.getMessage(), 
                "Save Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private <T> List<T> loadJsonFile(String filename, TypeReference<List<T>> typeReference) {
        File file = new File(filename);
        File fileBackup = new File(filename.replace(".json", "_backup.json"));
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Check if the file exists, if not, replace it with the backup if available
            if (!file.exists()) {
                if (!fileBackup.exists()) {
                    System.out.println("No " + filename + " file or backup found.");
                    return Collections.emptyList();
                }
                Files.copy(fileBackup.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            // Attempt to read the primary file
            return mapper.readValue(file, typeReference);
        } catch (IOException e) {
            System.out.println("Error reading file: " + filename + ", attempting to use backup.");
            // Attempt to recover from backup
            if (fileBackup.exists()) {
                try {
                    Files.copy(fileBackup.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    return mapper.readValue(file, typeReference);
                } catch (IOException backupException) {
                    System.out.println("Error reading backup file: " + fileBackup.getName());
                }
            } else {
                System.out.println("Backup file not found or inaccessible.");
            }
        }
        // If both attempts fail, return an empty list
        return Collections.emptyList();
    }
    
    private void loadRecipesFromFile() {
        List<Recipe> recipes = loadJsonFile("data/recipes.json", new TypeReference<List<Recipe>>() {});
        for (Recipe recipe : recipes) {
            recipeMap.put(recipe.getName(), recipe);
        }
    }

    private void loadDefaultRecipes() {
        defaultRecipes = loadJsonFile("data/defaultRecipes.json", new TypeReference<List<Recipe>>() {});
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
        recipeDetails.append("Name: ").append(recipe.getName()).append("\n")
            .append("Cooking Time: ").append(recipe.getCookingTime()).append(" minutes\n")
            .append("Serving Size: ").append(recipe.getServingSize()).append("\n")
            .append("Steps: \n").append(recipe.getSteps()).append("\n")
            .append("Ingredients: \n");
    
        for (Ingredient ingredient : recipe.getIngredients()) {
            recipeDetails.append(ingredient.getName())
                .append(", ").append(ingredient.getQuantity())
                .append(" ").append(ingredient.getUnit()).append("\n");
        }
    
        recipeDetailsArea.setText(recipeDetails.toString());
    }    

    private boolean validateRecipeInput(String name, String cookingTime, String steps, String servingSize, String ingredientInput) {
        try {
            // Validate required fields
            validateField(name, "Recipe name");
            validateNumericField(cookingTime, "Cooking time");
            validateNumericField(servingSize, "Serving size");
            
            // Parse ingredients to validate their format
            parseIngredients(ingredientInput);
            
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

    private void ensureDataDirectoryExists() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            boolean dirCreated = dataDir.mkdirs();
            if (!dirCreated) {
                JOptionPane.showMessageDialog(frame, 
                    "Could not create data directory. Please check permissions.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
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
            case "Large Serving Meals (6+ servings)":
                matchesFilter = recipe.getServingSize() >= 6;
                break;
            default:
                matchesFilter = true;
        }
        return matchesSearch && matchesFilter;
    }
}