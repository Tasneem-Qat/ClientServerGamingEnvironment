/*
Name: Tasneem Y. Qat
ID:   0201502
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class Server {
    public static final int servicePort = 12345;
    private static String filename = "questions.out";                               //file to deserialize questions from
    private static ArrayList<Question> questions = new ArrayList<>();               //arraylist of deserialized question objects
    private static ArrayList<Player> players = new ArrayList<>();                   //arraylist of player objects deserialized from players.out
    private static ServerSocket serverSocket;
    private static Socket serverSide;
    public static boolean flag=false;                                               //for detecting issues with deserialization
    public static ArrayList<Socket> playersConnected = new ArrayList<>();           //arraylist of sockets of server-connected players

    public static void main(String[] args) {
        ServerStart();
    }

    public static void ServerStart(){
        DeserializeQuestions(filename);
        if (flag==true){
            System.out.println("Could not fetch game questions.");
            return;
        }
        DeserializePlayers("players.out");
        if (flag==true){
            System.out.println("Could not fetch player data.");
            return;
        }
        try {
            serverSocket = new ServerSocket(servicePort);                            //creating server socket bound to specific service port
            for(;;){
                serverSide = serverSocket.accept();
                playersConnected.add(serverSide);                                    //adding accepted player's socket to the arraylist
                ClientManager clientManager = new ClientManager(serverSide);         //creating a clientmanager object to manage registeration in serparate thread
                clientManager.start();                                               //thread starts while server continues listening
            }
        }
        catch (IOException ioe) {
            System.out.println("Unable to create socket.");
        }


    }
    public static ArrayList<Question> getQuestions() {
        return questions;
    }

    public static ArrayList<Player> getPlayers() {
        return players;
    }
    public static void DeserializeQuestions(String filename){
        try {
            FileInputStream qfileinput = new FileInputStream(filename);                //input stream that reads from questions.out file
            ObjectInputStream qdeserializer = new ObjectInputStream(qfileinput);       //object input stream to deserialize the question objects

            Object oArray = qdeserializer.readObject();                                //creating new arraylist of question objects
            ArrayList q = (ArrayList) oArray;
            for (int i = 0; i <q.size(); i++) {
                questions.add((Question)q.get(i));
            }
        }
        catch(ClassNotFoundException cnfe){
            System.out.println("Class not found.");
            cnfe.printStackTrace();
            flag = true;
        }
        catch (FileNotFoundException ex) {
            System.out.println("File doesn't exist");
        }
        catch(IOException ioe){
            System.out.println("Unable to deserialize.");
        }
    }

    public static void DeserializePlayers(String filename){
        try {
            FileInputStream qfileinput = new FileInputStream(filename);                //Input stream that reads from file
            ObjectInputStream qdeserializer = new ObjectInputStream(qfileinput);       //Object input stream to deserialize the Player objects

            Object oArray = qdeserializer.readObject();
            ArrayList p = (ArrayList) oArray;
            for (int i = 0; i <p.size(); i++) {
                players.add((Player)p.get(i));
            }

        }
        catch(ClassNotFoundException cnfe){
            System.out.println("Class not found.");
            flag = true;
        }
        catch (FileNotFoundException ex) {
            System.out.println("File doesn't exist");
            flag = true;
        }
        catch(IOException ioe){
            System.out.println("Unable to deserialize.");
        }
    }
}
