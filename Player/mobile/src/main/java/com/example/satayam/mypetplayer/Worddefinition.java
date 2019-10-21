package com.example.satayam.mypetplayer;

public class Worddefinition {
    private String word;
    private String definition;
    private boolean important;
    private boolean didload;
    private boolean isSorryCard;

    public Worddefinition() {
    }

    public Worddefinition(String name, String definition, boolean important, boolean didload) {
        this.word = name;
        this.definition = definition;
        this.important = important;
        this.didload = didload;
        this.isSorryCard = false;
    }

    public String getWord() { return word; }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public boolean getImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public void setDidload(boolean didload){this.didload = didload;}

    public boolean getDidload() { return this.didload; }

    public void setIsSorryCard(boolean isSorryCard){ this.isSorryCard = isSorryCard; }

    public boolean getIsSorryCard() { return this.isSorryCard; }
}
