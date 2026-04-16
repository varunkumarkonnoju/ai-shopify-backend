package com.ai.shopify.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HtmlFormattingService {

    public String toHtml(String text) {
        if (text == null || text.isBlank()) {
            return "<p></p>";
        }

        String normalized = text.replace("\r\n", "\n").trim();
        String[] lines = normalized.split("\n");

        StringBuilder html = new StringBuilder();
        List<String> bulletBuffer = new ArrayList<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.isEmpty()) {
                flushBullets(html, bulletBuffer);
                continue;
            }

            if (isBullet(line)) {
                bulletBuffer.add(stripBullet(line));
                continue;
            }

            flushBullets(html, bulletBuffer);

            if (isMarkdownHeading(line)) {
                html.append("<h2>")
                        .append(escapeHtml(stripMarkdownBold(stripHeadingHashes(line))))
                        .append("</h2>");
            } else if (isBoldOnlyLine(line)) {
                html.append("<h3>")
                        .append(escapeHtml(stripMarkdownBold(line)))
                        .append("</h3>");
            } else {
                html.append("<p>")
                        .append(applyInlineFormatting(escapeHtml(line)))
                        .append("</p>");
            }
        }

        flushBullets(html, bulletBuffer);

        return html.toString();
    }

    private boolean isBullet(String line) {
        return line.startsWith("- ")
                || line.startsWith("* ")
                || line.startsWith("• ");
    }

    private String stripBullet(String line) {
        return line.substring(2).trim();
    }

    private boolean isMarkdownHeading(String line) {
        return line.startsWith("#");
    }

    private String stripHeadingHashes(String line) {
        return line.replaceFirst("^#+\\s*", "").trim();
    }

    private boolean isBoldOnlyLine(String line) {
        return line.startsWith("**")
                && line.endsWith("**")
                && line.length() > 4;
    }

    private String stripMarkdownBold(String line) {
        String result = line;
        if (result.startsWith("**")) {
            result = result.substring(2);
        }
        if (result.endsWith("**")) {
            result = result.substring(0, result.length() - 2);
        }
        return result.trim();
    }

    private void flushBullets(StringBuilder html, List<String> bulletBuffer) {
        if (bulletBuffer.isEmpty()) {
            return;
        }

        html.append("<ul>");
        for (String bullet : bulletBuffer) {
            html.append("<li>")
                    .append(applyInlineFormatting(escapeHtml(bullet)))
                    .append("</li>");
        }
        html.append("</ul>");
        bulletBuffer.clear();
    }

    private String applyInlineFormatting(String text) {
        return text.replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>");
    }

    private String escapeHtml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}