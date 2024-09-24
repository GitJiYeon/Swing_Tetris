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
    int row = 20;//ㅅㄹ
    int col = 10;//ㄱㄹ
    int[][] grid = new int[row][col]; // 맵 사이즈
    int gridSize = 30;// 한칸 사이즈
    int nowBlockRow = 0; // 블록이 있는 위치
    int nowBlockCol = col / 2; // 블록이 있는 위치(중앙 위)
    int Timerturm;

    int[][] holdBlock;
    int[][] holdgrid = new int[4][4];
    int goldgridSize = 30;

    //int BlockHave; //홀드에 블럭이 있는지 확인 -> 불필요한 변수 제거
    Color HoldBlockColor;
    int Score = 0;
    Block nowBlock;
    Random random = new Random(); // 랜덤 블록 생성을 위한 랜덤 객체

    // 블록 모양
    int[][] blockI = {{1, 1, 1, 1}};
    int[][] blockT = {{1, 1, 1}, {0, 1, 0}};
    int[][] blockO = {{1, 1}, {1, 1}};
    int[][] blockL = {{1, 1}, {0, 1}, {0, 1}};
    int[][] blockJ = {{1, 1}, {1, 0}, {1, 0}};
    int[][] blockZ = {{1, 1, 0}, {0, 1, 1}};
    int[][] blockS = {{0, 1, 1}, {1, 1, 0}};

    Block[] blocks = {
            new Block(blockI, Color.CYAN),
            new Block(blockT, Color.MAGENTA),
            new Block(blockO, Color.YELLOW),
            new Block(blockL, Color.ORANGE),
            new Block(blockJ, Color.BLUE),
            new Block(blockZ, Color.RED),
            new Block(blockS, Color.GREEN)
    };

    private List<Block> blockBag = new ArrayList<>(); // 블록 가방
    private boolean gameOver = false; // 게임 오버 상태 변수 추가
    private JButton restartButton; // 재시작 버튼 추가
    private Thread gameThread; // 게임 루프를 실행할 스레드
    private boolean running = false; // 게임 루프 실행 상태

    private boolean holdUsed = false; // 한 턴에 한 번만 홀드 사용 가능하도록 플래그 변수 추가

    public TetrisPanel() {
        Timerturm = 400;
        setFocusable(true);
        requestFocusInWindow();  // 포커스를 패널로

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
        restartButton.setVisible(false); // 초기에는 버튼을 숨김
        add(restartButton);

        // 게임 시작
        gameThread = new Thread(this);
        startGame();
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
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK), "Hold");
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
        if (!gameOver) {  // 게임 오버가 아닌 경우에만 블록을 그림
            int[][] shape = nowBlock.getShape();
            int startX = (getWidth() - col * gridSize) / 2; // 그리드 가로 중앙
            int startY = (getHeight() - row * gridSize) / 2; // 그리드 세로 중앙
            g.setColor(nowBlock.getColor());

            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] == 1) {
                        g.fillRect(startX + (nowBlockCol + c) * gridSize, startY + (nowBlockRow + r) * gridSize, gridSize, gridSize);
                        g.setColor(Color.LIGHT_GRAY); // 블록 테두리
                        g.drawRect(startX + (nowBlockCol + c) * gridSize, startY + (nowBlockRow + r) * gridSize, gridSize, gridSize);
                        g.setColor(nowBlock.getColor());
                    }
                }
            }
        }


        if (gameOver) { // 게임 오버 시 메시지 출력
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Game Over", getWidth() / 2 - 100, getHeight() / 2 - 50);

            restartButton.setVisible(true); // 게임 오버 시 버튼 표시
            restartButton.setBounds(getWidth() / 2 - 50, getHeight() / 2, 100, 50);
        } else {
            restartButton.setVisible(false); // 게임 중에는 버튼 숨김
        }
    }

    void fillHold(Graphics g) {
        int startX = 25; // 홀드 블록 그리기 시작 X 좌표
        int startY = 104; // 홀드 블록 그리기 시작 Y 좌표

        // 홀드 블록 배경 그리기
        g.setColor(Color.BLACK);
        g.fillRect(startX, startY, 4 * gridSize, 4 * gridSize);

        if (holdBlock != null) { // qmffhrdl dlTdmaus
            g.setColor(HoldBlockColor);  //holdBlock 색
            int[][] shape = holdBlock;

            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] == 1) {
                        g.fillRect(startX + c * gridSize, startY + r * gridSize, gridSize, gridSize);
                        g.setColor(Color.LIGHT_GRAY); // 블록 테두리
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
        int startX = 490;
        int startY = 104;
        int blockSize = 27;
        int gap = 100;    //간격

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Next", 520, 97);

        for (int r = 0; r < 17; r++) {
            for (int c = 0; c < 4; c++) {
                g.setColor(Color.BLACK);
                g.fillRect(startX + c * gridSize, startY + r * gridSize, gridSize, gridSize);
            }
        }
        for (int i = 0; i < 5 && i < blockBag.size(); i++) {
            Block nextBlock = blockBag.get(i); //다음 블록
            int[][] shape = nextBlock.getShape();
            g.setColor(nextBlock.getColor());

            //각 블록을 그림
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

    void fill(Graphics g) {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int gridWidth = col * gridSize;
        int gridHeight = row * gridSize;

        int startX = (panelWidth - gridWidth) / 2; // 가로 중앙
        int startY = (panelHeight - gridHeight) / 2; // 세로 중앙

        for (int r = 0; r < row; r++) {
            for (int c = 0; c < col; c++) {
                if (grid[r][c] == 1) {
                    g.setColor(Color.GRAY); // 쌓인 블록 색

                } else {
                    g.setColor(Color.BLACK); // 배경색
                }
                g.fillRect(startX + c * gridSize, startY + r * gridSize, gridSize, gridSize);
                g.setColor(Color.GRAY);
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

    void BlockDown() {
        if (canMove(nowBlockRow + 1, nowBlockCol)) {
            nowBlockRow++;
        } else {
            placeBlock();
            clearFullRows();
            nowBlock = getNextBlock();
            nowBlockRow = 0;
            nowBlockCol = col / 2;
            holdUsed = false; // 새로운 블록이 나올 때마다 홀드 초기화 // 새로운 블록이 나올 때마다 홀드 초기화

            if (!canMove(nowBlockRow, nowBlockCol)) {
                // 게임 오버 로직
                gameOver = true; // 게임 오버 상태 설정
                // running = false; // 게임 오버 시 스레드 종료 (선택 사항)
                System.out.println("Game Over!");
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

                    if (gridRow >= row || gridCol < 0 || gridCol >= col ||
                            (gridRow >= 0 && grid[gridRow][gridCol] == 1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    void placeBlock() {
        int[][] shape = nowBlock.getShape();
        Score++;
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    grid[nowBlockRow + r][nowBlockCol + c] = 1;
                }
            }
        }
    }

    void rotateBlock() {
        int[][] currentShape = nowBlock.getShape();
        int[][] rotatedShape = new int[currentShape[0].length][currentShape.length];

        for (int r = 0; r < currentShape.length; r++) {
            for (int c = 0; c < currentShape[0].length; c++) {
                rotatedShape[c][currentShape.length - 1 - r] = currentShape[r][c];
            }
        }

        if (canPlaceRotatedBlock(rotatedShape)) {
            nowBlock.setShape(rotatedShape);
        }
    }

    boolean canPlaceRotatedBlock(int[][] rotatedShape) {
        for (int r = 0; r < rotatedShape.length; r++) {
            for (int c = 0; c < rotatedShape[r].length; c++) {
                if (rotatedShape[r][c] == 1) {
                    int gridRow = nowBlockRow + r;
                    int gridCol = nowBlockCol + c;

                    if (gridRow >= row || gridCol < 0 || gridCol >= col ||
                            (gridRow >= 0 && grid[gridRow][gridCol] == 1)) {
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
                for (int i = r; i > 0; i--) {
                    System.arraycopy(grid[i - 1], 0, grid[i], 0, col);
                }
                for (int c = 0; c < col; c++) {
                    grid[0][c] = 0;
                }
                r++; // 같은 행을 다시 확인
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
        //BlockHave = 0; -> 불필요한 변수 제거
        holdBlock = null;
        HoldBlockColor = null;
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