import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class Server {

    private ServerSocket serverSocket;
    private DatagramSocket udpSocket;

    public Server(ServerSocket serverSocket, DatagramSocket udpSocket){
        this.serverSocket = serverSocket;
        this.udpSocket = udpSocket;
    }

    public void start() {
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
                ClientHandler clientHandler = new ClientHandler(socket, uuid);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForUDP() {
        byte[] buffer = new byte[256];
        try {
            while(!udpSocket.isClosed()){
                //Receive UDP
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received:" + message);

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
        ServerSocket serverSocket = new ServerSocket(4890);
        DatagramSocket udpSocket = new DatagramSocket(4890);
        Server server = new Server(serverSocket, udpSocket);
        server.start();
    }
}
