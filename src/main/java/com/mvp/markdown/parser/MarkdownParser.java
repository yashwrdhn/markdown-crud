package com.mvp.markdown.parser;

import com.mvp.markdown.storage.Document;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MarkdownParser {

    public ParsedDocument parse(Document document) {
        String[] linesArray = document.getContent().split("\\R", -1);
        List<String> lines = List.of(linesArray);

        // Track indices of lines that are inside code blocks
        boolean[] insideCodeBlock = identifyCodeBlockLines(lines);

        List<Heading> headings = new ArrayList<>();
        List<ExternalLink> externalLinks = new ArrayList<>();
        List<CodeBlock> codeBlocks = new ArrayList<>();

        Pattern headingPattern = Pattern.compile("^(#{1,6})\\s+(.*)");
        Pattern linkPattern = Pattern.compile("\\[([^\\]]+)\\]\\((https?://[^\\)]+)\\)");

        // Logic to extract code blocks and parse lines only if NOT inside a code block
        boolean inBlock = false;
        int startLine = -1;
        StringBuilder content = new StringBuilder();
        String language = "";

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNumber = i + 1;

            // 1. Detect Code Block Boundaries
            if (line.trim().startsWith("```")) {
                if (!inBlock) {
                    inBlock = true;
                    startLine = lineNumber;
                    language = line.trim().substring(3).trim();
                } else {
                    codeBlocks.add(new CodeBlock(startLine, lineNumber, content.toString(), language));
                    inBlock = false;
                    content.setLength(0);
                }
                continue; // Skip the ``` line itself
            }

            // 2. If inside a code block, just accumulate and skip parsing
            if (inBlock) {
                if (content.length() > 0) content.append("\n");
                content.append(line);
                continue;
            }

            // 3. Parse Headings (only if not in code block)
            Matcher hMatcher = headingPattern.matcher(line);
            if (hMatcher.find()) {
                headings.add(new Heading(hMatcher.group(1).length(), hMatcher.group(2).trim(), lineNumber));
            }

            // 4. Parse Links (only if not in code block)
            Matcher lMatcher = linkPattern.matcher(line);
            while (lMatcher.find()) {
                externalLinks.add(new ExternalLink(lMatcher.group(2), lMatcher.group(1), lineNumber));
            }
        }

        return new ParsedDocument(document, new ArrayList<>(externalLinks), headings, codeBlocks);
    }

    private boolean[] identifyCodeBlockLines(List<String> lines) {
        boolean[] mask = new boolean[lines.size()];
        boolean inBlock = false;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().startsWith("```")) {
                inBlock = !inBlock;
                mask[i] = true;
            } else if (inBlock) {
                mask[i] = true;
            }
        }
        return mask;
    }
}