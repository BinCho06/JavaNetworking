import java.awt.Graphics;

import javax.swing.JPanel;

public class Game extends Thread  {
    private boolean running = true;
    private final int TARGET_FPS = 60;
    private final long OPTIMAL_TIME = 1000 / TARGET_FPS;
    private GameState gameState;
    private Runnable sendDataCallback;
    private JPanel gamePanel;
    
    public Game(GameState gameState, Runnable sendDataCallback, JPanel gamePanel) {
        this.gameState = gameState;
        this.sendDataCallback = sendDataCallback;
        this.gamePanel = gamePanel;
    }

    public void run() {
        while (running) {
            long startTime = System.currentTimeMillis();

            gameState.update();
            sendDataCallback.run();
            gamePanel.repaint();

            long elapsedTime = System.currentTimeMillis() - startTime;
            long sleepTime = Math.max(OPTIMAL_TIME - elapsedTime, 0);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void render(Graphics g) {
        for (Player player : gameState.getPlayersClone()) {
            g.drawString(player.getUsername(), player.getX(), player.getY());
        }
    }
}