
package com.example.kursa;

import java.io.Serializable;
import java.util.List;
/**
 * Level — класс для представления уровня в приложении.
 * Содержит название уровня, список слов и флаг разблокировки.
 * Реализует Serializable для передачи между активностями.
 */
public class Level implements Serializable {
    private String levelName;
    private List<Word> words;
    private boolean isUnlocked;

    /**
     * Пустой конструктор для создания экземпляра уровня.
     */
    public Level() {}

    /**
     * Конструктор для инициализации уровня с заданными параметрами.
     *
     * @param levelName  Название уровня
     * @param words      Список слов для уровня
     * @param isUnlocked Флаг, указывающий, разблокирован ли уровень
     */
    public Level(String levelName, List<Word> words, boolean isUnlocked) {
        this.levelName = levelName;
        this.words = words;
        this.isUnlocked = isUnlocked;
    }

    /**
     * Возвращает название уровня.
     *
     * @return Название уровня
     */
    public String getLevelName() {
        return levelName;
    }

    /**
     * Возвращает список слов уровня.
     *
     * @return Список слов
     */
    public List<Word> getWords() {
        return words;
    }

    /**
     * Устанавливает список слов для уровня.
     *
     * @param words Новый список слов
     */
    public void setWords(List<Word> words) {
        this.words = words;
    }

    /**
     * Проверяет, разблокирован ли уровень.
     *
     * @return true, если уровень разблокирован, иначе false
     */
    public boolean isUnlocked() {
        return isUnlocked;
    }
}