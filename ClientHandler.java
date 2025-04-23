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
    private String username;
    private Player player;
    private GameState gameState;

    public ClientHandler(Socket socket, GameState gameState, String uuid){
        try {
            this.uuid = uuid;
            this.gameState = gameState;
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            usernameHandshake();

            this.player = new Player(uuid, username);
            this.gameState.addPlayer(player);
            
            bufferedWriter.write(gameState.getVisibleData(uuid));
            bufferedWriter.newLine();
            bufferedWriter.flush();

            clientHandlers.add(this);
            broadcastMessage("SERVER: " + username + " has entered the chat!");
            System.out.println("A client with uuid:"+uuid+" and username: "+username+" has connected!");
        } catch (IOException e) {
            closeEverything();
        }
    }

    private void usernameHandshake() throws IOException {
        this.username = bufferedReader.readLine();
        while(gameState.getPlayerByUsername(username) != null) {
            bufferedWriter.write("USERNAME_TAKEN");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            username = bufferedReader.readLine();
        }
        bufferedWriter.write("USERNAME_ACCEPTED");
        bufferedWriter.newLine();
        bufferedWriter.flush();
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
                clientHandler.bufferedWriter.write(message);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            } catch (IOException e) {
                closeEverything();
            }
        }
    }

    public void removeClientHandler() {
        gameState.removePlayer(player);
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + username + " has left the chat!");
        System.out.println("A client with uuid:"+uuid+" and username: "+username+" has disconnected!");
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
