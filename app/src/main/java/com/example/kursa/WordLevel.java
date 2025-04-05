package com.example.kursa;

public class WordLevel {
    private String english;
    private String translation;
    private boolean isHard;

    public WordLevel(String english, String translation, boolean isHard) {
        this.english = english;
        this.translation = translation;
        this.isHard = isHard;
    }

    public String getEnglish() {
        return english;
    }

    public String getTranslation() {
        return translation;
    }

    public boolean isHard() {
        return isHard;
    }

    public void setHard(boolean hard) {
        isHard = hard;
    }
}