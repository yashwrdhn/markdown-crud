package com.mvp.markdown.parser;

public abstract class Link {
    protected final String target;
    protected final int lineNumber;
    protected final String text;
    protected Link(String target, int lineNumber, String text) {
        this.target = target;
        this.lineNumber = lineNumber;
        this.text = text;
    }

    public String getTarget() { return target; }
    public int getLineNumber() { return lineNumber; }
    public String getText() { return text; }
}