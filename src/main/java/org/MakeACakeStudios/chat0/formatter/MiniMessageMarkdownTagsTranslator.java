package org.MakeACakeStudios.chat0.formatter;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class MiniMessageMarkdownTagsTranslator {

    public static @NotNull String markdown(@NotNull String s) {
        var temp = wrapLocationTag(s);
        for (MarkdownTag tag : MarkdownTag.entries) {
            temp = tag.stripTags(temp);
        }
        return unwrapLocationTag(temp);
    }

    enum MarkdownTag {
        BOLD("\\*\\*", "<b>", "</b>"),
        ITALIC("\\*", "<i>", "</i>"),

        ITALIC_US("_", "<i>", "</i>"),
        BOLD_ITALIC_DS("\\*\\*\\*", "<b><i>", "</b></i>"),

        STRIKETHROUGH("~", "<st>", "</st>"),
        UNDERLINED("__", "<u>", "</u>"),
        TEST("%", "<red>", "</red>")
        ;

        public final @NotNull String tag, openTag, closingTag;
        public static final @NotNull List<@NotNull MarkdownTag> entries = Arrays.stream(MarkdownTag.values()).toList();

        MarkdownTag(
                @NotNull String tag, @NotNull String openTag, @NotNull String closingTag
        ) {
            this.tag = tag; this.openTag = openTag; this.closingTag = closingTag;
        }

        public @NotNull String stripTags(@NotNull String s) {
            var regex = tag + "(.*?)" + tag;
            return s.replaceAll(regex, openTag + "$1" + closingTag);
        }
    }

    public static @NotNull String wrapLocationTag(@NotNull String s) {
        return s.replaceAll(":loc:", "###LOC###");
    }

    public static @NotNull String unwrapLocationTag(@NotNull String s) {
        return s.replaceAll("###LOC###", ":loc:");
    }
}
