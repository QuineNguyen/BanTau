/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import model.game.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class UserDAO extends DAO {

    public UserDAO() {
        super();
    }

//    public User verifyUser(User user) {
//        try {
//            PreparedStatement preparedStatement = con.prepareStatement("SELECT *\n"
//                    + "FROM user\n"
//                    + "WHERE username = ?\n"
//                    + "AND password = ?"
//            );
//            preparedStatement.setString(1, user.getUsername());
//            preparedStatement.setString(2, user.getPassword());
//            ResultSet rs = preparedStatement.executeQuery();
//            if (rs.next()) {
//                return new User(rs.getString(1),
//                        rs.getString(2),
//                        rs.getString(3));
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
    public User verifyUser(String username,String password) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("SELECT *\n"
                    + "FROM user\n"
                    + "WHERE username = ?\n"
                    + "AND password = ?"
                    + "AND status = 'offline'"
            );
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
              return new User(rs.getString(2),
                      rs.getString(3),
                      rs.getString(4));
          }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // public boolean modifyStatus(String username, String status) {
    //     try {
    //         PreparedStatement preparedStatement = con.prepareStatement(
    //             "UPDATE user SET status = ? WHERE username = ?"
    //         );
    //         preparedStatement.setString(1, status);
    //         preparedStatement.setString(2, username);
    //         int rowsUpdated = preparedStatement.executeUpdate();
    //         return rowsUpdated > 0;  // Trả về true nếu cập nhật thành công
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    //     return false;  // Trả về false nếu cập nhật không thành công
    // }
    
    public void addUser(User user) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO user(username, password, score)\n"
                    + "VALUES(?,?,?)");
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getScore());
            System.out.println(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean checkDuplicated(String username) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM user WHERE username = ?");
            preparedStatement.setString(1, username);
            System.out.println(preparedStatement);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY score DESC";
        PreparedStatement statement = con.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            User user = new User();
            user.setId(resultSet.getInt("id"));
            user.setUsername(resultSet.getString("username"));
            user.setPassword(resultSet.getString("password"));
            user.setScore(resultSet.getString("score"));
            users.add(user);
        }

        return users;
    }

}