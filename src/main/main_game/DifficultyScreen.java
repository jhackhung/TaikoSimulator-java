package main.main_game;

import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class DifficultyScreen extends StackPane {

    private int selected = 0;
    private final String[] levels = {"Easy", "Normal", "Hard", "Oni"};
    private final Color[] difficultyColors = {
            Color.GREEN,      // Easy - Green
            Color.ORANGE,     // Normal - Orange
            Color.RED,        // Hard - Red
            Color.PURPLE      // Oni - Purple
    };
    private final Label[] levelLabels = new Label[levels.length];
    private final String fontPath = "file:assets/fonts/Taiko_No_Tatsujin_Official_Font.ttf";

    public DifficultyScreen(MainController controller) {
        // Title
        Label titleLabel = createOutlinedLabel("Select Difficulty", 46, Color.rgb(229, 109, 50), Color.WHITE);

        // Background
        ImageView bg = new ImageView(new Image("file:assets/main/menu_bg.jpg"));
        bg.setFitWidth(controller.screenWidth);
        bg.setFitHeight(controller.screenHeight);

        // Difficulty options container
        VBox vbox = new VBox(30);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(titleLabel);

        // Difficulty options
        for (int i = 0; i < levels.length; i++) {
            Label label = createOutlinedLabel(levels[i], 36, Color.WHITE, Color.BLACK);
            levelLabels[i] = label;

//            final int index = i;  // For use in lambda
//            label.setOnMouseClicked(e -> {
//                selected = index;
//                highlight();
//                String difficultyName = levels[selected];
//                controller.startGameScreen(difficultyName);
//            });
//
//            label.setOnMouseEntered(e -> {
//                if (selected != index) {
//                    selected = index;
//                    highlight();
//                }
//            });

            vbox.getChildren().add(label);
        }

        // Instructions label
        Label instructionsLabel = createOutlinedLabel("Use UP/DOWN arrows and ENTER to select", 24, Color.WHITE, Color.BLACK);
        vbox.getChildren().add(instructionsLabel);

        // Back option
        Label backLabel = createOutlinedLabel("Back to Menu", 30, Color.WHITE, Color.DARKGREY);
        backLabel.setOnMouseClicked(e -> controller.showMenuScreen());
        vbox.getChildren().add(backLabel);

        highlight(); // Initialize highlighting

        getChildren().addAll(bg, vbox);

        // Key navigation
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN) {
                selected = (selected + 1) % levels.length;
                highlight();
            } else if (e.getCode() == KeyCode.UP) {
                selected = (selected - 1 + levels.length) % levels.length;
                highlight();
            } else if (e.getCode() == KeyCode.ENTER) {
                String difficultyName = levels[selected];
                controller.startGameScreen(difficultyName);
            } else if (e.getCode() == KeyCode.ESCAPE) {
                controller.showMenuScreen();
            }
        });

        requestFocus();
    }

    private void highlight() {
        for (int i = 0; i < levelLabels.length; i++) {
            if (i == selected) {
                // Selected difficulty effect
                levelLabels[i].setTextFill(difficultyColors[i]);
                levelLabels[i].setScaleX(1.2);
                levelLabels[i].setScaleY(1.2);

                // Animation for selected item
                ScaleTransition st = new ScaleTransition(Duration.millis(200), levelLabels[i]);
                st.setFromX(1.1);
                st.setToX(1.3);
                st.setFromY(1.1);
                st.setToY(1.3);
                st.setAutoReverse(true);
                st.setCycleCount(ScaleTransition.INDEFINITE);
                st.play();
            } else {
                // Non-selected difficulties
                levelLabels[i].setTextFill(Color.WHITE);
                levelLabels[i].setScaleX(1.0);
                levelLabels[i].setScaleY(1.0);

                // Stop any animations
                levelLabels[i].setEffect(new DropShadow(3, Color.BLACK));
            }
        }
    }

    private Label createOutlinedLabel(String text, int fontSize, Color fillColor, Color outlineColor) {
        Font customFont = Font.loadFont(fontPath, fontSize);

        Label label = new Label(text);
        label.setFont(customFont);
        label.setTextFill(fillColor);

        DropShadow outline = new DropShadow();
        outline.setColor(outlineColor);
        outline.setOffsetX(0);
        outline.setOffsetY(0);
        outline.setRadius(1);
        outline.setSpread(1);
        label.setEffect(outline);

        return label;
    }
}