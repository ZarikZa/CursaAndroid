package com.example.kursa;

public class WordBuild {
    private String text;
    private boolean isSelected;

    public WordBuild(String text) {
        this.text = text;
        this.isSelected = false;
    }

    public String getText() {
        return text;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
}