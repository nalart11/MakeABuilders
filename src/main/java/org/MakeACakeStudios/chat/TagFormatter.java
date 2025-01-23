package org.MakeACakeStudios.chat;

import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TagFormatter {

    public static String format(String message) {
        message = replaceTextFormatting(message);
        message = replaceEmojis(message);
        message = replaceLinks(message);
        message = replaceBackSlashes(message);

        return message;
    }

    private static String replaceEmojis(String message) {
        return message
                .replace(":cry:", "<yellow>☹</yellow><aqua>,</aqua>")
                .replace(":skull:", "☠")
                .replace(":skulley:", "<red>☠'ey</red>")
                .replace("<3", "<red>❤</red>")
                .replace(":heart:", "<red>❤</red>")
                .replace(":fire:", "<color:#FF7800>\uD83D\uDD25</color>")
                .replace(":star:", "<yellow>★</yellow>")
                .replace(":stop:", "<red>⚠</red>")
                .replace(":sun:", "<yellow>☀</yellow>")
                .replace(":mail:", "✉")
                .replace(":happy:", "<yellow>☺</yellow>")
                .replace(":sad:", "<yellow>☹</yellow>")
                .replace(":umbrella:", "☂")
                .replace(":tada:", "<color:#FF00FF>\uD83C\uDF89</color>")
                .replace(":nyaboom:", "<rainbow>NYABOOM</rainbow> <aqua>:333</aqua>")
                .replace(":no:", "<red>✖</red>")
                .replace(":yes:", "<green>✔</green>")
                .replace(":shrug:", "¯\\_(ツ)_/¯")
                .replace(":tableflip:", "<red>(╯°□°)╯︵ ┻━┻</red>")
                .replace(":unflip:", "┬─┬ノ( º _ ºノ)")
                .replace(":questionmark:", "<b>???<b>")
                .replace(":sadge:", "<yellow>ⓈⒶⒹⒼⒺ</yellow><aqua>...</aqua>");
    }

    private static String replaceLinks(String message) {
        if (message.contains("http://") || message.contains("https://")) {
            String[] words = message.split(" ");
            for (String word : words) {
                if (word.startsWith("http://") || word.startsWith("https://")) {
                    String formattedLink = "<click:open_url:'" + word + "'><green>" + word + "</green></click>";
                    message = message.replace(word, formattedLink);
                }
            }
        }
        return message;
    }

    private static String replaceTextFormatting(String message) {
        String locTagRegex = ":loc:";
        String placeholder = "###LOC###";

        if (message.contains(locTagRegex)) {
            message = message.replace(locTagRegex, placeholder);
        }

        message = message.replaceAll("\\*\\*\\*(.*?)\\*\\*\\*", "<b><i>$1</i></b>");
        message = message.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        message = message.replaceAll("\\*(.*?)\\*", "<i>$1</i>");
        message = message.replaceAll("~(.*?)~", "<st>$1</st>");
        message = message.replaceAll("_(.*?)_", "<u>$1</u>");

        if (message.contains(placeholder)) {
            message = message.replace(placeholder, locTagRegex);
        }

        return message;
    }


    private static String replaceBackSlashes(String message) {
        return message.replace("\\", "\\\\");
    }
}
