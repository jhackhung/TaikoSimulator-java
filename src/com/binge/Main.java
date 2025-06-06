package com.binge;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            String song = "Zenryoku Shounen";
            String diff = "Easy";

            /* 解析譜面 */
            List<NoteData> chart = TjaParser.parse(song + ".tja", diff);

            /* 建立遊戲畫面 */
            GamePane gamePane = new GamePane(chart, song);

            Scene scene = new Scene(gamePane, 900, 400);
            stage.setTitle("Taiko Simulator");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}