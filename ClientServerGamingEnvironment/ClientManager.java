/*
Name: Tasneem Y. Qat
ID:   0201502
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class ClientManager extends Thread{
    private final Socket serverSideSocket;                                      //for the passed socket from server side using this class' constructor
    private Player player;                                                      //for the passed player to be managed
    private static List<Player> waitingPlayers = new ArrayList<>();             //arraylist that keeps track of waiting player
    private OutputStream outstream = null;                                      //associated with the server side socket
    private InputStream inStream = null;                                        //associated with the server side socket
    private ArrayList<String> userStringResponse = new ArrayList<>();           //for if a player responds as text
    int userIntResponse = 0;                                                    //for if a player responds as num
    static String welcomeInterface = "Welcome! What would you like to do? Enter the number 1 or 2:\n1. Sign-up\n2. Already have an account? Log-in";
    static String signUpPrompt = "Enter a new username and password on separate lines:";
    static String logInPrompt = "Enter your username and password on separate lines:";
    private DataInputStream dataInputStream;                                    //to read Int answers from player

    public ClientManager(Socket serverSideSocket){
        this.serverSideSocket = serverSideSocket;
        try {
            outstream = serverSideSocket.getOutputStream();
            inStream = serverSideSocket.getInputStream();
            dataInputStream = new DataInputStream(inStream);
        } catch (IOException e) {
            System.out.println("Failed to create stream.");
        }
    }
    public OutputStream getOutputStream() {
        return outstream;
    }

    public void WriteToSocket(String message){
            //stream to write to client socket
            PrintStream printStream = new PrintStream(outstream);                           //print stream used to send text over
            printStream.println(message);
            printStream.flush();
    }

    public int ReadIntFromSocket(){
        try {
            //stream to read from client socket
            userIntResponse = dataInputStream.readInt();                                    //data input stream used to read Int from socket inout stream

        } catch (IOException e) {
            System.out.println("Failed to read input from client.");
        }
        return userIntResponse;
    }
    public ArrayList<String> ReadTextFromSocket(){
        try {
            //stream to read from client socket
            InputStreamReader inputStreamReader = new InputStreamReader(inStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);          //buffered reader to read text from socket input stream
            while(bufferedReader.ready()) {
                userStringResponse.add(bufferedReader.readLine());
            }

        } catch (IOException e) {
            System.out.println("Failed to read input from client.");
        }
        return userStringResponse;
    }

    private void SignUp() {
        WriteToSocket(signUpPrompt);                                                        //send a welcome message to player
        try {
            outstream.flush();
        } catch (IOException e) {
            System.out.println("Couldn't flush data.");
        }

        String username = ReadTextFromSocket().get(0);                                      //reads the username and password respectively from player
        String password = ReadTextFromSocket().get(1);
        boolean exists = false;
        for (int i=0; i<Server.getPlayers().size(); i++) {                                  //checks if the username exists in database(array of deserialzed players)
            if(username.equals(Server.getPlayers().get(i).username)) {
                exists = true;
            }
            }
        if(exists){
            //user already exists! send message to client to either enter another username or terminate.
            WriteToSocket("""
                    Username is already in use!
                    1. Enter another username
                    2. Terminate the program""");
            try {
                outstream.flush();
            } catch (IOException e) {
                System.out.println("Couldn't flush data.");
            }
            int response = ReadIntFromSocket();
            switch (response){
                case 1:
                    //enter another username
                    SignUp();                                                                //recursive
                case 2:
                    //terminate program
                    try {
                        serverSideSocket.close();
                        outstream.close();
                        inStream.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                default: break;
            }
        }
        else{
            //notify player that sign up is complete and log in automatically
            WriteToSocket("Sign up complete! Logging in..");
            player = new Player();
            player.username = username;                                                       //creating new player object and their info
            player.setPassword(password);
        }
        }

    private void LogIn() {
        WriteToSocket(logInPrompt);
        try {
            outstream.flush();
        } catch (IOException e) {
            System.out.println("Couldn't flush data.");
        }

        String username = ReadTextFromSocket().get(0);
        String password = ReadTextFromSocket().get(1);
        boolean userexists = false;
        boolean passcorrect = false;
        int playerindex=-1;
        for (int i=0; i<Server.getPlayers().size(); i++) {                                      //checks if the username exists in database(array of deserialzed players)
            if(username.equals(Server.getPlayers().get(i).username)) {
                userexists = true;
                if(password.equals(Server.getPlayers().get(i).getPassword())){                  //also checks if password matches found username
                    passcorrect = true;
                    playerindex = i;
                }
            }
        }
        if(!userexists){
            //notify player no such username exists, log in fails
            WriteToSocket("No such username exists. Login failed.");
            LogIn();                                                                            //recursive
        }
        else{
            if(!passcorrect){
                //let user know pass is not correct, log in fails
                WriteToSocket("Incorrect password. Login failed.");
                LogIn();                                                                       //recursive
            }
        }
        if(userexists & passcorrect){
            //start the game service
            WriteToSocket("Login successful. Starting the game service...");
            player = Server.getPlayers().get(playerindex);                                    //we fetch the already existing player from the players array
        }
    }

    //methods with sync to add and remove player from waiting queue
    private static synchronized void addToWaitingPlayers(Player player) {
        waitingPlayers.add(player);
    }

    private static synchronized Player removeFromWaitingPlayers(int index) {
        return waitingPlayers.remove(index);
    }

    @Override
    public void run(){
        //handle client here
        WriteToSocket(welcomeInterface);
        while(userIntResponse==0) {
            userIntResponse = ReadIntFromSocket();
        }
        switch (userIntResponse){
            case 1: //sign up
                SignUp();
                break;
            case 2: //log in
                LogIn();
                break;
            default: break;
        }
        WriteToSocket("""
                Welcome to the 5 question game! What would you like to do?
                1. Start a new game
                2. Show my results
                3. Show leaderboard
                4. Sign-out
                """);

        while(userIntResponse==0) {
            userIntResponse = ReadIntFromSocket();
        }
        boolean popFirstWaitingPlayer = true;
        Player waitingPlayer;
        switch (userIntResponse){
            case 1: //Start game
                if (waitingPlayers.isEmpty()) {                                        //no players requested a game, player must wait
                    //mark the current player as waiting
                    addToWaitingPlayers(player);
                    popFirstWaitingPlayer = false;
                    WriteToSocket("Waiting for another player to join...");
                    while(waitingPlayers.size()<2){};
                }
                else{
                    WriteToSocket("Connecting with opponent...");
                }
                    // Pair up the current player with the waiting player
                if(popFirstWaitingPlayer) {
                    waitingPlayer = removeFromWaitingPlayers(0);                //remove the first waiting player
                }
                else{
                    waitingPlayer = removeFromWaitingPlayers(1);                //remove the second waiting player
                }
                    GameService gameService = new GameService(player, Server.playersConnected.get(0), waitingPlayer, Server.playersConnected.get(1));
                    Server.playersConnected.remove(0);                          //create new GameService object and run it as a game thread
                    Server.playersConnected.remove(1);
                    gameService.start();
            case 2: //Show player results
                for(int i=0; i<player.gamesPlayed.size(); i++){
                    WriteToSocket("Date: " + player.gamesPlayed.get(i).gamePlayDate + "Opponent: " + player.gamesPlayed.get(i).opponent + "\n My points: " + player.gamesPlayed.get(i).playerPoints + " Opponent points: "+ player.gamesPlayed.get(i).opponentPoints + "\n");
                }
            case 3: //Show leaderboard
                ArrayList<Integer> playersCopy = null;
                ArrayList<Player> top5 = null;
                for(int i=0; i<Server.getPlayers().size(); i++){
                    playersCopy.add(Server.getPlayers().get(i).totalPoints);
                }

                for(int i=0; i<playersCopy.size(); i++){
                    int max = playersCopy.get(0);
                    for (int number : playersCopy) {                                            //find top 5 max points players and store them in top5 arraylist
                        if (number > max) {
                            max = number;
                            top5.add(Server.getPlayers().get(i));
                        }
                    }
                }
                for(int i=0; i<top5.size(); i++){
                    WriteToSocket(i+1 + ": " + "Username" + top5.get(i).username + "Total earned points: " + top5.get(i).totalPoints + "Number of games played: " + top5.get(i).gamesPlayed.size() + "\n");
                }
            case 4: //Sign-out
                WriteToSocket("Signing out.");
                try {
                    outstream.close();
                    inStream.close();
                    serverSideSocket.close();
                } catch (IOException e) {
                    System.out.println("Error while signing out.");
                }
            default: break;
        }

    }


}

