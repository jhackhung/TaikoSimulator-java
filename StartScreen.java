import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import javax.sound.sampled.*;
import java.io.*;

public class StartScreen extends JPanel implements MouseListener, KeyListener {
    private JFrame parentFrame;
    private Image bgImage;
    private boolean transitioning = false;
    private Font customFont;

    public StartScreen(JFrame frame) {
        this.parentFrame = frame;
        this.setPreferredSize(new Dimension(800, 400));
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        this.addMouseListener(this);
        this.addKeyListener(this);

        bgImage = new ImageIcon(getClass().getResource("/resources/start_bg.jpg")).getImage();

        setBorder(null);


        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT,
                            getClass().getResourceAsStream("/resources/Taiko_No_Tatsujin_Official_Font.ttf"))
                    .deriveFont(Font.BOLD, 50);
        } catch(Exception ex) {
            ex.printStackTrace();
            customFont = new Font("Comic Sans MS", Font.BOLD, 50);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


        g2.setFont(customFont);

        drawOutlinedText(g2, "Taiko Simulator", 220, 150, Color.RED, Color.white, 7f);


        g2.setFont(new Font("Comic Sans MS", Font.BOLD, 30));
        drawShadowText(g2, "Click or Press Enter to Start", 190, 300, Color.BLUE, Color.white, 2, 2);
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


    private void drawShadowText(Graphics2D g2, String text, int x, int y, Color fillColor, Color shadowColor, int offsetX, int offsetY) {

        g2.setColor(shadowColor);
        g2.drawString(text, x + offsetX, y + offsetY);

        g2.setColor(fillColor);
        g2.drawString(text, x, y);
    }

    private void playEnterSound(Runnable callback) {
        try {

            InputStream in = getClass().getResourceAsStream("/resources/v_title.wav");
            if (in == null) {
                System.out.println("資源未找到: /resources/v_title.wav");
                if (callback != null) callback.run();
                return;
            }
            BufferedInputStream bin = new BufferedInputStream(in);
            AudioInputStream audio = AudioSystem.getAudioInputStream(bin);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    SwingUtilities.invokeLater(callback);
                }
            });
            clip.start();
        } catch (Exception e) {
            System.out.println("音效播放失敗：" + e.getMessage());
            e.printStackTrace();
            if (callback != null) callback.run();
        }
    }


    private void goToMenu() {
        if (transitioning) return;
        transitioning = true;
        playEnterSound(() -> {
            parentFrame.setContentPane(new MenuScreen(parentFrame)); // MenuScreen 請自行實作
            parentFrame.revalidate();
            parentFrame.repaint();
        });
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            goToMenu();
        }
    }
    @Override public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            goToMenu();
        }
    }
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            goToMenu();
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Taiko Simulator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new StartScreen(frame));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}



