package client.view;

import client.Client;
import model.game.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class WaitingRoomView extends JFrame {

    private Client client;
    private HashMap<String, String[]> waitingList;
    private ArrayList<User> playerList;
    private JTable playersTable;
    private DefaultTableModel tableModel;
    private JButton sendInvite;
    private JButton friendListButton;
    private JLabel playersNumber;

    public WaitingRoomView() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel mainPanel = new JPanel(new BorderLayout(10, 5));
        mainPanel.setBackground(new Color(230, 240, 250)); // Đặt màu nền cho mainPanel
        setTitle("Game tàu chiến");
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Initialize the table model and playersTable
        tableModel = new DefaultTableModel(new Object[]{ "Tên", "Điểm" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        playersTable = new JTable(tableModel);
        playersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sendInvite = new JButton("Gửi thách đấu");
        sendInvite.setEnabled(false);
        sendInvite.setBackground(new Color(200, 220, 240));
        sendInvite.setForeground(Color.BLACK);
        sendInvite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = playersTable.getSelectedRow();
                if (selectedRow != -1) {
                    String name = (String) tableModel.getValueAt(selectedRow, 0);
                    client.sendJoinGameRequest(name, name);
                }
            }
        });

        // Nút "Danh sách bạn bè"
        friendListButton = new JButton("Danh sách người chơi");
        friendListButton.setBackground(new Color(200, 220, 240));
        friendListButton.setForeground(Color.BLACK);
        friendListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	try {
					client.sendPlayerListRequest();
				
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
               // Mở giao diện FriendListView
            }
        });

        playersNumber = new JLabel("Người chơi trong sảnh: 0");
        playersNumber.setHorizontalAlignment(JLabel.CENTER);
        playersNumber.setForeground(new Color(60, 60, 60)); // Màu chữ cho JLabel

        mainPanel.add(playersNumber, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(playersTable), BorderLayout.CENTER);

        // Panel chứa các nút sendInvite và friendListButton
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(230, 240, 250));
        buttonPanel.add(sendInvite);
        buttonPanel.add(friendListButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

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
            usernameLabel.setForeground(new Color(60, 60, 60));
            usernameField.setFont(new Font("Arial", Font.PLAIN, 14));

            panel.add(usernameLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
            panel.add(usernameField);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));

            JPasswordField passwordField = new JPasswordField(10);
            JLabel passwordLabel = new JLabel("Mật khẩu:");
            passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
            passwordLabel.setForeground(new Color(60, 60, 60));
            passwordField.setFont(new Font("Arial", Font.PLAIN, 14));

            panel.add(passwordLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
            panel.add(passwordField);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));

            UIManager.put("OptionPane.background", new Color(230, 240, 250));
            UIManager.put("Panel.background", new Color(230, 240, 250));
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
                    message = "Tài khoản hoặc mật khẩu không đúng, thử lại!!";
                } else if (state == Client.NameState.TAKEN) {
                    message = "Nickname đã tồn tại, thử lại!!";
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
                tableModel.addRow(new Object[] {name, score});
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

        public RoomPlayer(String key, String name, String score) {
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

	public void updatePlayerList(ArrayList<User> playerList2) {
		// TODO Auto-generated method stub
		playerList = playerList2;
		System.out.println("so luong user trong db " +  playerList.size());
		if(playerList.size() >2) {
			new FriendListView(playerList);
		}
		
	}
}
