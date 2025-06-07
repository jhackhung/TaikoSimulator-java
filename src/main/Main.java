package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.main_game.GameEngine;
import main.main_game.MainController;

public class Main extends Application {
//    @Override
//    public void start(Stage primaryStage) {
//        String tjaPath = "assets/music/Yoru ni Kakeru.tja";
//        String musicPath = "assets/music/Yoru ni Kakeru.wav";
//        String course = "Normal";
//
//        GameEngine engine = new GameEngine(tjaPath, musicPath, course);
//        Scene scene = new Scene(engine.getRoot(), 1280, 720);
//
//        primaryStage.setTitle("Taiko Simulator - JavaFX");
//        primaryStage.setScene(scene);
//        engine.setupInput(scene);
//        primaryStage.show();
//
//        engine.start();
//    }

    @Override
    public void start(Stage primaryStage) {
        MainController controller = new MainController(primaryStage);
        controller.showStartScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

