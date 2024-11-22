package SwingTetris;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class TetrisPanel extends JPanel implements Runnable {
	int row = 20;// ㅅㄹ
	int col = 10;// ㄱㄹ
	int[][] grid = new int[row][col]; // 맵 사이즈
	int gridSize = 30;// 한칸 사이즈
	int nowBlockY = 0; // 블록이 있는 위치
	int nowBlockX = col / 2; // 블록이 있는 위치(중앙 위)
	int TimeTurn;

	int[][] holdBlock;
	int[][] holdgrid = new int[4][4];
	int goldgridSize = 30;

	Color HoldBlockColor;
	Block nowBlock;
	Random random = new Random(); // 랜덤 블록 생성을 위한 랜덤 객체

	private static final String HIGH_SCORE_FILE = "highscore.txt"; // 최고 기록 저장 파일 이름
	private int highScore = 0; // 최고 기록 변수

	int Score = 0;
	int tetrisCount = 0;
	int yummyCount = 0;

	// 블록 모양
	int[][] blockI = { { 1, 1, 1, 1 } };
	int[][] blockT = { { 0, 1, 0 }, { 1, 1, 1 } };
	int[][] blockO = { { 1, 1 }, { 1, 1 } };
	int[][] blockL = { { 1, 1 }, { 0, 1 }, { 0, 1 } };
	int[][] blockJ = { { 1, 1 }, { 1, 0 }, { 1, 0 } };
	int[][] blockZ = { { 1, 1, 0 }, { 0, 1, 1 } };
	int[][] blockS = { { 0, 1, 1 }, { 1, 1, 0 } };

	Block[] blocks = { new Block(blockI, Color.decode("#74c4f2")), new Block(blockT, Color.decode("#db48cf")),
			new Block(blockO, Color.decode("#f7d136")), new Block(blockL, Color.decode("#f79736")),
			new Block(blockJ, Color.decode("#3640f7")), new Block(blockZ, Color.decode("#f73636")),
			new Block(blockS, Color.decode("#55d941")) };

	private List<Block> blockBag = new ArrayList<>(); // 블록 가방-
	
	private JButton restartButton1; // 재시작 버튼
	private JButton diffButton; // 난이도 설정 버튼
	private JButton BGMbutton; // BGM 버튼 
	private JButton attackModeButton; // 공격 모드 버튼
	private JButton bugButton; // 버그 모드 버튼 
	
	private JButton settingButton; // 설정 버튼 추가
	boolean ButtonOnOff = false; // 설정 on/off
	boolean BGMon = true; // BGM on/off
	boolean attackModeOnOff = false; // 공격 모드 on/off
	boolean bugModOnOff = false; // 버그 모드 on/off
	
	long lastAttackTime = 0; // 마지막 공격 발생 시간 저장
	int attackInterval = 10000; // 15초 간격으로 공격

	boolean gameoverSound; // 게임 오버 사운드가 한 번만 실행되도록 하는 변수
	boolean eatingSound = true; // 블록 먹는 소리 한번만 재생
	private boolean gameOver = false; // 게임 오버 상태

	private JButton restartButton; // 재시작 버튼 추가
	
	private Thread gameThread; // 게임 루프를 실행할 스레드
	private boolean running = false; // 게임 루프 실행 상태
	private boolean holdUsed = false; // 한 턴에 한 번만 홀드 사용 가능하도록 플래그 변수 추가
	boolean isPaused = false; // 게임 일시 정지 여부 확인 변수
	
	int placeBlockTimeCount = 1200; //블록이 바닥에 닿고 고정되기까지 걸리는 시간

	//스코어 엠블럼 이미지
	private Image score100Image;
	private Image score300Image;
	private Image score500Image;
	private Image score1000Image;
	

	public TetrisPanel() {
		TimeTurn = 600; // 재생 속도 (블럭이 떨어지는 속도)
		setFocusable(true); //포커스가 보이도록
		requestFocusInWindow(); // 포커스를 패널로 가져오기

		fillBlockBag(); // 초기 블록 가방을 채우는 메서드
		nowBlock = getNextBlock(); // 다음 블럭을 현재 블럭으로 가져오기

		// KeyBindings 설정
		setKeyBindings();

		loadHighScore(); // 최고 기록 불러오기
		
		
		//==============================================================================================================     버튼 설정
		restartButton = new JButton("Restart"); //게임 오버 시 보이는 버튼
		restartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				restartGame();
			}
		});
		
		setLayout(null); // null 레이아웃 사용

		settingButton = new JButton("Setting"); // 왼쪽 상단 설정 버튼
		settingButton.setBackground(Color.WHITE);
		settingButton.setBounds(50, 10, 100, 25);
		add(settingButton);

		restartButton1 = new JButton("Restart"); // 왼쪽 상단 재시작 버튼
		restartButton1.setBackground(Color.WHITE);
		restartButton1.setBounds(50, 40, 100, 25); 
		add(restartButton1);
		
		BGMbutton = new JButton("BGM"); // 설정 BGM 전원 버튼
		BGMbutton.setBackground(Color.WHITE);
		BGMbutton.setForeground(Color.BLACK);
		BGMbutton.setBounds(245, 15, 60, 25); 
		BGMbutton.setVisible(false); // 처음에는 숨김 상태
		add(BGMbutton);
		
		bugButton = new JButton("Hungry BUG🍗"); // 배고픈 벌레(?) 모드
		bugButton.setBackground(Color.WHITE);
		bugButton.setBounds(310, 15, 120, 25); 
		bugButton.setVisible(false); // 처음에는 숨김 상태
		add(bugButton);

		attackModeButton = new JButton("Attack Mode"); // 공격 모드
		attackModeButton.setBackground(Color.WHITE);
		attackModeButton.setBounds(100, 20, 90, 30); 
		attackModeButton.setVisible(false); // 처음에는 숨김 상태
		add(attackModeButton);

		diffButton = new JButton("1"); // 공격모드 난이도 버튼
		diffButton.setBackground(Color.GREEN); // 초기 색상 초록
		diffButton.setBounds(360, 50, 50, 30); // attackMode 버튼 옆에 위치
		diffButton.setVisible(false); // 처음에는 숨김 상태
		
		add(diffButton);
		//==========================================================================================================      버튼 리스너
		restartButton1.addActionListener(new ActionListener() { // 왼쪽 상단 재시작버튼 
			@Override
			public void actionPerformed(ActionEvent e) {
				restartGame(); // 게임 재시작
			}
		});
		
		settingButton.addActionListener(new ActionListener() { // 설정 버튼
			@Override
			public void actionPerformed(ActionEvent e) { 
				ButtonOnOff = !ButtonOnOff;
				if (ButtonOnOff) { // 켜짐
					isPaused = true; // 일시정지
					SoundPlayer.stopBGM(); // 브금 종료
					settingButton.setText("Back"); // 글씨를 Back으로 변경
					settingButton.setBackground(Color.GRAY); // 버튼 색상 회색으로 변경
					attackModeButton.setVisible(true); // Attack Mode 버튼 보이기
					diffButton.setVisible(true); // diffButton 보이기
					BGMbutton.setVisible(true); // BGMbutton 보이기
					bugButton.setVisible(true); // bugButton 보이기
					attackModeButton.setBounds(getWidth() / 2 - 80, 50, 110, 30);

				} else { //꺼짐
					isPaused = false; //일시정지 끄기
					if(BGMon) { SoundPlayer.playBGM("./src/sounds/tetrisBGM.wav");} // 브금 설정이 켜져있으면 브금 실행
					requestFocusInWindow(); //포커스를 화면으로
					settingButton.setText("Setting"); //글씨를 Setting으로 	변경
					settingButton.setBackground(Color.WHITE); // 버튼 색상 흰색으로 변견
					attackModeButton.setVisible(false); // Attack Mode 버튼 숨기기
					diffButton.setVisible(false); // diffButton 숨기기
					BGMbutton.setVisible(false); // BGMbutton 숨기기
					bugButton.setVisible(false); // bugButton 숨기기

				}
			}
		});
		//============= 설정 누르면 나오는 버튼들 =============
		diffButton.addActionListener(new ActionListener() {// 난이도 설정 버튼
			@Override
			public void actionPerformed(ActionEvent e) {
				String currentText = diffButton.getText(); // 현재 텍스트를 변수에 저장
				int nextNumber = Integer.parseInt(currentText) % 4 + 1; // 1, 2, 3, 4 순환
				diffButton.setText(String.valueOf(nextNumber)); // 클릭마다 바꾸기

				if (nextNumber == 1) { // 난이도 1
					diffButton.setBackground(Color.GREEN); //색상 초록
					diffButton.setForeground(Color.BLACK); //글씨 검정
					attackInterval = 12000; // 방해줄 12초마다
					placeBlockTimeCount = 1200; // 블럭이 바닥에 닿는 시간
				} else if (nextNumber == 2) { // 난이도 2
					diffButton.setBackground(Color.YELLOW); //색상 노랑
					diffButton.setForeground(Color.BLACK); //글씨 검정
					attackInterval = 9000; // 방해즐 9초마다
					placeBlockTimeCount = 1150; // 블럭이 바닥에 닿는 시간
				} else if (nextNumber == 3) { // 난이도 3
					diffButton.setBackground(Color.RED); //색상 빨강
					diffButton.setForeground(Color.BLACK); // 글씨 검정
					attackInterval = 5000; // 방해줄 5초마다
					placeBlockTimeCount = 1100; // 블럭이 바닥에 닿는 시간
				} else if (nextNumber == 4) { 
					diffButton.setBackground(Color.BLACK); //색상 블랙
					diffButton.setForeground(Color.WHITE); //글씨 흰색
					attackInterval = 1500; // 방해줄 1.5초마다
					placeBlockTimeCount = 900; // 블럭이 바닥에 닿는 시간
				}
			}
		});
		attackModeButton.addActionListener(new ActionListener() { // 공격 모드
			@Override
			public void actionPerformed(ActionEvent e) {
				attackModeOnOff = !attackModeOnOff;
				if (attackModeOnOff) { // 켜짐
					attackmode(); // 모드 실행
					attackModeButton.setBackground(Color.GRAY); // 버튼 색상 회색
				} else { // 꺼짐
					attackModeButton.setBackground(Color.WHITE); // 버튼 색상 흰색
					placeBlockTimeCount = 1200; // 블럭 놓이는 시간 초기화
				}
			}
		});

		BGMbutton.addActionListener(new ActionListener() { // BGMonOff
					@Override
					public void actionPerformed(ActionEvent e) {
						BGMon = !BGMon;
						if (BGMon) { // 켜짐
							BGMbutton.setBackground(Color.WHITE); // 버튼 색상 흰색
							BGMbutton.setForeground(Color.BLACK); // 글씨 검정색
						} else { // 꺼짐
							BGMbutton.setBackground(Color.GRAY); // 버튼 색상 회색
							BGMbutton.setForeground(Color.WHITE); // 글씨 검정색
						}
					}
				});
		
		bugButton.addActionListener(new ActionListener() { // 버그 모드
					@Override
					public void actionPerformed(ActionEvent e) { 
						bugModOnOff = !bugModOnOff;
						if (bugModOnOff) { // 켜짐
							bugButton.setBackground(Color.GRAY);
							bugButton.setForeground(Color.WHITE);
						} else { // 꺼짐
							bugButton.setBackground(Color.WHITE);
							bugButton.setForeground(Color.BLACK);
						}
					}
				});

		gameThread = new Thread(this);
		startGame();// 게임 시작 
		
		this.requestFocusInWindow();
	}
	//===============================================================================================================   파일처리

	private void loadHighScore() { //최고기록 불러오는 메서드
		File file = new File(HIGH_SCORE_FILE);
		if (!file.exists()) {
			saveHighScore(0); // 파일이 없으면 최고 기록 파일을 만들고 기본값 저장
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
			String line = reader.readLine();
			if (line != null) {
				highScore = Integer.parseInt(line);
			}
		} catch (IOException | NumberFormatException e) {
			e.printStackTrace();
		}
	}

	private void saveHighScore(int score) { // 최고 기록을 파일에 저장하는 메서드
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
			writer.write(String.valueOf(score));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void checkAndSaveHighScore() { // 최고기록 갱신
		if (Score > highScore) {
			highScore = Score;
			saveHighScore(highScore);
		}
	}

	private void fillBlockBag() { // 블록 가방 채우는 메서드
		blockBag.clear();
		List<Block> tempBlocks = new ArrayList<>();
		for (Block block : blocks) {
			tempBlocks.add(new Block(block.getShape(), block.getColor()));
		}
		Collections.shuffle(tempBlocks);
		blockBag.addAll(tempBlocks);
	}

	private Block getNextBlock() { //다음 블럭 가져오는 메서드
		if (blockBag.isEmpty()) {
			fillBlockBag(); // 가방이 비었으면 다시 채우기
		}
		return blockBag.remove(0);
	}
	
	
	//==================================================================================================================   키 처리
	private void setKeyBindings() {
		// 왼쪽 화살표 키
		this.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
		this.getActionMap().put("moveLeft", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!gameOver && canMove(nowBlockY, nowBlockX - 1)) {
					nowBlockX--; //현재 블럭의 X값 --
					repaint(); // 그림 새로고침
				}
			}
		});

		// 오른쪽 화살표 키
		this.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");
		this.getActionMap().put("moveRight", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!gameOver && canMove(nowBlockY, nowBlockX + 1)) {
					nowBlockX++; // 현재 블럭의 y값 ++
					repaint(); // 그림 새로고침
				}
			}
		});

		// 아래쪽 화살표 키
		this.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
		this.getActionMap().put("moveDown", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!gameOver) {
					BlockDown(); // 빠르게 떨어뜨리기 
					repaint(); // 그림 새로고침
				}
			}
		});

		
		// 위쪽 화살표(회전)
		this.getInputMap().put(KeyStroke.getKeyStroke("UP"), "rotate");
		this.getActionMap().put("rotate", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!gameOver) {
					if(bugModOnOff) { bugRotateBlock(); } // 버그 모드가 켜져있으면 버그모드 회전 
					else { rotateBlock(); } // 버그 모드가 꺼져있으면 시계방향 회전
					repaint(); // 그림 새로고침
				}
			}
		});
		
		// z 키 (회전)
		this.getInputMap().put(KeyStroke.getKeyStroke("Z"), "counterclockwiserotate");
		this.getActionMap().put("counterclockwiserotate", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!gameOver) {
					if(bugModOnOff) { SoundPlayer.playSound("./src/sounds/dontEat.wav");}// 버그 모드가 켜져있으면 효과음 
					counterclockwiserotateBlock(); // 반시계방향 회전
					repaint(); // 그림 새로고침
				}
			}
		});

		// 스페이스 바 (하드 드롭)
		this.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "hardDrop");
		this.getActionMap().put("hardDrop", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!gameOver) {
					SoundPlayer.playSound("./src/sounds/block_place.wav");// 블록 놓는 효과음
					hardDrop(); // 하드드롭
					repaint(); // 그림 새로고침
				}
			}
		});

		// 쉬프트 (홀드)
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK), "Hold");
		this.getActionMap().put("Hold", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[][] shape = nowBlock.getShape(); //련재 블록의 shape을 변수에 저장(2차원 배열
				if (!gameOver) {
					Hold(); // 홀드
					repaint(); // 그림 새로고침
				}
			}
		});
		
		//d 홀드 (위와 같음)
		this.getInputMap().put(KeyStroke.getKeyStroke("D"), "DHold");
		this.getActionMap().put("DHold", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[][] shape = nowBlock.getShape();
				if (!gameOver) {
					Hold();
					repaint();
				}
			}
		});
	}
	
	
			
	//================================================================================================================================  그래픽
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		fill(g); //10 * 20 그리드 그리기
		fillHold(g); // 홀드 그리드 그리기
		fillNext(g); // 넥스트 그리드 그리기
		fillScore(g); // 스코어 글자 표시
		tetrisClear(g); // 테트리스 글자 표시
		stop(g); // 일시정지 글자 표시
		
		// 배경 이미지 그리기
		if (!gameOver) {
			int ghostRow = findGhostRow(); //고스트 블록의 X좌표 저장
			int[][] shape = nowBlock.getShape(); //고스트 블록의 모양 저장(2차원 배열
			int startX = (getWidth() - col * gridSize) / 2; //고스트 블록 시작 위치를 그리드 중앙으로
			int startY = (getHeight() - row * gridSize) / 2;

			Color currentColor = nowBlock.getColor(); //현재 블럭의 색상 저장
			Color GostBlockColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 90);// R, G, B, 투명도값 0 ~ 225
			g.setColor(GostBlockColor); //고스트블록 색 채우기

			for (int r = 0; r < shape.length; r++) { //블럭의 X
				for (int c = 0; c < shape[r].length; c++) { //블럭의 Y
					if (shape[r][c] == 1) { //현재 모양 범위의 그리드 위치값을 1로
						g.fillRect(startX + (nowBlockX + c) * gridSize, startY + (ghostRow + r) * gridSize, gridSize, gridSize);
						// 유령블록 테투리
						g.setColor(Color.YELLOW);
						g.drawRect(startX + (nowBlockX + c) * gridSize, startY + (ghostRow + r) * gridSize, gridSize, gridSize); 
						g.setColor(GostBlockColor);
					}
				}
			}
		}
		
		
		
		if (!gameOver) { // 게임 오버가 아닌 경우에만 블록을 그림
			int[][] shape = nowBlock.getShape();
			int startX = (getWidth() - col * gridSize) / 2; // 그리드 가로 중앙
			int startY = (getHeight() - row * gridSize) / 2; // 그리드 세로 중앙
			g.setColor(nowBlock.getColor());

			for (int r = 0; r < shape.length; r++) {
				for (int c = 0; c < shape[r].length; c++) {
					if (shape[r][c] == 1) {
						g.fillRect(startX + (nowBlockX + c) * gridSize, startY + (nowBlockY + r) * gridSize,
								gridSize, gridSize);
						g.setColor(Color.WHITE); // 블록 테두리
						g.drawRect(startX + (nowBlockX + c) * gridSize, startY + (nowBlockY + r) * gridSize,
								gridSize, gridSize);
						g.setColor(nowBlock.getColor());
					}
				}
			}
		}

		if (gameOver) { // 게임 오버 시 메시지 출력
			SoundPlayer.stopBGM();
			if(gameoverSound) {
				SoundPlayer.playSound("./src/sounds/tetrisGameOver.wav"); // 효과음 재생
				gameoverSound = false;
			}
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.BOLD, 30));
			g.drawString("Game Over!", getWidth() / 2 - 75, getHeight() / 2 - 30);

			g.setFont(new Font("Arial", Font.PLAIN, 20));
			g.drawString("score: " + Score, getWidth() / 2 - 40, getHeight() / 2 + 50);
			if(bugModOnOff) {
				g.setFont(new Font("Arial", Font.PLAIN, 20));
				g.drawString("you are... " + yummyCount+"kg!", getWidth() / 2 - 40, getHeight() / 2 + 70);
			}
			else {
			g.setFont(new Font("Arial", Font.PLAIN, 20));
			g.drawString("tetris: " + tetrisCount, getWidth() / 2 - 40, getHeight() / 2 + 70);
			}
			checkAndSaveHighScore(); // 최고 기록 갱신 여부 확인 및 저장
			restartButton.setVisible(true); // 게임 오버 시 버튼 표시
			restartButton.setBounds(getWidth() / 2 - 40, getHeight() / 2 - 20, 100, 40);
		} else {
			restartButton.setVisible(false); // 게임 중에는 버튼 숨김
		}
		
		//=============================================================================================    엠블럼 추가
				if (highScore >= 100 || Score >= 100) {
					try {
						score100Image = ImageIO.read(new File("./src/images/score100.png"));
					} catch (IOException e) {
						e.printStackTrace(); // 이미지 로드 실패 시 예외 처리
					}
				}
				if (score100Image != null) { g.drawImage(score100Image, 30, 260, 120, 120, this); }
				
				if (highScore >= 300 || Score >= 300) {
					try {
						score300Image = ImageIO.read(new File("./src/images/score300.png"));
					} catch (IOException e) {
						e.printStackTrace(); // 이미지 로드 실패 시 예외 처리
					}
				}
				if (score300Image != null) { g.drawImage(score300Image, 30, 380, 120, 120, this); }
				
				if (highScore >= 500 || Score >= 500) {
					try {
						score500Image = ImageIO.read(new File("./src/images/score500.png"));
					} catch (IOException e) {
						e.printStackTrace(); // 이미지 로드 실패 시 예외 처리
					}
				}
				if (score500Image != null) { g.drawImage(score500Image, 30, 500, 120, 120, this); }
				
				if (highScore >= 1000 || Score >= 1000) {
					try {
						score1000Image = ImageIO.read(new File("./src/images/score1000.png"));
					} catch (IOException e) {
						e.printStackTrace(); // 이미지 로드 실패 시 예외 처리
					}
				}
				if (score1000Image != null) { g.drawImage(score1000Image, 30, 620, 120, 120, this); }
	}
	
