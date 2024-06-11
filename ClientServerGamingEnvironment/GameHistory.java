/*
Name: Tasneem Y. Qat
ID:   0201502
 */

import java.util.Date;

public class GameHistory {                                          //a class representing a game played, along with its info
    public String opponent;                                         //player who played against the player object
    public int opponentPoints;                                      //with their in-game points
    public int playerPoints;                                        //player's in-game points
    public Date gamePlayDate;                                       //date of when the game first started

    public GameHistory(String opponent, int opponentPoints, int playerPoints, Date date){
        this.opponent = opponent;
        this.opponentPoints = opponentPoints;
        this.playerPoints = playerPoints;
        this.gamePlayDate = date;
    }
}
