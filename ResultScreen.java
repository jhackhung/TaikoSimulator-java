import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import javax.sound.sampled.*;
import java.io.*;

public class ResultScreen extends JPanel {

    private int score = 76845;
    private int great = 77;
    private int good = 40;
    private int miss = 4;
    private int maxCombo = 108;

    private Image bgImage;

    private Font customFont;

    public ResultScreen() {

        playResultMusic();
        this.setPreferredSize(new Dimension(800, 400));

        bgImage = new ImageIcon(getClass().getResource("/resources/result_bg.jpg")).getImage();

        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT,
                            getClass().getResourceAsStream("/resources/Taiko_No_Tatsujin_Official_Font.ttf"))
                    .deriveFont(Font.BOLD, 30);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (Exception ex) {
            ex.printStackTrace();
            customFont = new Font("Comic Sans MS", Font.BOLD, 30);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);


        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillRoundRect(180, 60, 500, 200, 30, 30);



        g2.setFont(new Font("Arial", Font.BOLD, 36));
        String scoreStr = String.valueOf(score);

        int sw = g2.getFontMetrics().stringWidth(scoreStr);

        drawOutlinedText(g2, scoreStr, (800 - sw) / 2, 100, Color.DARK_GRAY, Color.white, 3.0f);


        g2.setFont(customFont.deriveFont(Font.BOLD, 24));

        drawOutlinedText(g2, "Great", 220, 150, Color.RED, Color.white, 3f);
        drawOutlinedText(g2, String.valueOf(great), 230, 180, Color.RED, Color.white, 3f);

        drawOutlinedText(g2, "Good", 330, 150, Color.ORANGE, Color.white, 3f);
        drawOutlinedText(g2, String.valueOf(good), 340, 180, Color.ORANGE, Color.white, 3f);

        drawOutlinedText(g2, "Miss", 440, 150, Color.BLUE, Color.white, 3f);
        drawOutlinedText(g2, String.valueOf(miss), 450, 180, Color.BLUE, Color.white, 3f);

        drawOutlinedText(g2, "Max Combo", 520, 150, Color.GREEN.darker(), Color.white, 3f);
        drawOutlinedText(g2, String.valueOf(maxCombo), 550, 180, Color.GREEN.darker(), Color.white, 3f);
    }


    private void drawOutlinedText(Graphics2D g2, String text, float x, float y, Color fillColor, Color outlineColor, float strokeWidth) {
        FontRenderContext frc = g2.getFontRenderContext();
        GlyphVector gv = g2.getFont().createGlyphVector(frc, text);
        Shape textShape = gv.getOutline(x, y);

        g2.setStroke(new BasicStroke(strokeWidth));
        g2.setColor(outlineColor);
        g2.draw(textShape);

        g2.setColor(fillColor);
        g2.fill(textShape);
    }

    private void playResultMusic() {
        try {

            InputStream in = getClass().getResourceAsStream("/resources/Anata_ni_Koi_Result.wav");
            if (in == null) {
                System.out.println("播放結果音樂失敗：無法找到 /resources/Anata_ni_Koi_Result.wav");
                return;
            }
            BufferedInputStream bin = new BufferedInputStream(in);
            AudioInputStream originalAudio = AudioSystem.getAudioInputStream(bin);
            AudioFormat sourceFormat = originalAudio.getFormat();

            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    16,
                    sourceFormat.getChannels(),
                    sourceFormat.getChannels() * 2,
                    sourceFormat.getSampleRate(),
                    false
            );

            AudioInputStream audio = AudioSystem.getAudioInputStream(targetFormat, originalAudio);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception e) {
            System.out.println("播放結果音樂失敗：" + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Result Screen");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new ResultScreen());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}



