package SwingTetris;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class setting extends JFrame{
	private JPanel menuPanel;
    private JButton settingButton;
    private TetrisPanel game;
    setting(){
    	setTitle("Tetris");
        setSize(650, 850);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

}
