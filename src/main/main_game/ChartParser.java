package main.main_game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ChartParser {
    public static double bpm = 120.0;
    public static double offset = 3.0;

    public static void parse(String path, ArrayList<Note> notes) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            boolean inNote = false;
            double currentTime = 0.0;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("BPM:")) {
                    bpm = Double.parseDouble(line.substring(4));
                } else if (line.startsWith("OFFSET:")) {
                    offset = Double.parseDouble(line.substring(7));
                } else if (line.equals("#START")) {
                    inNote = true;
                } else if (line.equals("#END")) {
                    break;
                } else if (inNote) {
                    for (char c : line.toCharArray()) {
                        if (c == '1' || c == '2') {
                            int type = c - '0';
                            notes.add(new Note(type, currentTime));
                        }
                        currentTime += 60.0 / bpm / 4; // Assume 16th note grid
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
