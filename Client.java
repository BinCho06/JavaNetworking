import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    
    private Socket socket;
    private DatagramSocket udpSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String uuid;
    private String username;

    public Client(Socket socket, DatagramSocket udpSocket, String username) {
        try {
            this.socket = socket;
            this.udpSocket = udpSocket;
            //socket.setSoTimeout(1000); //handle server close
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void establishConnection() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            this.uuid = bufferedReader.readLine();

            listenForMessages();
            listenForUDP();
            sendMessage();
        } catch (IOException e) {
            closeEverything();
            System.out.println("Server handshake failed");
        }
    }

    private void sendMessage() {
        Scanner scanner = new Scanner(System.in);
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            while(socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(username+": "+messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (Exception e) {
            scanner.close();
            closeEverything();
        }
    }

    private void listenForMessages(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message;
                while(socket.isConnected()) {
                    try {
                        message = bufferedReader.readLine();
                        //if (message == null) throw new IOException(); //server closes
                        System.out.println(message);
                    //} catch (SocketTimeoutException e) {
                    } catch (IOException e) {
                        closeEverything();
                    }
                }
            }
        }).start();
    }

    private void listenForUDP() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[256];
                String message;
                String packetUUID;
                try {
                    while(!udpSocket.isClosed()){
                        //Receive UDP
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        udpSocket.receive(packet);
                        message = new String(packet.getData(), 0, packet.getLength());
                        
                        System.out.println("Received UDP:" + message);
                        packetUUID = message.split(" ")[0];
                        //todo

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void closeEverything() {
        try {
            if(bufferedReader != null) bufferedReader.close();
            if(bufferedWriter != null) bufferedWriter.close();
            if(socket != null) socket.close();
            if(udpSocket != null) udpSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        String username = "Guest"+(int)(Math.random()*100);

        Socket socket = new Socket("localhost",4890);
        DatagramSocket udpSocket = new DatagramSocket(4890);
        Client client = new Client(socket, udpSocket, username);
        client.establishConnection();
    }
}