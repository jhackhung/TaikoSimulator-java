package main.main_game;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class DifficultyScreen extends StackPane {

    private int selected = 0;
    private final String[] levels = {"Easy", "Normal", "Hard"};
    private final Label[] levelLabels = new Label[levels.length];

    public DifficultyScreen(MainController controller) {

        ImageView bg = new ImageView(new Image(getClass().getResource("/assets/main/menu_bg.jpg").toExternalForm()));
        bg.setFitWidth(800);
        bg.setFitHeight(400);

        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);

        for (int i = 0; i < levels.length; i++) {
            Label label = new Label(levels[i]);
            label.setFont(Font.font("Comic Sans MS", 30));
            levelLabels[i] = label;
            vbox.getChildren().add(label);
        }

        highlight();

        getChildren().addAll(bg, vbox);

        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN) {
                selected = (selected + 1) % levels.length;
                highlight();
            } else if (e.getCode() == KeyCode.UP) {
                selected = (selected - 1 + levels.length) % levels.length;
                highlight();
            } else if (e.getCode() == KeyCode.ENTER) {
                controller.setSelectedDifficulty(selected);
                controller.startGameScreen();
            }
        });

        requestFocus();
    }

    private void highlight() {
        for (int i = 0; i < levelLabels.length; i++) {
            levelLabels[i].setTextFill(i == selected ? Color.BLUE : Color.BLACK);
        }
    }
}
