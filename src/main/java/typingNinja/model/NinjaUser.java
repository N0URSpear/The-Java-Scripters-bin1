package typingNinja.model;

public class NinjaUser {
    private int userID;
    private final String userName;
    private String passwordHash;
    private final String secretQuestion1;
    private final String secretQuestion2;
    private final String secretQuestion1Answer;
    private final String secretQuestion2Answer;

    public NinjaUser(String userName, String passwordHash, String secretQuestion1, String secretQuestion2, String secretQuestion1Answer, String secretQuestion2Answer) {
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.secretQuestion1 = secretQuestion1;
        this.secretQuestion2 = secretQuestion2;
        this.secretQuestion1Answer = secretQuestion1Answer;
        this.secretQuestion2Answer = secretQuestion2Answer;
    }

    public int getId() {
        return userID;
    }

    public void setId(int id) {
        this.userID = id;
    }

    public String getUserName() {return userName;}

    public String getPasswordHash() {return passwordHash;}

    public void setPasswordHash(String passwordHash) {this.passwordHash = passwordHash;}

    public String getSecretQuestion1() {return secretQuestion1;}

    public String getSecretQuestion2() {return secretQuestion2;}

    public String getSecretQuestion1Answer() {return secretQuestion1Answer;}

    public String getSecretQuestion2Answer() {return secretQuestion2Answer;}

}
