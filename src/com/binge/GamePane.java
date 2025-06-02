package com.binge;

import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.File;
import java.util.*;

public class GamePane extends Pane {

    private final Image rS = new Image("file:resources/img/red_small.png");
    private final Image bS = new Image("file:resources/img/blue_small.png");
    private final Image rL = new Image("file:resources/img/red_big.png");
    private final Image bL = new Image("file:resources/img/blue_big.png");

    private static final double AHEAD = 2.5;
    private static final double HIT_R = 0.20;
    private static final double HIT_D = 60;

    private double pxPerSec;
    private boolean ready = false;
    private Circle guide;

    private final List<NoteData> chart;
    private final List<Note> active = new ArrayList<>();
    private int idx = 0;

    private final MediaPlayer mp;

    public GamePane(List<NoteData> chart, String songName) {
        this.chart = chart;
        mp = new MediaPlayer(new Media(new File(songName + ".wav").toURI().toString()));
        mp.play();
        loop();
    }

    @Override protected void layoutChildren() {
        super.layoutChildren();
        double w = getWidth(), h = getHeight();
        double hitX = w * HIT_R, hitY = h / 2 + HIT_D;
        pxPerSec = (w - hitX) / AHEAD;

        if (guide == null) {
            guide = new Circle(hitX, hitY, 35, Color.TRANSPARENT);
            guide.setStroke(Color.RED); guide.setStrokeWidth(3);
            getChildren().add(guide);
        } else { guide.setCenterX(hitX); guide.setCenterY(hitY); }
        ready = true;
    }

    private void loop() {
        new AnimationTimer() {
            @Override public void handle(long now) {
                if (!ready) return;

                double t    = mp.getCurrentTime().toSeconds();
                double w    = getWidth();
                double hitX = w * HIT_R;
                double hitY = getHeight() / 2 + HIT_D;

                while (idx < chart.size() && chart.get(idx).timestamp <= t + AHEAD) {
                    NoteData d = chart.get(idx++);
                    if (d.timestamp >= t) spawn(d, w, hitY);    // 右緣出場
                }

                Iterator<Note> it = active.iterator();
                while (it.hasNext()) {
                    Note n = it.next();
                    double dt = n.data.timestamp - t;
                    double x  = hitX + dt * pxPerSec;
                    n.imageView.setX(x);
                    if (x < -120) { getChildren().remove(n.imageView); it.remove(); }
                }
            }
        }.start();
    }

    private void spawn(NoteData d, double startX, double y) {
        Image img = switch (d.type) {
            case '1' -> rS; case '2' -> bS; case '3' -> rL; case '4' -> bL; default -> null;
        };
        if (img == null) return;

        Note n = new Note(img, d);
        n.imageView.setX(startX);        // 從右緣開始
        n.imageView.setY(y);
        active.add(n);
        getChildren().add(n.imageView);
    }
}