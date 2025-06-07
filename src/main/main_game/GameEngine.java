package main.main_game;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class GameEngine {
    private final Group root = new Group();
    private final Canvas canvas = new Canvas(1280, 720);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    private final ArrayList<Note> notes = new ArrayList<>();
    private final ArrayList<Drum> drumQueue = new ArrayList<>();

    private Image bgImage = new Image("file:assets/game/bg_genre_2.jpg");
    private Image redNoteImage = new Image("file:assets/game/red_note.png");
    private Image redBigNoteImage = new Image("file:assets/game/red_big.png");
    private Image blueBigNoteImage = new Image("file:assets/game/blue_big.png");
    private Image blueNoteImage = new Image("file:assets/game/blue_note.png");
    private String fontPath = "file:assets/fonts/Taiko_No_Tatsujin_Official_Font.ttf";
    private Font font = Font.loadFont(fontPath, 24);

    private AudioClip redClipFX;
    private AudioClip blueClipFX;
    private MediaPlayer bgPlayer;

    private double bpm = 120.0;
    private double offset = 3.0;
    private final double hitX = 450;
    private double noteSpeed;
    private final double noteRadius = 32;

    private long startNano;
    private long delayStartTime;
    private double currentTime = 0;
    private boolean delayStarted = false;
    private boolean gameStarted = false;
    private int currentNoteIndex = 0;
    private boolean musicActuallyPlaying = false;
    private double noteAppearLeadTime;  // 預留提前時間 (秒)


    private long lastUpdateTime = 0;

    private int score = 0;
    private int combo = 0;
    private int maxCombo = 0;
    private int missCount = 0;
    private int goodCount = 0;
    private int perfectCount = 0;

    private String lastJudgement = "";
    private long judgementDisplayTime = 0;

    private MainController controller;
    private AnimationTimer gameTimer;
    private GameResult gameResult;

    // 建立 GameResult 類來存儲遊戲結果
    public static class GameResult {
        private final int score;
        private final int maxCombo;
        private final int missCount;
        private final int goodCount;
        private final int perfectCount;

        public GameResult(int score, int maxCombo, int missCount, int goodCount, int perfectCount) {
            this.score = score;
            this.maxCombo = maxCombo;
            this.missCount = missCount;
            this.goodCount = goodCount;
            this.perfectCount = perfectCount;
        }

        public int getScore() {
            return score;
        }

        public int getMaxCombo() {
            return maxCombo;
        }

        public int getMissCount() {
            return missCount;
        }

        public int getGoodCount() {
            return goodCount;
        }

        public int getPerfectCount() {
            return perfectCount;
        }

    }

    public GameEngine(MainController controller, String tjaPath, String musicPath, String course) {
        this.controller = controller;
        root.getChildren().add(canvas);

        try {
            ChartParser.parse(tjaPath, notes, course);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bpm = ChartParser.bpm;
        offset = ChartParser.offset;
        loadSounds(musicPath);
        calculateNoteSpeed();
    }

    public Group getRoot() {
        return root;
    }

    private void loadSounds(String musicPath) {
        redClipFX = new AudioClip(new File("assets/main/rednote.wav").toURI().toString());
        blueClipFX = new AudioClip(new File("assets/main/bluenote.wav").toURI().toString());

        try {
            Media bgMusic = new Media(new File(musicPath).toURI().toString());
            bgPlayer = new MediaPlayer(bgMusic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateNoteSpeed() {
        double travelDistance = 1280 - (hitX + noteRadius);
        double travelBeats = 4.0;
        double secondsPerBeat = 60.0 / bpm;
        double timeToReach = travelBeats * secondsPerBeat;
        noteSpeed = travelDistance / timeToReach;
        noteAppearLeadTime = timeToReach;
        System.out.printf("noteSpeed: %.4f px/sec (travel %.1f px in %.2f sec)\n", noteSpeed, travelDistance, timeToReach);
    }

    public void setupInput(Scene scene) {
        scene.setOnKeyPressed((KeyEvent e) -> {
            if (!gameStarted) return;

            if (e.getCode() == KeyCode.F || e.getCode() == KeyCode.J) {
                playClip(redClipFX);
                checkHit(1);
            } else if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.K) {
                playClip(blueClipFX);
                checkHit(2);
            }
        });
    }

    private void playClip(AudioClip clip) {
        if (clip != null) {
            clip.play();
        }
    }

    private void checkHit(int type) {
        if (drumQueue.isEmpty()) return;

        double perfectLeft = hitX - 32;
        double perfectRight = hitX + 32;
        double goodLeft = hitX - 64;
        double goodRight = hitX + 64;

        Iterator<Drum> iter = drumQueue.iterator();

        while (iter.hasNext()) {
            Drum drum = iter.next();
            int drumType = drum.getType();

            boolean hitMatched = false;
            if (drumType == 1 || drumType == 3) { // 紅音符、小紅或大紅
                if (type == 1) hitMatched = true;
            } else if (drumType == 2 || drumType == 4) { // 藍音符、小藍或大藍
                if (type == 2) hitMatched = true;
            }

            if (!hitMatched) return;

            double noteWidth = (drumType <= 2) ? 56 : 90;
            double leftEdge = drum.getX();
            double rightEdge = drum.getX() + noteWidth;
            double centerX = (leftEdge + rightEdge) / 2;

//        System.out.println("Checking hit: " + type + " on drum type " + drumType + " at X=" + drum.getX() + " | ");
//        System.out.println("Left Edge: " + leftEdge + ", Right Edge: " + rightEdge);
//        System.out.println("HitX: "+ hitX + "," + "Hit zones: Perfect [" + perfectLeft + ", " + perfectRight + "], Good [" + goodLeft + ", " + goodRight + "]");

            // 已經超過可判定範圍，當 miss 處理
            if (centerX < goodLeft) {
                iter.remove();
                lastJudgement = "Miss";
                judgementDisplayTime = System.currentTimeMillis();
                missCount++;
                combo = 0;
                continue; // 繼續往後找下一顆
            }

            if (leftEdge > goodRight + 50) {
                return; // Not in the hit zone
            }

            // 基礎分數
            int baseScore = 0;

            if (centerX >= perfectLeft && centerX <= perfectRight) {
                baseScore = (drumType <= 2) ? 300 : 600;  // 大音符分數*2
                drumQueue.removeFirst();
                lastJudgement = "Perfect";
                judgementDisplayTime = System.currentTimeMillis();
                if (drumType <= 2) perfectCount++;
                else perfectCount += 2; // 大音符算兩個 Perfect
                combo++;
                score += baseScore + combo * 2;
                break;
            } else if (centerX >= goodLeft && centerX <= goodRight) {
                baseScore = (drumType <= 2) ? 100 : 200;
                drumQueue.removeFirst();
                lastJudgement = "Good";
                judgementDisplayTime = System.currentTimeMillis();
                if (drumType <= 2) goodCount++;
                else goodCount += 2; // 大音符算兩個 Good
                combo++;
                score += baseScore + combo * 2;
                break;
            }
        }
        if (combo > maxCombo) maxCombo = combo;
    }

    public void start() {
        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!delayStarted) {
                    delayStarted = true;
                    delayStartTime = now;
                    return;
                }

                double delayElapsed = (now - delayStartTime) / 1e9;

                if (!gameStarted && delayElapsed >= 0) {
                    gameStarted = true;
                    if (bgPlayer != null) {
                        bgPlayer.stop();
                        bgPlayer.play();

                        bgPlayer.setOnPlaying(() -> {
                            startNano = System.nanoTime();
                            lastUpdateTime = startNano;
                            musicActuallyPlaying = true;
                            System.out.println("Music started! startNano reset.");
                        });
                    }
                    return;
                }

                if (!gameStarted || startNano == 0 || !musicActuallyPlaying) return;

                long nowNano = System.nanoTime();
                double deltaTime = (nowNano - lastUpdateTime) / 1e9;
                lastUpdateTime = nowNano;

                currentTime = (nowNano - startNano) / 1e9;

                spawnNotes();
                updateDrums(deltaTime);
                render();

                // 檢查是否所有音符都已處理完畢
                if (currentNoteIndex >= notes.size() && drumQueue.isEmpty()) {
                    handleGameEnd();
                }
            }

            private void handleGameEnd() {
                this.stop();
                if (bgPlayer != null) {
                    bgPlayer.stop();
                }

                // 創建遊戲結果對象
                gameResult = new GameResult(score, maxCombo, missCount, goodCount, perfectCount);

                // 延遲顯示結果畫面，給玩家一點時間看最後的畫面
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                delay.setOnFinished(e -> {
                    controller.showResultScreen(gameResult);
                });
                delay.play();
            }
        };
        gameTimer.start();
    }

    // 添加一個方法獲取遊戲結果
    public GameResult getGameResult() {
        return gameResult != null ? gameResult : new GameResult(score, maxCombo, missCount, goodCount, perfectCount);
    }

    private void spawnNotes() {
        while (currentNoteIndex < notes.size() && currentTime >= notes.get(currentNoteIndex).getTime() - noteAppearLeadTime) {
            Note note = notes.get(currentNoteIndex);
            if (note.getType() != 0) {
                if (note.getType() == 1 || note.getType() == 2) {
                    drumQueue.add(new Drum(note.getType(), 1280, 225));
                } else if (note.getType() == 3 || note.getType() == 4) {
                    drumQueue.add(new Drum(note.getType(), 1280, 210));
                }
            }
            currentNoteIndex++;
        }
    }

    private void updateDrums(double deltaTime) {
        Iterator<Drum> iter = drumQueue.iterator();
        while (iter.hasNext()) {
            Drum drum = iter.next();
            drum.move(-noteSpeed * deltaTime);
            if (drum.getX() + 64 < hitX - 64) {
                iter.remove();
//                lastJudgement = "Miss";
//                judgementDisplayTime = System.currentTimeMillis();
                missCount++;
                combo = 0;
            }
        }
    }

    private void render() {
        gc.clearRect(0, 0, 1280, 720);
        gc.drawImage(bgImage, 0, 0);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        double radius = 32;
        double radius_2 = radius * 1.5;
        gc.strokeOval(hitX - radius, 200 + 55 - radius, radius * 2, radius * 2);
        gc.strokeOval(hitX - radius_2, 200 + 55 - radius_2, radius_2 * 2, radius_2 * 2);

//        gc.setStroke(Color.LIME);
//        gc.strokeRect(hitX - 32, 200 + 55 - 32, 64, 64); // Perfect zone
//        gc.setStroke(Color.ORANGE);
//        gc.strokeRect(hitX - 45, 200 + 55 - 45, 90, 90); // Good zone

        // 畫出小音符的判定區域 (紅色)
//        gc.setStroke(Color.RED);
//        gc.setLineWidth(2);
//        double perfectLeftSmall = hitX - 32;
//        double perfectRightSmall = hitX + 32;
//        double goodLeftSmall = hitX - 45;
//        double goodRightSmall = hitX + 64;
//        gc.strokeRect(perfectLeftSmall, 200, perfectRightSmall - perfectLeftSmall, 64);
//        gc.strokeRect(goodLeftSmall, 200, goodRightSmall - goodLeftSmall, 64);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFont(font);
        gc.fillText("" + score, 150, 210);

        if (combo > 10) {
            gc.setFill(Color.BLACK);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.loadFont(fontPath, 36));
            gc.fillText("" + combo, 250, 255);
        }

        if (!lastJudgement.isEmpty() && System.currentTimeMillis() - judgementDisplayTime < 800) {
            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.loadFont(fontPath, 24));
            gc.fillText(lastJudgement, 450, 180);
        }

        for (Drum drum : drumQueue) {
            Image img;
            switch (drum.getType()) {
                case 1:
                    img = redNoteImage;
                    break;
                case 2:
                    img = blueNoteImage;
                    break;
                case 3:
                    img = redBigNoteImage;
                    break;
                case 4:
                    img = blueBigNoteImage;
                    break;
                default:
                    img = null;
            }
            if (img != null) {
                gc.drawImage(img, drum.getX(), drum.getY());
            }
        }
    }

    static class Drum {
        private final int type;
        private double x;
        private final double y;

        public Drum(int type, double x, double y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }

        public int getType() {
            return type;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void move(double dx) {
            x += dx;
        }
    }
}
