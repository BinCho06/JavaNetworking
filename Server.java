import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class Server {

    private ServerSocket serverSocket;
    private DatagramSocket udpSocket;
    private GameState gameState;
    private Game game;

    private static final int PORT = 4890;

    public Server(ServerSocket serverSocket, DatagramSocket udpSocket){
        this.serverSocket = serverSocket;
        this.udpSocket = udpSocket;
        this.gameState = new GameState();
        this.game = new Game(gameState);
    }

    public void start() {
        game.start();
        new Thread(this::listenForUDP).start();
        handleTCPclients();
    }

    private void handleTCPclients() {
        String uuid;
        try {
            while(!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                uuid=UUID.randomUUID().toString();
                System.out.println("A new client with uuid:"+uuid+" has connected!");

                Player player = new Player(uuid);
                ClientHandler clientHandler = new ClientHandler(socket, gameState, player);
                gameState.addPlayer(player); //TODO: add player to gameState before making a client handler or sth like that
                new Thread(clientHandler).start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForUDP() {
        int port;
        byte[] buffer;
        String message;
        String data;
        String uuid;
        DatagramPacket packet;
        InetAddress address;
        try {
            while(!udpSocket.isClosed()){
                buffer = new byte[256];
                //Receive UDP
                packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
                message = new String(packet.getData(), 0, packet.getLength());
                uuid = message.substring(0, 36);
                data = message.substring(36);
                System.out.println("Received from: " + uuid + " Data: "+data);
                //TODO: syncServerState(data);

                //Send UDP
                buffer = gameState.getData(uuid).getBytes();
                address = packet.getAddress();
                port = packet.getPort();
                packet = new DatagramPacket(buffer, buffer.length, address, port);
                udpSocket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if(serverSocket != null) serverSocket.close();
            if(udpSocket != null) udpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(PORT);
        DatagramSocket udpSocket = new DatagramSocket(PORT);
        Server server = new Server(serverSocket, udpSocket);
        server.start();
    }
}
