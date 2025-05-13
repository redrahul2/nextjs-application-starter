package com.recipes.app;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Arrays;
import java.util.Optional;
import javafx.print.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.awt.Desktop;

public class RecipeController implements Initializable {
    @FXML private TableView<Recipe> recipeTable;
    @FXML private TableColumn<Recipe, String> favoriteColumn;
    @FXML private TableColumn<Recipe, String> titleColumn;
    @FXML private TableColumn<Recipe, String> categoryColumn;
    @FXML private TableColumn<Recipe, String> cookingTimeColumn;
    @FXML private TableColumn<Recipe, String> difficultyColumn;
    @FXML private TableColumn<Recipe, Integer> servingsColumn;
    @FXML private TableColumn<Recipe, String> restrictionsColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> difficultyComboBox;
    @FXML private CheckBox veganCheckBox;
    @FXML private CheckBox vegetarianCheckBox;
    @FXML private CheckBox glutenFreeCheckBox;
    @FXML private CheckBox dairyFreeCheckBox;
    @FXML private CheckBox favoritesOnlyCheckBox;
    @FXML private ToggleButton toggleViewButton;
    
    @FXML private VBox tableViewContainer;
    @FXML private ScrollPane cardViewContainer;
    @FXML private FlowPane recipeCardContainer;

    private final RecipeManager recipeManager = new RecipeManager();
    private final RecipeFilter filter = new RecipeFilter();

    private static final ObservableList<String> CATEGORIES = FXCollections.observableArrayList(
        "Breakfast", "Lunch", "Dinner", "Dessert", "Snack", "Appetizer"
    );

    private static final ObservableList<String> DIFFICULTIES = FXCollections.observableArrayList(
        "Easy", "Medium", "Hard"
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupComboBoxes();
        setupSearchField();
        setupToggleView();
        loadSampleData();
        setupEventHandlers();
    }

