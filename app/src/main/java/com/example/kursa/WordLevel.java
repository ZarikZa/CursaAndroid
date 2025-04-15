package com.example.kursa;

/**
 * Класс, представляющий слово с переводом и уровнем сложности
 */
public class WordLevel {
    private String english;
    private String translation;
    private boolean isHard;

    /**
     * Конструктор создания слова
     * @param english слово на английском
     * @param translation перевод слова
     * @param isHard флаг сложности (true - сложное)
     */
    public WordLevel(String english, String translation, boolean isHard) {
        this.english = english;
        this.translation = translation;
        this.isHard = isHard;
    }

    /**
     * @return английское слово
     */
    public String getEnglish() {
        return english;
    }

    /**
     * @return перевод слова
     */
    public String getTranslation() {
        return translation;
    }

    /**
     * Проверяет, является ли слово сложным
     * @return true если слово отмечено как сложное
     */
    public boolean isHard() {
        return isHard;
    }

    /**
     * Устанавливает уровень сложности слова
     * @param hard true - пометить как сложное
     */
    public void setHard(boolean hard) {
        isHard = hard;
    }
}