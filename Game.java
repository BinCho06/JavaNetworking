import java.awt.Graphics;

public class Game extends Thread  {
    private boolean running = true;
    private final int TARGET_FPS = 60;
    private final long OPTIMAL_TIME = 1000 / TARGET_FPS;
    private GameState gameState;
    
    public Game(GameState gameState) {
        this.gameState = gameState;
    }

    public void run() {
        while (running) {
            long startTime = System.currentTimeMillis();

            update();

            long elapsedTime = System.currentTimeMillis() - startTime;
            long sleepTime = Math.max(OPTIMAL_TIME - elapsedTime, 0);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        gameState.update();
    }

    public void render(Graphics g) {
        for (Player player : gameState.getPlayers()) {
            g.drawString(player.getUsername(), player.getX(), player.getY());
        }
    }
}