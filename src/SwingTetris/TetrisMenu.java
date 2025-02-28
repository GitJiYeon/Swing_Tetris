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
	private JButton startButton, attackModeButton, bugModeButton;
	JButton diffButton; // 난이도 설정 버튼
	private TetrisPanel game;
	private Image backgroundImage;

	public TetrisMenu() {
		setTitle("Tetress - made by Jiyeon");
		setSize(650, 850);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		// 배경 이미지 로드
		try {
			backgroundImage = ImageIO.read(new File("./src/images/tetrisMenu.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		menuPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (backgroundImage != null) {
					g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
				}
			}
		};
		menuPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.insets = new Insets(20, 0, 20, 0);

		// START 버튼
		startButton = new JButton("Nomal Mode");
		startButton.setFont(new Font("Arial", Font.PLAIN, 18));
		startButton.setPreferredSize(new Dimension(180, 50));
		gbc.gridy = 0;
		menuPanel.add(startButton, gbc);

		// attacmode 버튼
		attackModeButton = new JButton("Attack Mode");
		attackModeButton.setFont(new Font("Arial", Font.PLAIN, 18));
		attackModeButton.setPreferredSize(new Dimension(180, 50));
		gbc.gridy = 1;
		menuPanel.add(attackModeButton, gbc);

		// bugMod 버튼
		bugModeButton = new JButton("Bug Mode");
		bugModeButton.setFont(new Font("Arial", Font.PLAIN, 18));
		bugModeButton.setPreferredSize(new Dimension(180, 50));
		gbc.gridy = 2;
		menuPanel.add(bugModeButton, gbc);

		// 버튼 이벤트 추가
		startButton.addActionListener(e -> {
			TetrisPanel.bugModOnOff = false;
			TetrisPanel.attackModeOnOff = false;
			startGame();
		});
		attackModeButton.addActionListener(e -> {
			TetrisPanel.bugModOnOff = false;
			TetrisPanel.attackModeOnOff = true;
			startGame();
		});
		bugModeButton.addActionListener(e -> {
			TetrisPanel.attackModeOnOff = false;
			TetrisPanel.bugModOnOff = true;
			
			startGame();
		});

		
		// 난이도 설정 버튼
		diffButton = new JButton("1");
		diffButton.setBackground(Color.GREEN);
		diffButton.setPreferredSize(new Dimension(50, 50)); // 버튼 크기 설정

		// GridBagConstraints 설정
		gbc.gridx = 1; // attackModeButton 오른쪽에 배치
		gbc.gridy = 1; // attackModeButton과 같은 줄
		gbc.insets = new Insets(0, 10, 0, 0); // 간격 조정

		menuPanel.add(diffButton, gbc);
		
		diffButton.addActionListener(new ActionListener() {// 난이도 설정 버튼
			@Override
			public void actionPerformed(ActionEvent e) {
				String currentText = diffButton.getText(); // 현재 텍스트를 변수에 저장
				int nextNumber = Integer.parseInt(currentText) % 4 + 1; // 1, 2, 3, 4 순환
				diffButton.setText(String.valueOf(nextNumber)); // 클릭마다 바꾸기

				if (nextNumber == 1) { // 난이도 1
					diffButton.setBackground(Color.GREEN); //색상 초록
					diffButton.setForeground(Color.BLACK); //글씨 검정
					TetrisPanel.attackInterval = 12000; // 방해줄 12초마다
					TetrisPanel.placeBlockTimeCount = 1200; // 블럭이 바닥에 닿는 시간
				} else if (nextNumber == 2) { // 난이도 2
					diffButton.setBackground(Color.YELLOW); //색상 노랑
					diffButton.setForeground(Color.BLACK); //글씨 검정
					TetrisPanel.attackInterval = 9000; // 방해즐 9초마다
					TetrisPanel.placeBlockTimeCount = 1150; // 블럭이 바닥에 닿는 시간
				} else if (nextNumber == 3) { // 난이도 3
					diffButton.setBackground(Color.RED); //색상 빨강
					diffButton.setForeground(Color.BLACK); // 글씨 검정
					TetrisPanel.attackInterval = 5000; // 방해줄 5초마다
					TetrisPanel.placeBlockTimeCount = 1100; // 블럭이 바닥에 닿는 시간
				} else if (nextNumber == 4) { 
					diffButton.setBackground(Color.BLACK); //색상 블랙
					diffButton.setForeground(Color.WHITE); //글씨 흰색
					TetrisPanel.attackInterval = 1500; // 방해줄 1.5초마다
					TetrisPanel.placeBlockTimeCount = 900; // 블럭이 바닥에 닿는 시간
				}
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
