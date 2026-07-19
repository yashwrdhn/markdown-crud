package com.mvp.markdown.parser;

public class CodeBlock {
    private final int startLine;
    private final int endLine;
    private final String content;
    private final String language;

    public CodeBlock(int startLine, int endLine, String content, String language) {
        this.startLine = startLine;
        this.endLine = endLine;
        this.content = content;
        this.language = language;
    }

    //Getters
    public int getStartLine() { return startLine;}
    public int getEndLine() { return endLine;}
    public String getContent() {return content;}
    public String getLanguage() {return language;}
}
