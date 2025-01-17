package org.MakeACakeStudios.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;

import java.util.*;

public class ChatUtils implements Listener {

    public static String getRandomMessage(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return messages.get(random.nextInt(messages.size()));
    }

    public static Component getFormattedPlayerName(Player player) {
        String prefix = MakeABuilders.instance.getPlayerPrefix(player);
        String suffix = MakeABuilders.instance.getPlayerSuffix(player);
        return MiniMessage.miniMessage().deserialize(prefix + player.getName() + suffix);
    }



    public static void handleExternalCommandMessage(Player sender, String message) {
        String formattedMessage = TagFormatter.format(message, sender);

        String prefix = MakeABuilders.instance.getPlayerPrefix(sender);
        String suffix = MakeABuilders.instance.getPlayerSuffix(sender);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String clickableName = "<click:suggest_command:'/msg " + sender.getName() + " '>" +
                        "<hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы отправить сообщение игроку " +
                        prefix + sender.getName() + suffix + ".'>" +
                        prefix + sender.getName() + suffix + "</hover></click>";

            String finalMessage = clickableName + " > " + formattedMessage;

            onlinePlayer.sendMessage(MiniMessage.miniMessage().deserialize(finalMessage));
        }
    }


}
