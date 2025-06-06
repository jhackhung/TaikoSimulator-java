package main.main_game;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class StartScreen extends StackPane {
    private String fontPath = "file:assets/fonts/Taiko_No_Tatsujin_Official_Font.ttf";
    private AudioClip titleSound, confirmSound, startSound;;

    public StartScreen(MainController controller) {

        ImageView bg = new ImageView(new Image("file:assets/main/start_bg.jpg"));
        bg.setFitWidth(controller.screenWidth);
        bg.setFitHeight(controller.screenHeight);

        try {
            titleSound = new AudioClip("file:assets/main/v_title.wav");
            startSound = new AudioClip("file:assets/main/v_start.wav");
            confirmSound = new AudioClip("file:assets/main/se_don.wav");
            playTitleSound();
        } catch (Exception e) {
            System.err.println("Could not load sound: " + e.getMessage());
            titleSound = null;
            startSound = null;
        }

        Label title = createOutlinedLabel("Taiko Simulator", 56, Color.rgb(229, 109, 50), Color.WHITE);
        Label subtitle = createOutlinedLabel("Press Enter or Click to Start", 36, Color.WHITE, Color.RED);

        // 動畫效果
        FadeTransition ft = new FadeTransition(Duration.seconds(1.2), subtitle);
        ft.setFromValue(1.0);
        ft.setToValue(0.3);
        ft.setAutoReverse(true);
        ft.setCycleCount(FadeTransition.INDEFINITE);
        ft.play();

        VBox box = new VBox(20, title, subtitle);
        box.setAlignment(Pos.CENTER);

        getChildren().addAll(bg, box);

        setOnMouseClicked(e -> {
            playConfirmSound();
            playStartSound();
            controller.showMenuScreen();
        });

        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                playConfirmSound();
                playStartSound();
                controller.showMenuScreen();
            }
        });

        requestFocus();
    }

    private Label createOutlinedLabel(String text, int fontSize, Color fillColor, Color outlineColor) {
        Font customFont = Font.loadFont(fontPath, fontSize);

        Label label = new Label(text);
        label.setFont(customFont);  // Set the loaded font
        label.setTextFill(fillColor);

        DropShadow outline = new DropShadow();
        outline.setColor(outlineColor);
        outline.setOffsetX(0);
        outline.setOffsetY(0);
        outline.setRadius(1);
        outline.setSpread(1);
        label.setEffect(outline);

        return label;
    }

    private void playTitleSound() {
        if (titleSound != null) {
            titleSound.play();
        }
    }

    private void playConfirmSound() {
        if (confirmSound != null) {
            confirmSound.play(0.5);
        }
    }

    private void playStartSound() {
        if (startSound != null) {
            startSound.play();
        } else {
            System.err.println("Start sound not loaded.");
        }
    }
}
