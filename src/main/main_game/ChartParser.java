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

                if (line.isEmpty()) continue;

                if (line.startsWith("BPM:")) {
                    bpm = Double.parseDouble(line.substring(4).trim());
                    System.out.println("BPM set to: " + bpm);
                } else if (line.startsWith("OFFSET:")) {
                    offset = Double.parseDouble(line.substring(7).trim());
                    System.out.println("Offset set to: " + offset);
                } else if (line.equals("#START")) {
                    inNote = true;
                    currentTime = 0.0;
                } else if (line.equals("#END")) {
                    break;
                } else if (inNote) {
                    // 拆成多個小節（以逗號為單位），每個小節必須獨立處理
                    String[] bars = line.split(",");

                    if (line.startsWith("#")) {
                        // 如果是註解行，則跳過
                        continue;
                    }

                    if (line.startsWith(",") && bars.length == 0) {
                        // 每一拍的秒數
                        double beatTime = 60.0 / bpm;
                        // 每一小節為 4 拍
                        double barTime = 4 * beatTime;

                        // 空小節也需計入時間
                        currentTime += barTime;
                        continue;

                    }

                    for (String bar : bars) {
                        bar = bar.trim();
                        int length = bar.length();

                        // 每一拍的秒數
                        double beatTime = 60.0 / bpm;
                        // 每一小節為 4 拍
                        double barTime = 4 * beatTime;

                        if (length == 0) {
                            // 空小節也需計入時間
                            currentTime += barTime;
                            continue;
                        }

                        double interval = barTime / length;

                        for (int i = 0; i < length; i++) {
                            char c = bar.charAt(i);
                            int type = c - '0';

                            if (type == 1 || type == 2 || type == 3 || type == 4) {
                                notes.add(new Note(type, currentTime));
                            }

                            currentTime += interval;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
