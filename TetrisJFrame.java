package SwingTetris;

import javax.swing.JFrame;

public class TetrisJFrame extends JFrame {
    public TetrisJFrame() {
        setTitle("Tetris");
        setSize(650, 850);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //패널 추가
        TetrisPanel panel = new TetrisPanel();
        add(panel);
        setVisible(true);
        //포커스 ㅐ=ㅍㄶ
        panel.requestFocusInWindow();
    }
}
