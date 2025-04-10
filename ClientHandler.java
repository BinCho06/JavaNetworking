import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String uuid;
    private String username; //TODO fix this player nonsense
    private Player player;
    private GameState gameState;

    public ClientHandler(Socket socket, GameState gameState, Player player){
        try {
            this.gameState = gameState;
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            this.username = bufferedReader.readLine(); //TODO more client info

            player.setUsername(username);
            this.player = player;
            this.uuid = player.getUUID();
            
            bufferedWriter.write(uuid+" "+player.getPlayer()+gameState.getData());
            bufferedWriter.newLine();
            bufferedWriter.flush();

            clientHandlers.add(this);
            broadcastMessage("SERVER: " + username + " has entered the chat!");
        } catch (IOException e) {
            closeEverything();
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while(socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(username+": "+messageFromClient);
            } catch (IOException e) {
                closeEverything();
                break;
            }
        }
    }

    public void broadcastMessage(String message) {
        for(ClientHandler clientHandler : clientHandlers) {
            try {
                if(!clientHandler.uuid.equals(uuid)) {
                    clientHandler.bufferedWriter.write(message);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything();
            }
        }
    }

    public void removeClientHandler() {
        //gameState.removePlayer(player); TODO consider adding the gamestate object to this class aswell
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + username + " has left the chat!");
        System.out.println("A client with uuid:"+uuid+" has disconnected!");
    }

    public void closeEverything() {
        removeClientHandler();
        try {
            if(bufferedReader != null) bufferedReader.close();
            if(bufferedWriter != null) bufferedWriter.close();
            if(socket != null) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }
    public String getUUID() {
        return uuid;
    }
}
