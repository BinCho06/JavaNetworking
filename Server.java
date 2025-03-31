import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class Server {

    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            String uuid;
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

    public void closeServer() {
        try {
            if(serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(4890);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
