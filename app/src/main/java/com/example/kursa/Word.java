package com.example.kursa;

import java.io.Serializable;

public class Word implements Serializable {
    private String english;
    private String translation;

    public Word() {}

    public Word(String english, String translation) {
        this.english = english;
        this.translation = translation;
    }

    public String getEnglish() {
        return english;
    }

    public String getTranslation() {
        return translation;
    }
}