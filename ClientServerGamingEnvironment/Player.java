/*
Name: Tasneem Y. Qat
ID:   0201502
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.stream.Stream;

public class Player implements Serializable{
    public String username;
    private transient String password;
    public int totalPoints;
    public ArrayList<GameHistory> gamesPlayed;       //arraylist of type GameHistory to store opponent, opponentPoints, playerPoints, and gamePlayDate
    public static int servicePort = 12345;
    Socket clientSideSocket;

    private transient Stream serveroutputStream;
    static InputStream inputStream;
    static OutputStream outputStream;
    static Scanner scanner = new Scanner(System.in);
    static int clientResponse=0;

    public static void main(String[] args) {
        Player player = new Player();                   //new player object
        player.CreateSocket();                          //request to connect with server sent
        ReceiveMessage();                               //receive welcome interface from server with the options to log in or sign up
        while(clientResponse != 1 & clientResponse != 2){
            clientResponse = scanner.nextInt();         //read answer/choice from client
        }
        WriteIntToServer(clientResponse);               //respond with 1 or 2 for sign-up or log-in
        ReceiveMessage();                               //receive game interface with game options
        while(clientResponse>4 | clientResponse<1){
            clientResponse = scanner.nextInt();         //read answer/choice from client
        }
        WriteIntToServer(clientResponse);               //respond with 1-4 for game options
        switch (clientResponse){
            case 1://Start Game
                ReceiveMessage();                       //either a message to wait for opponent or that connection is being established
                ReceiveMessage();                       //to get ready for 1st question

                for(int i=0; i<5; i++) {
                    ReceiveMessage();                   //receive question
                    clientResponse = scanner.nextInt(); //answer
                    WriteIntToServer(clientResponse);   //send answer
                }
                ReceiveMessage();                       //Results of the game
            case 2: // Show my results
                ReceiveMessage();                       //Results of all games
            case 3: // Show leaderboard
                ReceiveMessage();                       //Display Leaderboard
            case 4: //Sign-out
                ReceiveMessage();                       //Goodbye message
                try {
                    inputStream.close();
                    outputStream.close();
                    player.clientSideSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    private void CreateSocket() {
        try {
            clientSideSocket = new Socket("localhost", servicePort);
            inputStream = clientSideSocket.getInputStream();
            outputStream = clientSideSocket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Failed to create client socket.");
        }
    }
    private static void ReceiveMessage() {                                          //uses buffered reader to read text from server
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Failed to read from client");
        }

    }

    private static void WriteIntToServer(int response) {                            //uses data output stream to write Int to server
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        try {
            dataOutputStream.writeInt(response);
            outputStream.flush();
            dataOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
