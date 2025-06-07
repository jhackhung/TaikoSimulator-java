package main.main_game;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainController {

    private final Stage stage;
    private String selectedSongName = "";
    private String selectedDifficultyName = "";

    protected GameEngine gameEngine;
    protected int screenWidth = 1280;
    protected int screenHeight = 720;
    protected String fontPath = "file:assets/fonts/Taiko_No_Tatsujin_Official_Font.ttf";

    public MainController(Stage stage) {
        this.stage = stage;
        stage.setTitle("Taiko Simulator");
        stage.setResizable(false);
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);
        stage.getIcons().add(new javafx.scene.image.Image("file:assets/game/red_note.png"));
    }

    public void showStartScreen() {
        StartScreen startScreen = new StartScreen(this);
        Scene scene = new Scene(startScreen, screenWidth, screenHeight);
        stage.setScene(scene);
        startScreen.requestFocus();
        stage.show();
    }

    public void showMenuScreen() {
        MenuScreen menuScreen = new MenuScreen(this);
        stage.setScene(new Scene(menuScreen, screenWidth, screenHeight));
        menuScreen.requestFocus();
    }

    public void showDifficultyScreen(String songName) {
        this.selectedSongName = songName;
        DifficultyScreen difficultyScreen = new DifficultyScreen(this);
        stage.setScene(new Scene(difficultyScreen, screenWidth, screenHeight));
        difficultyScreen.requestFocus();
    }

    public void startGameScreen(String difficultyName) {
        this.selectedDifficultyName = difficultyName;

        // example: build your file path dynamically:
        String tjaPath = "assets/music/" + selectedSongName + ".tja";
        String musicPath = "assets/music/" + selectedSongName + ".wav";

        gameEngine = new GameEngine(this, tjaPath, musicPath, difficultyName);
        Scene gameScene = new Scene(gameEngine.getRoot(), screenWidth, screenHeight);

        gameEngine.setupInput(gameScene);
        stage.setScene(gameScene);
        gameEngine.start();
    }

    public void showResultScreen(GameEngine.GameResult result) {
        ResultScreen resultScreen = new ResultScreen(this, result);
        Scene scene = new Scene(resultScreen, screenWidth, screenHeight);
        stage.setScene(scene);
        resultScreen.requestFocus();
    }
}
