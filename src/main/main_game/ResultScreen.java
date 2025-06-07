package main.main_game;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class ResultScreen extends StackPane {
    private MainController controller;
    private String fontPath = "file:assets/fonts/Taiko_No_Tatsujin_Official_Font.ttf";
    private AudioClip resultSound;

    public ResultScreen(MainController controller, GameEngine.GameResult result) {
        this.controller = controller;

        // 背景
        ImageView bg = new ImageView(new Image("file:assets/main/result_bg.jpg"));
        bg.setFitWidth(controller.screenWidth);
        bg.setFitHeight(controller.screenHeight);

        // 結果面板
        Rectangle resultPanel = new Rectangle(700, 500);
        resultPanel.setFill(Color.rgb(255, 255, 255, 0.9));
        resultPanel.setArcWidth(20);
        resultPanel.setArcHeight(20);
        resultPanel.setStroke(Color.rgb(229, 109, 50));
        resultPanel.setStrokeWidth(3);

        // 標題
        Label title = createOutlinedLabel("RESULT", 48, Color.rgb(229, 109, 50), Color.BLACK);

        // 分數
        Label score = createOutlinedLabel(String.format("%,d", result.getScore()), 60, Color.BLACK, Color.TRANSPARENT);
        score.setAlignment(Pos.CENTER);
        score.setMinWidth(300);

        // 創建詳細統計網格
        GridPane statsGrid = createStatsGrid(result);

        // 計算完成率
        int totalNotes = result.getPerfectCount() + result.getGoodCount() + result.getMissCount();
        double completionRate = totalNotes > 0 ?
                (double)(result.getPerfectCount() + result.getGoodCount()) / totalNotes * 100 : 0;

        // 完成率標籤
        Label completionLabel = createOutlinedLabel(
                String.format("Completion: %.1f%%", completionRate),
                28, Color.BLACK, Color.TRANSPARENT
        );

        // 連擊數
        Label comboLabel = createOutlinedLabel("MAX COMBO", 32, Color.rgb(50, 50, 200), Color.TRANSPARENT);
        Label comboValue = createOutlinedLabel(String.valueOf(result.getMaxCombo()), 40, Color.BLACK, Color.TRANSPARENT);

        VBox comboBox = new VBox(5, comboLabel, comboValue);
        comboBox.setAlignment(Pos.CENTER);

        // 按鈕
        Button retryButton = createButton("RETRY", 200);
        retryButton.setOnAction(e -> {
            // 重新開始遊戲
            stopResultSound();
             controller.startGameScreen(controller.getSelectedDifficultyName());
        });

        Button menuButton = createButton("MENU", 200);
        menuButton.setOnAction(e -> {
            stopResultSound();
            controller.showMenuScreen();
        });

        // 組合按鈕
        HBox buttonBox = new HBox(20, retryButton, menuButton);
        buttonBox.setAlignment(Pos.CENTER);

        // 組合所有元素
        VBox resultBox = new VBox(20);
        resultBox.setAlignment(Pos.CENTER);
        resultBox.getChildren().addAll(
                title,
                score,
                completionLabel,
                statsGrid,
                comboBox,
                buttonBox
        );
        resultBox.setPadding(new Insets(20, 30, 20, 30));

        StackPane resultContainer = new StackPane(resultPanel, resultBox);

        getChildren().addAll(bg, resultContainer);

        // 動畫效果
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), resultContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // 鍵盤控制
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.ENTER) {
                stopResultSound();
                controller.showMenuScreen();
            }
        });

        // 添加結果音效
        try {
            resultSound = new AudioClip("file:assets/main/result_bgm.wav");
            playResultSound();
        } catch (Exception e) {
            System.err.println("Could not load sound: " + e.getMessage());
            resultSound = null;
        }

        requestFocus();
    }

    private GridPane createStatsGrid(GameEngine.GameResult result) {
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        // 標題行
        Label typeHeader = createOutlinedLabel("Type", 24, Color.rgb(80, 80, 80), Color.TRANSPARENT);
        Label countHeader = createOutlinedLabel("Count", 24, Color.rgb(80, 80, 80), Color.TRANSPARENT);

        grid.add(typeHeader, 0, 0);
        grid.add(countHeader, 1, 0);

        // 資料行
        Label perfectLabel = createOutlinedLabel("PERFECT", 28, Color.rgb(255, 150, 0), Color.TRANSPARENT);
        Label perfectCount = createOutlinedLabel(String.valueOf(result.getPerfectCount()), 28, Color.BLACK, Color.TRANSPARENT);

        Label goodLabel = createOutlinedLabel("GOOD", 28, Color.rgb(50, 200, 50), Color.TRANSPARENT);
        Label goodCount = createOutlinedLabel(String.valueOf(result.getGoodCount()), 28, Color.BLACK, Color.TRANSPARENT);

        Label missLabel = createOutlinedLabel("MISS", 28, Color.rgb(200, 50, 50), Color.TRANSPARENT);
        Label missCount = createOutlinedLabel(String.valueOf(result.getMissCount()), 28, Color.BLACK, Color.TRANSPARENT);

        // 添加到網格
        grid.add(perfectLabel, 0, 1);
        grid.add(perfectCount, 1, 1);

        grid.add(goodLabel, 0, 2);
        grid.add(goodCount, 1, 2);

        grid.add(missLabel, 0, 3);
        grid.add(missCount, 1, 3);

        return grid;
    }

    private Label createOutlinedLabel(String text, int fontSize, Color fillColor, Color outlineColor) {
        Font customFont = Font.loadFont(fontPath, fontSize);

        Label label = new Label(text);
        label.setFont(customFont != null ? customFont : Font.font("System", fontSize));
        label.setTextFill(fillColor);
        label.setTextAlignment(TextAlignment.CENTER);

        if (outlineColor != Color.TRANSPARENT) {
            DropShadow outline = new DropShadow();
            outline.setColor(outlineColor);
            outline.setOffsetX(0);
            outline.setOffsetY(0);
            outline.setRadius(1);
            outline.setSpread(0.7);
            label.setEffect(outline);
        }

        return label;
    }

    private Button createButton(String text, double width) {
        Button button = new Button(text);
        button.setFont(Font.loadFont(fontPath, 24));
        button.setPrefWidth(width);
        button.setPrefHeight(50);
        button.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #e56d32; -fx-border-width: 2;");

        // 添加懸停效果
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #ffe8c8; -fx-border-color: #e56d32; -fx-border-width: 2;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #e56d32; -fx-border-width: 2;"));

        return button;
    }

    private void playResultSound() {
        if (resultSound != null) {
            resultSound.setCycleCount(AudioClip.INDEFINITE);
            resultSound.play();
        } else {
            System.err.println("Result sound not loaded.");
        }
    }

    public void stopResultSound() {
        if (resultSound != null) {
            resultSound.stop();
        }
    }
}