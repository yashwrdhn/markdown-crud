package com.mvp.markdown.parser;

import com.mvp.markdown.storage.Document;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class ParsedDocument {

    private final UUID uuid;
    private final Path path;
    private final List<Link> links;
    private final List<Heading> headings;
    private final List<CodeBlock> codeBlocks;
    private final String plainText;

    public ParsedDocument(UUID uuid, Path path, List<Link> links, List<Heading> headings, List<CodeBlock> codeBlocks, String plainText) {
        this.uuid = uuid;
        this.path = path;
        this.links = links;
        this.headings = headings;
        this.codeBlocks = codeBlocks;
        this.plainText = plainText;
    }

    public UUID getUuid() { return uuid; }
    public Path getPath() { return path; }
    public List<Link> getLinks() {return links;}
    public List<Heading> getHeadings() {return headings;}
    public List<CodeBlock> getCodeBlocks() {return codeBlocks;}
    public String getPlainText() { return plainText; }

}
