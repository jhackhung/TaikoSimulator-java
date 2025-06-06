package main.main_game;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.File;

public class MenuScreen extends StackPane {

    private int selected = 0;
    private final String[] songs = {"Yoru ni Kakeru", "Zen Zen Zense", "Zenryoku Shounen"};
    private final String[] artists = {"YOASOBI", "RADWIMPS", "Sukima Switch"};
    private final StackPane[] songItems = new StackPane[songs.length];
    private final Label[] songLabels = new Label[songs.length];
    private final String fontPath = "file:assets/fonts/Taiko_No_Tatsujin_Official_Font.ttf";
    private final double itemWidth = 500;
    private AudioClip menuSelectSound;
    private AudioClip menuConfirmSound;
    private ImageView songCoverArt;
    private Label songInfoLabel;
    private Label songArtistLabel;
    private MediaPlayer currentPreview;
    private Label previewStatusLabel;

    public MenuScreen(MainController controller) {
        // Set up background with a subtle pattern
        ImageView bg = new ImageView(new Image("file:assets/main/menu_bg.jpg"));
        bg.setFitWidth(controller.screenWidth);
        bg.setFitHeight(controller.screenHeight);

        // Load sound effects
        try {
            menuSelectSound = new AudioClip("file:assets/main/se_ka.wav");
            menuConfirmSound = new AudioClip("file:assets/main/se_don.wav");
        } catch (Exception e) {
            System.err.println("Could not load sound: " + e.getMessage());
            menuSelectSound = null;
            menuConfirmSound = null;
        }

        // Create header with title
        Label titleLabel = createOutlinedLabel("Select a Song", 48, Color.rgb(229, 109, 50), Color.WHITE);
        titleLabel.setTranslateY(-200);

        // Create song selection area
        VBox songListBox = new VBox(15);
        songListBox.setAlignment(Pos.CENTER);

        // Create song items
        for (int i = 0; i < songs.length; i++) {
            StackPane songItem = createSongItem(songs[i], artists[i], i);
            songItems[i] = songItem;
            songListBox.getChildren().add(songItem);

            // Add click event
            final int index = i;
            songItem.setOnMouseClicked(e -> {
                if (selected != index) {
                    selected = index;
                    highlight();
                    playSelectSound();
                    previewSong(index);
                } else if (e.getClickCount() == 2) {
                    playConfirmSound();
                    stopPreview();
                    controller.setSelectedSong(index);
                    controller.showDifficultyScreen();
                }
            });

            songItem.setOnMouseEntered(e -> {
                if (selected != index) {
                    selected = index;
                    highlight();
                    playSelectSound();
                    previewSong(index);
                }
            });
        }

        // Create song preview panel
        BorderPane songPreviewPane = createSongPreviewPane();

        // Create song list panel
        VBox songListPane = new VBox(20);
        songListPane.setAlignment(Pos.CENTER);
        songListPane.getChildren().add(songListBox);

        // Create instruction box
        StackPane instructionBox = createInstructionBox();

        // Main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(songListPane);
        mainLayout.setRight(songPreviewPane);
        mainLayout.setBottom(instructionBox);
        mainLayout.setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        BorderPane.setAlignment(instructionBox, Pos.CENTER);

        // Add all elements to the root pane
        getChildren().addAll(bg, mainLayout);

        // Initialize selection
        highlight();
        previewSong(selected); // Start previewing the first song

        // Key input handling
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.S) {
                selected = (selected + 1) % songs.length;
                highlight();
                playSelectSound();
                previewSong(selected);
            } else if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.W) {
                selected = (selected - 1 + songs.length) % songs.length;
                highlight();
                playSelectSound();
                previewSong(selected);
            } else if (e.getCode() == KeyCode.ENTER) {
                playConfirmSound();
                stopPreview();
                controller.setSelectedSong(selected);
                controller.showDifficultyScreen();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                stopPreview();
                controller.showStartScreen();
            }
        });

        requestFocus();
    }

    private StackPane createSongItem(String songName, String artist, int index) {
        // Create a stylish background for the song item
        Rectangle bgRect = new Rectangle(itemWidth, 70);
        bgRect.setArcWidth(20);
        bgRect.setArcHeight(20);
        bgRect.setFill(Color.rgb(240, 240, 240, 0.85));
        bgRect.setStroke(Color.rgb(80, 80, 80));
        bgRect.setStrokeWidth(2);

        // Create the main song label
        Label label = createOutlinedLabel(songName, 28, Color.BLACK, Color.TRANSPARENT);
        songLabels[index] = label;

        // Create the artist label
        Label artistLabel = new Label("by " + artist);
        artistLabel.setFont(Font.loadFont(fontPath, 18));
        artistLabel.setTextFill(Color.GRAY);

        // Create a note icon
        ImageView noteIcon = new ImageView(new Image("file:assets/main/note_icon.png"));
        noteIcon.setFitHeight(30);
        noteIcon.setFitWidth(30);
        noteIcon.setTranslateX(-itemWidth/2 + 25);

        // Create a container for the text (song name and artist)
        VBox textBox = new VBox(2, label, artistLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setTranslateX(20);  // Indent the text

        // Create the difficulty stars (just for visual)
        HBox difficultyStars = new HBox(5);
        for (int i = 0; i < index + 3; i++) {  // More stars for higher index songs
            ImageView star = new ImageView(new Image("file:assets/main/star.png"));
            star.setFitWidth(15);
            star.setFitHeight(15);
            difficultyStars.getChildren().add(star);
        }
        difficultyStars.setTranslateX(itemWidth/2 - 80);
        difficultyStars.setTranslateY(15);

        // Combine everything in a single pane
        StackPane pane = new StackPane();
        pane.getChildren().addAll(bgRect, noteIcon, textBox, difficultyStars);
        pane.setMaxWidth(itemWidth);

        // Add hover effects
        pane.setOnMouseEntered(e -> {
            bgRect.setFill(Color.rgb(255, 240, 200, 0.9));
        });

        pane.setOnMouseExited(e -> {
            if (selected != index) {
                bgRect.setFill(Color.rgb(240, 240, 240, 0.85));
            }
        });

        return pane;
    }

    private BorderPane createSongPreviewPane() {
        BorderPane previewPane = new BorderPane();
        previewPane.setPrefWidth(300);
        previewPane.setMaxHeight(400);
        previewPane.setTranslateX(-30);  // Adjust position

        // Song cover art
        songCoverArt = new ImageView();
        songCoverArt.setFitWidth(250);
        songCoverArt.setFitHeight(250);

        // Add a fancy border to the cover art
        Rectangle coverBorder = new Rectangle(260, 260);
        coverBorder.setFill(Color.TRANSPARENT);
        coverBorder.setStroke(Color.WHITE);
        coverBorder.setStrokeWidth(5);
        coverBorder.setArcWidth(10);
        coverBorder.setArcHeight(10);

        StackPane coverArtPane = new StackPane(coverBorder, songCoverArt);

        // Song information
        songInfoLabel = createOutlinedLabel("", 26, Color.WHITE, Color.BLACK);
        songArtistLabel = createOutlinedLabel("", 20, Color.LIGHTGRAY, Color.BLACK);

        // Preview status
        previewStatusLabel = createOutlinedLabel("Preview Playing", 18, Color.WHITE, Color.BLACK);

        // Create a stylish preview indicator
        StackPane previewIndicator = new StackPane();
        Rectangle previewBg = new Rectangle(160, 40);
        previewBg.setFill(Color.rgb(50, 50, 50, 0.8));
        previewBg.setArcWidth(20);
        previewBg.setArcHeight(20);
        previewIndicator.getChildren().addAll(previewBg, previewStatusLabel);

        // Create a stylish audio waveform visualization (just for visual effect)
        HBox waveformBox = new HBox(3);
        waveformBox.setAlignment(Pos.CENTER);

        for (int i = 0; i < 5; i++) {
            Rectangle bar = new Rectangle(8, 10 + Math.random() * 15);
            bar.setFill(Color.rgb(229, 109, 50, 0.8));
            bar.setArcWidth(3);
            bar.setArcHeight(3);
            waveformBox.getChildren().add(bar);

            // Add small animation to each bar
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(bar.heightProperty(), bar.getHeight())),
                    new KeyFrame(Duration.millis(800 + Math.random() * 400),
                            new KeyValue(bar.heightProperty(), 5 + Math.random() * 20))
            );
            timeline.setAutoReverse(true);
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }

        previewIndicator.getChildren().add(waveformBox);

        // Combine elements for song info
        VBox songInfoBox = new VBox(10, songInfoLabel, songArtistLabel);
        songInfoBox.setAlignment(Pos.CENTER);

        // Combine all elements
        VBox infoContainer = new VBox(20, songInfoBox, previewIndicator);
        infoContainer.setAlignment(Pos.CENTER);
        infoContainer.setTranslateY(20);

        // Arrange in the border pane
        previewPane.setCenter(coverArtPane);
        previewPane.setBottom(infoContainer);

        return previewPane;
    }

    private StackPane createInstructionBox() {
        // Create a stylish instruction box
        Rectangle box = new Rectangle(650, 80);
        box.setArcWidth(15);
        box.setArcHeight(15);
        box.setFill(Color.rgb(240, 240, 240, 0.8));
        box.setStroke(Color.rgb(80, 80, 80));
        box.setStrokeWidth(2);

        // Control instructions
        VBox instructionsVBox = new VBox(8);
        instructionsVBox.setAlignment(Pos.CENTER);

        Label controlsLabel = createOutlinedLabel("CONTROLS", 22, Color.rgb(229, 109, 50), Color.BLACK);

        HBox keyControls = new HBox(20);
        keyControls.setAlignment(Pos.CENTER);

        // Individual control instructions
        Label upDownLabel = new Label("↑/↓ or W/S: Select Song");
        Label enterLabel = new Label("ENTER: Confirm");
        Label escLabel = new Label("ESC: Back");

        Font controlFont = Font.loadFont(fontPath, 18);
        upDownLabel.setFont(controlFont);
        enterLabel.setFont(controlFont);
        escLabel.setFont(controlFont);

        keyControls.getChildren().addAll(upDownLabel, enterLabel, escLabel);
        instructionsVBox.getChildren().addAll(controlsLabel, keyControls);

        StackPane pane = new StackPane(box, instructionsVBox);
        pane.setPadding(new javafx.geometry.Insets(20));
        pane.setMaxHeight(120);
        return pane;
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

    private void highlight() {
        // Update song items appearance
        for (int i = 0; i < songs.length; i++) {
            Rectangle bgRect = (Rectangle) songItems[i].getChildren().get(0);

            if (i == selected) {
                // Selected song gets highlighted
                songLabels[i].setTextFill(Color.rgb(229, 109, 50));
                bgRect.setFill(Color.rgb(255, 240, 200, 0.9));
                bgRect.setStroke(Color.rgb(229, 109, 50));
                bgRect.setStrokeWidth(3);

                // Add glow effect
                Glow glow = new Glow(0.5);
                songItems[i].setEffect(glow);

                // Add scale animation
                ScaleTransition st = new ScaleTransition(Duration.millis(200), songItems[i]);
                st.setToX(1.05);
                st.setToY(1.05);
                st.play();

                // Update preview panel
                updateSongPreview(i);
            } else {
                // Non-selected songs return to normal
                songLabels[i].setTextFill(Color.BLACK);
                bgRect.setFill(Color.rgb(240, 240, 240, 0.85));
                bgRect.setStroke(Color.rgb(80, 80, 80));
                bgRect.setStrokeWidth(2);

                // Remove effects
                songItems[i].setEffect(null);

                // Reset scale
                ScaleTransition st = new ScaleTransition(Duration.millis(200), songItems[i]);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            }
        }
    }

    private void updateSongPreview(int index) {
        try {
            // Update cover art
            Image coverImage = new Image("file:assets/covers/" + songs[index].replaceAll("\\s+", "") + ".jpg");
            songCoverArt.setImage(coverImage);

            // Update song info
            songInfoLabel.setText(songs[index]);
            songArtistLabel.setText(artists[index]);

        } catch (Exception e) {
            System.err.println("Could not load cover art: " + e.getMessage());
            // Use a default/placeholder image if the specific one isn't available
            try {
                songCoverArt.setImage(new Image("file:assets/covers/default.jpg"));
            } catch (Exception ex) {
                // If even that fails, just leave it blank
                System.err.println("Could not load default cover art");
            }
        }
    }

    private void previewSong(int index) {
        stopPreview(); // Stop any currently playing preview

        try {
//            String songFileName = songs[index].replaceAll("\\s+", "") + ".wav";
            String songFileName = songs[index] + ".wav";
            String previewPath = "assets/music/" + songFileName;

            File audioFile = new File(previewPath);
            if (!audioFile.exists()) {
                System.err.println("Preview file not found: " + previewPath);
                return;
            }

            Media media = new Media(audioFile.toURI().toString());
            currentPreview = new MediaPlayer(media);

            // Set the start position (e.g., 30 seconds into the song)
            currentPreview.setStartTime(Duration.seconds(30));

            // Set the stop time (e.g., play for 15 seconds)
            currentPreview.setStopTime(Duration.seconds(45));

            // Set volume
            currentPreview.setVolume(0.5);

            // Start playing
            currentPreview.play();

            // Update the preview status
            previewStatusLabel.setText("▶ Now Playing");

            // Add a fade-in effect
            currentPreview.setVolume(0);
            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(currentPreview.volumeProperty(), 0)),
                    new KeyFrame(Duration.seconds(1), new KeyValue(currentPreview.volumeProperty(), 0.5))
            );
            fadeIn.play();

        } catch (Exception e) {
            System.err.println("Could not play preview: " + e.getMessage());
            previewStatusLabel.setText("Preview Unavailable");
        }
    }

    private void stopPreview() {
        if (currentPreview != null) {
            // Fade out and then stop
            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(currentPreview.volumeProperty(), currentPreview.getVolume())),
                    new KeyFrame(Duration.millis(300), new KeyValue(currentPreview.volumeProperty(), 0))
            );
            fadeOut.setOnFinished(e -> {
                currentPreview.stop();
                currentPreview.dispose();
                currentPreview = null;
            });
            fadeOut.play();

            previewStatusLabel.setText("Preview");
        }
    }

    private void playSelectSound() {
        if (menuSelectSound != null) {
            menuSelectSound.play(0.3);
        }
    }

    private void playConfirmSound() {
        if (menuConfirmSound != null) {
            menuConfirmSound.play(0.5);
        }
    }

    // Clean up resources
    public void dispose() {
        stopPreview();
    }
}