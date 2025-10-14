package com.example.addressbook;

public class NinjaUser {
    private int userID;
    private String userName;
    private String passwordHash;
    private String passwordPlain;
    private String secretQuestion1;
    private String secretQuestion2;
    private String secretQuestion1Answer;
    private String secretQuestion2Answer;
    private String secretAnswer1Plain;      // plain
    private String secretAnswer2Plain;

    public NinjaUser(String userName, String passwordHash, String secretQuestion1, String secretQuestion2, String secretQuestion1Answer, String secretQuestion2Answer) {
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.secretQuestion1 = secretQuestion1;
        this.secretQuestion2 = secretQuestion2;
        this.secretQuestion1Answer = secretQuestion1Answer;
        this.secretQuestion2Answer = secretQuestion2Answer;
    }

    public NinjaUser(String userName, String passwordHash, String passwordPlain,
                     String secretQuestion1, String secretQuestion2,
                     String secretQuestion1Answer, String secretQuestion2Answer,
                     String secretAnswer1Plain, String secretAnswer2Plain) {
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.passwordPlain = passwordPlain;
        this.secretQuestion1 = secretQuestion1;
        this.secretQuestion2 = secretQuestion2;
        this.secretQuestion1Answer = secretQuestion1Answer;
        this.secretQuestion2Answer = secretQuestion2Answer;
        this.secretAnswer1Plain = secretAnswer1Plain;
        this.secretAnswer2Plain = secretAnswer2Plain;
    }

    public int getId() {return userID;}
    public String getUserName() {return userName;}
    public String getPasswordHash() {return passwordHash;}
    public String getSecretQuestion1() {return secretQuestion1;}
    public String getSecretQuestion2() {return secretQuestion2;}
    public String getSecretQuestion1Answer() {return secretQuestion1Answer;}
    public String getSecretQuestion2Answer() {return secretQuestion2Answer;}
    public String getPasswordPlain() { return passwordPlain; }
    public String getSecretAnswer1Plain() { return secretAnswer1Plain; }
    public String getSecretAnswer2Plain() { return secretAnswer2Plain; }

    public void setId(int id) {this.userID = id;}
    public void setUserName(String userName) { this.userName = userName; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setPasswordPlain(String passwordPlain) { this.passwordPlain = passwordPlain; }
    public void setSecretQuestion1(String secretQuestion1) { this.secretQuestion1 = secretQuestion1; }
    public void setSecretQuestion2(String secretQuestion2) { this.secretQuestion2 = secretQuestion2; }
    public void setSecretQuestion1Answer(String answer) { this.secretQuestion1Answer = answer; }
    public void setSecretQuestion2Answer(String answer) { this.secretQuestion2Answer = answer; }
    public void setSecretAnswer1Plain(String answer) { this.secretAnswer1Plain = answer; }
    public void setSecretAnswer2Plain(String answer) { this.secretAnswer2Plain = answer; }

}
