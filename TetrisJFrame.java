package SwingTetris;

import javax.swing.*;

public class TetrisJFrame extends JFrame {
    public TetrisJFrame() {
        setTitle("Tetris - made by Jiyeon");
        setSize(650, 850);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 패널 추가
        TetrisPanel panel = new TetrisPanel();
        panel.setPreferredSize(new java.awt.Dimension(650, 800)); // 패널의 크기를 설정
        add(panel); // 패널 추가

        pack(); // JFrame의 크기를 패널에 맞게 조정
        setVisible(true);

        // 포커스 요청
        panel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TetrisJFrame());
    }
}
