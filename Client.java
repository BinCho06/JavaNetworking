import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
            sendMessages();
        } catch (IOException e) {
            closeEverything();
            System.out.println("Server handshake failed");
        }
    }

    private void sendMessages() {
        Scanner scanner = new Scanner(System.in);
        String messageToSend;
        try {
            while(socket.isConnected()) {
                messageToSend = scanner.nextLine();
                if(messageToSend.equals("udp")){ //temporary (testing only)
                    sendUDPpacket(uuid+" "+messageToSend);
                    continue;
                }
                bufferedWriter.write(messageToSend);
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

    public void sendUDPpacket(String message){
        byte[] buffer = new byte[256];
        DatagramPacket packet;
        InetAddress address;
        try {
            buffer = message.getBytes();
            address = InetAddress.getByName("localhost");
            packet = new DatagramPacket(buffer, buffer.length, address, 4890);
            udpSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForUDP() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[256];
                String message;
                String packetUUID;
                String data;
                try {
                    while(!udpSocket.isClosed()){
                        //Receive UDP
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        udpSocket.receive(packet);
                        message = new String(packet.getData(), 0, packet.getLength());
                        
                        //System.out.println("Received UDP: " + message);
                        packetUUID = message.split(" ")[0];
                        data = message.split(" ")[1];
                        //todo
                        if(packetUUID.equals(uuid)) syncServerState(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void syncServerState(String data){
        //TODO
        System.out.println("UDP Server: "+data);
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
        DatagramSocket udpSocket = new DatagramSocket();
        Client client = new Client(socket, udpSocket, username);
        client.establishConnection();
    }
}