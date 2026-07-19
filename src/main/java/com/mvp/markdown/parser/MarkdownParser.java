package com.mvp.markdown.parser;
import com.mvp.markdown.storage.Document;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MarkdownParser {

    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.*)");
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)");

    public ParsedDocument parse(Document document) {
        String[] lines = document.getContent().split("\\R", -1);

        List<Heading> headings = new ArrayList<>();
        List<Link> links = new ArrayList<>();
        List<CodeBlock> codeBlocks = new ArrayList<>();
        StringBuilder plainTextBuilder = new StringBuilder();

        boolean inBlock = false;
        int startLine = -1;
        StringBuilder codeContent = new StringBuilder();
        String language = "";

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            // 1. Detect Code Block Fences
            if (line.trim().startsWith("```")) {
                if (!inBlock) {
                    inBlock = true;
                    startLine = lineNumber;
                    language = line.trim().substring(3).trim();
                } else {
                    codeBlocks.add(new CodeBlock(startLine, lineNumber, codeContent.toString(), language));
                    inBlock = false;
                    codeContent.setLength(0);
                }
                continue;
            }

            // 2. Handle Content
            if (inBlock) {
                codeContent.append(line).append("\n");
            } else {
                // Extract Headings
                Matcher hMatcher = HEADING_PATTERN.matcher(line);
                if (hMatcher.find()) {
                    headings.add(new Heading(hMatcher.group(1).length(), hMatcher.group(2).trim(), lineNumber));
                }

                // Extract Links
                Matcher lMatcher = LINK_PATTERN.matcher(line);
                while (lMatcher.find()) {
                    links.add(new ExternalLink(lMatcher.group(2), lMatcher.group(1), lineNumber));
                }

                // Append stripped text to plain text collector
                plainTextBuilder.append(stripMarkdown(line)).append("\n");
            }
        }

        return new ParsedDocument(document.getUuid(), document.getPath(), links, headings, codeBlocks, plainTextBuilder.toString());
    }

    private String stripMarkdown(String line) {
        String stripped = line;

        // Remove heading markers (e.g., ### Title -> Title)
        stripped = stripped.replaceAll("^(#{1,6})\\s+", "");

        // Replace [text](url) with just "text"
        stripped = stripped.replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1");

        // Remove remaining common markdown symbols (*, _, `, ~)
        stripped = stripped.replaceAll("[*_`~]", "");

        return stripped.trim();
    }
}