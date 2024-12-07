package com.mealmate;

import javax.swing.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
public class CalendarGUI extends JFrame implements RecipeSelectionListener {
    private JLabel monthLabel;
    private JPanel calendarPanel;
    private LocalDate currentDate;
    private LocalDate selectedDate;
    private Meal selectedMeal;
    private Map<LocalDate, MealPlan> mealSchedule;
    private JPanel prevSelectedDayPanel;
    private JLabel prevSelectedMealPanel;
    private int mealPlanColorCounter;
    // private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    // private int screenWidth = screenSize.width;
    // private int screenHeight = screenSize.height;

    public CalendarGUI() {
        currentDate = LocalDate.now();
        selectedDate = null;
        prevSelectedDayPanel = null;
        prevSelectedMealPanel = null;
        selectedMeal = null;
        mealSchedule = new HashMap<>();
        mealPlanColorCounter = 0;

        // Load calendar when program starts
        mealSchedule = FileManagement.loadJsonFile("data", "calendar.json", new TypeReference<Map<LocalDate, MealPlan>>() {});

        setTitle("Meal Calendar");
        // Maximize the window
        // setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(800, 600);
        setUndecorated(false); // if true the window will have no borders or title bar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                FileManagement.saveToJson(mealSchedule, "data", "calendar.json");
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
        JButton viewRecipeButton = new JButton("View Meal");
        viewRecipeButton.addActionListener(e -> viewSelectedMeal());

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

        JButton createMealPlanButton = new JButton("Create Meal Plan");
        createMealPlanButton.addActionListener(e -> createMealPlan());

        JButton deleteMealPlanButton = new JButton("Delete Meal Plan");
        deleteMealPlanButton.addActionListener(e -> deleteMealPlan());

        JButton genGroceryButton = new JButton("Generate Grocery List");
        genGroceryButton.addActionListener(e -> genGroceryList());

        bottomPanel.add(viewRecipeButton);
        bottomPanel.add(addMealButton);
        bottomPanel.add(removeMealButton);
        bottomPanel.add(createMealPlanButton);
        bottomPanel.add(deleteMealPlanButton);
        bottomPanel.add(genGroceryButton);

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
    
    private void viewSelectedMeal() {
        if (selectedMeal != null) {
            JOptionPane.showMessageDialog(this, selectedMeal.toString(), "Meal Details", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a meal first.", "Missing Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private MealPlan createMealPlan() {
        LocalDate weekStart = selectedDate;

        String[] options = {"End Date", "Duration (days)"};
        int choice = JOptionPane.showOptionDialog(
                this, 
                "Select an option to define the meal plan end date.", 
                "Meal Plan Options", 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                options, 
                options[0]);

        LocalDate weekEnd = null;
        
        if (choice == 0) { // Date option
            String dateInput = JOptionPane.showInputDialog(this, "Enter the meal plan end date (yyyy-MM-dd):");
            if (dateInput != null && !dateInput.isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    weekEnd = LocalDate.parse(dateInput, formatter);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Please use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            } else {
                JOptionPane.showMessageDialog(this, "End date is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } else if (choice == 1) { // Duration option
            String durationInput = JOptionPane.showInputDialog(this, "Enter the meal plan duration in days:");
            if (durationInput != null && !durationInput.isEmpty()) {
                try {
                    int duration = Integer.parseInt(durationInput);
                    weekEnd = weekStart.plusDays(duration - 1);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid duration. Please enter a valid number of days.", "Error", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Duration is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        
        MealPlan mealPlan = mealSchedule.get(weekStart);

        if (mealPlan == null) {
            mealPlan = new MealPlan(weekStart, weekEnd);
            mealSchedule.put(weekStart, mealPlan);
        }

        updateCalendar(currentDate);
        return mealPlan;
    }

    private MealPlan findMealPlan(LocalDate date) {
        for (MealPlan mealPlan : mealSchedule.values()) {
            if ((date.isEqual(mealPlan.getStartDate()) || date.isAfter(mealPlan.getStartDate())) &&
                (date.isEqual(mealPlan.getEndDate()) || date.isBefore(mealPlan.getEndDate()))) {
                return mealPlan; 
            }
        }
        return null;
    }

    private void deleteMealPlan(){
        if(selectedDate != null){
            mealSchedule.remove(findMealPlan(selectedDate).getStartDate());
            updateCalendar(currentDate);
        }else{
            JOptionPane.showMessageDialog(this, "Please select a day with a meal plan first.", "Missing Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void genGroceryList() {
        MealPlan mealPlan = findMealPlan(selectedDate);
        
        if (mealPlan != null) {
            GroceryList list = mealPlan.getGroceryList();
            String groceryListText = list.toString();
            
            Object[] options = {"Return", "Save as PDF"};
            int choice = JOptionPane.showOptionDialog(this,
                    groceryListText, 
                    "Grocery List", 
                    JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.INFORMATION_MESSAGE,
                    null, 
                    options, 
                    options[0]);
    
            if (choice == 1) {
                saveGroceryListAsPDF(groceryListText);
                return;
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a day that has a meal plan.", "Missing Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveGroceryListAsPDF(String groceryListText) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Grocery List as PDF");
        fileChooser.setSelectedFile(new File("grocery_list.pdf"));
    
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
    
            if (!filePath.endsWith(".pdf")) {
                filePath += ".pdf";
            }
    
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);
    
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
    
                float yPosition = 750;
                contentStream.newLineAtOffset(25, yPosition);
    
                String[] lines = groceryListText.split("\n");
    
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
    
                JOptionPane.showMessageDialog(this, "Grocery List saved as PDF.", "Success", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("PDF Saved to: " + filePath);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateCalendar(LocalDate date) {
        currentDate = date;
        calendarPanel.removeAll(); // Clear existing panels | TODO optimize
        selectedMeal = null;
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
    
        // Add current months days
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
            calendarPanel.add(dayPanel);
        }
    
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    MealPlan current;
    private Color getMealPlanColor(LocalDate date, MealPlan plan) {
        if(current != plan){
            current = plan;
            mealPlanColorCounter++;
        }

        Color[] colors = {Color.RED, Color.GREEN, Color.LIGHT_GRAY, Color.ORANGE, Color.CYAN, Color.MAGENTA, Color.PINK, Color.YELLOW};
    
        Color mealPlanColor = colors[mealPlanColorCounter % colors.length];
        
        return mealPlanColor;
    }

    private JPanel createDayPanel(LocalDate date, int day) {
        JPanel dayPanel = new JPanel(new BorderLayout());
        dayPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        dayPanel.setBackground(Color.WHITE);
        dayPanel.setLayout(new BoxLayout(dayPanel, BoxLayout.Y_AXIS));

        String dayText = (day == 1) 
        ? date.getMonth().toString().substring(0, 3) + " " + day 
        : String.valueOf(day);
    
        JLabel dayLabel = new JLabel(dayText, SwingConstants.CENTER);
        dayLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dayLabel.setOpaque(true);

        MealPlan plan = findMealPlan(date);
        if(plan != null){
            Color mealPlanColor = getMealPlanColor(date, plan);
            dayLabel.setBackground(mealPlanColor);
        }else{
            dayLabel.setBackground(Color.WHITE);
        }
        
        dayPanel.add(dayLabel, BorderLayout.NORTH);

        if (mealSchedule != null) {
            for (MealPlan mealPlan : mealSchedule.values()) {
                if (mealPlan.getMeals().containsKey(date)) {
                    for (Meal meal : mealPlan.getMeals().get(date)) {
                        JLabel mealLabel = new JLabel(meal.getName(), SwingConstants.CENTER);
                        mealLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                        mealLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                        mealLabel.setVerticalAlignment(SwingConstants.TOP);
                        mealLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        mealLabel.setBackground(Color.GRAY);
                        mealLabel.setOpaque(false);
            
                        mealLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseClicked(java.awt.event.MouseEvent evt) {
                                selectDayPanel(dayPanel, date);
                                selectedMeal = meal;
                                
                                if (prevSelectedMealPanel != null) {
                                    prevSelectedMealPanel.setOpaque(false);
                                }
                                mealLabel.setOpaque(true);
                                prevSelectedMealPanel = mealLabel;
                            }
                        });
                        dayPanel.add(mealLabel);
                    }
                }
            }
        }
    
        // Add listener for selecting the whole day panel | TODO shouldn't be two per
        dayPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                selectDayPanel(dayPanel, date);
                if (prevSelectedMealPanel != null) {
                    prevSelectedMealPanel.setOpaque(false);
                }
                selectedMeal = null;
            }
        });
    
        return dayPanel;
    }
    
    private void selectDayPanel(JPanel panel, LocalDate date) {
        if (prevSelectedDayPanel != null) {
            prevSelectedDayPanel.setBackground(Color.WHITE);
        }
        panel.setBackground(Color.CYAN);
        prevSelectedDayPanel = panel;
        selectedDate = date;
    }

    @Override
    public void onRecipeSelected(Recipe recipe) {
        if (selectedDate != null) {
            String input = JOptionPane.showInputDialog(
                this,
                "Enter serving size for " + recipe.getName() + ":",
                "Serving Size",
                JOptionPane.PLAIN_MESSAGE
            );

            try {
                int servingSize = Integer.parseInt(input);
                if (servingSize <= 0) {
                    throw new NumberFormatException("Serving size must be positive.");
                }

                Meal meal = new Meal(recipe);
                meal.setServingSize(servingSize);

                addMealToDate(meal);
                updateCalendar(currentDate);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Invalid serving size. Please enter a positive integer.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Select a date first before choosing a recipe.",
                "Missing Selection",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    public void addMealToDate(Meal meal) {
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Select a date first.", "Missing Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        MealPlan mealPlan = findMealPlan(selectedDate);
        if(mealPlan == null){
            mealPlan = createMealPlan();
        }
    
        mealPlan.addMeal(selectedDate, meal);
    
        updateCalendar(currentDate);
    }

    private void removeMealFromSelectedDate() {
        if (selectedDate == null || selectedMeal == null) {
            JOptionPane.showMessageDialog(this, "Select a date and a meal first.", "Missing Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    
        LocalDate weekStart = selectedDate.with(java.time.DayOfWeek.SATURDAY);
    
        MealPlan mealPlan = mealSchedule.get(weekStart);
    
        if (mealPlan != null) {
            mealPlan.removeMeal(selectedDate, selectedMeal);
    
            if (mealPlan.getMeals().isEmpty()) {
                mealSchedule.remove(weekStart);
            }
    
            updateCalendar(currentDate);
        } else {
            JOptionPane.showMessageDialog(this, "Meal plan not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
}
