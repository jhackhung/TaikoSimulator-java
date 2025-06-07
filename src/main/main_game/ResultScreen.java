package main.main_game;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class ResultScreen extends StackPane {
    private MainController controller;
    private String fontPath = "file:assets/fonts/Taiko_No_Tatsujin_Official_Font.ttf";

    public ResultScreen(MainController controller, GameEngine.GameResult result) {
        this.controller = controller;

        // 背景
        ImageView bg = new ImageView(new Image("file:assets/main/result_bg.jpg"));
        bg.setFitWidth(controller.screenWidth);
        bg.setFitHeight(controller.screenHeight);

        // 結果面板
        Rectangle resultPanel = new Rectangle(500, 400);
        resultPanel.setFill(Color.rgb(255, 255, 255, 0.9));
        resultPanel.setArcWidth(20);
        resultPanel.setArcHeight(20);
        resultPanel.setStroke(Color.rgb(229, 109, 50));
        resultPanel.setStrokeWidth(3);

        // 標題
        Label title = createOutlinedLabel("RESULT", 48, Color.rgb(229, 109, 50), Color.BLACK);

        // 分數
        Label score = createOutlinedLabel("SCORE: " + result.getScore(), 36, Color.BLACK, Color.TRANSPARENT);

        // 連擊數
        Label combo = createOutlinedLabel("MAX COMBO: " + result.getMaxCombo(), 32, Color.BLACK, Color.TRANSPARENT);

        // 等級評定
        String grade = calculateGrade(result.getScore());
        Label gradeLabel = createOutlinedLabel(grade, 72, Color.GOLD, Color.BLACK);

        // 按鈕
        Button retryButton = createButton("RETRY", 200);
        retryButton.setOnAction(e -> {
            // 重新開始遊戲
            // controller.startGameScreen(controller.getSelectedSongName(), controller.getSelectedDifficultyName());
        });

        Button menuButton = createButton("MENU", 200);
        menuButton.setOnAction(e -> {
            controller.showMenuScreen();
        });

        // 組合所有元素
        VBox buttonBox = new VBox(20, retryButton, menuButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox resultBox = new VBox(20, title, score, combo, gradeLabel, buttonBox);
        resultBox.setAlignment(Pos.CENTER);

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
                controller.showMenuScreen();
            }
        });

        requestFocus();
    }

    private Label createOutlinedLabel(String text, int fontSize, Color fillColor, Color outlineColor) {
        Font customFont = Font.loadFont(fontPath, fontSize);

        Label label = new Label(text);
        label.setFont(customFont != null ? customFont : Font.font("System", fontSize));
        label.setTextFill(fillColor);

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

    private String calculateGrade(int score) {
        if (score >= 950000) return "S+";
        else if (score >= 900000) return "S";
        else if (score >= 800000) return "A";
        else if (score >= 700000) return "B";
        else if (score >= 600000) return "C";
        else return "D";
    }
}