//=======================================================================================================           어택모드 
	public void startAttackMode() { // 어택모드 시작
		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (attackModeOnOff) {
					attackmode(); // attackmode 호출
				}
			}
		};
		// actionListener를 호출
		new javax.swing.Timer(attackInterval, actionListener).start();
	}

	// 공격 모드에서 n초마다 공격 발생
	void attackmode() { // 
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastAttackTime >= attackInterval) {
			attackline(); // 공격 발생
			lastAttackTime = currentTime; // 마지막 공격 시간 갱신
		}
	}

	// 공격 발생 함수
	void attackline() {
		if (attackModeOnOff) { //어택모드가 켜져있다면
			for (int i = 0; i < row - 1; i++) {// 가로 조회
				System.arraycopy(grid[i + 1], 0, grid[i], 0, col); // 그리드 좌표 이동
				System.arraycopy(colors[i + 1], 0, colors[i], 0, col); // 색상 배열도 같이 이동
			}

			// 가장 아래 줄에 회색 줄 추가 (한 칸이 비어있는 회색 줄)
			// 가장 아래 줄을 모두 회색으로 채우기
	        for (int c = 0; c < col; c++) {
	            grid[row - 1][c] = 1; // 블록이 있는 상태
	            colors[row - 1][c] = Color.GRAY; // 색상 배열에 회색 설정
	        }
			
			// 회색으로 채운 후, 랜덤으로 하나의 칸을 빈 칸으로 설정
			int emptyIndex = random.nextInt(col); // 랜덤으로 비울 칸 위치 결정
			grid[row - 1][emptyIndex] = 0; // 비워진 칸의 값을 0으로(빈칸)
			repaint(); // 화면 다시 그리기
		}
	}

	//=================================================================================================================   기본 화면 그래픽
	void fill(Graphics g) {
		int panelWidth = getWidth();
		int panelHeight = getHeight();
		int gridWidth = col * gridSize;
		int gridHeight = row * gridSize;

		int startX = (panelWidth - gridWidth) / 2; // 가로 중앙
		int startY = (panelHeight - gridHeight) / 2; // 세로 중앙

		for (int r = 0; r < row; r++) {
			for (int c = 0; c < col; c++) {
				// 쌓인 블록 색상으로 채우기
				if (grid[r][c] == 1) {
					g.setColor(colors[r][c]); // 색상 사용
				} else {
					g.setColor(Color.BLACK); // 배경색
				}
				g.fillRect(startX + c * gridSize, startY + r * gridSize, gridSize, gridSize);
				g.setColor(Color.GRAY); // 격자무늬 / 쌓인블럭
				g.drawRect(startX + c * gridSize, startY + r * gridSize, gridSize, gridSize);
			}
		}
	}
	
	void fillHold(Graphics g) { // 홀드 그리드 그리기
		int startX = 30; // 홀드 블록 그리기 시작 X 좌표
		int startY = 104; // 홀드 블록 그리기 시작 Y 좌표

		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.BOLD, 30));
		g.drawString("HOLD", 44, 97); // 홀드 글씨

		g.setColor(Color.RED);
		g.setFont(new Font("Arial", Font.BOLD, 20));
		g.drawString("BEST: " + highScore, 37, 250); // 최고 기록을 화면에 표시
		
		// 홀드 블록 배경 그리기
		g.setColor(Color.BLACK);
		g.fillRect(startX, startY, 4 * gridSize, 4 * gridSize);
		
		if (holdBlock != null) { // 홀드에 블럭이 있으면
			g.setColor(HoldBlockColor); // holdBlock 색상 채우기
			int[][] shape = holdBlock; // 홀드에 있는 블럭을 저장

			for (int r = 0; r < shape.length; r++) {
				for (int c = 0; c < shape[r].length; c++) {
					if (shape[r][c] == 1) { //블럭 모양의 좌표를 1로 설정
						g.fillRect(startX + c * gridSize, startY + r * gridSize, gridSize, gridSize);
						g.setColor(Color.GRAY); // 블록 테두리
						g.drawRect(startX + c * gridSize, startY + r * gridSize, gridSize, gridSize);
						g.setColor(HoldBlockColor);
					}
				}
			}
		}
	}


	void fillNext(Graphics g) { //다음 블록 리스트를 화면에 표시
		int startX = 500;
		int startY = 104;
		int blockSize = 27;
		int gap = 100; // 간격

		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.BOLD, 30));
		g.drawString("NEXT", 520, 97);

		for (int r = 0; r < 17; r++) { //세로
			for (int c = 0; c < 4; c++) { //가로
				g.setColor(Color.BLACK);
				g.fillRect(startX + c * gridSize, startY + r * gridSize, gridSize, gridSize);
			}
		}
		for (int i = 0; i < 5 && i < blockBag.size(); i++) { //최대 5개까지 보이기
			Block nextBlock = blockBag.get(i); // 다음 블록을 담기
			int[][] shape = nextBlock.getShape(); // 모양 저장
			g.setColor(nextBlock.getColor()); // 다음 블럭의 색상을 불러와 채우기

			// 각 블록을 그림
			for (int r = 0; r < shape.length; r++) { //
				for (int c = 0; c < shape[r].length; c++) {
					if (shape[r][c] == 1) { // 모양의 좌표값을 1로 저장
						g.fillRect(startX + 5 + c * blockSize, startY + i * gap + r * blockSize, blockSize, blockSize);
						g.setColor(Color.LIGHT_GRAY);
						g.drawRect(startX + 5 + c * blockSize, startY + i * gap + r * blockSize, blockSize, blockSize);
						g.setColor(nextBlock.getColor());
					}
				}
			}
		}
	}
	
	void fillScore(Graphics g) { // 현재 스코어 그리기
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.BOLD, 30));
		g.drawString("Score", 513, 645);
		g.setColor(Color.RED);
		g.setFont(new Font("Arial", Font.BOLD, 30));
		g.drawString(String.valueOf(Score), 542, 675);
	}
	
	void stop(Graphics g) { // 일시정지시 pause 출력
		if (isPaused) {
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 30));
			g.drawString("pause", getWidth() / 2 - 40, getHeight() / 2);
		}
	}

 //============================================================================================================    시스템

	Color[][] colors = new Color[row][col]; // 색상을 저장하는 변수

	// 블록이 쌓일 때 색상 저장
	void placeBlock(int r, int c, Color color) {
		grid[r][c] = 1; // 블록 위치 값을 1로
		colors[r][c] = color; // 색상 채우기
	}
	
	void hardDrop() {//하드드롭
		while (canMove(nowBlockY + 1, nowBlockX)) {//움직이기가 가능한동안 아래로 내리기
			nowBlockY++;
		}
		placeBlock();//블록 놓기
		clearFullRows();//행이 찼는지 확인
		nowBlock = getNextBlock();//다음블록 가져오기
		nowBlockY = 0;
		nowBlockX = col / 2;
		holdUsed = false; // 새로운 블록이 나올 때마다 홀드 사용 초기화
		if (!canMove(nowBlockY, nowBlockX)) {//더이상 움직일 수 없으면
			// 게임 오버 로직
			gameOver = true; // 게임 오버 상태 설정
			// running = false; // 게임 오버 시 스레드 종료 (선택 사항)
			System.out.println("Game Over!");
		}
	}
	void rotateBlock() {
		int[][] currentShape = nowBlock.getShape();
		int[][] rotatedShape = new int[currentShape[0].length][currentShape.length];

		// 회전 로직 (시계 방향)
		for (int r = 0; r < currentShape.length; r++) {
			for (int c = 0; c < currentShape[0].length; c++) {
				rotatedShape[c][currentShape.length - 1 - r] = currentShape[r][c];
			}
		}

		// SRS 벽 킥 시도
		if (canPlaceRotatedBlock(rotatedShape)) {
			nowBlock.setShape(rotatedShape);
		} else if (attemptWallKick(rotatedShape)) {
			nowBlock.setShape(rotatedShape);
		}
	}

	void counterclockwiserotateBlock() {
		int[][] currentShape = nowBlock.getShape();
		int[][] rotatedShape = new int[currentShape[0].length][currentShape.length];

		// 반시계 회전 로직
		for (int r = currentShape.length - 1; r >= 0; r--) {
			for (int c = currentShape[0].length - 1; c >= 0; c--) {
				rotatedShape[currentShape[0].length - 1 - c][r] = currentShape[r][c];
			}
		}

		// SRS 벽 킥 시도
		if (canPlaceRotatedBlock(rotatedShape)) {
			nowBlock.setShape(rotatedShape);
		} else if (attemptWallKick(rotatedShape)) {
			nowBlock.setShape(rotatedShape);
		}
	}

	// I 미노 회전 시 벽 킥 시도
	boolean attemptWallKick(int[][] rotatedShape) {
		int[][] kicks;

		// I 미노 전용 벽 킥 패턴 (시계 방향 회전 기준)
		if (nowBlock.isImino()) {
			// I 미노가 위아래로 긴 상태에서 오른쪽 끝에 있을 때
			if (nowBlockX + nowBlock.getShape()[0].length >= col) {
				// 네 칸 왼쪽으로 이동 (가로로 긴 모양이 되도록)
				int newCol = nowBlockX - 3; // 네 칸 왼쪽 이동

				// 그리드 경계를 벗어나지 않도록 확인
				if (newCol >= 0) {
					nowBlockX = newCol; // 위치 업데이트
					return true; // 성공적으로 이동
				}
			}

			// I 미노 전용 벽 킥 패턴 (시계 방향)
			kicks = new int[][] { { 0, 0 }, { 0, -2 }, { 0, 2 }, { 1, -2 }, { -1, 2 } };
		} else {
			// 다른 블록의 벽 킥 패턴
			kicks = new int[][] { { 0, 0 }, { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };
		}

		// 벽 킥 시도
		for (int[] kick : kicks) {
			int newRow = nowBlockY + kick[0];
			int newCol = nowBlockX + kick[1];

			// 그리드 경계를 벗어나지 않도록 확인
			if (newCol >= 0 && newCol + rotatedShape[0].length <= col) {
				if (checkBlockPlacement(rotatedShape, newRow, newCol)) {
					nowBlockY = newRow;
					nowBlockX = newCol;
					return true;
				}
			}
		}

		return false; // 벽 킥 실패
	}
	boolean canPlaceRotatedBlock(int[][] rotatedShape) { // 회전이 가능한지 확인하는 bool
		// 기본 위치에서 확인
		if (checkBlockPlacement(rotatedShape, nowBlockY, nowBlockX)) { //회전된 모양, x좌표, y좌표를 검사후 true일 시 회전 가능
			return true;
		}

		// 벽에 닿으면 킥을 함
		int[][] kicks = { { 0, -1 }, { 0, 1 }, { -1, 0 } };
		for (int[] kick : kicks) {
			int newRow = nowBlockY + kick[0];
			int newCol = nowBlockX + kick[1];
			if (checkBlockPlacement(rotatedShape, newRow, newCol)) {
				nowBlockY = newRow; // 위치 업데이트
				nowBlockX = newCol; 
				return true; // 킥으로 회전 가능
			}
		}

		// 회전된 블록이 놓일 수 있는 공간을 추가로 확인
		int[][] additionalKicks = { { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } }; // 대각선 킥
		for (int[] kick : additionalKicks) {
			int newRow = nowBlockY + kick[0];
			int newCol = nowBlockX + kick[1];
			if (checkBlockPlacement(rotatedShape, newRow, newCol)) {
				nowBlockY = newRow; // 위치 업데이트
				nowBlockX = newCol;
				return true;
			}
		}
		return false;//모두 해당되지 않으면 회전 불가
	}

	boolean checkBlockPlacement(int[][] shape, int rowOffset, int colOffset) { //블록의 모양과 좌표를 받음
		for (int r = 0; r < shape.length; r++) {
			for (int c = 0; c < shape[r].length; c++) {
				if (shape[r][c] == 1) {
					int gridRow = rowOffset + r;
					int gridCol = colOffset + c;

					if (gridRow >= row || gridCol < 0 || gridCol >= col
							|| (gridRow >= 0 && grid[gridRow][gridCol] == 1)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	void Hold() {//홀드
		if (!holdUsed) { // 한 턴에 한 번만 홀드 가능
			if (holdBlock == null) { // 홀드 슬롯이 비어있는 경우
				holdBlock = nowBlock.getShape();// 블럭의 모양을 홀드에 저장
				HoldBlockColor = nowBlock.getColor();//색상도 저장
				nowBlock = getNextBlock();//다음 블럭을 현재 블럭으로 가져옴
			} else { // 홀드 슬롯에 이미 블록이 있는 경우
				int[][] tempBlock = holdBlock; //잠깐 hold 블럭을 저장해둘 변수
				Color tempColor = HoldBlockColor; //색상도
				holdBlock = nowBlock.getShape(); // 현재 블럭의 모영을 홀드에 저장
				HoldBlockColor = nowBlock.getColor();//색상도
				nowBlock = new Block(tempBlock, tempColor);//현재 블럭에 저장해둔 hold 블럭의 모양과 색 호출
			}
			nowBlockY = 0;//바꾼 블럭의 초기 위치(중앙 위)
			nowBlockX = col / 2;
			holdUsed = true; // 홀드 사용(1번 하기위한)변수
		}
	}
	@Override
	public void run() {
		long lastAttackTime = System.currentTimeMillis(); // 마지막 공격 시간 초기화

		while (running) {//게임이 실행되는동안
			if (!gameOver) { //게임오버가 아니면
				BlockDown();//블록 내리기
				// 어택 모드가 켜져 있고 일시정지가 아닐때 때만 attackline 호출
				if (attackModeOnOff && !isPaused) { 
					long currentTime = System.currentTimeMillis();
					if (currentTime - lastAttackTime >= attackInterval) {
						attackline(); // 공격 라인 추가
						lastAttackTime = currentTime; // 마지막 공격 시간 갱신
					}
				}

				repaint();
			}

			try {
				Thread.sleep(TimeTurn);
			} catch (InterruptedException e) {
				e.printStackTrace();
				// 스레드 인터럽트 처리
			}
		}
	}

	long placeBlockTime = 0; // 블록이 바닥에 닿은 시간을 저장

	void BlockDown() {
		if (isPaused) {
			lastAttackTime = System.currentTimeMillis(); //마지막 공격 시간 저장
			return; // 게임이 일시 정지 상태이면 아무 것도 하지 않음
			
		}
		if (canMove(nowBlockY + 1, nowBlockX)) {//
			nowBlockY++;
			placeBlockTime = 0; // 블록이 바닥에 닿지 않았을 때는 시간을 초기화
		} else {
			// 블록이 바닥에 닿은 경우 시간을 기록 (단, 아직 시간이 기록되지 않았다면)
			if (placeBlockTime == 0) {
				placeBlockTime = System.currentTimeMillis(); // 현재 시간 기록
			}
			// 블록이 바닥에 닿은 후 n초가 지났는지 확인
			if (System.currentTimeMillis() - placeBlockTime >= placeBlockTimeCount) {
				placeBlock(); // 블록 고정
				clearFullRows();
				nowBlock = getNextBlock();
				nowBlockY = 0;
				nowBlockX = col / 2;
				holdUsed = false; // 새로운 블록이 나올 때마다 홀드 초기화
				placeBlockTime = 0; // 시간을 초기화

				// 새로운 블록을 초기 위치에 배치할 수 없는 경우 게임 오버 처리
				if (!canMove(nowBlockY, nowBlockX)) {
					gameOver = true;
					System.out.println("Game Over!");
				}
			}
		}
	}

	boolean canMove(int newRow, int newCol) { //블록이 움직일 수 있는지 확인하는 메서드
		int[][] shape = nowBlock.getShape(); // 현재의 모양을 저장
		for (int r = 0; r < shape.length; r++) {
			for (int c = 0; c < shape[r].length; c++) {
				if (shape[r][c] == 1) { // 모양이 있는 위치에서 n더하기
					int gridRow = newRow + r;
					int gridCol = newCol + c;

					// 배열의 범위를 벗어나면 false 반환
					if (gridRow >= row || gridRow < 0 || gridCol < 0 || gridCol >= col) {
						return false;
					}

					// 블록이 그리드에 겹치는 경우 false 반환
					if (gridRow >= 0 && grid[gridRow][gridCol] == 1) {
						return false;
					}
				}
			}
		}
		return true;
	}

	void placeBlock() { //블럭이 바닥에 닿으면
		int[][] shape = nowBlock.getShape();//모양 저장
		fullLineCount = 0; // 라인이 한번에 몇 줄 지워졌는지 확인
		Score++; //1점 추가
		SoundPlayer.playSound("./src/sounds/block_place.wav"); // 효과음 재생
		eatingSound = true; // bug모드에서 필요한 변수

		for (int r = 0; r < shape.length; r++) {
			for (int c = 0; c < shape[r].length; c++) {
				if (shape[r][c] == 1) {
					placeBlock(nowBlockY + r, nowBlockX + c, nowBlock.getColor());
				}
			}
		}
	}

	void bugRotateBlock() {//버그모드의 회전
	    int[][] currentShape = nowBlock.getShape(); // 현재 블록 모양 가져오기
	    int rows = currentShape.length; // 행 개수
	    int cols = currentShape[0].length; // 열 개수

	    // L, J 모양 블록인 경우 2x2 블록으로 강제 변환
	    if ((currentShape == blockL || currentShape == blockJ)) {
	        int[][] rotatedShape = new int[2][2]; // 2x2 크기로 설정

	        // 회전 로직 (시계 방향 회전)
	        for (int r = 0; r < rows; r++) {
	            for (int c = 0; c < cols; c++) {
	                if (c < 2 && r < 2) { // 경계를 잘못 설정하여 오른쪽 데이터가 잘리게 함
	                    rotatedShape[c][1 - r] = currentShape[r][c]; // 회전 시 2x2로 변환
	                }
	            }
	        }
	        nowBlock.setShape(rotatedShape);
	    }
	 // O 모양 블록 회전 처리
	    else if (currentShape == blockO) {
	    	SoundPlayer.playSound("./src/sounds/eating.wav"); // 효과음 재생
	        //1x2
	        int[][] rotatedShape = new int[1][2]; // O 블록을 1x2 형태로 설정

	        for (int r = 0; r < 2; r++) { //경계를 잘못 설정하여 크기가 줄도록 함
	            for (int c = 0; c < 2; c++) {
	                rotatedShape[0][c] = currentShape[r][c]; // 2x2 배열을 1x2 배열로 변환
	            }
	        }
	        // 회전된 O 블록을 설정
	        nowBlock.setShape(rotatedShape);
	    } else {
	        // L, J 블록이 아닌 경우에는 일반적인 회전 로직 사용
	        int[][] rotatedShape = new int[rows][rows]; // 회전 후 배열 크기 설정

	        // 회전 로직 (시계 방향 회전)
	        for (int r = 0; r < rows; r++) {
	            for (int c = 0; c < cols; c++) {
	                if (c < rows) { // 경계를 잘못 설정하여 오른쪽 데이터가 잘리게 함
	                    rotatedShape[c][rows - 1 - r] = currentShape[r][c];
	                }
	            }
	        }
	        
	        nowBlock.setShape(rotatedShape); // 현재 블록을 새로 회전해 작아진 블록모양으로 변경
	        if(eatingSound) {//한 번만 실행되기 위해 사용한 변수
	        	SoundPlayer.playSound("./src/sounds/eating.wav"); // 효과음 재생
	        	yummyCount++;// bug모드의 스코어 점수 추가
	        	eatingSound = false;//한번 실행하고 다음블록까지 false
	        }
	    }
	} 
	
	int findGhostRow() {// 유령블록 밑으로 내리는 메서드
		int ghostRow = nowBlockY;
		while (canMove(ghostRow + 1, nowBlockX)) {//y좌표를 가능할 때 까지 내리기
			ghostRow++;
		}
		return ghostRow;
	}
	
	boolean fullTetris = false; // 4줄이 한번에 깎였는지 확인
	int fullLineCount = 0; // 깎인 라인 수 카운트
	long tetrisDisplayTime = 0; // 테트리스 문구가 화면에 표시되는 시간

	boolean tetrisclearsound = true; //라인 클리어 효과음이 반복재생되지 않도록
	void tetrisClear(Graphics g) {
		if (fullTetris) { //4줄이 한번에 깎이면
			if(tetrisclearsound) { SoundPlayer.playSound("./src/sounds/lineclear4.wav"); }//효과음
			tetrisclearsound = false;//한번만 실행
			
			if (System.currentTimeMillis() - tetrisDisplayTime < 1500) { // 1.5초 보이기
				g.setColor(Color.WHITE);
				g.setFont(new Font("Arial", Font.BOLD, 30));
				g.drawString("Tetris!", getWidth() / 2 - 40, getHeight() / 2 - 50);

			} else {
				//1.5초가 지나면 fullTetris를 false로 설 정
				tetrisclearsound = true;
				fullTetris = false;
				fullLineCount = 0;
				Score += 10;//10점 추가
				tetrisCount++;//테트리스 횟수를 저장
			}
		}
		repaint();
	}

	void clearFullRows() { //한 줄이 찼는지 확인
		for (int r = row - 1; r >= 0; r--) {// 아래서부터 순회
			boolean fullRow = true;
			for (int c = 0; c < col; c++) {//한 칸이라도 1이 있으면 false
				if (grid[r][c] == 0) {
					fullRow = false;
					break;
				}
			}
			if (fullRow) {
				Score += 3;
				fullLineCount++;
				SoundPlayer.playSound("./src/sounds/lineclear1.wav");
				for (int i = r; i > 0; i--) {
					System.arraycopy(grid[i - 1], 0, grid[i], 0, col); //블럭을 아래로
					System.arraycopy(colors[i - 1], 0, colors[i], 0, col); // 색상 배열도 같이 이동
				}
				for (int c = 0; c < col; c++) {
					grid[0][c] = 0;
					colors[0][c] = Color.BLACK; // 초기 색상 설정
				}
				r++; // 같은 행을 다시 확인
			}

			if (fullLineCount == 4) {//4줄이 한거번에 지워졌으면 true로
				fullTetris = true;
				repaint();
				tetrisDisplayTime = System.currentTimeMillis(); // 현재 시간 기록
			}
		}
	}

	// 게임 재시작 메서드
	private void restartGame() {
		gameOver = false;
		grid = new int[row][col];
		nowBlockY = 0;
		nowBlockX = col / 2;
		nowBlock = getNextBlock();
		requestFocusInWindow(); // 게임 패널에 포커스 설정
		startGame(); // 게임 루프 다시 시작
		repaint();
	}

	// 게임 시작 메서드
	public void startGame() {
		holdBlock = null;
		HoldBlockColor = null;
		tetrisCount = 0;
		Score = 0;
		gameoverSound = true;
		if(BGMon) { SoundPlayer.playBGM("./src/sounds/tetrisBGM.wav"); }//bgm 켜져있으면 실행
		if (!running) {
			running = true;
			gameThread = new Thread(this);
			gameThread.start();
		}
	}
}

// 블록들
class Block {
	private int[][] shape;
	private Color color;

	// I 미노인지 확인하는 메서드 (블록 클래스에서)
	boolean isImino() {
		// I 미노는 4x1 또는 1x4 크기임
		return (shape.length == 4 && shape[0].length == 1) || (shape.length == 1 && shape[0].length == 4);
	}

	public Block(int[][] shape, Color color) {//모양과 색을 저장
		this.shape = shape;
		this.color = color;
	}

	public int[][] getShape() {//모양을 가져오기
		return shape;
	}

	public void setShape(int[][] newShape) {//모양 조정
		this.shape = newShape;
	}

	public Color getColor() {//색 가져오기
		return color;
	}
}