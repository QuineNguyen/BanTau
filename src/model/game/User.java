package model.game;

public class User {
	private int id;
    private String username;
    private String password;
    private String score;

    // Constructors, getters, and setters
    public User() {}

    public User(String username, String password, String score) {
        this.username = username;
        this.password = password;
        this.score = score;
    }
    public String toString() {
    	return "username: " + this.username + " password: " + this.password;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getScore() { return score; }
    public void setScore(String score) { this.score = score; }
}
