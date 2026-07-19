package com.mvp.markdown.parser;

public class Heading {

    private final int level;
    private final String text;
    private final int lineNumber;

    public Heading(int level, String text, int lineNumber) {
        this.level = level;
        this.text = text;
        this.lineNumber = lineNumber;
    }

    public int getLevel() { return level;}
    public String getText() {return text;}
    public int getLineNumber() {return lineNumber;}
}
