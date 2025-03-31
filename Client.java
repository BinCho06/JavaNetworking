import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class Client {
    
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            socket.setSoTimeout(1000); //handle server close
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void sendMessage() {
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

    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message;
                
                while(socket.isConnected()) {
                    try {
                        message = bufferedReader.readLine();
                        if (message == null) throw new IOException(); //server closes
                        System.out.println(message);
                    } catch (SocketTimeoutException e) {
                    } catch (IOException e) {
                        closeEverything();
                    }
                }
            }
        }).start();
    }

    public void closeEverything() {
        try {
            if(bufferedReader != null) bufferedReader.close();
            if(bufferedWriter != null) bufferedWriter.close();
            if(socket != null) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        String username = "Guest"+(int)(Math.random()*100);
        Socket socket = new Socket("localhost",4890);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
    }
}