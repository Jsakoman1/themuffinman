package com.themuffinman.app.common.validation;

import org.springframework.web.util.HtmlUtils;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

public final class RichTextInputValidator {

    private static final Set<String> ALLOWED_TAGS = Set.of(
            "b", "strong", "i", "em", "u", "p", "div", "ul", "ol", "li", "span", "blockquote", "br", "a", "img"
    );
    private static final Set<String> BLOCKED_TAGS = Set.of("script", "style", "iframe", "object", "embed");

    private RichTextInputValidator() {
    }

    public static boolean hasContent(String value) {
        return !extractPlainText(value).isBlank();
    }

    public static String extractPlainText(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return "";
        }

        if (!looksLikeHtml(normalized)) {
            return normalized;
        }

        StringBuilder text = new StringBuilder();
        try {
            new ParserDelegator().parse(new StringReader(normalized), new PlainTextCallback(text), true);
        } catch (IOException ex) {
            return HtmlUtils.htmlUnescape(stripTags(normalized)).replaceAll("\\s+", " ").trim();
        }

        return HtmlUtils.htmlUnescape(text.toString()).replaceAll("\\s+", " ").trim();
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        if (!looksLikeHtml(normalized)) {
            return normalized;
        }

        StringBuilder html = new StringBuilder();
        try {
            new ParserDelegator().parse(new StringReader(normalized), new SanitizingCallback(html), true);
        } catch (IOException ex) {
            return HtmlUtils.htmlEscape(stripTags(normalized));
        }

        return html.toString().trim();
    }

    private static boolean looksLikeHtml(String value) {
        return value.matches(".*<\\/?[a-zA-Z][\\s\\S]*>");
    }

    private static String stripTags(String value) {
        return value.replaceAll("(?s)<[^>]*>", " ");
    }

    private static String safeUrl(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        if (trimmed.matches("(?i)^(https?:|mailto:|/|#).*$")) {
            return trimmed;
        }

        return "";
    }

    private static String safeImageUrl(String value) {
        String trimmed = value.trim();
        if (trimmed.matches("(?i)^data:image/[a-zA-Z0-9.+-]+;base64,.*$")) {
            return trimmed;
        }

        return "";
    }

    private static final class PlainTextCallback extends HTMLEditorKit.ParserCallback {
        private final StringBuilder out;

        private PlainTextCallback(StringBuilder out) {
            this.out = out;
        }

        @Override
        public void handleText(char[] data, int pos) {
            out.append(data);
            out.append(' ');
        }
    }

    private static final class SanitizingCallback extends HTMLEditorKit.ParserCallback {
        private final StringBuilder out;
        private final Deque<String> openTags = new ArrayDeque<>();
        private int skipDepth = 0;

        private SanitizingCallback(StringBuilder out) {
            this.out = out;
        }

        @Override
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int pos) {
            String tagName = tag.toString().toLowerCase();

            if (BLOCKED_TAGS.contains(tagName)) {
                skipDepth++;
                return;
            }

            if (skipDepth > 0 || !ALLOWED_TAGS.contains(tagName)) {
                return;
            }

            if ("a".equals(tagName)) {
                String href = safeUrl(String.valueOf(attributes.getAttribute(HTML.Attribute.HREF) == null ? "" : attributes.getAttribute(HTML.Attribute.HREF)));
                if (href.isBlank()) {
                    return;
                }

                out.append("<a href=\"").append(HtmlUtils.htmlEscape(href)).append("\" target=\"_blank\" rel=\"noreferrer noopener\">");
                openTags.push(tagName);
                return;
            }

            if ("img".equals(tagName)) {
                String src = safeImageUrl(String.valueOf(attributes.getAttribute(HTML.Attribute.SRC) == null ? "" : attributes.getAttribute(HTML.Attribute.SRC)));
                if (src.isBlank()) {
                    return;
                }

                String alt = attributes.getAttribute(HTML.Attribute.ALT) == null ? "" : String.valueOf(attributes.getAttribute(HTML.Attribute.ALT));
                out.append("<img src=\"")
                        .append(HtmlUtils.htmlEscape(src))
                        .append("\" alt=\"")
                        .append(HtmlUtils.htmlEscape(alt))
                        .append("\" loading=\"lazy\">");
                return;
            }

            out.append('<').append(tagName).append('>');
            openTags.push(tagName);
        }

        @Override
        public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributes, int pos) {
            String tagName = tag.toString().toLowerCase();

            if (BLOCKED_TAGS.contains(tagName) || skipDepth > 0 || !ALLOWED_TAGS.contains(tagName)) {
                return;
            }

            if ("br".equals(tagName)) {
                out.append("<br>");
                return;
            }

            if ("img".equals(tagName)) {
                String src = safeImageUrl(String.valueOf(attributes.getAttribute(HTML.Attribute.SRC) == null ? "" : attributes.getAttribute(HTML.Attribute.SRC)));
                if (src.isBlank()) {
                    return;
                }

                String alt = attributes.getAttribute(HTML.Attribute.ALT) == null ? "" : String.valueOf(attributes.getAttribute(HTML.Attribute.ALT));
                out.append("<img src=\"")
                        .append(HtmlUtils.htmlEscape(src))
                        .append("\" alt=\"")
                        .append(HtmlUtils.htmlEscape(alt))
                        .append("\" loading=\"lazy\">");
            }
        }

        @Override
        public void handleText(char[] data, int pos) {
            if (skipDepth > 0) {
                return;
            }

            out.append(HtmlUtils.htmlEscape(new String(data)));
        }

        @Override
        public void handleEndTag(HTML.Tag tag, int pos) {
            String tagName = tag.toString().toLowerCase();

            if (BLOCKED_TAGS.contains(tagName)) {
                skipDepth = Math.max(0, skipDepth - 1);
                return;
            }

            if (skipDepth > 0 || !ALLOWED_TAGS.contains(tagName) || "br".equals(tagName) || "img".equals(tagName)) {
                return;
            }

            while (!openTags.isEmpty()) {
                String openTag = openTags.pop();
                out.append("</").append(openTag).append('>');
                if (openTag.equals(tagName)) {
                    break;
                }
            }
        }

        @Override
        public void handleError(String errorMsg, int pos) {
            // Ignore parser errors and keep the best-effort sanitized output.
        }
    }
}
