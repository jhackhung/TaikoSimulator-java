package main.main_game;

import java.io.*;
import java.util.*;

public class ChartParser {

    public static double bpm = 120.0;
    public static double offset = 0.0;

    public static void parse(String path, ArrayList<Note> notes, String course) throws IOException {

        course = course.trim().toLowerCase();
        double measure = 4.0, curSec = 0.0;
        boolean inCourse = false, started = false;

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String ln;
            while ((ln = br.readLine()) != null) {
                ln = ln.trim();
                if (ln.isEmpty() || ln.startsWith("//")) continue;

                if (ln.startsWith("OFFSET:")) offset = Double.parseDouble(ln.substring(7).trim());
                if (ln.startsWith("BPM:")) bpm = Double.parseDouble(ln.substring(4).trim());
                if (ln.startsWith("COURSE:")) {
                    inCourse = ln.substring(7).trim().equalsIgnoreCase(course);
                    started = false;
                    curSec = 0;
                    continue;
                }
                if (!inCourse) continue;

                if (ln.startsWith("#MEASURE")) {
                    String[] p = ln.substring(8).trim().split("/");
                    measure = Double.parseDouble(p[0]) / Double.parseDouble(p[1]);
                } else if (ln.startsWith("#BPMCHANGE")) bpm = Double.parseDouble(ln.substring(10).trim());
                else if (ln.startsWith("#DELAY")) curSec += Double.parseDouble(ln.substring(6).trim());
                else if (ln.equals("#START")) {
                    started = true;
                    continue;
                } else if (ln.equals("#END")) break;

                if (started && ln.endsWith(",")) {
                    String raw = ln.substring(0, ln.length() - 1);
                    String data = convert(raw);

                    double secPerBeat = 60d / bpm;
                    double secPerMeasure = measure * secPerBeat;
                    double interval = secPerMeasure / data.length();

                    for (int i = 0; i < data.length(); i++) {
                        char c = data.charAt(i);
                        int type = mapType(c);
                        if (type != 0) {
                            notes.add(new Note(type, curSec + i * interval));
                        }
                    }

                    curSec += secPerMeasure;
                    measure = 4.0;
                }
            }
        }

        for (Note n : notes) n.time -= offset;
    }

    private static int mapType(char c) {
        return switch (c) {
            case '1' -> 1;
            case '2' -> 2;
            case '3' -> 3;
            case '4' -> 4;
            default -> 0;
        };
    }

    private static String convert(String s) {
        StringBuilder out = new StringBuilder();
        boolean roll = false;
        for (char c : s.toCharArray()) {
            if (roll) {
                if (c == '0') out.append('0');
                else if (c == '8') {
                    out.append('1');
                    roll = false;
                }
                else out.append('0');
            } else {
                switch (c) {
                    case '0', '1', '2', '3', '4' -> out.append(c);
                    case '5' -> {
                        roll = true;
                        out.append('1');
                    }
                    case '6' -> out.append('1');
                    case '7' -> out.append('2');
                    default -> out.append('0');
                }
            }
        }
        return out.toString();
    }
}
