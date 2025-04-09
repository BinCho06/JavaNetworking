import java.util.ArrayList;

public class GameState {
    private ArrayList<Player> players = new ArrayList<>();

    public GameState() {

    }

    public void update() {
        for(Player player : players){
            player.update();
        }
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public String getData(String uuid){
        String data = uuid+" ";
        for (Player player : players) {
            data += player.toString() + " ";
        }
        return data;
    }
    public ArrayList<Player> getPlayers() {
        return players;
    }
    public Player getPlayer(String uuid) {
        for (Player player : players) {
            if (player.getUUID().equals(uuid)) {
                return player;
            }
        }
        return null;
    }
}