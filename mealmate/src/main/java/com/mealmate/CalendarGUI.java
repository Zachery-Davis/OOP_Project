package com.mealmate;

import javax.swing.*;

import java.awt.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class CalendarGUI extends JFrame implements RecipeSelectionListener {
    private JLabel monthLabel;
    private JPanel calendarPanel;
    private LocalDate currentDate;
    private LocalDate selectedDate;
    private Map<LocalDate, List<String>> mealSchedule;
    private HashMap<String, Recipe> recipeMap;
    private JPanel previouslySelectedPanel;
    private JLabel previouslySelectedMeal;
    private String selectedMealName;
    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private int screenWidth = screenSize.width;
    private int screenHeight = screenSize.height;

    public CalendarGUI() {
        currentDate = LocalDate.now();
        selectedDate = null;
        previouslySelectedPanel = null;
        previouslySelectedMeal = null;
        selectedMealName = null;
        mealSchedule = new HashMap<>();
        recipeMap = new HashMap<>();

        loadMealSchedule();
        loadRecipesFromFile();

        setTitle("Meal Calendar");
        // Maximize the window
        // setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(800, 600);
        setUndecorated(false); // if true the window will have no borders or title bar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveMealSchedule(); // Save data before exiting
                System.exit(0);
            }
        });
        setLayout(new BorderLayout());

        // Top panel for month navigation
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        monthLabel = new JLabel("", SwingConstants.CENTER);

        JPanel navPanel = new JPanel();
        navPanel.add(prevButton);
        navPanel.add(monthLabel);
        navPanel.add(nextButton);

        // Top panel for month navigation and days of week
        JPanel daysOfWeekPanel = new JPanel(new GridLayout(1, 7));
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 16));
            daysOfWeekPanel.add(dayLabel);
        }

        topPanel.add(navPanel);
        topPanel.add(daysOfWeekPanel);


        // Add Bottom Buttons
        JButton viewRecipeButton = new JButton("View Recipe");
        viewRecipeButton.addActionListener(e -> viewSelectedRecipe());

        JButton addMealButton = new JButton("Add/Modify Meal");
        addMealButton.addActionListener(e -> {
            if (selectedDate != null) {
                new RecipeGUI(this); // Open RecipeGUI to select a meal
            } else {
                JOptionPane.showMessageDialog(this, "Select a day first.", "Missing Selection", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton removeMealButton = new JButton("Remove Meal");
        removeMealButton.addActionListener(e -> removeMealFromSelectedDate());

        bottomPanel.add(viewRecipeButton);
        bottomPanel.add(addMealButton);
        bottomPanel.add(removeMealButton);

        // Set layouts for the panels that allow resizing of contents
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

        calendarPanel = new JPanel(new GridLayout(0, 7)); // 7 columns for days of the week

        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);
        add(calendarPanel, BorderLayout.CENTER);

        prevButton.addActionListener(e -> updateCalendar(currentDate.minusMonths(1)));
        nextButton.addActionListener(e -> updateCalendar(currentDate.plusMonths(1)));

        updateCalendar(currentDate);
        setVisible(true);
    }

    private void viewSelectedRecipe() {
        if (selectedMealName != null) {
            Recipe selectedRecipe = recipeMap.get(selectedMealName);
            if (selectedRecipe != null) {
                JOptionPane.showMessageDialog(this, formatRecipeDetails(selectedRecipe), "Recipe Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Recipe details not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a meal first.", "Missing Selection", JOptionPane.INFORMATION_MESSAGE);
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
    
    private String formatRecipeDetails(Recipe recipe) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(recipe.getName()).append("\n")
               .append("Cooking Time: ").append(recipe.getCookingTime()).append(" minutes\n")
               .append("Serving Size: ").append(recipe.getServingSize()).append("\n\n")
               .append("Steps:\n").append(recipe.getSteps()).append("\n\n")
               .append("Ingredients:\n");
    
        for (Ingredient ingredient : recipe.getIngredients()) {
            details.append("- ").append(ingredient.getName()).append(": ")
                   .append(ingredient.getQuantity()).append(" ")
                   .append(ingredient.getUnit()).append("\n");
        }
        return details.toString();
    }    

    private void removeMealFromSelectedDate() {
        if (selectedDate != null && selectedMealName != null) {
            if (mealSchedule.get(selectedDate).remove(selectedMealName)) {
                if (mealSchedule.get(selectedDate).isEmpty()) {
                    mealSchedule.remove(selectedDate); // Remove date if no meals remain
                }
                updateCalendar(currentDate); // Refresh calendar
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove meal. Please try again.", "Remove Failure", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a day and a meal to remove.", "Missing Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }    
    

    private void updateCalendar(LocalDate date) {
        currentDate = date;
        calendarPanel.removeAll(); // Clear existing panels
        selectedMealName = null;
        selectedDate = null;
    
        YearMonth yearMonth = YearMonth.of(currentDate.getYear(), currentDate.getMonth());
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Adjust for Sunday start
    
        monthLabel.setText(yearMonth.getMonth() + " " + yearMonth.getYear());
    
        // Add days from previous month
        LocalDate prevMonthStart = firstOfMonth.minusDays(dayOfWeek);
        for (int i = 0; i < dayOfWeek; i++) {
            LocalDate prevMonthDay = prevMonthStart.plusDays(i);
            JPanel dayPanel = createDayPanel(prevMonthDay, prevMonthDay.getDayOfMonth());
            calendarPanel.add(dayPanel);
        }
    
        // Add days with meal information
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDay = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), day);
            JPanel dayPanel = createDayPanel(currentDay, day);
            calendarPanel.add(dayPanel);
        }

        // Add days from next month to complete the grid
        int remainingCells = 35 - (dayOfWeek + daysInMonth); // 35 = 5 rows * 7 columns
        LocalDate nextMonthStart = firstOfMonth.plusMonths(1);
        for (int i = 0; i < remainingCells; i++) {
            LocalDate nextMonthDay = nextMonthStart.plusDays(i);
            JPanel dayPanel = createDayPanel(nextMonthDay, nextMonthDay.getDayOfMonth());
            dayPanel.setBackground(Color.LIGHT_GRAY); // Optional: distinguish next month days
            calendarPanel.add(dayPanel);
        }
    
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private JPanel createDayPanel(LocalDate date, int day) {
        JPanel dayPanel = new JPanel(new BorderLayout());
        dayPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        dayPanel.setBackground(Color.WHITE);
        dayPanel.setLayout(new BoxLayout(dayPanel, BoxLayout.Y_AXIS));

        String dayText = (day == 1) 
        ? date.getMonth().toString().substring(0, 3) + " " + day 
        : String.valueOf(day);
    
        // Day label
        JLabel dayLabel = new JLabel(dayText, SwingConstants.CENTER);
        dayLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        dayPanel.add(dayLabel, BorderLayout.NORTH);
    
        if (mealSchedule.containsKey(date)) {
            for (String meal : mealSchedule.get(date)) {
                JLabel mealLabel = new JLabel(meal, SwingConstants.CENTER);
                mealLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                mealLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                mealLabel.setVerticalAlignment(SwingConstants.TOP);
                mealLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                mealLabel.setBackground(Color.GRAY);
                mealLabel.setOpaque(false);
    
                // Add mouse listener to meal labels
                mealLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        selectDayPanel(dayPanel, date);
                        selectedMealName = meal;
                        
                        if (previouslySelectedMeal != null) {
                            previouslySelectedMeal.setOpaque(false);
                        }
                        mealLabel.setOpaque(true);
                        previouslySelectedMeal = mealLabel;
                    }
                });
    
                dayPanel.add(mealLabel);
            }
        }
    
        // Add listener for selecting the whole day panel
        dayPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                selectDayPanel(dayPanel, date);
                if (previouslySelectedMeal != null) {
                    previouslySelectedMeal.setOpaque(false);
                }
                selectedMealName = null;
            }
        });
    
        return dayPanel;
    }
    
    private void selectDayPanel(JPanel panel, LocalDate date) {
        if (previouslySelectedPanel != null) {
            previouslySelectedPanel.setBackground(Color.WHITE);
        }
        panel.setBackground(Color.CYAN);
        previouslySelectedPanel = panel;
        selectedDate = date;
    }
    
    @Override
    public void onRecipeSelected(Recipe recipe) {
        if (selectedDate != null) {
            addRecipeToDate(selectedDate, recipe.getName());
            updateCalendar(currentDate); // Refresh calendar to show the updated meals
        }
    }

    public void addRecipeToDate(LocalDate date, String recipe) {
        mealSchedule.putIfAbsent(date, new ArrayList<>()); // Initialize list if it doesn't exist
        mealSchedule.get(date).add(recipe); // Add the recipe to the list
    }

    private void saveMealSchedule() {
        try {
            File directory = new File("data");
            if (!directory.exists()) {
                directory.mkdirs();
            }
    
            File file = new File("data/calendar.json");
            Gson gson = new Gson().newBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter()) // Adapter for LocalDate
                .create();
    
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(mealSchedule, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving meal schedule.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadMealSchedule() {
        try {
            File file = new File("data/calendar.json");
            if (!file.exists()) {
                return;
            }
    
            Gson gson = new Gson().newBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter()) // Adapter for LocalDate
                .create();
    
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<Map<LocalDate, List<String>>>() {}.getType();
            try (Reader reader = new FileReader(file)) {
                mealSchedule = gson.fromJson(reader, type);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading meal schedule.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } 
}
