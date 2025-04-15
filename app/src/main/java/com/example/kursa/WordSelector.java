package com.example.kursa;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Класс для выбора случайных уникальных слов из списка
 */
public class WordSelector {

    /**
     * Выбирает указанное количество случайных уникальных слов из списка
     * @param words исходный список слов
     * @param count количество слов для выбора
     * @return список случайных уникальных слов
     */
    public List<Word> getRandomWords(List<Word> words, int count) {
        Collections.shuffle(words);

        Set<Word> uniqueWords = new HashSet<>();

        for (Word word : words) {
            if (uniqueWords.size() >= count) {
                break;
            }
            uniqueWords.add(word);
        }

        return List.copyOf(uniqueWords);
    }
}