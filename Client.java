import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client {
    
    private Socket socket;
    private DatagramSocket udpSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String uuid;
    private String username;
    private Player player;
    private Game game;
    private GameState gameState;
    private JFrame frame;
    private JTextArea messageArea;

    public Client(Socket socket, DatagramSocket udpSocket) {
        try {
            this.socket = socket;
            this.udpSocket = udpSocket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            this.gameState = new GameState();
        } catch (IOException e) {
            closeEverything();
        }
    }

    private void initializeGUI() { //Have to replace this with a proper GUI
        frame = new JFrame("Client - " + username);
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Add a panel to represent the game state
        JPanel gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                game.render(g); // Call the render method of the game object
            }
        };
        gamePanel.setBackground(Color.WHITE);

        // Add a KeyListener to the gamePanel
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        player.up = true;
                        break;
                    case KeyEvent.VK_A:
                        player.left = true;
                        break;
                    case KeyEvent.VK_S:
                        player.down = true;
                        break;
                    case KeyEvent.VK_D:
                        player.right = true;
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        player.up = false;
                        break;
                    case KeyEvent.VK_A:
                        player.left = false;
                        break;
                    case KeyEvent.VK_S:
                        player.down = false;
                        break;
                    case KeyEvent.VK_D:
                        player.right = false;
                        break;
                }
            }
        });

        frame.add(gamePanel, BorderLayout.CENTER);

        // Add a panel for chat components
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());

        // Add a text area to display messages
        messageArea = new JTextArea(5, 20);
        messageArea.setEditable(false);
        messageArea.setFocusable(false);
        chatPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        // Add a text field for user input
        JTextField inputField = new JTextField();
        chatPanel.add(inputField, BorderLayout.SOUTH);

        // Add an action listener to send messages
        inputField.addActionListener(evt -> {
            String messageToSend = inputField.getText();
            inputField.setText("");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
            sendMessage(messageToSend);
        });

        frame.add(chatPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        game = new Game(gameState, this::sendPlayerMovement, gamePanel);
    }

    public void establishConnection() {
        usernameHandshake();
        getServerInfo();

        initializeGUI();
        game.start();
        
        listenForMessages();
        listenForUDP();
    }

    private void usernameHandshake(){
        try {
            while(username == null){
                username = JOptionPane.showInputDialog("Enter your username: ");
                if(username == null || username.trim().isEmpty()) {
                    username = "Guest" + (int)(Math.random()*100);
                }
                sendMessage(username);
                String serverMessage = bufferedReader.readLine();
                if(serverMessage.equals("USERNAME_TAKEN")) {
                    username = null;
                    JOptionPane.showMessageDialog(null, "Username already taken!");
                }
            }
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void sendPlayerMovement(){
        sendUDPpacket(uuid+serializePlayerMovement(player));
    }

    private String serializePlayerMovement(Player player){
        String data="";
        data += player.up?"1":"0";
        data += player.down?"1":"0";
        data += player.left?"1":"0";
        data += player.right?"1":"0";
        return data;
    }

    private void getServerInfo() {
        try {
            String serverMessage = bufferedReader.readLine(); //format: uuid player:x:y player:x:y...
            this.uuid = serverMessage.substring(0, 36);
            System.out.println("message: " + serverMessage);

            String[] players = serverMessage.substring(37).split(" ");
            Player tempPlayer;
            for (int i=0; i<players.length; i++) {
                String[] data = players[i].split(":");
                if(data.length != 3) continue;

                tempPlayer = new Player(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]));
                if(data[0].equals(username)) this.player = tempPlayer;
                gameState.addPlayer(tempPlayer);
            }
            
        } catch (Exception e) {
            closeEverything();
        }
    }

    private void sendMessage(String messageToSend) {
        try {
            bufferedWriter.write(messageToSend);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (Exception e) {
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
                        messageArea.append(message + "\n");
                        messageArea.setCaretPosition(messageArea.getDocument().getLength());
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
                        packetUUID = message.substring(0, 36);
                        data = message.substring(36);
                        
                        if(packetUUID.equals(uuid)) syncClientState(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void syncClientState(String data){
        String[] playersData = data.split(" ");
        ArrayList<String> receivedPlayerUsernames = new ArrayList<>();
        ArrayList<Player> playersToRemove = new ArrayList<>();
        
        for (String playerData : playersData) {
            String[] attributes = playerData.split(":");
            if (attributes.length != 3) continue; // Skip invalid data
    
            String playerUsername = attributes[0];
            int x = Integer.parseInt(attributes[1]);
            int y = Integer.parseInt(attributes[2]);

            receivedPlayerUsernames.add(playerUsername);
    
            // Check if this is the current client’s player
            if (playerUsername.equals(username)) {
                player.setX(x);
                player.setY(y);
            } else {
                // Add or update other players in the game state
                Player otherPlayer = gameState.getPlayerByUsername(playerUsername);
                if (otherPlayer == null) {
                    otherPlayer = new Player(playerUsername, x, y);
                    gameState.addPlayer(otherPlayer);
                } else {
                    otherPlayer.setX(x);
                    otherPlayer.setY(y);
                }
            }
        }

        // Remove players from gameState that are not in the receivedPlayerUsernames list
        for (Player player : gameState.getPlayers()) {
            if (!receivedPlayerUsernames.contains(player.getUsername())) {
                playersToRemove.add(player);
            }
        }
        for (Player playerToRemove : playersToRemove) {
            gameState.removePlayer(playerToRemove);
        }
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
        Socket socket = new Socket("localhost",4890);
        DatagramSocket udpSocket = new DatagramSocket();
        Client client = new Client(socket, udpSocket);
        client.establishConnection();
    }
}