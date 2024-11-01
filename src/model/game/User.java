package model.game;
import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
	private int id;
    private String username;
    private String password;
    private int score;
    private String status;

    // Constructors, getters, and setters
    public User() {}

    public User(String username, String password, int score, String status) {
        this.username = username;
        this.password = password;
        this.score = score;
        this.status = status;
    }
    
    public User(String username, String password, int score) {
        this.username = username;
        this.password = password;
        this.score = score;
    }
    public String toString() {
    	return "username: " + this.username + " password: " + this.password + " score: " + this.score + " status: " + this.status;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
