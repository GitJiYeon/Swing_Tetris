package SwingTetris;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.util.Random;

public class TetrisPanel extends JPanel implements ActionListener {
    int row = 20;//ㅅㄹ
    int col = 10;//ㄱㄹ
    int[][] grid = new int[row][col]; // 맵 사이즈
    int gridSize = 30; // 한칸 사이즈
    Timer timer; // 타이머
    int nowBlockRow = 0; // 블록이 있는 위치
    int nowBlockCol = col / 2; // 블록이 있는 위치(중앙 위)
    int Timerturm;
    
    Block nowBlock;
    Random random = new Random(); // 랜덤 블록 생성을 위한 랜덤 객체
    
    // 블록 모양
    int[][] blockI = {{1, 1, 1, 1}};
    int[][] blockT = {{1, 1, 1},{0, 1, 0}};
    int[][] blockO = {{1, 1},{1, 1}};
    int[][] blockL = {{1, 1},{0, 1},{0, 1}};
    int[][] blockJ = {{1, 1},{1, 0},{1, 0}};
    int[][] blockZ = {{1, 1},{0, 1, 1}};
    int[][] blockS = {{0, 1, 1},{1, 1}};
    
    Block[] blocks = {
        new Block(blockI), new Block(blockT), new Block(blockO), 
        new Block(blockL), new Block(blockJ), new Block(blockZ), new Block(blockS)
    };

    public TetrisPanel() {
        Timerturm = 300;
        timer = new Timer(Timerturm, this); // 300ms마다 액션퍼포머 호출
        timer.start();
        setFocusable(true);
        requestFocusInWindow();  // 포커스를 패널로

        // 처음 블록 설정
        nowBlock = blocks[random.nextInt(blocks.length)];
        
        // KeyBindings 설정
        setKeyBindings();
        repaint();
    }

    private void setKeyBindings() {
        // 왼쪽 화살표 키
        this.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
        this.getActionMap().put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (nowBlockCol > 0 && grid[nowBlockRow][nowBlockCol - 1] == 0) {
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
                if (nowBlockCol < col - 1 && grid[nowBlockRow][nowBlockCol + 1] == 0) {
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
                BlockDown(); // 빠르게 떨어뜨리기
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        fill(g);
        
        int[][] shape = nowBlock.Getshape();
        int startX = (getWidth() - col * gridSize) / 2; // 그리드 가로 중앙
        int startY = (getHeight() - row * gridSize) / 2; // 그리드 세로 중앙
        int RandomColor = (int)(Math.random()*6+1);
        switch(RandomColor){
        	case 1: g.setColor(new Color(0xADD8E6)); break;//하늘
        	case 2: g.setColor(new Color(0xFFC0CB)); break;//분홍
        	case 3: g.setColor(new Color(0xFFFACD)); break;//노랑
        	case 4: g.setColor(new Color(0x98FB98)); break;//연두
        	case 5: g.setColor(new Color(0xE6E6FA)); break;//보라
        	case 6: g.setColor(new Color(0xFFDAB9)); break;//주황
        }

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    g.fillRect(startX + (nowBlockCol + c) * gridSize, startY + (nowBlockRow + r) * gridSize, gridSize, gridSize);
                    g.setColor(Color.LIGHT_GRAY); // 블록 테두리
                    g.drawRect(startX + (nowBlockCol + c) * gridSize, startY + (nowBlockRow + r) * gridSize, gridSize, gridSize);
                    switch(RandomColor){
                	case 1: g.setColor(new Color(0xADD8E6)); break;//하늘
                	case 2: g.setColor(new Color(0xFFC0CB)); break;//분홍
                	case 3: g.setColor(new Color(0xFFFACD)); break;//노랑
                	case 4: g.setColor(new Color(0x98FB98)); break;//연두
                	case 5: g.setColor(new Color(0xE6E6FA)); break;//보라
                	case 6: g.setColor(new Color(0xFFDAB9)); break;//주황
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
    public void actionPerformed(ActionEvent e) {
        BlockDown();
        repaint();
    }

    void BlockDown() {
        if (Move(nowBlockRow + 1, nowBlockCol)) {
            nowBlockRow++;
        } else {
            placeBlock();
            nowBlock = blocks[random.nextInt(blocks.length)]; // 새로운 블록을 랜덤으로 설정
            nowBlockRow = 0;
            nowBlockCol = col / 2;
        }
    }

    boolean Move(int nowRow, int nowCol) {
        int[][] shape = nowBlock.Getshape();
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    int gridRow = nowRow + r;
                    int gridCol = nowCol + c;
                    
                    if (gridRow >= row || gridCol < 0 || gridCol >= col || grid[gridRow][gridCol] == 1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    void placeBlock() {
        int[][] shape = nowBlock.Getshape();
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    grid[nowBlockRow + r][nowBlockCol + c] = 1;
                }
            }
        }
    }
}

// 블록들
class Block {
    int[][] shape;

    public Block(int[][] shape) {
        this.shape = shape;
    }

    public int[][] Getshape() {
        return shape;
    }
}
