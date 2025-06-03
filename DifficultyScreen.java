import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DifficultyScreen extends JPanel implements KeyListener {
    private String[] levels = {"Easy", "Normal", "Hard"};
    private int selected = 0;
    private JFrame parent;
    private Image bgImage;

    public DifficultyScreen(JFrame frame) {
        this.parent = frame;
        this.setPreferredSize(new Dimension(800, 400));
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(this);
        this.bgImage = new ImageIcon(getClass().getResource("/resources/menu_bg.jpg")).getImage();
    }


    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);


        try {
            Font customFont = Font.createFont(Font.TRUETYPE_FONT,
                            getClass().getResourceAsStream("/resources/Taiko_No_Tatsujin_Official_Font.ttf"))
                    .deriveFont(Font.BOLD, 30);
            g.setFont(customFont);
        } catch (Exception ex) {
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 30)); // fallback
        }


        g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);

        g.setFont(new Font("Comic Sans MS", Font.BOLD, 28));
        for (int i = 0; i < levels.length; i++) {
            if (i == selected) {
                g.setColor(Color.DARK_GRAY);
                g.fillRoundRect(300, 100 + i * 80, 200, 50, 20, 20);
                g.setColor(Color.WHITE);
            } else {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRoundRect(300, 100 + i * 80, 200, 50, 20, 20);
                g.setColor(Color.BLACK);
            }
            g.drawString(levels[i], 350, 135 + i * 80);
        }


        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        g.setColor(Color.BLACK);
        g.drawString("Use W/S to choose difficulty", 250, 330);
        g.drawString("Press ENTER to confirm", 270, 355);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
            selected = (selected + 1) % levels.length;
            repaint();
        } else if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
            selected = (selected - 1 + levels.length) % levels.length;
            repaint();
        } else if (key == KeyEvent.VK_ENTER) {

            parent.setContentPane(new ResultScreen());
            parent.revalidate();
            parent.repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
}

