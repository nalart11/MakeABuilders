package org.MakeACakeStudios.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.commands.VanishCommand;
import org.MakeACakeStudios.storage.PlayerDataStorage;
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
import java.util.regex.Pattern;


public class ChatListener implements Listener {

    private static final Pattern MINI_MESSAGE_TAG_PATTERN = Pattern.compile("<[^>]+>");

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#FF3D4D:#FCBDBD>С возвращением!</gradient>"));

        if (VanishCommand.isVanished(player)) {
            event.joinMessage(null); // ОТКЛЮЧАЕМ сообщение о входе
            return;
        }

        List<String> joinMessages = MakeABuilders.instance.config.getStringList("Messages.Join");
        String rawMessage = ChatUtils.getRandomMessage(joinMessages);

        String parsedMessage;
        if (rawMessage != null) {
            parsedMessage = rawMessage.replace("<player>", ChatUtils.getFormattedPlayerString(player.getName(), true));

            Component joinMessage = MiniMessage.miniMessage().deserialize(parsedMessage);

            event.joinMessage(joinMessage);

            List<String[]> playerMessages = MakeABuilders.instance.getMailStorage().getMessages(player.getName());
            if (!playerMessages.isEmpty()) {
                if (playerMessages.size() % 10 == 1) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<click:run_command:/mailcheck><hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы открыть непрочитанное сообщение.'><green>У вас есть <yellow>" + playerMessages.size() + "</yellow> непрочитанное сообщение.</green></hover></click>"));
                } else if (playerMessages.size() % 10 == 2 || playerMessages.size() % 10 == 3 || playerMessages.size() % 10 == 4) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<click:run_command:/mailcheck><hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы открыть непрочитанные сообщения.'><green>У вас есть <yellow>" + playerMessages.size() + "</yellow> непрочитанных сообщения.</green></hover></click>"));
                } else {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<click:run_command:/mailcheck><hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы открыть непрочитанные сообщения.'><green>У вас есть <yellow>" + playerMessages.size() + "</yellow> непрочитанных сообщений.</green></hover></click>"));
                }
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        List<String> quitMessages = MakeABuilders.instance.config.getStringList("Messages.Quit");
        String rawMessage = ChatUtils.getRandomMessage(quitMessages);

        if (VanishCommand.isVanished(player)) {
            event.quitMessage(null);
            return;
        }

        String parsedMessage;
        if (rawMessage != null) {
            parsedMessage = rawMessage.replace("<player>", ChatUtils.getFormattedPlayerString(player.getName(), true));

            Component quitMessage = MiniMessage.miniMessage().deserialize(parsedMessage);

            event.quitMessage(quitMessage);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        Component originalDeathMessage = event.deathMessage();

        if (originalDeathMessage != null) {
            Component formattedDeathMessage = originalDeathMessage
                    .replaceText(builder -> builder
                            .matchLiteral(player.getName())
                            .replacement(ChatUtils.getFormattedPlayerComponent(player))
                    );

            if (killer != null) {
                formattedDeathMessage = formattedDeathMessage.replaceText(builder -> builder
                        .matchLiteral(killer.getName())
                        .replacement(ChatUtils.getFormattedPlayerComponent(killer))
                );
            }

            event.deathMessage(formattedDeathMessage);
        }
    }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        Component originalMessage = event.message();

        if (originalMessage != null) {
            Component customAdvancementMessage = originalMessage.replaceText(builder ->
                    builder.matchLiteral(player.getName()).replacement(ChatUtils.getFormattedPlayerComponent(player))
            );

            event.message(customAdvancementMessage);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = ChatUtils.getFormattedPlayerString(player.getName(), true);
        String playerFooter = ChatUtils.getFormattedPlayerString(player.getName(), false);
        String message = event.getMessage();

        String muteStatus = PunishmentStorage.instance.checkMute(player.getName());

        if (!muteStatus.contains("не замьючен")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Вы замьючены и не можете отправлять сообщения.</red>"));
            event.setCancelled(true);
            return;
        }

        message = TagFormatter.format(message);
        message = ChatUtils.replaceLocationTag(player, message);
        message = ChatUtils.replaceMentions(player, message);

        if (AdminChat.isInAdminChat(player)) {
            AdminChat.sendAdminMessage(player, message);
            event.setCancelled(true);
            return;
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String finalMessage = "<click:run_command:'/profile " + player.getName() + "'>"
                    + "<hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы открыть профиль игрока " + playerFooter + ".'>"
                    + playerName + "</hover></click> > " + message;

            Component playerMessage = MiniMessage.miniMessage().deserialize(finalMessage);
            onlinePlayer.sendMessage(playerMessage);
        }

        event.setCancelled(true);
    }
}
