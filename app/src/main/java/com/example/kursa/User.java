package com.example.kursa;

public class User {
    private String nickname;
    private String login;
    private String password;
    private int reytingPoints;
    public User() { }
    public User(String nickname, String login, String password) {
        this.nickname = nickname;
        this.login = login;
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getlogin() {
        return login;
    }

    public void setlogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getReytingPoints() {
        return reytingPoints;
    }

    public void setReytingPoints(int reytingPoints) {
        this.reytingPoints = reytingPoints;
    }
}