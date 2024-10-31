package model.messages;

import model.game.User;
import java.io.Serializable;
import java.util.ArrayList;

public class PlayerListMessage implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<User> playerList;

    public PlayerListMessage(ArrayList<User> playerList) {
        this.playerList = playerList;
    }

    public ArrayList<User> getPlayerList() {
        return playerList;
    }
}
