package main.main_game;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class ResultScreen extends StackPane {

    public ResultScreen(MainController controller) {

        Label title = new Label("Result");
        title.setFont(Font.font("Comic Sans MS", 48));

        Label score = new Label("Score: 76845");
        score.setFont(Font.font("Comic Sans MS", 32));

        Label combo = new Label("Max Combo: 108");
        combo.setFont(Font.font("Comic Sans MS", 28));

        VBox vbox = new VBox(20, title, score, combo);
        vbox.setAlignment(Pos.CENTER);

        getChildren().add(vbox);
    }
}
