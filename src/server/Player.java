package server;

import model.game.Board;
import model.game.User;
import model.messages.ChatMessage;
import model.messages.MoveMessage;
import model.messages.NotificationMessage;
import model.messages.PlayerListMessage;
import util.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import client.view.FriendListView;
import dao.UserDAO;

import static util.Constants.ALPHABET;


public class Player extends Thread {
    public Socket socket;
    private MatchRoom matchRoom;
    private String name = "";
    private int score = 0;
    private String username ="";
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Game game;
    private Board board;
    private HashMap<String, Player> requestList;
    private String ownKey;
    private String requestedGameKey;
    public Player(Socket socket, MatchRoom matchRoom) {
        this.socket = socket;
        this.matchRoom = matchRoom;
        this.requestList = new HashMap<>();
        
        matchRoom.addPlayer(this);
        System.out.println(">> " + socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.PLAYER_CONNECTED + " " + "connected");
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Object input;
            while ((input = in.readObject()) != null) {
                //type string[]: tao nickname,join phong chờ, gui thách đấu, ....
                if (input instanceof String[]) {
                    String[] array = (String[]) input;
                    int length = array.length;
                    if (length > 0) {
                        String message = array[0];
                        switch (message) {
                            case "join":
                                String option = array[1];
                                switch (option) {
                                    //join vào waiting list
                                    case "start":
                                        leaveGame();
                                        matchRoom.joinWaitingList(this);
                                        break;
                                    //gui thach dau den player khac
                                    case "join":
                                        this.leaveGame();
                                        if (array.length == 3) {
                                            this.leaveGame();
                                            System.out.println("<< " + this.socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.NEW_JOIN_GAME_REQUEST + " " + array[2]);
                                            matchRoom.joinRequest(this, array[2]);
                                        }
                                        break;
                                    //đồng ý một thách đấu
                                    case "accept":
                                        this.leaveGame();
                                        if (array.length == 3) {
                                            System.out.println("<< " + this.socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.JOIN_GAME_REQUEST_ACCEPTED + " " + array[2]);
                                            matchRoom.acceptRequest(this, array[2]);
                                        }
                                        break;
                                    //từ chối thách đấu
                                    case "reject":
                                        if (array.length == 3) {
                                            System.out.println("<< " + this.socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.JOIN_GAME_REQUEST_REJECTED + " " + array[2]);
                                            matchRoom.rejectRequest(this, array[2]);
                                        }
                                        //cancel thách đấu đang gửi đi
                                    case "cancel":
                                        if (array.length == 2) {
                                            System.out.println("<< " + this.socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.JOIN_GAME_REQUEST_CANCELLED);
                                            matchRoom.cancelRequest(this);
                                        }
                                }
                                break;
                            //đặt tên nickname
                            case "name":
                                // Kiểm tra thông tin đăng nhập của người dùng
                                User user = matchRoom.checkUser(array[1], array[2]);

                                if (user != null) {
                                    if (matchRoom.getUserDAO().modifyStatus(user.getUsername(), "online")) {
                                        // Cập nhật trạng thái online và chấp nhận đăng nhập
                                        name = user.getUsername();
                                        score = user.getScore();
                                        username = user.getUsername();
                                        setKey();
                                        System.out.println(">> " + socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.NAME_ACCEPTED + " tên là: " + name);
                                        writeNotification(Constants.NotificationCode.NAME_ACCEPTED);
                                        matchRoom.sendMatchRoomList();
                                    }
                                } 
                                else if (matchRoom.getUserDAO().checkDuplicated(array[1])) {
                                    // Nếu tên đăng nhập đã tồn tại và đang online, gửi NAME_TAKEN
                                    System.out.println(">> " + socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.NAME_TAKEN + " Nickname đã tồn tại, thử lại!");
                                    writeNotification(Constants.NotificationCode.NAME_TAKEN);
                                } 
                                else {
                                    // Tên đăng nhập hoặc mật khẩu sai, gửi INVALID_NAME
                                    System.out.println("Tài khoản hoặc mật khẩu không đúng, thử lại.");
                                    writeNotification(Constants.NotificationCode.INVALID_NAME);
                                }
                                break;
                            case "request":
                                if (array.length > 1 && "playerList".equals(array[1])) {
                                    System.out.println("<< " + socket.getRemoteSocketAddress().toString() + " requesting player list");
                                    matchRoom.sendPlayerListToClient(this); // Sends the player list to the client
                                }
                                break;

                        }
                    }
                } else if (input instanceof Board) {
                    Board board = (Board) input;

                    // Print Board nhân được từ Client
                    System.out.println("<< " + socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.SEND_BOARD);
                    board.printBoard(true);

                    if (Board.isValid(board) && game != null) {
                        System.out.println(">> " + socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.BOARD_ACCEPTED);
                        writeNotification(Constants.NotificationCode.BOARD_ACCEPTED);
                        this.board = board;
                        //neu 2 player board hợp lệ thì start game
                        game.checkBoards();
                    } else if (game == null) {
                        System.out.println(">> " + socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.NOT_IN_GAME);
                        writeNotification(Constants.NotificationCode.NOT_IN_GAME);
                    } else {
                        System.out.println(">> " + socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.INVALID_BOARD);
                        writeNotification(Constants.NotificationCode.INVALID_BOARD);
                    }
                } else if (input instanceof MoveMessage) {
                    if (game != null) {
                        game.applyMove((MoveMessage) input, this);
                    }
                } else if (input instanceof ChatMessage) {
                    if (game != null) {
                        Player opponent = game.getOpponent(this);
                        if (opponent != null) {
                            opponent.writeObject(input);
                        }
                    }
                }
            }

        } catch (IOException e) {
            if (game != null) {
                leaveGame();
            } else {
                matchRoom.removeWaitingPlayer(this);
            }
            matchRoom.removePlayer(this);
            System.out.println(">> " + socket.getRemoteSocketAddress().toString() + " connected");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public String getPlayerName() {
        return name;
    }
    public int getPlayerScore() {
    	return score;
    }
    public String getPlayerUsername() {
    	return username;
    }
    public void plusScore(){
        score = score + 1;
    }
    //gui mess đến 1 client
    public void writeMessage(String message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //gui obj den client
    public void writeObject(Object object) {
        try {
            out.writeObject(object);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //gui thong bao den client co the kem them Thong tin bo sung
    public void writeNotification(int notificationMessage, String... text) {
        try {
            NotificationMessage nm = new NotificationMessage(notificationMessage, text);
            out.writeObject(nm);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Board getBoard() {
        return this.board;
    }

    // Send  game request đến player, và update req list, req game key
    //  requester là player gửi request
    public synchronized void sendRequest(Player requester) {
        requestList.put(requester.getOwnKey(), requester);
        requester.requestedGameKey = this.ownKey;
        System.out.println(">> " + socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.NEW_JOIN_GAME_REQUEST + " " + requester.ownKey);
        writeNotification(Constants.NotificationCode.NEW_JOIN_GAME_REQUEST, requester.getOwnKey(), requester.getPlayerName());
    }

    //  đối thủ chấp nhận một yêu cầu và thông báo cho người chơi
    //  opponent là player  đã accept request
    public synchronized void requestAccepted(Player opponent) {
        opponent.requestList.remove(ownKey);
        requestedGameKey = null;
        System.out.println(">> " + socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.JOIN_GAME_REQUEST_ACCEPTED + " " + this.ownKey);
        writeNotification(Constants.NotificationCode.JOIN_GAME_REQUEST_ACCEPTED);
    }

    // đối thủ từ chối một yêu cầu
    public synchronized void requestRejected(Player opponent) {
        opponent.requestList.remove(ownKey);
        requestedGameKey = null;
        System.out.println(">> " + socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.JOIN_GAME_REQUEST_REJECTED + " " + this.ownKey);
        writeNotification(Constants.NotificationCode.JOIN_GAME_REQUEST_REJECTED);
    }
    public void requestPlayerList() {
        writeNotification(Constants.NotificationCode.REQUEST_PLAYER_LIST);
    }

    //set own key cho player
    public void setKey() {
//        StringBuilder keyBuilder = new StringBuilder();
//        Random random = new Random();
//        int length = ALPHABET.length();
//        for (int i = 0; i < 10; ++i) {
//            keyBuilder.append(ALPHABET.charAt(random.nextInt(length)));
//        }
//        String key = keyBuilder.toString();
        this.ownKey = username;
    }

    public String getOwnKey() {
        return ownKey;
    }

    public void setRequestedGameKey(String key) {
        this.requestedGameKey = key;
    }

    // key của player gửi req
    public String getRequestedGameKey() {
        return requestedGameKey;
    }

    //Rejects all game invite
    public void rejectAll() {
        for (Player p : requestList.values()) {
            p.requestRejected(this);
        }
    }

    //End game và thông báo cho đối thủ
    public void leaveGame() {
        if (game != null) {
            Player opponent = game.getOpponent(this);
            System.out.println(">> " + socket.getRemoteSocketAddress().toString() + " " + Constants.NotificationCode.OPPONENT_DISCONNECTED + " ");
            opponent.writeNotification(Constants.NotificationCode.OPPONENT_DISCONNECTED);
            game.killGame();
        }
    }
}
