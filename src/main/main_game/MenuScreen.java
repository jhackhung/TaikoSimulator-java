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

public class MenuScreen extends StackPane {

    private int selected = 0;
    private final String[] songs = {"Yoru ni Kakeru", "Zen Zen Zense", "Zenryoku Shounen"};
    private final Label[] songLabels = new Label[songs.length];

    public MenuScreen(MainController controller) {

        ImageView bg = new ImageView(new Image("file:assets/main/menu_bg.jpg"));
        bg.setFitWidth(800);
        bg.setFitHeight(400);

        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);

        for (int i = 0; i < songs.length; i++) {
            Label label = new Label(songs[i]);
            label.setFont(Font.font("Comic Sans MS", 30));
            songLabels[i] = label;
            vbox.getChildren().add(label);
        }

        highlight();

        getChildren().addAll(bg, vbox);

        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN) {
                selected = (selected + 1) % songs.length;
                highlight();
            } else if (e.getCode() == KeyCode.UP) {
                selected = (selected - 1 + songs.length) % songs.length;
                highlight();
            } else if (e.getCode() == KeyCode.ENTER) {
                controller.setSelectedSong(selected);
                controller.showDifficultyScreen();
            }
        });

        requestFocus();
    }

    private void highlight() {
        for (int i = 0; i < songLabels.length; i++) {
            songLabels[i].setTextFill(i == selected ? Color.RED : Color.BLACK);
        }
    }
}
