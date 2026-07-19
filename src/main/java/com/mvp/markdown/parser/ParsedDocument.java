package com.mvp.markdown.parser;

import com.mvp.markdown.storage.Document;

import java.util.List;

public class ParsedDocument {

    private final Document document;
    private final List<Link> links;
    private final List<Heading> headings;
    private final List<CodeBlock> codeBlocks;

    public ParsedDocument(Document document, List<Link> links, List<Heading> headings, List<CodeBlock> codeBlocks) {
        this.document = document;
        this.links = links;
        this.headings = headings;
        this.codeBlocks = codeBlocks;
    }

    public Document getDocument() {return document;}
    public List<Link> getLinks() {return links;}
    public List<Heading> getHeadings() {return headings;}
    public List<CodeBlock> getCodeBlocks() {return codeBlocks;}

}
