package main.main_game;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import javax.sound.sampled.*;
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
    private Image blueNoteImage = new Image("file:assets/game/blue_note.png");
    private Font font = Font.loadFont("file:assets/fonts/Taiko_No_Tatsujin_Official_Font.ttf", 24);

    private Clip redClip, blueClip;

    private double bpm = 120.0;
    private double offset = 3.0;
    private final double hitX = 450;
    private final double noteSpeed = 7.493;
    private final double noteRadius = 32;

    private long startNano;
    private long delayStartTime;
    private double currentTime = 0;
    private boolean delayStarted = false;
    private boolean gameStarted = false;
    private int currentNoteIndex = 0;

    public GameEngine() {
        root.getChildren().add(canvas);
        ChartParser.parse("assets/game/yoasobi.txt", notes);
        bpm = ChartParser.bpm;
        offset = ChartParser.offset;
        loadSounds();
        setupInput();
    }

    public Group getRoot() {
        return root;
    }

    private void loadSounds() {
        redClip = loadClip("assets/main/rednote.wav");
        blueClip = loadClip("assets/main/bluenote.wav");
    }

    private Clip loadClip(String path) {
        try {
            File audioFile = new File(path);
            AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupInput() {
        root.setOnKeyPressed((KeyEvent e) -> {
            if (!gameStarted) return;

            if (e.getCode() == KeyCode.F || e.getCode() == KeyCode.J) {
                playClip(redClip);
                checkHit(1);
            } else if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.K) {
                playClip(blueClip);
                checkHit(2);
            }
        });
    }

    private void playClip(Clip clip) {
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    private void checkHit(int type) {
        if (drumQueue.isEmpty()) return;
        Drum drum = drumQueue.get(0);
        double distance = Math.abs(drum.getX() + noteRadius - hitX);
        if (drum.getType() != type) return;
        if (distance <= 30) drumQueue.remove(0);
    }

    public void start() {
        startNano = System.nanoTime();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                currentTime = (now - startNano) / 1e9;

                if (!delayStarted) {
                    delayStarted = true;
                    delayStartTime = now;
                }

                if (!gameStarted && (now - delayStartTime) / 1e9 >= offset) {
                    gameStarted = true;
                }

                if (gameStarted) {
                    spawnNotes();
                    updateDrums();
                }

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

    private void updateDrums() {
        Iterator<Drum> iter = drumQueue.iterator();
        while (iter.hasNext()) {
            Drum drum = iter.next();
            drum.move(-noteSpeed);
            if (drum.getX() + 64 < hitX - 30) iter.remove();
        }
    }

    private void render() {
        gc.clearRect(0, 0, 1280, 720);
        gc.drawImage(bgImage, 0, 0);

        // draw judgement circle
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        double radius = 32;
        gc.strokeOval(hitX - radius, 200 + 55 - radius, radius * 2, radius * 2);

        for (Drum drum : drumQueue) {
            Image img = drum.getType() == 1 ? redNoteImage : blueNoteImage;
            gc.drawImage(img, drum.getX(), drum.getY());
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

        public int getType() { return type; }
        public double getX() { return x; }
        public double getY() { return y; }
        public void move(double dx) { x += dx; }
    }
}
