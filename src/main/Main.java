package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.main_game.GameEngine;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        GameEngine engine = new GameEngine();
        Scene scene = new Scene(engine.getRoot(), 1280, 720);

        primaryStage.setTitle("Taiko Simulator - JavaFX");
        primaryStage.setScene(scene);
        engine.setupInput(scene);
        primaryStage.show();

        engine.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

