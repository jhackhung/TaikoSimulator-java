package main.main_game;

public class Note {
    private final int type; // 1 = red, 2 = blue
    double time;

    public Note(int type, double time) {
        this.type = type;
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public double getTime() {
        return time;
    }
}

