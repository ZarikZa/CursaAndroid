package com.example.kursa;

import java.io.Serializable;
import java.util.List;

public class Level implements Serializable {
    private String levelName;
    private List<Word> words;
    private boolean isUnlocked;

    public Level() {}

    public Level(String levelName, List<Word> words, boolean isUnlocked) {
        this.levelName = levelName;
        this.words = words;
        this.isUnlocked = isUnlocked;
    }

    public String getLevelName() {
        return levelName;
    }

    public List<Word> getWords() {
        return words;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }
}