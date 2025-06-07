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
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MenuScreen extends StackPane {

    private int selected = 0;
    private final String[] songIds = {"Yoru ni Kakeru", "Zen Zen Zense", "Zenryoku Shounen"};
    private final Map<String, SongInfo> songInfoMap = new HashMap<>();
    private final StackPane[] songItems = new StackPane[songIds.length];
    private final Label[] songLabels = new Label[songIds.length];
    private final String fontPath = "file:assets/fonts/Taiko_No_Tatsujin_Official_Font.ttf";
    private final double itemWidth = 500;
    private AudioClip menuSelectSound;
    private AudioClip menuConfirmSound;
    private ImageView songCoverArt;
    private Label songInfoLabel;
    private Label songArtistLabel;
    private MediaPlayer currentPreview;
    private Label previewStatusLabel;

    // 靜態類別用於存儲從TJA檔案讀取的所有歌曲資訊
    private static class SongInfo {
        String title;
        String subtitle;
        String artist;
        String wave;
        double demoStart;
        int level;

        // 解析SUBTITLE字串以獲取藝術家名稱
        void parseArtistFromSubtitle() {
            if (subtitle != null && subtitle.startsWith("--")) {
                int slashIndex = subtitle.indexOf('/');
                if (slashIndex > 0) {
                    artist = subtitle.substring(2, slashIndex).trim();
                } else {
                    artist = subtitle.substring(2).trim();
                }
            } else {
                artist = subtitle;
            }
        }
    }

    public MenuScreen(MainController controller) {
        // 預先載入所有歌曲資訊
        loadAllSongInfo();

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
        for (int i = 0; i < songIds.length; i++) {
            SongInfo info = songInfoMap.get(songIds[i]);
            String songName = (info != null && info.title != null) ? info.title : songIds[i];
            String artist = (info != null && info.artist != null) ? info.artist : "Unknown";
            StackPane songItem = createSongItem(songName, artist, i);
            songItems[i] = songItem;
            songListBox.getChildren().add(songItem);
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

        // 延遲加載預覽音樂，確保UI先載入完成
        javafx.application.Platform.runLater(() -> {
            previewSong(selected);
        });

        // Key input handling
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.S) {
                selected = (selected + 1) % songIds.length;
                highlight();
                playSelectSound();
                previewSong(selected); // 每次選擇後重新播放
            } else if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.W) {
                selected = (selected - 1 + songIds.length) % songIds.length;
                highlight();
                playSelectSound();
                previewSong(selected); // 每次選擇後重新播放
            } else if (e.getCode() == KeyCode.ENTER) {
                playConfirmSound();
                stopPreview();
                String selectedSongName = songIds[selected];
                controller.showDifficultyScreen(selectedSongName);
            } else if (e.getCode() == KeyCode.ESCAPE) {
                stopPreview();
                controller.showStartScreen();
            }
        });

        requestFocus();
    }

    // 從TJA檔案讀取所有歌曲資訊
    private void loadAllSongInfo() {
        for (String songId : songIds) {
            SongInfo info = loadSongInfoFromTja(songId);
            if (info != null) {
                songInfoMap.put(songId, info);
            }
        }
    }

    // 從TJA檔案讀取單個歌曲資訊
    private SongInfo loadSongInfoFromTja(String songId) {
        // 尋找對應的 TJA 檔案
        File tjaFile = findTjaFile(songId);
        if (tjaFile == null) {
            System.err.println("TJA file not found for: " + songId);
            return null;
        }

        SongInfo info = new SongInfo();
        info.title = songId; // 預設值

        try (Scanner scanner = new Scanner(tjaFile, "UTF-8")) {
            // 逐行讀取尋找相關標籤
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                if (line.startsWith("TITLE:")) {
                    info.title = line.substring("TITLE:".length());
                } else if (line.startsWith("SUBTITLE:")) {
                    info.subtitle = line.substring("SUBTITLE:".length());
                } else if (line.startsWith("WAVE:")) {
                    info.wave = line.substring("WAVE:".length());
                    info.wave = info.wave.replace(".ogg", ".wav"); // 確保使用正確的音訊格式
                } else if (line.startsWith("DEMOSTART:")) {
                    try {
                        info.demoStart = Double.parseDouble(line.substring("DEMOSTART:".length()));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid DEMOSTART value in " + songId + ".tja");
                        info.demoStart = 30.0; // 預設值
                    }
                } else if (line.startsWith("COURSE:Oni") || line.startsWith("COURSE:Hard") || line.startsWith("COURSE:Normal")) {
                    // 讀取下一行尋找難度等級
                    if (scanner.hasNextLine()) {
                        String nextLine = scanner.nextLine().trim();
                        if (nextLine.startsWith("LEVEL:")) {
                            try {
                                info.level = Integer.parseInt(nextLine.substring("LEVEL:".length()));
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid LEVEL value in " + songId + ".tja");
                            }
                            break; // 找到Oni難度後停止尋找
                        }
                    }
                }
            }

            // 解析藝術家資訊
            info.parseArtistFromSubtitle();

        } catch (FileNotFoundException e) {
            System.err.println("Could not read TJA file: " + e.getMessage());
            return null;
        }

        return info;
    }

    // 尋找TJA檔案
    private File findTjaFile(String songName) {
        // 首先嘗試直接使用歌曲名稱
        File file = new File("assets/music/" + songName + ".tja");
        if (file.exists()) {
            return file;
        }

        // 嘗試無空格版本
        file = new File("assets/music/" + songName.replaceAll("\\s+", "") + ".tja");
        if (file.exists()) {
            return file;
        }

        return null;
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

        // Create a container for the text (song name and artist)
        VBox textBox = new VBox(2, label, artistLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setTranslateX(20);  // Indent the text

        // Create the difficulty stars (just for visual)
        HBox difficultyStars = new HBox(5);
        SongInfo info = songInfoMap.get(songIds[index]);
        int level = (info != null) ? info.level : index + 3; // 如果找不到難度，使用索引+3作為預設值

        for (int i = 0; i < level; i++) {  // 使用實際難度作為星星數量
            ImageView star = new ImageView(new Image("file:assets/main/star.png"));
            star.setFitWidth(15);
            star.setFitHeight(15);
            difficultyStars.getChildren().add(star);
        }
        difficultyStars.setTranslateX(itemWidth/2 - 80);
        difficultyStars.setTranslateY(15);

        // Combine everything in a single pane
        StackPane pane = new StackPane();
        pane.getChildren().addAll(bgRect, textBox, difficultyStars);
        pane.setMaxWidth(itemWidth);

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
        previewStatusLabel = createOutlinedLabel("Preview", 18, Color.WHITE, Color.BLACK);

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

        // Individual control instructions - 僅保留鍵盤控制說明
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
        for (int i = 0; i < songIds.length; i++) {
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
        String songId = songIds[index];
        SongInfo info = songInfoMap.get(songId);

        if (info == null) {
            return;
        }

        try {
            // Update cover art
            Image coverImage = new Image("file:assets/covers/" + songId.replaceAll("\\s+", "") + ".jpg");
            songCoverArt.setImage(coverImage);

            // Update song info
            songInfoLabel.setText(info.title);
            songArtistLabel.setText(info.artist);

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

    private synchronized void previewSong(int index) {
        stopPreview(); // 確保先停止當前播放的預覽

        // 從先前讀取的資訊獲取預覽時間
        SongInfo info = songInfoMap.get(songIds[index]);
        if (info == null) {
            return;
        }

        try {
            // 使用TJA檔中指定的音訊檔案
            String audioFileName = info.wave;
            if (audioFileName == null || audioFileName.isEmpty()) {
                audioFileName = songIds[index] + ".wav"; // 預設檔案名
            }

            File audioFile = new File("assets/music/" + audioFileName);
            if (!audioFile.exists()) {
                System.err.println("Preview file not found: " + audioFile.getPath());
                return;
            }

            // 建立媒體播放器並設定
            Media media = new Media(audioFile.toURI().toString());
            currentPreview = new MediaPlayer(media);

            currentPreview.setVolume(0.0);

            // 設定播放器事件處理
            currentPreview.setOnReady(() -> {
                // 使用從TJA檔案讀取的DEMOSTART值
                double startTime = info.demoStart;
                if (startTime <= 0) {
                    startTime = Math.min(30, currentPreview.getMedia().getDuration().toSeconds() * 0.3);
                }

                double endTime = Math.min(startTime + 15, currentPreview.getMedia().getDuration().toSeconds() * 0.9);

                currentPreview.setStartTime(Duration.seconds(startTime));
                currentPreview.setStopTime(Duration.seconds(endTime));

                // 設定音量和開始播放
                currentPreview.play();
                previewStatusLabel.setAlignment(Pos.CENTER);
                previewStatusLabel.setText("Now Playing");

                // 使用 MediaPlayer 的監聽器逐步提高音量，而非 Timeline
                animateVolume(currentPreview, 0.0, 0.5, 800);
            });

            // 設定錯誤處理
            currentPreview.setOnError(() -> {
                System.err.println("Error playing preview: " + currentPreview.getError().getMessage());
                previewStatusLabel.setText("Preview Error");
                currentPreview = null;
            });

            // 設定播放結束後的處理
            currentPreview.setOnEndOfMedia(() -> {
                // 循環播放預覽
                currentPreview.seek(currentPreview.getStartTime());
                currentPreview.play();
            });

        } catch (Exception e) {
            System.err.println("Could not play preview: " + e.getMessage());
            e.printStackTrace();
            previewStatusLabel.setText("Preview Unavailable");
        }
    }

    private void animateVolume(MediaPlayer player, double startVolume, double endVolume, int durationMs) {
        if (player == null) return;

        final int steps = 20;
        final int stepTime = durationMs / steps;
        final double volumeStep = (endVolume - startVolume) / steps;

        Thread volumeThread = new Thread(() -> {
            try {
                player.setVolume(startVolume);
                for (int i = 1; i <= steps; i++) {
                    Thread.sleep(stepTime);
                    final double newVolume = startVolume + (volumeStep * i);

                    javafx.application.Platform.runLater(() -> {
                        try {
                            // 判斷傳入的 player 是否還活著
                            if (player.getStatus() != MediaPlayer.Status.DISPOSED) {
                                player.setVolume(newVolume);
                            }
                        } catch (Exception e) {
//                            e.printStackTrace();
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        volumeThread.setDaemon(true);
        volumeThread.start();
    }

    private synchronized void stopPreview() {
        if (currentPreview != null && currentPreview.getStatus() != MediaPlayer.Status.DISPOSED) {
            try {
                MediaPlayer playerToStop = currentPreview;
                currentPreview = null; // 立即清除引用，避免後續操作

                // 避免使用 Timeline，改為直接停止
//                playerToStop.setVolume(0);
                playerToStop.stop();
                playerToStop.dispose();
                previewStatusLabel.setText("Preview");
            } catch(Exception e) {
                System.err.println("Error in stopPreview: " + e.getMessage());
            }
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