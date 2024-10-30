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
                      rs.getInt(4));
          }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void updateUserScore(String username) {
        try {
            // Prepare the SQL statement to update the score
            PreparedStatement preparedStatement = con.prepareStatement(
                "UPDATE user SET score = score + 1 WHERE username = ?"
            );
            preparedStatement.setString(1, username);

            // Execute the update
            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Score updated successfully for user: " + username);
            } else {
                System.out.println("No user found with the username: " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addUser(User user) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO user(username, password, score)\n"
                    + "VALUES(?,?,?)");
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setInt(3, user.getScore());
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
            user.setScore(resultSet.getInt("score"));
            users.add(user);
        }

        return users;
    }

}