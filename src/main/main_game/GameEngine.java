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

import java.io.File;
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

    private long lastUpdateTime = 0;

    private int score = 0;
    private int combo = 0;
    private int maxCombo = 0;

    private String lastJudgement = "";
    private long judgementDisplayTime = 0;

    public GameEngine() {
        root.getChildren().add(canvas);
        ChartParser.parse("assets/game/yoasobi.txt", notes);
        bpm = ChartParser.bpm;
        offset = ChartParser.offset;
        loadSounds();
        calculateNoteSpeed();
    }

    public Group getRoot() {
        return root;
    }

    private void loadSounds() {
        redClipFX = new AudioClip(new File("assets/main/rednote.wav").toURI().toString());
        blueClipFX = new AudioClip(new File("assets/main/bluenote.wav").toURI().toString());

        try {
            Media bgMusic = new Media(new File("assets/game/yoru_ni_kakeru.wav").toURI().toString());
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
        Drum drum = drumQueue.getFirst();

        int drumType = drum.getType();
        boolean hitMatched = false;
        if (drumType == 1 || drumType == 3) { // 紅音符、小紅或大紅
            if (type == 1) hitMatched = true;
        } else if (drumType == 2 || drumType == 4) { // 藍音符、小藍或大藍
            if (type == 2) hitMatched = true;
        }

        if (!hitMatched) return;

        double leftEdge = drum.getX();
        double rightEdge = drum.getX() + 64;

        double perfectLeft = (hitX - 96);
        double perfectRight = (hitX - 64);
        double goodLeft = (hitX - 135);
        double goodRight = (hitX + 45);

        if (leftEdge > goodRight + 32) {
            return; // Not in the hit zone
        }

        // 基礎分數
        int baseScore = 0;
        if (leftEdge >= perfectLeft && leftEdge <= perfectRight) {
            baseScore = (drumType <= 2) ? 300 : 600;  // 大音符分數*2
            drumQueue.removeFirst();
            lastJudgement = "Perfect";
            judgementDisplayTime = System.currentTimeMillis();
            combo++;
            score += baseScore + combo * 2;
        } else if (leftEdge >= goodLeft && rightEdge <= goodRight) {
            baseScore = (drumType <= 2) ? 100 : 200;
            drumQueue.removeFirst();
            lastJudgement = "Good";
            judgementDisplayTime = System.currentTimeMillis();
            combo++;
            score += baseScore + combo * 2;
        } else {
            drumQueue.removeFirst();
            lastJudgement = "Miss";
            judgementDisplayTime = System.currentTimeMillis();
            combo = 0;
            return;
        }

        if (combo > maxCombo) maxCombo = combo;
    }

    public void start() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!delayStarted) {
                    delayStarted = true;
                    delayStartTime = now;
                    return;
                }

                double delayElapsed = (now - delayStartTime) / 1e9;

                if (!gameStarted && delayElapsed >= offset) {
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
            }
        };
        timer.start();
    }

    private void spawnNotes() {
        while (currentNoteIndex < notes.size() && currentTime >= notes.get(currentNoteIndex).getTime()) {
            Note note = notes.get(currentNoteIndex);
            if (note.getType() != 0) {
                drumQueue.add(new Drum(note.getType(), 1280, 200));
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

        gc.setFill(Color.WHITE);
        gc.setFont(font);
        gc.fillText("" + score, 30, 210);

        if (combo > 10) {
            gc.setFill(Color.BLACK);
            gc.setFont(Font.loadFont(fontPath, 36));
            gc.fillText("" + combo, 230, 255);
        }

        if (!lastJudgement.isEmpty() && System.currentTimeMillis() - judgementDisplayTime < 800) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(36));
            gc.fillText(lastJudgement, hitX - 30, 180);
        }

        for (Drum drum : drumQueue) {
            Image img;
            switch (drum.getType()) {
                case 1: img = redNoteImage; break;
                case 2: img = blueNoteImage; break;
                case 3: img = redBigNoteImage; break;
                case 4: img = blueBigNoteImage; break;
                default: img = null;
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
