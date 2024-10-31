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
import java.util.List;

public class FriendListView extends JFrame {
    private Client client;
    private JTable friendsTable;
    private DefaultTableModel tableModel;
    private JButton backButton;

    public FriendListView(Client client) {
        this.client = client;

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

        loadFriendData();

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

    // Load friend data from UserDAO and display in table
    private void loadFriendData() {
        UserDAO userDAO = new UserDAO();
        try {
            List<User> users = userDAO.getAllUsers();
            tableModel.setRowCount(0);
            for (User user : users) {
                tableModel.addRow(new Object[]{user.getUsername(), user.getScore(), user.getStatus()});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách bạn bè.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
