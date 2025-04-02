import java.util.ArrayList;

public class Game extends Thread  {
    private boolean running = true;
    private final int TARGET_FPS = 60;
    private final long OPTIMAL_TIME = 1000 / TARGET_FPS; // milliseconds per frame
    private ArrayList<Player> players = new ArrayList<>();

    public void run() {
        while (running) {
            long startTime = System.currentTimeMillis();

            update();  // Update game logic
            render();  // Render game frame

            long elapsedTime = System.currentTimeMillis() - startTime;
            long sleepTime = Math.max(OPTIMAL_TIME - elapsedTime, 0); // ~60 FPS

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted: " + e.getMessage());
            }
        }
    }

    private void update() {
        for(Player player : players){
            player.move();
        }
    }

    private void render() {
        System.out.println("Rendering frame...");
    }
}