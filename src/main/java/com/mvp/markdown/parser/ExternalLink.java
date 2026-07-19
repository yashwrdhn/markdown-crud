package com.mvp.markdown.parser;

public class ExternalLink extends Link {

    public ExternalLink(String target, String text, int lineNumber) {
        super(target, lineNumber, text);
    }

}
