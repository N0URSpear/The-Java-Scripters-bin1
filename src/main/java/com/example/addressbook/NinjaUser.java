package com.example.addressbook;

public class NinjaUser {
    private int id;
    private String userName;
    private String passwordHash;
    private String secretQuestion1;
    private String secretQuestion2;
    private String secretQuestion1Answer;
    private String secretQuestion2Answer;

    public NinjaUser(String userName, String passwordHash,
                     String secretQuestion1, String secretQuestion2,
                     String secretQuestion1Answer, String secretQuestion2Answer) {
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.secretQuestion1 = secretQuestion1;
        this.secretQuestion2 = secretQuestion2;
        this.secretQuestion1Answer = secretQuestion1Answer;
        this.secretQuestion2Answer = secretQuestion2Answer;
    }

    // === Getters ===
    public int getId() { return id; }
    public String getUserName() { return userName; }
    public String getPasswordHash() { return passwordHash; }
    public String getSecretQuestion1() { return secretQuestion1; }
    public String getSecretQuestion2() { return secretQuestion2; }
    public String getSecretQuestion1Answer() { return secretQuestion1Answer; }
    public String getSecretQuestion2Answer() { return secretQuestion2Answer; }

    // === Setters ===
    public void setId(int id) { this.id = id; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setSecretQuestion1(String secretQuestion1) { this.secretQuestion1 = secretQuestion1; }
    public void setSecretQuestion2(String secretQuestion2) { this.secretQuestion2 = secretQuestion2; }
    public void setSecretQuestion1Answer(String answer) { this.secretQuestion1Answer = answer; }
    public void setSecretQuestion2Answer(String answer) { this.secretQuestion2Answer = answer; }
}
