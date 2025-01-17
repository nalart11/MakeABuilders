package org.MakeACakeStudios.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.storage.PunishmentStorage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class ChatListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#FF3D4D:#FCBDBD>С возвращением!</gradient>"));

        String prefix = MakeABuilders.instance.getPlayerPrefix(player);
        String suffix = MakeABuilders.instance.getPlayerSuffix(player);
        List<String> joinMessages = MakeABuilders.instance.config.getStringList("Messages.Join");
        String rawMessage = ChatUtils.getRandomMessage(joinMessages);

        if (rawMessage != null) {
            String parsedMessage = rawMessage.replace("<player>", prefix + player.getName() + suffix);
            Component joinMessage = MiniMessage.miniMessage().deserialize(parsedMessage);

            event.joinMessage(joinMessage);

            List<String[]> playerMessages = MakeABuilders.instance.getMailStorage().getMessages(player.getName());
            if (!playerMessages.isEmpty()) {
                Sound selectedSound = MakeABuilders.instance.getPlayerSound(player);
                player.playSound(player.getLocation(), selectedSound, 1.0F, 1.0F);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>У вас есть <yellow>" + playerMessages.size() + "</yellow> непрочитанных сообщений.</green>"));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String prefix = MakeABuilders.instance.getPlayerPrefix(player);
        String suffix = MakeABuilders.instance.getPlayerSuffix(player);
        List<String> quitMessages = MakeABuilders.instance.config.getStringList("Messages.Quit");
        String rawMessage = ChatUtils.getRandomMessage(quitMessages);

        if (rawMessage != null) {
            String parsedMessage = rawMessage.replace("<player>", prefix + player.getName() + suffix);
            Component quitMessage = MiniMessage.miniMessage().deserialize(parsedMessage);

            event.quitMessage(quitMessage);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Component originalDeathMessage = event.deathMessage();

        if (originalDeathMessage != null) {
            Component customDeathMessage = originalDeathMessage.replaceText(builder ->
                    builder.matchLiteral(player.getName()).replacement(ChatUtils.getFormattedPlayerName(player))
            );

            event.deathMessage(customDeathMessage);
        }
    }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        Component originalMessage = event.message();

        if (originalMessage != null) {
            Component customAdvancementMessage = originalMessage.replaceText(builder ->
                    builder.matchLiteral(player.getName()).replacement(ChatUtils.getFormattedPlayerName(player))
            );

            event.message(customAdvancementMessage);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getDisplayName();
        String message = event.getMessage();

        String muteStatus = PunishmentStorage.instance.checkMute(player.getName());

        if (!muteStatus.contains("не замьючен")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Вы замьючены и не можете отправлять сообщения.</red>"));
            event.setCancelled(true);
            return;
        }

        System.out.println(playerName + " → " + message);

        if (message.contains("@")) {
            String[] words = message.split(" ");
            for (String word : words) {
                if (word.startsWith("@")) {
                    String mentionedPlayerName = word.substring(1);
                    Player mentionedPlayer = Bukkit.getPlayerExact(mentionedPlayerName);

                    if (mentionedPlayer != null && mentionedPlayer.isOnline()) {
                        Sound notificationSound = MakeABuilders.instance.getPlayerSound(mentionedPlayer);
                        mentionedPlayer.playSound(mentionedPlayer.getLocation(), notificationSound, 1.0f, 1.0f);

                        String mentionedPlayerPrefix = MakeABuilders.instance.getPlayerPrefix(mentionedPlayer);
                        String mentionedPlayerSuffix = MakeABuilders.instance.getPlayerSuffix(mentionedPlayer);

                        String formattedMention = "<yellow>@" + mentionedPlayerPrefix + mentionedPlayer.getName() + mentionedPlayerSuffix + "</yellow>";
                        message = message.replace(word, formattedMention);
                    } else {
                        String formattedMention = "<yellow>@" + mentionedPlayerName + "</yellow>";
                        message = message.replace(word, formattedMention);
                    }
                }
            }
        }

        String formattedMessage = TagFormatter.format(message, player);
        String prefix = MakeABuilders.instance.getPlayerPrefix(player);
        String suffix = MakeABuilders.instance.getPlayerSuffix(player);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String finalMessage = "<click:suggest_command:'/msg " + player.getName() + " '>"
                    + "<hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы отправить сообщение игроку " + prefix + playerName + suffix + ".'>"
                    + prefix + playerName + suffix + "</hover></click> > " + formattedMessage;

            Component playerMessage = MiniMessage.miniMessage().deserialize(finalMessage);
            onlinePlayer.sendMessage(playerMessage);
        }

        event.setCancelled(true);
    }
}
