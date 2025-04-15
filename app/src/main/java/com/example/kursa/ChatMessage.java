
package com.example.kursa;
/**
 * ChatMessage — модель данных для представления сообщения в чате.
 * Содержит информацию об отправителе и тексте сообщения, предоставляет геттеры для доступа к этим данным.
 */
public class ChatMessage {
    private String sender;
    private String message;

    /**
     * Конструктор для создания объекта сообщения.
     *
     * @param sender  Имя отправителя сообщения
     * @param message Текст сообщения
     */
    public ChatMessage(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    /**
     * Возвращает имя отправителя сообщения.
     *
     * @return Имя отправителя
     */
    public String getSender() {
        return sender;
    }

    /**
     * Возвращает текст сообщения.
     *
     * @return Текст сообщения
     */
    public String getMessage() {
        return message;
    }
}