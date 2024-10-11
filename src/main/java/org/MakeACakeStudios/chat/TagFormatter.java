package org.MakeACakeStudios.chat;

import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TagFormatter {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public String format(String message, Player player) {
        message = replaceLocationTag(message, player);
        message = replaceEmojis(message);
        message = replaceLinks(message);
        message = replaceTextFormatting(message);

        return message;
    }

    private String replaceLocationTag(String message, Player player) {
        if (message.contains(":loc:")) {
            int x = player.getLocation().getBlockX();
            int y = player.getLocation().getBlockY();
            int z = player.getLocation().getBlockZ();
            String worldName = player.getWorld().getName();

            String color;
            switch (worldName) {
                case "world":
                    color = "<gradient:#00FF1A:#7EFF91>";
                    break;
                case "world_nether":
                    color = "<gradient:#FF0000:#FF7E7E>";
                    break;
                case "world_the_end":
                    color = "<gradient:#ED00FF:#DE7EFF>";
                    break;
                default:
                    color = "<gradient:#FFFFFF:#FFFFFF>";
            }

            String location = color + "<click:run_command:'/goto " + worldName + " " + x + " " + y + " " + z + "'>["
                    + x + "x/" + y + "y/" + z + "z, " + worldName + "]</click></gradient>";

            return message.replace(":loc:", location);
        }
        return message;
    }

    private String replaceEmojis(String message) {
        return message
                .replace(":cry:", "<yellow>☹</yellow><aqua>,</aqua>")
                .replace(":skull:", "☠")
                .replace(":skulley:", "<red>☠</red>")
                .replace("<3", "<red>❤</red>")
                .replace(":heart:", "<red>❤</red>")
                .replace(":fire:", "<color:#FF7800>\uD83D\uDD25</color>")
                .replace(":star:", "<yellow>★</yellow>")
                .replace(":stop:", "<red>⚠</red>")
                .replace(":sun:", "<yellow>☀</yellow>")
                .replace(":mail:", "✉")
                .replace(":happy:", "☺")
                .replace(":sad:", "☹")
                .replace(":umbrella:", "☂")
                .replace(":tada:", "<color:#FF00FF>\uD83C\uDF89</color>")
                .replace(":nyaboom:", "<rainbow>NYABOOM :333</rainbow>")
                .replace(":decline:", "<red>✖</red>")
                .replace(":accept:", "<green>✔</green>");
    }

    // Новая функция для обработки ссылок
    private String replaceLinks(String message) {
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

    private String replaceTextFormatting(String message) {
        message = message.replaceAll("\\*\\*\\*(.*?)\\*\\*\\*", "<b><i>$1</i></b>"); // ***фраза***
        message = message.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>"); // **фраза**
        message = message.replaceAll("\\*(.*?)\\*", "<i>$1</i>"); // *фраза*
        message = message.replaceAll("~(.*?)~", "<st>$1</st>"); // ~фраза~
        message = message.replaceAll("_(.*?)_", "<u>$1</u>"); // _фраза_
//        message = message.replaceAll("\\|\\|(.*?)\\|\\|", "<obf>$1<obf>"); // ||фраза||

        return message;
    }
}
