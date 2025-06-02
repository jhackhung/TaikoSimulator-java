package com.binge;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Note {
    public final ImageView imageView;
    public final NoteData data;

    public Note(Image image, NoteData data) {
        this.imageView = new ImageView(image);
        this.data      = data;
    }
}