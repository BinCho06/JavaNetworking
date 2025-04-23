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

    private static final int PORT = 4890;

    public Server(ServerSocket serverSocket, DatagramSocket udpSocket){
        this.serverSocket = serverSocket;
        this.udpSocket = udpSocket;
        this.gameState = new GameState();
    }

    public void start() {
        new Thread(this::updateGameState).start();
        new Thread(this::listenForUDP).start();
        handleTCPclients();
    }

    private void updateGameState() {
        try {
            while(!serverSocket.isClosed()) {
                gameState.update();
                Thread.sleep(1000 / 60);
            }
            System.out.println("Server closed!");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void handleTCPclients() {
        String uuid;
        try {
            while(!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                uuid=UUID.randomUUID().toString();

                ClientHandler clientHandler = new ClientHandler(socket, gameState, uuid);
                new Thread(clientHandler).start();
            }
            System.out.println("Server closed!");
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
                syncServerState(uuid, data);

                //Send UDP
                buffer = gameState.getVisibleData(uuid).getBytes();
                address = packet.getAddress();
                port = packet.getPort();
                packet = new DatagramPacket(buffer, buffer.length, address, port);
                udpSocket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void syncServerState(String uuid, String data) {
        Player player = gameState.getPlayer(uuid);
        if(player != null) {
            player.up = data.charAt(0) == '1';
            player.down = data.charAt(1) == '1';
            player.left = data.charAt(2) == '1';
            player.right = data.charAt(3) == '1';
        } else {
            System.out.println("Player with uuid: " + uuid + " not found!");
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
