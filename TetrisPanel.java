package SwingTetris;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
	int nowBlockRow = 0; // 블록이 있는 위치
	int nowBlockCol = col / 2; // 블록이 있는 위치(중앙 위)
	int Timerturm;

	int[][] holdBlock;
	int[][] holdgrid = new int[4][4];
	int goldgridSize = 30;

	Color HoldBlockColor;
	Block nowBlock;
	Random random = new Random(); // 랜덤 블록 생성을 위한 랜덤 객체

	int Score = 0;
	int tetrisCount = 0;

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
	private JButton settingButton; // 설정 버튼 추가
	boolean ButtonOnOff = false; // 설정 on/off
	boolean attackModeOnOff = false; // 공격 모드 on/off
	private JButton attackModeButton; // 공격 모드 버튼

	private boolean gameOver = false; // 게임 오버 상태 변수 추가
	private JButton restartButton; // 재시작 버튼 추가
	private Thread gameThread; // 게임 루프를 실행할 스레드
	private boolean running = false; // 게임 루프 실행 상태

	private boolean holdUsed = false; // 한 턴에 한 번만 홀드 사용 가능하도록 플래그 변수 추가

	boolean isPaused = false; // 게임 일시 정지 여부 확인 변수

	public TetrisPanel() {
		Timerturm = 600;
		setFocusable(true);
		requestFocusInWindow(); // 포커스를 패널로

		fillBlockBag(); // 초기 블록 가방 채우기
		nowBlock = getNextBlock();

		// KeyBindings 설정
		setKeyBindings();

		// 재시작 버튼 설정
		restartButton = new JButton("Restart");
		restartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				restartGame();
			}
		});

		settingButton = new JButton("Setting");
		settingButton.setBackground(Color.WHITE);

		settingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ButtonOnOff = !ButtonOnOff;
				if (ButtonOnOff) { // 켜짐
					togglePause(); // 설정 버튼을 누르면 일시 정지 상태 전환
					settingButton.setText("Back");
					settingButton.setBackground(Color.GRAY);
				} else {

					isPaused = false;
					requestFocusInWindow();
					settingButton.setText("Setting");
					settingButton.setBackground(Color.WHITE);
				}

			}
		});

		attackModeButton = new JButton("Attack Mode");
		settingButton.setBackground(Color.WHITE);
		attackModeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				attackModeOnOff = !attackModeOnOff;
				if (attackModeOnOff) { // 켜짐
					attackmode();
					attackModeButton.setBackground(Color.GRAY);
				} else {
					attackModeButton.setBackground(Color.WHITE);

				}
			}
		});

		restartButton.setVisible(false);// 재시작 버튼
		add(restartButton);
		settingButton.setVisible(true);// 설정 버튼
		add(settingButton);
		attackModeButton.setVisible(false);
		add(attackModeButton);

		// 게임 시작
		gameThread = new Thread(this);
		startGame();

		this.requestFocusInWindow();
	}

	void togglePause() {
		isPaused = !isPaused; // 설정 창이 열리면 일시 정지
	}

	// 블록 가방 채우는 메서드
	private void fillBlockBag() {
		blockBag.clear();
		List<Block> tempBlocks = new ArrayList<>();
		for (Block block : blocks) {
			tempBlocks.add(new Block(block.getShape(), block.getColor()));
		}
		Collections.shuffle(tempBlocks);
		blockBag.addAll(tempBlocks);
	}

	private Block getNextBlock() {
		if (blockBag.isEmpty()) {
			fillBlockBag(); // 가방이 비었으면 다시 채우기
		}
		return blockBag.remove(0);
	}

	private void setKeyBindings() {
		// 왼쪽 화살표 키
		this.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
		this.getActionMap().put("moveLeft", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!gameOver && canMove(nowBlockRow, nowBlockCol - 1)) {
					nowBlockCol--;
					repaint();
				}
			}
		});

		// 오른쪽 화살표 키
		this.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");
		this.getActionMap().put("moveRight", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!gameOver && canMove(nowBlockRow, nowBlockCol + 1)) {
					nowBlockCol++;
					repaint();
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
					repaint();
				}
			}
		});

		// 위쪽 화살표 키 (회전)
		this.getInputMap().put(KeyStroke.getKeyStroke("UP"), "rotate");
		this.getActionMap().put("rotate", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!gameOver) {
					rotateBlock();
					repaint();
				}
			}
		});
		// z 키 (회전)
		this.getInputMap().put(KeyStroke.getKeyStroke("Z"), "counterclockwiserotate");
		this.getActionMap().put("counterclockwiserotate", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!gameOver) {
					counterclockwiserotateBlock();
					repaint();
				}
			}
		});

		// 스페이스 바 (하드 드롭)
		this.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "hardDrop");
		this.getActionMap().put("hardDrop", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!gameOver) {
					hardDrop();
					repaint();
				}
			}
		});

		// 쉬프트 (홀드)
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK), "Hold");
		this.getActionMap().put("Hold", new AbstractAction() {
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

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		fill(g);
		fillHold(g);
		fillNext(g);
		fillScore(g);
		tetrisClear(g);
		stop(g);
		if (!gameOver) {
			int ghostRow = findGhostRow();
			int[][] shape = nowBlock.getShape();
			int startX = (getWidth() - col * gridSize) / 2;
			int startY = (getHeight() - row * gridSize) / 2;

			Color currentColor = nowBlock.getColor();
			// R, G, B, 투명도값 0 ~ 225
			Color transparentColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(),
					90);
			g.setColor(transparentColor);

			for (int r = 0; r < shape.length; r++) {
				for (int c = 0; c < shape[r].length; c++) {
					if (shape[r][c] == 1) {
						g.fillRect(startX + (nowBlockCol + c) * gridSize, startY + (ghostRow + r) * gridSize, gridSize,
								gridSize);
						// 유령블록 테투리
						g.setColor(Color.YELLOW);
						g.drawRect(startX + (nowBlockCol + c) * gridSize, startY + (ghostRow + r) * gridSize, gridSize,
								gridSize);
						g.setColor(transparentColor);
					}
				}
			}
		}
		if (ButtonOnOff) {
			attackModeButton.setVisible(true); // setting 버튼 클릭 시 표시
			attackModeButton.setBounds(getWidth() / 2 - 80, 50, 110, 30);
		} else {
			attackModeButton.setVisible(false);
			attackModeButton.setBounds(getWidth() / 2 - 80, 50, 110, 30);
		}

		if (!gameOver) { // 게임 오버가 아닌 경우에만 블록을 그림
			int[][] shape = nowBlock.getShape();
			int startX = (getWidth() - col * gridSize) / 2; // 그리드 가로 중앙
			int startY = (getHeight() - row * gridSize) / 2; // 그리드 세로 중앙
			g.setColor(nowBlock.getColor());

			for (int r = 0; r < shape.length; r++) {
				for (int c = 0; c < shape[r].length; c++) {
					if (shape[r][c] == 1) {
						g.fillRect(startX + (nowBlockCol + c) * gridSize, startY + (nowBlockRow + r) * gridSize,
								gridSize, gridSize);
						g.setColor(Color.WHITE); // 블록 테두리
						g.drawRect(startX + (nowBlockCol + c) * gridSize, startY + (nowBlockRow + r) * gridSize,
								gridSize, gridSize);
						g.setColor(nowBlock.getColor());
					}
				}
			}
		}

		if (gameOver) { // 게임 오버 시 메시지 출력
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.BOLD, 30));
			g.drawString("Game Over!", getWidth() / 2 - 75, getHeight() / 2 - 30);

			g.setFont(new Font("Arial", Font.PLAIN, 20));
			g.drawString("score: " + Score, getWidth() / 2 - 40, getHeight() / 2 + 50);

			g.setFont(new Font("Arial", Font.PLAIN, 20));
			g.drawString("tetris: " + tetrisCount, getWidth() / 2 - 40, getHeight() / 2 + 70);

			restartButton.setVisible(true); // 게임 오버 시 버튼 표시
			restartButton.setBounds(getWidth() / 2 - 40, getHeight() / 2 - 20, 100, 40);
		} else {
			restartButton.setVisible(false); // 게임 중에는 버튼 숨김
		}
	}

	/*
	 * -----------------------------------------------모드----------------------------
	 * ---------
	 */
	long lastAttackTime = 0; // 마지막 공격 발생 시간 저장
	int attackInterval = 15000; // 15초 간격으로 공격

	void attackmode() {
		// attackMode가 켜졌고 15초가 지났을 때
		if (attackModeOnOff && System.currentTimeMillis() - lastAttackTime >= attackInterval) {
			attackline(); // 한 줄 추가
			lastAttackTime = System.currentTimeMillis(); // 공격 시간 갱신
		}
	}

	void attackline() {
		// 첫 번째 줄부터 마지막 줄까지 한 줄씩 위로 올리기
		for (int r = 0; r < row - 1; r++) {
			System.arraycopy(grid[r + 1], 0, grid[r], 0, col); // 아랫줄을 윗줄로 복사
		}

		// 가장 아래 줄에 랜덤으로 한 칸이 뚫린 회색 줄 추가
		int emptyIndex = random.nextInt(col); // 랜덤으로 빈 칸 위치 결정

		for (int c = 0; c < col; c++) {
			if (c == emptyIndex) {
				grid[row - 1][c] = 0; // 빈 칸
			} else {
				grid[row - 1][c] = -1; // 회색 블록
			}
		}

		repaint(); // 화면 다시 그리기
	}

	/*-----------------------------------------------------------------------------------------*/
	void stop(Graphics g) {
		if (isPaused) {
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 30));
			g.drawString("pause", getWidth() / 2 - 40, getHeight() / 2);
		}
	}

	int findGhostRow() {// 유령블록
		int ghostRow = nowBlockRow;
		while (canMove(ghostRow + 1, nowBlockCol)) {
			ghostRow++;
		}
		return ghostRow;
	}

	void fillHold(Graphics g) {
		int startX = 30; // 홀드 블록 그리기 시작 X 좌표
		int startY = 104; // 홀드 블록 그리기 시작 Y 좌표

		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.BOLD, 30));
		g.drawString("HOLD", 44, 97);

		// 홀드 블록 배경 그리기
		g.setColor(Color.BLACK);
		g.fillRect(startX, startY, 4 * gridSize, 4 * gridSize);

		if (holdBlock != null) { // qmffhrdl dlTdmaus
			g.setColor(HoldBlockColor); // holdBlock 색
			int[][] shape = holdBlock;

			for (int r = 0; r < shape.length; r++) {
				for (int c = 0; c < shape[r].length; c++) {
					if (shape[r][c] == 1) {
						g.fillRect(startX + c * gridSize, startY + r * gridSize, gridSize, gridSize);
						g.setColor(Color.WHITE); // 블록 테두리
						g.drawRect(startX + c * gridSize, startY + r * gridSize, gridSize, gridSize);
						g.setColor(HoldBlockColor);
					}
				}
			}
		}
	}

	void fillScore(Graphics g) {
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.BOLD, 30));
		g.drawString("Score", 513, 645);
		g.setColor(Color.RED);
		g.setFont(new Font("Arial", Font.BOLD, 30));
		g.drawString(String.valueOf(Score), 542, 675);
	}

	void fillNext(Graphics g) {
		int startX = 500;
		int startY = 104;
		int blockSize = 27;
		int gap = 100; // 간격

		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.BOLD, 30));
		g.drawString("NEXT", 520, 97);

		for (int r = 0; r < 17; r++) {
			for (int c = 0; c < 4; c++) {
				g.setColor(Color.BLACK);
				g.fillRect(startX + c * gridSize, startY + r * gridSize, gridSize, gridSize);
			}
		}
		for (int i = 0; i < 5 && i < blockBag.size(); i++) {
			Block nextBlock = blockBag.get(i); // 다음 블록
			int[][] shape = nextBlock.getShape();
			g.setColor(nextBlock.getColor());

			// 각 블록을 그림
			for (int r = 0; r < shape.length; r++) {
				for (int c = 0; c < shape[r].length; c++) {
					if (shape[r][c] == 1) {
						g.fillRect(startX + 5 + c * blockSize, startY + i * gap + r * blockSize, blockSize, blockSize);
						g.setColor(Color.LIGHT_GRAY);
						g.drawRect(startX + 5 + c * blockSize, startY + i * gap + r * blockSize, blockSize, blockSize);
						g.setColor(nextBlock.getColor());
					}
				}
			}
		}
	}

	Color[][] colors = new Color[row][col];

	// 블록이 쌓일 때 색상 저장
	void placeBlock(int r, int c, Color color) {
		grid[r][c] = 1; // 블록 위치 표시
		colors[r][c] = color; // 색상 저장
	}

	// 그래픽을 그릴 때 색상을 표시
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
					// 쌓인 블록의 색상 사용
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

	@Override
	public void run() {

		while (running) {
			if (!gameOver) {
				BlockDown();
				repaint();
			}

			try {
				Thread.sleep(Timerturm);
			} catch (InterruptedException e) {
				e.printStackTrace();
				// 스레드 인터럽트 처리
			}
		}
	}

	long placeBlockTime = 0; // 블록이 바닥에 닿은 시간을 저장

	void BlockDown() {
		if (isPaused) {
			return; // 게임이 일시 정지 상태이면 아무 것도 하지 않음
		}
		if (canMove(nowBlockRow + 1, nowBlockCol)) {
			nowBlockRow++;
			placeBlockTime = 0; // 블록이 바닥에 닿지 않았을 때는 시간을 초기화
		} else {
			// 블록이 바닥에 닿은 경우 시간을 기록 (단, 아직 시간이 기록되지 않았다면)
			if (placeBlockTime == 0) {
				placeBlockTime = System.currentTimeMillis(); // 현재 시간 기록
			}
			// 블록이 바닥에 닿은 후 1.5초가 지났는지 확인
			if (System.currentTimeMillis() - placeBlockTime >= 1500) {
				placeBlock(); // 블록 고정
				clearFullRows();
				nowBlock = getNextBlock();
				nowBlockRow = 0;
				nowBlockCol = col / 2;
				holdUsed = false; // 새로운 블록이 나올 때마다 홀드 초기화
				placeBlockTime = 0; // 시간을 초기화

				if (!canMove(nowBlockRow, nowBlockCol)) {
					gameOver = true; // 게임 오버 상태 설정
					System.out.println("Game Over!");
				}
			}
		}
	}

	boolean canMove(int newRow, int newCol) {
		int[][] shape = nowBlock.getShape();
		for (int r = 0; r < shape.length; r++) {
			for (int c = 0; c < shape[r].length; c++) {
				if (shape[r][c] == 1) {
					int gridRow = newRow + r;
					int gridCol = newCol + c;

					if (gridRow >= row || gridCol < 0 || gridCol >= col
							|| (gridRow >= 0 && grid[gridRow][gridCol] == 1)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	void placeBlock() {
		int[][] shape = nowBlock.getShape();
		fullLineCount = 0;
		Score++;
		for (int r = 0; r < shape.length; r++) {
			for (int c = 0; c < shape[r].length; c++) {
				if (shape[r][c] == 1) {
					placeBlock(nowBlockRow + r, nowBlockCol + c, nowBlock.getColor());
				}
			}
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
			if (nowBlockCol + nowBlock.getShape()[0].length >= col) {
				// 네 칸 왼쪽으로 이동 (가로로 긴 모양이 되도록)
				int newCol = nowBlockCol - 3; // 네 칸 왼쪽 이동

				// 그리드 경계를 벗어나지 않도록 확인
				if (newCol >= 0) {
					nowBlockCol = newCol; // 위치 업데이트
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
			int newRow = nowBlockRow + kick[0];
			int newCol = nowBlockCol + kick[1];

			// 그리드 경계를 벗어나지 않도록 확인
			if (newCol >= 0 && newCol + rotatedShape[0].length <= col) {
				if (checkBlockPlacement(rotatedShape, newRow, newCol)) {
					nowBlockRow = newRow;
					nowBlockCol = newCol;
					return true;
				}
			}
		}

		return false; // 벽 킥 실패
	}

	// T-스핀 감지
	boolean isTSpin(int[][] rotatedShape) {
		// T-블록의 중앙 좌표를 기준으로 회전 시 주변 3곳이 막혀 있는지 확인
		int centerRow = nowBlockRow + 1; // T-블록의 중앙 부분 위치
		int centerCol = nowBlockCol + 1;

		int blockedCount = 0;

		// 중심 주변 4개의 좌표 중 3개가 막혀 있으면 T-스핀 가능
		if (centerRow > 0 && grid[centerRow - 1][centerCol] == 1)
			blockedCount++; // 위쪽 확인
		if (centerCol > 0 && grid[centerRow][centerCol - 1] == 1)
			blockedCount++; // 왼쪽 확인
		if (centerCol < col - 1 && grid[centerRow][centerCol + 1] == 1)
			blockedCount++; // 오른쪽 확인
		if (centerRow < row - 1 && grid[centerRow + 1][centerCol] == 1)
			blockedCount++; // 아래쪽 확인

		return blockedCount >= 3; // 3곳이 막혀 있을 경우 T-스핀
	}

	boolean canPlaceRotatedBlock(int[][] rotatedShape) {
		// 기본 위치에서 확인
		if (checkBlockPlacement(rotatedShape, nowBlockRow, nowBlockCol)) {
			return true;
		}

		// 벽 킥 시도 (좌우, 위쪽)
		int[][] kicks = { { 0, -1 }, { 0, 1 }, { -1, 0 } };
		for (int[] kick : kicks) {
			int newRow = nowBlockRow + kick[0];
			int newCol = nowBlockCol + kick[1];
			if (checkBlockPlacement(rotatedShape, newRow, newCol)) {
				nowBlockRow = newRow; // 위치 업데이트
				nowBlockCol = newCol;
				return true;
			}
		}

		// 회전된 블록이 놓일 수 있는 공간을 추가로 확인
		int[][] additionalKicks = { { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } }; // 대각선 킥
		for (int[] kick : additionalKicks) {
			int newRow = nowBlockRow + kick[0];
			int newCol = nowBlockCol + kick[1];
			if (checkBlockPlacement(rotatedShape, newRow, newCol)) {
				nowBlockRow = newRow; // 위치 업데이트
				nowBlockCol = newCol;
				return true;
			}
		}
		return false;
	}

	boolean checkBlockPlacement(int[][] shape, int rowOffset, int colOffset) {
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

	void Hold() {
		if (!holdUsed) { // 한 턴에 한 번만 홀드 가능
			if (holdBlock == null) { // 홀드 슬롯이 비어있는 경우
				holdBlock = nowBlock.getShape();
				HoldBlockColor = nowBlock.getColor();
				nowBlock = getNextBlock();
			} else { // 홀드 슬롯에 이미 블록이 있는 경우
				int[][] tempBlock = holdBlock;
				Color tempColor = HoldBlockColor;
				holdBlock = nowBlock.getShape();
				HoldBlockColor = nowBlock.getColor();
				nowBlock = new Block(tempBlock, tempColor);
			}
			nowBlockRow = 0;
			nowBlockCol = col / 2;
			holdUsed = true; // 홀드 사용 표시
		}
	}

	boolean fullTetris = false; // 4줄이 한번에 깎였는지 확인
	int fullLineCount = 0; // 깎인 라인 수 카운트
	long tetrisDisplayTime = 0; // 테트리스가 화면에 표시되는 시간

	void tetrisClear(Graphics g) {
		if (fullTetris) {
			if (System.currentTimeMillis() - tetrisDisplayTime < 1500) { // 1.5초
				g.setColor(Color.WHITE);
				g.setFont(new Font("Arial", Font.BOLD, 30));
				g.drawString("Tetris!", getWidth() / 2 - 40, getHeight() / 2 - 50);

			} else {
				// 2초가 지나면 fullTetris를 false로 설정
				fullTetris = false;
				fullLineCount = 0;
				Score += 10;
				tetrisCount++;
			}
		}
		repaint();
	}

	void clearFullRows() {
		for (int r = row - 1; r >= 0; r--) {
			boolean fullRow = true;
			for (int c = 0; c < col; c++) {
				if (grid[r][c] == 0) {
					fullRow = false;
					break;
				}
			}
			if (fullRow) {
				Score += 3;
				fullLineCount++;
				for (int i = r; i > 0; i--) {
					System.arraycopy(grid[i - 1], 0, grid[i], 0, col);
				}
				for (int c = 0; c < col; c++) {
					grid[0][c] = 0;
				}
				r++; // 같은 행을 다시 확인
			}

			if (fullLineCount == 4) {
				fullTetris = true;
				repaint();
				tetrisDisplayTime = System.currentTimeMillis(); // 현재 시간 기록
			}

		}
	}

	void hardDrop() {
		while (canMove(nowBlockRow + 1, nowBlockCol)) {
			nowBlockRow++;
		}
		placeBlock();
		clearFullRows();
		nowBlock = getNextBlock();
		nowBlockRow = 0;
		nowBlockCol = col / 2;
		holdUsed = false; // 새로운 블록이 나올 때마다 홀드 초기화
		if (!canMove(nowBlockRow, nowBlockCol)) {
			// 게임 오버 로직
			gameOver = true; // 게임 오버 상태 설정
			// running = false; // 게임 오버 시 스레드 종료 (선택 사항)
			System.out.println("Game Over!");
		}
	}

	private void setting() {

	}

	// 게임 재시작 메서드
	private void restartGame() {
		gameOver = false;
		grid = new int[row][col];
		nowBlockRow = 0;
		nowBlockCol = col / 2;
		nowBlock = getNextBlock();
		requestFocusInWindow(); // 게임 패널에 포커스 설정
		startGame(); // 게임 루프 다시 시작
		repaint();
	}

	// 게임 시작 메서드
	public void startGame() {
		// BlockHave = 0; -> 불필요한 변수 제거
		holdBlock = null;
		HoldBlockColor = null;
		tetrisCount = 0;
		Score = 0;
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

	public Block(int[][] shape, Color color) {
		this.shape = shape;
		this.color = color;
	}

	public int[][] getShape() {
		return shape;
	}

	public void setShape(int[][] newShape) {
		this.shape = newShape;
	}

	public Color getColor() {
		return color;
	}
}