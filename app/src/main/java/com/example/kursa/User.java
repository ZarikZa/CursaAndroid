package com.example.kursa;
/**
 * User — модель данных пользователя.
 * Содержит информацию о никнейме, логине, пароле и рейтинговых баллах.
 */
public class User {
    private String nickname;
    private String login;
    private String password;
    private int reytingPoints;

    /**
     * Пустой конструктор для создания объекта User.
     */
    public User() {}

    /**
     * Конструктор для создания объекта User с указанными параметрами.
     *
     * @param nickname Никнейм пользователя
     * @param login    Логин пользователя
     * @param password Пароль пользователя
     */
    public User(String nickname, String login, String password) {
        this.nickname = nickname;
        this.login = login;
        this.password = password;
    }

    /**
     * Возвращает никнейм пользователя.
     *
     * @return Никнейм
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Устанавливает никнейм пользователя.
     *
     * @param nickname Новый никнейм
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Возвращает логин пользователя.
     *
     * @return Логин
     */
    public String getlogin() {
        return login;
    }

    /**
     * Устанавливает логин пользователя.
     *
     * @param login Новый логин
     */
    public void setlogin(String login) {
        this.login = login;
    }

    /**
     * Возвращает пароль пользователя.
     *
     * @return Пароль
     */
    public String getPassword() {
        return password;
    }

    /**
     * Устанавливает пароль пользователя.
     *
     * @param password Новый пароль
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Возвращает рейтинговые баллы пользователя.
     *
     * @return Количество рейтинговых баллов
     */
    public int getReytingPoints() {
        return reytingPoints;
    }

    /**
     * Устанавливает рейтинговые баллы пользователя.
     *
     * @param reytingPoints Новое количество рейтинговых баллов
     */
    public void setReytingPoints(int reytingPoints) {
        this.reytingPoints = reytingPoints;
    }
}