package server;

import model.game.User;
import model.messages.MatchRoomListMessage;
import model.messages.PlayerListMessage;
import util.Constants;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.ArayList;
import dao.UserDAO;
public class MatchRoom {
	UserDAO userDAO = new UserDAO();
    private HashMap<String, Player> waitingPlayerList;
    
    private ArrayList<Player> connectedPlayers;

    public MatchRoom() {
        this.waitingPlayerList = new HashMap<String, Player>();
        this.connectedPlayers = new ArrayList<>();
    }

    //  đưa key vào HashMap, gửi lại cho người chơi
    //  để người chơi join vào danh sách phòng chờ
    public synchronized void joinWaitingList(Player player) {
        waitingPlayerList.put(player.getOwnKey(), player);
        player.writeNotification(Constants.NotificationCode.GAME_TOKEN, player.getOwnKey());
        sendMatchRoomList();
    }

    //gửi request từ player đến player khác thông qua key
    public synchronized void joinRequest(Player player, String key) {
        Player opponent = waitingPlayerList.get(key);
        if (player == opponent) {
            player.writeNotification(Constants.NotificationCode.CANNOT_PLAY_YOURSELF);
        } else if (opponent != null) {
            opponent.sendRequest(player);
        }
    }

    //player acp request từ player có key được cung cấp
    public synchronized void acceptRequest(Player player, String key) {
        Player opponent = waitingPlayerList.get(key);
        if (opponent != null && opponent.getRequestedGameKey().equals(player.getOwnKey())) {
            waitingPlayerList.remove(key);
            waitingPlayerList.values().remove(player);
            opponent.requestAccepted(player);
            new Game(opponent, player);
            sendMatchRoomList();
            player.rejectAll();
            opponent.rejectAll();
        }
    }

    //player reject request từ player có key được cung cấp
    public synchronized void rejectRequest(Player player, String key) {
        Player opponent = waitingPlayerList.get(key);
        if (opponent != null &&
                opponent.getRequestedGameKey().equals(player.getOwnKey())) {
            opponent.requestRejected(player);
        }
    }

    // request từ player bị cancel
    public synchronized void cancelRequest(Player player) {
        Player opponent = waitingPlayerList.get(player.getRequestedGameKey());
        player.setRequestedGameKey(null);
        if (opponent != null) {
            opponent.writeNotification(
                    Constants.NotificationCode.JOIN_GAME_REQUEST_CANCELLED,
                    player.getOwnKey());
            System.out.println (">> " + opponent.socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.JOIN_GAME_REQUEST_CANCELLED );
        }
    }

    //remove player trong waitting room
    //Gửi lại danh sách chờ cho các player còn lại
    public synchronized void removeWaitingPlayer(Player player) {
        userDAO.modifyStatus(player.getPlayerUsername(),"offline");
        waitingPlayerList.values().remove(player);
        sendMatchRoomList();
    }

    //check player name đã tồn tại
    public User checkUser(String username,String password) {
    	return userDAO.verifyUser(username, password);
    }

    public boolean playerNameExists(String name) {
        for (Player player : connectedPlayers) {
            if (name.equals(player.getPlayerName())) {
                return true;
            }
        }
        return false;
    }
    

    //gui danh sach cách player khác trong sảnh chờ cho tất cả người chơi
    public synchronized void sendMatchRoomList() {
        HashMap<String, String[]> matchRoomList = new HashMap<String, String[]>();
        for (Map.Entry<String, Player> entry : waitingPlayerList.entrySet()) {
            String key = entry.getKey();
            Player player = entry.getValue();
            matchRoomList.put(key, new String[] {player.getPlayerName(), String.valueOf(player.getPlayerScore())});

        }
        MatchRoomListMessage message = new MatchRoomListMessage(matchRoomList);
        for (Map.Entry<String, Player> entry : waitingPlayerList.entrySet()) {
            Player player = entry.getValue();
            player.writeObject(message);
        }
    }
    public synchronized void sendPlayerListToClient(Player player) {
        // Fetch player list from DAO
        ArrayList<User> playerList;
        try {
            playerList = userDAO.getAllUsers();
                    // Create a message to send to client
        PlayerListMessage message = new PlayerListMessage(playerList);
        
        // Send message to the specific player (client)
        player.writeObject(message);
        System.out.println("gưi playlist den server gom co : " + playerList.size());
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // Assuming getAllPlayers() returns a list of User objects

    }
    
    //thêm player vào danh sách đã kết nối phong chờ
    public void addPlayer(Player player) {
        if (!connectedPlayers.contains(player)) {
            connectedPlayers.add(player);
        }
    }

    //xóa player khỏi danh sách đã kết nối phong chờ
    public void removePlayer(Player player) {
        connectedPlayers.remove(player);
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }
}
