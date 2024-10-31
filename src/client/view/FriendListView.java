package client.view;

import client.Client;
import dao.UserDAO;
import model.game.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FriendListView extends JFrame {
    private JTable friendsTable;
    private DefaultTableModel tableModel;
    private JButton backButton;
    ArrayList<User> playerList;
    public FriendListView(ArrayList<User> playerList) {


        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Danh sách bạn bè");
        setSize(400, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 5));
        mainPanel.setBackground(new Color(230, 240, 250));
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Table model setup with columns "Tên", "Điểm", "Trạng thái"
        tableModel = new DefaultTableModel(new Object[]{"Tên", "Điểm", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        friendsTable = new JTable(tableModel);
        friendsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(friendsTable);

        // loadFriendData();
        updatePlayerList(playerList);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Back button setup
        backButton = new JButton("Quay lại");
        backButton.setBackground(new Color(200, 220, 240));
        backButton.setForeground(Color.BLACK);
        backButton.addActionListener(e -> dispose());
        mainPanel.add(backButton, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }


    public void updatePlayerList(ArrayList<User> playerList) {
        // Xóa tất cả các hàng hiện có trước khi thêm dữ liệu mới
        tableModel.setRowCount(0);
        
        for (User user : playerList) {
            tableModel.addRow(new Object[]{user.getUsername(), user.getScore(), user.getStatus()});
            System.out.println("Username: " + user.getUsername() + ", Score: " + user.getScore());
        }
    }

}
