//package client.view;
//
//import client.Client;
//
//import javax.swing.*;
//import javax.swing.border.EmptyBorder;
//import javax.swing.event.ListSelectionEvent;
//import javax.swing.event.ListSelectionListener;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Map.Entry;
//
//public class WaitingRoomView extends JFrame {
//
//    private Client client;
//    private DefaultListModel<RoomPlayer> playersListModel = new DefaultListModel<RoomPlayer>();
//    private boolean firstTimeListing = true;
//    private HashMap<String, String[]> waitingList;
//    private JList<RoomPlayer> playersList;
//    private JButton sendInvite;
//    private JLabel playersNumber;
//
//    public WaitingRoomView() {
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            SwingUtilities.updateComponentTreeUI(this);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        JPanel mainPanel = new JPanel(new BorderLayout(10, 5));
//        setTitle("Game tàu chiến");
//        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
//
//        playersList = new JList<>();
//        playersList.setModel(playersListModel);
//        playersList.addMouseListener(new PlayersListMouseAdapter());
//        playersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        sendInvite = new JButton("Gửi thách đấu");
//        sendInvite.setEnabled(false);
//        sendInvite.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                RoomPlayer player = playersList.getSelectedValue();
//                client.sendJoinGameRequest(player.getKey(), player.getName());
//            }
//        });
//
//
//        playersList.addListSelectionListener(new ListSelectionListener() {
//            @Override
//            public void valueChanged(ListSelectionEvent e) {
//                sendInvite.setEnabled(true);
//            }
//        });
//
//        playersNumber = new JLabel("Người chơi trong sảnh: " + playersListModel.getSize());
//        playersNumber.setHorizontalAlignment(JLabel.CENTER);
//
//        mainPanel.add(playersNumber, BorderLayout.NORTH);
//        mainPanel.add(new JScrollPane(playersList), BorderLayout.CENTER);
//        mainPanel.add(sendInvite, BorderLayout.SOUTH);
//
//        add(mainPanel, BorderLayout.CENTER);
//        setVisible(true);
//        setLocationRelativeTo(null);
//        pack();
//
//        this.client = new Client(this);
//        createNickname();
//        client.joinLobby();
//
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    }
//
//    private class PlayersListMouseAdapter extends MouseAdapter {
//
//        public void mouseClicked(MouseEvent e) {
//            if (e.getClickCount() != 2) {
//                return;
//            }
//
//            RoomPlayer player = playersList.getSelectedValue();
//
//            if (player != null) {
//                client.sendJoinGameRequest(player.getKey(), player.getName());
//            }
//        }
//
//    }
//
//    private void createNickname() {
//        String message = "Nhập nickname của bạn.";
//        while (true) {
//            JPanel panel = new JPanel();
//            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//            panel.setBackground(new Color(230, 240, 250)); // Màu nền nhạt cho toàn bộ panel
//    
//            // Tên người dùng
//            JTextField usernameField = new JTextField(10);
//            JLabel usernameLabel = new JLabel("Tên người dùng:");
//            usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
//            usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
//    
//            // Khoảng cách và màu sắc cho label và field
//            panel.add(usernameLabel);
//            panel.add(Box.createRigidArea(new Dimension(0, 5)));
//            panel.add(usernameField);
//            panel.add(Box.createRigidArea(new Dimension(0, 10))); // Thêm khoảng cách giữa các thành phần
//    
//            // Mật khẩu
//            JPasswordField passwordField = new JPasswordField(10);
//            JLabel passwordLabel = new JLabel("Mật khẩu:");
//            passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
//            passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
//    
//            panel.add(passwordLabel);
//            panel.add(Box.createRigidArea(new Dimension(0, 5)));
//            panel.add(passwordField);
//            panel.add(Box.createRigidArea(new Dimension(0, 10)));
//    
//            // Tùy chọn hiển thị hộp thoại với nút OK và Cancel
//            UIManager.put("OptionPane.okButtonText", "Đăng nhập");
//            UIManager.put("OptionPane.cancelButtonText", "Hủy");
//    
//            // Hiển thị hộp thoại
//            int option = JOptionPane.showConfirmDialog(this, panel, message, 
//                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
//    
//            if (option == JOptionPane.OK_OPTION) {
//                String username = usernameField.getText();
//                String password = new String(passwordField.getPassword());
//    
//                if (username.isEmpty() || password.isEmpty()) {
//                    JOptionPane.showMessageDialog(this, "Vui lòng nhập cả tên người dùng và mật khẩu.");
//                    continue;
//                }
//    
//                this.client.sendName(username, password);
//    
//                synchronized (client) {
//                    try {
//                        if (client.getNameState() == Client.NameState.WAITING) {
//                            client.wait();
//                        }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//    
//                Client.NameState state = client.getNameState();
//                if (state == Client.NameState.ACCEPTED) {
//                    client.setOwnName(username);
//                    break;
//                } else if (state == Client.NameState.INVALID) {
//                    message = "Nickname không hợp lệ, thử lại.";
//                } else if (state == Client.NameState.TAKEN) {
//                    message = "Nickname đã tồn tại, thử lại.";
//                }
//            } else {
//                System.exit(0);
//            }
//        }
//    }
//    
//    
//
//    public boolean playerNameExists(String name) {
//        boolean exists = false;
//        for (Entry<String, String[]> entry : waitingList.entrySet()) {
//            if (entry.getValue().equals(name)) {
//                return true;
//            }
//        }
//        return exists;
//    }
//
//    public synchronized void updateWaitingList(HashMap<String, String[]> matchRoomList) {
//        this.waitingList = matchRoomList;
//        this.playersListModel.clear();
//        for (Entry<String, String[]> entry : matchRoomList.entrySet()) {
//            String key = entry.getKey();
//            if (!key.equals(client.getKey())) {
//                String[] data = entry.getValue();
//                RoomPlayer player = new RoomPlayer(key, data[0],data[1]); // data[0] là tên data[1] là score
//                this.playersListModel.addElement(player);
//            }
//        }
//        if (playersList.isSelectionEmpty()) {
//            sendInvite.setEnabled(false);
//        }
//        playersNumber.setText("Người chơi trong sảnh: " + playersListModel.getSize());
//    }
//
//    public static void main(String[] args) {
//        new WaitingRoomView();
//    }
//
//    private class RoomPlayer {
//
//        private String key;
//        private String name;
//        private String score;
//        public RoomPlayer(String key, String name,String score) {
//            this.key = key;
//            this.name = name;
//            this.score = score;
//        }
//
//        public String toString() {
//            return this.name;
//        }
//
//        public String getKey() {
//            return this.key;
//        }
//
//        public String getName() {
//            return this.name;
//        }
//        public String getScore() {
//        	return this.score;
//        }
//
//    }
//
//    public int showInitialConnectionError() {
//        String message = "Không thể kết nối đến server";
//        String options[] = {"Quit", "Retry"};
//        return JOptionPane.showOptionDialog(this, message,
//                "Lỗi kết nối", JOptionPane.OK_CANCEL_OPTION,
//                JOptionPane.ERROR_MESSAGE, null, options, options[1]);
//    }
//
//    public void showLostConnectionError() {
//        JOptionPane.showMessageDialog(this,
//                "Mất kết nối server.", "Connection Error",
//                JOptionPane.ERROR_MESSAGE);
//        System.exit(-1);
//    }
//}



