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

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public String getAllData(){
        String data="";
        for (Player player : players) {
            data +=  " "+player.toString();
        }
        return data;
    }
    public String getVisibleData(String uuid){
        String data=uuid;
        for (Player player : players){
            if(Math.abs(player.getX() - getPlayer(uuid).getX()) > 100 || player.getUUID().equals(uuid)) continue;
            data +=  " "+player.toString();
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
    public Player getPlayerByUsername(String username) {
        for (Player player : players) {
            if (player.getUsername().equals(username)) {
                return player;
            }
        }
        return null;
    }
}