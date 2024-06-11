/*
Name: Tasneem Y. Qat
ID:   0201502
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
public class GameRound extends Thread {                                          //a class that represents the round of a game, typically will happen 5 times
    private Player player;
    private Socket socket;

    private int answer;
    public Date dateAnswered;

    public GameRound(Player player, Socket socket) {
        this.player = player;
        this.socket = socket;
    }
    public int getAnswer() {
        return answer;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            answer = dataInputStream.readInt();                                       //as soon as answer is read, create date object to record time
            dateAnswered = new Date();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
