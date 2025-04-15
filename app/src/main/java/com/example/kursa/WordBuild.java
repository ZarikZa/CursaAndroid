package com.example.kursa;

/**
 * Класс, представляющий слово с возможностью выделения
 */
public class WordBuild {
    private String text;
    private boolean isSelected;

    /**
     * Конструктор слова
     * @param text текст слова
     */
    public WordBuild(String text) {
        this.text = text;
        this.isSelected = false;
    }

    /**
     * @return текст слова
     */
    public String getText() {
        return text;
    }

    /**
     * Проверяет, выделено ли слово
     * @return true если слово выделено
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Устанавливает состояние выделения слова
     * @param selected флаг выделения
     */
    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
}