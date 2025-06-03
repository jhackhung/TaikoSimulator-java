import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.*;

public class MenuScreen extends JPanel implements KeyListener {
    private String[] songs = {"Yoru ni Kakeru", "Zen Zen Zense", "Zenryoku Shounen"};
    private int selectedIndex = 0;
    private Clip clip;
    private JFrame parent;
    private Image bgImage;

    public MenuScreen(JFrame frame) {
        this.parent = frame;
        this.setPreferredSize(new Dimension(800, 400));
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(this);

        bgImage = new ImageIcon(getClass().getResource("/resources/menu_bg.jpg")).getImage();

        playPreview(selectedIndex);
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


        try {
            Font customFont = Font.createFont(Font.TRUETYPE_FONT,
                            getClass().getResourceAsStream("/resources/Taiko_No_Tatsujin_Official_Font.ttf"))
                    .deriveFont(Font.BOLD, 30);
            g.setFont(customFont);
        } catch (Exception ex) {
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 30));
        }


        g.setFont(new Font("Comic Sans MS", Font.BOLD, 28));
        for (int i = 0; i < songs.length; i++) {
            if (i == selectedIndex) {
                g.setColor(Color.DARK_GRAY);
                g.fillRoundRect(280, 100 + i * 80, 290, 50, 20, 20);
                g.setColor(Color.WHITE);
            } else {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRoundRect(280, 100 + i * 80, 290, 50, 20, 20);
                g.setColor(Color.BLACK);
            }
            g.drawString(songs[i], 310, 135 + i * 80);
        }


        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        g.setColor(Color.BLACK);
        g.drawString("Use W/S to select songs", 270, 350);
        g.drawString("Press ENTER to start", 290, 375);
    }


    private void playPreview(int index) {

        stopPreview();


        String file = switch (index) {
            case 0 -> "yorunikakeru.wav";
            case 1 -> "zenzenzense.wav";
            case 2 -> "zenryokushounen.wav";
            default -> null;
        };

        if (file != null) {
            try {

                InputStream in = getClass().getResourceAsStream("/resources/" + file);
                if (in == null) {
                    System.out.println("資源未找到：/resources/" + file);
                    return;
                }
                BufferedInputStream bin = new BufferedInputStream(in);
                AudioInputStream audio = AudioSystem.getAudioInputStream(bin);
                clip = AudioSystem.getClip();
                clip.open(audio);


                long chorusPosition = switch (index) {
                    case 0 -> 10_000_000L;
                    case 1 -> 12_000_000L;
                    case 2 -> 11_000_000L;
                    default -> 0L;
                };
                clip.setMicrosecondPosition(chorusPosition);
                clip.start();
            } catch (Exception e) {
                System.out.println("播放副歌音效失敗：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void stopPreview() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
            selectedIndex = (selectedIndex + 1) % songs.length;
            playPreview(selectedIndex);
            repaint();
        } else if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
            selectedIndex = (selectedIndex - 1 + songs.length) % songs.length;
            playPreview(selectedIndex);
            repaint();
        } else if (key == KeyEvent.VK_ENTER) {
            stopPreview();

            parent.setContentPane(new DifficultyScreen(parent));
            parent.revalidate();
            parent.repaint();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}