    private void setupTableColumns() {
        favoriteColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isFavorite() ? "★" : "☆"));
        favoriteColumn.setCellFactory(col -> new TableCell<Recipe, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item);
                    setOnMouseClicked(event -> {
                        Recipe recipe = getTableView().getItems().get(getIndex());
                        recipeManager.toggleFavorite(recipe.getId());
                        getTableView().refresh();
                        updateCardView();
                    });
                }
            }
        });

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        cookingTimeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFormattedCookingTime()));
        difficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        servingsColumn.setCellValueFactory(new PropertyValueFactory<>("servings"));
        restrictionsColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.join(", ", cellData.getValue().getDietaryRestrictions())));

        recipeTable.setItems(recipeManager.getFilteredRecipes());
    }

    private void setupComboBoxes() {
        categoryComboBox.setItems(CATEGORIES);
        difficultyComboBox.setItems(DIFFICULTIES);
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filter.setSearchText(newValue);
            applyFilters();
        });
    }

    private void setupToggleView() {
        toggleViewButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            tableViewContainer.setVisible(!newVal);
            cardViewContainer.setVisible(newVal);
            toggleViewButton.setText(newVal ? "Switch to Table" : "Switch to Cards");
            if (newVal) {
                updateCardView();
            }
        });
    }

    private void setupEventHandlers() {
        favoritesOnlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            filter.setShowFavoritesOnly(newVal);
            applyFilters();
        });
    }

    private void updateCardView() {
        recipeCardContainer.getChildren().clear();
        for (Recipe recipe : recipeManager.getFilteredRecipes()) {
            recipeCardContainer.getChildren().add(createRecipeCard(recipe));
        }
    }

    private VBox createRecipeCard(Recipe recipe) {
        VBox card = new VBox(10);
        card.getStyleClass().add("recipe-card");

        // Recipe image
        if (recipe.getImageUrl() != null) {
            try {
                ImageView imageView = new ImageView(new Image(recipe.getImageUrl()));
                imageView.setFitWidth(300);
                imageView.setFitHeight(200);
                imageView.setPreserveRatio(true);
                card.getChildren().add(imageView);
            } catch (Exception e) {
                // Handle missing image
            }
        }

        // Title with favorite star
        HBox titleBox = new HBox(10);
        Label titleLabel = new Label(recipe.getTitle());
        titleLabel.getStyleClass().add("title");
        Label favoriteLabel = new Label(recipe.isFavorite() ? "★" : "☆");
        favoriteLabel.setOnMouseClicked(e -> {
            recipeManager.toggleFavorite(recipe.getId());
            updateCardView();
        });
        titleBox.getChildren().addAll(titleLabel, favoriteLabel);
        
        // Details
        VBox details = new VBox(5);
        details.getStyleClass().add("details");
        details.getChildren().addAll(
            new Label("Category: " + recipe.getCategory()),
            new Label("Time: " + recipe.getFormattedCookingTime()),
            new Label("Difficulty: " + recipe.getDifficulty()),
            new Label("Servings: " + recipe.getServings()),
            new Label("Dietary: " + String.join(", ", recipe.getDietaryRestrictions()))
        );

        card.getChildren().addAll(titleBox, details);
        return card;
    }

    @FXML
    private void handleFilter() {
        filter.getDietaryRestrictions().clear();
        if (veganCheckBox.isSelected()) filter.getDietaryRestrictions().add("Vegan");
        if (vegetarianCheckBox.isSelected()) filter.getDietaryRestrictions().add("Vegetarian");
        if (glutenFreeCheckBox.isSelected()) filter.getDietaryRestrictions().add("Gluten-Free");
        if (dairyFreeCheckBox.isSelected()) filter.getDietaryRestrictions().add("Dairy-Free");

        filter.setCategory(categoryComboBox.getValue());
        filter.setDifficulty(difficultyComboBox.getValue());
        
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        categoryComboBox.setValue(null);
        difficultyComboBox.setValue(null);
        veganCheckBox.setSelected(false);
        vegetarianCheckBox.setSelected(false);
        glutenFreeCheckBox.setSelected(false);
        dairyFreeCheckBox.setSelected(false);
        favoritesOnlyCheckBox.setSelected(false);
        
        filter.getDietaryRestrictions().clear();
        filter.setCategory(null);
        filter.setDifficulty(null);
        filter.setSearchText(null);
        filter.setShowFavoritesOnly(false);
        
        applyFilters();
    }

    private void applyFilters() {
        recipeManager.setFilter(filter);
        if (toggleViewButton.isSelected()) {
            updateCardView();
        }
    }

    @FXML
    private void handleAddRecipe() {
        Dialog<Recipe> dialog = createRecipeDialog("Add New Recipe", null);
        dialog.showAndWait().ifPresent(recipe -> {
            recipeManager.addRecipe(recipe);
            if (toggleViewButton.isSelected()) {
                updateCardView();
            }
        });
    }

    @FXML
    private void handleEditRecipe() {
        Recipe selectedRecipe = recipeTable.getSelectionModel().getSelectedItem();
        if (selectedRecipe == null) {
            showAlert("No Recipe Selected", "Please select a recipe to edit.");
            return;
        }

        Dialog<Recipe> dialog = createRecipeDialog("Edit Recipe", selectedRecipe);
        dialog.showAndWait().ifPresent(recipe -> {
            recipeManager.updateRecipe(recipe);
            if (toggleViewButton.isSelected()) {
                updateCardView();
            }
        });
    }

    @FXML
    private void handleDeleteRecipe() {
        Recipe selectedRecipe = recipeTable.getSelectionModel().getSelectedItem();
        if (selectedRecipe == null) {
            showAlert("No Recipe Selected", "Please select a recipe to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Recipe");
        alert.setHeaderText("Delete " + selectedRecipe.getTitle());
        alert.setContentText("Are you sure you want to delete this recipe?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                recipeManager.deleteRecipe(selectedRecipe.getId());
                if (toggleViewButton.isSelected()) {
                    updateCardView();
                }
            }
        });
    }

    @FXML
    private void handleShareRecipe() {
        Recipe selectedRecipe = recipeTable.getSelectionModel().getSelectedItem();
        if (selectedRecipe == null) {
            showAlert("No Recipe Selected", "Please select a recipe to share.");
            return;
        }

        // Create sharing options dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Share Recipe");
        dialog.setHeaderText("Share " + selectedRecipe.getTitle());

        ButtonType emailButton = new ButtonType("Email", ButtonBar.ButtonData.OK_DONE);
        ButtonType copyButton = new ButtonType("Copy to Clipboard", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(emailButton, copyButton, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == emailButton) {
                return "EMAIL";
            }
            if (dialogButton == copyButton) {
                return "COPY";
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(action -> {
            String recipeText = formatRecipeForSharing(selectedRecipe);
            if (action.equals("EMAIL")) {
                // Implement email sharing
                showAlert("Share Recipe", "Email sharing feature coming soon!");
            } else if (action.equals("COPY")) {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(recipeText);
                clipboard.setContent(content);
                showAlert("Success", "Recipe copied to clipboard!");
            }
        });
    }

    @FXML
    private void handlePrintRecipe() {
        Recipe selectedRecipe = recipeTable.getSelectionModel().getSelectedItem();
        if (selectedRecipe == null) {
            showAlert("No Recipe Selected", "Please select a recipe to print.");
            return;
        }

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            VBox printContent = createPrintableRecipe(selectedRecipe);
            boolean success = job.printPage(printContent);
            if (success) {
                job.endJob();
                showAlert("Success", "Recipe sent to printer!");
            }
        }
    }

    private String formatRecipeForSharing(Recipe recipe) {
        StringBuilder sb = new StringBuilder();
        sb.append("Recipe: ").append(recipe.getTitle()).append("\n\n");
        sb.append("Category: ").append(recipe.getCategory()).append("\n");
        sb.append("Cooking Time: ").append(recipe.getFormattedCookingTime()).append("\n");
        sb.append("Difficulty: ").append(recipe.getDifficulty()).append("\n");
        sb.append("Servings: ").append(recipe.getServings()).append("\n");
        sb.append("Dietary Restrictions: ").append(String.join(", ", recipe.getDietaryRestrictions())).append("\n\n");
        sb.append("Ingredients:\n").append(recipe.getIngredients()).append("\n\n");
        sb.append("Instructions:\n").append(recipe.getInstructions());
        return sb.toString();
    }

    private VBox createPrintableRecipe(Recipe recipe) {
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label title = new Label(recipe.getTitle());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        content.getChildren().addAll(
            title,
            new Label("Category: " + recipe.getCategory()),
            new Label("Cooking Time: " + recipe.getFormattedCookingTime()),
            new Label("Difficulty: " + recipe.getDifficulty()),
            new Label("Servings: " + recipe.getServings()),
            new Label("Dietary Restrictions: " + String.join(", ", recipe.getDietaryRestrictions())),
            new Label("\nIngredients:"),
            new Label(recipe.getIngredients()),
            new Label("\nInstructions:"),
            new Label(recipe.getInstructions())
        );

        return content;
    }

    private Dialog<Recipe> createRecipeDialog(String title, Recipe recipe) {
        Dialog<Recipe> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Recipe Title");
        
        ComboBox<String> categoryField = new ComboBox<>(CATEGORIES);
        categoryField.setPromptText("Select Category");
        
        TextField cookingTimeField = new TextField();
        cookingTimeField.setPromptText("Cooking Time (minutes)");
        
        ComboBox<String> difficultyField = new ComboBox<>(DIFFICULTIES);
        difficultyField.setPromptText("Select Difficulty");
        
        TextField servingsField = new TextField();
        servingsField.setPromptText("Number of Servings");
        
        TextArea ingredientsArea = new TextArea();
        ingredientsArea.setPromptText("Ingredients (one per line)");
        
        TextArea instructionsArea = new TextArea();
        instructionsArea.setPromptText("Instructions");
        
        TextField imageUrlField = new TextField();
        imageUrlField.setPromptText("Image URL");

        CheckBox veganCheck = new CheckBox("Vegan");
        CheckBox vegetarianCheck = new CheckBox("Vegetarian");
        CheckBox glutenFreeCheck = new CheckBox("Gluten-Free");
        CheckBox dairyFreeCheck = new CheckBox("Dairy-Free");

        if (recipe != null) {
            titleField.setText(recipe.getTitle());
            categoryField.setValue(recipe.getCategory());
            cookingTimeField.setText(String.valueOf(recipe.getCookingTimeMinutes()));
            difficultyField.setValue(recipe.getDifficulty());
            servingsField.setText(String.valueOf(recipe.getServings()));
            ingredientsArea.setText(recipe.getIngredients());
            instructionsArea.setText(recipe.getInstructions());
            imageUrlField.setText(recipe.getImageUrl());
            
            veganCheck.setSelected(recipe.getDietaryRestrictions().contains("Vegan"));
            vegetarianCheck.setSelected(recipe.getDietaryRestrictions().contains("Vegetarian"));
            glutenFreeCheck.setSelected(recipe.getDietaryRestrictions().contains("Gluten-Free"));
            dairyFreeCheck.setSelected(recipe.getDietaryRestrictions().contains("Dairy-Free"));
        }

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(new Label("Cooking Time:"), 0, 2);
        grid.add(cookingTimeField, 1, 2);
        grid.add(new Label("Difficulty:"), 0, 3);
        grid.add(difficultyField, 1, 3);
        grid.add(new Label("Servings:"), 0, 4);
        grid.add(servingsField, 1, 4);
        grid.add(new Label("Image URL:"), 0, 5);
        grid.add(imageUrlField, 1, 5);
        grid.add(new Label("Ingredients:"), 0, 6);
        grid.add(ingredientsArea, 1, 6);
        grid.add(new Label("Instructions:"), 0, 7);
        grid.add(instructionsArea, 1, 7);
        
        VBox restrictionsBox = new VBox(10);
        restrictionsBox.getChildren().addAll(
            new Label("Dietary Restrictions:"),
            veganCheck, vegetarianCheck, glutenFreeCheck, dairyFreeCheck
        );
        grid.add(restrictionsBox, 1, 8);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Recipe newRecipe = recipe != null ? recipe : new Recipe();
                    newRecipe.setTitle(titleField.getText());
                    newRecipe.setCategory(categoryField.getValue());
                    newRecipe.setCookingTimeMinutes(Integer.parseInt(cookingTimeField.getText()));
                    newRecipe.setDifficulty(difficultyField.getValue());
                    newRecipe.setServings(Integer.parseInt(servingsField.getText()));
                    newRecipe.setIngredients(ingredientsArea.getText());
                    newRecipe.setInstructions(instructionsArea.getText());
                    newRecipe.setImageUrl(imageUrlField.getText());
                    
                    newRecipe.getDietaryRestrictions().clear();
                    if (veganCheck.isSelected()) newRecipe.addDietaryRestriction("Vegan");
                    if (vegetarianCheck.isSelected()) newRecipe.addDietaryRestriction("Vegetarian");
                    if (glutenFreeCheck.isSelected()) newRecipe.addDietaryRestriction("Gluten-Free");
                    if (dairyFreeCheck.isSelected()) newRecipe.addDietaryRestriction("Dairy-Free");
                    
                    return newRecipe;
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter valid numbers for cooking time and servings.");
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private void loadSampleData() {
        recipeManager.addSampleRecipes();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
