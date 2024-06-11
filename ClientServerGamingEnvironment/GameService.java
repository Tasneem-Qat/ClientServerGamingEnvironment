/*
Name: Tasneem Y. Qat
ID:   0201502
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class GameService extends Thread{
    private final ArrayList<Question> questions = Server.getQuestions();                    //get all question objects from server
    private ArrayList<Question> gameQuestions= new ArrayList<>();                           //will contain the 5 questions to ask players
    private Player player1;
    private Socket socket1;
    private int gamePoints1;                                                                //player1's points in one game
    private Player player2;
    private Socket socket2;
    private int gamePoints2;                                                                //player2's points in one game
    public GameService(Player player1, Socket socket1, Player player2, Socket socket2) {
        this.player1 = player1;
        this.socket1 = socket1;
        this.player2 = player2;
        this.socket2 = socket2;
        gameQuestions = get5Questions();
    }

    private ArrayList<Question> get5Questions() {
        ArrayList<Question> questionsCopy = new ArrayList<>();                               //makes a copy of the original questions arraylist
        for (int i=0; i<questions.size(); i++){
            questionsCopy.add(questions.get(i));
        }
        ArrayList<Question> chosenQuestions = new ArrayList<>();
        for (int i = 0; i<5; i++) {                                                         //picks random 5 questions without repetitions
            int randomIndex = (int) (Math.random() * questionsCopy.size());
            chosenQuestions.add(questionsCopy.get(randomIndex));
            questionsCopy.remove(randomIndex);
        }
        return chosenQuestions;
    }

    @Override
    public void run() {
        Date date = new Date();
        writeToPlayer(player1, socket1, "Starting Game, get ready!");
        writeToPlayer(player2, socket2, "Starting Game, get ready!");
        gamePoints1 =0;
        gamePoints2 =0;

        for (int i=0; i<gameQuestions.size(); i++){

            askQuestion(gameQuestions.get(i), i+1);
            GameRound player1Round = new GameRound(player1, socket1);                       //creates 2 GameRound threads, one for each player, so the can play in parallel
            GameRound player2Round = new GameRound(player2, socket2);

            player1Round.start();
            player2Round.start();


            try {
                player1Round.join();                                                        //threads have to wait for each other to finish
                player2Round.join();

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            //compare dates and answers to determine round winner
            if(player1Round.getAnswer() == gameQuestions.get(i).getCorrectAnswer() & player2Round.getAnswer() == gameQuestions.get(i).getCorrectAnswer()) {
                if (player1Round.dateAnswered.compareTo(player2Round.dateAnswered) < 0) {
                    //player 1 wins points
                    player1Wins(i);
                }
                else if(player1Round.dateAnswered.compareTo(player2Round.dateAnswered) > 0){
                    //player 2 wins points
                    player2Wins(i);
                }
            }
            else if(player1Round.getAnswer() == gameQuestions.get(i).getCorrectAnswer()){
                //player 1 wins
                player1Wins(i);
                }
            else if(player2Round.getAnswer() == gameQuestions.get(i).getCorrectAnswer()){
                //player 2 wins
                player2Wins(i);
                }
            else{
                //nobody wins
                writeToPlayer(player1,socket1, "The correct answer is: " + gameQuestions.get(i).getCorrectAnswer() + "\n No winner this round!");
                writeToPlayer(player2,socket2, "The correct answer is: " + gameQuestions.get(i).getCorrectAnswer() + "\n No winner this round!");
            }
        }
        if(gamePoints1>gamePoints2){
            //player1 wins game
            writeToPlayer(player1,socket1, "The winner is: " + player1.username + " with points " + gamePoints1 + " Vs." + gamePoints2);
            writeToPlayer(player2,socket2, "The winner is: " + player1.username + " with points " + gamePoints1 + " Vs." + gamePoints2);
        }
        else{
            //player2 wins game
            writeToPlayer(player1,socket1, "The winner is: " + player2.username + " with points " + gamePoints2 + " Vs." + gamePoints1);
            writeToPlayer(player2,socket2, "The winner is: " + player2.username + " with points " + gamePoints2 + " Vs." + gamePoints1);
        }
        player1.gamesPlayed.add(new GameHistory(player2.username, gamePoints2, gamePoints1, date));
        player2.gamesPlayed.add(new GameHistory(player1.username, gamePoints1, gamePoints2, date));
        }



    private void askQuestion(Question question, int i) {
        //Send question to players
        writeToPlayer(player1, socket1, "Question" + i + ": " + question.getQuestion() + "\n" + question.getChoices());
        writeToPlayer(player2, socket2, "Question" + i + ": " + question.getQuestion() + "\n" + question.getChoices());

    }

    //uses print stream to send strings to players
    private void writeToPlayer(Player player, Socket socket, String message) {
        OutputStream outputStream;
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PrintStream printStream = new PrintStream(outputStream);
        printStream.print(message);
    }

    //display winning player and round results to players
    private void player1Wins(int i){
        player1.totalPoints+=gameQuestions.get(i).getPoints();
        gamePoints1+=gameQuestions.get(i).getPoints();
        writeToPlayer(player1,socket1, "The correct answer is: " + gameQuestions.get(i).getCorrectAnswer() + "\n The winner this round is " + player1.username);
        writeToPlayer(player2,socket2, "The correct answer is: " + gameQuestions.get(i).getCorrectAnswer() + "\n The winner this round is " + player1.username);
    }

    private void player2Wins(int i){
        player2.totalPoints+=gameQuestions.get(i).getPoints();
        gamePoints2+=gameQuestions.get(i).getPoints();
        writeToPlayer(player1,socket1, "The correct answer is: " + gameQuestions.get(i).getCorrectAnswer() + "\n The winner this round is " + player2.username);
        writeToPlayer(player2,socket2, "The correct answer is: " + gameQuestions.get(i).getCorrectAnswer() + "\n The winner this round is " + player2.username);
    }

}
