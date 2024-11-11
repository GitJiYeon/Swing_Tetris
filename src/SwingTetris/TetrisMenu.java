package SwingTetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class TetrisMenu extends JFrame {

    private JPanel menuPanel;
    private JButton startButton;
    private TetrisPanel game;
    private Image backgroundImage; // 배경 이미지 저장

    public TetrisMenu() {
        setTitle("Tetris");
        setSize(650, 850);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 배경 이미지 로드
        try {
            backgroundImage = ImageIO.read(new File("./src/images/tetrisMenu.png"));
        } catch (IOException e) {
            e.printStackTrace(); // 이미지 로드 실패 시 예외 처리
        }

        menuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 배경 이미지 그리기
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        menuPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(450, 0, 10, 0);
        startButton = new JButton("START");
        startButton.setFont(new Font("Arial", Font.PLAIN, 18));
        startButton.setPreferredSize(new Dimension(180, 50));
        gbc.gridy = 1;
        menuPanel.add(startButton, gbc);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        add(menuPanel, BorderLayout.CENTER);
    }

    private void startGame() {
        menuPanel.setVisible(false);
        game = new TetrisPanel();
        add(game, BorderLayout.CENTER);
        game.requestFocusInWindow();
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TetrisMenu menu = new TetrisMenu();
            menu.setVisible(true);
        });
    }
}
