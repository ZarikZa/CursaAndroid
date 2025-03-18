package com.example.kursa;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordSelector {
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