package client.view;

import client.Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map.Entry;

public class WaitingRoomView extends JFrame {

    private Client client;
    private HashMap<String, String[]> waitingList;
    private JTable playersTable;
    private DefaultTableModel tableModel;
    private JButton sendInvite;
    private JLabel playersNumber;

    public WaitingRoomView() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel mainPanel = new JPanel(new BorderLayout(10, 5));
        setTitle("Game tàu chiến");
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Initialize the table model and playersTable
        tableModel = new DefaultTableModel(new Object[]{"ID","Tên", "Điểm"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        playersTable = new JTable(tableModel);
        playersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sendInvite = new JButton("Gửi thách đấu");
        sendInvite.setEnabled(false);
        sendInvite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = playersTable.getSelectedRow();
                if (selectedRow != -1) {
                    String key = (String) tableModel.getValueAt(selectedRow, 0);
                    String name = (String) tableModel.getValueAt(selectedRow, 1);
                    client.sendJoinGameRequest(key, name);
                }
            }
        });

        playersNumber = new JLabel("Người chơi trong sảnh: 0");
        playersNumber.setHorizontalAlignment(JLabel.CENTER);

        mainPanel.add(playersNumber, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(playersTable), BorderLayout.CENTER);
        mainPanel.add(sendInvite, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
        setLocationRelativeTo(null);
        pack();

        this.client = new Client(this);
        createNickname();
        client.joinLobby();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        playersTable.getSelectionModel().addListSelectionListener(e -> {
            sendInvite.setEnabled(playersTable.getSelectedRow() != -1);
        });
    }

    private void createNickname() {
        String message = "Nhập nickname của bạn.";
        while (true) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(new Color(230, 240, 250));

            JTextField usernameField = new JTextField(10);
            JLabel usernameLabel = new JLabel("Tên người dùng:");
            usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            usernameField.setFont(new Font("Arial", Font.PLAIN, 14));

            panel.add(usernameLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
            panel.add(usernameField);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));

            JPasswordField passwordField = new JPasswordField(10);
            JLabel passwordLabel = new JLabel("Mật khẩu:");
            passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
            passwordField.setFont(new Font("Arial", Font.PLAIN, 14));

            panel.add(passwordLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
            panel.add(passwordField);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));

            UIManager.put("OptionPane.okButtonText", "Đăng nhập");
            UIManager.put("OptionPane.cancelButtonText", "Hủy");

            int option = JOptionPane.showConfirmDialog(this, panel, message,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập cả tên người dùng và mật khẩu.");
                    continue;
                }

                this.client.sendName(username, password);

                synchronized (client) {
                    try {
                        if (client.getNameState() == Client.NameState.WAITING) {
                            client.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Client.NameState state = client.getNameState();
                if (state == Client.NameState.ACCEPTED) {
                    client.setOwnName(username);
                    break;
                } else if (state == Client.NameState.INVALID) {
                    message = "Nickname không hợp lệ, thử lại.";
                } else if (state == Client.NameState.TAKEN) {
                    message = "Nickname đã tồn tại, thử lại.";
                }
            } else {
                System.exit(0);
            }
        }
    }

    public synchronized void updateWaitingList(HashMap<String, String[]> matchRoomList) {
        this.waitingList = matchRoomList;
        tableModel.setRowCount(0);

        for (Entry<String, String[]> entry : matchRoomList.entrySet()) {
            String key = entry.getKey();
            if (!key.equals(client.getKey())) {
                String[] data = entry.getValue();
                String name = data[0];
                String score = data[1];
                tableModel.addRow(new Object[] {key,name, score});
            }
        }

        playersNumber.setText("Người chơi trong sảnh: " + tableModel.getRowCount());
    }

    public static void main(String[] args) {
        new WaitingRoomView();
    }
    private class RoomPlayer {

        private String key;
        private String name;
        private String score;
        public RoomPlayer(String key, String name,String score) {
            this.key = key;
            this.name = name;
            this.score = score;
        }

        public String toString() {
            return this.name;
        }

        public String getKey() {
            return this.key;
        }

        public String getName() {
            return this.name;
        }
        public String getScore() {
        	return this.score;
        }

    }

    public int showInitialConnectionError() {
        String message = "Không thể kết nối đến server";
        String options[] = {"Quit", "Retry"};
        return JOptionPane.showOptionDialog(this, message,
                "Lỗi kết nối", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.ERROR_MESSAGE, null, options, options[1]);
    }

    public void showLostConnectionError() {
        JOptionPane.showMessageDialog(this,
                "Mất kết nối server.", "Connection Error",
                JOptionPane.ERROR_MESSAGE);
        System.exit(-1);
    }
}
