package com.mvp.markdown.parser;

public class WikiLink extends Link {

    private final String alias;

    public WikiLink(String target, String alias, int lineNumber, String text) {
        super(target, lineNumber, text);
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }
}