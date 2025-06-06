package main.main_game;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainController {

    private final Stage stage;
    private int selectedSong = 0;
    private int selectedDifficulty = 0;
    protected int screenWidth = 1280;
    protected int screenHeight = 720;
    protected String fontPath = "file:assets/fonts/Taiko_No_Tatsujin_Official_Font.ttf";

    public MainController(Stage stage) {
        this.stage = stage;
        stage.setTitle("Taiko Simulator");
    }

    public void showStartScreen() {
        StartScreen startScreen = new StartScreen(this);
        stage.setScene(new Scene(startScreen, screenWidth, screenHeight));
        stage.show();
    }

    public void showMenuScreen() {
        MenuScreen menuScreen = new MenuScreen(this);
        stage.setScene(new Scene(menuScreen, screenWidth, screenHeight));
    }

    public void showDifficultyScreen() {
        DifficultyScreen difficultyScreen = new DifficultyScreen(this);
        stage.setScene(new Scene(difficultyScreen, screenWidth, screenHeight));
    }

    public void startGameScreen() {
//        GameScreen gameScreen = new GameScreen(this);
//        stage.setScene(new Scene(gameScreen.getRoot(), 1280, 720));
//        gameScreen.start();
    }

    public void showResultScreen() {
        ResultScreen resultScreen = new ResultScreen(this);
        stage.setScene(new Scene(resultScreen, screenWidth, screenHeight));
    }

    public void setSelectedSong(int index) {
        selectedSong = index;
    }

    public void setSelectedDifficulty(int level) {
        selectedDifficulty = level;
    }

    public int getSelectedSong() {
        return selectedSong;
    }

    public int getSelectedDifficulty() {
        return selectedDifficulty;
    }
}
