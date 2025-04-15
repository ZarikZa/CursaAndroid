package com.example.kursa;

import java.io.Serializable;
/**
 * Word — модель данных для слова и его перевода.
 * Используется для хранения английского слова и его перевода на русский язык.
 * Реализует Serializable для передачи между компонентами приложения.
 */
public class Word implements Serializable {
    private String english;
    private String translation;

    /**
     * Пустой конструктор для создания объекта Word.
     */
    public Word() {}

    /**
     * Конструктор для создания объекта Word с указанными параметрами.
     *
     * @param english     Английское слово
     * @param translation Перевод слова
     */
    public Word(String english, String translation) {
        this.english = english;
        this.translation = translation;
    }

    /**
     * Возвращает английское слово.
     *
     * @return Английское слово
     */
    public String getEnglish() {
        return english;
    }

    /**
     * Возвращает перевод слова.
     *
     * @return Перевод слова
     */
    public String getTranslation() {
        return translation;
    }
}